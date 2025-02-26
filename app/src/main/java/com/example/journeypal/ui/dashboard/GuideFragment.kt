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
     <html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Enhanced Travel Guide</title>
    <style>
        :root {
            --primary-color: #3498db;
            --secondary-color: #2ecc71;
            --dark-color: #2c3e50;
            --light-color: #f4f4f9;
            --accent-color: #e74c3c;
            --text-color: #333;
            --light-text: #555;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: var(--light-color);
            margin: 0;
            padding: 0;
            color: var(--text-color);
            transition: background-color 0.3s ease-in-out;
            line-height: 1.6;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 30px;
        }
        
        header {
            text-align: center;
            padding: 40px 0;
            background: linear-gradient(135deg, var(--primary-color), #2980b9);
            color: white;
            border-radius: 12px;
            margin-bottom: 30px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
            position: relative;
            overflow: hidden;
            animation: headerGlow 3s infinite alternate;
        }
        
        @keyframes headerGlow {
            0% {
                box-shadow: 0 4px 15px rgba(52, 152, 219, 0.2);
            }
            100% {
                box-shadow: 0 4px 25px rgba(52, 152, 219, 0.6);
            }
        }
        
        header::before {
            content: "";
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 70%);
            transform: rotate(45deg);
            animation: headerShine 5s linear infinite;
        }
        
        @keyframes headerShine {
            0% {
                transform: rotate(45deg) translateY(0);
            }
            100% {
                transform: rotate(45deg) translateY(100%);
            }
        }
        
        header img {
            width: 150px;
            margin-bottom: 20px;
            animation: float 6s ease-in-out infinite;
            filter: drop-shadow(0 5px 15px rgba(0,0,0,0.1));
        }
        
        @keyframes float {
            0%, 100% {
                transform: translateY(0);
            }
            50% {
                transform: translateY(-10px);
            }
        }
        
        h1 {
            font-size: 2.8em;
            color: var(--dark-color);
            margin-bottom: 20px;
            text-align: center;
            border-bottom: 3px solid var(--primary-color);
            padding-bottom: 15px;
            animation: fadeInUp 1s ease-in-out;
            position: relative;
        }
        
        h1::after {
            content: "";
            position: absolute;
            bottom: -3px;
            left: 50%;
            width: 0;
            height: 3px;
            background-color: var(--secondary-color);
            animation: borderExpand 2s ease-in-out forwards;
            transform: translateX(-50%);
        }
        
        @keyframes borderExpand {
            0% {
                width: 0;
            }
            100% {
                width: 50%;
            }
        }
        
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        h2 {
            font-size: 1.8em;
            color: var(--dark-color);
            margin-top: 50px;
            border-bottom: 2px solid var(--primary-color);
            padding-bottom: 10px;
            position: relative;
            animation: fadeInLeft 0.8s ease-in-out;
            display: flex;
            align-items: center;
        }
        
        @keyframes fadeInLeft {
            from {
                opacity: 0;
                transform: translateX(-30px);
            }
            to {
                opacity: 1;
                transform: translateX(0);
            }
        }
        
        h2::before {
            content: "✈️";
            margin-right: 10px;
            font-size: 0.8em;
            animation: spinIn 1s ease-in-out;
        }
        
        @keyframes spinIn {
            from {
                transform: rotate(-180deg);
                opacity: 0;
            }
            to {
                transform: rotate(0);
                opacity: 1;
            }
        }
        
        p, ul {
            font-size: 1.1em;
            line-height: 1.8;
            color: var(--light-text);
            animation: fadeIn 1s ease-in-out;
        }
        
        ul {
            padding-left: 20px;
        }
        
        ul li {
            margin-bottom: 12px;
            transition: all 0.3s ease;
            position: relative;
            padding-left: 10px;
            animation: fadeInRight 0.8s ease-in-out;
        }
        
        @keyframes fadeInRight {
            from {
                opacity: 0;
                transform: translateX(30px);
            }
            to {
                opacity: 1;
                transform: translateX(0);
            }
        }
        
        ul li::before {
            content: "•";
            color: var(--primary-color);
            font-weight: bold;
            display: inline-block;
            width: 1em;
            margin-left: -1em;
            animation: pulse 2s infinite;
        }
        
        @keyframes pulse {
            0% {
                transform: scale(1);
            }
            50% {
                transform: scale(1.2);
            }
            100% {
                transform: scale(1);
            }
        }
        
        ul li:hover {
            color: var(--primary-color);
            transform: translateX(5px);
        }
        
        strong {
            color: var(--dark-color);
            font-weight: 600;
            border-bottom: 1px dotted var(--primary-color);
        }
        
        a {
            color: var(--primary-color);
            text-decoration: none;
            transition: all 0.3s ease;
            position: relative;
        }
        
        a:hover {
            color: var(--secondary-color);
        }
        
        a::after {
            content: "";
            position: absolute;
            width: 100%;
            height: 2px;
            bottom: -2px;
            left: 0;
            background-color: var(--secondary-color);
            transform: scaleX(0);
            transform-origin: bottom right;
            transition: transform 0.3s ease-out;
        }
        
        a:hover::after {
            transform: scaleX(1);
            transform-origin: bottom left;
        }
        
        img {
            width: 100%;
            height: auto;
            border-radius: 12px;
            margin-top: 20px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;
            animation: zoomIn 1s ease;
        }
        
        @keyframes zoomIn {
            from {
                opacity: 0;
                transform: scale(0.9);
            }
            to {
                opacity: 1;
                transform: scale(1);
            }
        }
        
        img:hover {
            transform: scale(1.02);
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
        }
        
        video, iframe {
            width: 100%;
            border-radius: 12px;
            margin-top: 20px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;
            animation: fadeIn 1s ease;
        }
        
        video:hover, iframe:hover {
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
        }
        
        .highlight {
            background: linear-gradient(135deg, #ecf0f1, #e8f4f8);
            padding: 25px;
            border-radius: 12px;
            margin-top: 30px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
            transition: all 0.3s ease;
            border-left: 5px solid var(--primary-color);
            animation: slideInUp 1s ease;
        }
        
        @keyframes slideInUp {
            from {
                opacity: 0;
                transform: translateY(50px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .highlight:hover {
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
            transform: translateY(-5px);
        }
        
        .step {
            background-color: white;
            border: 1px solid #e0e0e0;
            padding: 20px;
            margin-top: 25px;
            border-radius: 12px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
            transition: all 0.3s ease;
            animation: fadeInUp 0.8s ease-in-out;
            position: relative;
            overflow: hidden;
        }
        
        .step::before {
            content: "";
            position: absolute;
            left: 0;
            top: 0;
            height: 100%;
            width: 5px;
            background: linear-gradient(to bottom, var(--primary-color), var(--secondary-color));
        }
        
        .step:hover {
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
            transform: translateY(-5px);
        }
        
        .step-title {
            font-weight: bold;
            color: var(--dark-color);
            font-size: 1.3em;
            margin-bottom: 10px;
            display: flex;
            align-items: center;
        }
        
        .step-title::before {
            content: "➡️";
            margin-right: 10px;
            animation: bounceRight 2s infinite;
        }
        
        @keyframes bounceRight {
            0%, 100% {
                transform: translateX(0);
            }
            50% {
                transform: translateX(10px);
            }
        }
        
        .step-content {
            font-size: 1.1em;
            color: var(--light-text);
        }
        
        .card-container {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
            margin-top: 30px;
            animation: fadeIn 1s ease;
        }
        
        .card {
            flex: 1 1 300px;
            background: white;
            border-radius: 12px;
            padding: 20px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
            transition: all 0.3s ease;
            border-top: 5px solid var(--primary-color);
            animation: fadeInUp 0.8s ease-in-out;
        }
        
        .card:nth-child(2) {
            border-top-color: var(--secondary-color);
            animation-delay: 0.2s;
        }
        
        .card:nth-child(3) {
            border-top-color: var(--accent-color);
            animation-delay: 0.4s;
        }
        
        .card:hover {
            transform: translateY(-10px);
            box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
        }
        
        .card-title {
            font-size: 1.4em;
            color: var(--dark-color);
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 2px solid #f0f0f0;
            display: flex;
            align-items: center;
        }
        
        .card-title i {
            margin-right: 10px;
            color: var(--primary-color);
        }
        
        .quote {
            font-style: italic;
            padding: 20px;
            background-color: rgba(52, 152, 219, 0.1);
            border-left: 5px solid var(--primary-color);
            margin: 20px 0;
            border-radius: 5px;
            position: relative;
            animation: fadeIn 1s ease;
        }
        
        .quote::before, .quote::after {
            content: '"';
            font-size: 4em;
            position: absolute;
            opacity: 0.2;
            color: var(--primary-color);
        }
        
        .quote::before {
            top: -20px;
            left: 10px;
        }
        
        .quote::after {
            bottom: -50px;
            right: 10px;
        }
        
        .alert {
            background-color: rgba(231, 76, 60, 0.1);
            border-left: 5px solid var(--accent-color);
            padding: 15px;
            margin: 20px 0;
            border-radius: 5px;
            animation: pulse 2s infinite;
        }
        
        .alert-title {
            color: var(--accent-color);
            font-weight: bold;
            margin-bottom: 5px;
            display: flex;
            align-items: center;
        }
        
        .alert-title::before {
            content: "⚠️";
            margin-right: 10px;
        }
        
        .progress-container {
            background-color: #f0f0f0;
            border-radius: 10px;
            margin: 20px 0;
            height: 20px;
            overflow: hidden;
        }
        
        .progress-bar {
            height: 100%;
            border-radius: 10px;
            background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
            animation: progressAnimation 3s ease-in-out forwards;
            width: 0;
        }
        
        @keyframes progressAnimation {
            from {
                width: 0;
            }
            to {
                width: 75%;
            }
        }
        
        .tooltip {
            position: relative;
            display: inline-block;
            border-bottom: 1px dotted black;
            cursor: help;
        }
        
        .tooltip .tooltip-text {
            visibility: hidden;
            width: 200px;
            background-color: var(--dark-color);
            color: #fff;
            text-align: center;
            border-radius: 6px;
            padding: 5px;
            position: absolute;
            z-index: 1;
            bottom: 125%;
            left: 50%;
            margin-left: -100px;
            opacity: 0;
            transition: opacity 0.3s;
        }
        
        .tooltip:hover .tooltip-text {
            visibility: visible;
            opacity: 1;
        }
        
        .image-with-caption {
            position: relative;
            margin: 30px 0;
            animation: fadeIn 1s ease;
        }
        
        .image-caption {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            background: rgba(0, 0, 0, 0.7);
            color: white;
            padding: 10px;
            border-bottom-left-radius: 12px;
            border-bottom-right-radius: 12px;
            transform: translateY(100%);
            opacity: 0;
            transition: all 0.3s ease;
        }
        
        .image-with-caption:hover .image-caption {
            transform: translateY(0);
            opacity: 1;
        }
        
        /* Dark mode toggle */
        #dark-mode-toggle {
            position: fixed;
            top: 20px;
            right: 20px;
            background-color: var(--dark-color);
            color: white;
            border: none;
            border-radius: 50%;
            width: 50px;
            height: 50px;
            cursor: pointer;
            z-index: 1000;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5em;
            transition: all 0.3s ease;
        }
        
        #dark-mode-toggle:hover {
            transform: rotate(180deg);
            background-color: var(--primary-color);
        }
        
        /* Dark mode styles */
        .dark-mode {
            background-color: #1a1a2e;
            color: #f0f0f0;
        }
        
        .dark-mode .container {
            background-color: #1a1a2e;
        }
        
        .dark-mode h1, .dark-mode h2 {
            color: #e0e0e0;
        }
        
        .dark-mode p, .dark-mode ul li {
            color: #b0b0b0;
        }
        
        .dark-mode .step, .dark-mode .card {
            background-color: #16213e;
            border-color: #0f3460;
        }
        
        .dark-mode .highlight {
            background: linear-gradient(135deg, #16213e, #0f3460);
        }
        
        .dark-mode strong {
            color: #4cc9f0;
        }
        
        /* Animation for page load */
        .page-transition {
            animation: pageTransition 1s ease;
        }
        
        @keyframes pageTransition {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        /* Loading animation */
        .loading {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            width: 100%;
            position: fixed;
            top: 0;
            left: 0;
            background-color: var(--light-color);
            z-index: 9999;
            animation: fadeOut 0.5s 2s forwards;
        }
        
        @keyframes fadeOut {
            from {
                opacity: 1;
            }
            to {
                opacity: 0;
                visibility: hidden;
            }
        }
        
        .loading-spinner {
            border: 5px solid #f3f3f3;
            border-top: 5px solid var(--primary-color);
            border-radius: 50%;
            width: 50px;
            height: 50px;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            0% {
                transform: rotate(0deg);
            }
            100% {
                transform: rotate(360deg);
            }
        }
        
        /* Scroll to top button */
        #scroll-to-top {
            display: none;
            position: fixed;
            bottom: 20px;
            right: 20px;
            background-color: var(--primary-color);
            color: white;
            border: none;
            border-radius: 50%;
            width: 50px;
            height: 50px;
            cursor: pointer;
            z-index: 1000;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
            transition: all 0.3s ease;
        }
        
        #scroll-to-top:hover {
            background-color: var(--dark-color);
            transform: translateY(-5px);
        }
        
        @media (max-width: 768px) {
            header {
                padding: 15px 0;
            }
            
            h1 {
                font-size: 2em;
            }
            
            h2 {
                font-size: 1.6em;
            }
            
            .container {
                padding: 15px;
            }
            
            .card-container {
                flex-direction: column;
            }
            
            #dark-mode-toggle, #scroll-to-top {
                width: 40px;
                height: 40px;
                font-size: 1.2em;
            }
        }

        /* Responsive iframe for videos */
        .video-container {
            position: relative;
            padding-bottom: 56.25%; /* 16:9 aspect ratio */
            height: 0;
            margin: 20px 0;
        }
        
        .video-container iframe {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            border-radius: 12px;
        }
        
        /* Table styles */
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
            animation: fadeIn 1s ease;
        }
        
        th, td {
            padding: 12px 15px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        
        th {
            background-color: var(--primary-color);
            color: white;
        }
        
        tr:nth-child(even) {
            background-color: rgba(52, 152, 219, 0.1);
        }
        
        tr:hover {
            background-color: rgba(52, 152, 219, 0.2);
        }
        
        /* Counter for steps */
        .steps-list {
            counter-reset: step-counter;
            list-style-type: none;
            padding-left: 0;
        }
        
        .steps-list li {
            counter-increment: step-counter;
            position: relative;
            padding-left: 40px;
            margin-bottom: 20px;
        }
        
        .steps-list li::before {
            content: counter(step-counter);
            position: absolute;
            left: 0;
            top: 0;
            width: 30px;
            height: 30px;
            background-color: var(--primary-color);
            color: white;
            border-radius: 50%;
            display: flex;
            justify-content: center;
            align-items: center;
            animation: pulse 2s infinite;
        }
    </style>
</head>
<body>
    <!-- Loading animation -->
    <div class="loading">
        <div class="loading-spinner"></div>
    </div>
    
    <!-- Dark mode toggle -->
    <button id="dark-mode-toggle">🌓</button>
    
    <!-- Scroll to top button -->
    <button id="scroll-to-top" style="display: none;">↑</button>
    
    <div class="container page-transition">
        <header>
            <h1>Travel Guide to Stay Safe Online and Offline</h1>
            <p>Your comprehensive resource for safe and enjoyable travels</p>
        </header>

        <div class="card-container">
            <div class="card">
                <div class="card-title"><i>🔍</i> Research</div>
                <p>Thorough preparation is the key to safe travels. Research your destination's culture, laws, and common scams.</p>
            </div>
            <div class="card">
                <div class="card-title"><i>💻</i> Online Safety</div>
                <p>Protect your digital information with VPNs, strong passwords, and cautious browsing habits.</p>
            </div>
            <div class="card">
                <div class="card-title"><i>🛡️</i> Physical Security</div>
                <p>Stay alert in unfamiliar surroundings and protect your belongings from theft or loss.</p>
            </div>
        </div>

        <div class="quote">
            "The world is a book, and those who do not travel read only one page. Stay safe while exploring new chapters of this magnificent book."
        </div>

        <h2>1. Research Before Booking Tickets</h2>
        <p>Before booking any tickets, it's crucial to do thorough research on your destination. Here are some key things to consider:</p>
        <ul>
            <li>Learn about the country's culture, laws, and customs. Respecting local traditions can help avoid uncomfortable situations.</li>
            <li>Research common scams to be aware of (e.g., fake taxi services, overcharging for goods).</li>
            <li>Check for visa requirements and health and safety guidelines for your destination.</li>
            <li>Consider travel insurance, especially if traveling to a destination with health risks or political instability.</li>
        </ul>

        <div class="progress-container">
            <div class="progress-bar"></div>
        </div>

        <div class="alert">
            <div class="alert-title">Important Reminder</div>
            <p>Always check travel advisories from your government before booking tickets to ensure your destination is safe to visit.</p>
        </div>

        <h2>2. Book Your Tickets</h2>
        <p>Use flight comparison websites like Skyscanner, Google Flights, and Kayak to find the best deals. Always double-check the airline's website for any hidden fees or restrictions before booking.</p>
        
        <div class="step">
            <div class="step-title">Tips for Finding the Best Deals</div>
            <div class="step-content">
                <ul>
                    <li>Book flights 2-3 months in advance for the best prices</li>
                    <li>Consider flying mid-week (Tuesday or Wednesday) for lower fares</li>
                    <li>Use incognito mode when searching to avoid price increases based on your browsing history</li>
                    <li>Sign up for price alerts to get notified when fares drop</li>
                </ul>
            </div>
        </div>

        <h2>3. Prepare for the Airport</h2>
        <p>Before heading to the airport, make sure you're prepared by following these steps:</p>
        <ul class="steps-list">
            <li>Check-in online to save time and avoid long queues.</li>
            <li>Ensure all your documents (passport, visa) are in order. Keep both hard and soft copies of important documents.</li>
            <li>Pack essentials like toiletries, medications, a first-aid kit, and any necessary documents like tickets and travel insurance info.</li>
            <li>Exchange some currency before you travel to avoid high exchange rates at the airport.</li>
        </ul>

        <h2>4. Airport and Transport Tips</h2>
        <div class="image-with-caption">
            <img src="https://i.pinimg.com/736x/09/73/44/0973445fc7fba799c5a2c12194c18245.jpg" alt="Airport Security" style="width:100%; height:auto;"/>
            <div class="image-caption">Stay vigilant while navigating through busy airport terminals</div>
        </div>

        <p>At the airport, be alert for scams. Use only authorized taxis or rideshare apps like Uber or Lyft. Avoid unsolicited offers of help from strangers. Here are some additional tips to ensure a smooth airport experience:</p>
        <ul>
            <li><span class="tooltip">Check-in Online<span class="tooltip-text">Most airlines allow check-in 24-48 hours before departure</span></span>: Save time and avoid long queues by checking in online before you arrive at the airport.</li>
            <li><strong>Use a Mobile Boarding Pass:</strong> Save paper and avoid losing your boarding pass by using a mobile version on your phone.</li>
            <li><strong>Arrive Early:</strong> Arrive at least two hours before a domestic flight or three hours before an international flight to ensure you have enough time for security checks.</li>
            <li><strong>Pack Smart:</strong> Make sure to pack your liquids and gels in compliance with TSA regulations (3.4 ounces or less in a quart-sized bag).</li>
            <li><strong>Keep Essentials Accessible:</strong> Have your passport, boarding pass, and any other important documents ready to show during check-in and security screening.</li>
            <li><strong>Be Aware of Security Rules:</strong> Familiarize yourself with security guidelines, like taking off shoes or belts, before reaching the security checkpoint to make the process faster.</li>
            <li><strong>Avoid Bringing Restricted Items:</strong> Don't pack items like sharp objects or anything prohibited by airport security. Check the airport's list of restricted items before packing.</li>
            <li><strong>Wear Easy-to-remove Clothing:</strong> Wear shoes and belts that are easy to remove, so you can breeze through security without delays.</li>
            <li><strong>Watch for Scams:</strong> Be cautious of individuals offering to help with luggage or provide assistance at a price. Only trust authorized airport staff or official services.</li>
            <li><strong>Stay Aware of Your Surroundings:</strong> Airports are busy places, so always keep an eye on your bags, especially when you're near crowded areas like check-in counters or food courts.</li>
        </ul>

        <table>
            <tr>
                <th>Item</th>
                <th>Carry-on</th>
                <th>Checked Baggage</th>
            </tr>
            <tr>
                <td>Liquids (>3.4oz/100ml)</td>
                <td>❌ Not Allowed</td>
                <td>✅ Allowed</td>
            </tr>
            <tr>
                <td>Electronics</td>
                <td>✅ Allowed</td>
                <td>✅ Allowed (but not recommended)</td>
            </tr>
            <tr>
                <td>Sharp Objects</td>
                <td>❌ Not Allowed</td>
                <td>✅ Allowed</td>
            </tr>
            <tr>
    <tr>
                <td>Medications</td>
                <td>✅ Allowed</td>
                <td>✅ Allowed</td>
            </tr>
            <tr>
                <td>Batteries</td>
                <td>✅ Allowed (with restrictions)</td>
                <td>❌ Some not allowed</td>
            </tr>
            <tr>
                <td>Lighters/Matches</td>
                <td>Limited (usually one lighter)</td>
                <td>❌ Not Allowed</td>
            </tr>
        </table>

        <h2>5. Digital and Cybersecurity</h2>
        <p>In today's digital age, protecting your online presence while traveling is just as important as physical safety:</p>
        <ul>
            <li>Use a VPN (Virtual Private Network) when connecting to public Wi-Fi networks to encrypt your data.</li>
            <li>Enable two-factor authentication on all your important accounts.</li>
            <li>Be careful about what you post on social media - avoid sharing real-time location updates.</li>
            <li>Use strong, unique passwords for different accounts.</li>
            <li>Consider using a dedicated travel email for bookings to minimize spam to your primary account.</li>
        </ul>

        <div class="highlight">
            <h3>Pro Tip: Secure Your Devices</h3>
            <p>Before traveling, back up all your devices and enable remote wipe features. This way, if your device is lost or stolen, you can erase sensitive data remotely.</p>
        </div>

        <h2>6. Accommodation Safety</h2>
        <p>Your accommodation should be a safe haven. Here's how to ensure that:</p>
        <ul>
            <li>Research neighborhoods before booking and opt for areas known to be safe for tourists.</li>
            <li>Read reviews from previous guests, focusing on comments about safety and security.</li>
            <li>Upon arrival, check that all locks on doors and windows work properly.</li>
            <li>Use the room safe for valuables or consider portable travel locks for additional security.</li>
            <li>Have a backup accommodation option in case your booking falls through.</li>
        </ul>

        <h2>7. Local Transportation</h2>
        <p>Navigating transportation in a new place can be challenging:</p>
        <ul>
            <li>Use reputable transportation services and avoid unmarked taxis.</li>
            <li>Have your destination written down in the local language to show drivers.</li>
            <li>Download offline maps of your destination before you arrive.</li>
            <li>Learn basic phrases in the local language related to transportation.</li>
            <li>Be aware of common transportation scams at your destination.</li>
        </ul>

        <div class="step">
            <div class="step-title">Avoiding Taxi Scams</div>
            <div class="step-content">
                <ol>
                    <li>Confirm the taxi is licensed and registered</li>
                    <li>Agree on a fare before starting the journey or ensure the meter is running</li>
                    <li>Keep Google Maps open on your phone to ensure you're taking the correct route</li>
                    <li>Take a photo of the taxi's license plate and share it with someone you trust</li>
                </ol>
            </div>
        </div>

        <h2>8. Health and Safety Precautions</h2>
        <p>Stay healthy during your travels with these important precautions:</p>
        <ul>
            <li>Research any required vaccinations well in advance.</li>
            <li>Pack a basic first-aid kit with essentials like bandages, pain relievers, and any prescription medications.</li>
            <li>Check if your health insurance covers you abroad; if not, consider travel health insurance.</li>
            <li>Know the emergency numbers and location of the nearest hospital or clinic.</li>
            <li>Be cautious with street food and drinking water in certain countries.</li>
        </ul>

        <div class="alert">
            <div class="alert-title">Health Advisory</div>
            <p>Always check the latest health advisories for your destination and consider travel insurance that covers medical emergencies and evacuation if needed.</p>
        </div>

        <h2>9. Stay Connected</h2>
        <p>Maintaining communication channels is essential for safety:</p>
        <ul>
            <li>Get an international SIM card or eSIM for your phone.</li>
            <li>Share your itinerary with family or friends.</li>
            <li>Schedule regular check-ins with someone back home.</li>
            <li>Know the contact information for your country's embassy or consulate.</li>
            <li>Save emergency contacts in your phone and on paper.</li>
        </ul>

        <h2>10. Trust Your Instincts</h2>
        <p>Finally, your intuition is a powerful tool:</p>
        <ul>
            <li>If a situation feels wrong, remove yourself from it.</li>
            <li>Don't feel obligated to be polite at the expense of your safety.</li>
            <li>Be confident and look like you know where you're going, even if you don't.</li>
            <li>Stay alert to your surroundings, especially in crowded areas.</li>
            <li>Avoid sharing too many details about your travel plans with strangers.</li>
        </ul>

        <div class="quote">
            "The real voyage of discovery consists not in seeking new landscapes, but in having new eyes. Stay safe by seeing the world as it is."
        </div>

        <script>
            // Dark mode toggle
            const darkModeToggle = document.getElementById('dark-mode-toggle');
            darkModeToggle.addEventListener('click', () => {
                document.body.classList.toggle('dark-mode');
            });
            
            // Scroll to top button
            const scrollToTopBtn = document.getElementById('scroll-to-top');
            window.addEventListener('scroll', () => {
                if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
                    scrollToTopBtn.style.display = 'block';
                } else {
                    scrollToTopBtn.style.display = 'none';
                }
            });
            
            scrollToTopBtn.addEventListener('click', () => {
                document.body.scrollTop = 0; // For Safari
                document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
            });
            
            // Remove loading animation after page load
            window.addEventListener('load', () => {
                const loadingElement = document.querySelector('.loading');
                if (loadingElement) {
                    setTimeout(() => {
                        loadingElement.style.display = 'none';
                    }, 2000);
                }
            });
        </script>
    </div>
</body>
</html>

    """

        webView.loadDataWithBaseURL(null, travelGuideContent, "text/html", "UTF-8", null)
    }

}
