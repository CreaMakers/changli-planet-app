package com.example.changli_planet_app.Activity.Action

sealed class ExamInquiryAction {
    object initilaize: ExamInquiryAction()
    data class UpdateExamData (val termTime: String, val termType: String) : ExamInquiryAction()
}