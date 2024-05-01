package com.example.kodaapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kodaapplication.databinding.ActivityMyAccountBinding

class MyAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAccountBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        val username = session.getUsername()
        val id = CurrentUser.loggedInParentId

        binding.usernameTextview.text = "Username: $username"
        binding.idTextview.text = "ID: $id"

        binding.initialsTextview.text = username.substring(0, 1).toUpperCase()
    }
}