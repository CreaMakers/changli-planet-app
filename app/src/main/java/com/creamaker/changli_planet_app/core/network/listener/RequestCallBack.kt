package com.creamaker.changli_planet_app.core.network.listener

import okhttp3.Response

interface RequestCallback {
    fun onSuccess(response: Response)
    fun onFailure(error: String)
}
