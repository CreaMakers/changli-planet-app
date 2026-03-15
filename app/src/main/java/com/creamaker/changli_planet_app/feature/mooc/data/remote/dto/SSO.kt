package com.creamaker.changli_planet_app.feature.mooc.data.remote.dto

import androidx.annotation.Keep

@Keep
data class LoginForm(
    val pwdEncryptSalt: String,
    val execution: String
)

@Keep
data class CheckCaptchaResponse(
    val isNeed: Boolean
)

@Keep
data class LoginUserResponse(
    val data: SSOProfile?
)

@Keep
data class SSOProfile(
    val categoryName: String,
    val userAccount: String,
    val userName: String,
    val certCode: String,
    val phone: String,
    val email: String?,
    val deptName: String,
    val defaultUserAvatar: String,
    val headImageIcon: String?
) {
    val avatar: String
        get() = headImageIcon ?: defaultUserAvatar
}