package com.example.kodaapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ServicesInitializer : AppCompatActivity() {

    private var isServiceRunning = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AccessibilityPref", MODE_PRIVATE)

        // Start the ForegroundService only if it's not already running
        if (!isServiceRunning) {
            val serviceIntent = Intent(this, ChildMainService::class.java)
            serviceIntent.action = ChildMainService.CounterAction.START.name
            startService(serviceIntent)
            isServiceRunning = true // Set the flag to indicate that the service is running
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset the flag when the activity is resumed
        isServiceRunning = false
    }
}


