package com.example.changli_planet_app.Network.Response

data class TransferGroupOwner(
    val group_id: Int,
    val new_owner_id: Int,
    val old_owner_id: Int
)