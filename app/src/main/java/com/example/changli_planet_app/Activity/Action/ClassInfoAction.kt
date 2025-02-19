package com.example.changli_planet_app.Activity.Action

import android.content.Context

sealed class ClassInfoAction {
    object initilaize : ClassInfoAction()
    class UpdateWeek(val week: String) : ClassInfoAction()
    class UpdateDay(val day: String) : ClassInfoAction()
    class UpdateRegion(val region: String) : ClassInfoAction()
    class UpdateStartAndEnd(val start: String, val end: String) : ClassInfoAction()

    class QueryEmptyClassInfo(val context: Context, val term: String) : ClassInfoAction()
}