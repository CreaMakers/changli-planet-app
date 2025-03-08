package com.example.changli_planet_app.Activity.State

import com.example.changli_planet_app.Data.model.ScoreDetail
import com.example.changli_planet_app.Network.Response.Grade

data class ScoreInquiryState (
    var grades: List<Grade> = mutableListOf()
)