package com.example.journeypal.ui.storage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.journeypal.R
import org.mindrot.jbcrypt.BCrypt

class RegisterActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button

    private val PREFERENCE_NAME = "user_credentials"
    private val KEY_EMAIL = "user_email"
    private val KEY_PASSWORD_HASH = "user_password_hash"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        registerButton = findViewById(R.id.registerButton)

        registerButton.setOnClickListener { handleRegistration() }
    }

    private fun handleRegistration() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())

        val preferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PASSWORD_HASH, passwordHash)
        editor.apply()

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()


        val intent = Intent(this, LoginFragment::class.java)
        startActivity(intent)

        finish()
    }

}
