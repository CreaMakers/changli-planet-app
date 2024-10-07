package com.example.changli_planet_app.Network.Response

data class Comment(
    val comment_id: Int,
    val content: String,
    val create_time: String,
    val replies: List<Reply>,
    val user_id: Int
)