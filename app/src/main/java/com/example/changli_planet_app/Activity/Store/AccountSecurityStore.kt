package com.example.changli_planet_app.Activity.Store

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import com.example.changli_planet_app.Activity.Action.AccountSecurityAction
import com.example.changli_planet_app.Activity.LoginActivity
import com.example.changli_planet_app.Activity.State.AccountSecurityState
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.UI.NormalResponseDialog
import com.example.changli_planet_app.Util.Event.FinishEvent
import com.example.changli_planet_app.Util.EventBusHelper
import okhttp3.Response
import org.greenrobot.eventbus.EventBus


class AccountSecurityStore : Store<AccountSecurityState, AccountSecurityAction>() {
    var currentState = AccountSecurityState()
    private val handler = Handler(Looper.getMainLooper())

    override fun handleEvent(action: AccountSecurityAction) {
        currentState = when (action) {
            is AccountSecurityAction.UpdateSafeType -> {
                currentState.password = action.newPassword
                currentState.safeType = 0
                currentState.isLengthValid = action.newPassword.length >= 8

                currentState.hasUpperAndLower = action.newPassword.matches(".*[A-Z].*".toRegex()) &&
                        action.newPassword.matches(".*[a-z].*".toRegex())

                currentState.hasNumberAndSpecial =
                    action.newPassword.matches(".*[0-9].*".toRegex()) &&
                            action.newPassword.matches(".*[^A-Za-z0-9].*".toRegex())

                if (currentState.isLengthValid) currentState.safeType++
                if (currentState.hasUpperAndLower) currentState.safeType++
                if (currentState.hasNumberAndSpecial) currentState.safeType++

                _state.onNext(currentState)
                currentState
            }

            AccountSecurityAction.initilaize -> {
                currentState.safeType = 0
                _state.onNext(currentState)
                currentState
            }

            is AccountSecurityAction.UpdateVisible -> {
                when (action.type) {
                    "curPasswordImg" -> currentState.curPasswordVisible =
                        !currentState.curPasswordVisible

                    "newPasswordImg" -> currentState.newPasswordVisible =
                        !currentState.newPasswordVisible

                    "confirmPasswordImg" -> currentState.confirmPasswordVisible =
                        !currentState.confirmPasswordVisible
                }
                _state.onNext(currentState)
                currentState
            }

            is AccountSecurityAction.ChangePassword -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .put(PlanetApplication.UserIp + "/me/password")
                    .body(OkHttpHelper.gson.toJson(ChangePasswordJson(action.newPassword, action.confirmPassword)))
                    .build()

                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        var fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        when (fromJson.code) {
                            "200" -> {
                                handler.post {
                                    EventBusHelper.post(FinishEvent("ChangePassword"))
                                }
                            }
                            else -> {
                                handler.post {
                                    NormalResponseDialog(
                                        action.context,
                                        fromJson.msg,
                                        "更改密码失败"
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(error: String) {

                    }

                })
                _state.onNext(currentState)
                currentState
            }
        }
    }
}

data class ChangePasswordJson(
    val new_password: String,
    val confirm_password: String
)