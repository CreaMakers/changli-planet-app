package com.example.changli_planet_app.feature.timetable.action

import android.content.Context
import com.example.changli_planet_app.feature.common.data.local.entity.TimeTableMySubject
import com.example.changli_planet_app.feature.common.data.remote.dto.GetCourse

/**
 * 课表功能Action
 */
sealed class TimeTableAction {
    data class FetchCourses(val context: Context, val getCourse: GetCourse, val refresh:()->Unit, val refreshSuccess: (() -> Unit)?=null) : TimeTableAction()
    data class UpdateCourses(val subjects: MutableList<TimeTableMySubject>) : TimeTableAction()
    data class AddCourse(val subject: TimeTableMySubject) : TimeTableAction()
    data class selectWeek(val weekInfo: String) : TimeTableAction()
    data class selectTerm(val context: Context, val stuNum: String, val password: String, val term: String, val refresh: () -> Unit) :
        TimeTableAction()

    data class DeleteCourse(
        val day: Int,
        val start: Int,
        val curDisplayWeek: Int,
        val term: String
    ) : TimeTableAction()
//    data class getStartTime(val data: String, val stuNum: String, val password: String) : TimeTableAction()
}