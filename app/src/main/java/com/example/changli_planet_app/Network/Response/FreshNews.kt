package com.example.changli_planet_app.Network.Response

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
    val authorName: String,
    val authorAvatar: String,
    val title: String,
    val content: String,
    val images: List<String?>,
    val tags: List<String>,
    val liked: Int,
    val comments: Int,
    val createTime: String,
    val allowComments: Int,
    val favoritesCount: Int
)

data class FreshNews_Publish(
    var user_id: Int=-1,
    var title: String="",
    var content: String="",
    var tags: String="",
    var allow_comments: Int=1
)


data class FreshNewsResponse(
    val code: String,
    val msg: String,
    val data: List<FreshNews>?
)
