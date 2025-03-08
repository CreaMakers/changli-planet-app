package com.example.changli_planet_app.Activity.State

import com.example.changli_planet_app.Cache.Room.MySubject

data class TimeTableState(
    var lastUpdate: Long = 0,
    var subjects: MutableList<MySubject> = mutableListOf(),
    var weekInfo: String = " ",
    var term: String = " ",
    val stuNum : String = " ",
    val password : String = " ",
//    var startTime :String = " "
)