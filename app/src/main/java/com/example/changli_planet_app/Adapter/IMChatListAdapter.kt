package com.example.changli_planet_app.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.changli_planet_app.Data.jsonbean.ChatListItem
import com.example.changli_planet_app.R

class IMChatListAdapter(val data: List<ChatListItem>) : RecyclerView.Adapter<IMChatListAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupAvatar: ImageView = view.findViewById(R.id.group_avatar)
        val groupName: TextView = view.findViewById(R.id.group_name)
        val lastMessage: TextView = view.findViewById(R.id.last_message)
        val messageCount: TextView = view.findViewById(R.id.message_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.chat_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(data[position].groupAvatar)
            .into(holder.groupAvatar)
        holder.groupName.text = data[position].groupName
        holder.lastMessage.text = data[position].lastMessage
        if(data[position].messageCount != 0) {
            holder.messageCount.text = data[position].messageCount.toString()
        } else {
            holder.messageCount.visibility = View.INVISIBLE
        }
    }

}