package com.example.changli_planet_app.Activity.Action

import android.content.Context
import com.example.changli_planet_app.Data.jsonbean.GetCourse
import com.example.changli_planet_app.Cache.Room.MySubject

sealed class TimeTableAction {
    data class FetchCourses(val getCourse: GetCourse) : TimeTableAction()
    data class UpdateCourses(val subjects: MutableList<MySubject>) : TimeTableAction()
    data class AddCourse(val subject: MySubject) : TimeTableAction()
    data class selectWeek(val weekInfo: String) : TimeTableAction()
    data class selectTerm(val term: String) : TimeTableAction()
    data class DeleteCourse(val day: Int, val start: Int, val curDisplayWeek: Int) : TimeTableAction()
}