package com.example.kodaapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class getAppsFromDatabase : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testing_activity)

        // Reference to your collection
        val collectionRef = db.collection("ChildAccounts")

// Replace "38749f37-c" with the desired childId
        val childId = "38749f37-c"

// Construct a query to retrieve documents where childId equals the specified value
        val query = collectionRef.whereEqualTo("childId", childId)

// Execute the query
        query.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Access the "apps" map within the document
                    val apps = document["apps"] as? Map<String, Map<String, Any>>
                    if (apps != null) {
                        Log.d("FirestoreData", "Apps Map: $apps")

                        // Iterate over each entry in the "apps" map
                        for ((appName, appData) in apps) {
                            val label = appData["label"] as? String ?: ""
                            val packageName = appData["packageName"] as? String ?: ""
                            val isBlocked = appData["isBlocked"] as? Boolean ?: false

                            // Logs for checking
                            Log.d("FirestoreData", "App Name: $appName, Label: $label, Package Name: $packageName, Is Blocked: $isBlocked")
                        }
                    } else {
                        Log.d("FirestoreData", "Document does not contain 'apps' field")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreData", "Error getting documents: ", exception)
            }
    }
}