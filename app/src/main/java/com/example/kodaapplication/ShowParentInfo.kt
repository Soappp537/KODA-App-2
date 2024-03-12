package com.example.kodaapplication

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowParentInfo : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var usernameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_parent_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.show_parent_info)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("parentAccounts")
        usernameTextView = findViewById(R.id.username)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            databaseReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userData = userSnapshot.getValue(UserData::class.java)
                            Log.d("ShowParentInfo", "User Data: $userData")
                            if (userData != null) {
                                usernameTextView.text = "Username: ${userData.username}"
                            }
                        }
                    } else {
                        Log.d("ShowParentInfo", "No user data found")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}