package com.example.changli_planet_app.feature.common.data.local.entity

import com.google.gson.annotations.SerializedName

data class Grade(
    val id: String,
    val item: String,
    val name: String,
    val grade: String,
    val flag: String,
    val score: String,
    val timeR: String,
    val point: String,
    @SerializedName("ReItem")
    val upperReItem: String,
    val method: String,
    val property: String,
    val attribute: String,
    val reItem: String,
    val pscjUrl: String? = null,
    val cookie: String? = null,
)

data class GradeResponse(
    val code: String,
    val msg: String,
    val data: List<Grade>
)