package com.creamaker.changli_planet_app.feature.mooc.data.remote.dto

import androidx.annotation.Keep

@Keep
data class MoocHomeworkItem(
    val realName: String,
    val startDateTime: String,
    val mutualTask: String,
    val submitStruts: Boolean,
    val id: Int,
    val title: String,
    val deadLine: String,
    val answerStatus: Boolean?
)

@Keep
data class MoocHomeworkResponse(
    val datas: MoocHomeworkDatas
)

@Keep
data class MoocHomeworkDatas(
    val hwtList: List<MoocHomeworkItem>?
)