package com.example.kodaapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
data class AppItem2(val label: String, val packageName: String, var isBlocked: Boolean)

class AppAdapter(private val apps: List<AppItem>) :
    RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define your views here
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        // Inflate your item layout and return a view holder
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.app_item, parent, false)
        return AppViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        // Bind data to views
    }

    override fun getItemCount(): Int = apps.size
}
