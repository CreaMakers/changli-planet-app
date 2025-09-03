package com.example.changli_planet_app.Feature.common.store

import android.os.Handler
import android.os.Looper
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Feature.common.action.ExamInquiryAction
import com.example.changli_planet_app.Feature.common.state.ExamInquiryState
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.ExamArrangementResponse
import com.example.changli_planet_app.Widget.Dialog.ErrorStuPasswordResponseDialog
import com.example.changli_planet_app.Widget.Dialog.NormalResponseDialog
import com.example.changli_planet_app.Widget.View.CustomToast
import okhttp3.Response

class ExamArrangementStore : Store<ExamInquiryState, ExamInquiryAction>() {
    var currentState = ExamInquiryState()
    val handler = Handler(Looper.getMainLooper())
    override fun handleEvent(action: ExamInquiryAction) {
        currentState = when (action) {
            ExamInquiryAction.initilaize -> {
                currentState.exams = emptyList()
                _state.onNext(currentState)
                currentState
            }
            is ExamInquiryAction.UpdateExamData -> {
                updateExamDate(action)
                currentState
            }
        }
    }

    private fun updateExamDate(action: ExamInquiryAction.UpdateExamData){
        val httpUrlHelper = HttpUrlHelper.HttpRequest()
            .get(PlanetApplication.ToolIp + "/exams")
            .addQueryParam("stuNum", action.studentId)
            .addQueryParam("password", action.password)
            .addQueryParam("term", action.termTime)
            .addQueryParam("examType", "")
            .build()
        OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
            override fun onSuccess(response: Response) {
                val examArrangementResponse = OkHttpHelper.gson.fromJson(
                    response.body?.string(),
                    ExamArrangementResponse::class.java
                )
                currentState.exams = when (examArrangementResponse.code) {
                    "200" -> {
                        handler.post{
                            CustomToast.showMessage(PlanetApplication.appContext,"刷新成功")
                        }
                        examArrangementResponse.data
                    }
                    "403" -> {
                        handler.post {
                            ErrorStuPasswordResponseDialog(
                                action.context,
                                "学号或密码错误ʕ⸝⸝⸝˙Ⱉ˙ʔ",
                                "查询失败",
                                action.refresh
                                ).show()
                        }
                        emptyList()
                    }
                    "404" -> {
                        handler.post {
                            NormalResponseDialog(
                                action.context,
                                "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡",
                                "查询失败"
                            ).show()
                        }
                        emptyList()
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