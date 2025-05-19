package com.example.changli_planet_app.Data.model

data class CommentDetailBean(
    val nickName: String,
    val content: String,
    val createDate: String,
    val userAvatarResId: Int,
    var isLiked: Boolean = false,
    val replyList: List<ReplyBean>? = null
)

data class ReplyBean(
    val nickName: String,
    val content: String
)