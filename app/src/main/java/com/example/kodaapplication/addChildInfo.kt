package com.example.kodaapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.example.kodaapplication.databinding.ActivityAddChildInfoBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore
import java.util.UUID

class addChildInfo : AppCompatActivity() {
    private lateinit var binding : ActivityAddChildInfoBinding
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChildInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonChild.setOnClickListener {
            addChildToFireStore()
        }

        binding.backTextView.setOnClickListener {
            startActivity(Intent(this@addChildInfo, child_homescreen::class.java))
        }
    }

    private fun addChildToFireStore() {
        val storeFirstName = binding.childFirstName.text.toString().trim()
        val storeLastName = binding.childLastName.text.toString().trim()
        val storeChildAge = binding.childAge.text.toString().trim()
        val randomId = UUID.randomUUID().toString().substring(0,10)

        val userMap = hashMapOf(
            "age" to storeChildAge,
            "firstName" to storeFirstName,
            "lastName" to storeLastName,
            "childId" to randomId
        )

        db.collection("ChildAccounts").document().set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Successfully added", Toast.LENGTH_SHORT).show()
                binding.childFirstName.text.clear()
                binding.childLastName.text.clear()
                binding.childAge.text.clear()
                /*binding.parentId.text.clear()*/
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed added", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        // Add any additional custom behavior here
    }
}