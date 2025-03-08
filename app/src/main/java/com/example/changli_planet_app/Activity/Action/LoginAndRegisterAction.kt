package com.example.changli_planet_app.Activity.Action

import android.content.Context
import com.example.changli_planet_app.Data.jsonbean.UserPassword

sealed class LoginAndRegisterAction {
    object initilaize : LoginAndRegisterAction()
    object ChangeVisibilityOfPassword : LoginAndRegisterAction()
    data class input(val content: String, val type: String) : LoginAndRegisterAction()
    data class InputLogin(val content: String, val type: String) : LoginAndRegisterAction()
    data class Login(val userPassword: UserPassword, val context: Context) :
        LoginAndRegisterAction()

    data class Register(val userPassword: UserPassword, val context: Context) :
        LoginAndRegisterAction()
}