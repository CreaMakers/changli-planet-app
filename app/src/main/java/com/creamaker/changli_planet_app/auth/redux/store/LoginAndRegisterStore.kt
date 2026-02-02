package com.creamaker.changli_planet_app.auth.redux.store

import android.os.Handler
import android.os.Looper
import com.creamaker.changli_planet_app.auth.data.remote.dto.Email
import com.creamaker.changli_planet_app.auth.data.remote.dto.UserPasswordAndEmail
import com.creamaker.changli_planet_app.auth.redux.action.LoginAndRegisterAction
import com.creamaker.changli_planet_app.auth.redux.state.LoginAndRegisterState
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.core.network.HttpUrlHelper
import com.creamaker.changli_planet_app.core.network.MyResponse
import com.creamaker.changli_planet_app.core.network.OkHttpHelper
import com.creamaker.changli_planet_app.core.network.listener.RequestCallback
import com.creamaker.changli_planet_app.utils.Event.FinishEvent
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.widget.dialog.LoginInformationDialog
import com.creamaker.changli_planet_app.widget.view.CustomToast
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.GlobalScope
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
                    .post(PlanetApplication.Companion.UserIp + "/sessions/password")
                    .header("deviceId", PlanetApplication.Companion.getSystemDeviceId())
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
                                PlanetApplication.Companion.is_expired = false
                                UserInfoManager.username = action.userPassword.username
                                UserInfoManager.userPassword = action.userPassword.password
                                PlanetApplication.Companion.accessToken = response.header("Authorization", "") ?: ""
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
////                                    ).show()
                                    LoginInformationDialog.Companion.showDialog(action.context,fromJson.msg,"登陆失败")
                                }
                            }
                        }
                    }

                    override fun onFailure(error: String) {}
                })
                currentState
            }

            is LoginAndRegisterAction.LoginByEmail->{
                val builder= HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.Companion.UserIp+"/sessions/email")
                    .header("deviceId", PlanetApplication.Companion.getSystemDeviceId())
                    .body(OkHttpHelper.gson.toJson(action.userEmail))
                    .build()
                OkHttpHelper.sendRequest(builder,object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson= OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        if(fromJson.msg=="用户登录成功"){
                            //修改
                            PlanetApplication.Companion.is_expired = false
                            UserInfoManager.userEmail = action.userEmail.email
                            PlanetApplication.Companion.accessToken = response.header("Authorization", "") ?: ""
                            handler.post {
                                Route.goHomeForcibly(action.context)
                                EventBusHelper.post(FinishEvent("LoginByEmail"))
                                EventBusHelper.post(FinishEvent("Login"))      //将两个登录界面都关闭
                            }
                        }else{
                            handler.post{
                                LoginInformationDialog.Companion.showDialog(action.context,fromJson.msg,"登陆失败")
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                    }

                })
                currentState
            }

            is LoginAndRegisterAction.Register -> {

                val newUser= UserPasswordAndEmail(
                    username = currentState.account,
                    password = currentState.password,
                    email = currentState.email,
                    verifyCode = currentState.captcha
                )
                val builder = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.Companion.UserIp + "/register")
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
                                LoginInformationDialog.Companion.showDialog(action.context,fromJson.msg,"注册失败")
                            }
                        }
                        else {
                            handler.post {
//                                LoginInformationDialog(
//                                    action.context,
//                                    fromJson.msg,
//                                    "注册失败"
//                                ).show()
                                LoginInformationDialog.Companion.showDialog(action.context,fromJson.msg,"注册失败")
                            }
                        }
                    }

                    override fun onFailure(error: String) {}
                })
                currentState
            }

            is LoginAndRegisterAction.CheckName->{
                val builder= HttpUrlHelper.HttpRequest()
                    .get(PlanetApplication.Companion.UserIp+"/availability/{username}")
                    .addPathParam("username",currentState.account)
                    .build()
                OkHttpHelper.sendRequest(builder,object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson= OkHttpHelper.gson.fromJson(
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
                                CustomToast.Companion.showMessage(action.context,"账号已被注册，请换一个吧~")
                            }

                        }
                    }
                    override fun onFailure(error: String) {}

                })
                currentState
            }

            is LoginAndRegisterAction.GetCaptcha->{
                val builder= HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.Companion.UserIp+"/auth/verification-code/register")
                    .body(OkHttpHelper.gson.toJson(Email(currentState.email)))
                    .build()
                OkHttpHelper.sendRequest(builder,object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson= OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        if(fromJson.msg=="验证码已发送"){
                            startCountDown()
                        }else{
                            handler.post{
                                CustomToast.Companion.showMessage(PlanetApplication.Companion.appContext,"发送失败")
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
                val builder= HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.Companion.UserIp+"/auth/verification-code/login")
                    .body(OkHttpHelper.gson.toJson(Email(currentState.email)))
                    .build()
                OkHttpHelper.sendRequest(builder,object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        val fromJson= OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        if(fromJson.msg=="验证码已发送"){
                            startCountDown()
                        }else{
                            handler.post{
                                CustomToast.Companion.showMessage(PlanetApplication.Companion.appContext,"发送失败")
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
        return currentState.account.isNotEmpty() && currentState.password.isNotEmpty() && currentState.password.length>=6
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