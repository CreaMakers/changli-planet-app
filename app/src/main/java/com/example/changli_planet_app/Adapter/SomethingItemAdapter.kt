package com.example.changli_planet_app.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Cache.Room.entity.SomethingItemEntity
import com.example.changli_planet_app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SomethingItemAdapter(val data: List<SomethingItemEntity>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class SomethingItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val allMoney: TextView = view.findViewById(R.id.all_money)
        val dailyCost: TextView = view.findViewById(R.id.daily_cost)
        val days: TextView = view.findViewById(R.id.days)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_something_value, parent, false)
        return SomethingItemViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val somethingItemViewHolder = holder as SomethingItemViewHolder
        val dsf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val lastTime = dsf.parse(data[position].startTime)
        val now = Date()
        val days = (lastTime.time - now.time)/1000/60/60/24;
        somethingItemViewHolder.name.text = data[position].name
        somethingItemViewHolder.allMoney.text = data[position].totalMoney.toString()
        somethingItemViewHolder.dailyCost.text = (data[position].totalMoney/days).toString()
        somethingItemViewHolder.days.text = days.toString()
    }

    override fun getItemCount(): Int = data.size
}