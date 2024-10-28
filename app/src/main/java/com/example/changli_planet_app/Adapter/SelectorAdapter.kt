package com.example.changli_planet_app.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Util.Event.SelectEvent
import com.example.changli_planet_app.Util.EventBusHelper

class SelectorAdapter(private val list: List<String>):RecyclerView.Adapter<SelectorAdapter.SelectorViewHodler>() {
    class SelectorViewHodler(item:View):ViewHolder(item){
        val selec : TextView = item.findViewById(R.id.select)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectorViewHodler {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selector,parent,false)
        return SelectorViewHodler(view)
    }
    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SelectorViewHodler, position: Int) {
        holder.selec.text = list[position]
        holder.selec.setOnClickListener {
            EventBusHelper.post(SelectEvent(holder.selec.text.toString(),1))
        }
    }
}