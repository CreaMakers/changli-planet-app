package com.example.changli_planet_app.Network.Response

data class LikePosts(
    val like_id: Int,
    val like_time: String,
    val post_id: Int,
    val user_id: Int
)