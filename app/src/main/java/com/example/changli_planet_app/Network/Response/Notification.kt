package com.example.changli_planet_app.Network.Response

data class Notification(
    val content: String,
    val description: String,
    val is_deleted: Int,
    val is_read: Int,
    val notification_id: Int,
    val notification_type: Int,
    val receiver_id: Int,
    val send_time: String,
    val sender_id: Int
)