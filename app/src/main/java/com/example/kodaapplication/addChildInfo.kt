package com.example.kodaapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView.FindListener
import android.widget.Toast
import com.example.kodaapplication.databinding.ActivityAddChildInfoBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class addChildInfo : AppCompatActivity() {
    private lateinit var binding : ActivityAddChildInfoBinding
    private lateinit var database : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChildInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonChild.setOnClickListener {
            createChild()
        }
    }

    private fun createChild() {
        val firstName = binding.childFirstName.text.toString()
        val lastName = binding.childLastName.text.toString()
        val age = binding.childAge.text.toString()
        val randomId = UUID.randomUUID().toString().substring(0,10)

        database = FirebaseDatabase.getInstance().getReference("childData")
        val childData = childData(randomId,firstName,lastName,age)
        database.child(firstName).setValue(childData).addOnSuccessListener {
            binding.childFirstName.text.clear()
            binding.childLastName.text.clear()
            binding.childAge.text.clear()

            Toast.makeText(this,"Successfully Added!",Toast.LENGTH_SHORT).show()
            finish()

        }.addOnFailureListener {
            Toast.makeText(this,"Failed!",Toast.LENGTH_SHORT).show()
        }
    }
}