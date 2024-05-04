@file:Suppress("UNREACHABLE_CODE")

package com.example.kodaapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kodaapplication.TfLiteModel.Companion.containsBlockedKeywords
import com.example.kodaapplication.TfLiteModel.Companion.loadModel
import com.example.kodaapplication.TfLiteModel.Companion.loadWordIndexMap
import com.example.kodaapplication.TfLiteModel.Companion.preprocessText
import com.google.firebase.firestore.FirebaseFirestore
import org.tensorflow.lite.Interpreter

// naka global declaration nung ibang variable para gumana ung functions sa kabila file TFLiteModel.kt
lateinit var wordIndexMap: Map<String, Int> // walang built in tokenizer android studio kaya ung tokenized words galing sa training data ng model ginawang dictionary
lateinit var interpreter: Interpreter

var preprocessedUrl: String = ""
// Define the maximum length for padding
var maxLength = 130 //base sa model
lateinit var tokens: List<Int>
lateinit var paddedSequence: IntArray

val blockedKeywords = listOf("whore", "penis", "dick", "pussy")
// eto papalitan ng database firebase, mga example ng mga false positive ni model
class Childscreen  : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var firebaseFirestore: FirebaseFirestore
    companion object {
        private const val SESSION_PREFS = "session_prefs"
        private const val KEYWORD_SESSION = "keyword_session"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_homescreen)
        firebaseFirestore = FirebaseFirestore.getInstance()

        // Load the TensorFlow Lite model
        interpreter = loadModel(this, "LSTM_Model.tflite")

        // Load word-index mappings from word_dict.json
        wordIndexMap =
            loadWordIndexMap(this, "word_dict.json") //dictionary ng words with tokens ex. fuck : 4;

        webView = findViewById(R.id.theWebView)
        webView.loadUrl("https://www.google.com")
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: ""
                preprocessedUrl = preprocessText(url)
                // Check if the session keyword is blocked
                val sessionKeyword = getSession()
                if (sessionKeyword != null && preprocessedUrl.contains(sessionKeyword, ignoreCase = true)) {
                    startActivity(Intent(this@Childscreen, BlockedActivity::class.java))
                    return true
                }

                // Check if any of the words are blocked in Firestore
                isKeywordBlocked(preprocessedUrl) { isBlocked ->
                    if (isBlocked) {
                        startActivity(Intent(this@Childscreen, BlockedActivity::class.java))
                    } else {
                        loadUrl(url)
                    }
                }
                return true

                /*// Check if the session keyword is blocked
                val sessionKeyword = getSession()
                if (sessionKeyword != null && preprocessedUrl.contains(sessionKeyword, ignoreCase = true)) {
                    startActivity(Intent(this@Childscreen, BlockedActivity::class.java))
                    return true
                }*/

                //asa kabila file function mga to
                val paddedSequence = TfLiteModel.cleanUrl(preprocessedUrl, maxLength)
                val predictedLabel = TfLiteModel.modelInference(
                    preprocessedUrl,
                    interpreter,
                    wordIndexMap,
                    maxLength,
                    paddedSequence
                )
                Log.d("Predicted Label:", predictedLabel.toString())

                if (predictedLabel == 1) {
                    // matic blocked
                    startActivity(Intent(this@Childscreen, BlockedActivity::class.java))
                    return true
                } else { // incase of false positive daan sa database na may keywords
                    val detectedKeyword = containsBlockedKeywords(preprocessedUrl)
                    return if (containsBlockedKeywords(preprocessedUrl)) {
                        // keywrods from database detected == blocked
                        startActivity(Intent(this@Childscreen, BlockedActivity::class.java))
                        Log.d("Detected Keyword:", detectedKeyword.toString())
                        true
                    } else {
                        // All good proceed
                        //
                        false
                    }
                }
                // Check if any of the words are blocked in Firestore
                isKeywordBlocked(preprocessedUrl) { isBlocked ->
                    if (isBlocked) {
                        startActivity(Intent(this@Childscreen, BlockedActivity::class.java))
                    } else {
                        loadUrl(url)
                    }
                }
                return true
                isSiteBlocked(url) { isBlocked ->
                    if (!isBlocked) {
                        webView.clearCache(true) // Clear the WebView cache
                        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
                        webView.settings.domStorageEnabled = false
                        webView.clearHistory()
                        webView.loadUrl(url)
                    } else {
                        Toast.makeText(
                            this@Childscreen,
                            "This site is blocked by KODA App",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        // para di lng mag exit
        @Deprecated("Deprecated in Java")
        fun onBackPressed() {
            // Check if the WebView can go back in its history
            if (webView.canGoBack()) {
                // If so, go back in the WebView's history
                webView.goBack()
            } else {
                // If not, perform the default back button action (exit the activity)
                super.onBackPressed()
            }
        }
    }

    private fun loadUrl(url: String) {
        webView.clearCache(true)
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.domStorageEnabled = false
        webView.clearHistory()
        webView.loadUrl(url)
    }

    private fun isKeywordBlocked(url: String, callback: (Boolean) -> Unit) {
        firebaseFirestore.collection("blocked_Keywords").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val blockedWords = document.get("words") as? List<String> ?: emptyList()
                    if (blockedWords.any { url.contains(it, ignoreCase = true) }) {
                        callback(true)
                        return@addOnSuccessListener
                    }
                }
                callback(false)
            }
            .addOnFailureListener { exception ->
                Log.e("Error", "Error searching keyword in Firestore: $exception")
                callback(false)
            }
    }

    private fun getSession(): String? {
        val prefs = getSharedPreferences(SESSION_PREFS, MODE_PRIVATE)
        return prefs.getString(KEYWORD_SESSION, null)
    }

    private fun saveSession(keyword: String) {
        val prefs = getSharedPreferences(SESSION_PREFS, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEYWORD_SESSION, keyword)
        editor.apply()
    }
    override fun onResume() {
        super.onResume()
        val keyword = intent.getStringExtra("keyword")
        if (keyword!= null) {
            saveSession(keyword)
        }
    }
    fun isSiteBlocked(url: String, callback: (Boolean) -> Unit) {
        val site = Uri.parse(url).host
        if (site != null) {
            firebaseFirestore.collection("blocked_Sites").document(site).get()
                .addOnSuccessListener { document ->
                    callback(document.getBoolean("blocked") ?: false)
                }
        } else {
            callback(false)
        }
    }

    fun updateBlockedStatus(site: String, isBlocked: Boolean) {
        val url = Uri.parse(site)
        val domain = url.host ?: return
        val documentReference = firebaseFirestore.collection("blocked_Sites").document(domain)
        documentReference.set(mapOf("blocked" to isBlocked))
    }
}