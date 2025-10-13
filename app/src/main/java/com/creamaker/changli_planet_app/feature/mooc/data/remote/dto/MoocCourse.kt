package com.creamaker.changli_planet_app.feature.mooc.data.remote.dto

data class MoocCourse(
    val id: String,
    val number: String,
    val name: String,
    val department: String,
    val teacher: String
)

data class PendingAssignmentCourse(
    val id: String,
    val name: String
)