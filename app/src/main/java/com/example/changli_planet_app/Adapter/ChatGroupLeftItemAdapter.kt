package com.example.changli_planet_app.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Data.jsonbean.ChatGroupLeftItem
import com.example.changli_planet_app.R

class ChatGroupLeftItemAdapter(val data: List<ChatGroupLeftItem>) : RecyclerView.Adapter<ChatGroupLeftItemAdapter.ViewHolder>() {
    private var selectedPosition = 0

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.chat_left_title)
        val img: ImageView = view.findViewById(R.id.chat_left_img)
        val indicator: View = view.findViewById(R.id.indicator)
        val leftAll: LinearLayout = view.findViewById(R.id.left_all)

        init {
            view.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =  LayoutInflater.from(parent.context).inflate(R.layout.chat_group_left_item, parent, false)

        return ViewHolder(view)

    }
    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = data[position].title
        holder.img.setImageResource(data[position].img)
        if (position == selectedPosition) {
            holder.indicator.visibility = View.VISIBLE
            holder.leftAll.setBackgroundResource(R.drawable.selected_chat_left_item)
        } else {
            holder.indicator.visibility = View.INVISIBLE
            holder.leftAll.setBackgroundResource(R.drawable.selected_chat_left_item_normal)
        }
    }
}