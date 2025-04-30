package com.example.journeypal

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.journeypal.ui.more.MoreFragment
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoreFragmentTest {
    @Test
    fun moreFragment_displaysMenuItemsCorrectly() {
        val scenario = launchFragmentInContainer<MoreFragment>(
            themeResId = R.style.Theme_JourneyPal
        )

        scenario.onFragment { fragment ->
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.recyclerViewMoreMenu)
            assertNotNull("RecyclerView should not be null", recyclerView)
            assertEquals(5, recyclerView?.adapter?.itemCount)
        }
    }
}
