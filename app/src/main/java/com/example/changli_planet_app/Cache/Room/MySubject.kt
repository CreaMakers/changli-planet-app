package com.example.changli_planet_app.Cache.Room
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.changli_planet_app.Cache.WeeksTypeConverter
import com.zhuangfei.timetable.model.Schedule
import com.zhuangfei.timetable.model.ScheduleEnable


@Entity(
    tableName = "courses",
    indices = [Index(value = ["courseName", "classroom", "teacher", "start", "step", "weekday"], unique = true)]
)
data class MySubject(
    var courseName: String = "",
    var classroom: String = "",
    var teacher: String = "",
    @TypeConverters(WeeksTypeConverter::class)
    var weeks: List<Int>? = emptyList(),
    var start: Int = 0,
    var step: Int = 0,
    var weekday: Int = 0,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0 // 主键放入主构造函数
) : ScheduleEnable {
    @Ignore
    var term: String = ""
    @Ignore
    var colorRandom: Int = 0 // 非持久化字段

    @Ignore
    var time: String = "" // 非持久化字段

    override fun getSchedule(): Schedule {
        return Schedule().apply {
            day = this@MySubject.weekday
            name = this@MySubject.courseName
            room = this@MySubject.classroom
            start = this@MySubject.start
            step = this@MySubject.step
            teacher = this@MySubject.teacher
            weekList = this@MySubject.weeks
            colorRandom = 0
        }
    }
}
