package com.example.kodaapplication

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.example.kodaapplication.R.id.theWebView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

    class child_homescreen : AppCompatActivity() {
        private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.light(
//                Color.TRANSPARENT, Color.TRANSPARENT
//            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_homescreen)

        webView = findViewById(theWebView)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                // Check if the URL contains the filtered word
                if (url.contains("Filtered")) {
                    // Block the request or handle it as needed
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        // Load your URL here
        webView.loadUrl("https://www.google.com")

        val toChild = findViewById<ExtendedFloatingActionButton>(R.id.fab)
        toChild.setOnClickListener {
            startActivity(Intent(this@child_homescreen, addChildInfo::class.java))
        }
    }

}