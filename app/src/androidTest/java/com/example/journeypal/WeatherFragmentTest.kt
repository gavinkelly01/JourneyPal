package com.example.journeypal.ui.weather

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class WeatherFragmentTest {

    @Test
    fun parseDailyForecast_validJson_returnsCorrectList() {
        // Given
        val json = """
            {
              "time": ["2025-05-07", "2025-05-08"],
              "temperature_2m_min": [10.5, 12.3],
              "temperature_2m_max": [20.0, 22.8],
              "weathercode": [1, 2],
              "windspeed_10m_max": [15.0, 10.0],
              "humidity_2m_max": [60, 55],
              "uv_index_max": [5, 7]
            }
        """.trimIndent()

        val dailyJson = JSONObject(json)
        val fragment = WeatherFragment()

        // When
        val result = fragment.run {
            // Use reflection or make parseDailyForecast internal for testing
            val method = WeatherFragment::class.java.getDeclaredMethod("parseDailyForecast", JSONObject::class.java)
            method.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            method.invoke(this, dailyJson) as List<ForecastItem>
        }

        // Then
        assertEquals(2, result.size)
        assertEquals("2025-05-07", result[0].date)
        assertEquals(10.5, result[0].minTemp, 0.0)
        assertEquals(20.0, result[0].maxTemp, 0.0)
        assertEquals(1, result[0].weatherCode)
        assertEquals(15.0, result[0].windSpeed, 0.0)
        assertEquals(60, result[0].humidity)
        assertEquals(5, result[0].uvIndex)
    }
}
