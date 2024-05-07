package com.example.kodaapplication.Activities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kodaapplication.Classes.TfLiteModel
import com.example.kodaapplication.Classes.TfLiteModel.Companion.loadModel
import com.example.kodaapplication.Classes.TfLiteModel.Companion.loadWordIndexMap
import com.example.kodaapplication.Classes.TfLiteModel.Companion.normalizeText
import com.example.kodaapplication.Classes.TfLiteModel.Companion.preprocessText
import com.example.kodaapplication.Classes.TfLiteModel.Companion.translateLeetToNormal
import com.example.kodaapplication.R
import com.example.kodaapplication.Services.ChildMainService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.tensorflow.lite.Interpreter

// naka global declaration nung ibang variable para gumana ung functions sa kabila file TFLiteModel.kt
lateinit var wordIndexMap: Map<String, Int> // walang built in tokenizer android studio kaya ung tokenized words galing sa training data ng model ginawang dictionary
lateinit var interpreter: Interpreter

var preprocessedUrl: String = ""
var finalText: String = ""
// Define the maximum length for padding
var maxLength = 130 //base sa model
lateinit var tokens: List<Int>
lateinit var paddedSequence: IntArray

/*val blockedKeywords = listOf("whore", "penis", "dick", "pussy") example lang
// eto papalitan ng database firebase, mga example ng mga false positive ni model*/
@Suppress("UNREACHABLE_CODE")
class Childscreen : AppCompatActivity() {

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var webView: WebView
    private var isServiceRunning = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_homescreen)

        // Save the childId to SharedPreferences before starting the services
        val sharedPreferences = getSharedPreferences("ChildIdPrefs", Context.MODE_PRIVATE)
        val childId = sharedPreferences.getString("childId", null)
        Log.e("fromGetChildApps", "ID $childId")



        val childId2 = sharedPreferences.getString("childId", "") ?: ""
        Log.d("CHILD ID", childId2)

        //ServicesInitializer = eto na yun pero di ko muna binura ung file baka may need pa dun
        if (!isServiceRunning) { // Run the services
            val serviceIntent = Intent(this, ChildMainService::class.java)
            serviceIntent.action = ChildMainService.CounterAction.START.name
            intent.putExtra("childId", childId) // Pass childId to ChildMainService
            startService(serviceIntent)
            isServiceRunning = true // Set the flag to indicate that the service is running
        }

        firebaseFirestore = FirebaseFirestore.getInstance()
        // Load the TensorFlow Lite model
        interpreter = loadModel(this, "LSTM_Model.tflite")

        // Load word-index mappings from word_dict.json
        wordIndexMap = loadWordIndexMap(this, "word_dict.json") //dictionary ng words with tokens ex. fuck : 4;

        webView = findViewById(R.id.theWebView)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true // Enable JavaScript
        /*webView.loadUrl("https://www.google.com/search?q=&safe=strict")*/
        webView.loadUrl("https://www.google.com")
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: "" // url input
                preprocessedUrl = preprocessText(url) // preprocess
                // leet speak
                finalText = translateLeetToNormal(preprocessedUrl)

                val normalText = normalizeText(finalText)
                Log.d("FILTERING LOGIC","SUCCESSFUL RUN")
                Log.d("text:", url.toString())
                Log.d("preprocessed text:", preprocessedUrl.toString())
                Log.d("Final text:", finalText.toString())
                Log.d("Normalized:", normalText.toString())

                //asa kabila mga functions for this
                val paddedSequence = TfLiteModel.cleanUrl(finalText, maxLength)
                val predictedLabel = TfLiteModel.modelInference(
                    finalText,
                    interpreter,
                    wordIndexMap,
                    maxLength,
                    paddedSequence
                )
                Log.d("Predicted Label:", predictedLabel.toString())

                CoroutineScope(Dispatchers.Main).launch {
                    val isBlocked = containsBlockedKeywords(preprocessedUrl, firebaseFirestore)
                    if (predictedLabel == 1 || isBlocked) {
                        // matic blocked or detected keyword from database
                        startActivity(Intent(this@Childscreen, BlockedActivity::class.java))
                    } else {
                        if (url.startsWith("https://www.google.com/")) {
                            // Allow Google search page to load
                            view?.loadUrl(url)
                        } else {
                            // Check if the site is blocked
                            isSiteBlocked(url) { isBlocked ->
                                if (!isBlocked) {
                                    view?.clearCache(true) // Clear the WebView cache
                                    view?.settings?.cacheMode = WebSettings.LOAD_NO_CACHE
                                    view?.settings?.domStorageEnabled = false
                                    view?.clearHistory()
                                    view?.loadUrl(url)
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
                }
                return true
            }
        }
    }
    override fun onResume() {
        super.onResume()
        // Reset the flag when the activity is resumed
        isServiceRunning = false
    }


    fun updateBlockedStatus(site: String, isBlocked: Boolean) {
        val url = Uri.parse(site)
        val domain = url.host?: return
        val documentReference = firebaseFirestore.collection("blocked_Sites").document(domain)
        documentReference.set(mapOf("blocked" to isBlocked))
    }

    // para di lng mag exit
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Check if the WebView can go back in its history
        if (webView.canGoBack()) {
            // If so, go back in the WebView's history
            webView.goBack()
        } else {
            // If not, perform the default back button action (exit the activity)
            super.onBackPressed()
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
    suspend fun containsBlockedKeywords(url: String, firestore: FirebaseFirestore): Boolean {
        var isBlocked = false
        // Fetch data from Firestore
        firestore.collection("blocked_Keywords")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val words = document["words"] as? List<String>?: emptyList()
                    val isBlockedInDoc = document.getBoolean("blocked")?: false
                    if (isBlockedInDoc) {
                        for (word in words) {
                            if (url.contains(word, ignoreCase = true)) {
                                isBlocked = true
                                break
                            }
                        }
                    }
                    if (isBlocked) break
                }
            }
            .addOnFailureListener { exception ->
                // Error handling
                Log.e(ContentValues.TAG, "Error getting documents: ", exception)
            }
            .await() // Wait for Firestore operation to complete
        return isBlocked
    }
}