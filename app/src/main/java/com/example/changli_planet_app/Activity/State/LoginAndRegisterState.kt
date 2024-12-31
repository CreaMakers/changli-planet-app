package com.example.changli_planet_app.Activity.State

data class LoginAndRegisterState(
    var account: String = "",
    var password: String = "",
    var isVisibilityPassword: Boolean = false,
    var isClearPassword: Boolean = false,
    var isEnable: Boolean = false,
    var isCheck: Boolean = false
)