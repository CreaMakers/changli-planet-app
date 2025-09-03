package com.example.changli_planet_app.Settings.action

import android.content.Context

sealed class BindingUserAction {
    class BindingStudentNumber(val context: Context, val student_number: String) : BindingUserAction()
}