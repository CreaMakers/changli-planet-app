package com.creamaker.changli_planet_app.settings.redux.store

import android.os.Handler
import android.os.Looper
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.core.network.HttpUrlHelper
import com.creamaker.changli_planet_app.core.network.MyResponse
import com.creamaker.changli_planet_app.core.network.OkHttpHelper
import com.creamaker.changli_planet_app.core.network.listener.RequestCallback
import com.creamaker.changli_planet_app.settings.redux.action.ChangeEmailAction
import com.creamaker.changli_planet_app.settings.redux.state.ChangeEmailState
import com.creamaker.changli_planet_app.utils.Event.FinishEvent
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.widget.dialog.NormalResponseDialog
import com.creamaker.changli_planet_app.widget.view.CustomToast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Response

class ChangeEmailStore:Store<ChangeEmailState,ChangeEmailAction>() {
    var currentState=ChangeEmailState()
    private val handler=Handler(Looper.getMainLooper())

    override fun handleEvent(action: ChangeEmailAction) {
        currentState = when(action){
            ChangeEmailAction.Initilaize -> {
                _state.onNext(currentState)
                currentState
            }
            is ChangeEmailAction.Input -> {
                when(action.type){
                    "newEmail"->currentState.newEmail=action.content
                    "captcha"->currentState.captcha=action.content
                    "curPassword"->currentState.curPassword=action.content
                }
                currentState.isEnable=checkEnable()
                _state.onNext(currentState)
                currentState
            }
            ChangeEmailAction.GetCaptcha -> {
                val builder=HttpUrlHelper.HttpRequest()
                    .post(PlanetApplication.UserIp+"/auth/verification-code/email-change")
                    .body(OkHttpHelper.gson.toJson(GetCaptchaJson(
                        currentState.newEmail,
                        currentState.curPassword
                    )))
                    .build()
                OkHttpHelper.sendRequest(builder,object :RequestCallback{
                    override fun onSuccess(response: Response) {
                        val formJson=OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        when(formJson.msg){
                            "验证码已发送"->{
                                startCountDown()
                            }
                            else->{
                                handler.post{
                                    CustomToast.showMessage(PlanetApplication.appContext,formJson.msg)
                                }
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                    }

                })
                currentState
            }

            is ChangeEmailAction.Change -> {
                val builder=HttpUrlHelper.HttpRequest()
                    .put(PlanetApplication.UserIp+"/me/email")
                    .body(OkHttpHelper.gson.toJson(ChangeEmailJson(
                        currentState.newEmail,
                        currentState.captcha
                    )))
                    .build()
                OkHttpHelper.sendRequest(builder,object :RequestCallback{
                    override fun onSuccess(response: Response) {
                        val formJson=OkHttpHelper.gson.fromJson(
                            response.body?.string(),
                            MyResponse::class.java
                        )
                        when(formJson.code){
                            "200"->{
                                handler.post{
                                    CustomToast.showMessage(PlanetApplication.appContext,"更改成功")
                                    EventBusHelper.post(FinishEvent("ChangeEmail"))
                                }
                            }
                            else->{
                                handler.post{
                                    NormalResponseDialog(
                                        action.context,
                                        formJson.msg,
                                        "更改失败"
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(error: String) {
                    }

                })
                currentState
            }

        }

    }

    private fun checkEnable():Boolean{
        return currentState.newEmail.isNotEmpty()&&
                currentState.captcha.isNotEmpty()&&
                currentState.curPassword.isNotEmpty()
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

data class ChangeEmailJson(
    val newEmail:String,
    val verificationCode:String
)

data class GetCaptchaJson(
    val newEmail:String,
    val currentPassword:String
)