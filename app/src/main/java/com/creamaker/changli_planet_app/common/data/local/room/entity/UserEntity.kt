package com.creamaker.changli_planet_app.common.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    "user_entity",
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserEntity(
    @PrimaryKey
    var userId: Int = 0,
    var username: String = "",
    var account: String = "用户名字",
    var avatarUrl: String = "注册默认头像",
    var bio: String = "",
    var description: String = "",
    var userLevel: Int = -1,
    var gender: Int = -1,
    var grade: String = "",
    var birthDate: String? = null,
    var location: String = "",
    var website: String? = null,
    var createTime: String? = null,
    var updateTime: String? = null,
    @ColumnInfo(name = "isDeleted")
    var deleted: Int = -1,
    var cacheTime: Long = System.currentTimeMillis()
)