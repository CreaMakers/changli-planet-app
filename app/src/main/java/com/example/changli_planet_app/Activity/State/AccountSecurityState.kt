package com.example.changli_planet_app.Activity.State

data class AccountSecurityState (
    var safeType: Int = 0,
    var password: String = "",
    var newPasswordVisible: Boolean = false,
    var curPasswordVisible: Boolean = false,
    var confirmPasswordVisible: Boolean = false,

    var hasUpperAndLower: Boolean = false,
    var isLengthValid: Boolean = false,
    var hasNumberAndSpecial: Boolean = false,

)