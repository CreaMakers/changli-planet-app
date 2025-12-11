package com.dcelysia.csust_spider.mooc.data.remote.dto

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

data class MoocHomeworkResponse(
    val datas: MoocHomeworkDatas
)

data class MoocHomeworkDatas(
    val hwtList: List<MoocHomeworkItem>?
)