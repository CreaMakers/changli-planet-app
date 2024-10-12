package com.example.changli_planet_app.Network.Response

data class RemoveMember(
    val group_id: Int,
    val removed: Boolean,
    val user_id: Int
)