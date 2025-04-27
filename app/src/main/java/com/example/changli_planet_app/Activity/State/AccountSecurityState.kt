package com.example.changli_planet_app.Activity.State

data class AccountSecurityState (
    var email:String="",
    var captcha:String="",
    var countDown:Int=0,
    var isCountDown:Boolean=false,
    var isEnable:Boolean=false,
    var safeType: Int = 0,
    var password: String = "",
    var confirmPassword: String="",
    var newPasswordVisible: Boolean = false,
    var curPasswordVisible: Boolean = false,
    var confirmPasswordVisible: Boolean = false,

    var hasUpperAndLower: Boolean = false,
    var isLengthValid: Boolean = false,
    var hasNumberAndSpecial: Boolean = false,

)