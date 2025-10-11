package com.creamaker.changli_planet_app.feature.common.redux.state

import com.dcelysia.csust_spider.education.data.remote.model.ExamArrange


data class ExamInquiryState(
    var showDataChosen: Boolean = false,
    var dataText: String = "",
    var exams: List<ExamArrange> = listOf()
)