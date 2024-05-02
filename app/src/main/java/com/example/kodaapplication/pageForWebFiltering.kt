package com.example.kodaapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

sealed class FilterResult {
    object Filtered : FilterResult()
    object NotFiltered : FilterResult()
}

class pageForWebFiltering : AppCompatActivity() {
    private val filterText: EditText by lazy { findViewById<EditText>(R.id.filter_text) }
    private val filterButton: Button by lazy { findViewById<Button>(R.id.filter_button) }

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var blockSiteButton: Button
    private lateinit var unBlockSiteButton: Button
    private lateinit var editTextBlockSite: EditText
    private lateinit var editTextUnblockSite: EditText

    private val BLOCKED_SITE_COLLECTION = "blocked_Sites"
    private val BLOCKED_KEYWORD_COLLECTION = "blocked_Keywords"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page_for_web_filtering)

        initViews()
        setupButtonListeners()
        setupWindowInsets()
    }

    private fun initViews() {
        firebaseFirestore = FirebaseFirestore.getInstance()

        blockSiteButton = findViewById(R.id.block_button)
        unBlockSiteButton = findViewById(R.id.Unblock_button)
        editTextBlockSite = findViewById(R.id.block_site)
        editTextUnblockSite = findViewById(R.id.unblock_site)
    }

    private fun setupButtonListeners() {
        blockSiteButton.setOnClickListener { blockSite(editTextBlockSite.text.toString()) }
        unBlockSiteButton.setOnClickListener { unblockSite(editTextUnblockSite.text.toString()) }
        filterButton.setOnClickListener { filterKeyword(filterText.text.toString().trim()) }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun filterKeyword(keyword: String) {
        if (keyword.isNotEmpty()) {
            val intent = Intent(this, Childscreen::class.java)
            intent.putExtra("keyword", keyword)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please enter a keyword", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchKeywordInFirestore(keyword: String) {
        firebaseFirestore.collection(BLOCKED_KEYWORD_COLLECTION)
            .whereArrayContainsAny("keywords", listOf(keyword.lowercase()))
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    blockSearchResults()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, R.string.error_searching_keyword, Toast.LENGTH_SHORT).show()
                Log.e("Error", "Error searching keyword in Firestore: $exception")
            }
    }

    private fun blockSearchResults() {
        startActivity(Intent(this, BlockedActivity::class.java))
    }

    fun blockSite(url: String) {
        val site = Uri.parse(url)?.host
        if (site!= null) {
            val docRef = firebaseFirestore.collection(BLOCKED_SITE_COLLECTION).document(site)
            docRef.set(mapOf("blocked" to true))
        } else {
            Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_SHORT).show()
        }
    }

    fun unblockSite(url: String) {
        val site = Uri.parse(url)?.host
        if (site!= null) {
            val docRef = firebaseFirestore.collection(BLOCKED_SITE_COLLECTION).document(site)
            docRef.set(mapOf("blocked" to false))
        } else {
            Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_SHORT).show()
        }
    }
}