package com.example.changli_planet_app.feature.mooc.data.remote.dto

data class LoginForm(
    val pwdEncryptSalt: String,
    val execution: String
)

data class CheckCaptchaResponse(
    val isNeed: Boolean
)

data class LoginUserResponse(
    val data: SSOProfile?
)

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