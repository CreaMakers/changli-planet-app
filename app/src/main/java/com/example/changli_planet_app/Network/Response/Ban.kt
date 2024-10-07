package com.example.changli_planet_app.Network.Response

data class Ban(
    val group_id: Int,
    val is_muted: Int,
    val mute_duration: Int,
    val mute_end_time: String,
    val mute_start_time: String,
    val user_id: Int
)