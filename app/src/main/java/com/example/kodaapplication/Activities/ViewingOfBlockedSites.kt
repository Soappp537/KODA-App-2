package com.example.kodaapplication.Activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kodaapplication.R
import com.google.firebase.firestore.FirebaseFirestore

class ViewingOfBlockedSites : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var recyclerViewBlockedSites: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_viewing_of_blocked_sites)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        recyclerViewBlockedSites = findViewById(R.id.recycler_view_blocked_sites)
        setupSwipeRefreshLayout()
        setupFirestoreQuery()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            setupFirestoreQuery()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Finish the current activity
        startActivity(Intent(this, pageForWebFiltering::class.java)) // Start the pageForWebFiltering activity
    }

    private fun setupFirestoreQuery() {
        firestore.collection("blocked_Sites")
            .get()
            .addOnSuccessListener { documents ->
                val blockedSites = documents.mapNotNull { doc ->
                    doc.getBoolean("blocked")?.let { blocked ->
                        if (blocked) doc.id else null
                    }
                }
                setupAdapter(blockedSites)
                swipeRefreshLayout.isRefreshing = false // Stop the refreshing animation
            }
            .addOnFailureListener { e ->
                // Handle any errors
                swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun setupAdapter(blockedSites: List<String>) {
        recyclerViewBlockedSites.adapter = BlockedSitesAdapter(blockedSites)
    }

    class BlockedSitesAdapter(private val blockedSites: List<String>) :
        RecyclerView.Adapter<BlockedSitesAdapter.ViewHolder>() {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val blockedSiteTextView: TextView = itemView.findViewById(R.id.text_blocked_site)
            val switchButton: Switch = itemView.findViewById(R.id.switch_button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_blocked_site, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val blockedSite = blockedSites[position]
            holder.blockedSiteTextView.text = blockedSite
            holder.switchButton.isChecked = isBlocked(blockedSite)

            holder.switchButton.setOnCheckedChangeListener { _, isChecked ->
                updateBlockedStatus(blockedSite, isChecked)
            }
        }

        override fun getItemCount(): Int {
            return blockedSites.size}

        private fun isBlocked(site: String): Boolean {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = firestore.collection("blocked_Sites").document(site)

            var isBlocked = false
            docRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document!= null) {
                        isBlocked = document.getBoolean("blocked")?: false
                    }
                }
            }
            return isBlocked
        }

        private fun updateBlockedStatus(site: String, blocked: Boolean) {
            val firestore = FirebaseFirestore.getInstance()
            val docRef = firestore.collection("blocked_Sites").document(site)

            docRef
                .update("blocked", blocked)
                .addOnSuccessListener {
                    // Update successful
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                }
        }
    }
}
