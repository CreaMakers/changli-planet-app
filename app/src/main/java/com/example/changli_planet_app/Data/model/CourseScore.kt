package com.example.changli_planet_app.Data.model


data class CourseScore(
    val name: String,
    val score: Int,
    val credit: Double,
    val earnedCredit: Double,
    val courseType: String,
    val pscj: String? = null,
    val pscjBL: String? = null,
    val qmcj: String? = null,
    val qmcjBL: String? = null,
    val qzcj: String? = null,
    val qzcjBL: String? = null,
    val sjcj: String? = null,
    val sjcjBL: String? = null,
)