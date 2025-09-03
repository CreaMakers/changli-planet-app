package com.example.changli_planet_app.Feature.common.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.changli_planet_app.R

class EmptyClassroomAdapter(
    val list: List<String>,
) : RecyclerView.Adapter<EmptyClassroomAdapter.EmptyClassroomHolder>() {

    class EmptyClassroomHolder(item: View) : ViewHolder(item) {
        val select: TextView = item.findViewById(R.id.select)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmptyClassroomHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selector, parent, false)
        return EmptyClassroomHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: EmptyClassroomHolder, position: Int) {
        holder.select.text = list[position]
    }
}