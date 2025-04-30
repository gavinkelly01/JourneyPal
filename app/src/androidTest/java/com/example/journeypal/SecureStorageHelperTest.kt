package com.example.journeypal

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.journeypal.ui.storage.SecureStorageHelper
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import java.io.File

class SecureStorageHelperTest {

    private lateinit var context: Context
    private lateinit var helper: SecureStorageHelper

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        helper = SecureStorageHelper(context)
    }

    @Test
    fun testIsPermissionGranted_QOrAbove_ReturnsTrue() {
        val granted = helper.isPermissionGranted()
        assertTrue(granted)
    }

    @Test
    fun testEncryptAndDecryptFile_PreservesContent() {
        val originalText = "Test secret!"
        val plainFile = File.createTempFile("plain", ".txt").apply { writeText(originalText) }
        val dir = File(context.filesDir, "test").apply { mkdirs() }
        val encrypted = helper.encryptFile(plainFile, dir)
        assertTrue(encrypted.exists())
        val decrypted = helper.decryptFile(encrypted)
        assertEquals(originalText, decrypted.readText())
    }
}
