package com.example.journeypal

import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.journeypal.ui.dashboard.CountryGuide
import com.example.journeypal.ui.dashboard.GuideFragment
import com.example.journeypal.ui.dashboard.GuideViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // Make sure Robolectric SDK version is set correctly
@LooperMode(LooperMode.Mode.PAUSED) // Handle looper in tests
class GuideTest {

    private lateinit var fragment: GuideFragment
    private lateinit var mockWebView: WebView
    private lateinit var mockDrawerLayout: DrawerLayout
    private lateinit var mockFabToggleView: FloatingActionButton
    private lateinit var mockFabOpenSidebar: FloatingActionButton
    private lateinit var mockGuideViewModel: GuideViewModel
    private lateinit var mockObserver: Observer<List<CountryGuide>>
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        fragment = GuideFragment()
        fragment.arguments = Bundle()

        // Initialize the real WebView for Robolectric testing
        mockWebView = WebView(activity)
        fragment.webView = mockWebView

        // Initialize other mock objects
        mockDrawerLayout = mock(DrawerLayout::class.java)
        mockFabToggleView = mock(FloatingActionButton::class.java)
        mockFabOpenSidebar = mock(FloatingActionButton::class.java)
        mockGuideViewModel = mock(GuideViewModel::class.java)
        mockObserver = mock(Observer::class.java) as Observer<List<CountryGuide>>
        mockContext = mock(Context::class.java)

        // Assign mocks to the fragment
        fragment.guideViewModel = mockGuideViewModel
        fragment.drawerLayout = mockDrawerLayout
        fragment.fabToggleView = mockFabToggleView
        fragment.fabOpenSidebar = mockFabOpenSidebar

        // Mock context
        `when`(fragment.requireContext()).thenReturn(mockContext)

        val mockLiveData: LiveData<List<CountryGuide>> = mock()
        `when`(mockGuideViewModel.scamsLiveData).thenReturn(mockLiveData)

        // Attach the fragment to the activity
        activity.supportFragmentManager.beginTransaction().add(fragment, null).commitNow()
    }

    @Test
    fun testWebViewInitialization() {
        // Verify WebView settings are correctly initialized
        assert(mockWebView.settings.useWideViewPort)
        assert(mockWebView.settings.loadWithOverviewMode)
        assert(mockWebView.settings.javaScriptEnabled)
    }

    @Test
    fun testDarkModeToggle() {
        // Simulate toggling dark mode
        fragment.toggleDarkMode()

        // Robolectric can handle real WebView interaction
        val script = "document.body.style.backgroundColor = 'black';"
        val resultCallback: ValueCallback<String> = mock()

        // Now call evaluateJavascript with both parameters
        mockWebView.evaluateJavascript(script, resultCallback)

        // Assert that evaluateJavascript is called with the script and callback
        verify(mockWebView).evaluateJavascript(eq(script), eq(resultCallback))
    }

    @Test
    fun testOpenSidebar() {
        // Simulate the FAB click to open the sidebar
        fragment.fabOpenSidebar.performClick()

        // Verify the DrawerLayout's openDrawer method is called with correct parameters
        verify(mockDrawerLayout).openDrawer(GravityCompat.START)
    }

    @Test
    fun testSidebarItemsSetup() {
        val view = mock(View::class.java)
        val mockSidebarContent = mock(LinearLayout::class.java)
        `when`(view.findViewById<LinearLayout>(R.id.sidebar_content)).thenReturn(mockSidebarContent)

        fragment.setupSidebarItems(view)

        // Verify that sidebar content is populated with the correct categories
        verify(mockSidebarContent, atLeastOnce()).addView(any(TextView::class.java))
    }

    @Test
    fun testLoadTravelGuide() {
        // Simulate loading the travel guide
        fragment.loadTravelGuide()

        // Verify the WebView's loadDataWithBaseURL method is called with correct parameters
        verify(mockWebView).loadDataWithBaseURL(
            eq("http://example.com"),
            any(),
            eq("text/html"),
            eq("UTF-8"),
            any()
        )
    }
}
