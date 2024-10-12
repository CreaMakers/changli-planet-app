package com.example.changli_planet_app

import android.app.Application
import com.example.changli_planet_app.Network.OkHttpHelper

class PlanetApplication:Application() {
    companion object{
        var accessToken:String ?= null
        var refreshToken:String ?= null
    }
    override fun onCreate() {
        super.onCreate()
        OkHttpHelper.preRequest("My_Url")
    }
}