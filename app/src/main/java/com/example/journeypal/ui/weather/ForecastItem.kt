package com.example.journeypal.ui.weather

data class ForecastItem(
    val date: String,
    val minTemp: Double,
    val maxTemp: Double,
    val weatherCode: Int,
    val windSpeed: Double,  // Wind speed in km/h
    val humidity: Int,      // Humidity in percentage
    val uvIndex: Int        // UV index
)
