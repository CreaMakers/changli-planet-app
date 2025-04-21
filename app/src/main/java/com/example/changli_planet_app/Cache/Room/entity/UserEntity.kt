package com.example.changli_planet_app.Cache.Room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    "user_entity",
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val userId: Int = -1,
    val username: String = "",
    val account: String = "用户名字",
    var avatarUrl: String = "注册默认头像",
    val bio: String = "",
    val description: String = "",
    val userLevel: Int = -1,
    val gender: Int = -1,
    val grade: String = "",
    val birthDate: String? = null,
    val location: String = "",
    val website: String? = "",
    val createTime: String? = null,
    val updateTime: String? = null,
    val isDeleted: Int = -1
)