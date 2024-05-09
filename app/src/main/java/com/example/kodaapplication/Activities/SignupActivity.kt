package com.example.kodaapplication.Activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.kodaapplication.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference /*required to create connection to the db*/
    private lateinit var auth: FirebaseAuth

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

        auth = FirebaseAuth.getInstance()

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("parentAccounts")



        binding.signupButton.setOnClickListener {
            val signupUsername = binding.getUsername.text.toString()
            val signupEmail = binding.getEmail.text.toString()
            val signupPassword = binding.getPassword.text.toString()

            if (signupUsername.isNotEmpty() && signupPassword.isNotEmpty()) {
                signupUser(signupUsername,signupEmail,signupPassword)
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


    private fun signupUser(username: String, email: String, password: String) {
        val usersCollection = FirebaseFirestore.getInstance().collection("ParentAccounts")
        val uniqueId = generateRandomString(10)
        val lowerCaseUsername = username.lowercase(Locale.getDefault())
        val eemail = email

        usersCollection.whereEqualTo("username", lowerCaseUsername)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    val userData = mapOf(
                        "id" to uniqueId,
                        "username" to lowerCaseUsername, // Save the lowercase username to the database
                        "email" to eemail, // Save the lowercase username to the database
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
                    Toast.makeText(
                        this@SignupActivity,
                        "Username already exists, please choose another one",
                        Toast.LENGTH_SHORT
                    ).show()
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
