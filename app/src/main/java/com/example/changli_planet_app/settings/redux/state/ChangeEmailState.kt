package com.example.changli_planet_app.settings.redux.state

data class ChangeEmailState(
    var newEmail:String="",
    var captcha:String="",
    var isEnable:Boolean=false,
    var curPassword:String="",
    var isCountDown:Boolean=false,
    var countDown:Int=0
)