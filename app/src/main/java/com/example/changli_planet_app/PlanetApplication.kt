package com.example.changli_planet_app

import android.app.Application

class PlanetApplication:Application() {
    companion object{
        var accessToken:String ?= null
        var refreshToken:String ?= null
        var isRefresh:Boolean = false
    }
}