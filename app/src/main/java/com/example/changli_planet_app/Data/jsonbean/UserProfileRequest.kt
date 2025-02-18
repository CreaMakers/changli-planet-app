package com.example.changli_planet_app.Data.jsonbean

import com.google.gson.annotations.SerializedName

data class UserProfileRequest(
    var avatarUrl: String = "", // 用户头像 URL，默认为空字符串
    val bio: String = "",        // 用户简介，默认为空字符串
    var userLevel: Int = 0,     // 用户等级，默认为 0
    val gender: Int = 0,         // 性别，默认为 0（例如：0 表示保密，1 表示男，2 表示女）
    val grade: String = "",          // 年级，默认为空字符串
    val birthDate: String = "",  // 出生日期，默认为空字符串
    val location: String = "",   // 地址，默认为空字符串
    val website: String = ""      // 个人网站，默认为空字符串
)