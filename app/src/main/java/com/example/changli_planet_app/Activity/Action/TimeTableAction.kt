package com.example.changli_planet_app.Activity.Action

import android.content.Context
import com.example.changli_planet_app.Data.jsonbean.GetCourse
import com.example.changli_planet_app.Cache.Room.entity.MySubject

sealed class TimeTableAction {
    data class FetchCourses(val context: Context, val getCourse: GetCourse,val refresh:()->Unit) : TimeTableAction()
    data class UpdateCourses(val subjects: MutableList<MySubject>) : TimeTableAction()
    data class AddCourse(val subject: MySubject) : TimeTableAction()
    data class selectWeek(val weekInfo: String) : TimeTableAction()
    data class selectTerm(val context: Context,val stuNum: String, val password: String, val term: String,val refresh: () -> Unit) :
        TimeTableAction()

    data class DeleteCourse(
        val day: Int,
        val start: Int,
        val curDisplayWeek: Int,
        val term: String
    ) : TimeTableAction()
//    data class getStartTime(val data: String, val stuNum: String, val password: String) : TimeTableAction()
}