package com.example.changli_planet_app.Activity.State

import com.example.changli_planet_app.Network.Response.Grade

data class ScoreInquiryState (
    var showDataChosen: Boolean = false,
    var dataText: String = "",
    var grades: List<Grade> = mutableListOf()
)