package com.example.changli_planet_app

import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhuangfei.timetable.model.Schedule
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException

/**
 * 数据类，加载课程数据
 */
object SubjectRepertory {

    fun addSchedule(schedule: Schedule): MySubject {
        return MySubject(
            courseName = schedule.name,
            weekday = schedule.day,
            classroom = schedule.room,
            teacher = schedule.teacher,
            start = schedule.start,
            step = schedule.step,
            weeks = schedule.weekList
        )
    }

    fun loadDefaultSubjects(): List<MySubject> {
        val json =
            "[[\"2017-2018学年秋\", \"\", \"\", \"计算机组成原理\", \"\", \"\", \"\", \"\", \"刘静\", \"\", \"\", \"1周上\", 1, 1, 2, \"\", \"计算机综合楼106\", \"\"]," +
                    "[\"2017-2018学年秋\", \"\", \"\", \"hahaha\", \"\", \"\", \"\", \"\", \"刘静\", \"\", \"\", \"2周上\", 1, 1, 4, \"\", \"计算机综合楼106\", \"\"]," +
                    "[\"2017-2018学年秋\", \"\", \"\", \"算法分析与设计\", \"\", \"\", \"\", \"\", \"王静\", \"\", \"\", \"1周\", 1, 3, 2, \"\", \"计算机综合楼205\", \"\"], " +
                    "[\"2017-2018学年秋\", \"\", \"\", \"毛泽东思想和中国特色社会主义理论体系概论\", \"\", \"\", \"\", \"\", \"杨晓军\", \"\", \"\", \"6-12,14-17周上\", 1, 5, 2, \"\", \"3号教学楼3208\", \"\"]]"
        return parse(json)
    }

    fun loadDefaultSubjects2(): List<MySubject> {
        val json =
            "[[\"2017-2018学年秋\", \"\", \"\", \"计算机组成原理\", \"\", \"\", \"\", \"\", \"刘静\", \"\", \"\", \"1,2,3,4,5\", 1, 1, 4, \"\", \"计算机综合楼106\", \"\"]," +
                    "[\"2017-2018学年秋\", \"\", \"\", \"高等数学\", \"\", \"\", \"\", \"\", \"壮飞\", \"\", \"\", \"1,2,3,7,8\", 1, 2, 2, \"\", \"计算机综合楼106\", \"\"]," +
                    "[\"2017-2018学年秋\", \"\", \"\", \"算法分析与设计\", \"\", \"\", \"\", \"\", \"王静\", \"\", \"\", \"1,3,5,9,10\", 1, 5, 2, \"\", \"计算机综合楼205\", \"\"]]"
        return parse(json)
    }

    private fun parse(parseString: String): List<MySubject> {
        val courses = mutableListOf<MySubject>()
        try {
            val array = JSONArray(parseString)
            for (i in 0 until array.length()) {
                val array2 = array.getJSONArray(i)
                val term = array2.getString(0)
                val name = array2.getString(3)
                val teacher = array2.getString(8)
                if (array2.length() <= 10) {
                    courses.add(
                        MySubject(
                            term, name,
                            null.toString(), teacher, null, -1, -1, -1, -1, null.toString()
                        )
                    )
                    continue
                }
                var room =
                    array2.getString(16) + array2.getString(17).replace("\\(.*?\\)".toRegex(), "")
                val weeks = array2.getString(11)
                val (day, start, step) = try {
                    Triple(
                        array2.getString(12).toInt(),
                        array2.getString(13).toInt(),
                        array2.getString(14).toInt()
                    )
                } catch (e: Exception) {
                    Triple(-1, -1, -1)
                }
                courses.add(
                    MySubject(
                        term, name, room, teacher, getWeekList(weeks), start, step, day, -1,
                        null.toString()
                    )
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return courses
    }

    private fun getWeekList(weeksString: String?): List<Int> {
        val weekList = mutableListOf<Int>()
        if (weeksString.isNullOrEmpty()) return weekList

        val cleanedWeeks = weeksString.replace("[^\\d\\-,]".toRegex(), "")
        if (cleanedWeeks.contains(",")) {
            cleanedWeeks.split(",").forEach { weekList.addAll(getWeekList2(it)) }
        } else {
            weekList.addAll(getWeekList2(cleanedWeeks))
        }
        return weekList
    }

    private fun getWeekList2(weeksString: String): List<Int> {
        val weekList = mutableListOf<Int>()
        val range = weeksString.split("-").map { it.toInt() }
        val (start, end) = if (range.size > 1) range[0] to range[1] else range[0] to range[0]
        for (i in start..end) {
            weekList.add(i)
        }
        return weekList
    }

    private fun parseWeeks(weekJson: String): weekJsonInfo {
        val pattern =
            java.util.regex.Pattern.compile("(\\d+-\\d+)(\\((单周|双周)?\\))?\\[(\\d{2})-(\\d{2})节\\]")
        val matcher = pattern.matcher(weekJson)
        if (matcher.find()) {
            val weeksRange = matcher.group(1) ?: null
            val weekType = matcher.group(3) // 单周、双周或 null（全部周）
            val startClass = matcher.group(4)?.toIntOrNull()
            val endClass = matcher.group(5)?.toIntOrNull()

            val step = try {
                endClass!! - startClass!! + 1
            } catch (e: Exception) {
                return weekJsonInfo(listOf(), 0, 0)
            }

            val weeks = try {
                weeksRange?.let {
                    val (weekDayFirst, weekDayLast) = it.split("-").map { it.toInt() }
                    when (weekType) {
                        "单周" -> (weekDayFirst..weekDayLast).filter { it % 2 != 0 }
                        "双周" -> (weekDayFirst..weekDayLast).filter { it % 2 == 0 }
                        else -> (weekDayFirst..weekDayLast).toList()
                    }
                }
            } catch (e: Exception) {
                return weekJsonInfo(listOf(), 0, 0)
            }
            return weekJsonInfo(weeks, startClass, step)
        }
        return weekJsonInfo(listOf(), 0, 0)

    }

    data class weekJsonInfo(val weeks: List<Int>?, val start: Int, val step: Int)
}
