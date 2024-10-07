package com.example.changli_planet_app.Network.Response
//给一个帖子发送评论
data class PostComment(
    val comment_id: Int,
    val content: String,
    val create_time: String,
    val parent_comment_id: Any,
    val post_id: Int,
    val user_id: Int
)