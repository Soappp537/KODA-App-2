package com.example.kodaapplication

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.light(
//                Color.TRANSPARENT, Color.TRANSPARENT
//            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val parentButton = findViewById<Button>(R.id.parent_button)
        parentButton.setOnClickListener {
            parentClicked()
        }
        val childButton = findViewById<Button>(R.id.child_button)
        childButton.setOnClickListener {
            childCLicked()
        }
    }
    fun parentClicked() {
        startActivity(Intent(this@MainActivity, homepage::class.java))
        finish()
    }
    fun childCLicked() {
        startActivity(Intent(this@MainActivity, child_homescreen::class.java))
        finish()
    }
}