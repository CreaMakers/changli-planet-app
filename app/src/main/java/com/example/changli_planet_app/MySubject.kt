package com.example.changli_planet_app

import com.zhuangfei.timetable.model.Schedule
import com.zhuangfei.timetable.model.ScheduleEnable

/**
 * 自定义实体类需要实现 ScheduleEnable 接口并实现 getSchedule()
 */
class MySubject(
    var term: String = "",
    var courseName: String = "",
    var classroom: String = "",
    var teacher: String = "",
    var weeks: List<Int>? = emptyList(),
    var start: Int = 0,
    var step: Int = 0,
    var weekday: Int = 0,
    var colorRandom: Int = 0,
    var time: String = ""
) : ScheduleEnable {

    companion object {
        const val EXTRAS_ID = "extras_id"
        const val EXTRAS_AD_URL = "extras_ad_url" // 广告 URL
    }

    var id: Int = 0
    var url: String? = null

    override fun getSchedule(): Schedule {
        return Schedule().apply {
            day = this@MySubject.weekday
            name = this@MySubject.courseName
            room = this@MySubject.classroom
            start = this@MySubject.start
            step = this@MySubject.step
            teacher = this@MySubject.teacher
            weekList = this@MySubject.weeks
            colorRandom = 2
            putExtras(EXTRAS_ID, id)
            url?.let { putExtras(EXTRAS_AD_URL, it) }
        }
    }


}