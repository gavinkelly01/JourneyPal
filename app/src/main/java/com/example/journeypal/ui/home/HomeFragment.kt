package com.example.journeypal.ui.home

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.journeypal.R
import com.example.journeypal.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var placesClient: PlacesClient
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private var currentMarker: Marker? = null
    val filterChips: MutableMap<String, Chip> = mutableMapOf()
    private val defaultLatLng = LatLng(0.0, 0.0)
    private val defaultZoom = 2.0f
    private var isCountryInfoMinimized = true
    private var originalMapParams: ConstraintLayout.LayoutParams? = null
    private var heatmapOverlay: TileOverlay? = null
    private var isHeatmapVisible = false
    private val countryDataCache = mutableMapOf<String, JSONObject>()
    private var allCountriesData: JSONObject? = null

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
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "google_maps_key")
        }
        placesClient = Places.createClient(requireContext())
        originalMapParams = binding.mapFragment.layoutParams as? ConstraintLayout.LayoutParams
        setupFilterChips()
        setupSearchView()
        setupClickListeners()
    }

    private fun setupFilterChips() {
        val chipMap = mapOf(
            "women" to binding.filterWomen,
            "people_of_color" to binding.filterPeopleOfColour,
            "lgbtq" to binding.filterLgbt,
            "disabilities" to binding.filterDisabilities,
            "religious_freedom" to binding.filterReligiousFreedom,
            "immigrants_refugees" to binding.filterImmigrantsRefugees,
            "transgender_non_binary" to binding.filterTransgenderNonBinary
        )

        chipMap.forEach { (key, chip) ->
            filterChips[key] = chip
            chip.apply {
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    val status = if (isChecked) "selected" else "deselected"
                    Log.d("ChipSelected", "$text $status")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun searchForLGBTQPlaces() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }
        if ("lgbtq" !in getSelectedFilters()) return
        val latLng = googleMap.cameraPosition.target
        val bounds = createBounds(latLng, 0.09)
        val placesRequest = FindCurrentPlaceRequest.newInstance(listOf(Place.Field.NAME, Place.Field.LAT_LNG))
        placesClient.findCurrentPlace(placesRequest)
            .addOnSuccessListener { response ->
                response.placeLikelihoods
                    .filter { it.place.name.contains("LGBTQ", true) }
                    .forEach { place ->
                        place.place.latLng?.let {
                            googleMap.addMarker(MarkerOptions().position(it).title(place.place.name))
                        }
                    }
            }
            .addOnFailureListener { e ->
                Log.e("PlacesAPIError", "Error fetching places: ${e.message}")
            }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun createBounds(center: LatLng, delta: Double): RectangularBounds {
        return RectangularBounds.newInstance(
            LatLng(center.latitude - delta, center.longitude - delta),
            LatLng(center.latitude + delta, center.longitude + delta)
        )
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    searchForLGBTQPlaces()
                } else {
                    Toast.makeText(requireContext(), "Location permission is required to fetch places.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun setupSearchView() {
        binding.searchView.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString()
                if (query.isNotEmpty()) {
                    searchCountry(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupClickListeners() {
        binding.minimizeCountryInfoButton.setOnClickListener {
            toggleCountryInfoContainer()
        }

        binding.closeCountryInfoButton.setOnClickListener {
            toggleCountryInfoContainer()
        }

        binding.filterButton.setOnClickListener {
            animateButtonClick(it) { toggleFilters() }
        }

        binding.closeFiltersButton?.setOnClickListener {
            animateButtonClick(it) { toggleFilters() }
        }

        binding.applyFiltersButton?.setOnClickListener {
            animateButtonClick(it) {
                applyFilters()
                toggleFilters()
            }
        }



        binding.resetButton.setOnClickListener {
            resetMapToDefault()
        }


        binding.layerButton.setOnClickListener {
            animateButtonClick(it) {
                showLayerSelectionDialog()
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
            // Hide filter container
            binding.filterContainer.animate()
                .translationY(-binding.filterContainer.height.toFloat())
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.filterContainer.visibility = View.GONE
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
        }
    }


    private fun applyFilters() {
        val selectedFilters = getSelectedFilters()
        if (selectedFilters.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one filter", Toast.LENGTH_SHORT).show()
            return
        }

        toggleFilters()
            currentMarker?.let { marker ->
                showSafetyInfo(marker.position, selectedFilters)
            } ?: run {
                Toast.makeText(requireContext(), "Select a location on the map to view safety information", Toast.LENGTH_SHORT).show()
            }

    }

    private fun resetMapToDefault() {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, defaultZoom), 1000, null)
        filterChips.values.forEach { it.isChecked = false }
        if (binding.countryInfoContainer.visibility == View.VISIBLE) {
            toggleCountryInfoContainer()
        }

        currentMarker?.remove()
        currentMarker = null

        if (binding.filterContainer.visibility == View.VISIBLE) {
            toggleFilters()
        }
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
            if (isHeatmapVisible) {
                Toast.makeText(requireContext(), "Disable heatmap to select individual countries", Toast.LENGTH_SHORT).show()
                return@setOnMapClickListener
            }

            val selectedFilters = getSelectedFilters()
            if (selectedFilters.isNotEmpty()) {
                if (binding.countryInfoContainer.visibility != View.VISIBLE) {
                    toggleCountryInfoContainer()
                }
                showSafetyInfo(latLng, selectedFilters)
            } else {
                Toast.makeText(requireContext(), "Please select at least one filter", Toast.LENGTH_SHORT).show()
                if (binding.filterContainer.visibility != View.VISIBLE) {
                    toggleFilters()
                }
            }
        }
    }

    private fun searchCountry(query: String) {
        val geocoder = Geocoder(requireContext())
        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                if (address != null) {
                    val latLng = LatLng(address.latitude, address.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))

                    val selectedFilters = getSelectedFilters()
                    if (selectedFilters.isNotEmpty()) {
                        if (binding.countryInfoContainer.visibility != View.VISIBLE) {
                            toggleCountryInfoContainer()
                        }
                        showSafetyInfo(latLng, selectedFilters)
                    } else {
                        Toast.makeText(requireContext(), "Please select at least one filter", Toast.LENGTH_SHORT).show()
                        // Show filter container if it's not visible
                        if (binding.filterContainer.visibility != View.VISIBLE) {
                            toggleFilters()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Country not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("SearchError", "Error searching for country", e)
            Toast.makeText(requireContext(), "Error searching for country: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun getSelectedFilters(): List<String> {
        return filterChips.filter { it.value.isChecked }.map { it.key }
    }

    fun showSafetyInfo(latLng: LatLng, filters: List<String>) {
        lifecycleScope.launch {
            val countryName = getCountryNameFromLatLng(latLng)
            if (countryName != "Unknown") {
                fetchCountryData(countryName, filters, latLng)
            } else {
                Toast.makeText(requireContext(), "Country not found at this location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCountryData(countryName: String, filters: List<String>, latLng: LatLng) {
        if (countryName.isEmpty() || countryName == "Unknown") {
            Toast.makeText(requireContext(), "Invalid country selected", Toast.LENGTH_SHORT).show()
            return
        }

        binding.safetyInfoTextView.text = "Loading safety information for $countryName..."

        val client = OkHttpClient()
        val encodedCountryName = countryName.replace(" ", "%20") // URL encode the country name
        val request = Request.Builder()
            .url("https://safetyratingapi.onrender.com/api/safety-ratings/$encodedCountryName")
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
                            val errorMsg = "Failed to fetch data: ${response.code} ${response.message}"
                            Log.e("API_ERROR", errorMsg)
                            showError(errorMsg)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("API_ERROR", "Exception: ${e.message}")
                requireActivity().runOnUiThread {
                    showError("Error occurred: ${e.message}")
                }
            }
        }.start()
    }

    private fun displaySafetyInfo(
        jsonResponse: JSONObject?,
        filters: List<String>,
        latLng: LatLng,
        countryName: String
    ) {
        if (jsonResponse == null) {
            showError("Failed to retrieve data. Please check your internet connection.")
            return
        }

        binding.countryNameTextView.text = countryName

        val infoBuilder = StringBuilder("Safety Information for $countryName:\n")
        val ratings = jsonResponse.optJSONObject("ratings")?.optJSONObject("ratings")

        if (ratings != null) {
            filters.forEach { filter ->
                val safetyInfo = ratings.optDouble(filter, -1.0)
                infoBuilder.append(
                    "\n${filter.replaceFirstChar { it.uppercase(Locale.ROOT) }}: " +
                            if (safetyInfo != -1.0) safetyInfo.toString() else "No data available"
                )
            }
        } else {
            infoBuilder.append("\nRatings data not available for $countryName.")
        }

        val countryData = jsonResponse.optJSONObject("ratings")
        val capital = countryData?.optString("capital", "No data") ?: "No data"
        val population = countryData?.optInt("population", -1)
        val region = countryData?.optString("region", "No data") ?: "No data"
        val language = countryData?.optJSONArray("language")?.join(", ") ?: "No data"
        val flagUrl = countryData?.optString("flag_url", "No data") ?: "No data"
        val currency = countryData?.optString("currency", "No data") ?: "No data"
        val timeZone = countryData?.optString("time_zone", "No data") ?: "No data"
        val tld = countryData?.optString("tld", "No data") ?: "No data"
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

        binding.activeFiltersChipGroup.removeAllViews()
        filters.forEach { filter ->
            val chip = Chip(requireContext())
            chip.text = filter.replaceFirstChar { it.uppercase(Locale.ROOT) }
            chip.isCheckable = false
            binding.activeFiltersChipGroup.addView(chip)
        }

        requireActivity().runOnUiThread {
            binding.safetyInfoTextView.apply {
                alpha = 0f
                visibility = View.VISIBLE
                text = infoBuilder.toString()
                scrollTo(0, 0) // Scroll to the top to ensure the text is visible
                animate().alpha(1f).setDuration(300).start()
            }

            if (flagUrl != "No data") {
                binding.flagImageView.apply {
                    alpha = 0f
                    visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(flagUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.flag_background)
                        .into(this)
                    animate().alpha(1f).setDuration(300).start()
                }
            } else {
                binding.flagImageView.visibility = View.GONE
            }

            currentMarker?.remove()
            currentMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(countryName)
            )
            currentMarker?.showInfoWindow()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f), 1000, null)
        }
    }

    private fun showError(message: String) {
        requireActivity().runOnUiThread {
            binding.safetyInfoTextView.apply {
                alpha = 0f
                visibility = View.VISIBLE
                text = message
                scrollTo(0, 0) // Scroll to the top for error messages
                animate().alpha(1f).setDuration(300).start()
            }
            binding.flagImageView.visibility = View.GONE
        }
    }

    private fun toggleCountryInfoContainer() {
        val countryInfoContainer = binding.countryInfoContainer

        if (countryInfoContainer.visibility != View.VISIBLE) {
            countryInfoContainer.visibility = View.VISIBLE
            countryInfoContainer.alpha = 0f
            countryInfoContainer.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        } else {
            countryInfoContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    countryInfoContainer.visibility = View.GONE
                }
                .start()
        }
    }

    interface Checkable {
        val isChecked: Boolean
    }

    @Suppress("DEPRECATION")
    private fun getCountryNameFromLatLng(latLng: LatLng): String {
        val geocoder = Geocoder(requireContext())
        return try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty() && addresses[0] != null) {
                addresses[0]?.countryName ?: "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            Log.e("GeocoderError", "Error getting country name", e)
            "Unknown"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
