package com.example.changli_planet_app

import android.app.Application
import android.util.Log
import com.example.changli_planet_app.Network.OkHttpHelper
import com.tencent.msdk.dns.DnsConfig
import com.tencent.msdk.dns.MSDKDnsResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PlanetApplication : Application() {
    companion object {
        // 双Token
        var accessToken: String? = null
        var refreshToken: String? = null
        var isLogin = false
        var isTourist = false
        const val ip: String = "https://www.baidu.com/"
    }
    override fun onCreate() {
        super.onCreate()
        val startTime = System.currentTimeMillis()
        // 配置HTTPDNS
        CoroutineScope(Dispatchers.IO).launch {
//             使用 async 启动并发任务
            val dnsConfigDeferred = async {
                val dnsConfigBuilder = DnsConfig.Builder()
                    .dnsId("98468")
                    .token("884069233")
                    .https()
                    .logLevel(Log.VERBOSE)
                    .build()
                MSDKDnsResolver.getInstance().init(applicationContext, dnsConfigBuilder)
            }
//            val mmkvDeferred = async {
//                MMKV.initialize(this@PlanetApplication)
//            }
            val httpPreRequestDeferred = async {
                // 进行HTTP预热
                OkHttpHelper.preRequest(ip)
            }
//             等待所有任务完成
            dnsConfigDeferred.await()
            httpPreRequestDeferred.await()
//            mmkvDeferred.await()
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            Log.d("YourTag", "onCreate 耗时: $duration ms")
        }
    }
}
