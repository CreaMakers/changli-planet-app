package com.dcelysia.csust_spider.education.data.remote.model

data class GradeComponent(
    val type: String, //成绩类型
    val grade: Double, //成绩
    val ratio: Int //成绩比例
)