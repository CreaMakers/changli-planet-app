package com.creamaker.changli_planet_app.feature.timetable.ui.compose

import androidx.compose.runtime.Immutable

@Immutable
data class TimeTableDayHeaderUi(
    val weekdayLabel: String,
    val dayOfMonthLabel: String,
    val isToday: Boolean,
)

@Immutable
data class TimeTableCourseUi(
    val id: Int,
    val title: String,
    val teacher: String,
    val room: String,
    val dayOfWeek: Int,
    val startSection: Int,
    val sectionSpan: Int,
    val weeks: Set<Int>,
    val isCustom: Boolean,
)
