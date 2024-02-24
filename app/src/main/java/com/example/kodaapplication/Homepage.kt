package com.example.kodaapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton


class Homepage : AppCompatActivity() {
    var dialog: AlertDialog? = null
    var layout: LinearLayout? = null
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
        setContentView(R.layout.activity_homepage)
       /* val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            showAlertDialog()
        }*/
        val add = findViewById<ExtendedFloatingActionButton>(R.id.addingBtn)
        layout = findViewById(R.id.recycle_container)
        buildDialog()
        add.setOnClickListener {
            dialog?.show()
        }

    }
    private fun buildDialog(){
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.add_item, null)
        val name: EditText = view.findViewById(R.id.userName)
        val age: EditText = view.findViewById(R.id.userAge)
        builder.setView(view)
            .setPositiveButton("Ok"){
                dialog, which ->
                addCard(name.text.toString(), age.text.toString())
            }
            .setNegativeButton("Cancel"){
                dialog, which ->
                dialog.dismiss()
            }
        dialog = builder.create()
    }
    private fun addCard(name: String, age: String){
        val view = layoutInflater.inflate(R.layout.list_item, null)
        val nameView: TextView = view.findViewById(R.id.mTitle)
        val ageView: TextView = view.findViewById(R.id.mSubTitle)
        val delete: Button = view.findViewById(R.id.delete)
        val layoutOuter: LinearLayout = view.findViewById(R.id.outer_layout)
        nameView.text = name
        ageView.text = (age + " years old")
        delete.setOnClickListener {
            layout?.removeView(view)
        }
        layoutOuter.setOnClickListener {
            startActivity(Intent(this@Homepage, child_homescreen::class.java))
            finish()
        }
        layout?.addView(view)
    }
    /*fun showAlertDialog() {
        // Create a new AlertDialog builder
        val builder = AlertDialog.Builder(this)
        // Set the title and message
        builder.setTitle("KODA APP")
        builder.setMessage("Go back to login screen?")
        // Set the positive button
        builder.setPositiveButton("Yes") { dialog, which ->
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            Toast.makeText(this, "Login Screen", Toast.LENGTH_SHORT).show()

        }
        // Set the negative button
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        // Create and show the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }*/
    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}