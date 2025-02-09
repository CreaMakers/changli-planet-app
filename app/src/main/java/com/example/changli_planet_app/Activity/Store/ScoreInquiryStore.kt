package com.example.changli_planet_app.Activity.Store

import android.os.Handler
import android.os.Looper
import com.example.changli_planet_app.Activity.Action.ScoreInquiryAction
import com.example.changli_planet_app.Activity.State.ScoreInquiryState
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.GradeResponse
import com.example.changli_planet_app.Widget.Dialog.ErrorStuPasswordResponseDialog
import com.example.changli_planet_app.Widget.Dialog.NormalResponseDialog
import okhttp3.Response

class ScoreInquiryStore : Store<ScoreInquiryState, ScoreInquiryAction>() {
    var currentState = ScoreInquiryState()
    val handler = Handler(Looper.getMainLooper())
    override fun handleEvent(action: ScoreInquiryAction) {
        currentState = when (action) {
            is ScoreInquiryAction.ShowData -> {
                _state.onNext(currentState)
                currentState
            }

            is ScoreInquiryAction.UpdateGrade -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.ToolIp + "/grades")
                    .addQueryParam("stuNum", action.studentId)
                    .addQueryParam("password", action.password)
                    .addQueryParam("term", "")
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        try {
                            val gradeResponse = OkHttpHelper.gson.fromJson(
                                response.body?.string(),
                                GradeResponse::class.java
                            )

                            when (gradeResponse.code) {
                                "200" -> {
                                    currentState.grades = gradeResponse.data
                                    _state.onNext(currentState)
                                }
                                "403" -> {
                                    currentState.grades = emptyList()
                                    handler.post {
                                        try {
                                            ErrorStuPasswordResponseDialog(
                                                action.context,
                                                "学号或密码错误ʕ⸝⸝⸝˙Ⱉ˙ʔ",
                                                "查询失败"
                                            ).show()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    _state.onNext(currentState)
                                }
                                "404" -> {
                                    currentState.grades = emptyList()
                                    handler.post {
                                        try {
                                            NormalResponseDialog(
                                                action.context,
                                                "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡",
                                                "查询失败"
                                            ).show()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    _state.onNext(currentState)
                                }
                                else -> {
                                    currentState.grades = emptyList()
                                    _state.onNext(currentState)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
                _state.onNext(currentState)
                currentState
            }
        }

    }

}