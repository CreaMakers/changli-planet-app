package com.creamaker.changli_planet_app.feature.common.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R

class SelectorAdapter(
    private val list: List<String>,
    private val onItemSelected: (String) -> Unit
) : RecyclerView.Adapter<SelectorAdapter.SelectorViewHodler>() {

    class SelectorViewHodler(item: View) : RecyclerView.ViewHolder(item) {
        val selec: TextView = item.findViewById(R.id.select)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectorViewHodler {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selector, parent, false)
        return SelectorViewHodler(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SelectorViewHodler, position: Int) {
        holder.selec.text = list[position]
        holder.selec.setOnClickListener {
            onItemSelected(list[position])
        }
    }
}