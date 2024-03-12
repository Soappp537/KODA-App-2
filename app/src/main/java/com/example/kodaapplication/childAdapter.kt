package com.example.kodaapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kodaapplication.R.layout.list_item

class childAdapter(private val childList : ArrayList<childData>) : RecyclerView.Adapter<childAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): childAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val child : childData = childList[position]
        holder.nameA.text = child.firstName
        holder.nameB.text = child.lastName
        holder.nameAge.text = "${child.age} Years Old"
    }
    override fun getItemCount(): Int {
        return childList.size
    }
    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameA: TextView = itemView.findViewById(R.id.CFName)
        val nameB: TextView = itemView.findViewById(R.id.CLName)
        val nameAge: TextView = itemView.findViewById(R.id.CHAge)
    }

}