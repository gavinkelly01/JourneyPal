package com.example.journeypal.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GuideViewModel : ViewModel() {
    private val _scamsLiveData = MutableLiveData<List<CountryGuide>>()
    val scamsLiveData: LiveData<List<CountryGuide>> get() = _scamsLiveData

    fun fetchScams(country: String) {
        val scams = getCountryGuideData(country)
        _scamsLiveData.value = scams
    }

    private fun getCountryGuideData(country: String): List<CountryGuide> {
        return listOf(
        )
    }
}
