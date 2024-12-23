package com.example.changli_planet_app.Activity.Store

import android.widget.Toast
import com.example.changli_planet_app.Activity.Action.ScoreInquiryAction
import com.example.changli_planet_app.Activity.State.ScoreInquiryState
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.GradeResponse
import com.example.changli_planet_app.Network.Response.MyResponse
import okhttp3.Response

class ScoreInquiryStore : Store<ScoreInquiryState, ScoreInquiryAction>() {
    var currentState = ScoreInquiryState()
    override fun handleEvent(action: ScoreInquiryAction) {
        currentState = when (action) {
            is ScoreInquiryAction.ShowData -> {
                currentState.showDataChosen = !currentState.showDataChosen
                _state.onNext(currentState)
                currentState
            }

            is ScoreInquiryAction.UpdateGrade -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.ToolIp + "/grades")
                    .addQueryParam("stuNum", "202308010135")
                    .addQueryParam("password", "Jianyu@123")
                    .addQueryParam("term", action.term)
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        var gradeResponse = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            GradeResponse::class.java
                        )
                        currentState.grades = when (gradeResponse.code) {
                            "200" -> {
                                gradeResponse.data
                            }
                            else -> {
                                emptyList()
                            }
                        }
                        _state.onNext(currentState)
                    }

                    override fun onFailure(error: String) {
                        currentState.grades = emptyList()
                        _state.onNext(currentState)
                    }

                })
                currentState
            }

            ScoreInquiryAction.initilaize -> {
                currentState.grades = emptyList()
                currentState.showDataChosen = false
                currentState.dataText = "日期"
                _state.onNext(currentState)
                currentState
            }
        }

    }

}