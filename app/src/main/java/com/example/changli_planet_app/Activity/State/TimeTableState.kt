package com.example.changli_planet_app.Activity.State

import com.example.changli_planet_app.MySubject

data class TimeTableState(var lastUpdate: Long = 0, var subjects: MutableList<MySubject>  = mutableListOf(), var week : Int = 0, var term : String = " ")