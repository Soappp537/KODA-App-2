package com.example.kodaapplication.Services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.kodaapplication.Activities.BlockedActivity
import com.google.firebase.firestore.FirebaseFirestore

class ChildAppInterceptorService : AccessibilityService() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("onServiceConnected", "Service is Connected")
        // Initialize SharedPreferences
        sharedPreferences = applicationContext.getSharedPreferences("ChildIdPrefs", Context.MODE_PRIVATE)
        // Retrieve childId from SharedPreferences
        val childId = sharedPreferences.getString("childId", "") ?: ""
        Log.d("ChildAppInterceptor", "childId: $childId")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if (packageName != null) {
                checkLockStatus(packageName)
            }
        }
    }

    private fun checkLockStatus(packageName: String) {
        Log.d("lockingLogic", "Logic is Running")
        // Query Firestore to check if the app is marked as locked
        // Reference to your collection
//        val tempoChildId = "38749f37-c" //delete after testing
        val childId = sharedPreferences.getString("childId", "") ?: ""
        Log.d("childIdLocking", childId)
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("ChildAccounts")
        // Construct a query to retrieve documents where childId equals the specified value
        val query = collectionRef.whereEqualTo("childId", childId)

        // Execute the query
        query.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    // Access the "apps" map within the document
                    val appsMap = document["apps"] as? Map<String, Map<String, Any>> ?: continue

                    // Iterate over each entry in the "apps" map
                    for ((appName, appData) in appsMap) {
                        val label = appData["label"] as? String ?: ""
                        val firestorePackageName = appData["packageName"] as? String ?: ""
                        val isBlocked = appData["isBlocked"] as? Boolean ?: false
                        val status = if (isBlocked) "Locked" else "Unlocked"

                        // Check if the package name matches and the app is blocked
                        if (packageName == firestorePackageName && isBlocked) { //eto logic
                            // If the app is locked, prevent it from launching
                            preventAppLaunch()
                            return@addOnSuccessListener
                        }
                        // Log the package name if it's not blocked
                        Log.d("locking result", "packageName: $packageName, firestorePackageName: $firestorePackageName, Status: $isBlocked")
                    }


                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreData", "Error getting documents: ", exception)
            }
    }

    private fun preventAppLaunch() {
        // Create an Intent to start the BlockedActivity
        val intent = Intent(applicationContext, BlockedActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // Start the BlockedActivity
        applicationContext.startActivity(intent)
    }


    override fun onInterrupt() {
        // Handle interruption
    }
}
