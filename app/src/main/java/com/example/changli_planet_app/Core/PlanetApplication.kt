package com.example.changli_planet_app.Core

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.changli_planet_app.Cache.Room.database.CoursesDataBase
import com.example.changli_planet_app.Network.OkHttpHelper
import com.tencent.mmkv.MMKV
import com.tencent.msdk.dns.DnsConfig
import com.tencent.msdk.dns.MSDKDnsResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlanetApplication : Application() {
    companion object {
        var accessToken: String?
            get() = MMKV.defaultMMKV()?.getString("token", null)
            set(value) {
                MMKV.defaultMMKV()?.putString("token", value)
            }
        var startTime: Long = 0
        var deviceId: String = ""
        lateinit var appContext: Context

        const val UserIp: String = "http://113.44.47.220:8083/app/users"
        const val ToolIp: String = "http://113.44.47.220:8081/app/tools"
        const val FreshNewsIp: String = "http://113.44.47.220:8085/app/fresh_news"
//        const val ToolIp: String = "http://10.0.2.2:8081/app/tools"

        val preRequestIps = listOf(
            "http://113.44.47.220:8083",
            "http://113.44.47.220:8081"
        )

        fun clearCacheAll() {
            CoroutineScope(Dispatchers.IO).launch {
                accessToken = ""
                MMKV.mmkvWithID("import_cache").clearAll()
                MMKV.mmkvWithID("content_cache").clearAll()
                CoursesDataBase.getDatabase(appContext).courseDao().clearAllCourses()
            }
        }

        fun clearContentCache() {
            CoroutineScope(Dispatchers.IO).launch {
                MMKV.mmkvWithID("content_cache").clearAll()
                CoursesDataBase.getDatabase(appContext).courseDao().clearAllCourses()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val startTime = System.currentTimeMillis()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initMMKV()

        CoroutineScope(Dispatchers.IO).launch {
            runCatching { initDNS() }.onFailure { Log.e("DNS", "DNS, Error") }
            runCatching { initMMKV() }.onFailure { Log.e("MMKV", "MMKV init Error") }
            runCatching { preRequestIps.forEach { OkHttpHelper.preRequest(it) } }.onFailure {
                Log.e(
                    "PreRequestIps",
                    "PreRequestIps, Error"
                )
            }
        }
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        appContext = applicationContext
    }

    private fun initDNS() {
        val dnsConfigBuilder = DnsConfig.Builder()
            .dnsId("98468")
            .token("884069233")
            .https()
            .logLevel(Log.VERBOSE)
            .build()
        MSDKDnsResolver.getInstance().init(applicationContext, dnsConfigBuilder)
    }

    private fun initMMKV() {
        MMKV.initialize(this@PlanetApplication)
    }
}

