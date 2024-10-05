package com.example.changli_planet_app.Network.Response

data class MyResponse<T>(val code: String, val data: T, val msg: String)
