package com.example.changli_planet_app.Network.Response

data class Group(
    val avatar_url: String,
    val background_url: String,
    val description: String,
    val group_id: Int,
    val group_name: String,
    val group_type: String,
    val joined_time: String,
    val member_count: Int,
    val member_limit: Int,
    val role: Int
)