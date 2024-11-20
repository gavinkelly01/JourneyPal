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
import androidx.camera.core.TorchState
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!OpenCVLoader.initDebug()) {
            Log.e("CameraFragment", "OpenCV initialization failed")
        } else {
            Log.d("CameraFragment", "OpenCV initialized successfully")
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
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(context, "Camera permission is required to use this feature", Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera(retryCount: Int = 3) {
        if (retryCount <= 0) {
            Toast.makeText(context, "Camera initialization failed after retries.", Toast.LENGTH_LONG).show()
            return
        }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(cameraProvider)
            } catch (e: Exception) {
                Log.e("CameraFragment", "Retrying camera initialization: ${retryCount - 1} attempts left")
                startCamera(retryCount - 1)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        try {
            val cameraSelector = CameraSelector.Builder().apply {
                requireLensFacing(lensFacing)
            }.build()

            val rotation = requireActivity().windowManager.defaultDisplay.rotation
            val preview = Preview.Builder()
                .setTargetRotation(rotation)
                .build()

            preview.setSurfaceProvider(previewView.surfaceProvider)

            val imageAnalyzer = ImageAnalysis.Builder().build()
            imageAnalyzer.setAnalyzer(cameraExecutor, { image -> analyzeImage(image) })

            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        } catch (e: Exception) {
            Log.e("CameraFragment", "Error binding camera use cases: ${e.message}")
            Toast.makeText(context, "Camera binding failed", Toast.LENGTH_LONG).show()
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
        try {
            val bitmap = imageProxyToBitmap(image)
            if (bitmap != null) {
                scanForHiddenCamera(bitmap)
            } else {
                Log.e("CameraFragment", "Bitmap is null, skipping analysis")
            }
        } catch (e: Exception) {
            Log.e("CameraFragment", "Error analyzing image: ${e.message}")
        } finally {
            image.close()
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
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
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, byteArrayOutputStream)
        val jpegData = byteArrayOutputStream.toByteArray()
        return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
    }

    private fun scanForHiddenCamera(bitmap: Bitmap) {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
        Imgproc.GaussianBlur(mat, mat, Size(15.0, 15.0), 0.0)
        val circles = Mat()
        Imgproc.HoughCircles(
            mat, circles, Imgproc.CV_HOUGH_GRADIENT, 1.0, 30.0, 100.0, 30.0, 10, 50
        )

        if (circles.cols() > 0) {
            val circle = circles.get(0, 0)
            if (circle != null) {
                val centerX = circle[0].toInt()
                val centerY = circle[1].toInt()
                val radius = circle[2].toInt()
                Imgproc.circle(mat, org.opencv.core.Point(centerX.toDouble(), centerY.toDouble()), radius, Scalar(0.0, 255.0, 0.0), 4)  // Lime green ring
                Log.d("CameraFragment", "Hidden camera detected at ($centerX, $centerY) with radius $radius")
            }
        }

        Utils.matToBitmap(mat, bitmap)

        val rotatedBitmap = rotateBitmap(bitmap, 90) // Rotate 90 degrees right

        imageView.setImageBitmap(rotatedBitmap)
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
