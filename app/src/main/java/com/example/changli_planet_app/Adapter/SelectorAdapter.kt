package com.example.changli_planet_app.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.changli_planet_app.Activity.Action.ElectronicAction
import com.example.changli_planet_app.Activity.Store.ElectronicStore
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Util.Event.SelectEvent
import com.example.changli_planet_app.Util.EventBusHelper

class SelectorAdapter(private val list: List<String>,val store: ElectronicStore):RecyclerView.Adapter<SelectorAdapter.SelectorViewHodler>() {
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
            if (list.size == 2) {
                store.dispatch(ElectronicAction.selectAddress(holder.selec.text.toString()))
                EventBusHelper.post(SelectEvent(1))
            } else {
                store.dispatch(ElectronicAction.selectBuild(holder.selec.text.toString()))
                EventBusHelper.post(SelectEvent(1))
            }
        }
    }
}