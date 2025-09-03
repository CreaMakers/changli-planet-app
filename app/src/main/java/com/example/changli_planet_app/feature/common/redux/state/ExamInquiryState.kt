package com.example.changli_planet_app.feature.common.redux.state

import com.example.changli_planet_app.feature.common.data.local.entity.ExamArrangement

data class ExamInquiryState(
    var showDataChosen: Boolean = false,
    var dataText: String = "",
    var exams: List<ExamArrangement> = listOf()
)