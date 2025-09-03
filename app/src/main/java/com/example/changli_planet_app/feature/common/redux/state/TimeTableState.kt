package com.example.changli_planet_app.feature.common.redux.state

import com.example.changli_planet_app.feature.common.data.local.entity.TimeTableMySubject

data class TimeTableState(
    var lastUpdate: Long = 0,
    var subjects: MutableList<TimeTableMySubject> = mutableListOf(),
    var weekInfo: String = " ",
    var term: String = " ",
    val stuNum : String = " ",
    val password : String = " ",
//    var startTime :String = " "
)