package com.zhelearn.CSUSTPlanet.feature.timetable.data.local

import com.tencent.mmkv.MMKV
import kotlin.math.roundToInt

data class TimeTableDisplaySettings(
    val sundayFirst: Boolean = false,
    val courseFontScale: Float = DEFAULT_COURSE_FONT_SCALE,
) {
    companion object {
        const val MIN_COURSE_FONT_SCALE = 0.9f
        const val DEFAULT_COURSE_FONT_SCALE = 1.1f
        const val MAX_COURSE_FONT_SCALE = 1.3f

        fun snapCourseFontScale(fontScale: Float): Float {
            val supportedScale = fontScale.coerceIn(MIN_COURSE_FONT_SCALE, MAX_COURSE_FONT_SCALE)
            return (supportedScale * 10).roundToInt() / 10f
        }
    }
}

/** 统一保存课表展示偏好，后续新增课表设置时继续收口到这里。 */
object TimeTableSettingsStore {
    private const val CACHE_ID = "timetable_settings"
    private const val KEY_SUNDAY_FIRST = "sunday_first"
    private const val KEY_COURSE_FONT_SCALE = "course_font_scale"

    private val mmkv by lazy { MMKV.mmkvWithID(CACHE_ID) }

    fun load(): TimeTableDisplaySettings {
        return TimeTableDisplaySettings(
            sundayFirst = mmkv.decodeBool(KEY_SUNDAY_FIRST, false),
            courseFontScale = TimeTableDisplaySettings.snapCourseFontScale(
                mmkv.decodeFloat(
                    KEY_COURSE_FONT_SCALE,
                    TimeTableDisplaySettings.DEFAULT_COURSE_FONT_SCALE,
                ),
            ),
        )
    }

    fun save(settings: TimeTableDisplaySettings): TimeTableDisplaySettings {
        val normalizedSettings = settings.copy(
            courseFontScale = TimeTableDisplaySettings.snapCourseFontScale(settings.courseFontScale),
        )
        mmkv.encode(KEY_SUNDAY_FIRST, normalizedSettings.sundayFirst)
        mmkv.encode(KEY_COURSE_FONT_SCALE, normalizedSettings.courseFontScale)
        return normalizedSettings
    }
}
