package com.example.changli_planet_app.Network.Response

data class GroupPostDetails(
    val category: Int,
    val coin_count: Int,
    val content: String,
    val create_time: String,
    val description: String,
    val group_id: Int,
    val is_pinned: Int,
    val is_deleted: Int,
    val post_id: Int,
    val title: String,
    val update_time: String,
    val user_id: Int,
    val view_count: Int
)