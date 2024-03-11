package com.example.kodaapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChildAccountsAdapter(private val cList:ArrayList<kidData>) :
    RecyclerView.Adapter<ChildAccountsAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val fName: TextView = itemView.findViewById(R.id.name)
        val sName: TextView = itemView.findViewById(R.id.nameB)
        val aAge: TextView = itemView.findViewById(R.id.ageA)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_b, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return cList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.fName.text = cList[position].aName
        holder.sName.text = cList[position].bName
        holder.aAge.text = cList[position].aAge
    }
}