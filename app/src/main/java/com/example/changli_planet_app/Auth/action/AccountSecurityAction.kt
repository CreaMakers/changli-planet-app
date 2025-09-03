package com.example.changli_planet_app.Auth.action

import android.content.Context

sealed class AccountSecurityAction {
    object initilaize : AccountSecurityAction()
    object GetCaptcha:AccountSecurityAction()
    data class UpdateSafeType(val newPassword: String) : AccountSecurityAction()
    data class UpdateVisible(val type: String) : AccountSecurityAction()
    data class ChangePassword(val context: Context, val oldPassword:String, val newPassword: String, val confirmPassword: String) :
        AccountSecurityAction()
    data class Input(val content:String,val type:String):AccountSecurityAction()
    data class ChangeByEmail(val context: Context):AccountSecurityAction()
}