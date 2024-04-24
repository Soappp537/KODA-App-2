package com.example.kodaapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

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
    override fun onBackPressed() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Warning")
            .setMessage("Are you sure you want to go back to the login screen?")
            .setPositiveButton("Yes") { _, _ ->
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                super.onBackPressed()
            }
            .setNegativeButton("No", null)
            .create()
        alertDialog.show()
    }
    fun parentClicked() {
        startActivity(Intent(this@MainActivity, mainScreen::class.java))
    }
    fun childCLicked() {
        startActivity(Intent(this@MainActivity, Childscreen::class.java))
    }
}