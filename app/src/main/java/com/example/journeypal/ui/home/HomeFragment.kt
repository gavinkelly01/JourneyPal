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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        filters["women"] = binding.filterWomen
        filters["people_of_color"] = binding.filterPeopleOfColour
        filters["LGBTQ"] = binding.filterLgbt
        filters["disabilities"] = binding.filterDisabilities
        filters["religious_freedom"] = binding.filterReligiousFreedom
        filters["immigrants_refugees"] = binding.filterImmigrantsRefugees
        filters["transgender_non_binary"] = binding.filterTransgenderNonBinary


        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.minimizeButton.setOnClickListener {
            if (binding.filterContainer.visibility == View.VISIBLE) {
                binding.filterContainer.visibility = View.GONE
                binding.minimizeButton.text = "Expand Filters"

                val params = binding.mapFragment.layoutParams as ConstraintLayout.LayoutParams
                params.height = resources.getDimensionPixelSize(R.dimen.map_minimized_height)
                params.topToBottom = R.id.text_home
                binding.mapFragment.layoutParams = params
                binding.flagImageView.visibility = View.VISIBLE
                binding.safetyInfoTextView.visibility = View.VISIBLE

            } else {
                binding.filterContainer.visibility = View.VISIBLE
                binding.minimizeButton.text = "Minimize"

                val params = binding.mapFragment.layoutParams as ConstraintLayout.LayoutParams
                params.height = resources.getDimensionPixelSize(R.dimen.map_default_height)
                params.topToBottom = R.id.resetMapButton
                binding.mapFragment.layoutParams = params
                binding.flagImageView.visibility = View.VISIBLE
                binding.safetyInfoTextView.visibility = View.VISIBLE
            }
        }


        binding.resetMapButton.setOnClickListener {
            resetMapToDefault()
            filters.values.forEach { it.isChecked = false }

        }

        binding.layerButton.setOnClickListener {
            showLayerSelectionDialog()
        }

        return binding.root
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
                Toast.makeText(
                    requireContext(),
                    "Please select at least one filter",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getSelectedFilters(): List<String> {
        return filters.filter { it.value.isChecked }.map { it.key }
    }


    private fun showSafetyInfo(latLng: LatLng, filters: List<String>) {
        lifecycleScope.launch {
            val countryName = getCountryNameFromLatLng(latLng)
            if (countryName != "Unknown") {
                fetchCountryData(countryName, filters, latLng)
            } else {
                Toast.makeText(requireContext(), "Country not found", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(
                                requireContext(),
                                "Failed to fetch data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Error occurred: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
                    infoBuilder.append("\n${filter.replaceFirstChar { it.uppercase(Locale.ROOT) }}: $safetyInfo")
                } else {
                    infoBuilder.append("\n${filter.replaceFirstChar { it.uppercase(Locale.ROOT) }}: No data available")
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
            .append("\nFlag: $flagUrl")
            .append("\nCurrency: $currency")
            .append("\nTime Zone: $timeZone")
            .append("\nCountry Domain (TLD): $tld")
            .append("\nHuman Development Index: ${if (humanDevelopmentIndex != -1.0) humanDevelopmentIndex else "No data"}")

        requireActivity().runOnUiThread {
            binding.safetyInfoTextView.text = infoBuilder.toString()
            binding.safetyInfoTextView.visibility = View.VISIBLE

            if (flagUrl != "No data") {
                Glide.with(requireContext())
                    .load(flagUrl)
                    .into(binding.flagImageView)
                binding.flagImageView.visibility = View.VISIBLE
            }

            currentMarker?.remove()

            currentMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Safety Info for $countryName")
                    .snippet(infoBuilder.toString())
            )

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        }
    }

    private fun resetMapToDefault() {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, defaultZoom))
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
