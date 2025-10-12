package com.creamaker.changli_planet_app.settings.redux.store

import android.os.Handler
import android.os.Looper
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.core.network.HttpUrlHelper
import com.creamaker.changli_planet_app.core.network.MyResponse
import com.creamaker.changli_planet_app.core.network.OkHttpHelper
import com.creamaker.changli_planet_app.core.network.listener.RequestCallback
import com.creamaker.changli_planet_app.settings.redux.action.BindingUserAction
import com.creamaker.changli_planet_app.settings.redux.state.BindingUserState
import com.creamaker.changli_planet_app.utils.Event.FinishEvent
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.utils.PlanetConst
import com.creamaker.changli_planet_app.widget.Dialog.NormalResponseDialog
import okhttp3.Response

class BindingUserStore : Store<BindingUserState, BindingUserAction>() {  // 修正泛型参数

    var currentState = BindingUserState()
    private val handler = Handler(Looper.getMainLooper())

    data class StudentNumberRequest(
        val studentNumber: String
    )

    override fun handleEvent(action: BindingUserAction) {
        currentState = when (action) {
            is BindingUserAction.BindingStudentNumber -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.Companion.UserIp + "/me/student-number")
                    .body(OkHttpHelper.gson.toJson(StudentNumberRequest(action.student_number)))
                    .build()

                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        try {
                            val fromJson = OkHttpHelper.gson.fromJson(
                                response.body?.string(),
                                MyResponse::class.java
                            )
                            when (fromJson.code) {
                                "200" -> {
                                    handler.post {
                                        EventBusHelper.post(FinishEvent("bindingUser"))
                                    }
                                }

                                "401" -> {
                                    if (fromJson.msg.equals(PlanetConst.UNAUTHORIZATION)) {

                                    } else {

                                    }
                                }

                                else -> {
                                    handler.post {
                                        NormalResponseDialog(
                                            action.context,
                                            fromJson.msg,
                                            "绑定失败"
                                        ).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            handler.post {
                                NormalResponseDialog(
                                    action.context,
                                    "数据解析错误",
                                    "绑定失败"
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                        handler.post {
                            NormalResponseDialog(
                                action.context,
                                "网络请求失败",
                                "绑定失败"
                            ).show()
                        }
                    }
                })

                _state.onNext(currentState)
                currentState
            }

            else -> currentState
        }

        // 通知状态更新
        _state.onNext(currentState)
    }
}