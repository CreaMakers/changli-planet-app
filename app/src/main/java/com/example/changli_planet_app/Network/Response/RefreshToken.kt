package com.example.changli_planet_app.Network.Response

data class RefreshToken(
    val access_token: String,
    val expires_in: String,
    val refresh_token: String)