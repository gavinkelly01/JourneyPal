package com.example.journeypal.ui.storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.journeypal.R
import com.example.journeypal.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        val emailInput: EditText = binding.emailInput
        val passwordInput: EditText = binding.passwordInput
        val loginButton: Button = binding.loginButton

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Simulate a successful login (you can replace this with real validation)
                findNavController().navigate(R.id.action_loginFragment_to_filesFragment)
            } else {
                // Show error toast if credentials are missing
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}
