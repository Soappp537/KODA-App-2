package com.example.kodaapplication

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class child_homescreen : AppCompatActivity() {
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
        setContentView(R.layout.activity_child_homescreen)

        val toChild = findViewById<ExtendedFloatingActionButton>(R.id.fab)
        toChild.setOnClickListener {
            startActivity(Intent(this@child_homescreen, addChildInfo::class.java))
            finish()
        }
    }
}