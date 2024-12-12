package com.example.journeypal.ui.dashboard

data class CountryGuide(
    val countryName: String,      //The name of the country
    val safetyTips: String,       //Safety tips for the country
    val imageUrl: String,         //URL for the image to be displayed
    val videoUrl: String          //URL for the video to be displayed
)
