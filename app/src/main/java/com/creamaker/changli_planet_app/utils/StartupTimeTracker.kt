package com.creamaker.changli_planet_app.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean

object StartupTimeTracker : Application.ActivityLifecycleCallbacks, LifecycleEventObserver {
    private const val TAG = "StartupTimeTracker"
    private var appStartTime = 0L
    private var firstActivityCreateTime = 0L
    private var firstActivityStartTime = 0L
    private var firstActivityResumeTime = 0L
    private var firstFrameDrawnTime = 0L
    private val isFirstActivity = AtomicBoolean(true)

    private val startupMetrics = mutableMapOf<String, Long>()

    fun initialize(application: Application) {
        try {
            appStartTime = System.currentTimeMillis()
            startupMetrics["app_start"] = appStartTime

            application.registerActivityLifecycleCallbacks(this)
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)

            Log.i("StartupTracker", "StartupTimeTracker initialized at $appStartTime")
        } catch (e: Exception) {
            Log.e("StartupTracker", "Error initializing StartupTimeTracker", e)
        }
    }

    fun recordMetric(name: String, time: Long = System.currentTimeMillis()) {
        startupMetrics[name] = time
        Log.d("StartupMetric", "$name: ${time - appStartTime}ms from app start")
    }

    // 处理应用级别的生命周期事件
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d("StartupTracker", "Process lifecycle event: $event")
        // 这里只处理应用级别的事件，不处理Activity相关的逻辑
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d("StartupTracker", "onActivityCreated: ${activity.javaClass.simpleName}")
        if (isFirstActivity.get()) {
            firstActivityCreateTime = System.currentTimeMillis()
            recordMetric("first_activity_create", firstActivityCreateTime)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        Log.d("StartupTracker", "onActivityStarted: ${activity.javaClass.simpleName}")
        if (isFirstActivity.get()) {
            firstActivityStartTime = System.currentTimeMillis()
            recordMetric("first_activity_start", firstActivityStartTime)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d("StartupTracker", "onActivityResumed: ${activity.javaClass.simpleName}")
        if (isFirstActivity.get()) {
            firstActivityResumeTime = System.currentTimeMillis()
            recordMetric("first_activity_resume", firstActivityResumeTime)

            // 标记不再是第一个Activity
            isFirstActivity.set(false)

            // 等待下一帧绘制完成
            try {
                Handler(Looper.getMainLooper()).post {
                    firstFrameDrawnTime = System.currentTimeMillis()
                    recordMetric("first_frame_drawn", firstFrameDrawnTime)
                    logStartupReport()
                }
            } catch (e: Exception) {
                Log.e("StartupTracker", "Error posting frame draw callback", e)
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d("StartupTracker", "onActivityPaused: ${activity.javaClass.simpleName}")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d("StartupTracker", "onActivityStopped: ${activity.javaClass.simpleName}")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        Log.d("StartupTracker", "onActivityDestroyed: ${activity.javaClass.simpleName}")
    }

    private fun logStartupReport() {
        try {
            val coldStartTime = firstFrameDrawnTime - appStartTime
            val applicationTime = firstActivityCreateTime - appStartTime
            val activityCreateTime = firstActivityStartTime - firstActivityCreateTime
            val activityResumeTime = firstActivityResumeTime - firstActivityStartTime
            val frameDrawTime = firstFrameDrawnTime - firstActivityResumeTime

            Log.i("StartupReport", "════════ 启动性能报告 ════════")
            Log.i("StartupReport", "冷启动总时间: ${coldStartTime}ms")
            Log.i("StartupReport", "Application初始化: ${applicationTime}ms")
            Log.i("StartupReport", "首个Activity创建: ${activityCreateTime}ms")
            Log.i("StartupReport", "Activity启动到Resume: ${activityResumeTime}ms")
            Log.i("StartupReport", "首帧绘制完成: ${frameDrawTime}ms")
            Log.i("StartupReport", "═══════════════════════════")

            // 详细指标
            startupMetrics.entries.sortedBy { it.value }.forEach { (name, time) ->
                Log.d("StartupMetrics", "$name: ${time - appStartTime}ms")
            }

            // 性能评级
            val rating = when {
                coldStartTime <= 500 -> "优秀"
                coldStartTime <= 1000 -> "良好"
                coldStartTime <= 2000 -> "一般"
                else -> "需要优化"
            }
            Log.i("StartupReport", "启动性能评级: $rating")
        } catch (e: Exception) {
            Log.e("StartupTracker", "Error generating startup report", e)
        }
    }

    fun getStartupTime(): Long =
        if (firstFrameDrawnTime > 0) firstFrameDrawnTime - appStartTime else -1

    fun getMetrics(): Map<String, Long> = startupMetrics.toMap()
}