package com.creamaker.changli_planet_app.feature.common.redux.state

import com.creamaker.changli_planet_app.feature.common.data.local.entity.Grade

data class ScoreInquiryState (
    var grades: List<Grade> = mutableListOf()
)