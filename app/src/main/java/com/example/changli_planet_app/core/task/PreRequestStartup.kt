package com.example.changli_planet_app.core.task

import android.content.Context
import android.util.Log
import com.dcelysia.cp_common.start_up.AndroidStartup
import com.dcelysia.cp_common.start_up.Startup
import com.example.changli_planet_app.core.network.OkHttpHelper

class PreRequestStartup : AndroidStartup<Unit>() {

    private val preRequestIps = listOf(
        "http://113.44.47.220:8083",
        "http://113.44.47.220:8081"
    )

    override fun create(context: Context): Unit {
        try {
            preRequestIps.forEach { url ->
                OkHttpHelper.preRequest(url)
            }
        } catch (e: Exception) {
            Log.e("PreRequestStartup", "Pre-request failed", e)
        }
    }

    override fun dependencies(): List<Class<out Startup<*>>>? =
        listOf(MMKVStartup::class.java)

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false
}