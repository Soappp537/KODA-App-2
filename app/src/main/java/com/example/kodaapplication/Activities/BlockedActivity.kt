@file:Suppress("DEPRECATION")

package com.example.kodaapplication.Activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kodaapplication.R
import com.google.firebase.firestore.FirebaseFirestore

class BlockedActivity : AppCompatActivity() {
    private lateinit var firebaseFirestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked)

        // Start the search
        val keyword = intent.getStringExtra("keyword")
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
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    override fun onBackPressed() {
        super.onBackPressed()
        // Do nothing, prevent the user from going back to the search results
    }
}