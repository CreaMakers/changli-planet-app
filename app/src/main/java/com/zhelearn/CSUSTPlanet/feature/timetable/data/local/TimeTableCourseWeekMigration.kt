package com.zhelearn.CSUSTPlanet.feature.timetable.data.local

import com.zhelearn.CSUSTPlanet.feature.common.data.local.room.dao.CourseDao
import com.tencent.mmkv.MMKV

/** 修正旧版本曾将周日课程持久化到前一周的数据。 */
object TimeTableCourseWeekMigration {
    private const val KEY_PREFIX = "sunday_course_week_migrated_"
    private val mmkv by lazy { MMKV.defaultMMKV() }

    suspend fun migrateIfNeeded(
        courseDao: CourseDao,
        term: String,
        studentId: String,
        studentPassword: String,
    ) {
        val migrationKey = "$KEY_PREFIX${studentId}_$term"
        if (mmkv.decodeBool(migrationKey, false)) return

        val storedCourses = courseDao.getCoursesByTerm(term, studentId, studentPassword)
        val correctedSundayCourses = buildList {
            for (course in storedCourses) {
                if (!course.isCustom && course.weekday == 7) {
                    add(course.copy(weeks = course.weeks?.map { week -> week + 1 }))
                }
            }
        }
        if (correctedSundayCourses.isNotEmpty()) {
            courseDao.updateCourses(correctedSundayCourses)
        }
        mmkv.encode(migrationKey, true)
    }
}
