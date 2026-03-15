package com.creamaker.changli_planet_app.feature.common.ui.adapter.model

import androidx.annotation.Keep

@Keep
data class Exam (
    val name: String,
    val time: String,
    val place: String,
    val room: String,
)