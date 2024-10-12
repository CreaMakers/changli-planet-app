package com.example.changli_planet_app
import android.app.Application
import android.util.Log
import com.example.changli_planet_app.Network.OkHttpHelper
import com.tencent.msdk.dns.DnsConfig
import com.tencent.msdk.dns.MSDKDnsResolver
class PlanetApplication:Application() {
    companion object{
        var accessToken:String ?= null
        var refreshToken:String ?= null
    }
    override fun onCreate() {
        super.onCreate()
        val dnsConfigBuilder = DnsConfig.Builder()
            .dnsId("98468")
            .token("884069233")
            .https() // (Optional) Log granularity, if debug mode is enabled, pass in "Log.VERBOSE".
            .logLevel(Log.VERBOSE)
            .build()
        MSDKDnsResolver.getInstance().init(this, dnsConfigBuilder)
        //OkHttpHelper.preRequest("My_Url")
    }
}