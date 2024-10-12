package com.example.changli_planet_app.Network.Response

data class Member(
    val avatar_url: String,
    val bio: String,
    val is_muted: Int,
    val joined_time: String,
    val mute_duration: Int,
    val mute_start_time: String,
    val nickname: String,
    val role: Int,
    val user_id: Int
)