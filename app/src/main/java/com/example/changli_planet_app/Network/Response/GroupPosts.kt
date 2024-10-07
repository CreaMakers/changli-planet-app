package com.example.changli_planet_app.Network.Response

data class GroupPosts(
    val category: Int,
    val coin_count: Int,
    val content: String,
    val create_time: String,
    val description: String,
    val is_pinned: Int,
    val post_id: Int,
    val title: String,
    val update_time: String,
    val view_count: Int
)