package com.example.kodaapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class BlockedActivity : AppCompatActivity() {
    private lateinit var firebaseFirestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked)

        // Start the search
        val keyword = getIntent().getStringExtra("keyword")
        if (keyword != null) {
            searchKeywordInFirestore(keyword)
        }
    }
    private fun searchKeywordInFirestore(keyword: String) {
        firebaseFirestore.collection("blocked_Keywords").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val keywords = document.data.values
                    if (keywords.any { it.toString().contains(keyword, ignoreCase = true) }) {
                        // Keyword found, show the blocked page
                        findViewById<TextView>(R.id.blockedTextView).visibility = View.VISIBLE
                        return@addOnSuccessListener
                    }
                }
                // Keyword not found, go back to the previous activity
                finish()
            }
            .addOnFailureListener { exception ->
                Log.e("Error", "Error searching keyword in Firestore: $exception")
                finish()
            }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        // Do nothing, prevent the user from going back to the search results
    }
}