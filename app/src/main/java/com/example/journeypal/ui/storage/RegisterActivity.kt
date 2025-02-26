package com.example.journeypal.ui.storage

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.journeypal.R
import org.mindrot.jbcrypt.BCrypt

class RegisterActivity : AppCompatActivity() {

    // Registration views
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button

    // Login views
    private lateinit var loginEmailInput: EditText
    private lateinit var loginPasswordInput: EditText
    private lateinit var loginButton: Button

    // Keys and preference name for secure storage
    private val PREFERENCE_NAME = "secure_user_credentials"
    private val KEY_EMAIL = "secure_user_email"
    private val KEY_PASSWORD_HASH = "secure_user_password_hash"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize registration views
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        registerButton = findViewById(R.id.registerButton)

        // Initialize login views (initially hidden in the layout)
        loginEmailInput = findViewById(R.id.emailInputLayout)
        loginPasswordInput = findViewById(R.id.passwordInputLayout)
        loginButton = findViewById(R.id.loginButton)

        registerButton.setOnClickListener {
            animateButton(registerButton)
            handleRegistration()
        }

        loginButton.setOnClickListener {
            animateButton(loginButton)
            handleLogin()
        }
    }

    /**
     * Handles the registration process:
     * - Validates email and password inputs.
     * - Hashes the password with BCrypt.
     * - Saves credentials in EncryptedSharedPreferences.
     * - Transitions the UI from registration to login.
     */
    private fun handleRegistration() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Hash the password with BCrypt using a stronger work factor (12)
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12))

        // Save credentials securely using EncryptedSharedPreferences
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val securePreferences = EncryptedSharedPreferences.create(
            PREFERENCE_NAME,
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        with(securePreferences.edit()) {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD_HASH, passwordHash)
            apply()
        }

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
        Log.d("RegisterActivity", "User registered: $email")

        // Transition the UI from registration fields to login fields.
        transitionToLogin()
    }

    /**
     * Handles the login process:
     * - Validates input.
     * - Retrieves stored credentials.
     * - Verifies credentials using BCrypt.
     * - Notifies the user upon success or failure.
     */
    private fun handleLogin() {
        val loginEmail = loginEmailInput.text.toString().trim()
        val loginPassword = loginPasswordInput.text.toString().trim()

        if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val securePreferences = EncryptedSharedPreferences.create(
            PREFERENCE_NAME,
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val storedEmail = securePreferences.getString(KEY_EMAIL, null)
        val storedPasswordHash = securePreferences.getString(KEY_PASSWORD_HASH, null)

        if (storedEmail == null || storedPasswordHash == null) {
            Toast.makeText(this, "No registered user found. Please register first.", Toast.LENGTH_SHORT).show()
            return
        }

        if (loginEmail != storedEmail) {
            Toast.makeText(this, "Incorrect email", Toast.LENGTH_SHORT).show()
            return
        }
        if (!BCrypt.checkpw(loginPassword, storedPasswordHash)) {
            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to the main part of your app (e.g., startActivity(Intent(this, MainActivity::class.java)))
    }

    // Helper method to validate email format.
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Button click animation for visual feedback.
    private fun animateButton(button: Button) {
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f)
        ObjectAnimator.ofPropertyValuesHolder(button, scaleX, scaleY).apply {
            duration = 300
            start()
        }
    }

    // Transitions the UI by fading out the registration views and fading in the login views.
    private fun transitionToLogin() {
        // Fade out registration views.
        emailInput.animate().alpha(0f).setDuration(500).withEndAction { emailInput.visibility = View.GONE }
        passwordInput.animate().alpha(0f).setDuration(500).withEndAction { passwordInput.visibility = View.GONE }
        registerButton.animate().alpha(0f).setDuration(500).withEndAction { registerButton.visibility = View.GONE }

        // Fade in login views.
        loginEmailInput.visibility = View.VISIBLE
        loginEmailInput.alpha = 0f
        loginEmailInput.animate().alpha(1f).setDuration(500).start()

        loginPasswordInput.visibility = View.VISIBLE
        loginPasswordInput.alpha = 0f
        loginPasswordInput.animate().alpha(1f).setDuration(500).start()

        loginButton.visibility = View.VISIBLE
        loginButton.alpha = 0f
        loginButton.animate().alpha(1f).setDuration(500).start()
    }
}
