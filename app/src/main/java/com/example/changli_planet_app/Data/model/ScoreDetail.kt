package com.example.changli_planet_app.Data.model

data class ScoreDetail(
    val pscj: String? = null,      // 平时成绩
    val pscjBL: String? = null,    // 平时成绩比例
    val qmcj: String? = null,      // 期末成绩
    val qmcjBL: String? = null,    // 期末成绩比例
    val qzcj: String? = null,      // 期中成绩
    val qzcjBL: String? = null,    // 期中成绩比例
    val score: String? = null,      // 总成绩
    val sjcj: String? = null,      // 上机成绩
    val sjcjBL: String? = null     // 上机成绩比例
)

data class ScoreDetailResponse(
    val code: String,
    val msg: String,
    val data: ScoreDetail
)