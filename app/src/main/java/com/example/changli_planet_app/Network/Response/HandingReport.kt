package com.example.changli_planet_app.Network.Response

data class HandingReport(
    val post_id: Int,
    val process_description: String,
    val report_id: Int,
    val status: Int,
    val update_time: String
)