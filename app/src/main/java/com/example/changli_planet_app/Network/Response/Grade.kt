package com.example.changli_planet_app.Network.Response

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
    val pscj: String?,
    val pscjBL: String?,
    val qmcjBL: String?,
    val qzcj: String?,
    val sjcj: String?,
    val sjcjBL: String?,
)

data class GradeResponse(
    val code: String,
    val msg: String,
    val data: List<Grade>
)