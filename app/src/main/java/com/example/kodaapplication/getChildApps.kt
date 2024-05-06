package com.example.kodaapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class getChildApps : AppCompatActivity() {

    private lateinit var appRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.e("getChildApps", "Successful Run")

        // Get list of installed apps
        val packageManager = packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .map { AppItem2(it.loadLabel(packageManager).toString(), it.packageName, false) }

        // Save list of apps to database
        saveAppsToFirestore(apps)
        navigateToChildScreen()
    }

    private fun navigateToChildScreen() {
        val childIdSharedPreferences = getSharedPreferences("ChildIdPrefs", Context.MODE_PRIVATE)
        val childId = childIdSharedPreferences.getString("childId", null)

        val intent = Intent(this, Childscreen::class.java)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun saveAppsToFirestore(apps: List<AppItem2>) {

        val sharedPreferences = getSharedPreferences("ChildIdPrefs", Context.MODE_PRIVATE)
        val childId = sharedPreferences.getString("childId", null)
        Log.e("getChildApps", "ID $childId")

        val db = FirebaseFirestore.getInstance()
        val appsData = hashMapOf<String, Any>()
        apps.forEachIndexed { index, app ->
            appsData["app$index"] = mapOf(
                "label" to app.label,
                "packageName" to app.packageName,
                "isBlocked" to false
            )
        }
        db.collection("ChildAccounts").whereEqualTo("childId", childId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Update each matching document with the app list data
                    document.reference
                        .update("apps", appsData)
                        .addOnSuccessListener {
                            Log.e("APPSTATUS", "SUCCESSFULLY ADDED APPS")
                        }
                        .addOnFailureListener { e ->
                            Log.e("APPSTATUS", "FAILED ADDING APPS")
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }
}
