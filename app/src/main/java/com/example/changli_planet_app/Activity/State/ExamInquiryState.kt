package com.example.changli_planet_app.Activity.State

import com.example.changli_planet_app.Data.jsonbean.Exam
import com.example.changli_planet_app.Network.Response.ExamArrangement

data class ExamInquiryState(
    var showDataChosen: Boolean = false,
    var dataText: String = "",
    var exams: List<ExamArrangement> = listOf()
)