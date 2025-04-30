package com.example.journeypal.ui.LiveAlerts

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.journeypal.databinding.FragmentTravelAlertsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

class LiveAlerts : Fragment() {

    private var _binding: FragmentTravelAlertsBinding? = null
    private val binding get() = _binding!!

    private val rapidApiKey =
        "be344708c1msh4e042dcf8d521b4p18acc1jsn4a22446a5722"  // Replace with your actual key
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTravelAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.fetchNewsButton.setOnClickListener {
            getLocationAndFetchNews()
        }

        binding.newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun getLocationAndFetchNews() {
        // Check if the location permission is granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Toast.makeText(
                    requireContext(),
                    "Location permission is needed to fetch local news.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            // Permission already granted, proceed to get location
            fetchLocationAndNews()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndNews()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission denied. Please enable location permission.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun fetchLocationAndNews() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                val cityName = addresses?.firstOrNull()?.locality
                val countryName = addresses?.firstOrNull()?.countryName

                // Get country code using the Locale class
                val countryCode = Locale.getDefault().country // This gets the 2-letter country code

                if (cityName != null && countryCode != null) {
                    fetchLocalNews(cityName, countryCode)  // Use country code instead of country name
                } else {
                    Toast.makeText(requireContext(), "Unable to get location details.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Unable to get location.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to get location.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchLocalNews(cityName: String, countryCode: String) {
        binding.progressBar.visibility = View.VISIBLE
        val client = OkHttpClient()
        val url =
            "https://real-time-news-data.p.rapidapi.com/local-headlines?query=$cityName&country=$countryCode&lang=en&limit=10"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-rapidapi-key", rapidApiKey)
            .addHeader("x-rapidapi-host", "real-time-news-data.p.rapidapi.com")
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                // Log status code for debugging
                Log.d("LiveAlerts", "API Response Code: ${response.code}")

                if (!response.isSuccessful) {
                    // Log error if the response is not successful
                    Log.e(
                        "LiveAlerts",
                        "Error: Unsuccessful response. Code: ${response.code}. Response Body: $responseBody"
                    )
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "API request failed: ${response.code}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                if (responseBody.isNullOrBlank()) {
                    // Log empty response case
                    Log.e("LiveAlerts", "Error: Empty response body")
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Empty response body from API",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                // Continue with processing the successful response
                val json = JSONObject(responseBody)
                val articles = json.optJSONArray("data")

                if (articles == null || articles.length() == 0) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.newsRecyclerView.adapter = NewsAdapter(emptyList())
                        Toast.makeText(
                            requireContext(),
                            "No news available for this city.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val newsItems = mutableListOf<NewsItem>()
                for (i in 0 until articles.length()) {
                    val article = articles.getJSONObject(i)
                    newsItems.add(
                        NewsItem(
                            title = article.optString("title"),
                            link = article.optString("link"),
                            snippet = article.optString("snippet"),
                            photoUrl = article.optString("photo_url"),
                            thumbnailUrl = article.optString("thumbnail_url"),
                            publishedAt = article.optString("published_datetime_utc")
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.newsRecyclerView.adapter = NewsAdapter(newsItems)
                }

            } catch (e: Exception) {
                // Log any exceptions
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Log.e("LiveAlerts", "Error: ${e.message}", e)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
