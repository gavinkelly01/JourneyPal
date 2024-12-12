package com.example.journeypal.ui.storage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.journeypal.R
import org.mindrot.jbcrypt.BCrypt

class LoginFragment : Fragment() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private val PREFERENCE_NAME = "user_credentials"
    private val KEY_EMAIL = "user_email"
    private val KEY_PASSWORD_HASH = "user_password_hash"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_login, container, false)

        emailInput = rootView.findViewById(R.id.emailInput)
        passwordInput = rootView.findViewById(R.id.passwordInput)
        loginButton = rootView.findViewById(R.id.loginButton)
        registerButton = rootView.findViewById(R.id.registerButton)
        loginButton.setOnClickListener { handleLogin() }
        registerButton.setOnClickListener { openRegistration() }

        return rootView
    }

    private fun handleLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val preferences = requireActivity().getSharedPreferences(PREFERENCE_NAME, AppCompatActivity.MODE_PRIVATE)
        val storedEmail = preferences.getString(KEY_EMAIL, null)
        val storedPasswordHash = preferences.getString(KEY_PASSWORD_HASH, null)

        if (storedEmail != null && storedPasswordHash != null && storedEmail == email && BCrypt.checkpw(password, storedPasswordHash)) {
            val intent = Intent(requireActivity(), SecureStorageActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        } else {
            Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openRegistration() {
        val intent = Intent(requireActivity(), RegisterActivity::class.java)
        startActivity(intent)
    }
}
