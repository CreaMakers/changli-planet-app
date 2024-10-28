package com.example.changli_planet_app
import android.app.Application
import android.content.pm.ActivityInfo
import android.util.Log
import com.example.changli_planet_app.Network.OkHttpHelper
import com.tencent.mmkv.MMKV
import com.tencent.msdk.dns.DnsConfig
import com.tencent.msdk.dns.MSDKDnsResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlanetApplication:Application() {
    companion object{
        //双Token
        var accessToken:String ?= null
        var refreshToken:String ?= null
        const val ip:String = "https://www.baidu.com/"
    }
    override fun onCreate() {
        super.onCreate()
        val startTime = System.currentTimeMillis()
        //配置HTTPDNS
        CoroutineScope(Dispatchers.IO).launch {
            val dnsConfigBuilder = DnsConfig.Builder()
                .dnsId("98468")
                .token("884069233")
                .https() // (Optional) Log granularity, if debug mode is enabled, pass in "Log.VERBOSE".
                .logLevel(Log.VERBOSE)
                .build()
            MSDKDnsResolver.getInstance().init(applicationContext, dnsConfigBuilder)
            //初始化MMKV
//        val point = MMKV.initialize(this)
//        println("mmkv root :$point")
            //进行HTTP预热
            OkHttpHelper.preRequest(ip)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            Log.d("YourTag", "onCreate 耗时: $duration ms")
        }
    }
}