package com.example.journeypal.ui.storage

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
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

    companion object {
        private const val PREFERENCE_NAME = "user_credentials"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_PASSWORD_HASH = "user_password_hash"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_login, container, false)

        emailInput = rootView.findViewById(R.id.emailInput)
        passwordInput = rootView.findViewById(R.id.passwordInput)
        loginButton = rootView.findViewById(R.id.loginButton)
        registerButton = rootView.findViewById(R.id.registerButton)

        loginButton.setOnClickListener { animateClick(it) { handleLogin() } }
        registerButton.setOnClickListener { animateClick(it) { openRegistration() } }

        return rootView
    }

    private fun handleLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Email and password cannot be empty")
            return
        }

        try {
            val preferences: SharedPreferences = requireActivity().getSharedPreferences(PREFERENCE_NAME, AppCompatActivity.MODE_PRIVATE)
            val storedEmail = preferences.getString(KEY_EMAIL, null)
            val storedPasswordHash = preferences.getString(KEY_PASSWORD_HASH, null)

            if (storedEmail != null && storedPasswordHash != null && storedEmail == email && BCrypt.checkpw(password, storedPasswordHash)) {
                showToast("Login successful!")
                startActivity(Intent(requireActivity(), SecureStorageActivity::class.java))
                requireActivity().finish()
            } else {
                showShakeAnimation(passwordInput)
                showToast("Invalid credentials")
            }
        } catch (e: Exception) {
            showToast("Error during login: ${e.message}")
            Log.e("LoginFragment", "Error: ${e.message}", e)
        }
    }

    private fun openRegistration() {
        startActivity(Intent(requireActivity(), RegisterActivity::class.java))
    }

    private fun animateClick(view: View, action: () -> Unit) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
                action()
            }
            .start()
    }

    private fun showShakeAnimation(view: View) {
        val shake = ObjectAnimator.ofFloat(view, "translationX", 0f, 10f, -10f, 8f, -8f, 6f, -6f, 4f, -4f, 2f, -2f, 0f)
        shake.duration = 500
        shake.interpolator = AccelerateDecelerateInterpolator()
        shake.start()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}