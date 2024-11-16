package com.example.changli_planet_app.Activity.Action

import android.content.Context
import com.example.changli_planet_app.Data.jsonbean.UserPassword

sealed class LoginAction {
    object initilaize:LoginAction()
    data class input(val content: String,val type:String):LoginAction()
    data class Login(val userPassword: UserPassword,val context: Context):LoginAction()
}