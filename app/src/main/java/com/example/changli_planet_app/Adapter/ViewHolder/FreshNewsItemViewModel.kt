package com.example.changli_planet_app.Adapter.ViewHolder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Adapter.ImageAdapter
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.Utils.GlideUtils
import com.example.changli_planet_app.databinding.FreshNewsItemBinding

class FreshNewsItemViewModel(
    val binding: FreshNewsItemBinding,
    val context: Context,
    private val onImageClick: (String) -> Unit,
    private val onNewsClick: (userId: Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(news: FreshNewsItem) {
        with(binding) {
            itemView.setOnClickListener {
                onNewsClick(news.userId)
            }
            GlideUtils.load(context, newsItemAvatar, news.authorAvatar)
            newsItemUsername.text = news.authorName
            newsTitle.text = news.title
            val time = news.createTime.replace("T", "   ").replace("Z", " ")
            newsItemTime.text = "发布时间: $time"
            newsContent.text = news.content
            imagesRecyclerView.adapter = ImageAdapter(news.images, onImageClick)
        }
    }

    fun updateAccountAndAvatar(account: String, avatarUrl: String) {
        with(binding) {
            GlideUtils.load(context, newsItemAvatar, avatarUrl)
            newsItemUsername.text = account
        }
    }
}