package com.example.changli_planet_app.Activity.Action

import android.content.Context

sealed class ChangeEmailAction {
    object Initilaize:ChangeEmailAction()
    object GetCaptcha:ChangeEmailAction()
    data class Input(val content:String,val type:String):ChangeEmailAction()
    data class Change(val context:Context):ChangeEmailAction()
}