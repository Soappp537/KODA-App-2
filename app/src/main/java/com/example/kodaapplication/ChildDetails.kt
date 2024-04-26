package com.example.kodaapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class ChildDetails : AppCompatActivity() {
    private lateinit var childNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_child_details)
        childNameTextView = findViewById(R.id.cont_for_childName)
        val buttonLockApps = findViewById<MaterialButton>(R.id.button_lock_apps)

        buttonLockApps.setOnClickListener {
            val intent = Intent(this@ChildDetails, AppListActivity::class.java)
            startActivity(intent)
        }

        val childId = intent.getStringExtra("childId")
        if (childId != null) {
            fetchChildName(CurrentUser.loggedInParentId, childId)
        }

        val buttonWebFilter = findViewById<MaterialButton>(R.id.button_WebFilter)
        buttonWebFilter.setOnClickListener {
            startActivity(Intent(this, pageForWebFiltering::class.java))
        }

        val deleteChildButton = findViewById<MaterialButton>(R.id.delete_child)
        deleteChildButton.setOnClickListener {
            val childId = intent.getStringExtra("childId")
            if (childId!= null) {
                deleteChildAccount(CurrentUser.loggedInParentId, childId)
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun fetchChildName(parentId: String, childId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("ChildAccounts")
            .whereEqualTo("parentId", parentId)
            .whereEqualTo("childId", childId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.firstOrNull()
                    val firstName = document?.getString("firstName")
                    val lastName = document?.getString("lastName")
                    if (firstName != null) {
                        childNameTextView.text = "$firstName $lastName"
                    } else {
                        // Handle case where firstName is null
                        childNameTextView.text = "Name not available"
                    }
                } else {
                    // Handle case where document doesn't exist
                    childNameTextView.text = "Child not found"
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("ChildDetails", "Failed to fetch child's name: ${e.message}")
                Toast.makeText(this@ChildDetails, "Failed to fetch child's name", Toast.LENGTH_SHORT).show()
            }
    }
    private fun deleteChildAccount(parentId: String, childId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("ChildAccounts")
            .whereEqualTo("parentId", parentId)
            .whereEqualTo("childId", childId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("ChildDetails", "Child account deleted successfully")
                            val intent = Intent(this@ChildDetails, mainScreen::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChildDetails", "Failed to delete child account: ${e.message}")
                            Toast.makeText(this@ChildDetails, "Failed to delete child account", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.d("ChildDetails", "Child account not found")
                    Toast.makeText(this@ChildDetails, "Child account not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChildDetails", "Failed to fetch child account: ${e.message}")
                Toast.makeText(this@ChildDetails, "Failed to fetch child account", Toast.LENGTH_SHORT).show()
            }
    }
}