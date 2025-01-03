package com.example.changli_planet_app.Core
import android.app.Application
import android.content.Context
import android.util.Log
import com.tencent.mmkv.MMKV
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
        var startTime: Long = 0
        var isLogin = false
        lateinit var appContext : Context
        const val UserIp: String = "http://113.44.47.220:8083/app/users"
        const val ToolIp: String = "http://113.44.47.220:8081/app/tools"
    }
    override fun onCreate() {
        super.onCreate()
        val startTime = System.currentTimeMillis()
//        // 配置HTTPDNS
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
            appContext = applicationContext
            val mmkvDeferred = async {
                MMKV.initialize(this@PlanetApplication)
            }
//            val httpPreRequestDeferred = async {
//                // 进行HTTP预热
//                OkHttpHelper.preRequest(ToolIp + "")
//            }
//             等待所有任务完成
//            dnsConfigDeferred.await()
//            httpPreRequestDeferred.await()
            mmkvDeferred.await()
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            Log.d("YourTag", "onCreate 耗时: $duration ms")
        }
    }
}
