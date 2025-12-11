package com.dcelysia.csust_spider.mooc.data.remote.dto

data class MoocHomework(
    val id: Int,
    val title: String,
    val publisher: String,
    val canSubmit: Boolean,
    val submitStatus: Boolean,
    val deadline: String,
    val startTime: String
)
