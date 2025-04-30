package com.example.journeypal

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.test.espresso.IdlingResource
import com.example.journeypal.ui.dashboard.GuideFragment

class WebViewIdlingResource(private val fragment: GuideFragment) : IdlingResource {

    private var resourceCallback: IdlingResource.ResourceCallback? = null
    private var isIdle = true

    init {
        // Ensure the WebView has been initialized and set its WebViewClient
        fragment.view?.findViewById<WebView>(R.id.webView)?.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    isIdle = false // Page is starting to load
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isIdle = true // Page has finished loading
                    resourceCallback?.onTransitionToIdle() // Notify that the WebView is idle
                }
            }
        }
    }

    override fun getName(): String {
        return WebViewIdlingResource::class.java.name
    }

    override fun isIdleNow(): Boolean {
        return isIdle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }
}
