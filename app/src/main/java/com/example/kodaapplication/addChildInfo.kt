package com.example.kodaapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.kodaapplication.databinding.ActivityAddChildInfoBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firestore.v1.DocumentChange
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

        /*binding.backTextView.setOnClickListener {
            startActivity(Intent(this@addChildInfo, child_homescreen::class.java))
            finish()
        }*/
    }

    private fun addChildToFireStore() {
        val storeFirstName = binding.childFirstName.text.toString().trim()
        val storeLastName = binding.childLastName.text.toString().trim()
        val storeChildAge = binding.childAge.text.toString().trim()
        val storeChildParentId = binding.parentId.text.toString().trim()
        val randomId = UUID.randomUUID().toString().substring(0,10)

        val userMap = hashMapOf(
            "age" to storeChildAge,
            "childId" to randomId,
            "parentId" to storeChildParentId,
            "firstName" to storeFirstName,
            "lastName" to storeLastName,
        )
        db.collection("ChildAccounts").document().set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Successfully added", Toast.LENGTH_SHORT).show()
                println("Child account created successfully")
                binding.childFirstName.text.clear()
                binding.childLastName.text.clear()
                binding.childAge.text.clear()
                binding.parentId.text.clear()
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