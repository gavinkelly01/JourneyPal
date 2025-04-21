package com.example.journeypal.ui.storage

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.example.journeypal.MainActivity
import com.example.journeypal.R
import org.mindrot.jbcrypt.BCrypt

class RegisterActivity : AppCompatActivity() {

    // UI elements
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var loginEmailInput: EditText
    private lateinit var loginPasswordInput: EditText
    private lateinit var loginButton: Button

    // Preference constants
    private val PREFERENCE_NAME = "secure_user_credentials"
    private val KEY_EMAIL = "secure_user_email"
    private val KEY_PASSWORD_HASH = "secure_user_password_hash"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()

        // Auto-login if credentials exist
        if (areCredentialsStored()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        registerButton.setOnClickListener {
            animateButton(it as Button)
            handleRegistration()
        }

        loginButton.setOnClickListener {
            animateButton(it as Button)
            handleLogin()
        }
    }

    private fun initViews() {
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        registerButton = findViewById(R.id.registerButton)

        loginEmailInput = findViewById(R.id.loginEmailInput)
        loginPasswordInput = findViewById(R.id.loginPasswordInput)
        loginButton = findViewById(R.id.loginButton)
    }

    private fun handleRegistration() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (!isValidEmail(email)) {
            emailInput.error = "Invalid email format"
            emailInput.requestFocus()
            return
        }

        if (password.length < 8) {
            passwordInput.error = "Password must be at least 8 characters"
            passwordInput.requestFocus()
            return
        }

        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12))

        try {
            val securePrefs = getSecurePreferences()
            with(securePrefs.edit()) {
                putString(KEY_EMAIL, email)
                putString(KEY_PASSWORD_HASH, passwordHash)
                apply()
            }

            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
            transitionToLogin()
        } catch (e: Exception) {
            Log.e("RegisterActivity", "Registration error", e)
            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleLogin() {
        val email = loginEmailInput.text.toString().trim()
        val password = loginPasswordInput.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val securePrefs = getSecurePreferences()
            val storedEmail = securePrefs.getString(KEY_EMAIL, null)
            val storedPasswordHash = securePrefs.getString(KEY_PASSWORD_HASH, null)

            if (email != storedEmail) {
                Toast.makeText(this, "Incorrect email", Toast.LENGTH_SHORT).show()
                return
            }

            if (!BCrypt.checkpw(password, storedPasswordHash)) {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                return
            }

            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()

        } catch (e: Exception) {
            Log.e("RegisterActivity", "Login error", e)
            Toast.makeText(this, "Login failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getSecurePreferences(): SharedPreferences {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFERENCE_NAME,
            masterKey,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }


    private fun areCredentialsStored(): Boolean {
        return try {
            val prefs = getSecurePreferences()
            val email = prefs.getString(KEY_EMAIL, null)
            val hash = prefs.getString(KEY_PASSWORD_HASH, null)
            !email.isNullOrEmpty() && !hash.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun animateButton(button: Button) {
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f)
        ObjectAnimator.ofPropertyValuesHolder(button, scaleX, scaleY).apply {
            duration = 300
            start()
        }
    }

    private fun transitionToLogin() {
        // Hide registration views
        emailInput.animate().alpha(0f).setDuration(300).withEndAction {
            emailInput.visibility = View.GONE
        }
        passwordInput.animate().alpha(0f).setDuration(300).withEndAction {
            passwordInput.visibility = View.GONE
        }
        registerButton.animate().alpha(0f).setDuration(300).withEndAction {
            registerButton.visibility = View.GONE
        }

        // Show login views
        loginEmailInput.visibility = View.VISIBLE
        loginPasswordInput.visibility = View.VISIBLE
        loginButton.visibility = View.VISIBLE

        loginEmailInput.alpha = 0f
        loginPasswordInput.alpha = 0f
        loginButton.alpha = 0f

        loginEmailInput.animate().alpha(1f).setDuration(300).start()
        loginPasswordInput.animate().alpha(1f).setDuration(300).start()
        loginButton.animate().alpha(1f).setDuration(300).start()
    }
}
