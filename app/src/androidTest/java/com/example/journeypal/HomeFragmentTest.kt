package com.example.journeypal

import com.example.journeypal.ui.home.HomeFragment
import com.google.android.material.chip.Chip
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeFragmentTest {

    private lateinit var fragment: HomeFragment

    @Before
    fun setup() {
        fragment = HomeFragment()
    }

    @Test
    fun getSelectedFilters_returnsEmptyListWhenNoChipsAreChecked() {
        // Create mock Chip objects with all unchecked
        val chip1 = mockk<Chip>(relaxed = true)
        val chip2 = mockk<Chip>(relaxed = true)
        val chip3 = mockk<Chip>(relaxed = true)

        every { chip1.isChecked } returns false
        every { chip2.isChecked } returns false
        every { chip3.isChecked } returns false
        fragment.filterChips["women"] = chip1
        fragment.filterChips["lgbtq"] = chip2
        fragment.filterChips["people_of_color"] = chip3
        val result = fragment.getSelectedFilters()

        assertEquals(emptyList<String>(), result)
    }


    @Test
    fun getSelectedFilters_returnsOnlyCheckedFilters() {
        // Create fake Chip objects
        val chip1 = mockk<Chip>(relaxed = true)
        val chip2 = mockk<Chip>(relaxed = true)
        val chip3 = mockk<Chip>(relaxed = true)

        every { chip1.isChecked } returns true
        every { chip2.isChecked } returns false
        every { chip3.isChecked } returns true
        fragment.filterChips["women"] = chip1
        fragment.filterChips["lgbtq"] = chip2
        fragment.filterChips["people_of_color"] = chip3
        val result = fragment.getSelectedFilters()

        assertEquals(listOf("women", "people_of_color"), result)
    }
}
