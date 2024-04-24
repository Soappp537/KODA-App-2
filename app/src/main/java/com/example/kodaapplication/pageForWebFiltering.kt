package com.example.kodaapplication

import android.net.Uri
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page_for_web_filtering)

        firebaseFirestore = FirebaseFirestore.getInstance()

        blockSiteButton = findViewById(R.id.block_button)
        unBlockSiteButton = findViewById(R.id.Unblock_button)
        editTextBlockSite = findViewById(R.id.block_site)

        blockSiteButton.setOnClickListener {
            val siteToBlock = editTextBlockSite.text.toString()
            if (siteToBlock.isNotEmpty()) {
                blockSite(siteToBlock)
            } else {
                Toast.makeText(this, "Please enter a site to block", Toast.LENGTH_SHORT).show()
            }
        }
        unBlockSiteButton.setOnClickListener {
            val siteToUnblock = editTextBlockSite.text.toString()
            if (siteToUnblock.isNotEmpty()) {
                unblockSite(siteToUnblock)
            } else {
                Toast.makeText(this, "Please enter a site to unblock", Toast.LENGTH_SHORT).show()
            }
        }

        filterButton.setOnClickListener {
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
    fun blockSite(url: String) {
        val site = Uri.parse(url).host
        if (site != null) {
            val docRef = firebaseFirestore.collection("blocked_sites").document(site)
            docRef.set(mapOf("blocked" to true))

        } else {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
        }
    }
    fun unblockSite(url: String) {
        val domain = Uri.parse(url).host
        if (domain != null) {
            firebaseFirestore.collection("blocked_sites").document(domain).delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Site unblocked", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to unblock site", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show()
        }
    }
}