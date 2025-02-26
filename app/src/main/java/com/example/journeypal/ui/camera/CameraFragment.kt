package com.example.journeypal.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.journeypal.R
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var imageView: ImageView
    private lateinit var toggleCameraButton: ImageButton
    private lateinit var flashlightButton: ImageButton
    private var camera: Camera? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private val CAMERA_REQUEST_CODE = 1001
    private var isFlashlightOn = false
    private var useLegacyCamera = false


    private var cameraManager: CameraManager? = null
    private var cameraDevice: android.hardware.camera2.CameraDevice? = null

    companion object {
        private const val TAG = "CameraFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed")
        } else {
            Log.d(TAG, "OpenCV initialized successfully")
        }

        val rootView = inflater.inflate(R.layout.fragment_camera, container, false)

        previewView = rootView.findViewById(R.id.camera_preview)
        imageView = rootView.findViewById(R.id.image_view)
        toggleCameraButton = rootView.findViewById(R.id.toggle_camera_button)
        flashlightButton = rootView.findViewById(R.id.flashlight_button)

        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        toggleCameraButton.setOnClickListener { toggleCamera() }
        flashlightButton.setOnClickListener { toggleFlashlight() }
        checkCameraAvailability()

        return rootView
    }

    private fun checkCameraAvailability() {
        try {
            val cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIds = cameraManager.cameraIdList

            if (cameraIds.isEmpty()) {
                Log.e(TAG, "No cameras found on device")
                Toast.makeText(context, "No camera detected on this device", Toast.LENGTH_LONG).show()
                return
            }

            Log.d(TAG, "Found ${cameraIds.size} cameras: ${cameraIds.joinToString()}")
            requestCameraPermission()

        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to access camera: ${e.message}")
            Toast.makeText(context, "Camera access error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(context, "Camera permission is required to use this feature", Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera(retryCount: Int = 3) {
        if (retryCount <= 0) {
            Log.e(TAG, "Camera initialization failed after retries. Trying fallback...")
            if (!useLegacyCamera) {
                useLegacyCamera = true
                startLegacyCamera()
            } else {
                Toast.makeText(context, "Camera initialization failed. Please restart the app.", Toast.LENGTH_LONG).show()
            }
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        try {
            cameraProviderFuture.get(2000, TimeUnit.MILLISECONDS) // Add timeout to prevent hanging
        } catch (e: Exception) {
            Log.e(TAG, "Camera provider future timed out: ${e.message}")
            useLegacyCamera = true
            startLegacyCamera()
            return
        }

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val hasCamera = when (lensFacing) {
                    CameraSelector.LENS_FACING_BACK ->
                        cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                    CameraSelector.LENS_FACING_FRONT ->
                        cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                    else -> false
                }

                if (!hasCamera) {
                    Log.w(TAG, "Requested camera not available, trying alternative")
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                }

                bindCameraUseCases(cameraProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization error: ${e.message}")
                if (e.message?.contains("No available camera") == true ||
                    e.message?.contains("Expected camera missing") == true) {
                    useLegacyCamera = true
                    startLegacyCamera()
                } else {
                    Log.e(TAG, "Retrying camera initialization: ${retryCount - 1} attempts left")
                    startCamera(retryCount - 1)
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startLegacyCamera() {
        Log.d(TAG, "Attempting to use Camera2 API directly as fallback")
        try {
            val cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIds = cameraManager.cameraIdList

            if (cameraIds.isEmpty()) {
                Toast.makeText(context, "No cameras available on this device", Toast.LENGTH_LONG).show()
                return
            }

            var cameraId = cameraIds[0]
            for (id in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

                if (facing == lensFacing) {
                    cameraId = id
                    break
                }
            }

            Toast.makeText(context, "Using alternative camera system", Toast.LENGTH_SHORT).show()

            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.photo_camera_24px)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Legacy camera initialization failed: ${e.message}")
            Toast.makeText(context, "Camera initialization failed. Please check device permissions.", Toast.LENGTH_LONG).show()
        }
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        try {
            cameraProvider.unbindAll()

            val cameraSelector = CameraSelector.Builder().apply {
                requireLensFacing(lensFacing)
            }.build()

            val rotation = requireActivity().windowManager.defaultDisplay.rotation
            val preview = Preview.Builder()
                .setTargetRotation(rotation)
                .build()

            preview.setSurfaceProvider(previewView.surfaceProvider)

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalyzer.setAnalyzer(cameraExecutor) { image ->
                try {
                    analyzeImage(image)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in image analysis: ${e.message}")
                    image.close()
                }
            }

            try {
                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                Log.d(TAG, "Camera bound successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed: ${e.message}")
                Toast.makeText(context, "Camera initialization error", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding camera use cases: ${e.message}")
            Toast.makeText(context, "Camera binding failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        if (useLegacyCamera) {
            startLegacyCamera()
        } else {
            startCamera()
        }
    }

    private fun toggleFlashlight() {
        if (useLegacyCamera) {
            Toast.makeText(context, "Flashlight not available in fallback mode", Toast.LENGTH_SHORT).show()
            return
        }
        camera?.cameraControl?.enableTorch(!isFlashlightOn)
        isFlashlightOn = !isFlashlightOn
    }

    private fun analyzeImage(image: ImageProxy) {
        try {
            val bitmap = imageProxyToBitmap(image)
            if (bitmap != null) {
                scanForHiddenCamera(bitmap)
            } else {
                Log.e(TAG, "Bitmap is null, skipping analysis")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing image: ${e.message}")
        } finally {
            image.close()
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        try {
            val yuvPlanes = image.planes
            val ySize = yuvPlanes[0].buffer.remaining()
            val uSize = yuvPlanes[1].buffer.remaining()
            val vSize = yuvPlanes[2].buffer.remaining()

            val yuvData = ByteArray(ySize + uSize + vSize)

            yuvPlanes[0].buffer.get(yuvData, 0, ySize)
            yuvPlanes[1].buffer.get(yuvData, ySize, uSize)
            yuvPlanes[2].buffer.get(yuvData, ySize + uSize, vSize)

            val nv21Data = ByteArray(ySize + uSize + vSize)
            System.arraycopy(yuvData, 0, nv21Data, 0, ySize)

            var uvIndex = ySize
            for (i in 0 until uSize / 2) {
                nv21Data[uvIndex++] = yuvData[ySize + i * 2 + 1]
                nv21Data[uvIndex++] = yuvData[ySize + i * 2]
            }

            val yuvImage = YuvImage(nv21Data, ImageFormat.NV21, image.width, image.height, null)
            val byteArrayOutputStream = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, byteArrayOutputStream)
            val jpegData = byteArrayOutputStream.toByteArray()
            return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting image: ${e.message}")
            return null
        }
    }

    private fun scanForHiddenCamera(bitmap: Bitmap) {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val originalMat = mat.clone()

        Imgproc.GaussianBlur(mat, mat, Size(11.0, 11.0), 0.0)

        val hsvMat = Mat()
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGB2HSV)

        val brightMask = Mat()
        Core.inRange(hsvMat, Scalar(0.0, 0.0, 220.0), Scalar(180.0, 30.0, 255.0), brightMask)

        val irMask = Mat()
        Core.inRange(hsvMat, Scalar(0.0, 0.0, 200.0), Scalar(180.0, 20.0, 255.0), irMask)

        val redMask1 = Mat()
        val redMask2 = Mat()
        Core.inRange(hsvMat, Scalar(0.0, 70.0, 50.0), Scalar(10.0, 255.0, 255.0), redMask1)
        Core.inRange(hsvMat, Scalar(160.0, 70.0, 50.0), Scalar(180.0, 255.0, 255.0), redMask2)

        val combinedMask = Mat()
        Core.bitwise_or(brightMask, irMask, combinedMask)
        val redMask = Mat()
        Core.bitwise_or(redMask1, redMask2, redMask)
        Core.bitwise_or(combinedMask, redMask, combinedMask)

        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(5.0, 5.0))
        Imgproc.morphologyEx(combinedMask, combinedMask, Imgproc.MORPH_OPEN, kernel)
        Imgproc.morphologyEx(combinedMask, combinedMask, Imgproc.MORPH_CLOSE, kernel)

        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(combinedMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val debugOverlay = Mat.zeros(combinedMask.size(), CvType.CV_8UC3)
        val detectedPoints = mutableListOf<Point>()
        val detectedRadii = mutableListOf<Float>()

        for (contour in contours) {
            val contourArea = Imgproc.contourArea(contour)
            if (contourArea > 50) {
                val contour2f = MatOfPoint2f(*contour.toArray())
                val center = Point()
                val radius = FloatArray(1)
                Imgproc.minEnclosingCircle(contour2f, center, radius)
                val perimeter = Imgproc.arcLength(contour2f, true)
                val circularity = 4 * Math.PI * contourArea / (perimeter * perimeter)
                val hull = MatOfInt()
                Imgproc.convexHull(contour, hull)
                val hullPoints = ArrayList<Point>()
                val hullIndices = hull.toArray()
                val contourPoints = contour.toArray()
                for (index in hullIndices) {
                    hullPoints.add(contourPoints[index])
                }

                val hullMat = MatOfPoint()
                hullMat.fromList(hullPoints)
                val hullArea = Imgproc.contourArea(hullMat)
                val convexity = contourArea / hullArea
                val boundingRect = Imgproc.boundingRect(contour)
                val aspectRatio = boundingRect.width.toDouble() / boundingRect.height

                if ((circularity > 0.7 && aspectRatio in 0.7..1.3 && convexity > 0.8) ||
                    (circularity > 0.5 && radius[0] < 15 && convexity > 0.9)) {
                    detectedPoints.add(center)
                    detectedRadii.add(radius[0])
                    Imgproc.drawContours(debugOverlay, listOf(contour), 0, Scalar(0.0, 255.0, 0.0), 2)
                    Imgproc.circle(debugOverlay, center, 2, Scalar(0.0, 0.0, 255.0), -1)
                    Log.d(TAG, "Potential hidden camera lens detected at $center with " +
                            "radius ${radius[0]}, circularity $circularity, convexity $convexity, aspect ratio $aspectRatio")
                }
            }
        }

        val detectionGroups = mutableListOf<MutableList<Int>>()
        val groupedIndices = BooleanArray(detectedPoints.size) { false }
        for (i in detectedPoints.indices) {
            if (groupedIndices[i]) continue
            val group = mutableListOf<Int>()
            group.add(i)
            groupedIndices[i] = true

            for (j in i + 1 until detectedPoints.size) {
                if (!groupedIndices[j] &&
                    Math.sqrt(Math.pow(detectedPoints[i].x - detectedPoints[j].x, 2.0) +
                            Math.pow(detectedPoints[i].y - detectedPoints[j].y, 2.0)) < 30) {
                    group.add(j)
                    groupedIndices[j] = true
                }
            }

            if (group.size > 0) {
                detectionGroups.add(group)
            }
        }

        var detectedCount = 0
        for (group in detectionGroups) {
            if (group.size >= 1) {
                var sumX = 0.0
                var sumY = 0.0
                var maxRadius = 0f

                for (idx in group) {
                    sumX += detectedPoints[idx].x
                    sumY += detectedPoints[idx].y
                    if (detectedRadii[idx] > maxRadius) {
                        maxRadius = detectedRadii[idx]
                    }
                }

                val avgCenter = Point(sumX / group.size, sumY / group.size)
                val finalRadius = Math.max(maxRadius * 1.2f, 15f)
                Imgproc.circle(originalMat, avgCenter, finalRadius.toInt(), Scalar(0.0, 255.0, 0.0), 3)
                Imgproc.line(originalMat,
                    Point(avgCenter.x - finalRadius, avgCenter.y),
                    Point(avgCenter.x + finalRadius, avgCenter.y),
                    Scalar(255.0, 0.0, 0.0), 2)
                Imgproc.line(originalMat,
                    Point(avgCenter.x, avgCenter.y - finalRadius),
                    Point(avgCenter.x, avgCenter.y + finalRadius),
                    Scalar(255.0, 0.0, 0.0), 2)

                detectedCount++
            }
        }

        if (detectedCount > 0) {
            activity?.runOnUiThread {
                val message = if (detectedCount == 1) {
                    "1 potential hidden camera detected!"
                } else {
                    "$detectedCount potential hidden cameras detected!"
                }
                val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(500)
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }

        Utils.matToBitmap(originalMat, bitmap)
        val rotatedBitmap = rotateBitmap(bitmap, 90)
        activity?.runOnUiThread {
            imageView.setImageBitmap(rotatedBitmap)
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}
