package com.creamaker.changli_planet_app.feature.mooc.data.remote.dto

import androidx.annotation.Keep

@Keep
data class MoocTest(
    val title: String,
    val startTime: String,
    val endTime: String,
    val allowRetake: Int?,
    val timeLimit: Int,
    val isSubmitted: Boolean
)