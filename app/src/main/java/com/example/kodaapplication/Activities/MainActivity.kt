package com.example.kodaapplication.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kodaapplication.R

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var session: SessionManager

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

        session = SessionManager(this)

        sharedPreferences = getSharedPreferences("UserMode", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        editor.putString("mode", "Parent")
        editor.apply()

        val parentButton = findViewById<Button>(R.id.parent_button)
        parentButton.setOnClickListener {
            parentClicked()

        }
        val childButton = findViewById<Button>(R.id.child_button)
        childButton.setOnClickListener {
           childCLicked()
        }
    }

    private fun navigateToChildScreen() {
        startActivity(Intent(this@MainActivity, Childscreen::class.java))
        finish() // Optionally finish this activity if not needed anymore
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
            navigateToPermissionActivity()
    }

    private fun navigateToPermissionActivity() {
        startActivity(Intent(this@MainActivity, ActivityPermissions::class.java))
        finish()
    }

}