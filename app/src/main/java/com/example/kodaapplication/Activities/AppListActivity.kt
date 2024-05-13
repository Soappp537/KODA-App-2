package com.example.kodaapplication.Activities

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import java.util.Timer
import java.util.TimerTask

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

    data class AppItem(val label: String, val packageName: String, var isBlocked: Boolean)

    interface OnAppItemClickListener {
        fun onAppItemClick(app: AppItem)
    }

    inner class AppAdapter(
        private val apps: List<AppItem>
    ) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

        inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val appName: TextView = itemView.findViewById(R.id.app_name)
            val toggleButton: ToggleButton = itemView.findViewById(R.id.toggle_button)
            val setTimeButton: Button = itemView.findViewById(R.id.set_TimeButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
            return AppViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            val app = apps[position]
            holder.appName.text = app.label

            holder.toggleButton.isChecked = app.isBlocked

            holder.toggleButton.setOnClickListener {
                app.isBlocked = holder.toggleButton.isChecked
                updateAppInFirestore(app)
                notifyDataSetChanged()
            }
            holder.setTimeButton.setOnClickListener {
                val dialog = Dialog(holder.itemView.context)
                dialog.setContentView(R.layout.timer_dialog)

                val setTimerButton = dialog.findViewById<Button>(R.id.set_timer_button)
                val hoursEditText = dialog.findViewById<EditText>(R.id.timer_edit_textHours)
                val minutesEditText = dialog.findViewById<EditText>(R.id.timer_edit_textMinutes)
                val secondsEditText = dialog.findViewById<EditText>(R.id.timer_edit_textSeconds)

                setTimerButton.setOnClickListener {
                    val hours = hoursEditText.text.toString().toIntOrNull() ?: 0
                    val minutes = minutesEditText.text.toString().toIntOrNull() ?: 0
                    val seconds = secondsEditText.text.toString().toIntOrNull() ?: 0

                    val totalSeconds = hours * 3600 + minutes * 60 + seconds

                    if (totalSeconds > 0) {
                        // Start the timer for the app
                        startTimerForApp(app, totalSeconds)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            holder.itemView.context,
                            "Please enter a valid timer value",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                dialog.show()
            }

            holder.itemView.setOnClickListener { onAppItemClick(app) }
        }

        private fun startTimerForApp(app: AppItem, totalSeconds: Int) {
            // Start a timer for the app
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    app.isBlocked = true
                    updateAppInFirestore(app)
                    runOnUiThread {
                        notifyDataSetChanged()
                        val hours = totalSeconds / 3600
                        val minutes = (totalSeconds % 3600) / 60
                        val seconds = totalSeconds % 60
                        Toast.makeText(
                            this@AppListActivity,
                            "${app.label} has been blocked after $hours hour(s), $minutes minute(s), and $seconds second(s)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }, totalSeconds * 1000L)
            
        }

        override fun getItemCount(): Int = apps.size
    }

    private fun fetchAppDataFromFirestore() {
        val query = collectionRef.whereEqualTo("childId", newChildId)

        query.get()
            .addOnSuccessListener { documents ->
                val apps = mutableListOf<AppItem>()
                for (document in documents) {
                    val appsMap = document["apps"] as? Map<String, Map<String, Any>> ?: continue
                    for ((appName, appData) in appsMap) {
                        val label = appData["label"] as? String ?: ""
                        val packageName = appData["packageName"] as? String ?: ""
                        val isBlocked = appData["isBlocked"] as? Boolean ?: false
                        apps.add(AppItem(label, packageName, isBlocked))
                    }
                }
                appRecyclerView.adapter = AppAdapter(apps)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreData", "Error getting documents: ", exception)
            }
    }

    private fun updateAppInFirestore(updatedApp: AppItem) {
        val query = collectionRef.whereEqualTo("childId", newChildId)
        query.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val appsMap = document["apps"] as? Map<String, Map<String, Any>> ?: continue
                    for ((appName, appData) in appsMap) {
                        val packageName = appData["packageName"] as? String ?: ""
                        if (packageName == updatedApp.packageName) {
                            val updatedAppData = appData.toMutableMap().apply {
                                put("isBlocked", updatedApp.isBlocked)
                            }
                            document.reference.update("apps.$appName", updatedAppData)
                                .addOnSuccessListener {
                                    Log.d("FirestoreData", "DocumentSnapshot successfully updated!")
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

    private fun onAppItemClick(app: AppItem) {
        if (app.isBlocked) {
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
}
