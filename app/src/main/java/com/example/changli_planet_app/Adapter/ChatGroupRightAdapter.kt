package com.example.changli_planet_app.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Data.jsonbean.ChatGroupItem
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.GlideUtils

class ChatGroupRightAdapter(val data: List<ChatGroupItem>) :
    RecyclerView.Adapter<ChatGroupRightAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chatRightImage: ImageView = view.findViewById(R.id.chat_right_img)
        val chatGroupName: TextView = view.findViewById(R.id.chat_group_name)
        val chatGroupDescription: TextView = view.findViewById(R.id.chat_group_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_group_right_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        GlideUtils.loadWithThumbnail(
            holder.itemView.context,
            holder.chatRightImage,
            data[position].avatarUrl
        )
        holder.chatGroupDescription.text = data[position].description
        holder.chatGroupName.text = data[position].groupName

    }
}