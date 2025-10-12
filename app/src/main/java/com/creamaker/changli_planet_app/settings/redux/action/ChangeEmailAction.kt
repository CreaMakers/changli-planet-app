package com.creamaker.changli_planet_app.settings.redux.action

import android.content.Context

sealed class ChangeEmailAction {
    object Initilaize:ChangeEmailAction()
    object GetCaptcha:ChangeEmailAction()
    data class Input(val content:String,val type:String):ChangeEmailAction()
    data class Change(val context: Context):ChangeEmailAction()
}