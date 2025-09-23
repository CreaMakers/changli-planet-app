package com.dcelysia.cp_common.start_up.run

import android.content.Context
import android.os.Process
import com.dcelysia.cp_common.start_up.Startup
import com.dcelysia.cp_common.start_up.annotation.ThreadPriority
import com.dcelysia.cp_common.start_up.dispatcher.ManagerDispatcher
import com.dcelysia.cp_common.start_up.manager.StartupCacheManager
import com.dcelysia.cp_common.start_up.model.ResultModel
import com.dcelysia.cp_common.start_up.model.StartupSortStore
import com.dcelysia.cp_common.start_up.utils.StartupCostTimesUtils
import com.dcelysia.cp_common.start_up.utils.StartupLogUtils

internal class StartupRunnable(
    private val context: Context,
    private val startup: Startup<*>,
    private val sortStore: StartupSortStore,
    private val dispatcher: ManagerDispatcher
) : Runnable {

    override fun run() {
        Process.setThreadPriority(
            startup::class.java.getAnnotation(ThreadPriority::class.java)?.priority
                ?: Process.THREAD_PRIORITY_DEFAULT
        )
        startup.toWait()
        StartupLogUtils.d { "${startup::class.java.simpleName} being create." }
        StartupCostTimesUtils.recordStart {
            Triple(
                startup::class.java,
                startup.callCreateOnMainThread(),
                startup.waitOnMainThread()
            )
        }
        val result = startup.create(context)
        StartupCostTimesUtils.recordEnd { startup::class.java }
        // To save result of initialized component.
        StartupCacheManager.instance.saveInitializedComponent(
            startup::class.java,
            ResultModel(result)
        )
        StartupLogUtils.d { "${startup::class.java.simpleName} was completed." }

        dispatcher.notifyChildren(startup, result, sortStore)
    }
}