package com.example.kodaapplication.Activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kodaapplication.Classes.CurrentUser
import com.example.kodaapplication.R
import com.example.kodaapplication.Classes.childAdapter
import com.example.kodaapplication.Classes.childData
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class mainScreen : AppCompatActivity(), childAdapter.OnItemClickListener {
    companion object {
        private const val TAG = "mainScreen"
        private const val RC_SIGN_IN = 123
    }
    private lateinit var childRecyclerView: RecyclerView
    private lateinit var childArrayList: ArrayList<childData>
    private lateinit var aadapter: childAdapter
    private lateinit var bd: FirebaseFirestore
    private lateinit var sessionManager: SessionManager // para sa parent ID

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

        sessionManager = SessionManager(this)// initialize session

        childRecyclerView = findViewById(R.id.main_recyclerView)
        childRecyclerView.layoutManager = LinearLayoutManager(this)
        childRecyclerView.setHasFixedSize(true)
        childArrayList = arrayListOf()
        aadapter = childAdapter(childArrayList, this)
        childRecyclerView.adapter = aadapter

        getChildData()

        val accShow = findViewById<ExtendedFloatingActionButton>(R.id.myAccount)
        accShow.setOnClickListener {
            val intent = Intent(this, MyAccountActivity::class.java)
            startActivity(intent)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getChildData() {
        bd = FirebaseFirestore.getInstance()
        val parentId = sessionManager.getParentId() // Retrive parent ID
        bd.collection("ChildAccounts")
            .whereEqualTo("parentId", parentId)
//            .whereEqualTo("parentId", CurrentUser.loggedInParentId) from this
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return@addSnapshotListener
                }
                childArrayList.clear()
                value?.let { snapshot ->
                    for (dc in snapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            childArrayList.add(dc.document.toObject(childData::class.java))
                        }
                    }
                    val parentID= CurrentUser.loggedInParentId
                    // Log the size of childArrayList
                    Log.d("Firestore", "Child data size: ${childArrayList.size}")
                    Log.d("PARENT", "LoggedID: $parentID")
                    aadapter.notifyDataSetChanged()
                }
            }
    }

    override fun onItemClick(childData: childData) {
        val intent = Intent(this, ChildDetails::class.java)
        intent.putExtra("childId", childData.childId)
        startActivity(intent)
    }

    private var dataLoaded = false
    override fun onResume() {
        super.onResume()
        if (!dataLoaded) {
            getChildData()
            dataLoaded = true
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Show a confirmation dialog before logging out
        val alertDialog = android.app.AlertDialog.Builder(this)
        alertDialog.setTitle("Log out?")
        alertDialog.setMessage("Are you sure you want to log out?")
        alertDialog.setPositiveButton("Yes") { _, _ ->
            // Clear the session and navigate to LoginActivity
            Toast.makeText(this@mainScreen, "Successfully Logged Out", Toast.LENGTH_SHORT).show()
            SessionManager(this).logout()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        alertDialog.setNegativeButton("No") { _, _ -> }
        alertDialog.show()
    }
}