package com.example.journeypal

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.example.journeypal.ui.camera.CameraFragment
import org.junit.Assert.assertEquals
import org.junit.Test

class CameraTest {

    @Test
    fun testRotateBitmap_90Degrees() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val original = Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888)
        val fragment = CameraFragment()
        val rotated = fragment.rotateBitmap(original, 90)
        assertEquals(50, rotated.width)
        assertEquals(100, rotated.height)
    }
}
