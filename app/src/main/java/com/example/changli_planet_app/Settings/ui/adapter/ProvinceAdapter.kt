package com.example.changli_planet_app.Settings.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.R

class ProvinceAdapter(
    private val provinceList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ProvinceAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemLayout = view.findViewById<RelativeLayout>(R.id.location_item_layout)
        val textView = view.findViewById<TextView>(R.id.location_item_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.location_adapter_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = provinceList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val province = provinceList[position]
        holder.textView.text = province
        holder.itemLayout.setOnClickListener { onItemClick(province) }
    }
}