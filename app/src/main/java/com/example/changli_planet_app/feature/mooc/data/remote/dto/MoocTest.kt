package com.example.changli_planet_app.feature.mooc.data.remote.dto

data class MoocTest(
    val title: String,
    val startTime: String,
    val endTime: String,
    val allowRetake: Int?,
    val timeLimit: Int,
    val isSubmitted: Boolean
)