package com.creamaker.changli_planet_app.freshNews.ui.adapter.diffUtil

import androidx.recyclerview.widget.DiffUtil
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem

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