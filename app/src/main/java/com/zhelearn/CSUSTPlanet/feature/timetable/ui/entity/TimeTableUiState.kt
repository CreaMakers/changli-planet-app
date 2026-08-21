package com.zhelearn.CSUSTPlanet.feature.timetable.ui.entity

import com.zhelearn.CSUSTPlanet.feature.common.data.local.entity.TimeTableMySubject

data class TimeTableUiState(
    val subjects: MutableList<TimeTableMySubject> = mutableListOf(),
    val term: String = "",
    val weekInfo: String = "第1周",
    val lastUpdate: Long = 0L,
    val remark: List<String> = emptyList()
)
