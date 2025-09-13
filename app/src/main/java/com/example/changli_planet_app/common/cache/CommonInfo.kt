package com.example.changli_planet_app.common.cache

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CommonInfo {
    val termMap  =
        mapOf(
            "2025-2026-1" to "2025-09-08 00:00:00",
            "2024-2025-2" to "2025-02-24 00:00:00",
            "2024-2025-1" to "2024-09-02 00:00:00",
            "2023-2024-2" to "2024-02-26 00:00:00",
            "2023-2024-1" to "2023-09-04 00:00:00",
            "2022-2023-2" to "2023-02-20 00:00:00",
            "2022-2023-1" to "2022-08-29 00:00:00",
            "2021-2022-2" to "2022-02-21 00:00:00",
            "2021-2022-1" to "2021-09-06 00:00:00",
            "2020-2021-2" to "2021-03-01 00:00:00",
            "2020-2021-1" to "2020-08-24 00:00:00",
            "2019-2020-2" to "2020-02-17 00:00:00",
            "2019-2020-1" to "2019-09-02 00:00:00",
        )

    var startTime = 0L

    /**
     * 获取当前是开学第几周
     * @param currentTerm 当前学期，如 "2024-2025-1"
     * @return 返回周数文本，如 "第 21 周"，如果学期不存在返回 "未知"
     */
    fun getCurrentWeek(currentTerm: String): String {
        val startTimeStr = termMap[currentTerm]
        if (startTimeStr == null) {
            return "未知"
        }

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val startDate = sdf.parse(startTimeStr)
            val currentDate = Date()

            if (startDate == null || currentDate.before(startDate)) {
                return "未开学"
            }

            // 计算相差的天数
            val diffInMillis = currentDate.time - startDate.time
            val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

            // 计算周数（开学第一天算第1周）
            val weekNumber = (diffInDays / 7) + 1

            return "第 $weekNumber 周"
        } catch (e: Exception) {
            return "计算错误"
        }
    }

    /**
     * 自动获取当前学期的周数
     * @return 返回当前学期的周数文本
     */
    fun getCurrentWeekAuto(): String {
        val currentDate = Date()

        // 按时间顺序查找当前应该是哪个学期
        val sortedTerms = termMap.entries.sortedByDescending { entry ->
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                sdf.parse(entry.value)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }

        for (term in sortedTerms) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val startDate = sdf.parse(term.value)

                if (startDate != null && !currentDate.before(startDate)) {
                    return getCurrentWeek(term.key)
                }
            } catch (e: Exception) {
                continue
            }
        }

        return "未开学"
    }
}