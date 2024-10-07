package com.example.changli_planet_app.Network.Response

data class ApplyMember(
    val apply_id: Int,
    val apply_message: String,
    val apply_time: String,
    val comments: Any,
    val group_id: Int,
    val processed_by: Any,
    val processed_time: Any,
    val status: Int,
    val user_id: Int
)