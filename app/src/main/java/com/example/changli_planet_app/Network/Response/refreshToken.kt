package com.example.changli_planet_app.Network.Response

data class refreshToken(val code: String, val data: Data, val msg: String){
    data class Data(val access_token: String, val expires_in: String, val refresh_token: String)
}
