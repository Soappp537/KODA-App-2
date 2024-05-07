package com.example.kodaapplication.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.kodaapplication.R

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

//data class AppItem(val label: String, val packageName: String, var isBlocked: Boolean)


class ChildMainService : Service() {

    private lateinit var sharedPreferences: SharedPreferences
    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("ChildAccounts")
    private val handler = android.os.Handler()
    private lateinit var runnable: Runnable
    private var isServiceRunning = false
    private val counter = Counter()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private lateinit var context: Context
    override fun onCreate() {
        super.onCreate()

        // Initialize SharedPreferences
        sharedPreferences = applicationContext.getSharedPreferences("ChildIdPrefs", Context.MODE_PRIVATE)
        // Retrieve childId from SharedPreferences
        val childId = sharedPreferences.getString("childId", "") ?: ""
        Log.d("ChildMainService", "childId: $childId")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!isServiceRunning){
            context = applicationContext
            // Schedule periodic execution of background tasks
            scheduleBackgroundTasks()
            // Create notification channel
            createNotificationChannel()
            // Set service running flag
            isServiceRunning = true
        }

        when(intent?.action){
            CounterAction.START.name -> start()
            CounterAction.STOP.name -> stop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun scheduleBackgroundTasks() {
        // Schedule tasks to run every minute (adjust as needed)
        runnable = Runnable {
//            fetchInstalledApps()
//            compareInstalledApps()
            fetchCompareApps()
            fetchAppDataFromFirestore()
            Log.d("LAP NUMBER", "5 second delay completed")
            handler.postDelayed(runnable, TimeUnit.MINUTES.toMillis(10))
        }
        handler.post(runnable)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("counter_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notification(counterValue: Int){
        val counterNotification = NotificationCompat
            .Builder(this,"counter_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Sending data on the database")
//            .setContentText("Elapsed Time $counterValue")
            .setStyle(NotificationCompat.BigTextStyle())
            .build()

        startForeground(1, counterNotification)
    }

    private fun start() {
        CoroutineScope(Dispatchers.Default).launch {
            counter.start().collect { counterValue ->
//                Log.d("Elapsed Time", counterValue.toString())
                notification(counterValue)
            }
        }
    }

    private fun stop() {
        counter.stop()
        stopSelf()
    }

    enum class CounterAction{
        START, RESUME, RESTART, PAUSE, STOP
    }

    class Counter {

        private var counterValue: Int = 0
        private var isRunning: Boolean = true

        fun start(): Flow<Int> = flow {
            while (isRunning){
                emit(counterValue)
                delay(1000)
                counterValue++
            }
        }

        fun stop() {
            isRunning = false
            counterValue = 0
        }
    }

    data class AppItem(val label: String, val packageName: String)

    private fun fetchCompareApps() {
//        Get list of installed apps
        val packageManager = packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .map {
                AppItem(
                    it.loadLabel(packageManager).toString(),
                    it.packageName
                )
            }
//        Compare installed apps with apps from Firestore
        compareInstalledApps(installedApps)
    }

    private fun compareInstalledApps(installedApps: List<AppItem>) {
        // Reference to your collection
//        val tempoChildId = "38749f37-c" //delete after testing
        val childId = sharedPreferences.getString("childId", "") ?: ""
        Log.d("childId", "compareInstalledApps")
        // Construct a query to retrieve documents where childId equals the specified value
        val query = collectionRef.whereEqualTo("childId", childId)

        // Execute the query
        query?.get()?.addOnSuccessListener { documents ->
            for (document in documents) {
                // Access the "apps" map within the document
                val appsMap = document["apps"] as? MutableMap<String, Map<String, Any>> ?: mutableMapOf()

                // Create a list to store package names of installed apps
                val installedPackageNames = installedApps.map { it.packageName }

                // Iterate over each app in Firestore
                val iterator = appsMap.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    val packageName = entry.key

                    // Check if the app from Firestore is installed
                    if (!installedPackageNames.contains(packageName)) {
                        // The app is not installed, remove it from Firestore
                        iterator.remove()
//                        Log.d("Comparison", "$packageName removed from Firestore")
                    }
                }
                // Iterate over installed apps and add missing ones to the appsData map
//                installedApps.forEach { installedApp ->
//                    val packageName = installedApp.packageName
//                    // Check if the app from installedApps is missing in Firestore
//                    if (!appsMap.containsKey(packageName)) {
//                        // Determine the next available index
//                        val nextIndex = appsMap.keys.filter { it.startsWith("app") }.size
//                        // Add the missing app details to appsMap using the next available index
//                        val newIndex = "app$nextIndex"
//                        val newApp = mapOf(
//                            "label" to installedApp.label,
//                            "packageName" to packageName,
//                            "isBlocked" to false // Assuming the default value is false
//                        )
//                        // Use the new index for the missing app
//                        appsMap[newIndex] = newApp
//                        // Log the addition of the missing app
//                        Log.d("Comparison", "${installedApp.label} added to Firestore")
//                    }
//                }
//
//// Update the Firestore document with the modified app list
//                document.reference.update("apps", appsMap)
//                    .addOnSuccessListener {
//                        Log.d("Updating Firestore", "Apps list updated successfully")
//                    }
//                    .addOnFailureListener { exception ->
//                        Log.e("Updating Firestore", "Error updating apps list: ", exception)
//                    }
            }
        }?.addOnFailureListener { exception ->
            Log.e("Comparison", "Error comparing apps: ", exception)
        }
    }

    private fun fetchAppDataFromFirestore() {
        // Reference to your collection
//        val tempoChildId = "38749f37-c" //delete after testing
        val childId = sharedPreferences.getString("childId", "") ?: ""
        Log.d("childId", "fetchAppDataFromFirestore")
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
                        val isBlocked = appData["isBlocked"] as? Boolean ?: false
                        val status = if (isBlocked) "Locked" else "Unlocked"

                        // Log the app name along with its lock status
                        Log.d("FirestoreData", "App: $label, Status: $status")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreData", "Error getting documents: ", exception) }
    }

}