package com.example.changli_planet_app.Adapter.ViewHolder

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Adapter.ImageAdapter
import com.example.changli_planet_app.Network.Response.FreshNewsItem
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.GlideUtils
import com.example.changli_planet_app.databinding.FreshNewsItemBinding

class FreshNewsItemViewHolder(
    val binding: FreshNewsItemBinding,
    val context: Context,
    private val onImageClick: (List<String?>, Int) -> Unit,
    private val onUserClick: (userId: Int) -> Unit,
    private val onNewsDetailClick: (FreshNewsItem) -> Unit,
    private val onLikeClick: (FreshNewsItem) -> Unit,
    private val onCommentClick: (FreshNewsItem) -> Unit,
    private val onCollectClick: (FreshNewsItem) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(news: FreshNewsItem) {
        with(binding) {
            GlideUtils.load(context, newsItemAvatar, news.authorAvatar)
            newsItemUsername.text = news.authorName
            newsTitle.text = news.title
            val time = news.createTime.replace("T", "   ").replace("Z", " ")
            newsItemTime.text = "$time"
            newsContent.text = news.content

            imagesRecyclerView.adapter = ImageAdapter(
                news.images,
                { imageUrl, position -> onImageClick(news.images, position) }
            )

            newsItemLocation.text = news.location ?: "未知"
            newsFavorCount.text = (news.liked ?: 0).toString()
            newsCommentCount.text = (news.comments ?: 0).toString()
            newsShareCount.text = (news.favoritesCount ?: 0).toString()

            if (news.images.isNullOrEmpty()) {
                imagesRecyclerView.visibility = android.view.View.GONE
                middleDivider.visibility = android.view.View.GONE
            } else {
                imagesRecyclerView.visibility = android.view.View.VISIBLE
                middleDivider.visibility = android.view.View.VISIBLE
            }

            //点用户头像和名字，跳转用户信息页
            newsItemAvatar.setOnClickListener { onUserClick(news.userId) }
            newsItemUsername.setOnClickListener { onUserClick(news.userId) }

            //点标题和内容点击进入详情页
            val contentClickArea = listOf(newsTitle, newsContent)
            contentClickArea.forEach { view ->
                view.setOnClickListener { onNewsDetailClick(news) }
            }

            if (news.isLiked) {
                newsFavor.setImageResource(R.drawable.ic_news_liked)
            } else {
                newsFavor.setImageResource(R.drawable.ic_like)
            }

            if (news.isFavorited) {
                newsShare.setImageResource(R.drawable.ic_collect)
            } else {
                newsShare.setImageResource(R.drawable.ic_un_collect)
            }

            //点赞、评论、收藏的点击事件
            favorContainer.setOnClickListener { onLikeClick(news) }
            commentContainer.setOnClickListener { onCommentClick(news) }
            shareContainer.setOnClickListener { onCollectClick(news) }
        }
    }
}