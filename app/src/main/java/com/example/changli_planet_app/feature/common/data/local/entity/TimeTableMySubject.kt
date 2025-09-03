package com.example.changli_planet_app.feature.common.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.changli_planet_app.feature.common.data.local.room.converter.WeeksTypeConverter
import com.zhuangfei.timetable.model.Schedule
import com.zhuangfei.timetable.model.ScheduleEnable

@Entity(
    tableName = "courses",
    indices = [Index(
        value = ["courseName", "classroom", "teacher", "start", "step", "weekday", "term"],
        unique = true
    )]
)
data class TimeTableMySubject(
    var courseName: String = "",
    var classroom: String? = "",
    var teacher: String = "",
    @TypeConverters(WeeksTypeConverter::class)
    var weeks: List<Int>? = emptyList(),
    var start: Int = 0,
    var step: Int = 0,
    var weekday: Int = 0,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0, // 主键放入主构造函数
    val isCustom: Boolean = false,
    var term: String = "",
    val studentId : String = "",
    val studentPassword : String = ""
) : ScheduleEnable {
    @Ignore
    constructor() :this("")




    @Ignore
    var colorRandom: Int = 0 // 非持久化字段

    @Ignore
    var time: String = "" // 非持久化字段

    override fun getSchedule(): Schedule {
        return Schedule().apply {
            day = this@TimeTableMySubject.weekday
            name = this@TimeTableMySubject.courseName
            room = this@TimeTableMySubject.classroom
            start = this@TimeTableMySubject.start
            step = this@TimeTableMySubject.step
            teacher = this@TimeTableMySubject.teacher
            weekList = this@TimeTableMySubject.weeks
            term= this@TimeTableMySubject.term
            colorRandom = 0
        }
    }
}