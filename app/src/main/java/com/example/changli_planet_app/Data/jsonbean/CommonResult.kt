package com.example.changli_planet_app.Data.jsonbean

data class CommonResult<T>(
    val data: T?,
    val code: String,
    val msg: String,
)