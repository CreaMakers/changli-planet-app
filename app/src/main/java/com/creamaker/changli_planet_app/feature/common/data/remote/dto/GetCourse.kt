package com.creamaker.changli_planet_app.feature.common.data.remote.dto

import androidx.annotation.Keep

@Keep
data class GetCourse(
    val stuNum : String,
    val password: String,
    val week :String,
    val termId: String
)