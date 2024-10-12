package com.example.changli_planet_app.Network.Response

data class ViolationsItem(
    val ban_duration: Int,
    val create_time: String,
    val description: String,
    val id: Int,
    val is_deleted: Int,
    val mute_duration: Int,
    val penalty_reason: String,
    val penalty_status: Int,
    val penalty_time: String,
    val penalty_type: Int,
    val update_time: String,
    val violation_time: String,
    val violation_type: Int
)