package com.example.changli_planet_app.Network.Response

import com.google.gson.annotations.SerializedName

data class ExamArrangement (
    val id: String,
    val place: String,
    val examId: String,
    val CourseId: String,
    val name: String,
    val teacher: String,
    val time: String,
    val room: String,
    @SerializedName("courseId")
    val courseId2: String,
)

data class ExamArrangementResponse (
    val code: String,
    val msg: String,
    val data: List<ExamArrangement>
)