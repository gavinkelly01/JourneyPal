package com.example.journeypal

import android.webkit.WebView
import com.example.journeypal.ui.dashboard.GuideFragment
import io.mockk.*
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class GuideFragmentTest {

    private lateinit var fragment: GuideFragment
    private lateinit var mockWebView: WebView

    @Before
    fun setup() {
        fragment = GuideFragment()
        mockWebView = mockk(relaxed = true)
        fragment.webView = mockWebView
    }

    @Test
    fun toggleDarkMode_enablesDarkMode_whenInitiallyOff() {
        fragment.isDarkMode = false
        fragment.toggleDarkMode()
        assertTrue(fragment.isDarkMode)
        verify {
            mockWebView.evaluateJavascript(
                match { it.contains("background-color: #121212") },
                null
            )
        }
    }

    @Test
    fun toggleDarkMode_disablesDarkMode_whenInitiallyOn() {
        fragment.isDarkMode = true
        fragment.toggleDarkMode()
        assertFalse(fragment.isDarkMode)
        verify {
            mockWebView.evaluateJavascript(
                match { it.contains("style.remove();") },
                null
            )
        }
    }

    @Test
    fun loadTravelGuide_loadsExpectedContentIntoWebView() {
        fragment.loadTravelGuide()
        verify {
            mockWebView.loadDataWithBaseURL(
                null,
                match { it.contains("<html") && it.contains("Enhanced Travel Guide") },
                "text/html",
                "UTF-8",
                null
            )
        }
    }
}
