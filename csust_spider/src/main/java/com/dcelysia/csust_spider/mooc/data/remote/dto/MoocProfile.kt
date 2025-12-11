package com.dcelysia.csust_spider.mooc.data.remote.dto

data class MoocProfile(
    val name: String, // 姓名
    val lastLoginTime: String, // 最后登录时间
    val totalOnlineTime: String, // 总在线时长
    val loginCount: Int // 登录次数
)