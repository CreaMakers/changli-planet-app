package com.example.changli_planet_app.Activity.Action

import android.content.Context
import com.example.changli_planet_app.Data.jsonbean.UserEmail
import com.example.changli_planet_app.Data.jsonbean.UserPassword

sealed class LoginAndRegisterAction {
    object initilaize : LoginAndRegisterAction()
    object ChangeVisibilityOfPassword : LoginAndRegisterAction()
    object GetCaptcha:LoginAndRegisterAction()          //获得注册的验证码
    object GetCaptchaByLogin:LoginAndRegisterAction()   //获得登录的验证码
    data class CheckName(val context: Context,val account:String,val password:String):LoginAndRegisterAction()
    data class input(val content: String, val type: String) : LoginAndRegisterAction()
    data class InputLogin(val content: String, val type: String) : LoginAndRegisterAction()
    data class InputLoginByEmail(val content: String,val type: String):LoginAndRegisterAction()
    data class Login(val userPassword: UserPassword, val context: Context) :
        LoginAndRegisterAction()
    data class LoginByEmail(val userEmail: UserEmail,val context: Context):LoginAndRegisterAction()
    data class Register(val context: Context) :
        LoginAndRegisterAction()
}