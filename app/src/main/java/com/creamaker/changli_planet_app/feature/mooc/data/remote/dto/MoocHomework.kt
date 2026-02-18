package com.creamaker.changli_planet_app.feature.mooc.data.remote.dto

import androidx.annotation.Keep

@Keep
data class MoocHomework(
    val id: Int,
    val title: String,
    val publisher: String,
    val canSubmit: Boolean,
    val submitStatus: Boolean,
    val deadline: String,
    val startTime: String
)
