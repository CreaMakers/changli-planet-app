package com.example.changli_planet_app.Network.Response

data class DynamicInformation(
    val article_count: Int,
    val coin_count: Int,
    val comment_count: Int,
    val create_time: String,
    val description: String,
    val is_deleted: Int,
    val last_login_time: String,
    val liked_count: Int,
    val quiz_type: Int,
    val statement_count: Int,
    val student_number: String,
    val update_time: String,
    val user_id: Int,
    val xp: Int
)