package com.example.journeypal.ui.storage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.journeypal.databinding.FragmentStorageBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class StorageFragment : Fragment() {

    private var _binding: FragmentStorageBinding? = null
    private val binding get() = _binding!!

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStorageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initializeButtons()

        return root
    }

    private fun initializeButtons() {
        val emailInput: EditText = binding.emailInput
        val passwordInput: EditText = binding.passwordInput
        val loginButton: Button = binding.loginButton
        val registerButton: Button = binding.registerButton
        val logoutButton: Button = binding.logoutButton
        val uploadButton: Button = binding.uploadButton
        val viewFilesButton: Button = binding.viewFilesButton

        loginButton.setOnClickListener { handleLogin(emailInput, passwordInput) }
        registerButton.setOnClickListener { handleRegistration(emailInput, passwordInput) }
        logoutButton.setOnClickListener { handleLogout(emailInput, passwordInput) }
        uploadButton.setOnClickListener { openFilePicker() }
        viewFilesButton.setOnClickListener { listFiles() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri: Uri? = data?.data
            uri?.let { uploadEncryptedFile(it) }
        }
    }

    // Handle login
    private fun handleLogin(emailInput: EditText, passwordInput: EditText) {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            showMessage("Login successful")
        } else {
            showMessage("Please enter email and password")
        }
    }

    // Handle registration
    private fun handleRegistration(emailInput: EditText, passwordInput: EditText) {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            showMessage("Registration successful")
        } else {
            showMessage("Please enter email and password")
        }
    }

    // Handle logout
    private fun handleLogout(emailInput: EditText, passwordInput: EditText) {
        emailInput.text.clear()
        passwordInput.text.clear()
        showMessage("Logged out")
    }

    // Show Toast message
    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Open file picker
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
        filePickerLauncher.launch(intent)
    }

    // Encrypt and upload file
    private fun uploadEncryptedFile(uri: Uri) {
        val context = requireContext()
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return
            val encryptedFileName = uri.lastPathSegment ?: "encrypted_file"
            val encryptedFile = File(context.filesDir, encryptedFileName)
            val cipher = getCipher(Cipher.ENCRYPT_MODE)

            CipherInputStream(inputStream, cipher).use { cipherInputStream ->
                FileOutputStream(encryptedFile).use { fileOutputStream ->
                    cipherInputStream.copyTo(fileOutputStream)
                }
            }

            showMessage("File uploaded securely")
        } catch (e: Exception) {
            Log.e("StorageFragment", "Error encrypting file", e)
            showMessage("Error uploading file: ${e.message}")
        }
    }

    // Generate Cipher instance with Android Keystore
    private fun getCipher(mode: Int): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = getSecretKey()
        cipher.init(mode, secretKey)
        return cipher
    }

    // Get Secret Key from Android Keystore
    private fun getSecretKey(): SecretKey {
        val key = keyStore.getKey("key_alias", null) ?: generateKey()
        return key as SecretKey
    }

    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder("key_alias", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }


    // List files in internal storage
    private fun listFiles() {
        val context = requireContext()
        val files = context.filesDir.listFiles()
        if (files.isNullOrEmpty()) {
            showMessage("No files found")
        } else {
            files.forEach { Log.d("StorageFragment", "File: ${it.name}") }
        }
    }

    // Download file and decrypt it
    private fun downloadFile(fileName: String) {
        val context = requireContext()
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            showMessage("File not found")
            return
        }

        try {
            val cipher = getCipher(Cipher.DECRYPT_MODE)
            FileInputStream(file).use { fileInputStream ->
                CipherInputStream(fileInputStream, cipher).use { cipherInputStream ->
                    val outputPath = File(context.getExternalFilesDir(null), fileName)
                    FileOutputStream(outputPath).use { outputStream ->
                        cipherInputStream.copyTo(outputStream)
                    }
                    showMessage("File downloaded securely")
                }
            }
        } catch (e: Exception) {
            Log.e("StorageFragment", "Error decrypting file", e)
            showMessage("Error downloading file: ${e.message}")
        }
    }

    // Delete file from internal storage
    private fun deleteFile(fileName: String) {
        val context = requireContext()
        val file = File(context.filesDir, fileName)
        if (file.exists() && file.delete()) {
            showMessage("File deleted")
        } else {
            showMessage("Failed to delete file")
        }
    }
}

