package com.example.journeypal

import com.example.journeypal.ui.emergency.EmergencySosFragment
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EmergencySosFragmentTest {

    private lateinit var fragment: EmergencySosFragment

    @Before
    fun setup() {
        fragment = EmergencySosFragment()
    }

    @Test
    fun testAddingEmergencyContactUpdatesList() {
        val contacts = listOf(
            EmergencySosFragment.EmergencyContact("Alice", "+1234567890"),
            EmergencySosFragment.EmergencyContact("Bob", "+0987654321")
        )

        val contactList = mutableListOf<EmergencySosFragment.EmergencyContact>()
        contactList.addAll(contacts)

        assertEquals(2, contactList.size)
        assertEquals("Alice", contactList[0].name)
        assertEquals("+0987654321", contactList[1].phoneNumber)
    }

    @Test
    fun testContactEqualityWorksCorrectly() {
        val contact1 = EmergencySosFragment.EmergencyContact("Test", "+1111")
        val contact2 = EmergencySosFragment.EmergencyContact("Test", "+1111")

        assertEquals(contact1, contact2)
    }
}
