package com.example.changli_planet_app.Activity.Action

import android.content.Context

sealed class AccountSecurityAction {
    object initilaize : AccountSecurityAction()
    data class UpdateSafeType(val newPassword: String) : AccountSecurityAction()
    data class UpdateVisible(val type: String) : AccountSecurityAction()
    data class ChangePassword(val context: Context, val newPassword: String, val confirmPassword: String) :
        AccountSecurityAction()
}