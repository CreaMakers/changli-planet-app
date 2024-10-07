package com.example.changli_planet_app.Network.Response

data class ReadMessage(
    val group_id: Int,
    val read_message_ids: List<Int>,
    val user_id: Int
)