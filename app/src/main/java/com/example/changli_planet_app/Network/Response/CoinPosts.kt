package com.example.changli_planet_app.Network.Response

data class CoinPosts(
    val coin_amount: Int,
    val coin_id: Int,
    val create_time: String,
    val post_id: Int,
    val user_id: Int
)