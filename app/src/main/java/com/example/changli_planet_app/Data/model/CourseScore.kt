package com.example.changli_planet_app.Data.model


data class CourseScore(
    val name: String,
    val score: Int,
    val credit: Double,
    val earnedCredit: Double,
    val courseType: String,
    val pscjUrl: String?,
    val cookie: String?
)