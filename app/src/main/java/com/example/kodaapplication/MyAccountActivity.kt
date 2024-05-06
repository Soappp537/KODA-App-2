package com.example.kodaapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

        setupUI()
        setupLogoutButton()
    }

    private fun setupLogoutButton() {
        binding.logOutParent.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.log_out)
        alertDialog.setMessage(R.string.log_out_confirmation)
        alertDialog.setPositiveButton(R.string.yes) { _, _ ->
            logoutAndNavigateToLogin()
        }
        alertDialog.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun logoutAndNavigateToLogin() {
        session.logout()
        CurrentUser.loggedInParentId = ""
        Toast.makeText(this@MyAccountActivity, "Account successfully logged out", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupUI() {
        val username = session.getUsername()
        val parentId = session.getParentId()

        binding.usernameTextview.text = getString(R.string.username_format, username)
        binding.idTextview.text = getString(R.string.id_format, parentId)

        binding.initialsTextview.text = (username.firstOrNull()?.uppercaseChar() ?: "").toString()
    }
}