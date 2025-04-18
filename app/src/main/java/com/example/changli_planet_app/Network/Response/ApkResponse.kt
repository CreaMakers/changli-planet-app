package com.example.changli_planet_app.Network.Response

data class ApkInfo(
    val versionCode: Int,
    var versionName: String,
    val downloadUrl: String,
    val updateMessage: String,
    val createdAt: String
)

data class ApkResponse(
    val code: String,
    val msg: String,
    val data: ApkInfo?
)