package com.example.kodaapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class addChildData : AppCompatActivity() {
    private lateinit var etFName: EditText
    private lateinit var etLName: EditText
    private lateinit var etAge: EditText
    private lateinit var etParentId: EditText
    private lateinit var etButton: Button
    private lateinit var backButtoon: Button
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_child_data)

        etFName = findViewById(R.id.sFName)
        etLName = findViewById(R.id.sLName)
        etAge = findViewById(R.id.sAge)
        etParentId = findViewById(R.id.sParentId)
        etButton = findViewById(R.id.sButton)
        backButtoon = findViewById(R.id.backTextView)

        etButton.setOnClickListener {
            addToFireStore()
        }
        /*backButtoon.setOnClickListener {
            finish() // Close the current activity and go back to the previous one
        }*/
    }

    private fun addToFireStore() {
        val sFirstName = etFName.text.toString().trim()
        val sLastName = etLName.text.toString().trim()
        val sAge = etAge.text.toString().trim()
        val sParentId = etParentId.text.toString().trim()

        val userMap = hashMapOf(
            "first name" to sFirstName,
            "last name" to sLastName,
            "age" to sAge,
            "parent id" to sParentId
        )
        /*val userD = FirebaseAuth.getInstance().currentUser!!.uid*/
        db.collection("ChildAccounts").document().set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Successfully added", Toast.LENGTH_SHORT).show()
                etFName.text.clear()
                etLName.text.clear()
                etAge.text.clear()
                etParentId.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed added", Toast.LENGTH_SHORT).show()
            }

    }


}
