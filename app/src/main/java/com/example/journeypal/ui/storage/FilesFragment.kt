package com.example.journeypal.ui.storage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.journeypal.MainActivity
import com.example.journeypal.R
import com.example.journeypal.databinding.FragmentFilesBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

class FilesFragment : Fragment() {

    private var _binding: FragmentFilesBinding? = null
    private val binding get() = _binding!!

    private lateinit var fileAdapter: FileAdapter
    private val fileList = mutableListOf<File>()

    private val currentUserEmail: String?
        get() = getSecurePreferences().getString("secure_user_email", null)

    // File picker launcher
    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedFile(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()

        binding.uploadButton.setOnClickListener { openFilePicker() }
        binding.logoutButton.setOnClickListener { logoutUser() }

        // Ensure encryption key exists
        generateEncryptionKey()

        // Load user's files
        loadUserFiles()
    }

    private fun setupToolbar() {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Secure Files"
            setDisplayHomeAsUpEnabled(true)
        }

        // Display user email in subtitle
        binding.userEmailText.text = currentUserEmail ?: "Unknown User"
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(fileList, object : FileAdapter.FileActionListener {
            override fun onDelete(file: File) = deleteFile(file)
            override fun onDownload(file: File) = decryptAndSaveFile(file)
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = fileAdapter
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        openFileLauncher.launch(intent)
    }

    private fun handleSelectedFile(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = getFileNameFromUri(uri) ?: "file_${System.currentTimeMillis()}"

            // Create a temporary file
            val tempFile = File(requireContext().cacheDir, fileName)
            FileOutputStream(tempFile).use { output ->
                inputStream?.copyTo(output)
            }

            // Encrypt and save the file
            encryptAndSaveFile(tempFile)

            // Delete the temporary file
            tempFile.delete()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex("_display_name")
                if (displayNameIndex != -1) {
                    it.getString(displayNameIndex)
                } else null
            } else null
        }
    }

    private fun getUserDirectory(): File {
        val email = currentUserEmail ?: throw IllegalStateException("No user logged in")
        val userDir = File(requireContext().filesDir, email.toSha256())

        if (!userDir.exists()) {
            userDir.mkdirs()
        }

        return userDir
    }

    private fun loadUserFiles() {
        try {
            val userDir = getUserDirectory()
            val files = userDir.listFiles()?.filter { it.name.endsWith(".enc") } ?: emptyList()

            fileList.clear()
            fileList.addAll(files)
            fileAdapter.notifyDataSetChanged()

            // Update empty state view
            binding.emptyStateView.visibility = if (fileList.isEmpty()) View.VISIBLE else View.GONE

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to load files: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun encryptAndSaveFile(file: File) {
        try {
            val userDir = getUserDirectory()
            val outputFile = File(userDir, "${file.name}.enc")

            // Generate a random IV for this encryption
            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)

            // Get cipher
            val cipher = getCipher(Cipher.ENCRYPT_MODE, iv)

            // Encrypt the file
            FileInputStream(file).use { input ->
                FileOutputStream(outputFile).use { output ->
                    // Write IV at the beginning of the file
                    output.write(iv)

                    // Encrypt and write the rest of the file
                    CipherOutputStream(output, cipher).use { cipherOut ->
                        input.copyTo(cipherOut)
                    }
                }
            }

            // Update the UI
            fileList.add(outputFile)
            fileAdapter.notifyDataSetChanged()
            binding.emptyStateView.visibility = View.GONE

            Toast.makeText(requireContext(), "File encrypted and saved", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Encryption failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decryptAndSaveFile(encryptedFile: File) {
        try {
            // Create output directory if it doesn't exist
            val downloadsDir = File(requireContext().getExternalFilesDir(null), "Decrypted")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            // Prepare output file (remove .enc extension)
            val outputFileName = encryptedFile.name.removeSuffix(".enc")
            val outputFile = File(downloadsDir, outputFileName)

            // Read the file and decrypt
            FileInputStream(encryptedFile).use { input ->
                // Read the IV from the beginning of the file
                val iv = ByteArray(12)
                if (input.read(iv) != iv.size) {
                    throw Exception("Invalid file format. Could not read IV.")
                }

                // Initialize the cipher for decryption
                val cipher = getCipher(Cipher.DECRYPT_MODE, iv)

                // Decrypt the file
                FileOutputStream(outputFile).use { output ->
                    CipherInputStream(input, cipher).use { cipherIn ->
                        cipherIn.copyTo(output)
                    }
                }
            }

            Toast.makeText(
                requireContext(),
                "File decrypted and saved to ${outputFile.absolutePath}",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Decryption failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun deleteFile(file: File) {
        if (file.delete()) {
            fileList.remove(file)
            fileAdapter.notifyDataSetChanged()

            // Update empty state view
            binding.emptyStateView.visibility = if (fileList.isEmpty()) View.VISIBLE else View.GONE

            Toast.makeText(requireContext(), "File deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Failed to delete file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateEncryptionKey() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Check if key already exists
            if (!keyStore.containsAlias("file_encryption_key")) {
                val keyGenerator = KeyGenerator.getInstance(
                    "AES",
                    "AndroidKeyStore"
                )

                val keyGenSpec = android.security.keystore.KeyGenParameterSpec.Builder(
                    "file_encryption_key",
                    android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                            android.security.keystore.KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()

                keyGenerator.init(keyGenSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            // Fallback to using a secure key derived from the user's credentials
            // This is less secure but works on devices without keystore support
            Toast.makeText(requireContext(), "Using backup encryption method", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCipher(mode: Int, iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        try {
            // Try to use Android KeyStore
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val key = keyStore.getKey("file_encryption_key", null)

            if (mode == Cipher.ENCRYPT_MODE) {
                cipher.init(mode, key)
            } else {
                cipher.init(mode, key, GCMParameterSpec(128, iv))
            }
        } catch (e: Exception) {
            // Fallback to a key derived from user credentials
            val email = currentUserEmail ?: throw IllegalStateException("No user logged in")
            val derivedKey = deriveKeyFromEmail(email)
            val secretKeySpec = SecretKeySpec(derivedKey, "AES")

            if (mode == Cipher.ENCRYPT_MODE) {
                cipher.init(mode, secretKeySpec, GCMParameterSpec(128, iv))
            } else {
                cipher.init(mode, secretKeySpec, GCMParameterSpec(128, iv))
            }
        }

        return cipher
    }

    private fun deriveKeyFromEmail(email: String): ByteArray {
        // Simple key derivation - in production, use a proper KDF
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(email.toByteArray())
    }

    private fun logoutUser() {
        try {
            // Clear current user but keep registrations
            getSecurePreferences().edit()
                .remove("secure_user_email")
                .apply()

            Toast.makeText(requireContext(), "Successfully logged out", Toast.LENGTH_SHORT).show()

            // Start the login activity directly
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSecurePreferences() = try {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "secure_user_credentials",
            masterKey,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to regular preferences if encryption fails
        requireContext().getSharedPreferences("secure_user_credentials", Context.MODE_PRIVATE)
    }

    private fun String.toSha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(this.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
