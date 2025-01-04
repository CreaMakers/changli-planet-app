package com.example.changli_planet_app.Activity.Action

import android.content.Context

sealed class BindingUserAction {
    class BindingStudentNumber(val context: Context, val student_number: String) : BindingUserAction()
}