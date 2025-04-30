package com.example.journeypal.ui.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.journeypal.databinding.FragmentWeatherBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WeatherFragment : Fragment() {

    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!
    private lateinit var geocoder: Geocoder
    private lateinit var areaNameText: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isCelsius = true // Temperature unit toggle
    private var fetchingWeather = false // Prevent multiple fetches

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) fetchWeather()
        else showSnackbar("Location permission denied.", retry = true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        areaNameText = binding.areaNameText
        initializeLocationClient()
        setupUI()
        checkLocationPermissionAndFetchWeather()
    }

    private fun initializeLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun setupUI() {
        binding.forecastRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.refreshButton.setOnClickListener { checkLocationPermissionAndFetchWeather() }
        binding.unitToggleButton.setOnClickListener {
            isCelsius = !isCelsius
            checkLocationPermissionAndFetchWeather()
        }
        binding.swipeRefreshLayout.setOnRefreshListener { checkLocationPermissionAndFetchWeather() }
    }

    private fun checkLocationPermissionAndFetchWeather() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchWeather()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchWeather() {
        if (fetchingWeather) return
        fetchingWeather = true
        showLoading(true)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                fetchAreaName(it.latitude, it.longitude)
                requestWeather(it.latitude, it.longitude)
            } ?: handleLocationFailure()
        }.addOnFailureListener {
            handleLocationFailure()
        }
    }

    private fun fetchAreaName(latitude: Double, longitude: Double) {
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()

            val areaName = addresses.firstOrNull()?.locality
                ?: addresses.firstOrNull()?.subAdminArea
                ?: addresses.firstOrNull()?.adminArea
                ?: "Unknown Location"

            areaNameText.text = areaName
        } catch (e: Exception) {
            areaNameText.text = "Error fetching location"
        }
    }

    private fun requestWeather(lat: Double, lon: Double) {
        val client = OkHttpClient()
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true&daily=temperature_2m_max,temperature_2m_min,weathercode,windspeed_10m_max&hourly=temperature_2m,weathercode,windspeed_10m&timezone=auto&alerts=true"
        val request = Request.Builder().url(url).build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && !body.isNullOrEmpty()) {
                        parseWeatherResponse(body)
                    } else {
                        withContext(Dispatchers.Main) { handleWeatherRequestFailure() }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { handleWeatherRequestFailure(e.localizedMessage) }
            }
        }
    }

    private suspend fun parseWeatherResponse(responseBody: String) {
        val json = JSONObject(responseBody)
        val currentWeather = json.getJSONObject("current_weather")
        val daily = json.getJSONObject("daily")
        val hourly = json.getJSONObject("hourly")

        val forecastList = parseDailyForecast(daily)
        val hourlyPreview = parseHourlyForecast(hourly)

        withContext(Dispatchers.Main) {
            showLoading(false)
            updateCurrentWeather(currentWeather)
            updateForecast(forecastList)
            updateAlerts(json.optJSONArray("alerts"))
            updateLastUpdated()
        }
    }

    private fun parseDailyForecast(daily: JSONObject): List<ForecastItem> {
        val forecastList = mutableListOf<ForecastItem>()
        val dates = daily.getJSONArray("time")
        val minTemps = daily.getJSONArray("temperature_2m_min")
        val maxTemps = daily.getJSONArray("temperature_2m_max")
        val weatherCodes = daily.getJSONArray("weathercode")
        val windSpeeds = daily.optJSONArray("windspeed_10m_max")  // Use optJSONArray for optional data
        val humidities = daily.optJSONArray("humidity_2m_max")
        val uvIndexes = daily.optJSONArray("uv_index_max")  // Use optJSONArray for uv_index_max

        for (i in 0 until dates.length()) {
            forecastList.add(ForecastItem(
                date = dates.getString(i),
                minTemp = minTemps.getDouble(i),
                maxTemp = maxTemps.getDouble(i),
                weatherCode = weatherCodes.getInt(i),
                windSpeed = windSpeeds?.getDouble(i) ?: 0.0,  // Default to 0 if wind speed is missing
                humidity = humidities?.getInt(i) ?: -1,  // Default to -1 if humidity is missing
                uvIndex = uvIndexes?.getInt(i) ?: -1  // Default to -1 if UV index is missing
            ))
        }
        return forecastList
    }



    private fun parseHourlyForecast(hourly: JSONObject): String {
        val hourlyTimes = hourly.getJSONArray("time")
        val hourlyTemps = hourly.getJSONArray("temperature_2m")
        val hourlyWeatherCodes = hourly.getJSONArray("weathercode")
        val hourlyWindSpeeds = hourly.getJSONArray("windspeed_10m") // Correct field for hourly wind speed

        val hourlyPreview = StringBuilder()
        for (i in 0 until minOf(5, hourlyTimes.length())) {
            val time = hourlyTimes.getString(i).substring(11, 16)
            val temp = hourlyTemps.getDouble(i)
            val code = hourlyWeatherCodes.getInt(i)
            val windSpeed = hourlyWindSpeeds.getDouble(i) // Extract wind speed
            val tempDisplayed = if (isCelsius) temp else (temp * 9/5) + 32
            val unit = if (isCelsius) "°C" else "°F"
            hourlyPreview.append("$time: ${String.format("%.1f", tempDisplayed)}$unit ${mapWeatherCodeToDescription(code)} Wind: ${String.format("%.1f", windSpeed)} km/h\n")
        }
        return hourlyPreview.toString()
    }

    private fun updateCurrentWeather(currentWeather: JSONObject) {
        val temp = currentWeather.getDouble("temperature")
        val code = currentWeather.getInt("weathercode")
        val windSpeed = currentWeather.optDouble("windspeed_10m", 0.0) // Safely handle missing wind speed
        val displayedTemp = if (isCelsius) temp else (temp * 9/5) + 32
        val unit = if (isCelsius) "°C" else "°F"

        binding.temperatureText.animate().alpha(0f).setDuration(200).withEndAction {
            binding.temperatureText.text = String.format(Locale.getDefault(), "%.1f%s", displayedTemp, unit)
            binding.weatherDescription.text = mapWeatherCodeToDescription(code)

            // Display the wind speed in the UI
            binding.windSpeedText.text = if (windSpeed > 0) {
                String.format(Locale.getDefault(), "Wind: %.1f km/h", windSpeed)
            } else {
                "Wind: N/A"  // Handle case where wind speed is unavailable
            }

            binding.temperatureText.alpha = 1f
        }.start()
    }

    private fun updateForecast(forecastList: List<ForecastItem>) {
        binding.forecastRecyclerView.adapter = ForecastAdapter(forecastList, isCelsius) { forecastItem ->
            showSnackbar("Clicked on ${forecastItem.date} weather!")
        }
    }

    // Method to show a snackbar message with an optional retry action
    private fun showSnackbar(message: String, retry: Boolean = false) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        if (retry) {
            snackbar.setAction("Retry") { checkLocationPermissionAndFetchWeather() }
        }
        snackbar.show()
    }

    // Method to handle location failure when location cannot be fetched
    private fun handleLocationFailure() {
        showSnackbar("Unable to retrieve location.", retry = true)
        showLoading(false)
    }

    // Method to handle failure when the weather request fails
    private fun handleWeatherRequestFailure(message: String = "Failed to fetch weather data.") {
        showSnackbar(message, retry = true)
        showLoading(false)
    }


    private fun updateAlerts(alertsArray: JSONArray?) {
        if (alertsArray?.length() ?: 0 > 0) {
            val event = alertsArray?.getJSONObject(0)?.getString("event")
            binding.extremeAlertCard.visibility = View.VISIBLE
            binding.extremeAlertsText.text = "⚠️ $event"
        } else {
            binding.extremeAlertCard.visibility = View.GONE
        }
    }

    private fun updateLastUpdated() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = sdf.format(Date())
        binding.lastUpdatedText.text = "Updated at: $currentTime"
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.show()
            binding.swipeRefreshLayout.isRefreshing = true
        } else {
            binding.progressBar.hide()
            binding.swipeRefreshLayout.isRefreshing = false
        }
        fetchingWeather = isLoading
    }

    private fun mapWeatherCodeToDescription(code: Int) = when (code) {
        0 -> "Clear Sky"
        1, 2, 3 -> "Partly Cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Rain Showers"
        95 -> "Thunderstorm"
        96, 99 -> "Severe Thunderstorm"
        else -> "Unknown"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Extension functions
    private fun View.show() { visibility = View.VISIBLE }
    private fun View.hide() { visibility = View.GONE }
}
