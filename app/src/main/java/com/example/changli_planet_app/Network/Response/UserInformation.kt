package com.example.changli_planet_app.Network.Response

data class UserInformation(val code: String, val `data`: Data, val msg: String){
    data class Data(
        val avatar_url: String, val bio: String, val birthdate: String,
        val create_time: String, val description: String, val gender: Int,
        val grade: Int, val is_deleted: Int, val location: String,
        val update_time: String, val user_id: Int, val user_level: Int, val website: String)
}