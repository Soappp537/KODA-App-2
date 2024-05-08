package com.example.kodaapplication.Activities

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.kodaapplication.Classes.AppItem2
import com.google.firebase.firestore.FirebaseFirestore

class getChildApps : AppCompatActivity() {

    private lateinit var appRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.e("getChildApps", "Successful Run")

        // Get list of installed apps
        val packageManager = packageManager
        val apps = getInstalledApps(packageManager)

        // Save list of apps to database
        saveAppsToFirestore(apps)
        navigateToChildScreen()
    }

    private fun getInstalledApps(packageManager: PackageManager): List<AppItem2> {
        // Package names of specific apps to include
        val includedApps = listOf("com.google.android.youtube", "com.android.chrome")

        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter {
                // Check if the app is included in the list or is a user-installed app with a launch intent
                includedApps.contains(it.packageName) ||
                        (it.flags and ApplicationInfo.FLAG_SYSTEM == 0 && packageManager.getLaunchIntentForPackage(it.packageName) != null)
            }
            .map {
                AppItem2(it.loadLabel(packageManager).toString(), it.packageName, false)
            }
    }

    private fun isGoogleApp(packageName: String): Boolean {
        // Check if the package name belongs to Google apps or not
        return packageName.startsWith("com.google") || packageName.startsWith("com.android.vending")
    }

    private fun navigateToChildScreen() {
        val childIdSharedPreferences = getSharedPreferences("ChildIdPrefs", Context.MODE_PRIVATE)
        val childId = childIdSharedPreferences.getString("childId", null)
        Log.e("getChildApps", "ID $childId")


        val intent = Intent(this, Childscreen::class.java)
        startActivity(intent)
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
