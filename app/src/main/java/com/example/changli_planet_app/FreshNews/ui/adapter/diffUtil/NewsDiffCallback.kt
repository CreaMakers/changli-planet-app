package com.example.changli_planet_app.FreshNews.ui.adapter.diffUtil

import androidx.recyclerview.widget.DiffUtil
import com.example.changli_planet_app.Network.Response.FreshNewsItem

class NewsDiffCallback(
    private val oldList: List<FreshNewsItem>,
    private val newList: List<FreshNewsItem>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return newList[newItemPosition].freshNewsId == oldList[oldItemPosition].freshNewsId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}