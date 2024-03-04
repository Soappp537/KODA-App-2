package com.example.kodaapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class childAdapter(private val childList : ArrayList<childData>) : RecyclerView.Adapter<childAdapter.myViewHolder>() {

    @SuppressLint("SuspiciousIndentation") /*pending to remove*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,
            parent,false)
            return myViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return childList.size
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentItem = childList[position]
        holder.childFirstName.text = currentItem.childFirstName
        holder.childLastName.text = currentItem.childLastName
        holder.childAge.text = currentItem.childAge
    }

    class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val childFirstName: TextView = itemView.findViewById(R.id.mTitle)
        val childLastName: TextView = itemView.findViewById(R.id.mTitle2)
        val childAge: TextView = itemView.findViewById(R.id.mSubTitle)
    }

}