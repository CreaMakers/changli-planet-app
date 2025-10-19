package com.creamaker.changli_planet_app.feature.common.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.feature.timetable.viewmodel.TimeTableViewModel
import com.creamaker.changli_planet_app.utils.Event.SelectEvent
import com.creamaker.changli_planet_app.utils.EventBusHelper

class TimeTableSelectorAdapter(
    private val context: Context,
    private val stuNum: String,
    private val stuPassword: String,
    val list: List<String>,
    val vm: TimeTableViewModel,
    val refresh:()->Unit
) :
    RecyclerView.Adapter<TimeTableSelectorAdapter.TimeTableViewHodler>() {
    class TimeTableViewHodler(item: View) : RecyclerView.ViewHolder(item) {
        val selec: TextView = item.findViewById(R.id.select)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeTableViewHodler {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selector, parent, false)
        return TimeTableViewHodler(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: TimeTableViewHodler, position: Int) {

        holder.selec.text = list[position]
        holder.selec.setOnClickListener {
            vm.selectTerm(list[position])
            EventBusHelper.post(SelectEvent(1))
        }
    }
}