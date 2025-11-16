package com.creamaker.changli_planet_app.freshNews.ui.adapter.vh

import android.content.Context
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.creamaker.changli_planet_app.freshNews.ui.adapter.ImageAdapter
import com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model.FreshNewsItem
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.utils.GlideUtils
import com.creamaker.changli_planet_app.databinding.FreshNewsItemBinding
import com.gradle.scan.plugin.internal.dep.com.esotericsoftware.kryo.kryo5.util.Util.pos

class FreshNewsItemViewHolder(
    val binding: FreshNewsItemBinding,
    val context: Context,
    private val onImageClick: (List<String?>, Int) -> Unit,
    private val onUserClick: (userId: Int) -> Unit,
    private val onNewsDetailClick: (FreshNewsItem) -> Unit,
    private val onLikeClick: (Int) -> Unit,
    private val onCommentClick: (FreshNewsItem) -> Unit,
    private val onCollectClick: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    private val TAG = "FreshNewsItemViewHolder"

    fun updateAccountAndAvatar(account: String, avatarUrl: String) {
        with(binding) {
            GlideUtils.load(context, newsItemAvatar, avatarUrl)
            newsItemUsername.text = account
        }
    }

    fun updateIsLike(liked: Int, isLiked: Boolean) {
//        Log.d(TAG,"invoked")
        with(binding) {
            if (isLiked) {
                newsFavor.setImageResource(R.drawable.ic_news_liked)
                newsFavor.imageTintList = null
            } else {
                newsFavor.setImageResource(R.drawable.ic_like)
                newsFavor.imageTintList =
                context.getColorStateList(R.color.color_icon_secondary)
            }
            newsFavorCount.text = liked.toString()
        }
    }

    fun updateIsFavorited(favouritesCount: Int, isFavorited: Boolean) {
        with(binding) {
            if (isFavorited) {
                newsShare.setImageResource(R.drawable.ic_collect)
                newsShare.imageTintList = null
            } else {
                newsShare.setImageResource(R.drawable.ic_un_collect)
                newsShare.imageTintList =
                    context.getColorStateList(R.color.color_icon_secondary)
            }
            newsShareCount.text = favouritesCount.toString()
        }
    }

    fun bind(news: FreshNewsItem) {
        Log.d(TAG, "Binding news item: ${news}")
        with(binding) {

            GlideUtils.load(context, newsItemAvatar, news.authorAvatar)
            newsItemUsername.text = news.authorName
            newsTitle.text = news.title
            val time = news.createTime.replace("T", "   ").replace("Z", " ")
            newsItemTime.text = "$time"
            newsContent.text = news.content
            Log.d("wsc","images:"+"${news.images} ")
            imagesRecyclerView.adapter = ImageAdapter(
                news.images
            ) { imageUrl, position -> onImageClick(news.images, position) }

            newsItemLocation.text = news.location ?: "未知"
            newsFavorCount.text = news.liked.toString()
            newsCommentCount.text = (news.comments ?: 0).toString()
            newsShareCount.text = (news.favoritesCount ?: 0).toString()

            if (news.images.isEmpty()) {
                imagesRecyclerView.visibility = View.GONE
//                Log.d("wsc","No images for news id: ${news.freshNewsId}")
            } else {
                imagesRecyclerView.visibility = View.VISIBLE
//                Log.d("wsc","Images found for news id: ${news.freshNewsId}, count: ${news.images.size}")
            }

            newsItemAvatar.setOnClickListener { onUserClick(news.userId) }
            newsItemUsername.setOnClickListener { onUserClick(news.userId) }

            val contentClickArea = listOf(newsTitle, newsContent)
            contentClickArea.forEach { view ->
                view.setOnClickListener { onNewsDetailClick(news) }
            }

            if (news.isLiked) {
                newsFavor.setImageResource(R.drawable.ic_news_liked)
                newsFavor.imageTintList =
                    context.getColorStateList(R.color.color_base_red)
            } else {
                newsFavor.setImageResource(R.drawable.ic_like)
                newsFavor.imageTintList =
                    context.getColorStateList(R.color.color_icon_secondary)
            }

            if (news.isFavorited) {
                newsShare.setImageResource(R.drawable.ic_collect)
                newsShare.imageTintList = null
            } else {
                newsShare.setImageResource(R.drawable.ic_un_collect)
                newsShare.imageTintList =
                    context.getColorStateList(R.color.color_icon_secondary)
            }

            favorContainer.setOnClickListener { onLikeClick(news.freshNewsId) }
            commentContainer.setOnClickListener { onCommentClick(news) }
            shareContainer.setOnClickListener { onCollectClick(news.freshNewsId) }
        }
    }
}