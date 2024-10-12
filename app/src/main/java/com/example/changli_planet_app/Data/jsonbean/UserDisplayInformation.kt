package com.example.changli_planet_app.Data.jsonbean

data class UserDisplayInformation(
    val avatar_url: String,
    val bio: String,
    val birthdate: String,
    val gender: Int,
    val grade: Int,
    val location: String,
    val user_level: Int,
    val website: String
)