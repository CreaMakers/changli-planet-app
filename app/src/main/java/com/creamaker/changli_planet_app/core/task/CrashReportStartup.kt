package com.creamaker.changli_planet_app.core.task


import android.content.Context
import com.dcelysia.cp_common.start_up.AndroidStartup
import com.dcelysia.cp_common.start_up.Startup
import com.creamaker.changli_planet_app.BuildConfig
import com.tencent.bugly.crashreport.CrashReport

class CrashReportStartup : AndroidStartup<Unit>() {

    override fun create(context: Context): Unit {
        if (!BuildConfig.DEBUG) {
            CrashReport.initCrashReport(context, "1c79201ce5", true)
        }
    }

    override fun dependencies(): List<Class<out Startup<*>>>? = null

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false
}