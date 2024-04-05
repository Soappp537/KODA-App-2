package com.example.kodaapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class childAdapter(private val childList: ArrayList<childData>, private val listener: mainScreen) : RecyclerView.Adapter<childAdapter.MyViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(childData: childData)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val child: childData = childList[position]
        holder.nameA.text = child.firstName
        holder.nameB.text = child.lastName
        holder.nameAge.text = "${child.age} Years Old"

        holder.itemView.setOnClickListener {
            listener.onItemClick(child)
        }
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
