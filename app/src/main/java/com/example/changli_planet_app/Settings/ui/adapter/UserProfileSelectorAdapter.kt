package com.example.changli_planet_app.Settings.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Common.store.UserStore
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.Event.SelectEvent
import com.example.changli_planet_app.Utils.EventBusHelper

class UserProfileSelectorAdapter(
    private val context: Context,
    val list: List<String>,
    val store: UserStore,
    val changeGender: (String) -> Unit,
    val changeGrade: (String) -> Unit
) :
    RecyclerView.Adapter<UserProfileSelectorAdapter.UserProfileViewHodler>() {
    class UserProfileViewHodler(item: View) : RecyclerView.ViewHolder(item) {
        val selec: TextView = item.findViewById(R.id.select)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProfileViewHodler {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selector, parent, false)
        return UserProfileViewHodler(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: UserProfileViewHodler, position: Int) {

        holder.selec.text = list[position]
        holder.selec.setOnClickListener {
            // 性别选择
            if (list.size == 3) {
                changeGender(list[position])
                EventBusHelper.post(SelectEvent(1))
            } else {
                // 年级选择
                changeGrade(list[position])
                EventBusHelper.post(SelectEvent(1))
            }
        }
    }
}