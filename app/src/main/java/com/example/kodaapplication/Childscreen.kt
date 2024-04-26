package com.example.kodaapplication

import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class Childscreen : AppCompatActivity() {
    // ...
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var webView: WebView
    private var urlLoadingCallback: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_homescreen)

        // ...
        firebaseFirestore = FirebaseFirestore.getInstance()

        webView = findViewById(R.id.theWebView)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.startsWith("https://www.google.com/")) {
                    // Allow Google search page to load
                    return false
                }
                isSiteBlocked(url) { isBlocked ->
                    if (!isBlocked) {
                        webView.clearCache(true) // Clear the WebView cache
                        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
                        webView.settings.domStorageEnabled = false
                        webView.clearHistory()
                        webView.loadUrl(url)
                    } else {
                        Toast.makeText(this@Childscreen, "This site is blocked by KODA App", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }
        }

        webView.loadUrl("https://www.google.com")


    }
    fun isSiteBlocked(url: String, callback: (Boolean) -> Unit) {
        val site = Uri.parse(url).host
        if (site != null) {
            firebaseFirestore.collection("blocked_Sites").document(site).get().addOnSuccessListener { document ->
                callback(document.getBoolean("blocked") ?: false)
            }
        } else {
            callback(false)
        }
    }
    fun updateBlockedStatus(site: String, isBlocked: Boolean) {
        val url = Uri.parse(site)
        val domain = url.host?: return
        val documentReference = firebaseFirestore.collection("blocked_Sites").document(domain)
        documentReference.set(mapOf("blocked" to isBlocked))
    }
}
