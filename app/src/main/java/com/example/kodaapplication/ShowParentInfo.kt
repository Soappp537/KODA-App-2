package com.example.kodaapplication

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ShowParentInfo : AppCompatActivity() {
    private lateinit var usernameTextView: TextView
    private lateinit var idTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_parent_info)

        usernameTextView = findViewById(R.id.par_username)
        idTextView = findViewById(R.id.par_id)

        val auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                // User is signed in
                val userId = currentUser.uid
                Log.d("ShowParentInfo", "Current user ID: $userId")
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("ParentAccounts")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val userData = document.toObject(UserData::class.java)
                            if (userData != null) {
                                usernameTextView.text = "Username: ${userData.username}"
                                idTextView.text = "ID: ${userData.id}"
                            } else {
                                Log.e("ShowParentInfo", "UserData is null")
                            }
                        } else {
                            Log.e("ShowParentInfo", "Document does not exist")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ShowParentInfo", "Error getting document", exception)
                        Toast.makeText(this@ShowParentInfo, "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // User is not signed in, handle this case
                Log.e("ShowParentInfo", "Current user is null")
                // For example, you can redirect the user to the login screen
                // or show a message indicating that they need to sign in
            }
        }
    }
}