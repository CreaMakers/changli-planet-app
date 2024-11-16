package com.example.changli_planet_app.Activity.Store

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.changli_planet_app.Activity.Action.LoginAction
import com.example.changli_planet_app.Activity.LoginActivity
import com.example.changli_planet_app.Activity.State.LoginState
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Data.jsonbean.UserPassword
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.UI.LoginInformationDialog
import okhttp3.Response
import okhttp3.internal.http2.Http2Reader
class LoginStore:Store<LoginState,LoginAction>() {
    var currentState = LoginState()
    var handler = Handler(Looper.getMainLooper())
    override fun handleEvent(action: LoginAction) {
        currentState = when(action){
            is LoginAction.initilaize->{
                _state.onNext(currentState)
                currentState
            }
            is LoginAction.input->{
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
            is LoginAction.Login->{
                val httpUrlHelper = HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp + "session")
                    .header("deviceId",LoginActivity.getDeviceId(action.context))
                    .body(OkHttpHelper.gson.toJson(action.userPassword))
                    .build()
                OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                    override fun onSuccess(response: Response) {
                        var fromJson = OkHttpHelper.gson.fromJson(response.body?.string(), MyResponse::class.java)
                        when(fromJson.msg){
                            "用户登录成功"->{}
                            else->{handler.post{var loginInformationDialog = LoginInformationDialog(action.context,fromJson.msg).show()}}
                        }
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