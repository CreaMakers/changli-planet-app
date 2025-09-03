package com.example.changli_planet_app.Feature.common.state

import com.example.changli_planet_app.Network.Response.ExamArrangement

data class ExamInquiryState(
    var showDataChosen: Boolean = false,
    var dataText: String = "",
    var exams: List<ExamArrangement> = listOf()
)