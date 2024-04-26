package com.example.kodaapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        aadapter = childAdapter(childArrayList, this)
        childRecyclerView.adapter = aadapter

        getChildData()

        val faab = findViewById<ExtendedFloatingActionButton>(R.id.fab)
        faab.setOnClickListener {
            startActivity(Intent(this@mainScreen, addChildInfo::class.java))
            finish()
        }


    }

    private fun getChildData() {
        bd = FirebaseFirestore.getInstance()
        bd.collection("ChildAccounts")
            .whereEqualTo("parentId", CurrentUser.loggedInParentId)
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
                    aadapter.notifyDataSetChanged()
                }
            }
    }

    override fun onItemClick(childData: childData) {
        val intent = Intent(this, ChildDetails::class.java)
        intent.putExtra("childId", childData.childId)
        startActivity(intent)
    }
}