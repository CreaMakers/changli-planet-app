package com.dcelysia.csust_spider.education.data.remote.model

data class Course(
    val courseName: String,
    val teacher: String,
    //周次（节次）
    val weeks: String,
    val classroom: String,
    val weekday: String
)
