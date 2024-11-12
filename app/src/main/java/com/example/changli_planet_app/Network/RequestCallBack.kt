package com.example.changli_planet_app.Network

import okhttp3.Response

interface RequestCallback {
    fun onSuccess(response: Response)
    fun onFailure(error: String)
}
