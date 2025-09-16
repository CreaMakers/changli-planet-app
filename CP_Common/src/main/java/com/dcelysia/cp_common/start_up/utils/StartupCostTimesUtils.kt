package com.dcelysia.cp_common.start_up.utils

import com.dcelysia.cp_common.start_up.Startup
import com.dcelysia.cp_common.start_up.extensions.getUniqueKey
import com.dcelysia.cp_common.start_up.manager.StartupCacheManager
import com.dcelysia.cp_common.start_up.model.CostTimesModel
import java.util.concurrent.ConcurrentHashMap

internal object StartupCostTimesUtils {

    val costTimesMap = ConcurrentHashMap<String, CostTimesModel>()

    private const val ACCURACY = 1000 * 1000L

    var startTime = 0L
    var endTime: Long? = null

    val mainThreadTimes
        get() = (endTime ?: System.nanoTime()) - startTime

    fun recordStart(block: () -> Triple<Class<out Startup<*>>, Boolean, Boolean>) {
        if (checkOpenStatistics()) {
            block().run {
                costTimesMap[first.getUniqueKey()] = CostTimesModel(
                    first.simpleName,
                    second,
                    third,
                    System.nanoTime() / ACCURACY
                )
            }
        }
    }

    fun recordEnd(block: () -> Class<out Startup<*>>) {
        if (checkOpenStatistics()) {
            costTimesMap[block().getUniqueKey()]?.let {
                it.endTime = System.nanoTime() / ACCURACY
            }
        }
    }

    fun clear() {
        if (checkOpenStatistics()) {
            endTime = null
            costTimesMap.clear()
        }
    }

    fun printAll() {
        StartupLogUtils.d {
            buildString {
                append("startup cost times detail:")
                append("\n")
                append("|=================================================================")
                costTimesMap.values.forEach {
                    append("\n")
                    append("|      Startup Name       |   ${it.name}")
                    append("\n")
                    append("| ----------------------- | --------------------------------------")
                    append("\n")
                    append("|   Call On Main Thread   |   ${it.callOnMainThread}")
                    append("\n")
                    append("| ----------------------- | --------------------------------------")
                    append("\n")
                    append("|   Wait On Main Thread   |   ${it.waitOnMainThread}")
                    append("\n")
                    append("| ----------------------- | --------------------------------------")
                    append("\n")
                    append("|       Cost Times        |   ${it.endTime - it.startTime} ms")
                    append("\n")
                    append("|=================================================================")
                }
                append("\n")
                append("| Total Main Thread Times |   ${mainThreadTimes / ACCURACY} ms")
                append("\n")
                append("|=================================================================")
            }
        }
    }

    private fun checkOpenStatistics() =
        StartupCacheManager.instance.initializedConfig?.openStatistic == true
}