package com.creamaker.changli_planet_app.feature.common.redux.action

import android.content.Context

/**
 * 成绩查询Action
 */
sealed class ScoreInquiryAction {
    object ShowData : ScoreInquiryAction()
    object initilaize : ScoreInquiryAction()
    data class UpdateGrade(val context: Context, val studentId: String, val password: String, val refresh:()->Unit) :
        ScoreInquiryAction()

    data class GetScoreDetail(val context: Context, val pscjUrl: String, val courseName: String) :
        ScoreInquiryAction()
}