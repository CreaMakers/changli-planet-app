package com.example.changli_planet_app.common.data.remote.dto

data class UserStats(
    val userId: Int = 0,
    val account: String = "",
    var studentNumber: String = "",
    val articleCount: Int = 0,
    val commentCount: Int = 0,
    val statementCount: Int = 0,
    val likedCount: Int = 0,
    val coinCount: Int = 0,
    val xp: Int = 0,
    val quizType: Int = 0,
    val lastLoginTime: String? = null,
    val isDeleted: Int = 0,
    val createTime: String ?= null,
    val updateTime: String ?= null,
    val description: String = ""
)

data class UserStatsResponse(
    val code: String,
    val msg: String,
    val data: UserStats?
)
