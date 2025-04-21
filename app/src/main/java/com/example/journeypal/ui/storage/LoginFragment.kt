package com.example.journeypal.ui.storage

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.journeypal.R
import com.example.journeypal.databinding.FragmentLoginBinding
import org.mindrot.jbcrypt.BCrypt

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Secure preference constants
    private val PREFERENCE_NAME = "secure_user_credentials"
    private val KEY_EMAIL = "secure_user_email"
    private val KEY_PASSWORD_HASH = "secure_user_password_hash"

    // Tracking login vs register state
    private var isInLoginMode = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if already logged in
        if (isUserLoggedIn()) {
            navigateToFiles()
            return
        }

        // Set initial UI state to login
        updateUiForLoginMode()

        // Button click handlers
        binding.loginButton.setOnClickListener {
            if (isInLoginMode) {
                handleLogin()
            } else {
                handleRegistration()
            }
        }

        binding.switchModeButton.setOnClickListener {
            isInLoginMode = !isInLoginMode
            updateUiForLoginMode()
        }
    }

    private fun updateUiForLoginMode() {
        if (isInLoginMode) {
            binding.loginButton.text = "Login"
            binding.switchModeButton.text = "Create an Account"
            binding.titleTextView.text = "Login"
            binding.confirmPasswordLayout.visibility = View.GONE
        } else {
            binding.loginButton.text = "Register"
            binding.switchModeButton.text = "Back to Login"
            binding.titleTextView.text = "Create Account"
            binding.confirmPasswordLayout.visibility = View.VISIBLE
        }
    }

    private fun handleLogin() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Email and password required", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val securePrefs = getSecurePreferences()

            // Get all stored emails
            val emailSet = securePrefs.getStringSet("all_emails", setOf()) ?: setOf()

            if (!emailSet.contains(email)) {
                Toast.makeText(requireContext(), "Account not found", Toast.LENGTH_SHORT).show()
                return
            }

            // Get user-specific stored hash
            val storedPasswordHash = securePrefs.getString("${email}_password_hash", null)

            if (storedPasswordHash == null || !BCrypt.checkpw(password, storedPasswordHash)) {
                Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
                return
            }

            // Save current logged in user
            with(securePrefs.edit()) {
                putString(KEY_EMAIL, email)
                apply()
            }

            Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToFiles()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleRegistration() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()

        // Validation
        if (!isValidEmail(email)) {
            binding.emailEditText.error = "Invalid email format"
            return
        }

        if (password.length < 8) {
            binding.passwordEditText.error = "Password must be at least 8 characters"
            return
        }

        if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords don't match"
            return
        }

        try {
            val securePrefs = getSecurePreferences()

            // Check if email already exists
            val emailSet = securePrefs.getStringSet("all_emails", setOf()) ?: setOf()
            if (emailSet.contains(email)) {
                Toast.makeText(requireContext(), "Email already registered", Toast.LENGTH_SHORT).show()
                return
            }

            // Save new user
            val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12))
            val updatedEmails = emailSet.toMutableSet()
            updatedEmails.add(email)

            with(securePrefs.edit()) {
                putStringSet("all_emails", updatedEmails)
                putString("${email}_password_hash", passwordHash)
                putString(KEY_EMAIL, email) // Log them in
                apply()
            }

            Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
            navigateToFiles()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Registration error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getSecurePreferences() = try {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            PREFERENCE_NAME,
            masterKey,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to regular preferences if encryption fails
        requireContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    private fun isUserLoggedIn(): Boolean {
        return try {
            val email = getSecurePreferences().getString(KEY_EMAIL, null)
            !email.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun navigateToFiles() {
        findNavController().navigate(R.id.action_loginFragment_to_filesFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}