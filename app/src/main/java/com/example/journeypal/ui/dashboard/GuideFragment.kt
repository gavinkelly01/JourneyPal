package com.example.journeypal.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.journeypal.R
import androidx.lifecycle.Observer

class GuideFragment : Fragment() {

    private lateinit var countryGuideAdapter: CountryGuideAdapter
    private lateinit var guideViewModel: GuideViewModel
    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guide, container, false)
        guideViewModel = ViewModelProvider(this).get(GuideViewModel::class.java)
        webView = view.findViewById(R.id.webView)
        loadTravelGuide()
        countryGuideAdapter = CountryGuideAdapter(requireContext())
        guideViewModel.scamsLiveData.observe(viewLifecycleOwner, Observer { countryGuides ->
            countryGuideAdapter.submitList(countryGuides)
        })

        guideViewModel.fetchScams("Thailand")
        return view
    }

    override fun onResume() {
        super.onResume()
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onPause() {
        super.onPause()
        activity?.actionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun loadTravelGuide() {
        val travelGuideContent = """
        <html>
            <head>
                <style>
                    /* General body styling */
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #f4f4f9;
                        margin: 0;
                        padding: 0;
                        color: #333;
                    }

                    /* Container to center content with padding */
                    .container {
                        max-width: 1200px;
                        margin: 0 auto;
                        padding: 30px;
                    }

                    /* Heading styles */
                    h1 {
                        font-size: 2.5em;
                        color: #2c3e50;
                        margin-bottom: 20px;
                        text-align: center;
                    }

                    h2 {
                        font-size: 1.8em;
                        color: #34495e;
                        margin-top: 40px;
                        border-bottom: 2px solid #3498db;
                        padding-bottom: 10px;
                    }

                    /* Paragraph styling */
                    p {
                        font-size: 1.1em;
                        line-height: 1.7;
                        color: #555;
                    }

                    /* Styling for unordered lists */
                    ul {
                        padding-left: 20px;
                        font-size: 1.1em;
                        color: #555;
                    }

                    ul li {
                        margin-bottom: 10px;
                    }

                    /* Styling for links */
                    a {
                        color: #3498db;
                        text-decoration: none;
                    }

                    a:hover {
                        text-decoration: underline;
                    }

                    /* Styling for images */
                    img {
                        width: 100%;
                        height: auto;
                        border-radius: 8px;
                        margin-top: 20px;
                    }

                    /* Video player styling */
                    video {
                        width: 100%;
                        border-radius: 8px;
                        margin-top: 20px;
                    }

                    /* Section with background color for emphasis */
                    .highlight {
                        background-color: #ecf0f1;
                        padding: 20px;
                        border-radius: 8px;
                        margin-top: 30px;
                    }

                    /* Styling for code blocks or steps */
                    .step {
                        background-color: #fff;
                        border: 1px solid #ddd;
                        padding: 15px;
                        margin-top: 20px;
                        border-radius: 5px;
                    }

                    .step-title {
                        font-weight: bold;
                        color: #2c3e50;
                    }

                    .step-content {
                        font-size: 1.1em;
                        color: #555;
                    }

                    /* Responsive design for smaller screens */
                    @media (max-width: 768px) {
                        .container {
                            padding: 15px;
                        }

                        h1 {
                            font-size: 2em;
                        }

                        h2 {
                            font-size: 1.6em;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Travel Guide to Stay Safe</h1>

                    <h2>1. Research Before Booking Tickets</h2>
                    <p>Before booking any tickets, do thorough research on your destination. Here are some key things to consider:</p>
                    <ul>
                        <li>Learn about the country's culture, laws, and customs.</li>
                        <li>Research common scams to be aware of (e.g., fake taxi services, overcharging for goods).</li>
                        <li>Check for visa requirements and health and safety guidelines.</li>
                        <li>Consider travel insurance, especially if traveling to a destination with health risks.</li>
                    </ul>

                    <h2>2. Book Your Tickets</h2>
                    <p>Use flight comparison websites like Skyscanner, Google Flights, and Kayak to find the best flight deals.</p>

                    <h2>3. Prepare for the Airport</h2>
                    <p>Make sure you’re prepared by following these steps:</p>
                    <ul>
                        <li>Check-in online to save time.</li>
                        <li>Ensure all your documents (passport, visa) are in order.</li>
                        <li>Pack essentials like toiletries, medications, and a first-aid kit.</li>
                        <li>Exchange some currency before you travel.</li>
                    </ul>

                    <h2>4. Airport and Transport Tips</h2>
                    <p>Be alert for scams at the airport. Use only authorized taxis and avoid unsolicited offers of help from strangers.</p>

                    <h2>5. Stay Safe in Hotels</h2>
                    <p>When you check into a hotel, make sure to:</p>
                    <ul>
                        <li>Inspect the room for hidden devices (e.g., hidden cameras, listening devices).</li>
                        <li>Use the hotel safe for valuables like your passport, electronics, and money.</li>
                        <li>Secure your room with the lock, safety chain, or deadbolt.</li>
                    </ul>

                    <h2>Images and Video</h2>
                    <p>Here's a useful video on staying safe while traveling:</p>
                    <video controls>
                        <source src="https://www.youtube.com/watch?v=2i3j6bs" type="video/mp4">
                        Your browser does not support the video tag.
                    </video>

                    <h2>Example Travel Image</h2>
                    <img src="https://www.pexels.com/photo/white-sand-beach-1320639/" alt="Travel Beach">

                    <!-- Highlighted Section -->
                    <div class="highlight">
                        <h2>Quick Safety Tips</h2>
                        <ul>
                            <li>Always keep your valuables close to you.</li>
                            <li>Use reputable transportation services.</li>
                            <li>Stay alert in crowded places.</li>
                        </ul>
                    </div>

                    <!-- Step-by-Step Guide -->
                    <div class="step">
                        <div class="step-title">Step 1: Prepare for Your Trip</div>
                        <div class="step-content">Research your destination thoroughly and plan accordingly.</div>
                    </div>

                </div>
            </body>
        </html>
    """

        webView.loadDataWithBaseURL(null, travelGuideContent, "text/html", "UTF-8", null)
    }

}
