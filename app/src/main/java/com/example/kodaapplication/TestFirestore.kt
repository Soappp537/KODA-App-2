package com.example.kodaapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class TestFirestore : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etButton: Button
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_firestore)

        etName = findViewById(R.id.sName)
        etAddress = findViewById(R.id.sAddress)
        etEmail = findViewById(R.id.sEmail)
        etPassword = findViewById(R.id.sPassword)
        etButton = findViewById(R.id.sButton)

        etButton.setOnClickListener {
            addToFireStore()
        }
    }

    private fun addToFireStore() {
        val sName = etName.text.toString().trim()
        val sAddress = etAddress.text.toString().trim()
        val sEmail = etEmail.text.toString().trim()
        val sPassword = etPassword.text.toString().trim()

        val userMap = hashMapOf(
            "name" to sName,
            "address" to sAddress,
            "email" to sEmail,
            "password" to sPassword
        )
        /*val userD = FirebaseAuth.getInstance().currentUser!!.uid*/
        db.collection("ChildAccounts").document().set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Successfully added", Toast.LENGTH_SHORT).show()
                etName.text.clear()
                etAddress.text.clear()
                etEmail.text.clear()
                etPassword.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed added", Toast.LENGTH_SHORT).show()
            }

    }


}
