package com.example.changli_planet_app.Network.Response

import com.example.changli_planet_app.Adapter.ViewHolder.FreshNewsItemViewHolder
import java.lang.ref.WeakReference

data class FreshNews(
    val freshNewsId: Int,
    val userId: Int,
    val title: String,
    val content: String,
    val images: List<String>,
    val tags: List<String>,
    val liked: Int,
    val comments: Int,
    val createTime: String,
    val updateTime: String,
    val isDeleted: Int,
    val allowComments: Int,
    val favoritesCount: Int
)

data class FreshNewsItem(
    val freshNewsId: Int,
    val userId: Int,
    var authorName: String,
    var authorAvatar: String,
    val title: String,
    val content: String,
    val images: List<String?>,
    val tags: List<String>,
    var liked: Int,
    val comments: Int,
    val createTime: String,
    val allowComments: Int,
    val favoritesCount: Int,

    @Transient
    val location: String,
    @Transient
    var isLiked: Boolean = false,
    @Transient
    var isFavorited: Boolean = false
)

data class FreshNews_Publish(
    var user_id: Int = -1,
    var title: String = "",
    var content: String = "",
    var tags: String = "",
    var allow_comments: Int = 1,
    var address: String = "未知"
)


data class FreshNewsResponse(
    val code: String,
    val msg: String,
    val data: List<FreshNews>?
)
