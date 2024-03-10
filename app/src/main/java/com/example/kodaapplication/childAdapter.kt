package com.example.kodaapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class childAdapter(private val childList : ArrayList<childData>) : RecyclerView.Adapter<childAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): childAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val child : childData = childList[position]
        holder.firstName.text = child.childFirstName
        holder.lastName.text = child.childLastName
        holder.age.text = child.childAge.toString()
    }
    override fun getItemCount(): Int {
        return childList.size
    }
    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val firstName: TextView = itemView.findViewById(R.id.CFName)
        val lastName: TextView = itemView.findViewById(R.id.CLName)
        val age: TextView = itemView.findViewById(R.id.CHAge)
    }

}