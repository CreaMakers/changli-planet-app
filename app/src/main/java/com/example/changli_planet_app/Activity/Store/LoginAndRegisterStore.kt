package com.example.changli_planet_app.Activity.Store

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.changli_planet_app.Activity.Action.LoginAndRegisterAction
import com.example.changli_planet_app.Activity.LoginActivity
import com.example.changli_planet_app.Activity.MainActivity
import com.example.changli_planet_app.Activity.State.LoginAndRegisterState
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.UI.LoginInformationDialog
import com.example.changli_planet_app.Util.Event.FinishEvent
import com.example.changli_planet_app.Util.EventBusHelper
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.Dispatcher
import okhttp3.Response

class LoginAndRegisterStore : Store<LoginAndRegisterState, LoginAndRegisterAction>() {
    var currentState = LoginAndRegisterState()
    var handler = Handler(Looper.getMainLooper())
    var mmkv = MMKV.defaultMMKV()
    override fun handleEvent(action: LoginAndRegisterAction) {
        currentState = when (action) {
            is LoginAndRegisterAction.initilaize -> {
                _state.onNext(currentState)
                currentState
            }

            is LoginAndRegisterAction.input -> {
                if (action.type == "account") {
                    currentState.account = action.content
                } else if (action.type == "password") {
                    currentState.password = action.content
                } else {
                    currentState.isCheck = action.content.equals("checked")
                }
                if (!currentState.isEnable && checkEnable()) {
                    currentState.isEnable = true
                }
                currentState.isClearPassword = currentState.password.isNotEmpty()
                if (!checkEnable()) {
                    currentState.isEnable = false
                }
                _state.onNext(currentState)
                currentState
            }

            is LoginAndRegisterAction.Login -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp + "/session")
                    .header("deviceId", LoginActivity.getDeviceId(action.context))
                    .body(OkHttpHelper.gson.toJson(action.userPassword))
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        var fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        when (fromJson.msg) {
                            "用户登录成功" -> {
                                UserInfoManager.username = action.userPassword.username
                                UserInfoManager.userPassword = action.userPassword.password
                                UserInfoManager.token = response.header("Authorization", "") ?: ""
                                handler.post {
                                    Route.goHome(action.context)
                                    EventBusHelper.post(FinishEvent("Login"))
                                }
                            }

                            else -> {
                                handler.post {
                                    LoginInformationDialog(
                                        action.context,
                                        fromJson.msg,
                                        "登陆失败"
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(error: String) {}
                })
                currentState
            }

            is LoginAndRegisterAction.Register -> {
                val builder = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp + "/register")
                    .body(OkHttpHelper.gson.toJson(action.userPassword))
                    .build()
                OkHttpHelper.sendRequest(builder, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        if (fromJson.msg == "用户注册成功") {
                            handler.post {
                                Route.goLogin(action.context)
                                EventBusHelper.post(FinishEvent("Register"))
                            }
                        } else {
                            handler.post {
                                LoginInformationDialog(
                                    action.context,
                                    fromJson.msg,
                                    "注册失败"
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(error: String) {}
                })
                currentState
            }

            LoginAndRegisterAction.ChangeVisibilityOfPassword -> {
                currentState.isVisibilityPassword = !currentState.isVisibilityPassword
                _state.onNext(currentState)
                currentState
            }
        }
    }

    private fun checkEnable(): Boolean {
        return currentState.account.isNotEmpty() && currentState.password.isNotEmpty() && currentState.isCheck
    }
}