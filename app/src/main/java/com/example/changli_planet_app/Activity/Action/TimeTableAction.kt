package com.example.changli_planet_app.Activity.Action

import android.content.Context
import com.example.changli_planet_app.Data.jsonbean.GetCourse
import com.example.changli_planet_app.MySubject

sealed class TimeTableAction {
    data class FetchCourses(val getCourse: GetCourse , val context: Context) : TimeTableAction()
    data class UpdateCourses(val subjects: MutableList<MySubject>) : TimeTableAction()
    data class AddCourse(val subject: MySubject) : TimeTableAction()
    data class selectWeek(val weekInfo: String) : TimeTableAction()
    data class selectTerm(val term: String) : TimeTableAction()
}