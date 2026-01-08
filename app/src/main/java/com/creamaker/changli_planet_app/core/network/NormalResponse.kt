package com.creamaker.changli_planet_app.core.network

import androidx.annotation.Keep

@Keep
data class NormalResponse(
    val code: String, val msg: String
)