package com.example.kodaapplication

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class mainScreen : AppCompatActivity() {
    companion object {
        private const val TAG = "mainScreen"
    }
    private lateinit var dbref : DatabaseReference
    private lateinit var childRecyclerView: RecyclerView
    private lateinit var childArrayList: ArrayList<childData>
    private lateinit var adapter: childAdapter
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
        setContentView(R.layout.activity_main_screen)

        /*val toChild = findViewById<ExtendedFloatingActionButton>(R.id.fab)
        toChild.setOnClickListener {
            startActivity(Intent(this@mainScreen, addChildInfo::class.java))
        }*/

        childRecyclerView = findViewById(R.id.main_recyclerView)
        childRecyclerView.layoutManager = LinearLayoutManager(this)
        childRecyclerView.setHasFixedSize(true)
        childArrayList = arrayListOf<childData>()
        getChildData()
    }

    private fun getChildData() {
        dbref = FirebaseDatabase.getInstance().getReference("childData")
        dbref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    childArrayList.clear()
                    for (childSnapshot in snapshot.children){
                        val childData = childSnapshot.getValue(childData::class.java)
                        val ageWithText = "${childData?.childAge} Years Old"
                        childData?.childAge = ageWithText
                        childArrayList.add(childData!!)
                    }
                    childRecyclerView.adapter = childAdapter(childArrayList)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database operation canceled: ${error.message}")
            }

        })
    }
}