package com.example.changli_planet_app.Network.Response

import java.time.LocalDateTime

data class UserProfile(
    val userId: Int,
    val avatarUrl: String,
    val bio: String,
    val description: String,
    val userLevel: Int,
    val gender: Int,
    val grade: String,
    val birthDate: LocalDateTime,
    val location: String,
    val website: String?,
    val createTime: LocalDateTime,
    val updateTime: LocalDateTime,
    val isDeleted: Int
)

data class UserProfileResponse(
    val code: String,
    val msg: String,
    val data: UserProfile?
)