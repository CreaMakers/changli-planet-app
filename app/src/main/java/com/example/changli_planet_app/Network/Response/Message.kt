package com.example.changli_planet_app.Network.Response

data class Message(
    val create_time: String,
    val file_type: Any,
    val file_url: Any,
    val message_content: String,
    val message_id: Int,
    val receiver_avatar_url: String,
    val receiver_id: Int,
    val receiver_nickname: String,
    val sender_avatar_url: String,
    val sender_id: Int,
    val sender_nickname: String
)