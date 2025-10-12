package com.creamaker.changli_planet_app.settings.redux.action

import android.content.Context

sealed class BindingUserAction {
    class BindingStudentNumber(val context: Context, val student_number: String) : BindingUserAction()
}