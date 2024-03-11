package com.example.kodaapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class sampleTest : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var kidsList: ArrayList<kidData>
    private var dd = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sample_test)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView = findViewById(R.id.recycView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        kidsList = arrayListOf()

        dd = FirebaseFirestore.getInstance()

        dd.collection("ChildAccounts").get()
            .addOnSuccessListener {
                if (!it.isEmpty){
                    for (data in it.documents){
                        val use: kidData? = data.toObject(kidData::class.java)
                        if (use != null){
                            kidsList.add(use)
                        }
                    }
                    recyclerView.adapter = ChildAccountsAdapter(kidsList)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this,"Failed!", Toast.LENGTH_SHORT).show()
            }
    }
}