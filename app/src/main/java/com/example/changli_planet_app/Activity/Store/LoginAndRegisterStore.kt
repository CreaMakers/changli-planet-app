package com.example.changli_planet_app.Activity.Store

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.changli_planet_app.Activity.Action.LoginAndRegisterAction
import com.example.changli_planet_app.Activity.LoginActivity
import com.example.changli_planet_app.Activity.MainActivity
import com.example.changli_planet_app.Activity.State.LoginAndRegisterState
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
import okhttp3.Response

class LoginAndRegisterStore:Store<LoginAndRegisterState,LoginAndRegisterAction>() {
    var currentState = LoginAndRegisterState()
    var handler = Handler(Looper.getMainLooper())
    var mmkv = MMKV.defaultMMKV()
    override fun handleEvent(action: LoginAndRegisterAction) {
        currentState = when(action){
            is LoginAndRegisterAction.initilaize->{
                _state.onNext(currentState)
                currentState
            }
            is LoginAndRegisterAction.input->{
                if(action.type=="account"){
                    currentState.account = action.content
                }else {
                    currentState.password = action.content
                }
                if(!currentState.isEnable&&checkEnable()){
                    currentState.isEnable = true
                }
                _state.onNext(currentState)
                currentState
            }
            is LoginAndRegisterAction.Login->{
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp + "/session")
                    .header("deviceId",LoginActivity.getDeviceId(action.context))
                    .body(OkHttpHelper.gson.toJson(action.userPassword))
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        var fromJson = OkHttpHelper.gson.fromJson(response.body?.string(), MyResponse::class.java)
                        when(fromJson.msg){
                            "用户登录成功"-> {
                                mmkv.encode("account","${action.userPassword.username}")
                                mmkv.encode("password","${action.userPassword.password}")
                                mmkv.encode("token","${PlanetApplication.accessToken}")
                                handler.post {
                                    Route.goHome(action.context)
                                    EventBusHelper.post(FinishEvent("Login"))
                                }
                            }
                            else->{handler.post{var loginInformationDialog = LoginInformationDialog(action.context,fromJson.msg).show()}}
                        }
                    }
                    override fun onFailure(error: String) {}
                })
                currentState
            }
            is LoginAndRegisterAction.Register->{
                val builder = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp)
                    .body(OkHttpHelper.gson.toJson(action.userPassword))
                    .build()
                OkHttpHelper.sendRequest(builder,object : RequestCallback{
                    override fun onSuccess(response: Response) {
                        val fromJson = OkHttpHelper.gson.fromJson(response.body?.string(),MyResponse::class.java)
                        if(fromJson.msg=="用户注册成功") {
                            handler.post{
                                Route.goLogin(action.context)
                                EventBusHelper.post(FinishEvent("Register"))
                            }
                        }else{handler.post{
                            var loginInformationDialog = LoginInformationDialog(action.context,fromJson.msg).show()}}
                    }
                    override fun onFailure(error: String) {}
                })
                currentState
            }
        }
    }
    private fun checkEnable():Boolean{
        return currentState.account.isNotEmpty()&&currentState.password.isNotEmpty()
    }
}