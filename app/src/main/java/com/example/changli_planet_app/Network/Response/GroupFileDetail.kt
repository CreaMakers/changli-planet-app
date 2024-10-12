package com.example.changli_planet_app.Network.Response

data class GroupFileDetail(
    val create_time: String,
    val description: String,
    val file_id: Int,
    val file_name: String,
    val file_size: Int,
    val file_type: Int,
    val file_url: String,
    val upload_user_id: Int,
    val upload_user_name: String
)