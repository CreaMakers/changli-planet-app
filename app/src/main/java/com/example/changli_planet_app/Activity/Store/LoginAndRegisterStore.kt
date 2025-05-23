package com.example.changli_planet_app.Activity.Store

import android.os.Handler
import android.os.Looper
import com.example.changli_planet_app.Activity.Action.LoginAndRegisterAction
import com.example.changli_planet_app.Activity.LoginActivity
import com.example.changli_planet_app.Activity.State.LoginAndRegisterState
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Data.jsonbean.Email
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.Data.jsonbean.UserPasswordAndEmail
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.Widget.Dialog.LoginInformationDialog
import com.example.changli_planet_app.Utils.Event.FinishEvent
import com.example.changli_planet_app.Utils.EventBusHelper
import com.example.changli_planet_app.Widget.View.CustomToast
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
                    currentState.isLengthValid = action.content.length >= 8
                    currentState.hasUpperAndLower = action.content.matches(".*[A-Z].*".toRegex()) &&
                            action.content.matches(".*[a-z].*".toRegex())

                    currentState.hasNumberAndSpecial =
                        action.content.matches(".*[0-9].*".toRegex()) &&
                                action.content.matches(".*[^A-Za-z0-9].*".toRegex())

                }else if(action.type=="email"){
                    currentState.email=action.content
                }else if(action.type=="captcha"){
                    currentState.captcha=action.content
                }

                currentState.canBind=checkCanBind()

                if (!currentState.isEnable && checkEnable()) {
                    currentState.isEnable = true
                }

                if (!checkEnable()) {
                    currentState.isEnable = false
                }
                _state.onNext(currentState)
                currentState
            }

            is LoginAndRegisterAction.Login -> {
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp + "/sessions/password")
                    .header("deviceId", LoginActivity.getDeviceId(action.context))
                    .body(OkHttpHelper.gson.toJson(action.userPassword))
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        when (fromJson.msg) {

                            "用户登录成功" -> {
                                UserInfoManager.username = action.userPassword.username
                                UserInfoManager.userPassword = action.userPassword.password
                                PlanetApplication.accessToken = response.header("Authorization", "") ?: ""
                                handler.post {
                                    Route.goHomeForcibly(action.context)
                                    EventBusHelper.post(FinishEvent("Login"))
                                    EventBusHelper.post(FinishEvent("LoginByEmail"))   //将两个登录界面都关闭
                                }
                            }

                            else -> {
                                handler.post {
//                                    LoginInformationDialog(
//                                        action.context,
//                                        fromJson.msg,
//                                        "登陆失败"
//                                    ).show()
                                    LoginInformationDialog.showDialog(action.context,fromJson.msg,"登陆失败")
                                }
                            }
                        }
                    }

                    override fun onFailure(error: String) {}
                })
                currentState
            }

            is LoginAndRegisterAction.LoginByEmail->{
                val builder=HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp+"/sessions/email")
                    .body(OkHttpHelper.gson.toJson(action.userEmail))
                    .build()
                OkHttpHelper.sendRequest(builder,object :RequestCallback{
                    override fun onSuccess(response: Response) {
                        val fromJson=OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        if(fromJson.msg=="用户登录成功"){
                            UserInfoManager.userEmail = action.userEmail.email
                            PlanetApplication.accessToken = response.header("Authorization", "") ?: ""
                            handler.post {
                                Route.goHomeForcibly(action.context)
                                EventBusHelper.post(FinishEvent("LoginByEmail"))
                                EventBusHelper.post(FinishEvent("Login"))      //将两个登录界面都关闭
                            }
                        }else{
                            handler.post{
                                LoginInformationDialog.showDialog(action.context,fromJson.msg,"登陆失败")
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                    }

                })
                currentState
            }

            is LoginAndRegisterAction.Register -> {

                val newUser=UserPasswordAndEmail(
                    username = currentState.account,
                    password = currentState.password,
                    email = currentState.email,
                    verifyCode = currentState.captcha
                )
                val builder = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp + "/register")
                    .body(OkHttpHelper.gson.toJson(newUser))
                    .build()
                OkHttpHelper.sendRequest(builder, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson = OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        if (fromJson.msg == "用户注册成功") {
                            handler.post {
                                Route.goLoginFromRegister(action.context, newUser.username, newUser.password)
                                EventBusHelper.post(FinishEvent("Register"))
                                EventBusHelper.post(FinishEvent("bindEmail"))
                            }
                        }
                        else if(fromJson.msg=="验证码错误"){
                            handler.post{
                                LoginInformationDialog.showDialog(action.context,fromJson.msg,"注册失败")
                            }
                        }
                        else {
                            handler.post {
//                                LoginInformationDialog(
//                                    action.context,
//                                    fromJson.msg,
//                                    "注册失败"
//                                ).show()
                                LoginInformationDialog.showDialog(action.context,fromJson.msg,"注册失败")
                            }
                        }
                    }

                    override fun onFailure(error: String) {}
                })
                currentState
            }

            is LoginAndRegisterAction.CheckName->{
                val builder=HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.UserIp+"/availability/{username}")
                    .addPathParam("username",currentState.account)
                    .build()
                OkHttpHelper.sendRequest(builder,object :RequestCallback{
                    override fun onSuccess(response: Response) {
                        val fromJson=OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        if(fromJson.msg=="用户验证通过"){
                            handler.post{
                                Route.goBindEmailFromRegister(action.context,action.account, action.password)
                            }
                        }
                        else if(fromJson.msg=="用户已存在"){
                            handler.post{
                                CustomToast.showMessage(action.context,"账号已被注册，请换一个吧~")
                            }

                        }
                    }
                    override fun onFailure(error: String) {}

                })
                currentState
            }

            is LoginAndRegisterAction.GetCaptcha->{
                val builder=HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp+"/auth/verification-code/register")
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
                                CustomToast.showMessage(PlanetApplication.appContext,"发送失败")
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                    }
                })
                _state.onNext(currentState)
                currentState
            }

            is LoginAndRegisterAction.GetCaptchaByLogin->{
                val builder=HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp+"/auth/verification-code/login")
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
                                CustomToast.showMessage(PlanetApplication.appContext,"发送失败")
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                    }
                })
                _state.onNext(currentState)
                currentState
            }

            LoginAndRegisterAction.ChangeVisibilityOfPassword -> {
                currentState.isVisibilityPassword = !currentState.isVisibilityPassword
                _state.onNext(currentState)
                currentState
            }

            is LoginAndRegisterAction.InputLogin -> {
                if (action.type == "account") {
                    currentState.account = action.content
                } else if (action.type == "password") {
                    currentState.password = action.content
                } else {
                    currentState.isCheck = action.content.equals("checked")
                }

                if (!currentState.isEnable && checkLoginEnable()) {
                    currentState.isEnable = true
                }
                currentState.isClearPassword = currentState.password.isNotEmpty()
                if (!checkLoginEnable()) {
                    currentState.isEnable = false
                }
                _state.onNext(currentState)
                currentState
            }

            is LoginAndRegisterAction.InputLoginByEmail->{
                if (action.type == "email") {
                    currentState.email = action.content
                } else if (action.type == "captcha") {
                    currentState.captcha = action.content
                } else {
                    currentState.isCheck = action.content.equals("checked")
                }
                currentState.isEnableByEmail=checkEnableEmail()
                _state.onNext(currentState)
                currentState
            }
        }

    }

    private fun checkEnable(): Boolean {
        return currentState.account.isNotEmpty() && currentState.password.isNotEmpty() && currentState.isLengthValid && currentState.hasUpperAndLower && currentState.hasNumberAndSpecial
    }

    private fun checkLoginEnable(): Boolean {
        return currentState.account.isNotEmpty() && currentState.password.isNotEmpty() && currentState.isCheck
    }

    private fun checkCanBind():Boolean{
        return currentState.email.isNotEmpty() && currentState.captcha.isNotEmpty()
    }

    private fun checkEnableEmail():Boolean{
        return currentState.email.isNotEmpty()&&currentState.captcha.isNotEmpty()&&currentState.isCheck
    }

    private fun startCountDown(){
        currentState.isCountDown=true
        val job=GlobalScope.launch {
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