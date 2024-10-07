package com.example.changli_planet_app.Network.Response

data class PublishedPosts(
    val category: Int,
    val content: String,
    val create_time: String,
    val description: String,
    val group_id: Int,
    val post_id: Int,
    val title: String,
    val update_time: String
)