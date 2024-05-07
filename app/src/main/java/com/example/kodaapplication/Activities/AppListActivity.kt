package com.example.kodaapplication.Activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kodaapplication.R
import com.google.firebase.firestore.FirebaseFirestore

class AppListActivity : AppCompatActivity() {

    private lateinit var appRecyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("ChildAccounts")  // Make this global
    private var newChildId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_app_list)

        appRecyclerView = findViewById(R.id.app_list)
        appRecyclerView.layoutManager = LinearLayoutManager(this)

        newChildId = intent.getStringExtra("newchildId")
        Log.d("AppListActivity", "Received newchildId: $newChildId")

        fetchAppDataFromFirestore()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    data class AppItem(val label: String, val packageName: String, var locked: Boolean = false)

    interface OnAppItemClickListener {
        fun onAppItemClick(app: AppItem)
    }

    class AppAdapter(
        private val apps: List<AppItem>,
        private val onAppItemClickListener: OnAppItemClickListener,
        private val newChildId: String?

        ) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
        class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val appName: TextView = itemView.findViewById(R.id.app_name)
            val toggleButton: ToggleButton = itemView.findViewById(R.id.toggle_button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
            return AppViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            holder.appName.text = apps[position].label
            holder.toggleButton.isChecked = apps[position].locked
            holder.toggleButton.setOnCheckedChangeListener { _, isChecked ->
                val updatedApp = apps[position].copy(locked = isChecked)

                if (isChecked) {
                    Toast.makeText(
                        holder.itemView.context,
                        "App is being locked",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "App is now unlocked",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                updateAppInFirestore(updatedApp, newChildId)
            }
            holder.itemView.setOnClickListener { onAppItemClickListener.onAppItemClick(apps[position]) }
        }

        private fun updateAppInFirestore(updatedApp: AppItem, newChildId: String?) {
            // Reference to your collection
            val db = FirebaseFirestore.getInstance()
            val collectionRef = db.collection("ChildAccounts")

            // Construct a query to retrieve documents where childId equals the specified value
            val query = collectionRef.whereEqualTo("childId", newChildId) // change to newChild after testing
            // Execute the query
            Log.d("updateAppInFirestore", "Received newchildId: $newChildId")
            query.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // Access the "apps" map within the document
                        val appsMap = document["apps"] as? Map<String, Map<String, Any>> ?: continue

                        // Iterate over each entry in the "apps" map
                        for ((appName, appData) in appsMap) {
                            val packageName = appData["packageName"] as? String ?: ""
                            if (packageName == updatedApp.packageName) {
                                val updatedAppData = appData.toMutableMap().apply {
                                    put("isBlocked", updatedApp.locked)
                                }
                                document.reference.update("apps.$appName", updatedAppData)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "FirestoreData",
                                            "DocumentSnapshot successfully updated!"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("FirestoreData", "Error updating document", e)
                                    }
                                break
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreData", "Error getting documents: ", exception)
                }
        }

        override fun getItemCount(): Int = apps.size
    }

    private fun fetchAppDataFromFirestore() {

        val query = collectionRef.whereEqualTo("childId", newChildId)
        Log.d("fetchAppDataFrom", "Received newchildId: $newChildId")

        query.get()
            .addOnSuccessListener { documents ->
                val apps = mutableListOf<AppItem>()
                for (document in documents) {

                    val appsMap = document["apps"] as? Map<String, Map<String, Any>> ?: continue

                    // Iterate over each entry in the "apps" map
                    for ((appName, appData) in appsMap) {
                        val label = appData["label"] as? String ?: ""
                        val packageName = appData["packageName"] as? String ?: ""
                        val isBlocked = appData["isBlocked"] as? Boolean ?: false
                        apps.add(AppItem(label, packageName, isBlocked))
                    }
                }
                updateRecyclerView(apps)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreData", "Error getting documents: ", exception)
            }
    }

    private fun updateRecyclerView(apps: List<AppItem>) {
        appRecyclerView.adapter = AppAdapter(apps, object : OnAppItemClickListener {
            override fun onAppItemClick(app: AppItem) {
                if (app.locked) {
                    Toast.makeText(
                        this@AppListActivity,
                        "App is locked by KODA App",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@AppListActivity,
                        "You clicked on ${app.label}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }, newChildId) // Pass newChildId here
    }

}
