package com.example.changli_planet_app.Activity.State

import com.example.changli_planet_app.Network.Response.Grade

data class ScoreInquiryState (
    var grades: List<Grade> = mutableListOf()
)