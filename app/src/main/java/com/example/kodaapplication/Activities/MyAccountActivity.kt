package com.example.kodaapplication.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kodaapplication.Classes.CurrentUser
import com.example.kodaapplication.R
import com.example.kodaapplication.databinding.ActivityMyAccountBinding
import com.google.firebase.firestore.FirebaseFirestore

class MyAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAccountBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setupUI()
        setupWebFilterButton()
        setupLogoutButton()
        setupDeleteButton()
    }

    private fun setupWebFilterButton() {
        binding.buttonWebFilter.setOnClickListener {
            startActivity(Intent(this, KeywordFiltering::class.java))
        }
    }

    private fun setupDeleteButton() {
        binding.deleteParent.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.delete_account)
        alertDialog.setMessage(R.string.delete_account_confirmation)
        alertDialog.setPositiveButton(R.string.yes) { _, _ ->
            deleteParentAndAssociatedChildAccounts()
        }
        alertDialog.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun deleteParentAndAssociatedChildAccounts() {
        val parentId = session.getParentId()
        val firestore = FirebaseFirestore.getInstance()

        // Delete parent account
        firestore.collection("ParentAccounts").document(parentId)
            .delete()
            .addOnSuccessListener {
                // Delete associated child accounts
                firestore.collection("ChildAccounts").whereEqualTo("parentId", parentId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val batch = firestore.batch()
                        for (document in querySnapshot.documents) {
                            batch.delete(document.reference)
                        }
                        batch.commit()
                            .addOnSuccessListener {
                                // Logout and navigate to login
                                logoutAndNavigateToLogin()
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
                            "Failed to fetch child accounts: ${e.message}",
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

        val ParentsharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val Parentdeditor = ParentsharedPreferences.edit()
        Parentdeditor.putBoolean("flowCompletedParent", false)
        Parentdeditor.apply()

        session.logout()
        CurrentUser.loggedInParentId = ""
        Toast.makeText(this@MyAccountActivity, "Account successfully logged out", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        /*session.logout()
        CurrentUser.loggedInParentId = ""
        Toast.makeText(this@MyAccountActivity, "Account successfully logged out", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()*/
    }

    private fun setupUI() {
        val username = session.getUsername()
        val parentId = session.getParentId()

        binding.usernameTextview.text = getString(R.string.username_format, username)
        binding.idTextview.text = getString(R.string.id_format, parentId)

        binding.initialsTextview.text = (username.firstOrNull()?.uppercaseChar() ?: "").toString()
    }
}