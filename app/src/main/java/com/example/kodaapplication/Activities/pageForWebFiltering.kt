package com.example.kodaapplication.Activities

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
import com.example.kodaapplication.R
import com.google.firebase.firestore.FirebaseFirestore

sealed class FilterResult {
    object Filtered : FilterResult()
    object NotFiltered : FilterResult()
}

class pageForWebFiltering : AppCompatActivity() {
    /*private val filterText: EditText by lazy { findViewById<EditText>(R.id.filter_text) }
    private val filterButton: Button by lazy { findViewById<Button>(R.id.filter_button) }*/

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var blockSiteButton: Button
    private lateinit var unBlockSiteButton: Button
    private lateinit var editTextBlockSite: EditText
    private lateinit var editTextUnblockSite: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page_for_web_filtering)

        firebaseFirestore = FirebaseFirestore.getInstance()

        blockSiteButton = findViewById(R.id.block_button)
        editTextBlockSite = findViewById(R.id.block_site)
        /*unBlockSiteButton = findViewById(R.id.Unblock_button)*/
        /*editTextUnblockSite = findViewById(R.id.unblock_site)*/

        blockSiteButton.setOnClickListener {
            val siteToBlock = editTextBlockSite.text.toString()
            if (siteToBlock.isNotEmpty()) {
                blockSite(siteToBlock)
            } else {
                Toast.makeText(this, "Please enter a site to block", Toast.LENGTH_SHORT).show()
            }
        }

        /*unBlockSiteButton.setOnClickListener {
            val siteToUnblock = editTextUnblockSite.text.toString()
            if (siteToUnblock.isNotEmpty()) {
                unblockSite(siteToUnblock)
            } else {
                Toast.makeText(this, "Please enter a site to unblock", Toast.LENGTH_SHORT).show()
            }
        }*/

        val seeBlockedSites = findViewById<Button>(R.id.see_BlockedSites)
        seeBlockedSites.setOnClickListener {
            startActivity(Intent(this@pageForWebFiltering, ViewingOfBlockedSites::class.java))
            finish()
        }

        /*DI NA NEED ITO*/
        /*filterButton.setOnClickListener {
            val keyword = filterText.text.toString().trim()
            if (keyword.isNotEmpty()) {
                val intent = Intent(this, Childscreen::class.java)
                intent.putExtra("keyword", keyword)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter a keyword", Toast.LENGTH_SHORT).show()
            }
        }*/

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun searchKeywordInFirestore(keyword: String) {
        firebaseFirestore.collection("blocked_Keywords").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val keywords = document.data.values
                    if (keywords.any { it.toString().contains(keyword, ignoreCase = true) }) {
                        // Keyword found, block the search results
                        blockSearchResults()
                        return@addOnSuccessListener
                    }
                }
                // Keyword not found, do nothing
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error searching keyword in Firestore", Toast.LENGTH_SHORT).show()
                Log.e("Error", "Error searching keyword in Firestore: $exception")
            }
    }

    private fun blockSearchResults() {
        // Start the BlockedActivity to show the blocked page
        startActivity(Intent(this, BlockedActivity::class.java))
    }

    fun blockSite(url: String) {
        val site = Uri.parse(url).host
        if (site != null) {
            val docRef = firebaseFirestore.collection("blocked_Sites").document(site)
            docRef.set(mapOf("blocked" to true))

        } else {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
        }
    }

    /*fun unblockSite(url: String) {
        val site = Uri.parse(url).host
        if (site != null) {
            val docRef = firebaseFirestore.collection("blocked_Sites").document(site)
            docRef.set(mapOf("blocked" to false))

        } else {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
        }
    }*/
}
