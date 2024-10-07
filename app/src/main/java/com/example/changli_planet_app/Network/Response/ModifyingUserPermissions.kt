package com.example.changli_planet_app.Network.Response

data class ModifyingUserPermissions(
    val group_id: Int,
    val role: Int,
    val user_id: Int
)