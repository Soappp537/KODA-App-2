package com.example.kodaapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.kodaapplication.databinding.ActivityAddChildInfoBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.util.UUID

class addChildInfo : AppCompatActivity() {
    private lateinit var binding : ActivityAddChildInfoBinding
    private var db = Firebase.firestore
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChildInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        /*binding.modernButton.setOnClickListener {
            getParentId()
        }*/

        binding.buttonChild.setOnClickListener {
            addChildToFireStore()
        }

    }

    private fun getParentId(callback: (parentId: String) -> Unit) {
        val currentUserId = CurrentUser.loggedInParentId

        db.collection("ParentAccounts")
            .whereEqualTo("id", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val documentSnapshot = documents.documents[0]
                    val parentId = documentSnapshot.id
                    callback(parentId)
                } else {
                    Toast.makeText(this, "Parent account not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get parent ID: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addChildToFireStore() {
        val storeFirstName = binding.childFirstName.text.toString().trim()
        val storeLastName = binding.childLastName.text.toString().trim()
        val storeChildAge = binding.childAge.text.toString().trim()

        // Check if all fields are filled
        if (storeFirstName.isEmpty() || storeLastName.isEmpty() || storeChildAge.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields first to proceed", Toast.LENGTH_SHORT).show()
            return
        }

        getParentId { parentId ->
            val randomId = UUID.randomUUID().toString().substring(0, 10)

            val userMap = hashMapOf(
                "age" to storeChildAge,
                "childId" to randomId,
                "parentId" to parentId,
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
                    /*binding.parentId.text.clear()*/
                    val intent = Intent(this@addChildInfo, ActivityPermissions::class.java)
                    startActivity(intent)
                    finish() // Finish the current activity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed added", Toast.LENGTH_SHORT).show()
                }
        }
    }


}