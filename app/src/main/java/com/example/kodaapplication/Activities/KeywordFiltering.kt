package com.example.kodaapplication.Activities

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kodaapplication.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

interface OnToggleClickListener {
    fun onToggleClicked(category: Category, isChecked: Boolean)
}
class KeywordFiltering : AppCompatActivity(), OnToggleClickListener { // Implement OnToggleClickListener
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: CategoryAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_keyword_filtering)

        val urlFilter = findViewById<MaterialButton>(R.id.url_Filtering)
        urlFilter.setOnClickListener {
            startActivity(Intent(this, pageForWebFiltering::class.java))
        }
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("CategoryPreferences", MODE_PRIVATE)
        // Setup RecyclerView
        adapter = CategoryAdapter(emptyList(), this)
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
                    if (!name.startsWith("exclude_")) { // Exclude specific document
                        val words = document["words"] as? List<String> ?: emptyList()
                        categories.add(Category(name, words))
                    }
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

    override fun onToggleClicked(category: Category, isChecked: Boolean) {
        // Handle toggle click here
        category.isSelected = isChecked
        updateTogglePreference(category.name, isChecked) // Save the toggle state
        updateBlockedFieldInFirestore(category, isChecked)
    }

    private fun updateTogglePreference(categoryName: String, isChecked: Boolean) {
        // Save the toggle state in SharedPreferences or Firestore
        // For example, using SharedPreferences:
        val sharedPreferences = getSharedPreferences("CategoryPreferences", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(categoryName, isChecked).apply()
    }

    private fun updateBlockedFieldInFirestore(category: Category, isChecked: Boolean) {
        val collectionRef = firestore.collection("blocked_Keywords").document(category.name)
        collectionRef.update("blocked", isChecked) // Update the "blocked" field to the new value
            .addOnSuccessListener {
                if (isChecked) {
                    Log.d(TAG, "${category.name} is now blocked.")
                } else {
                    Log.d(TAG, "${category.name} is now unblocked.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating ${category.name} blocked status: ", exception)
            }
    }

    private fun blockWordsInFirestore(category: Category) {
        val collectionRef = firestore.collection("blocked_Keywords").document(category.name)
        collectionRef.update("blocked", true) // Update the "blocked" field to false
        for (word in category.words) {
            collectionRef.collection("words").document(word).delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Word $word is blocked.")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error unblocking word $word: ", exception)
                }
        }
    }
    private fun unblockWordsInFirestore(category: Category) {
        val collectionRef = firestore.collection("blocked_Keywords").document(category.name)
        collectionRef.update("blocked", false) // Update the "blocked" field to false
        for (word in category.words) {
            collectionRef.collection("words").document(word).delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Word $word is unblocked.")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error unblocking word $word: ", exception)
                }
        }
    }
}

data class Category(val name: String, val words: List<String>, var isSelected: Boolean = false)

class CategoryAdapter(private var categories: List<Category>, private val toggleClickListener: OnToggleClickListener) :
RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setCategories(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.category_name)
        val categoryToggle: Switch = itemView.findViewById(R.id.category_toggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryName.text = category.name
        holder.categoryToggle.isChecked = category.isSelected // Update toggle state
        /*holder.categoryToggle.setOnCheckedChangeListener(null) // Remove previous listener*/
        holder.categoryToggle.setOnCheckedChangeListener { _, isChecked ->
            // Update the isSelected property of the category when the toggle is clicked
            category.isSelected = isChecked
            toggleClickListener.onToggleClicked(category, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}