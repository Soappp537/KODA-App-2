package com.example.kodaapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
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
                webView.clearCache(true) // Clear the WebView cache
                urlLoadingCallback = { isAllowed ->
                    if (isAllowed) {
                        Toast.makeText(this@Childscreen, "This site is blocked by KODA App", Toast.LENGTH_SHORT).show()
                    } else {

                        webView.settings.setCacheMode(WebSettings.LOAD_NO_CACHE)
                        webView.settings.setDomStorageEnabled(false)
                        webView.clearHistory()
                        webView.loadUrl(url)
                    }
                }
                isSiteBlocked(url)
                return true
            }
            override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
                if (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    // Handle search button press
                    val searchQuery = webView.url?.substringAfterLast("q=")
                    if (searchQuery!!.isNotEmpty()) {
                        webView.loadUrl("https://www.google.com/search?q=$searchQuery")
                        return true
                    }
                }
                // Return false to let the WebView handle the key event
                return false
            }
        }

        webView.loadUrl("https://www.google.com")


        val fab = findViewById<ExtendedFloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->
            // Perform your desired action here, e.g. opening a new activity
            startActivity(Intent(this, addChildInfo::class.java))
        }
    }

    private fun isSiteBlocked(siteToCheck: String) {
        val url = Uri.parse(siteToCheck)
        val domain = url.host ?: return
        val pathSegments = url.pathSegments
        val documentId = if (pathSegments.isNotEmpty()) {
            pathSegments[0]
        } else {
            domain
        }
        val documentReference = firebaseFirestore.collection("blocked_Sites").document(documentId)
        documentReference.get().addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                val isBlocked = document != null && document.exists() && document.getBoolean("blocked") == true
                urlLoadingCallback?.invoke(isBlocked)
            } else {
                Log.e("Firebase", "Error checking if site is blocked", task.exception)
                urlLoadingCallback?.invoke(false)
            }
            urlLoadingCallback = null
        })
    }
}
