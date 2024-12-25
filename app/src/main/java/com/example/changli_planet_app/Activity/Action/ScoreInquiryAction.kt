package com.example.changli_planet_app.Activity.Action

import android.content.Context
import com.example.changli_planet_app.Activity.State.ScoreInquiryState
import com.google.android.material.color.utilities.Score

sealed class ScoreInquiryAction {
    object ShowData : ScoreInquiryAction()
    object initilaize : ScoreInquiryAction()
    data class UpdateGrade(val studentId: String, val password: String, val term: String) :
        ScoreInquiryAction()
}