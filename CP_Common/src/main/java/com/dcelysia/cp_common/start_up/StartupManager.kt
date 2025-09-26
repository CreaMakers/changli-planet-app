package com.dcelysia.cp_common.start_up

import android.content.Context
import android.os.Looper
import androidx.core.os.TraceCompat
import com.dcelysia.cp_common.start_up.annotation.MultipleProcess
import com.dcelysia.cp_common.start_up.dispatcher.StartupManagerDispatcher
import com.dcelysia.cp_common.start_up.execption.StartupException
import com.dcelysia.cp_common.start_up.manager.StartupCacheManager
import com.dcelysia.cp_common.start_up.model.LoggerLevel
import com.dcelysia.cp_common.start_up.model.StartupConfig
import com.dcelysia.cp_common.start_up.model.StartupSortStore
import com.dcelysia.cp_common.start_up.sort.TopologySort
import com.dcelysia.cp_common.start_up.utils.ProcessUtils
import com.dcelysia.cp_common.start_up.utils.StartupCostTimesUtils
import com.dcelysia.cp_common.start_up.utils.StartupLogUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class StartupManager private constructor(
    private val context: Context,
    private val startupList: List<AndroidStartup<*>>,
    private val needAwaitCount: AtomicInteger,
    private val config: StartupConfig
) {

    private var mAwaitCountDownLatch: CountDownLatch? = null

    companion object {
        const val AWAIT_TIMEOUT = 10000L
    }

    init {
        // save initialized config
        StartupCacheManager.instance.saveConfig(config)
        StartupLogUtils.level = config.loggerLevel
    }

    fun start() = apply {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw StartupException("start method must be call in MainThread.")
        }

        if (mAwaitCountDownLatch != null) {
            throw StartupException("start method repeated call.")
        }
        mAwaitCountDownLatch = CountDownLatch(needAwaitCount.get())

        if (startupList.isEmpty()) {
            StartupLogUtils.e { "startupList is empty in the current process." }
            return@apply
        }

        TraceCompat.beginSection(StartupManager::class.java.simpleName)
        StartupCostTimesUtils.startTime = System.nanoTime()

        TopologySort.sort(startupList).run {
            mDefaultManagerDispatcher.prepare()
            execute(this)
        }

        if (needAwaitCount.get() <= 0) {
            StartupCostTimesUtils.endTime = System.nanoTime()
            TraceCompat.endSection()
        }
    }

    private fun execute(sortStore: StartupSortStore) {
        sortStore.result.forEach { mDefaultManagerDispatcher.dispatch(it, sortStore) }
    }

    /**
     * Startup dispatcher
     */
    private val mDefaultManagerDispatcher by lazy {
        StartupManagerDispatcher(
            context,
            needAwaitCount,
            mAwaitCountDownLatch,
            startupList.size,
            config.listener
        )
    }

    /**
     * to await startup completed
     * block main thread.
     */
    fun await() {
        if (mAwaitCountDownLatch == null) {
            throw StartupException("must be call start method before call await method.")
        }

        val count = needAwaitCount.get()
        try {
            mAwaitCountDownLatch?.await(config.awaitTimeout, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        if (count > 0) {
            StartupCostTimesUtils.endTime = System.nanoTime()
            TraceCompat.endSection()
        }
    }

    class Builder {
        private var mStartupList = mutableListOf<AndroidStartup<*>>()
        private var mNeedAwaitCount = AtomicInteger()
        private var mLoggerLevel = LoggerLevel.NONE
        private var mAwaitTimeout = AWAIT_TIMEOUT
        private var mConfig: StartupConfig? = null

        fun addStartup(startup: AndroidStartup<*>) = apply {
            mStartupList.add(startup)
        }

        fun addAllStartup(list: List<AndroidStartup<*>>) = apply {
            list.forEach {
                addStartup(it)
            }
        }

        fun setConfig(config: StartupConfig?) = apply {
            mConfig = config
        }

        @Deprecated("Use setConfig() instead.")
        fun setLoggerLevel(level: LoggerLevel) = apply {
            mLoggerLevel = level
        }

        @Deprecated("Use setConfig() instead.")
        fun setAwaitTimeout(timeoutMilliSeconds: Long) = apply {
            mAwaitTimeout = timeoutMilliSeconds
        }

        fun build(context: Context): StartupManager {
            val realStartupList = mutableListOf<AndroidStartup<*>>()
            mStartupList.forEach {
                val process =
                    it::class.java.getAnnotation(MultipleProcess::class.java)?.process ?: arrayOf()
                if (process.isEmpty() || ProcessUtils.isMultipleProcess(context, process)) {
                    realStartupList.add(it)
                    if (it.waitOnMainThread() && !it.callCreateOnMainThread()) {
                        mNeedAwaitCount.incrementAndGet()
                    }
                }
            }

            return StartupManager(
                context,
                realStartupList,
                mNeedAwaitCount,
                mConfig ?: StartupConfig.Builder()
                    .setLoggerLevel(mLoggerLevel)
                    .setAwaitTimeout(mAwaitTimeout)
                    .build()
            )
        }
    }

}