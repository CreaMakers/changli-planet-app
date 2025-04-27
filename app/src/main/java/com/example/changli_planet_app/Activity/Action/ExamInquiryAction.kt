package com.example.changli_planet_app.Activity.Action

import android.content.Context

sealed class ExamInquiryAction {
    object initilaize: ExamInquiryAction()
    data class UpdateExamData (val context: Context, val studentId: String, val password: String, val termTime: String,val refresh:()->Unit) : ExamInquiryAction()
}