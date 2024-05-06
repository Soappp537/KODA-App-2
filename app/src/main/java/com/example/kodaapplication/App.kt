package com.example.kodaapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.TIRAMISU){
            val counterChannel = NotificationChannel(
                "notification_channel_name",
                "channel_description",
                NotificationManager.IMPORTANCE_LOW
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(counterChannel)
        }

    }
}