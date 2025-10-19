package com.creamaker.changli_planet_app.feature.timetable.ui.entity

import com.creamaker.changli_planet_app.feature.common.data.local.entity.TimeTableMySubject


data class TimeTableUiState(
    val subjects: MutableList<TimeTableMySubject> = mutableListOf(),
    val term: String = "",
    val weekInfo: String = "第1周",
    val lastUpdate: Long = 0L
)
