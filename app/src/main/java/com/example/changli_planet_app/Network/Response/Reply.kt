package com.example.changli_planet_app.Network.Response

data class Reply(
    val comment_id: Int,
    val content: String,
    val create_time: String,
    val parent_comment_id: Int,
    val user_id: Int
)