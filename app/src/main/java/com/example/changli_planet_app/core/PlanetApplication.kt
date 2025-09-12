package com.example.changli_planet_app.core

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.changli_planet_app.BuildConfig
import com.example.changli_planet_app.core.network.OkHttpHelper
import com.example.changli_planet_app.feature.common.data.local.room.database.CoursesDataBase
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.mmkv.MMKV
import com.tencent.msdk.dns.DnsConfig
import com.tencent.msdk.dns.MSDKDnsResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlanetApplication : Application() {
    companion object {
        private const val TIME_TABLE_APP_WIDGET = "TimeTableAppWidget"
        
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
                MMKV.mmkvWithID(TIME_TABLE_APP_WIDGET).clearAll()
                CoursesDataBase.getDatabase(appContext).courseDao().clearAllCourses()
            }
        }
        fun clearSchoolDataCacheAll(){
            CoroutineScope(Dispatchers.IO).launch {
                MMKV.mmkvWithID("import_cache").clearAll()
                MMKV.mmkvWithID("content_cache").clearAll()
                MMKV.mmkvWithID(TIME_TABLE_APP_WIDGET).clearAll()
                CoursesDataBase.getDatabase(appContext).courseDao().clearAllCourses()
            }
        }

        fun clearContentCache() {
            CoroutineScope(Dispatchers.IO).launch {
                MMKV.mmkvWithID("content_cache").clearAll()
                CoursesDataBase.getDatabase(appContext).courseDao().clearAllCourses()
            }
        }

        fun getSystemDeviceId(): String {
            val androidId =
                Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
            return when {
                androidId.isNullOrEmpty() -> "unknown_device"
                androidId == "9774d56d682e549c" -> "emulator_device"
                else -> androidId
            }
        }
    }
    private val fpsHandlerThread = HandlerThread("fpsHandlerThread").apply { start() }
    private val fpsHandler by lazy(LazyThreadSafetyMode.NONE) { Handler(fpsHandlerThread.looper) }

    override fun onCreate() {
        super.onCreate()
        val startTime = System.currentTimeMillis()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initMMKV()
        if (!BuildConfig.DEBUG) {
            CrashReport.initCrashReport(applicationContext, "1c79201ce5", true)
        }
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { initDNS() }.onFailure { Log.e("DNS", "DNS, Error") }
            runCatching { preRequestIps.forEach { OkHttpHelper.preRequest(it) } }.onFailure {
                Log.e(
                    "PreRequestIps",
                    "PreRequestIps, Error"
                )
            }
        }
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        if (BuildConfig.DEBUG) {
            startMemoryMonitor()
        }
        appContext = applicationContext
    }
    private fun startMemoryMonitor() {
        fpsHandler.post(object : Runnable {
            override fun run() {
                val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memoryInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)

                val debugMemoryInfo = Debug.MemoryInfo()
                Debug.getMemoryInfo(debugMemoryInfo)

                val logString = "系统内存可用 ${memoryInfo.availMem shr 20}MB /总内存 ${memoryInfo.totalMem shr 20}MB " +
                        "java内存 ${debugMemoryInfo.dalvikPss shr 10}MB native内存 ${debugMemoryInfo.nativePss shr 10}MB"
                Log.v("Memory", logString)
                fpsHandler.postDelayed(this, 2000)
            }
        })
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


//@SuppressLint("StaticFieldLeak")
//object CrashHandler : Thread.UncaughtExceptionHandler {
//
//    private lateinit var myContext: Context
//    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
//
//    fun init(context: Context) {
//        this.myContext = context.applicationContext
//        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
//        Thread.setDefaultUncaughtExceptionHandler(this)
//    }
//
//    override fun uncaughtException(t: Thread, e: Throwable) {
//        try {
//            // 记录崩溃日志 可加入后端
//            Log.e("CrashHandler", "App crashed: ", e)
//
//            val intent = myContext.packageManager.getLaunchIntentForPackage(myContext.packageName)
//            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//
//            val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
//                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//            } else {
//                PendingIntent.FLAG_ONE_SHOT
//            }
//
//            val pendingIntent = PendingIntent.getActivity(
//                myContext,
//                0,
//                intent,
//                flags
//            )
//
//            val alarmManager = myContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, pendingIntent)
//        } catch (e: Exception) {
//            Log.e("CrashHandler", "Error in crash handler", e)
//        } finally {
//            Process.killProcess(Process.myPid())
//            exitProcess(1)
//        }
//    }
//}
