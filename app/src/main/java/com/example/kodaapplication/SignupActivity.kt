package com.example.kodaapplication

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import com.example.kodaapplication.databinding.ActivitySignupBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference /*required to create connection to the db*/

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
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("parentAccounts")

        binding.signupButton.setOnClickListener {
            val signupUsername = binding.getUsername.text.toString()
            val signupPassword = binding.getPassword.text.toString()

            if (signupUsername.isNotEmpty() && signupPassword.isNotEmpty()) {
                signupUser(signupUsername,signupPassword)
            }else{
                Toast.makeText(this@SignupActivity, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginRedirect.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            finish()
        }
    }
    override fun onBackPressed() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Warning")
            .setMessage("Are you sure you want to go back to the login screen?")
            .setPositiveButton("Yes") { _, _ ->
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                super.onBackPressed()
            }
            .setNegativeButton("No", null)
            .create()
        alertDialog.show()
    }


    private fun signupUser(username: String, password: String) {
        val usersCollection = FirebaseFirestore.getInstance().collection("ParentAccounts")
        val uniqueId = generateRandomString(10)

        usersCollection.document(uniqueId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    val userData = mapOf(
                        "id" to uniqueId,
                        "username" to username,
                        "password" to password
                    )
                    usersCollection.document(uniqueId)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@SignupActivity,
                                "Signup Successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this@SignupActivity,
                                "Failed to add user: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // Retry with a new unique ID if the generated ID already exists
                    signupUser(username, password)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@SignupActivity,
                    "Database Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    private fun generateRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}