package com.example.journeypal.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
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
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
    private var currentToast: Toast? = null

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
        toggleCameraButton.setOnClickListener { toggleCamera() }
        flashlightButton.setOnClickListener { toggleFlashlight() }
        requestCameraPermission()

        return rootView
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
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(cameraProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization error: ${e.message}")
                Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        try {
            cameraProvider.unbindAll()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
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
                    Log.e(TAG, "Image analysis error: ${e.message}")
                } finally {
                    image.close()
                }
            }
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed: ${e.message}")
        }
    }

    private fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera()
    }

    private fun toggleFlashlight() {
        camera?.cameraControl?.enableTorch(!isFlashlightOn)
        isFlashlightOn = !isFlashlightOn
    }

    private fun analyzeImage(image: ImageProxy) {
        val bitmap = imageProxyToBitmap(image)
        if (bitmap != null) {
            detectPotentialCameras(bitmap)
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        try {
            val yBuffer = image.planes[0].buffer
            val uBuffer = image.planes[1].buffer
            val vBuffer = image.planes[2].buffer
            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()
            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting image: ${e.message}")
            return null
        }
    }

    private fun detectPotentialCameras(bitmap: Bitmap) {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        val resultMat = mat.clone()

        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY)
        val normalizedMat = Mat()
        Core.normalize(grayMat, normalizedMat, 0.0, 255.0, Core.NORM_MINMAX)
        Imgproc.GaussianBlur(normalizedMat, normalizedMat, Size(5.0, 5.0), 0.0)
        val darkMask = Mat()
        Imgproc.threshold(normalizedMat, darkMask, 50.0, 255.0, Imgproc.THRESH_BINARY_INV)
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(darkMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        val detectedCircles = mutableListOf<Pair<Point, Double>>()
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area in 20.0..2000.0) {
                val contour2f = MatOfPoint2f(*contour.toArray())
                val center = Point()
                val radius = FloatArray(1)
                Imgproc.minEnclosingCircle(contour2f, center, radius)
                val perimeter = Imgproc.arcLength(contour2f, true)
                val circularity = 4 * Math.PI * area / (perimeter * perimeter)
                if (circularity > 0.7) {
                    detectedCircles.add(Pair(center, radius[0].toDouble()))
                    Log.d(TAG, "Potential camera lens detected: center=$center, radius=${radius[0]}, circularity=$circularity")
                }
            }
        }

        var detectionCount = 0
        for ((center, radius) in detectedCircles) {
            Imgproc.circle(resultMat, center, radius.toInt(), Scalar(0.0, 255.0, 0.0), 2)
            Imgproc.line(
                resultMat,
                Point(center.x - radius, center.y),
                Point(center.x + radius, center.y),
                Scalar(255.0, 0.0, 0.0), 2
            )
            Imgproc.line(
                resultMat,
                Point(center.x, center.y - radius),
                Point(center.x, center.y + radius),
                Scalar(255.0, 0.0, 0.0), 2
            )
            detectionCount++
        }

        if (detectionCount > 0) {
            activity?.runOnUiThread {
                val message = when (detectionCount) {
                    1 -> "1 potential hidden camera detected"
                    else -> "$detectionCount potential hidden cameras detected"
                }
                currentToast?.cancel()
                currentToast = Toast.makeText(context, message, Toast.LENGTH_LONG)
                currentToast?.show()
            }
        }
        val resultBitmap = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(resultMat, resultBitmap)
        val rotatedBitmap = rotateBitmap(resultBitmap, 90)

        activity?.runOnUiThread {
            imageView.setImageBitmap(rotatedBitmap)
        }
        mat.release()
        resultMat.release()
        grayMat.release()
        normalizedMat.release()
        darkMask.release()
        hierarchy.release()
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Show Toast when fragment is resumed
    override fun onResume() {
        super.onResume()
        // Show a toast indicating the fragment is active (if necessary)
        Toast.makeText(context, "Camera Fragment is active", Toast.LENGTH_SHORT).show()
    }

    // Cancel the toast when the fragment is paused or destroyed
    override fun onPause() {
        super.onPause()
        currentToast?.cancel()  // Dismiss the current toast when the fragment is paused
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}
