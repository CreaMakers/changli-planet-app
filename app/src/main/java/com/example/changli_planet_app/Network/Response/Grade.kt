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
    val pscj: String? = null,
    val pscjBL: String? = null,
    val qmcj: String? = null,
    val qmcjBL: String? = null,
    val qzcj: String? = null,
    val qzcjBL: String? = null,
    val sjcj: String? = null,
    val sjcjBL: String? = null,
)

data class GradeResponse(
    val code: String,
    val msg: String,
    val data: List<Grade>
)