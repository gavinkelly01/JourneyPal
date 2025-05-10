package com.example.journeypal.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.journeypal.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GuideFragment : Fragment() {

    private lateinit var countryGuideAdapter: CountryGuideAdapter
    lateinit var guideViewModel: GuideViewModel
    lateinit var webView: WebView
    lateinit var drawerLayout: DrawerLayout
    lateinit var fabToggleView: FloatingActionButton
    lateinit var fabOpenSidebar: FloatingActionButton
    var isDarkMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guide, container, false)

        guideViewModel = ViewModelProvider(this).get(GuideViewModel::class.java)
        webView = view.findViewById(R.id.webView)
        drawerLayout = view.findViewById(R.id.drawer_layout)
        fabToggleView = view.findViewById(R.id.fabToggleView)
        fabOpenSidebar = view.findViewById(R.id.fabOpenSidebar)

        // WebView settings
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (isDarkMode) {
                    injectDarkModeCss()
                }
            }
        }

        // Ensure layout fills parent
        val layoutParams = webView.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        webView.layoutParams = layoutParams

        // Load initial content
        loadTravelGuide()

        // Toggle dark mode
        fabToggleView.setOnClickListener {
            toggleDarkMode()
        }

        // Open sidebar
        fabOpenSidebar.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Sidebar setup
        setupSidebarItems(view)

        countryGuideAdapter = CountryGuideAdapter(requireContext())
        guideViewModel.scamsLiveData.observe(viewLifecycleOwner, Observer { countryGuides ->
            countryGuideAdapter.submitList(countryGuides)
        })

        guideViewModel.fetchScams("Thailand")
        return view
    }

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode

        if (isDarkMode) {
            val darkCss = """
            (function() {
                var css = `
                    html, body {
                        background-color: #121212 !important;
                        color: #e0e0e0 !important;
                    }
                    a { color: #bb86fc !important; }
                    img { filter: brightness(0.8) contrast(1.2); }
                    * {
                        background-color: transparent !important;
                        border-color: #333 !important;
                    }
                `;
                var style = document.createElement('style');
                style.type = 'text/css';
                style.id = 'darkModeStyle';
                style.appendChild(document.createTextNode(css));
                document.head.appendChild(style);
            })();
        """.trimIndent()

            webView.evaluateJavascript(darkCss, null)
        } else {
            val removeDarkCss = """
            (function() {
                var style = document.getElementById('darkModeStyle');
                if (style) {
                    style.remove();
                }
            })();
        """.trimIndent()

            webView.evaluateJavascript(removeDarkCss, null)
        }
    }


    fun setupSidebarItems(view: View) {
        val sidebarContent = view.findViewById<LinearLayout>(R.id.sidebar_content)

        val categories = listOf(
            "Travelers with Disabilities" to disabilityTravelGuide(),
            "LGBTQ+ Travelers" to lgbtTravelGuide(),
            "Women Travelers" to womenTravelGuide(),
            "Transgender Travelers" to transTravelGuide(),
            "People of Color" to pocTravelGuide(),
            "Solo Travelers" to soloTravelGuide()
        )

        sidebarContent.removeAllViews()

        val context = requireContext()

        categories.forEach { (category, content) ->
            val itemView = TextView(context).apply {
                text = category
                textSize = 16f
                setPadding(32, 24, 32, 24)
                setBackgroundResource(android.R.drawable.list_selector_background)
                setTextColor(ContextCompat.getColor(context, R.color.black))
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            sidebarContent.addView(itemView)

            val divider = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1
                ).apply {
                    setMargins(16, 0, 16, 0)
                }
                setBackgroundColor(ContextCompat.getColor(context, R.color.textSecondary))
            }
            sidebarContent.addView(divider)
        }

        val returnButton = TextView(context).apply {
            text = "Return to Main Guide"
            textSize = 16f
            setPadding(32, 24, 32, 24)
            setBackgroundResource(android.R.drawable.list_selector_background)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                loadTravelGuide()
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        sidebarContent.addView(returnButton)
    }

    private fun injectDarkModeCss() {
        val darkCss = """
        (function() {
            var css = `
                html, body {
                    background-color: #121212 !important;
                    color: #e0e0e0 !important;
                }
                a { color: #bb86fc !important; }
                img { filter: brightness(0.8) contrast(1.2); }
                * {
                    background-color: transparent !important;
                    border-color: #333 !important;
                }
            `;
            var style = document.createElement('style');
            style.type = 'text/css';
            style.appendChild(document.createTextNode(css));
            document.head.appendChild(style);
        })();
    """.trimIndent()

        webView.evaluateJavascript(darkCss, null)
    }


    fun loadTravelGuide() {
        val travelGuideContent = """
     <html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
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
        
        html, body {
            margin: 0;
            padding: 0;
            width: 100%;
            height: 100%;
            overflow-x: hidden;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: var(--light-color);
            color: var(--text-color);
            transition: background-color 0.3s ease-in-out;
            line-height: 1.6;
        }
        
        .container {
            width: 100%;
            margin: 0;
            padding: 5px 15px;
            box-sizing: border-box;
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



    private fun disabilityTravelGuide(): String {
        return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <meta name="description" content="Comprehensive guide for travelers with disabilities, including accessibility tips and resources">
        <title>Travel Guide for People with Disabilities</title>
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
                color: var(--text-color);
                line-height: 1.6;
                margin: 0;
                padding: 0;
            }
            
            .container {
                width: 100%;
                max-width: 1200px;
                margin: 0 auto;
                padding: 5px 15px;
                box-sizing: border-box;
            }
            
            header {
                text-align: center;
                padding: 40px 0;
                background: linear-gradient(135deg, var(--primary-color), #2980b9);
                color: white;
                border-radius: 12px;
                margin-bottom: 30px;
                box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
            }
            
            h1 {
                font-size: 2.8em;
                color: white;
                margin-bottom: 20px;
                text-align: center;
                padding-bottom: 15px;
            }
            
            h2 {
                font-size: 1.8em;
                color: var(--dark-color);
                margin-top: 50px;
                border-bottom: 2px solid var(--primary-color);
                padding-bottom: 10px;
            }
            
            h3 {
                font-size: 1.4em;
                color: var(--dark-color);
                margin-top: 25px;
            }
            
            p, ul {
                font-size: 1.1em;
                line-height: 1.8;
                color: var(--light-text);
            }
            
            ul {
                padding-left: 20px;
            }
            
            ul li {
                margin-bottom: 12px;
                position: relative;
                padding-left: 10px;
            }
            
            .highlight {
                background: linear-gradient(135deg, #ecf0f1, #e8f4f8);
                padding: 25px;
                border-radius: 12px;
                margin: 30px 0;
                box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
                border-left: 5px solid var(--primary-color);
            }
            
            .continent-section {
                margin-bottom: 40px;
                padding: 20px;
                background-color: white;
                border-radius: 12px;
                box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
            }
            
            .continent-section h2 {
                margin-top: 0;
                color: var(--primary-color);
            }
            
            .media {
                margin: 20px 0;
                text-align: center;
            }
            
            .media img, .media iframe {
                max-width: 100%;
                border-radius: 8px;
                box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
            }
            
            .media iframe {
                width: 100%;
                height: 315px;
                border: none;
            }
            
            button {
                background-color: var(--primary-color);
                color: white;
                border: none;
                padding: 10px 15px;
                border-radius: 5px;
                cursor: pointer;
                font-size: 1em;
                margin: 10px 0;
                transition: background-color 0.3s;
            }
            
            button:hover {
                background-color: #2980b9;
            }
            
            #dark-mode-toggle {
                position: fixed;
                top: 20px;
                right: 20px;
                z-index: 1000;
                padding: 10px;
                border-radius: 50%;
                width: 50px;
                height: 50px;
                display: flex;
                align-items: center;
                justify-content: center;
            }
            
            .dark-mode {
                background-color: #1a1a2e;
                color: #f0f0f0;
            }
            
            .dark-mode .container {
                background-color: #1a1a2e;
            }
            
            .dark-mode h1 {
                color: white;
            }
            
            .dark-mode h2, .dark-mode h3 {
                color: #e0e0e0;
                border-bottom-color: var(--primary-color);
            }
            
            .dark-mode p, .dark-mode ul li {
                color: #b0b0b0;
            }
            
            .dark-mode .continent-section {
                background-color: #252542;
            }
            
            .dark-mode .highlight {
                background: linear-gradient(135deg, #252542, #1f1f35);
            }
            
            .back-to-top {
                position: fixed;
                bottom: 20px;
                right: 20px;
                background-color: var(--primary-color);
                color: white;
                border-radius: 50%;
                width: 50px;
                height: 50px;
                text-align: center;
                line-height: 50px;
                font-size: 1.5em;
                cursor: pointer;
                display: none;
                box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
                z-index: 1000;
            }
            
            @media (max-width: 768px) {
                h1 {
                    font-size: 2.2em;
                }
                
                h2 {
                    font-size: 1.5em;
                }
                
                p, ul {
                    font-size: 1em;
                }
                
                .media iframe {
                    height: 240px;
                }
            }
        </style>
    </head>
    <body>
        <button id="dark-mode-toggle" aria-label="Toggle dark mode">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
            </svg>
        </button>
        
        <div class="container">
            <header>
                <h1>Travel Guide for People with Disabilities</h1>
                <p>Comprehensive information and resources for accessible travel worldwide</p>
            </header>
            
            <nav aria-label="Table of Contents">
                <div class="highlight">
                    <h3>Quick Navigation</h3>
                    <ul>
                        <li><a href="#planning">Planning Your Trip</a></li>
                        <li><a href="#transportation">Transportation Tips</a></li>
                        <li><a href="#accommodation">Accommodation Considerations</a></li>
                        <li><a href="#insurance">Travel Insurance</a></li>
                        <li><a href="#packing">Packing Essentials</a></li>
                        <li><a href="#destinations">Accessible Destinations</a></li>
                        <li><a href="#resources">Helpful Resources</a></li>
                    </ul>
                </div>
            </nav>
            
            <section id="planning">
                <h2>Planning Your Trip</h2>
                <p>Traveling with a disability requires additional planning, but with proper preparation, you can have a rewarding experience:</p>
                <ul>
                    <li>Research destinations known for accessibility standards (parts of Europe, Japan, Australia, and Canada often have strong accessibility laws)</li>
                    <li>Contact airlines, hotels, and attractions in advance to confirm specific accessibility features</li>
                    <li>Consider working with travel agents specializing in accessible travel</li>
                    <li>Research medical facilities at your destination that can handle your specific needs</li>
                    <li>Check if your destination requires any special documentation for assistive devices</li>
                    <li>Look into travel companion services if you need personal assistance</li>
                </ul>
            </section>
            
            <section id="transportation">
                <h2>Transportation Tips</h2>
                <p>Getting to and around your destination requires careful planning:</p>
                <ul>
                    <li>Book airline assistance at least 48 hours in advance</li>
                    <li>Consider requesting bulkhead or aisle seats for easier access</li>
                    <li>Research accessible public transportation options at your destination</li>
                    <li>Look into wheelchair-accessible taxi services or ride-sharing options</li>
                    <li>Consider renting an adapted vehicle if you plan to drive</li>
                    <li>Check if cruise lines offer accessible cabins and shore excursions</li>
                    <li>Research train accessibility for long-distance travel within continents</li>
                </ul>
            </section>
            
            <section id="accommodation">
                <h2>Accommodation Considerations</h2>
                <p>Finding the right place to stay is crucial for an enjoyable trip:</p>
                <ul>
                    <li>Call hotels directly to ask specific questions about accessibility features</li>
                    <li>Request detailed information about room accessibility, bathroom features, and bed height</li>
                    <li>Ask about elevator access, entrance ramps, and accessibility in common areas</li>
                    <li>Consider booking ground floor rooms when possible</li>
                    <li>Check if the hotel has emergency plans for guests with disabilities</li>
                    <li>Look for properties with roll-in showers and grab bars if needed</li>
                    <li>Consider vacation rentals with verified accessibility features</li>
                </ul>
            </section>
            
            <section id="insurance">
                <h2>Travel Insurance</h2>
                <p>Getting appropriate insurance coverage is particularly important:</p>
                <ul>
                    <li>Ensure your policy covers pre-existing conditions</li>
                    <li>Look for coverage that includes medical equipment damage or loss</li>
                    <li>Consider evacuation insurance for emergency transportation</li>
                    <li>Check if your policy covers accessible accommodation if your original booking fails to meet needs</li>
                    <li>Review policies that offer 24/7 assistance hotlines</li>
                    <li>Keep digital and physical copies of all insurance documents</li>
                </ul>
            </section>
            
            <section id="packing">
                <h2>Packing Essentials</h2>
                <p>Beyond regular travel items, consider these additional necessities:</p>
                <ul>
                    <li>Extra medication and copies of prescriptions</li>
                    <li>Spare parts for mobility equipment</li>
                    <li>International plug adaptors for medical devices</li>
                    <li>Translation cards explaining your condition in the local language</li>
                    <li>Documentation of your accessibility needs for airlines and hotels</li>
                    <li>Medical alert bracelet or card with emergency information</li>
                    <li>Portable chargers for electronic assistive devices</li>
                    <li>Compression garments for long flights if needed</li>
                </ul>
            </section>
            
            <section id="resources" class="highlight">
                <h2>Helpful Resources</h2>
                <p>These websites and apps can help you plan an accessible trip:</p>
                <ul>
                    <li><strong>Accessible Travel Online</strong> - Comprehensive travel planning information</li>
                    <li><strong>Wheelchair Travel</strong> - First-hand accounts and detailed accessibility guides</li>
                    <li><strong>AccessibleGO</strong> - Booking platform with verified accessibility information</li>
                    <li><strong>Accomable</strong> (now part of Airbnb's accessibility filters) - Vacation rentals</li>
                    <li><strong>TripAdvisor's accessibility reviews</strong> - User-generated accessibility feedback</li>
                    <li><strong>Access Now</strong> - Crowdsourced accessibility mapping app</li>
                    <li><strong>Mobility International USA</strong> - Resources for international travelers with disabilities</li>
                </ul>
            </section>
            
            <section id="destinations">
      <h2>Top Accessible Destinations by Continent</h2>
    
    <div class="continent-section">
        <h2>North America</h2>
        <p><strong>Best Countries:</strong> USA, Canada</p>
        <ul>
            <li><strong>USA:</strong> New York City, Washington D.C., San Francisco</li>
            <li><strong>Canada:</strong> Vancouver, Toronto, Montreal</li>
        </ul>
        <div class="media">
            <a href="https://gisgeography.com/wp-content/uploads/2023/12/North-America-Blank-Map-Labels.jpg" target="_blank">
                <img src="https://gisgeography.com/wp-content/uploads/2023/12/North-America-Blank-Map-Labels.jpg" alt="Accessible travel in North America" width="100%">
            </a>
        </div>
    </div>

                
                <div class="continent-section">
    <h2>Europe</h2>
    <p><strong>Best Countries:</strong> UK, Germany, Netherlands, Sweden</p>
    <ul>
        <li><strong>UK:</strong> London, Edinburgh</li>
        <li><strong>Germany:</strong> Berlin, Munich</li>
        <li><strong>Netherlands:</strong> Amsterdam</li>
        <li><strong>Sweden:</strong> Stockholm</li>
    </ul>
    <div class="media">
        <img src="https://gisgeography.com/wp-content/uploads/2023/04/Europe-Country-Map.jpg" alt="Accessible travel in Europe" width="100%">
    </div>
</div>
                
<div class="continent-section">
    <h2>Asia</h2>
    <p><strong>Best Countries:</strong> Japan, Singapore, South Korea</p>
    <ul>
        <li><strong>Japan:</strong> Tokyo, Kyoto</li>
        <li><strong>Singapore:</strong> Entire city is highly accessible</li>
        <li><strong>South Korea:</strong> Seoul</li>
    </ul>
    <div class="media">
        <img src="https://a0.anyrgb.com/pngimg/1436/1762/country-map-asia-east-maps-country-cartoon-map-east-asia-map-capital-city-africa-map.png" alt="Accessible travel in Asia" width="100%">
    </div>
</div>

                
<div class="continent-section">
    <h2>Australia & Oceania</h2>
    <p><strong>Best Countries:</strong> Australia, New Zealand</p>
    <ul>
        <li><strong>Australia:</strong> Sydney, Melbourne</li>
        <li><strong>New Zealand:</strong> Auckland, Wellington</li>
    </ul>
    <div class="media">
        <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQfp383tYff1O75iftBWoQJ7rwkCeSvkQMsqw&s" alt="Accessible travel in Australia and Oceania" width="100%">
    </div>
</div>

                
<div class="continent-section">
    <h2>South America</h2>
    <p><strong>Best Countries:</strong> Brazil, Argentina</p>
    <ul>
        <li><strong>Brazil:</strong> Rio de Janeiro, São Paulo</li>
        <li><strong>Argentina:</strong> Buenos Aires</li>
    </ul>
    <div class="media">
        <img src="https://kidspressmagazine.com/t500/9278/9290/map-continent-south-america-countries-capitals.png" alt="Accessible travel in South America" width="100%">
    </div>
</div>

                
<div class="continent-section">
    <h2>Africa</h2>
    <p><strong>Best Countries:</strong> South Africa</p>
    <ul>
        <li><strong>South Africa:</strong> Cape Town, Johannesburg</li>
    </ul>
    <div class="media">
        <img src="https://tile.loc.gov/image-services/iiif/service:gmd:gmd8:g8200:g8200:ct002539/full/pct:25/0/default.jpg#h=3015&w=2677" alt="Accessible travel in Africa" width="100%">
    </div>
</div>

<section id="disability-guides" class="highlight">
  <h2>Disability-Specific Travel Guides</h2>
  
  <div class="guide-section">
    <h3>For Wheelchair Users</h3>
    <ul>
      <li><strong>Transportation:</strong> Request aisle chairs for boarding aircraft, and confirm measurements of doorways and bathrooms in advance</li>
      <li><strong>Accommodation:</strong> Ask about roll-in showers, bed height, and sufficient turning space in rooms (minimum 5 feet diameter)</li>
      <li><strong>Sightseeing:</strong> Research terrain in advance - cobblestones, steep hills, and sand can be challenging</li>
      <li><strong>Equipment:</strong> Consider portable ramps, wheelchair gloves, and protective covers for wet conditions</li>
      <li><strong>Repairs:</strong> Locate wheelchair repair shops at your destination before traveling</li>
    </ul>
  </div>
  
  <div class="guide-section">
    <h3>For Travelers with Visual Impairments</h3>
    <ul>
      <li><strong>Transportation:</strong> Request meet-and-assist services at airports and train stations</li>
      <li><strong>Accommodation:</strong> Ask hotels about audible elevator announcements and braille signage</li>
      <li><strong>Sightseeing:</strong> Look for museums and attractions with audio descriptions or tactile exhibits</li>
      <li><strong>Technology:</strong> Use apps like Be My Eyes, Seeing AI, or Aira for real-time visual assistance</li>
      <li><strong>Guide Dogs:</strong> Research country-specific requirements for traveling with service animals</li>
    </ul>
  </div>
  
  <div class="guide-section">
    <h3>For Travelers with Hearing Impairments</h3>
    <ul>
      <li><strong>Transportation:</strong> Request visual announcements when booking flights</li>
      <li><strong>Accommodation:</strong> Request rooms with visual fire alarms and doorbell signals</li>
      <li><strong>Communication:</strong> Prepare common phrase cards in local languages or use translation apps</li>
      <li><strong>Technology:</strong> Use captioning apps for real-time transcription of conversations</li>
      <li><strong>Tours:</strong> Look for destinations offering sign language tours or written guides</li>
    </ul>
  </div>
  
  <div class="guide-section">
    <h3>For Travelers with Cognitive Disabilities</h3>
    <ul>
      <li><strong>Planning:</strong> Create detailed visual itineraries with photos of locations</li>
      <li><strong>Transportation:</strong> Request priority boarding to avoid crowds and confusion</li>
      <li><strong>Accommodation:</strong> Choose quieter hotels in less stimulating environments</li>
      <li><strong>Sightseeing:</strong> Plan visits during less crowded times and build in plenty of rest breaks</li>
      <li><strong>Resources:</strong> Carry ID cards explaining your condition to staff if needed</li>
    </ul>
  </div>
  
  <div class="guide-section">
    <h3>For Travelers with Limited Mobility</h3>
    <ul>
      <li><strong>Planning:</strong> Choose destinations with minimal stairs and level terrain</li>
      <li><strong>Transportation:</strong> Request seats with extra legroom and close to bathrooms</li>
      <li><strong>Accommodation:</strong> Ask about elevator access and distances between facilities</li>
      <li><strong>Equipment:</strong> Consider portable grab bars, shower seats, and walking aids</li>
      <li><strong>Timing:</strong> Plan shorter sightseeing days with rest opportunities</li>
    </ul>
  </div>
  
  <div class="guide-section">
    <h3>For Travelers with Chronic Health Conditions</h3>
    <ul>
      <li><strong>Medical:</strong> Research healthcare facilities at your destination that specialize in your condition</li>
      <li><strong>Documentation:</strong> Carry a letter from your doctor explaining your condition and medications</li>
      <li><strong>Medication:</strong> Pack extra medication in both carry-on and checked luggage</li>
      <li><strong>Diet:</strong> Research restaurants that can accommodate specific dietary requirements</li>
      <li><strong>Pacing:</strong> Build rest days into your itinerary and allow for flexible scheduling</li>
    </ul>
  </div>
            </section>
        </div>
        
        <div class="back-to-top" id="back-to-top" aria-label="Back to top">↑</div>
        
        <script>
            // Dark mode toggle
            const darkModeToggle = document.getElementById('dark-mode-toggle');
            const body = document.body;
            
            // Check for saved preference
            if (localStorage.getItem('darkMode') === 'enabled') {
                body.classList.add('dark-mode');
            }
            
            darkModeToggle.addEventListener('click', () => {
                body.classList.toggle('dark-mode');
                
                // Save preference
                if (body.classList.contains('dark-mode')) {
                    localStorage.setItem('darkMode', 'enabled');
                } else {
                    localStorage.setItem('darkMode', null);
                }
            });
            
            // Back to top button
            const backToTopButton = document.getElementById('back-to-top');
            
            window.addEventListener('scroll', () => {
                if (window.pageYOffset > 300) {
                    backToTopButton.style.display = 'block';
                } else {
                    backToTopButton.style.display = 'none';
                }
            });
            
            backToTopButton.addEventListener('click', () => {
                window.scrollTo({
                    top: 0,
                    behavior: 'smooth'
                });
            });
            
   
        </script>
    </body>
    </html>
    """
    }

    private fun lgbtTravelGuide(): String {
        return """
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <title>Travel Guide for LGBTQ+ Travelers</title>
        <style>
            /* Include the same CSS styles as before */
            :root {
                --primary-color: #9b59b6;  /* Changed to purple for LGBTQ+ theme */
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
                color: var(--text-color);
                line-height: 1.6;
                margin: 0;
                padding: 0;
            }
            
            .container {
                width: 100%;
                margin: 0 auto;
                padding: 5px 15px;
                box-sizing: border-box;
            }
            
            header {
                text-align: center;
                padding: 40px 0;
                background: linear-gradient(135deg, #9b59b6, #8e44ad);
                color: white;
                border-radius: 12px;
                margin-bottom: 30px;
                box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
            }
            
            h1 {
                font-size: 2.8em;
                color: var(--dark-color);
                margin-bottom: 20px;
                text-align: center;
                border-bottom: 3px solid var(--primary-color);
                padding-bottom: 15px;
            }
            
            h2 {
                font-size: 1.8em;
                color: var(--dark-color);
                margin-top: 50px;
                border-bottom: 2px solid var(--primary-color);
                padding-bottom: 10px;
            }
            
            p, ul {
                font-size: 1.1em;
                line-height: 1.8;
                color: var(--light-text);
            }
            
            ul {
                padding-left: 20px;
            }
            
            ul li {
                margin-bottom: 12px;
                position: relative;
                padding-left: 10px;
            }
            
            .highlight {
                background: linear-gradient(135deg, #f5f0f8, #e8e8f8);
                padding: 25px;
                border-radius: 12px;
                margin-top: 30px;
                box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
                border-left: 5px solid var(--primary-color);
            }
            
            .alert {
                background-color: rgba(231, 76, 60, 0.1);
                border-left: 5px solid var(--accent-color);
                padding: 15px;
                margin: 20px 0;
                border-radius: 5px;
            }
            
            .alert-title {
                color: var(--accent-color);
                font-weight: bold;
                margin-bottom: 5px;
                display: flex;
                align-items: center;
            }
            
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
            
            .destination-image {
                width: 100%;
                height: auto;
                margin-top: 15px;
                border-radius: 10px;
            }

            iframe {
                width: 100%;
                max-width: 1000px;
                height: 600px;
                border: none;
                border-radius: 8px;
                margin-top: 30px;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <header>
                <h1>Travel Guide for LGBTQ+ Travelers 🏳️‍🌈 </h1>
                <p>Safety tips and resources for LGBTQ+ travelers around the world</p>
            </header>
            
            <h2>Research Before You Go</h2>
            <p>Understanding the legal and social climate for LGBTQ+ people at your destination is essential:</p>
            <ul>
                <li>Research local laws regarding same-sex relationships and gender expression</li>
                <li>Understand cultural attitudes that might not be reflected in laws</li>
                <li>Identify LGBTQ+-friendly neighborhoods and establishments</li>
                <li>Connect with local LGBTQ+ organizations for up-to-date information</li>
            </ul>
            
            <div class="alert">
                <div class="alert-title">Important Safety Information</div>
                <p>In some countries, same-sex relationships or gender non-conformity may be criminalized. Always prioritize your safety and be aware of local laws and customs.</p>
            </div>
            
            <h2>LGBTQ+-Friendly Destinations</h2>
            <p>While you can travel almost anywhere with proper preparation, these destinations are known for being particularly welcoming:</p>
            <h3>North America</h3>
            <ul>
                <li>Canada (Toronto, Vancouver, Montreal) </li>
            </ul>
            <h3>Europe</h3>
            <ul>
                <li>Spain (Madrid, Barcelona, Sitges) </li>
                <li>Iceland (Reykjavik) </li>
            </ul>
            <h3>Asia</h3>
            <ul>
                <li>Thailand (Bangkok, Chiang Mai) </li>
                <li>Taiwan (Taipei) </li>
            </ul>
            <h3>Oceania</h3>
            <ul>
                <li>Australia (Sydney, Melbourne) </li>
                <li>New Zealand </li>
            </ul>
            <h3>South America</h3>
            <ul>
                <li>Brazil (Rio de Janeiro, São Paulo) </li>
                <li>Uruguay (Montevideo) </li>
            </ul>

            <h2>Accommodation Tips</h2>
            <p>Finding welcoming places to stay can enhance your travel experience:</p>
            <ul>
                <li>Look for hotels with explicit LGBTQ+-friendly policies</li>
                <li>Consider LGBTQ+-owned or -certified accommodations</li>
                <li>Read reviews from other LGBTQ+ travelers</li>
                <li>In less accepting regions, consider booking accommodations with a single bed if traveling with a partner</li>
            </ul>

            <h2>Travel Documentation</h2>
            <p>For transgender and non-binary travelers, documentation requires special consideration:</p>
            <ul>
                <li>Ensure your ID documents match your gender presentation as closely as possible</li>
                <li>Carry copies of medical documents related to transition if needed</li>
                <li>Research countries that recognize non-binary gender markers if applicable</li>
                <li>Consider bringing a letter from your healthcare provider explaining medical equipment or medications</li>
            </ul>

            <div class="highlight">
                <h3>Helpful Resources</h3>
                <p>These organizations provide up-to-date information for LGBTQ+ travelers:</p>
                <ul>
                    <li>International LGBTQ+ Travel Association (ILGTA)</li>
                    <li>National Center for Transgender Equality's travel information</li>
                    <li>Equaldex (global LGBTQ+ rights database)</li>
                    <li>GayTravel.com</li>
                    <li>Spartacus Gay Travel Index (annual ranking of LGBTQ+-friendly countries)</li>
                </ul>
            </div>

            <h2>Safe Social Connections</h2>
            <p>Meeting people while traveling as an LGBTQ+ person:</p>
            <ul>
                <li>Use LGBTQ+-specific apps or websites to find local events and venues</li>
                <li>Connect with LGBTQ+ travel groups or forums before your trip</li>
                <li>Exercise caution when using dating apps in less tolerant regions</li>
                <li>Consider joining LGBTQ+ tours for built-in community</li>
            </ul>

            <h2> LGBTQ+ Travel Map </h2>
            <iframe src="https://www.allgaylong.com/lgbtq-worldwide-travel-map/" title="LGBTQ+ Worldwide Travel Map"></iframe>
             
            

        
        </div>
    </body>
    </html>
    """
    }



    private fun womenTravelGuide(): String {
        return """
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <title>Travel Guide for Women Travelers</title>
            <style>
                /* Include the same CSS styles as before with a feminine color theme */
                :root {
                    --primary-color: #e84393;
                    --secondary-color: #fd79a8;
                    --dark-color: #2c3e50;
                    --light-color: #f4f4f9;
                    --accent-color: #e74c3c;
                    --text-color: #333;
                    --light-text: #555;
                }
                
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: var(--light-color);
                    color: var(--text-color);
                    line-height: 1.6;
                    margin: 0;
                    padding: 0;
                }
                
                .container {
                    width: 100%;
                    margin: 0 auto;
                    padding: 5px 15px;
                    box-sizing: border-box;
                }
                
                header {
                    text-align: center;
                    padding: 40px 0;
                    background: linear-gradient(135deg, #e84393, #fd79a8);
                    color: white;
                    border-radius: 12px;
                    margin-bottom: 30px;
                    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                }
                
                h1 {
                    font-size: 2.8em;
                    color: var(--dark-color);
                    margin-bottom: 20px;
                    text-align: center;
                    border-bottom: 3px solid var(--primary-color);
                    padding-bottom: 15px;
                }
                
                h2 {
                    font-size: 1.8em;
                    color: var(--dark-color);
                    margin-top: 50px;
                    border-bottom: 2px solid var(--primary-color);
                    padding-bottom: 10px;
                }
                
                p, ul {
                    font-size: 1.1em;
                    line-height: 1.8;
                    color: var(--light-text);
                }
                
                ul {
                    padding-left: 20px;
                }
                
                ul li {
                    margin-bottom: 12px;
                    position: relative;
                    padding-left: 10px;
                }
                
                .highlight {
                    background: linear-gradient(135deg, #ffeef8, #fff0f7);
                    padding: 25px;
                    border-radius: 12px;
                    margin-top: 30px;
                    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
                    border-left: 5px solid var(--primary-color);
                }
                
                .alert {
                    background-color: rgba(231, 76, 60, 0.1);
                    border-left: 5px solid var(--accent-color);
                    padding: 15px;
                    margin: 20px 0;
                    border-radius: 5px;
                }
                
                .alert-title {
                    color: var(--accent-color);
                    font-weight: bold;
                    margin-bottom: 5px;
                    display: flex;
                    align-items: center;
                }
                
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
            </style>
        </head>
        <body>
            <div class="container">
                <header>
                    <h1>Travel Guide for Women Travelers</h1>
                    <p>Safety tips and empowering advice for women exploring the world</p>
                </header>
                
                <h2>Research and Preparation</h2>
                <p>Knowledge is power when it comes to traveling safely as a woman:</p>
                <ul>
                    <li>Research cultural norms and expectations regarding women at your destination</li>
                    <li>Understand appropriate dress codes and adapt accordingly</li>
                    <li>Learn about areas to avoid, particularly at night</li>
                    <li>Join women's travel groups online to get first-hand advice from other travelers</li>
                </ul>
                
                <h2>Safety Strategies</h2>
                <p>While these tips apply to all travelers, they're particularly important for women:</p>
                <ul>
                    <li>Share your itinerary with trusted friends or family</li>
                    <li>Use location-sharing apps with trusted contacts</li>
                    <li>Consider wearing a wedding ring in some destinations to reduce unwanted attention</li>
                    <li>Trust your intuition - if a situation feels unsafe, leave immediately</li>
                    <li>Have emergency numbers programmed in your phone</li>
                    <li>Consider carrying a doorstop for additional hotel room security</li>
                </ul>
                
                <div class="alert">
                    <div class="alert-title">Transportation Safety</div>
                    <p>When using taxis or rideshares, take a photo of the license plate and share it with someone. Verify the driver's identity before entering the vehicle, and track your route on your own map app.</p>
                </div>
                
                <h2>Accommodation Tips</h2>
                <p>Choosing the right place to stay is crucial for peace of mind:</p>
                <ul>
                    <li>Look for accommodations with 24-hour front desks and good security</li>
                    <li>Request rooms that aren't on the ground floor</li>
                    <li>Consider women-only floors in hotels that offer them</li>
                    <li>Check reviews from other women travelers</li>
                    <li>Always use all locks and security features in your room</li>
                </ul>
                
                <h2>Packing Essentials</h2>
                <p>Some items are particularly useful for women travelers:</p>
                <ul>
                    <li>A cross-body bag that can be worn in front for crowded areas</li>
                    <li>A scarf that can double as a head covering in conservative regions</li>
                    <li>Menstrual products, which may be difficult to find in some destinations</li>
                    <li>Medication for UTIs or yeast infections if you're prone to them</li>
                    <li>Comfortable walking shoes that still look nice for various settings</li>
                </ul>
                
                <div class="highlight">
                    <h3>Women-Friendly Destinations</h3>
                    <p>While women can travel almost anywhere with proper preparation, these destinations are known for being particularly safe and comfortable for women travelers:</p>
                    <ul>
                        <li>Iceland</li>
                        <li>New Zealand</li>
                        <li>Canada</li>
                        <li>Japan</li>
                        <li>Portugal</li>
                        <li>Slovenia</li>
                        <li>Rwanda</li>
                        <li>Singapore</li>
                    </ul>
                </div>
                
                <h2>Solo Travel Empowerment</h2>
                <p>Traveling solo as a woman can be incredibly rewarding:</p>
                <ul>
                    <li>Start with destinations known to be safer for women travelers</li>
                    <li>Consider joining day tours to meet other travelers</li>
                    <li>Be confident and purposeful in your movements</li>
                    <li>Have a safety phrase or code word to use with family/friends if you need help</li>
                    <li>Take a self-defense class before your trip for confidence</li>
                </ul>
                
                <h2>Handling Harassment</h2>
                <p>Unfortunately, women travelers sometimes face harassment. Here's how to handle it:</p>
                <ul>
                    <li>Be firm and loud when saying "no" to unwanted attention</li>
                    <li>Move to a public area with other people around</li>
                    <li>Seek help from other women or families when possible</li>
                    <li>Know how to contact local authorities</li>
                    <li>Consider learning a few key phrases in the local language such as "leave me alone"</li>
                </ul>
            </div>
        </body>
        </html>
        """
    }
    private fun transTravelGuide(): String {
        return """
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <title>Travel Guide for Transgender Travelers</title>
            <style>
                /* Include the same CSS styles with trans flag colors */
                :root {
                    --primary-color: #5bcefa;  /* Trans flag blue */
                    --secondary-color: #f5a9b8; /* Trans flag pink */
                    --dark-color: #2c3e50;
                    --light-color: #f4f4f9;
                    --accent-color: #e74c3c;
                    --text-color: #333;
                    --light-text: #555;
                }
                
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: var(--light-color);
                    color: var(--text-color);
                    line-height: 1.6;
                    margin: 0;
                    padding: 0;
                }
                
                .container {
                    width: 100%;
                    margin: 0 auto;
                    padding: 5px 15px;
                    box-sizing: border-box;
                }
                
                header {
                    text-align: center;
                    padding: 40px 0;
                    background: linear-gradient(135deg, #5bcefa, #f5a9b8);
                    color: white;
                    border-radius: 12px;
                    margin-bottom: 30px;
                    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                }
                
                h1 {
                    font-size: 2.8em;
                    color: var(--dark-color);
                    margin-bottom: 20px;
                    text-align: center;
                    border-bottom: 3px solid var(--primary-color);
                    padding-bottom: 15px;
                }
                
                h2 {
                    font-size: 1.8em;
                    color: var(--dark-color);
                    margin-top: 50px;
                    border-bottom: 2px solid var(--primary-color);
                    padding-bottom: 10px;
                }
                
                p, ul {
                    font-size: 1.1em;
                    line-height: 1.8;
                    color: var(--light-text);
                }
                
                ul {
                    padding-left: 20px;
                }
                
                ul li {
                    margin-bottom: 12px;
                    position: relative;
                    padding-left: 10px;
                }
                
                .highlight {
                    background: linear-gradient(135deg, #e6f7ff, #fff0f7);
                    padding: 25px;
                    border-radius: 12px;
                    margin-top: 30px;
                    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
                    border-left: 5px solid var(--primary-color);
                }
                
                .alert {
                    background-color: rgba(231, 76, 60, 0.1);
                    border-left: 5px solid var(--accent-color);
                    padding: 15px;
                    margin: 20px 0;
                    border-radius: 5px;
                }
                
                .alert-title {
                    color: var(--accent-color);
                    font-weight: bold;
                    margin-bottom: 5px;
                    display: flex;
                    align-items: center;
                }
                
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
            </style>
        </head>
        <body>
            <div class="container">
                <header>
                    <h1>Travel Guide for Transgender Travelers</h1>
                    <p>Essential information and resources for transgender and non-binary travelers</p>
                </header>
                
                <h2>Pre-Trip Planning</h2>
                <p>Before traveling as a transgender or non-binary person, consider these important steps:</p>
                <ul>
                    <li>Research local laws and social attitudes toward transgender people at your destination</li>
                    <li>Ensure your identification documents match your gender presentation as closely as possible</li>
                    <li>Research transgender-friendly healthcare providers at your destination</li>
                    <li>Connect with local transgender organizations for up-to-date information</li>
                </ul>
                
                <div class="alert">
                    <div class="alert-title">Travel Documentation</div>
                    <p>Having identification documents that match your gender identity and presentation can significantly reduce complications while traveling. Consider updating your passport, driver's license, and other IDs before travel if possible.</p>
                </div>
                
                <h2>Security Screening</h2>
                <p>Airport security can be stressful for transgender travelers. These tips may help:</p>
                <ul>
                    <li>Arrive early to allow time for additional screening if needed</li>
                    <li>Consider carrying a letter from your healthcare provider explaining medical equipment or prosthetics</li>
                    <li>You can request a private screening if you feel uncomfortable</li>
                    <li>Know your rights: in many countries, you can request an officer of your preferred gender to conduct any pat-down</li>
                    <li>Pack prosthetics, binders, packers, or other gender-affirming items in your carry-on luggage</li>
                </ul>
                
                <h2>Medication Considerations</h2>
                <p>If you're taking hormone therapy or other medications:</p>
                <ul>
                    <li>Pack extra medication in case of delays</li>
                    <li>Keep medications in original, labeled containers</li>
                    <li>Carry a letter from your doctor explaining your need for these medications</li>
                    <li>Research whether your medications are legal at your destination</li>
                    <li>Consider time zone changes that might affect medication schedules</li>
                </ul>
                
                <div class="highlight">
                    <h3>Trans-Friendly Destinations</h3>
                    <p>While safety concerns vary, these destinations have generally progressive laws and social attitudes regarding transgender rights:</p>
                    <ul>
                        <li>Canada (especially Toronto, Vancouver, and Montreal)</li>
                        <li>Uruguay</li>
                        <li>New Zealand</li>
                        <li>Malta</li>
                        <li>The Netherlands</li>
                        <li>Iceland</li>
                        <li>Portugal</li>
                        <li>Spain</li>
                    </ul>
                </div>
                
                <h2>Accommodation Tips</h2>
                <p>Finding comfortable and respectful lodging:</p>
                <ul>
                    <li>Research LGBTQ+-friendly accommodations</li>
                    <li>Look for hotels with gender-neutral bathroom options when possible</li>
                    <li>Consider booking accommodations with private bathrooms</li>
                    <li>Read reviews from other transgender travelers</li>
                    <li>For hostels, look for those offering private rooms or gender-neutral dormitories</li>
                </ul>
                
                <h2>Bathroom Access</h2>
                <p>Bathroom access can be challenging while traveling:</p>
                <ul>
                    <li>Research local customs and laws regarding bathroom use</li>
                    <li>Consider using family or accessible bathrooms when available</li>
                    <li>Apps like Refuge Restrooms can help locate gender-neutral bathrooms</li>
                    <li>Plan bathroom breaks around safe locations like LGBTQ+-friendly establishments</li>
                </ul>
                
                <h2>Emergency Resources</h2>
                <p>Know where to turn if you encounter difficulties:</p>
                <ul>
                    <li>Save contact information for your country's embassy or consulate</li>
                    <li>Research local LGBTQ+ organizations that might provide assistance</li>
                    <li>Consider travel insurance that covers emergency evacuation</li>
                    <li>Have a contingency plan and emergency funds if you need to leave a location quickly</li>
                </ul>
            </div>
        </body>
        </html>
        """
    }
    private fun pocTravelGuide(): String {
        return """
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <title>Travel Guide for People of Color</title>
            <style>
                :root {
                    --primary-color: #ff9800;  /* Warm orange */
                    --secondary-color: #8d6e63; /* Warm brown */
                    --dark-color: #2c3e50;
                    --light-color: #f4f4f9;
                    --accent-color: #e74c3c;
                    --text-color: #333;
                    --light-text: #555;
                }
                
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: var(--light-color);
                    color: var(--text-color);
                    line-height: 1.6;
                    margin: 0;
                    padding: 0;
                }
                
                .container {
                    width: 100%;
                    margin: 0 auto;
                    padding: 5px 15px;
                    box-sizing: border-box;
                }
                
                header {
                    text-align: center;
                    padding: 40px 0;
                    background: linear-gradient(135deg, #ff9800, #8d6e63);
                    color: white;
                    border-radius: 12px;
                    margin-bottom: 30px;
                    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                }
                
                h1 {
                    font-size: 2.8em;
                    color: var(--dark-color);
                    margin-bottom: 20px;
                    text-align: center;
                    border-bottom: 3px solid var(--primary-color);
                    padding-bottom: 15px;
                }
                
                h2 {
                    font-size: 1.8em;
                    color: var(--dark-color);
                    margin-top: 50px;
                    border-bottom: 2px solid var(--primary-color);
                    padding-bottom: 10px;
                }
                
                p, ul {
                    font-size: 1.1em;
                    line-height: 1.8;
                    color: var(--light-text);
                }
                
                ul {
                    padding-left: 20px;
                }
                
                ul li {
                    margin-bottom: 12px;
                    position: relative;
                    padding-left: 10px;
                }
                
                .highlight {
                    background: linear-gradient(135deg, #fff8e1, #f5f5f5);
                    padding: 25px;
                    border-radius: 12px;
                    margin-top: 30px;
                    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
                    border-left: 5px solid var(--primary-color);
                }
                
                .alert {
                    background-color: rgba(231, 76, 60, 0.1);
                    border-left: 5px solid var(--accent-color);
                    padding: 15px;
                    margin: 20px 0;
                    border-radius: 5px;
                }
                
                .alert-title {
                    color: var(--accent-color);
                    font-weight: bold;
                    margin-bottom: 5px;
                    display: flex;
                    align-items: center;
                }
                
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
            </style>
        </head>
        <body>
            <div class="container">
                <header>
                    <h1>Travel Guide for People of Color</h1>
                    <p>Insights, tips, and resources for navigating travel as a person of color</p>
                </header>
                
                <h2>Research Before You Go</h2>
                <p>Understanding the social climate for people of color at your destination is essential:</p>
                <ul>
                    <li>Research historical and current race relations at your destination</li>
                    <li>Look for travel blogs and forums by travelers of color who have visited your destination</li>
                    <li>Connect with local groups or organizations that support people of your background</li>
                    <li>Learn about any areas where racial tensions may be higher</li>
                </ul>
                
                <div class="alert">
                    <div class="alert-title">Important Safety Note</div>
                    <p>In some regions, people of color may experience different treatment from locals or authorities. Research local emergency numbers and know where your country's embassy or consulate is located.</p>
                </div>
                
                <h2>Destination Insights</h2>
                <p>While experiences vary greatly by individual, these regional insights may be helpful:</p>
                <ul>
                    <li>Some parts of Europe may have less diversity and possibly more staring or curiosity</li>
                    <li>Parts of Asia might associate darker skin with lower social status due to historical classism</li>
                    <li>In many tourist areas, you might experience "foreigner treatment" rather than race-specific treatment</li>
                    <li>Some remote areas with little tourism may result in more curiosity or attention</li>
                </ul>
                
                <h2>Handling Uncomfortable Situations</h2>
                <p>Strategies for managing difficult interactions while traveling:</p>
                <ul>
                    <li>Have phrases ready in the local language to politely address inappropriate comments or questions</li>
                    <li>Identify potential allies and safe spaces in your destination</li>
                    <li>Consider traveling with companions when visiting areas known for racial tension</li>
                    <li>Document incidents of discrimination at hotels, restaurants, or with transportation for later reporting</li>
                    <li>Know your rights as a traveler and tourist in each country</li>
                </ul>
                
                <div class="highlight">
                    <h3>Welcoming Destinations</h3>
                    <p>These destinations are often noted for their diversity and generally positive experiences reported by travelers of color:</p>
                    <ul>
                        <li>Brazil (particularly Salvador and Rio de Janeiro)</li>
                        <li>South Africa (Cape Town, Johannesburg)</li>
                        <li>Canada (Toronto, Vancouver)</li>
                        <li>Singapore</li>
                        <li>Malaysia</li>
                        <li>Mexico (Mexico City)</li>
                        <li>Portugal</li>
                        <li>Colombia (Cartagena, Medellín)</li>
                    </ul>
                </div>
                
                <h2>Community Connections</h2>
                <p>Finding community while traveling can enhance your experience:</p>
                <ul>
                    <li>Use social media groups specific to travelers of color to connect with others</li>
                    <li>Seek out cultural centers, festivals, or events related to your heritage</li>
                    <li>Consider joining tours led by people of color or that focus on diverse perspectives</li>
                    <li>Look for accommodations owned by people of color or that actively promote diversity</li>
                </ul>
                
                <h2>Border Crossing and Security</h2>
                <p>Navigating immigration and security as a person of color:</p>
                <ul>
                    <li>Carry all necessary documentation, including return tickets and accommodation details</li>
                    <li>Be prepared to answer questions about the purpose of your visit</li>
                    <li>Know your rights, but understand that these vary significantly by country</li>
                    <li>Allow extra time for security and immigration processes</li>
                    <li>Consider registering with your country's embassy when visiting regions with known issues</li>
                </ul>
                
                <h2>Cultural Heritage Travel</h2>
                <p>For those interested in exploring their cultural roots:</p>
                <ul>
                    <li>Research tour companies specializing in heritage travel for your specific background</li>
                    <li>Connect with local historical societies or cultural organizations</li>
                    <li>Consider DNA testing before travel to pinpoint regions of ancestry</li>
                    <li>Look for community events or festivals celebrating your heritage</li>
                    <li>Document your journey through journals, photos, or videos to share with family</li>
                </ul>
            </div>
        </body>
        </html>
        """
    }
    private fun soloTravelGuide(): String {
        return """
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <title>Travel Guide for Solo Travelers</title>
            <style>
                :root {
                    --primary-color: #27ae60;  /* Green for independence */
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
                    color: var(--text-color);
                    line-height: 1.6;
                    margin: 0;
                    padding: 0;
                }
                
                .container {
                    width: 100%;
                    margin: 0 auto;
                    padding: 5px 15px;
                    box-sizing: border-box;
                }
                
                header {
                    text-align: center;
                    padding: 40px 0;
                    background: linear-gradient(135deg, #27ae60, #2ecc71);
                    color: white;
                    border-radius: 12px;
                    margin-bottom: 30px;
                    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
                }
                
                h1 {
                    font-size: 2.8em;
                    color: var(--dark-color);
                    margin-bottom: 20px;
                    text-align: center;
                    border-bottom: 3px solid var(--primary-color);
                    padding-bottom: 15px;
                }
                
                h2 {
                    font-size: 1.8em;
                    color: var(--dark-color);
                    margin-top: 50px;
                    border-bottom: 2px solid var(--primary-color);
                    padding-bottom: 10px;
                }
                
                p, ul {
                    font-size: 1.1em;
                    line-height: 1.8;
                    color: var(--light-text);
                }
                
                ul {
                    padding-left: 20px;
                }
                
                ul li {
                    margin-bottom: 12px;
                    position: relative;
                    padding-left: 10px;
                }
                
                .highlight {
                    background: linear-gradient(135deg, #e8f5e9, #f1f8e9);
                    padding: 25px;
                    border-radius: 12px;
                    margin-top: 30px;
                    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
                    border-left: 5px solid var(--primary-color);
                }
                
                .alert {
                    background-color: rgba(231, 76, 60, 0.1);
                    border-left: 5px solid var(--accent-color);
                    padding: 15px;
                    margin: 20px 0;
                    border-radius: 5px;
                }
                
                .alert-title {
                    color: var(--accent-color);
                    font-weight: bold;
                    margin-bottom: 5px;
                    display: flex;
                    align-items: center;
                }
                
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
            </style>
        </head>
        <body>
            <div class="container">
                <header>
                    <h1>Travel Guide for Solo Travelers</h1>
                    <p>Tips, strategies, and inspiration for exploring the world on your own</p>
                </header>
                
                <h2>Benefits of Solo Travel</h2>
                <p>Traveling alone offers unique advantages that can lead to profound personal growth:</p>
                <ul>
                    <li>Complete freedom to create your own itinerary and change plans spontaneously</li>
                    <li>Enhanced self-confidence and problem-solving skills</li>
                    <li>More opportunities to connect with locals and other travelers</li>
                    <li>Space for self-reflection and personal discovery</li>
                    <li>The ability to fully immerse yourself in new cultures at your own pace</li>
                </ul>
                
                <h2>Safety Strategies</h2>
                <p>While solo travel is generally safe, these practices can help minimize risks:</p>
                <ul>
                    <li>Research your destination thoroughly before arriving</li>
                    <li>Share your itinerary with trusted friends or family</li>
                    <li>Use location-sharing apps with trusted contacts</li>
                    <li>Trust your intuition about people and situations</li>
                    <li>Keep digital and physical copies of important documents</li>
                    <li>Arrive at new destinations during daylight hours when possible</li>
                </ul>
                
                <div class="alert">
                    <div class="alert-title">Solo Travel Safety Tip</div>
                    <p>When checking into accommodation, don't announce that you're traveling alone. If someone asks, you can mention that you're meeting friends later or that your travel companion is arriving soon.</p>
                </div>
                
                <h2>Meeting Other Travelers</h2>
                <p>Solo doesn't have to mean lonely - here's how to connect with others:</p>
                <ul>
                    <li>Stay in social accommodations like hostels with communal spaces</li>
                    <li>Join free walking tours offered in many cities</li>
                    <li>Use travel apps like Meetup, Couchsurfing events, or Backpackr</li>
                    <li>Take classes (cooking, language, dance) where you can meet locals and travelers</li>
                    <li>Eat at communal tables in restaurants or food halls</li>
                </ul>
                
                <h2>Accommodation Tips</h2>
                <p>Choosing the right place to stay can enhance your solo travel experience:</p>
                <ul>
                    <li>Read recent reviews with a focus on safety and location</li>
                    <li>Look for accommodations with 24-hour reception</li>
                    <li>Consider the proximity to public transportation and well-lit areas</li>
                    <li>Mix private rooms with social hostels to balance solitude and socializing</li>
                    <li>Try homestays for authentic local experiences and built-in connections</li>
                </ul>
                
                <div class="highlight">
                    <h3>Great Destinations for Solo Travelers</h3>
                    <p>These destinations are particularly well-suited for those traveling alone:</p>
                    <ul>
                        <li>Japan (excellent public transportation and low crime rate)</li>
                        <li>New Zealand (friendly locals and many solo travelers)</li>
                        <li>Portugal (affordable, safe, and welcoming)</li>
                        <li>Taiwan (safe, easy to navigate, and friendly to foreigners)</li>
                        <li>Ireland (English-speaking with a culture of hospitality)</li>
                        <li>Costa Rica (established tourist infrastructure and eco-adventures)</li>
                        <li>Vietnam (budget-friendly with established backpacker routes)</li>
                        <li>Canada (safe cities with diverse cultures and natural beauty)</li>
                    </ul>
                </div>
                
                <h2>Handling Alone Time</h2>
                <p>Embracing solitude is part of the solo travel experience:</p>
                <ul>
                    <li>Bring entertainment for downtime (books, podcasts, journals)</li>
                    <li>Use meals alone as opportunities to observe local culture</li>
                    <li>Try meditation or mindfulness to appreciate the present moment</li>
                    <li>Document your journey through photography, writing, or sketching</li>
                    <li>Schedule video calls with friends and family when you need familiar connection</li>
                </ul>
                
                <h2>Practical Solo Travel Tips</h2>
                <p>These practical strategies can make solo travel smoother:</p>
                <ul>
                    <li>Pack light - you'll be handling all your luggage yourself</li>
                    <li>Learn basic phrases in the local language</li>
                    <li>Carry a portable power bank for your devices</li>
                    <li>Use a money belt or anti-theft bag for important items</li>
                    <li>Take photos of directions or save offline maps before heading out</li>
                    <li>Budget for occasional splurges like private transportation when tired or arriving late</li>
                </ul>
                
                <h2>Solo Travel Challenges</h2>
                <p>Acknowledging common challenges can help you prepare:</p>
                <ul>
                    <li>Occasional loneliness - have strategies ready to connect or enjoy your own company</li>
                    <li>Decision fatigue - sometimes making every choice can be tiring</li>
                    <li>Higher costs without someone to share expenses - look for single supplements or room sharing</li>
                    <li>Limited photo opportunities of yourself - use tripods, ask others, or embrace the selfie</li>
                    <li>Safety concerns - stay alert without letting fear limit your experience</li>
                </ul>
            </div>
        </body>
        </html>
        """
    }

}
