package com.creamaker.changli_planet_app.freshNews.data.local.mmkv.model

data class Level2CommentItem(
    val parentCommentId: Int,
    val freshNewsId: Int,
    val liked:Int,
    val userAvatar: String,
    val userName: String,
    val createTime: String,
    val userIp: String,
    val content: String,
    val userId: String,
    val commentId: String,
    val isLiked: Boolean = false,
)
