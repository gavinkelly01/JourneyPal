package com.example.journeypal.ui.home

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.journeypal.R
import com.example.journeypal.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private var currentMarker: Marker? = null
    private val filters = mutableMapOf<String, CheckBox>()
    private val defaultLatLng = LatLng(0.0, 0.0)
    private val defaultZoom = 2.0f
    private var selectedCountryName = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize filters
        filters["women"] = binding.filterWomen
        filters["people_of_color"] = binding.filterPeopleOfColour
        filters["LGBTQ"] = binding.filterLgbt
        filters["disabilities"] = binding.filterDisabilities
        filters["religious_freedom"] = binding.filterReligiousFreedom
        filters["immigrants_refugees"] = binding.filterImmigrantsRefugees
        filters["transgender_non_binary"] = binding.filterTransgenderNonBinary

        // Initialize map
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize UI elements
        setupSearchView()
        setupButtons()
        setupFilterChangeListeners()

        // Initially hide the info containers
        binding.countryInfoContainer.visibility = View.GONE
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchCountry(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun setupButtons() {
        binding.minimizeButton.setOnClickListener {
            animateButtonClick(it) { toggleFilters() }
        }
        binding.resetMapButton.setOnClickListener {
            animateButtonClick(it) { resetMapToDefault() }
        }
        binding.layerButton.setOnClickListener {
            animateButtonClick(it) { showLayerSelectionDialog() }
        }
    }

    private fun setupFilterChangeListeners() {
        // Add listeners to filter checkboxes to update info when filters change
        filters.values.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, _ ->
                if (selectedCountryName.isNotEmpty() && selectedCountryName != "Unknown") {
                    val selectedFilters = getSelectedFilters()
                    if (selectedFilters.isNotEmpty()) {
                        currentMarker?.position?.let { position ->
                            showSafetyInfo(position, selectedFilters)
                        }
                    } else {
                        binding.countryInfoContainer.visibility = View.GONE
                        Toast.makeText(requireContext(), "Please select at least one filter", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun animateButtonClick(view: View, action: () -> Unit) {
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

    private fun toggleFilters() {
        if (binding.filterContainer.visibility == View.VISIBLE) {
            binding.filterContainer.animate()
                .translationY(-binding.filterContainer.height.toFloat())
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.filterContainer.visibility = View.GONE
                    binding.minimizeButton.text = "Expand Filters"
                    val params = binding.mapFragment.layoutParams as ConstraintLayout.LayoutParams
                    params.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                    params.topToBottom = R.id.searchView
                    binding.mapFragment.layoutParams = params
                }
                .start()
        } else {
            binding.filterContainer.visibility = View.VISIBLE
            binding.filterContainer.alpha = 0f
            binding.filterContainer.translationY = -binding.filterContainer.height.toFloat()
            binding.filterContainer.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .start()

            binding.minimizeButton.text = "Minimize"
            val params = binding.mapFragment.layoutParams as ConstraintLayout.LayoutParams
            params.height = resources.getDimensionPixelSize(R.dimen.map_default_height)
            params.topToBottom = R.id.filter_container
            binding.mapFragment.layoutParams = params
        }
    }

    private fun resetMapToDefault() {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, defaultZoom), 1000, null)
        filters.values.forEach { it.isChecked = false }
        binding.countryInfoContainer.visibility = View.GONE
        selectedCountryName = ""
        currentMarker?.remove()
        currentMarker = null
    }

    private fun showLayerSelectionDialog() {
        val layerOptions = arrayOf("Google Maps", "Satellite View", "Terrain View")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Map Layer")
            .setItems(layerOptions) { _, which ->
                when (which) {
                    0 -> switchToGoogleMapLayer()
                    1 -> switchToSatelliteLayer()
                    2 -> switchToTerrainLayer()
                }
            }
            .setCancelable(true)
            .show()
    }

    private fun switchToGoogleMapLayer() {
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        Toast.makeText(requireContext(), "Switched to Google Maps", Toast.LENGTH_SHORT).show()
    }

    private fun switchToSatelliteLayer() {
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        Toast.makeText(requireContext(), "Switched to Satellite View", Toast.LENGTH_SHORT).show()
    }

    private fun switchToTerrainLayer() {
        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        Toast.makeText(requireContext(), "Switched to Terrain View", Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, defaultZoom))
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnMapClickListener { latLng ->
            val selectedFilters = getSelectedFilters()
            if (selectedFilters.isNotEmpty()) {
                showSafetyInfo(latLng, selectedFilters)
            } else {
                Toast.makeText(requireContext(), "Please select at least one filter", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchCountry(query: String) {
        val geocoder = Geocoder(requireContext())
        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val latLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))

                val selectedFilters = getSelectedFilters()
                if (selectedFilters.isNotEmpty()) {
                    showSafetyInfo(latLng, selectedFilters)
                } else {
                    Toast.makeText(requireContext(), "Please select at least one filter", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Country not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error searching for country", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSelectedFilters(): List<String> {
        return filters.filter { it.value.isChecked }.map { it.key }
    }

    private fun showSafetyInfo(latLng: LatLng, filters: List<String>) {
        lifecycleScope.launch {
            val countryName = getCountryNameFromLatLng(latLng)
            selectedCountryName = countryName

            if (countryName != "Unknown") {
                // Show loading indicator
                binding.safetyInfoTextView.text = "Loading safety information for $countryName..."
                binding.countryInfoContainer.visibility = View.VISIBLE
                binding.flagImageView.visibility = View.GONE

                fetchCountryData(countryName, filters, latLng)
            } else {
                Toast.makeText(requireContext(), "Country not found", Toast.LENGTH_SHORT).show()
                binding.countryInfoContainer.visibility = View.GONE
            }
        }
    }

    private fun fetchCountryData(countryName: String, filters: List<String>, latLng: LatLng) {
        if (countryName.isEmpty() || countryName == "Unknown") {
            Toast.makeText(requireContext(), "Invalid country selected", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://safetyratingapi.onrender.com/api/safety-ratings/$countryName")
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val jsonResponse = response.body?.string() ?: ""
                        Log.d("API_RESPONSE", "Response: $jsonResponse")
                        val jsonObject = JSONObject(jsonResponse)

                        requireActivity().runOnUiThread {
                            displaySafetyInfo(jsonObject, filters, latLng, countryName)
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            binding.safetyInfoTextView.text = "Failed to fetch data for $countryName"
                            binding.countryInfoContainer.visibility = View.VISIBLE
                            binding.flagImageView.visibility = View.GONE
                            Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    binding.safetyInfoTextView.text = "Error: ${e.message}"
                    binding.countryInfoContainer.visibility = View.VISIBLE
                    binding.flagImageView.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun displaySafetyInfo(
        jsonResponse: JSONObject,
        filters: List<String>,
        latLng: LatLng,
        countryName: String
    ) {
        val infoBuilder = StringBuilder("Safety Information for $countryName:\n")
        val ratings = jsonResponse.optJSONObject("ratings")?.optJSONObject("ratings")
        Log.d("RATINGS_OBJECT", "Ratings: ${ratings.toString()}")

        if (ratings != null) {
            filters.forEach { filter ->
                val safetyInfo = ratings.optDouble(filter, -1.0)
                if (safetyInfo != -1.0) {
                    infoBuilder.append("\n${formatFilterName(filter)}: $safetyInfo")
                } else {
                    infoBuilder.append("\n${formatFilterName(filter)}: No data available")
                }
            }
        } else {
            infoBuilder.append("\nRatings data not available for $countryName.")
        }

        val countryData = jsonResponse.optJSONObject("ratings")
        val capital = countryData?.optString("capital", "No data")
        val population = countryData?.optInt("population", -1)
        val region = countryData?.optString("region", "No data")
        val language = countryData?.optJSONArray("language")?.join(", ") ?: "No data"
        val flagUrl = countryData?.optString("flag_url", "No data")
        val currency = countryData?.optString("currency", "No data")
        val timeZone = countryData?.optString("time_zone", "No data")
        val tld = countryData?.optString("tld", "No data")
        val humanDevelopmentIndex = countryData?.optDouble("humandevelopmentindex", -1.0)

        infoBuilder.append("\n\nOther Information:")
            .append("\nCapital: $capital")
            .append("\nPopulation: ${if (population != -1) population else "No data"}")
            .append("\nRegion: $region")
            .append("\nLanguages: $language")
            .append("\nCurrency: $currency")
            .append("\nTime Zone: $timeZone")
            .append("\nCountry Domain (TLD): $tld")
            .append("\nHuman Development Index: ${if (humanDevelopmentIndex != -1.0) humanDevelopmentIndex else "No data"}")

        requireActivity().runOnUiThread {
            // Set the text content to the TextView
            binding.safetyInfoTextView.text = infoBuilder.toString()

            // Make sure the container is visible
            binding.countryInfoContainer.visibility = View.VISIBLE
            binding.countryInfoContainer.alpha = 1f

            // Handle flag image
            if (flagUrl != "No data") {
                binding.flagImageView.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(flagUrl)
                    .placeholder(R.drawable.flag_background) // Add a placeholder drawable
                    .error(R.drawable.flag_background) // Add an error drawable
                    .into(binding.flagImageView)
            } else {
                binding.flagImageView.visibility = View.GONE
            }

            // Update or add marker
            currentMarker?.remove()
            currentMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(countryName)
            )
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f), 1000, null)
        }
    }

    private fun formatFilterName(filter: String): String {
        return filter.replace("_", " ").split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
            }
    }

    @Suppress("DEPRECATION")
    private fun getCountryNameFromLatLng(latLng: LatLng): String {
        val geocoder = Geocoder(requireContext())
        return try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0]?.countryName ?: "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}