package com.creamaker.changli_planet_app.feature.common.ui.adapter.model

data class SemesterGroup(
    val semesterName: String, //开课学期
    val gpa: Double, //学期GPA绩点
    val course: List<CourseScore> //课程成绩
)