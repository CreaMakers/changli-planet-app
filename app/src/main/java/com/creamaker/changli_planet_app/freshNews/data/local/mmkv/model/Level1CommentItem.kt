package com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model

data class Level1CommentItem(
    val freshNewsId: Int,
    val commentId: Int,
    val liked:Int,
    val userAvatar: String,
    val userName: String,
    val createTime: String,
    val userIp: String,
    val content: String,
    val level2CommentsCount: Int = 0,
    val userId: Int,
    val isLiked: Boolean = false,
)
