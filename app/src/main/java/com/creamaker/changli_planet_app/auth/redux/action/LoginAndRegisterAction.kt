package com.creamaker.changli_planet_app.auth.redux.action

import android.content.Context
import com.creamaker.changli_planet_app.auth.data.remote.dto.UserEmail
import com.creamaker.changli_planet_app.auth.data.remote.dto.UserPassword

/**
 * 注册与登陆Action
 */
sealed class LoginAndRegisterAction {
    object initilaize : LoginAndRegisterAction()
    object ChangeVisibilityOfPassword : LoginAndRegisterAction()
    object GetCaptcha:LoginAndRegisterAction()          //获得注册的验证码
    object GetCaptchaByLogin:LoginAndRegisterAction()   //获得登录的验证码
    data class CheckName(val context: Context, val account:String, val password:String):LoginAndRegisterAction()
    data class input(val content: String, val type: String) : LoginAndRegisterAction()
    data class InputLogin(val content: String, val type: String) : LoginAndRegisterAction()
    data class InputLoginByEmail(val content: String,val type: String):LoginAndRegisterAction()
    data class Login(val userPassword: UserPassword, val context: Context) :
        LoginAndRegisterAction()
    data class LoginByEmail(val userEmail: UserEmail, val context: Context):LoginAndRegisterAction()
    data class Register(val context: Context) :
        LoginAndRegisterAction()
}