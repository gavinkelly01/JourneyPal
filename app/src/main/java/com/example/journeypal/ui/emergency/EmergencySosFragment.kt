package com.example.journeypal.ui.emergency

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.journeypal.R
import com.example.journeypal.databinding.FragmentSosBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EmergencySosFragment : Fragment() {
    private var _binding: FragmentSosBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var countDownTimer: CountDownTimer? = null
    private var isCountingDown = false

    private val emergencyContacts = mutableListOf<EmergencyContact>()
    private val emergencyPhoneNumber = "" // 112 or any default number

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val smsGranted = permissions[Manifest.permission.SEND_SMS] == true

        if (locationGranted && smsGranted) {
            binding.sosButton.isEnabled = true
        } else {
            Toast.makeText(
                requireContext(),
                "Location and SMS permissions are required for the SOS feature",
                Toast.LENGTH_LONG
            ).show()
            binding.sosButton.isEnabled = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        loadEmergencyContacts() // Load saved contacts
        updateEmergencyContactsList()

        binding.addContactButton.setOnClickListener {
            showAddContactDialog()
        }

        checkAndRequestPermissions()
        setupSosButton()
    }

    private fun setupSosButton() {
        binding.sosButton.setOnClickListener {
            if (isCountingDown) {
                cancelCountdown()
            } else {
                startCountdown()
            }
        }
    }

    private fun startCountdown() {
        binding.sosButton.text = "CANCEL SOS"
        binding.sosButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.redError))
        binding.countdownText.visibility = View.VISIBLE
        isCountingDown = true

        countDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.countdownText.text = "Sending SOS in ${secondsLeft}s"
            }

            override fun onFinish() {
                binding.countdownText.text = "Sending SOS..."
                triggerSosAlert()
            }
        }.start()
    }

    private fun cancelCountdown() {
        countDownTimer?.cancel()
        resetSosButton()
    }

    private fun resetSosButton() {
        binding.sosButton.text = "SOS"
        binding.sosButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.alert_red))
        binding.countdownText.visibility = View.GONE
        isCountingDown = false
    }

    private fun triggerSosAlert() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val location = getCurrentLocation()
                sendEmergencySms(location)
                showEmergencyCallDialog(location)

                Toast.makeText(
                    requireContext(),
                    "SOS alert sent to emergency contacts",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to send SOS: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                resetSosButton()
            }
        }
    }

    private suspend fun getCurrentLocation(): Location {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("Location permission not granted")
        }

        // Creating a LocationRequest with high accuracy
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Set the desired priority here
        }

        return fusedLocationClient.getCurrentLocation(locationRequest.priority, object : CancellationToken() {
            override fun onCanceledRequested(listener: OnTokenCanceledListener) =
                CancellationTokenSource().token

            override fun isCancellationRequested() = false
        }).await() ?: throw Exception("Could not get current location")
    }

    private fun sendEmergencySms(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val mapsUrl = "https://maps.google.com/?q=$latitude,$longitude"
        val message = "🚨 EMERGENCY SOS 🚨\nI need help! My current location is:\n$mapsUrl"

        if (emergencyContacts.isEmpty()) {
            Toast.makeText(requireContext(), "No emergency contacts available", Toast.LENGTH_SHORT).show()
            return
        }

        for (contact in emergencyContacts) {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${contact.phoneNumber}") // This ensures only SMS apps handle it
                    putExtra("sms_body", message)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("EmergencySos", "Failed to launch SMS intent for ${contact.phoneNumber}: ${e.message}")
                Toast.makeText(
                    requireContext(),
                    "Failed to open SMS app for ${contact.phoneNumber}: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }



    private fun showEmergencyCallDialog(location: Location) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$emergencyPhoneNumber")
        }
        startActivity(intent)
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions)
            binding.sosButton.isEnabled = false
        } else {
            binding.sosButton.isEnabled = true
        }
    }

    private fun loadSampleEmergencyContacts() {
        emergencyContacts.add(EmergencyContact("Emergency Contact 1: Gavin Kelly", "+353-87 119 4024"))
    }

    private fun loadEmergencyContacts() {
        emergencyContacts.clear()

        val sharedPreferences = requireActivity().getSharedPreferences("EmergencyContacts", 0)
        val count = sharedPreferences.getInt("count", 0)

        for (i in 0 until count) {
            val name = sharedPreferences.getString("name_$i", "") ?: ""
            val phone = sharedPreferences.getString("phone_$i", "") ?: ""

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                emergencyContacts.add(EmergencyContact(name, phone))
            }
        }

        if (emergencyContacts.isEmpty()) {
            loadSampleEmergencyContacts()
            saveEmergencyContacts()
        }
    }

    private fun saveEmergencyContacts() {
        val sharedPreferences = requireActivity().getSharedPreferences("EmergencyContacts", 0)
        val editor = sharedPreferences.edit()
        editor.clear()

        emergencyContacts.forEachIndexed { index, contact ->
            editor.putString("name_$index", contact.name)
            editor.putString("phone_$index", contact.phoneNumber)
        }
        editor.putInt("count", emergencyContacts.size)
        editor.apply()
    }

    private fun updateEmergencyContactsList() {
        binding.emergencyContactsContainer.removeAllViews()

        for ((index, contact) in emergencyContacts.withIndex()) {
            val contactView = layoutInflater.inflate(
                R.layout.emergency_contact,
                binding.emergencyContactsContainer,
                false
            )

            val nameTextView = contactView.findViewById<TextView>(R.id.contactName)
            val numberTextView = contactView.findViewById<TextView>(R.id.contactNumber)
            val editButton = contactView.findViewById<ImageButton>(R.id.editContactButton)
            val deleteButton = contactView.findViewById<ImageButton>(R.id.deleteContactButton)

            nameTextView.text = contact.name
            numberTextView.text = contact.phoneNumber

            editButton.setOnClickListener {
                showEditContactDialog(index, contact)
            }

            deleteButton.setOnClickListener {
                if (index < emergencyContacts.size) {
                    emergencyContacts.removeAt(index)
                    updateEmergencyContactsList()
                    saveEmergencyContacts()
                    Toast.makeText(requireContext(), "Contact deleted", Toast.LENGTH_SHORT).show()
                }
            }

            binding.emergencyContactsContainer.addView(contactView)
        }
    }

    private fun showAddContactDialog() {
        val dialogView = layoutInflater.inflate(R.layout.edit_contacts, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.editContactName)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.editContactPhone)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val phone = phoneEditText.text.toString().trim()

                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    emergencyContacts.add(EmergencyContact(name, phone))
                    updateEmergencyContactsList()
                    saveEmergencyContacts()
                    Toast.makeText(requireContext(), "Contact added", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Name and phone number cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditContactDialog(index: Int, contact: EmergencyContact) {
        val dialogView = layoutInflater.inflate(R.layout.edit_contacts, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.editContactName)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.editContactPhone)

        nameEditText.setText(contact.name)
        phoneEditText.setText(contact.phoneNumber)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val phone = phoneEditText.text.toString().trim()

                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    emergencyContacts[index] = EmergencyContact(name, phone)
                    updateEmergencyContactsList()
                    saveEmergencyContacts()
                    Toast.makeText(requireContext(), "Contact updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Name and phone number cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }

    data class EmergencyContact(val name: String, val phoneNumber: String)
}
