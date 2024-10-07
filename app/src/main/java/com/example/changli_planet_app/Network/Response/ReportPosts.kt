package com.example.changli_planet_app.Network.Response

data class ReportPosts(
    val post_id: Int,
    val reason: String,
    val report_id: Int,
    val report_time: String,
    val reporter_id: Int
)