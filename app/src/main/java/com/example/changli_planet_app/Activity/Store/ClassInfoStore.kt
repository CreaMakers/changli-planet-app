package com.example.changli_planet_app.Activity.Store

import android.os.Handler
import android.os.Looper
import com.example.changli_planet_app.Activity.Action.ClassInfoAction
import com.example.changli_planet_app.Activity.State.ClassInfoState
import com.example.changli_planet_app.Cache.StudentInfoManager
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.EmptyClassroomResponse
import com.example.changli_planet_app.Network.Response.ExamArrangementResponse
import com.example.changli_planet_app.Widget.Dialog.EmptyClassroomDialog
import com.example.changli_planet_app.Widget.View.CustomToast
import okhttp3.Response

class ClassInfoStore : Store<ClassInfoState, ClassInfoAction>() {
    var currentState = ClassInfoState()
    val handler = Handler(Looper.getMainLooper())
    override fun handleEvent(action: ClassInfoAction) {
        currentState = when (action) {
            is ClassInfoAction.UpdateDay -> {
                currentState.day = action.day
                _state.onNext(currentState)
                currentState
            }

            is ClassInfoAction.UpdateRegion -> {
                currentState.region = action.region
                _state.onNext(currentState)
                currentState
            }

            is ClassInfoAction.UpdateStartAndEnd -> {
                currentState.start = action.start
                currentState.end = action.end
                _state.onNext(currentState)
                currentState
            }

            is ClassInfoAction.UpdateWeek -> {
                currentState.week = action.week
                _state.onNext(currentState)
                currentState
            }

            ClassInfoAction.initilaize -> {
                _state.onNext(currentState)
                currentState
            }

            is ClassInfoAction.QueryEmptyClassInfo -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.ToolIp + "/classroom")
                    .addQueryParam("stuNum", StudentInfoManager.studentId)
                    .addQueryParam("password", StudentInfoManager.studentPassword)
                    .addQueryParam("term", action.term)
                    .addQueryParam("week", currentState.week)
                    .addQueryParam(
                        "region", when (currentState.region) {
                            "金盆岭校区" -> "2"
                            "云塘校区" -> "1"
                            else -> "-1"
                        }
                    )
                    .addQueryParam("start", currentState.start)
                    .addQueryParam("end", currentState.end)
                    .addQueryParam(
                        "day", when (currentState.day) {
                            "星期天" -> "0"
                            "星期一" -> "1"
                            "星期二" -> "2"
                            "星期三" -> "3"
                            "星期四" -> "4"
                            "星期五" -> "5"
                            "星期六" -> "6"
                            else -> "-1"
                        }
                    )
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            EmptyClassroomResponse::class.java
                        )
                        when (fromJson.code) {
                            "200" -> {
                                handler.post {
                                    EmptyClassroomDialog.showDialog(action.context,fromJson.data)
                                }
                            }

                            else -> {
                                handler.post {
                                    CustomToast.showMessage(
                                        action.context,
                                        "出错啦，${fromJson.msg}"
                                    )
                                }
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                        handler.post {
                            CustomToast.showMessage(action.context, "出错啦，${error}")
                        }
                    }

                })
                currentState
            }
        }
    }
}