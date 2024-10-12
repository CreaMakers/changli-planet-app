package com.example.changli_planet_app.Network.Response

data class ReportedPosts(
    val post_id: Int,
    val process_description: Any,
    val reason: String,
    val report_id: Int,
    val report_time: String,
    val reporter_id: Int,
    val status: Int
)