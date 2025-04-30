package com.example.journeypal

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.journeypal.ui.dashboard.GuideFragment
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource

@RunWith(AndroidJUnit4::class)
class GuideFragmentTest {

    @Test
    fun testGuideFragmentUI() {
        // Launch the fragment in a container
        val scenario = launchFragmentInContainer<GuideFragment>(themeResId = R.style.Theme_JourneyPal)

        // Access the fragment via scenario.onFragment
        scenario.onFragment { fragment ->
            // Register the IdlingResource with the fragment instance
            val idlingResource = WebViewIdlingResource(fragment)
            IdlingRegistry.getInstance().register(idlingResource)

            // Proceed with your test actions, ensuring that WebView has finished loading before proceeding
            onView(withId(R.id.webView))
                .check(matches(isDisplayed()))

            // Click the toggle dark mode FAB
            onView(withId(R.id.fabToggleView))
                .perform(click())

            // Ensure the FAB is still visible after the click
            onView(withId(R.id.fabToggleView))
                .check(matches(isDisplayed()))

            // Open the drawer using DrawerActions
            onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open())

            // Ensure the drawer is open
            onView(withId(R.id.drawer_layout))
                .check(matches(DrawerMatchers.isOpen()))

            // Click on a sidebar item ("Women Travelers")
            onView(withText("Women Travelers"))
                .check(matches(isDisplayed()))
                .perform(click())

            // Add some delay or synchronization (although not recommended in production tests)
            Thread.sleep(500)

            // Re-open the drawer to return
            onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open())

            // Ensure the "Return to Main Guide" button is visible and click it
            onView(withText("Return to Main Guide"))
                .check(matches(isDisplayed()))
                .perform(click())

            // Unregister the IdlingResource after the test is finished
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
