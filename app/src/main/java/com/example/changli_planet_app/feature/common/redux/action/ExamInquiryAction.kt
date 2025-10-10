package com.example.changli_planet_app.feature.common.redux.action

import android.content.Context

/**
 * 考试场次查询
 */
sealed class ExamInquiryAction {
    object initilaize: ExamInquiryAction()
    data class UpdateExamData (val context: Context, val termTime: String, val refresh:()->Unit) : ExamInquiryAction()
}