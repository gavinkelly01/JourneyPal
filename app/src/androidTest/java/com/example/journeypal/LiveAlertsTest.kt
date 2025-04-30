package com.example.journeypal.ui.LiveAlerts

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class LiveAlertsTest {

    @Test
    fun parseNewsJson_validJson_returnsCorrectNewsList() {
        // Given
        val json = """
            {
              "data": [
                {
                  "title": "Alert in City A",
                  "link": "https://example.com/alert1",
                  "snippet": "News snippet 1",
                  "photo_url": "https://example.com/image1.jpg",
                  "thumbnail_url": "https://example.com/thumb1.jpg",
                  "published_datetime_utc": "2025-05-09T10:00:00Z"
                },
                {
                  "title": "Alert in City B",
                  "link": "https://example.com/alert2",
                  "snippet": "News snippet 2",
                  "photo_url": "https://example.com/image2.jpg",
                  "thumbnail_url": "https://example.com/thumb2.jpg",
                  "published_datetime_utc": "2025-05-09T11:00:00Z"
                }
              ]
            }
        """.trimIndent()

        val fragment = LiveAlerts()

        // When
        val result = fragment.parseNewsJson(json)

        // Then
        assertEquals(2, result.size)
        assertEquals("Alert in City A", result[0].title)
        assertEquals("https://example.com/alert1", result[0].link)
        assertEquals("News snippet 1", result[0].snippet)
        assertEquals("https://example.com/image1.jpg", result[0].photoUrl)
        assertEquals("2025-05-09T10:00:00Z", result[0].publishedAt)
    }
}
