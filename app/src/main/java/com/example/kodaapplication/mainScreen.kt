package com.example.kodaapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class mainScreen : AppCompatActivity() {
    companion object {
        private const val TAG = "mainScreen"
        private const val RC_SIGN_IN = 123
    }
    private lateinit var childRecyclerView: RecyclerView
    private lateinit var childArrayList: ArrayList<childData>
    private lateinit var aadapter: childAdapter
    private lateinit var bd: FirebaseFirestore

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


        childRecyclerView = findViewById(R.id.main_recyclerView)
        childRecyclerView.layoutManager = LinearLayoutManager(this)
        childRecyclerView.setHasFixedSize(true)
        childArrayList = arrayListOf()
        aadapter = childAdapter(childArrayList)
        childRecyclerView.adapter = aadapter

        getChildData()

        val fab: ExtendedFloatingActionButton = findViewById(R.id.fabBB)
        fab.setOnClickListener {
            startActivity(Intent(this@mainScreen, ShowParentInfo::class.java))
        }
    }

    private fun getChildData() {
        bd = FirebaseFirestore.getInstance()
        bd.collection("ChildAccounts")
            .whereEqualTo("parentId", CurrentUser.loggedInParentId)
            .addSnapshotListener(object : EventListener<QuerySnapshot>{
                @SuppressLint("NotifyDataSetChanged")
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ){
                    if (error != null){
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }
                    childArrayList.clear()
                    for (dc : DocumentChange in value?.documentChanges!!){
                        if (dc.type == DocumentChange.Type.ADDED){
                            childArrayList.add(dc.document.toObject(childData::class.java))
                        }
                    }
                    aadapter.notifyDataSetChanged()
                }
            })
    }
}