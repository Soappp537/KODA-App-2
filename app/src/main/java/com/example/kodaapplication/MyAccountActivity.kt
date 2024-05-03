package com.example.kodaapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kodaapplication.databinding.ActivityMyAccountBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MyAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAccountBinding
    private lateinit var session: SessionManager
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setupUI()
        setupLogoutButton()
        setupDeleteAccountButton()
    }

    private fun setupUI() {
        val username = session.getUsername()
        val parentId = CurrentUser.loggedInParentId

        binding.usernameTextview.text = getString(R.string.username_format, username)
        binding.idTextview.text = getString(R.string.id_format, parentId)

        binding.initialsTextview.text = (username.firstOrNull()?.uppercaseChar() ?: "").toString()
    }
    private fun setupLogoutButton() {
        binding.logOutParent.setOnClickListener {
            showLogoutDialog()
        }
    }
    private fun setupDeleteAccountButton() {
        binding.deleteParent.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun showDeleteAccountDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.delete_account)
        alertDialog.setMessage(R.string.delete_account_confirmation)
        alertDialog.setPositiveButton(R.string.yes) { _, _ ->
            deleteAccountAndNavigateToLogin()
        }
        alertDialog.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun deleteAccountAndNavigateToLogin() {
        db.collection("ParentAccounts").document(CurrentUser.loggedInParentId).delete()
            .addOnSuccessListener {
                db.collection("ChildAccounts").whereEqualTo("parentId", CurrentUser.loggedInParentId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            document.reference.delete()
                        }
                        session.logout()
                        CurrentUser.loggedInParentId = ""
                        Toast.makeText(this@MyAccountActivity, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@MyAccountActivity,
                            "Failed to delete child accounts: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@MyAccountActivity,
                    "Failed to delete parent account: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
}