package com.zhelearn.CSUSTPlanet.core.task


import android.content.Context
import com.dcelysia.cp_common.start_up.AndroidStartup
import com.dcelysia.cp_common.start_up.Startup
import com.zhelearn.CSUSTPlanet.BuildConfig
import com.tencent.bugly.crashreport.CrashReport

class CrashReportStartup : AndroidStartup<Unit>() {

    override fun create(context: Context): Unit {
        if (!BuildConfig.DEBUG) {
            CrashReport.initCrashReport(context, BuildConfig.BUGLY_APP_ID, true)
        }
    }

    override fun dependencies(): List<Class<out Startup<*>>>? = null

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false
}
