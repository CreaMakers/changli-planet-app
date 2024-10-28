package com.example.changli_planet_app.Network
interface RequestCallback {
    fun onSuccess(response: String)
    fun onFailure(error: String)
}
