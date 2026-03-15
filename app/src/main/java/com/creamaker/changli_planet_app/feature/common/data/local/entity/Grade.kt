package com.creamaker.changli_planet_app.feature.common.data.local.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Grade(
    @SerializedName("id")
    val id: String, //课程编号
    @SerializedName("item")
    val item: String,
    @SerializedName("name")
    val name: String, //课程名称
    @SerializedName("grade")
    val grade: String, //成绩
    @SerializedName("flag")
    val flag: String, //课程标识
    @SerializedName("score")
    val score: String, //学分
    @SerializedName("timeR")
    val timeR: String, //课程学时
    @SerializedName("point")
    val point: String, //绩点
    @SerializedName("ReItem")
    val upperReItem: String,
    @SerializedName("method")
    val method: String, //考核方式
    @SerializedName("property")
    val property: String,
    @SerializedName("attribute")
    val attribute: String, //课程属性
    @SerializedName("reItem")
    val reItem: String,
    @SerializedName("pscjUrl")
    val pscjUrl: String? = null, //详细成绩链接
)

@Keep
data class GradeResponse(
    val code: String,
    val msg: String,
    val data: List<Grade>
)