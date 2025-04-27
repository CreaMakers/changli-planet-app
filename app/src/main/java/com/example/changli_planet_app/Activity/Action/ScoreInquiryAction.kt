package com.example.changli_planet_app.Activity.Action

import android.content.Context
import com.example.changli_planet_app.Activity.State.ScoreInquiryState
import com.google.android.material.color.utilities.Score
import okhttp3.Cookie

sealed class ScoreInquiryAction {
    object ShowData : ScoreInquiryAction()
    object initilaize : ScoreInquiryAction()
    data class UpdateGrade(val context: Context, val studentId: String, val password: String,val refresh:()->Unit) :
        ScoreInquiryAction()

    data class GetScoreDetail(val context: Context, val pscjUrl: String, val courseName: String) :
        ScoreInquiryAction()
}