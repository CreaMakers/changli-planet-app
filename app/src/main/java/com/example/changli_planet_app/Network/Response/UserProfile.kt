package com.example.changli_planet_app.Network.Response

import android.os.Parcelable
import com.example.changli_planet_app.Cache.Room.entity.UserEntity
import java.time.LocalDateTime

// LocalDateTime 暂时用String替代看看是否产生问题
data class UserProfile(
    val userId: Int = -1,
    val username: String = "",
    val account: String = "用户名字",
    var avatarUrl: String = "注册默认头像",
    var emailbox: String? = "",
    val bio: String = "",
    val description: String = "",
    val userLevel: Int = -1,
    val gender: Int = -1,
    val grade: String = "",
    val birthDate: String? = null,
    var location: String = "",
    val website: String? = "",
    val createTime: String? = null,
    val updateTime: String? = null,
    val isDeleted: Int = -1
)


data class UserProfileResponse(
    val code: String,
    val msg: String,
    val data: UserProfile?
)