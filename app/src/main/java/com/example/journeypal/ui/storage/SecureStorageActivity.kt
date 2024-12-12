package com.example.journeypal.ui.storage

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.journeypal.databinding.FragmentSecurestorageBinding
import com.example.journeypal.ui.storage.FileAdapter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

class SecureStorageActivity : AppCompatActivity() {

    private lateinit var binding: FragmentSecurestorageBinding
    private lateinit var encryptedFile: File
    private val fileList = mutableListOf<File>()
    private lateinit var fileAdapter: FileAdapter

    private val openFileLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { handleFileUri(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSecurestorageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        generateKey()

        fileAdapter = FileAdapter(fileList, { file -> deleteFile(file) }, { file -> downloadFile(file) })
        binding.fileRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.fileRecyclerView.adapter = fileAdapter

        binding.uploadButton.setOnClickListener { openFilePicker() }
        binding.downloadButton.setOnClickListener {
            if (fileList.isNotEmpty()) {
                val fileToDownload = fileList[0]
                downloadFile(fileToDownload)
            } else {
                showToast("No files available to download.")
            }
        }

    }

    private fun generateKey() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (keyStore.containsAlias("storage_key")) return

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.apply {
                init(
                    KeyGenParameterSpec.Builder("storage_key", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                )
            }
            keyGenerator.generateKey()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openFilePicker() {
        if (isPermissionGranted()) {
            openFileLauncher.launch("*/*")
        } else {
            requestPermissions()
        }
    }

    private fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSION
            )
        } else {
            openFilePicker()
        }
    }

    private fun handleFileUri(uri: Uri) {
        try {
            val file = resolveUriToFile(uri)
            if (file.exists()) {
                encryptAndUploadFile(file)
            } else {
                showToast("File not found or cannot be accessed.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error processing file: ${e.message}")
        }
    }

    private fun resolveUriToFile(uri: Uri): File {
        val contentResolver: ContentResolver = contentResolver
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload_", ".tmp", filesDir)
            inputStream?.copyTo(FileOutputStream(tempFile))
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to resolve URI to file.")
        }
    }

    private fun encryptAndUploadFile(file: File) {
        try {
            encryptFile(file)
            showToast("File uploaded and encrypted successfully!")
            fileList.add(file)
            fileAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error uploading file: ${e.message}")
        }
    }

    private fun encryptFile(file: File) {
        try {
            val cipher = getCipher(Cipher.ENCRYPT_MODE)
            val fileInputStream = FileInputStream(file)
            encryptedFile = File(filesDir, "${file.name}.enc")
            val fileOutputStream = FileOutputStream(encryptedFile)

            val iv = cipher.iv
            fileOutputStream.write(iv)

            val cipherOutputStream = CipherOutputStream(fileOutputStream, cipher)
            val buffer = ByteArray(1024)
            var len: Int
            while (fileInputStream.read(buffer).also { len = it } != -1) {
                cipherOutputStream.write(buffer, 0, len)
            }

            cipherOutputStream.close()
            fileInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to encrypt the file.")
        }
    }

    private fun getCipher(mode: Int): Cipher {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val key = keyStore.getKey("storage_key", null)
            if (key == null) {
                throw Exception("Encryption key not found.")
            }

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(mode, key)
            return cipher
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to initialize cipher.")
        }
    }

    private fun downloadFile(file: File) {
        if (fileList.isEmpty()) {
            showToast("No files available to download.")
            return
        }

        val fileToDownload = fileList[0]
        try {
            val decryptedFile = decryptFile(fileToDownload)

            if (isWritePermissionGranted()) {
                saveDecryptedFile(decryptedFile)
                showToast("File decrypted and saved successfully!")
            } else {
                requestWritePermissions()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error downloading file: ${e.message}")
        }
    }

    private fun isWritePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestWritePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSION
            )
        } else {
            showToast("Storage permission required.")
        }
    }

    private fun decryptFile(file: File): File {
        val fileInputStream = FileInputStream(file)
        val iv = ByteArray(12)
        fileInputStream.read(iv)

        val cipher = getCipher(Cipher.DECRYPT_MODE, iv)
        val decryptedFile = File(filesDir, "decrypted_${file.name}")
        val fileOutputStream = FileOutputStream(decryptedFile)

        val cipherInputStream = CipherInputStream(fileInputStream, cipher)
        val buffer = ByteArray(1024)
        var len: Int
        while (cipherInputStream.read(buffer).also { len = it } != -1) {
            fileOutputStream.write(buffer, 0, len)
        }

        cipherInputStream.close()
        fileInputStream.close()
        fileOutputStream.close()

        return decryptedFile
    }

    private fun getCipher(mode: Int, iv: ByteArray? = null): Cipher {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val key = keyStore.getKey("storage_key", null)
            if (key == null) {
                throw Exception("Encryption key not found.")
            }

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            if (iv != null) {
                val gcmParameterSpec = GCMParameterSpec(128, iv)
                cipher.init(mode, key, gcmParameterSpec)
            } else {
                cipher.init(mode, key)
            }
            return cipher
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to initialize cipher.")
        }
    }

    private fun saveDecryptedFile(decryptedFile: File) {
        val destination = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), decryptedFile.name)
        try {
            decryptedFile.copyTo(destination, overwrite = true)
            decryptedFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error saving decrypted file.")
        }
    }

    private fun deleteFile(file: File) {
        try {
            file.delete()
            fileList.remove(file)
            fileAdapter.notifyDataSetChanged()
            showToast("File deleted.")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error deleting file.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 1
    }
}
