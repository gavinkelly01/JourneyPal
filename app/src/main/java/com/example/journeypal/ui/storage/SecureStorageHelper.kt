package com.example.journeypal.ui.storage

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.GCMParameterSpec

// SecureStorageHelper.kt
class SecureStorageHelper(private val context: Context) {

    fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun encryptFile(file: File, directory: File): File {
        val cipher = getCipher(Cipher.ENCRYPT_MODE)
        val encryptedFile = File(directory, "${file.name}.enc")
        val iv = cipher.iv

        FileInputStream(file).use { input ->
            FileOutputStream(encryptedFile).use { output ->
                output.write(iv)
                CipherOutputStream(output, cipher).use { cipherOut ->
                    input.copyTo(cipherOut)
                }
            }
        }
        return encryptedFile
    }

    fun decryptFile(encryptedFile: File): File {
        FileInputStream(encryptedFile).use { input ->
            val iv = ByteArray(12)
            if (input.read(iv) != iv.size) {
                throw Exception("Invalid IV length. Possible file corruption.")
            }

            val cipher = getCipher(Cipher.DECRYPT_MODE, iv)
            val decryptedFile = File(context.filesDir, encryptedFile.name.removeSuffix(".enc"))
            FileOutputStream(decryptedFile).use { output ->
                CipherInputStream(input, cipher).use { cipherIn ->
                    cipherIn.copyTo(output)
                }
            }
            return decryptedFile
        }
    }

    private fun getCipher(mode: Int, iv: ByteArray? = null): Cipher {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val secretKey = keyStore.getKey("storage_key", null) ?: throw Exception("No key found.")

        return Cipher.getInstance("AES/GCM/NoPadding").apply {
            if (mode == Cipher.ENCRYPT_MODE) {
                init(mode, secretKey)
            } else {
                requireNotNull(iv) { "IV must not be null for decryption." }
                init(mode, secretKey, GCMParameterSpec(128, iv))
            }
        }
    }
}
