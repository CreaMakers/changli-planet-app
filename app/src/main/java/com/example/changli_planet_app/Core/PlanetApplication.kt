package com.example.changli_planet_app.Core

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.changli_planet_app.Cache.Room.CoursesDataBase
import com.example.changli_planet_app.Cache.ScoreCache
import com.example.changli_planet_app.Cache.StudentInfoManager
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.R
import com.tencent.mmkv.MMKV
import com.tencent.msdk.dns.DnsConfig
import com.tencent.msdk.dns.MSDKDnsResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlanetApplication : Application() {
    companion object {
        var accessToken: String?
            get() = MMKV.defaultMMKV()?.getString("token", null)
            set(value) {
                MMKV.defaultMMKV()?.putString("token", value)
            }
        var startTime: Long = 0
        var isLogin = false
        var deviceId: String = ""
        lateinit var appContext: Context

        //        const val UserIp: String = "http://10.0.2.2:8083/app/users"
        const val UserIp: String = "http://113.44.47.220:8083/app/users"
        const val ToolIp: String = "http://113.44.47.220:8081/app/tools"

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
            // 并发执行所有初始化任务
            val tasks = listOf(
                async { initDNS() },
                async {
                    // https预热
                    preRequestIps.forEach {
                        OkHttpHelper.preRequest(it)
                    }
                }
            )

            // 等待所有任务完成
            tasks.awaitAll()

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            Log.d("YourTag", "onCreate 耗时: $duration ms")
        }

        appContext = applicationContext
    }

    private suspend fun initDNS() {
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

    private suspend fun preloadImages() {
        // 预加载所有固定图标
        val iconResources = listOf(
            R.drawable.planet_logo,
            R.drawable.ngrade,
            R.drawable.ncourse,
            R.drawable.nmap,
            R.drawable.ncet,
            R.drawable.ntest,
            R.drawable.ncalender,
            R.drawable.nadd,
            R.drawable.nmande,
            R.drawable.nlose,
            R.drawable.nnotice,
            R.drawable.nelectronic,
            R.drawable.nrank,
            R.drawable.nbalance,
            R.drawable.nclassroom
        )

        // Glide 需要在主线程初始化
        withContext(Dispatchers.Main) {
            iconResources.chunked(4) {
                it.forEach { resId ->
                    Glide.with(applicationContext)
                        .load(resId)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .preload()
                }
            }
        }
    }


}

