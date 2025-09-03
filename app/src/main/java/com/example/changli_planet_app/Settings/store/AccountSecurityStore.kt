package com.example.changli_planet_app.Settings.store

import android.os.Handler
import android.os.Looper
import com.example.changli_planet_app.Auth.action.AccountSecurityAction
import com.example.changli_planet_app.Auth.state.AccountSecurityState
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Data.jsonbean.Email
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.Utils.Event.FinishEvent
import com.example.changli_planet_app.Utils.EventBusHelper
import com.example.changli_planet_app.Utils.PlanetConst
import com.example.changli_planet_app.Widget.Dialog.LoginInformationDialog
import com.example.changli_planet_app.Widget.Dialog.NormalResponseDialog
import com.example.changli_planet_app.Widget.View.CustomToast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Response


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
                    .body(OkHttpHelper.gson.toJson(ChangePasswordJson(action.oldPassword,action.newPassword, action.confirmPassword)))
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

            is AccountSecurityAction.Input->{
                when(action.type){
                    "email"->currentState.email=action.content
                    "captcha"->currentState.captcha=action.content
                    "password"->{
                        currentState.password=action.content
                        dispatch(AccountSecurityAction.UpdateSafeType(action.content))
                    }
                    "confirmPassword"->currentState.confirmPassword=action.content
                }
                currentState.isEnable=checkEnable()
                _state.onNext(currentState)
                currentState
            }

            is AccountSecurityAction.ChangeByEmail->{
                val json=ChangePasswordByEmail(
                    currentState.email,
                    currentState.captcha,
                    currentState.password,
                    currentState.confirmPassword
                )
                val builder=HttpUrlHelper.HttpRequest()
                    .put(PlanetApplication.UserIp+"/password/reset")
                    .body(OkHttpHelper.gson.toJson(json))
                    .build()
                OkHttpHelper.sendRequest(builder,object :RequestCallback{
                    override fun onSuccess(response: Response) {
                        val formJson=OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        when(formJson.msg){
                            "用户信息更新成功"->{
                                handler.post{
                                    CustomToast.showMessage(action.context,"密码更改成功")
                                    Route.goLoginForcibly(action.context)
                                    EventBusHelper.post(FinishEvent("changePasswordByEmail"))
                                }
                            }
                            else->{
                                handler.post{
                                    LoginInformationDialog.showDialog(action.context,formJson.msg,"更改失败")
                                }
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                    }

                })
                currentState
            }

            is AccountSecurityAction.GetCaptcha->{
                val builder=HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp+"/auth/verification-code/forget-password")
                    .body(OkHttpHelper.gson.toJson(Email(currentState.email)))
                    .build()
                OkHttpHelper.sendRequest(builder,object :RequestCallback{
                    override fun onSuccess(response: Response) {
                        val fromJson=OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        if(fromJson.msg=="验证码已发送"){
                            startCountDown()
                        }else{
                            handler.post{
                                CustomToast.showMessage(PlanetApplication.appContext,fromJson.msg)
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

    private fun checkEnable():Boolean{
        return currentState.email.isNotEmpty()&&
                currentState.captcha.isNotEmpty()&&
                currentState.password.isNotEmpty()&&
                currentState.confirmPassword.isNotEmpty()&&
                currentState.isLengthValid&&
                currentState.hasUpperAndLower&&
                currentState.hasNumberAndSpecial
    }

    private fun startCountDown(){
        currentState.isCountDown=true
        val job= GlobalScope.launch {
            val totalTime=60
            for(i in 0..totalTime-1) {
                currentState.countDown = totalTime - i
                _state.onNext(currentState)
                delay(1000)
            }
            currentState.countDown=0
            currentState.isCountDown=false
            _state.onNext(currentState)
        }
        job.invokeOnCompletion {
            job.cancel()
        }
    }
}

data class ChangePasswordByEmail(
    val email:String,
    val verification_code:String,
    val new_password:String,
    val confirm_password:String
)

data class ChangePasswordJson(
    val old_password:String,
    val new_password: String,
    val confirm_password: String
)