package com.example.kodaapplication.Activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kodaapplication.R

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

        val sharedPreferences = getSharedPreferences("UserMode", Context.MODE_PRIVATE)
        val mode = sharedPreferences.getString("mode", null)

//        if (mode != null) {
//            // Mode has been previously selected
//            if (mode == "Parent") {
//                // Navigate to ParentActivity
//                val intent = Intent(this, mainScreen::class.java)
//                startActivity(intent)
//                finish() // Optional: Close MainActivity
//            } else if (mode == "Child") {
//                // Navigate to ChildActivity
//                val intent = Intent(this, addChildInfo::class.java)
//                startActivity(intent)
//                finish()
//            }
//        } else {
//            // Mode has not been previously selected, continue with MainActivity
//        }

        val parentButton = findViewById<Button>(R.id.parent_button)
        parentButton.setOnClickListener {
            parentClicked()
            // Set user mode to Parent
//            setUserMode("Parent")
        }
        val childButton = findViewById<Button>(R.id.child_button)
        childButton.setOnClickListener {
            childCLicked()
//            setUserMode("Child")
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
        startActivityForResult(Intent(this@MainActivity, addChildInfo::class.java), CHILD_INFO_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHILD_INFO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Start Permissions activity
            startActivityForResult(Intent(this@MainActivity, ActivityPermissions::class.java), PERMISSIONS_REQUEST_CODE)
        } else if (requestCode == PERMISSIONS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Start getChildApps activity
            startActivityForResult(Intent(this@MainActivity, getChildApps::class.java), GET_APPS_REQUEST_CODE)
        } else if (requestCode == GET_APPS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // All one-time activities completed, navigate to ChildScreen
            startActivity(Intent(this@MainActivity, Childscreen::class.java))
        }
    }
    companion object {
        const val CHILD_INFO_REQUEST_CODE = 11
        const val PERMISSIONS_REQUEST_CODE = 12
        const val GET_APPS_REQUEST_CODE = 13
    }

//    private fun setUserMode(mode: String) {
//        sharedPreferences = getSharedPreferences("UserMode", MODE_PRIVATE)
//        editor = sharedPreferences.edit()
//        editor.putString("mode", mode)
//        editor.apply()
//
//        // Navigate to mainScreen or addChildInfo based on the selected mode
//        if (mode == "Parent") {
//            startActivity(Intent(this@MainActivity, mainScreen::class.java))
//        } else {
//            startActivity(Intent(this@MainActivity, addChildInfo::class.java))
//        }
//    }
}