package com.example.changli_planet_app.Activity.State

data class LoginAndRegisterState(
    var account: String = "",
    var password: String = "",
    var email:String="",
    var captcha:String="",
    var countDown:Int=0,         //发送验证码的倒计时
    var isVisibilityPassword: Boolean = false,
    var isClearPassword: Boolean = false,
    var isEnable: Boolean = false,
    var isCheck: Boolean = false,
    var canBind: Boolean=false,       //是否可以绑定邮箱，当email和captcha不为空时为true
    var isCountDown:Boolean=false,    //发送验证码的text是否在倒计时
    var isEnableByEmail:Boolean=false,  //邮箱登录的按钮是否可用

    var hasUpperAndLower: Boolean = false,
    var isLengthValid: Boolean = false,
    var hasNumberAndSpecial: Boolean = false,
)