package com.example.changli_planet_app.Data.model

data class SemesterGroup(
    val semesterName: String,
    val gpa: Double,
    val cours: List<CourseScore>
)
