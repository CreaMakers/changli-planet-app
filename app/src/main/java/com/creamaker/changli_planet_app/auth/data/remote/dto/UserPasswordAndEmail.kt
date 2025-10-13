package com.creamaker.changli_planet_app.auth.data.remote.dto

data class UserPasswordAndEmail (
    val username: String,
    val password: String,
    val email:String,
    val verifyCode:String
)