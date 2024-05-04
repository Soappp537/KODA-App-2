package com.example.kodaapplication

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class KeywordFiltering : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_keyword_filtering)

        // Setup RecyclerView
        val adapter = CategoryAdapter(emptyList())
        val recyclerView_forDocuments = findViewById<RecyclerView>(R.id.recyclerView_forDocuments)
        recyclerView_forDocuments.layoutManager = LinearLayoutManager(this)
        recyclerView_forDocuments.adapter = adapter

        // Fetch data from Firestore
        firestore.collection("blocked_Keywords")
            .get()
            .addOnSuccessListener { documents ->
                val categories = mutableListOf<Category>()
                for (document in documents) {
                    val name = document.id
                    val words = document["words"] as? List<String> ?: emptyList()
                    categories.add(Category(name, words))
                }
                adapter.setCategories(categories)
            }
            .addOnFailureListener { exception ->
                // Error handling
                Log.e(TAG, "Error getting documents: ", exception)
            }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    data class Category(val name: String, val words: List<String>)
    class CategoryAdapter(private var categories: List<Category>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
        @SuppressLint("NotifyDataSetChanged")
        fun setCategories(categories: List<Category>) {
            this.categories = categories
            notifyDataSetChanged()
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val categoryName: TextView = itemView.findViewById(R.id.category_name)
            /*val categoryWords: TextView = itemView.findViewById(R.id.category_words)*/
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category = categories[position]
            holder.categoryName.text = category.name
            /*holder.categoryWords.text = category.words.joinToString(", ")*/
        }

        override fun getItemCount(): Int {
            return categories.size
        }

    }
}