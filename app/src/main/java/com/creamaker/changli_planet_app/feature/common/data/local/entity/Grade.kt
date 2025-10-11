package com.creamaker.changli_planet_app.feature.common.data.local.entity

import com.google.gson.annotations.SerializedName

data class Grade(
    val id: String, //课程编号
    val item: String,
    val name: String, //课程名称
    val grade: String, //成绩
    val flag: String, //课程标识
    val score: String, //学分
    val timeR: String, //课程学时
    val point: String, //绩点
    @SerializedName("ReItem")
    val upperReItem: String,
    val method: String, //考核方式
    val property: String,
    val attribute: String, //课程属性
    val reItem: String,
    val pscjUrl: String? = null, //详细成绩链接
    val cookie: String? = null,
)

data class GradeResponse(
    val code: String,
    val msg: String,
    val data: List<Grade>
)