package com.creamaker.changli_planet_app.core.network

data class MyResponse<T>(val code: String, val data: T, val msg: String)