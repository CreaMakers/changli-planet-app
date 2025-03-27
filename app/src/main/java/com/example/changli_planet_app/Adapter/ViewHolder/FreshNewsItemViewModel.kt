package com.example.changli_planet_app.Adapter.ViewHolder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Adapter.ImageAdapter
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.Util.GlideUtils
import com.example.changli_planet_app.databinding.FreshNewsItemBinding

class FreshNewsItemViewModel(
    val binding: FreshNewsItemBinding,
    val context: Context,
    private val onImageClick: (String) -> Unit,
    private val onNewsClick: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(news: FreshNewsItem) {
        with(binding) {
            itemView.setOnClickListener { onNewsClick() }
            GlideUtils.load(context, newsItemAvatar, news.authorAvatar)
            newsItemUsername.text = news.authorName
            newsTitle.text=news.title
            newsItemTime.text = "发布时间: ${news.createTime}"
            newsContent.text = news.content

            imagesRecyclerView.adapter = ImageAdapter(news.images, onImageClick)
        }
    }
}