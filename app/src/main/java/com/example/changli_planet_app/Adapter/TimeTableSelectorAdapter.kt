package com.example.changli_planet_app.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.changli_planet_app.Activity.Action.ElectronicAction
import com.example.changli_planet_app.Activity.Action.TimeTableAction
import com.example.changli_planet_app.Activity.Store.ElectronicStore
import com.example.changli_planet_app.Activity.Store.TimeTableStore
import com.example.changli_planet_app.R
import com.example.changli_planet_app.UI.WheelBottomDialog
import com.example.changli_planet_app.Util.Event.SelectEvent
import com.example.changli_planet_app.Util.EventBusHelper
import org.greenrobot.eventbus.EventBus

class TimeTableSelectorAdapter(private val list: List<String>, val store: TimeTableStore) :
    RecyclerView.Adapter<TimeTableSelectorAdapter.TimeTableViewHodler>() {
    class TimeTableViewHodler(item: View) : ViewHolder(item) {
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
            if (list.size == 20) {
                store.dispatch(TimeTableAction.selectWeek(list[position]))
                EventBusHelper.post(SelectEvent(1))
            } else {
                store.dispatch(TimeTableAction.selectTerm(list[position]))
                EventBusHelper.post(SelectEvent(1))
            }
        }
    }
}