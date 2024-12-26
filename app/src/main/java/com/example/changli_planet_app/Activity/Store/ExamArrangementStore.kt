package com.example.changli_planet_app.Activity.Store

import com.example.changli_planet_app.Activity.Action.ExamInquiryAction
import com.example.changli_planet_app.Activity.State.ExamInquiryState
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.ExamArrangementResponse
import okhttp3.Response

class ExamArrangementStore : Store<ExamInquiryState, ExamInquiryAction>() {
    var currentState = ExamInquiryState()
    override fun handleEvent(action: ExamInquiryAction) {
        currentState = when (action) {
            ExamInquiryAction.initilaize -> {
                currentState.exams = emptyList()
                _state.onNext(currentState)
                currentState
            }
            is ExamInquiryAction.UpdateExamData -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.ToolIp + "/exams")
                    .addQueryParam("stuNum", action.studentId)
                    .addQueryParam("password", action.password)
                    .addQueryParam("term", action.termTime)
                    .addQueryParam("examType", "期末")
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        var examArrangementResponse = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            ExamArrangementResponse::class.java
                        )
                        currentState.exams = when (examArrangementResponse.code) {
                            "200" -> {
                                examArrangementResponse.data
                            }
                            else -> {
                                emptyList()
                            }
                        }
                        _state.onNext(currentState)
                    }

                    override fun onFailure(error: String) {
                        currentState.exams = emptyList()
                        _state.onNext(currentState)
                    }

                })
                currentState
            }
        }
    }
}