package com.creamaker.changli_planet_app.feature.common.data.remote.dto

import androidx.annotation.Keep

@Keep
data class EmptyClassroomResponse(
    val code: String,
    val msg: String,
    val data: List<String>
)