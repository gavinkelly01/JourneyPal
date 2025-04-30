package com.example.journeypal.ui.weather

import com.example.journeypal.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.journeypal.databinding.ItemForecastBinding
import java.text.SimpleDateFormat
import java.util.*

class ForecastAdapter(
    private val forecastList: List<ForecastItem>,
    private val isCelsius: Boolean,
    private val onItemClicked: (ForecastItem) -> Unit
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(val binding: ItemForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = forecastList[position]
        holder.binding.apply {
            dayText.text = formatDate(item.date)
            minMaxTempText.text = "${item.minTemp}° / ${item.maxTemp}°"
            weatherDescText.text = mapWeatherCodeToDescription(item.weatherCode)
            weatherIcon.setImageResource(getIconForWeatherCode(item.weatherCode))

            // Bind wind speed
            windSpeedText.text = "${item.windSpeed} km/h"

            // Bind humidity
            humidityText.text = "${item.humidity}%"

            // Bind UV index
            uvIndexText.text = "UV Index: ${item.uvIndex}"

            // Set onClickListener
            root.setOnClickListener {
                onItemClicked(item)
            }
        }
    }

    override fun getItemCount() = forecastList.size

    private fun formatDate(dateStr: String): String {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("EEE", Locale.getDefault())
        return formatter.format(parser.parse(dateStr)!!)
    }

    private fun mapWeatherCodeToDescription(code: Int): String {
        return when (code) {
            0 -> "Clear"
            1, 2, 3 -> "Cloudy"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snow"
            80, 81, 82 -> "Rain Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Severe Storm"
            else -> "Unknown"
        }
    }

    private fun getIconForWeatherCode(code: Int): Int {
        return when (code) {
            0 -> R.drawable.ic_clear
            1, 2, 3 -> R.drawable.ic_cloudy
            45, 48 -> R.drawable.ic_fog
            51, 53, 55 -> R.drawable.ic_drizzle
            61, 63, 65 -> R.drawable.ic_rain
            71, 73, 75 -> R.drawable.ic_snow
            80, 81, 82 -> R.drawable.ic_showers
            95, 96, 99 -> R.drawable.ic_thunderstorm
            else -> R.drawable.ic_unknown
        }
    }
}
