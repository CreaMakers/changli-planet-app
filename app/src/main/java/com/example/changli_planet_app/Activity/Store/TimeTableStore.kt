package com.example.changli_planet_app.Activity.Store

import android.annotation.SuppressLint
import android.util.Log
import com.example.changli_planet_app.Activity.Action.TimeTableAction
import com.example.changli_planet_app.Activity.State.TimeTableState
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Cache.Room.CourseDao
import com.example.changli_planet_app.Cache.Room.MySubject
import com.example.changli_planet_app.Data.jsonbean.GetCourse
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.Course
import com.example.changli_planet_app.Network.Response.MyResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.CountDownLatch


class TimeTableStore(private val courseDao: CourseDao) : Store<TimeTableState, TimeTableAction>() {
    companion object {
        @JvmStatic
        var curState = TimeTableState()
    }

    @SuppressLint("CheckResult")
    override fun handleEvent(action: TimeTableAction) {
        when (action) {
            is TimeTableAction.FetchCourses -> {
                val cur = System.currentTimeMillis()
                if (curState.lastUpdate - cur > 1000 * 60 * 60 * 24 || curState.lastUpdate == 0.toLong()) {
//                    fetchTimetableFromLocalData(action).map { result ->
                    fetchTimetableFromNetwork(action).map { result ->
                        (result + curState.subjects).distinctBy { "${it.courseName}${it.teacher}${it.weeks}${it.classroom}${it.start}${it.step}${it.term}" }
                            .filter { it.term == curState.term }.toMutableList()
                    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe { result ->
                            handleEvent(TimeTableAction.UpdateCourses(result))
                        }
                    Log.d("TimeTableStore", "网络请求获得课表")
                } else {
                    //从数据库中获得课表
                    courseDao.getCoursesByTerm(curState.term).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
                            if (result.isNotEmpty()) {
                                curState.subjects = result
                                _state.onNext(curState)
                            } else {
                                Log.w("Debug", "No courses found in database")
                            }
                        }, { error ->
                            error.printStackTrace()
                        })
                    Log.d("TimeTableStore", "从数据库获得课表")
                }
            }

            is TimeTableAction.UpdateCourses -> {
                //更新数据库
                Observable.fromCallable {
                    courseDao.insertCourses(action.subjects)
                    Log.d("Debug", "Courses inserted successfully")
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    //更新curState
                    curState.lastUpdate = System.currentTimeMillis()
                    curState.subjects = action.subjects
                    _state.onNext(curState)
                    Log.d("Debug", "Courses update successfully")
                }, { error ->
                    Log.e("Debug", "Error updating course , $error", error)
                    error.printStackTrace()
                })

            }

            is TimeTableAction.AddCourse -> {
                Observable.fromCallable {
                    //更新数据库
                    courseDao.insertCourse(action.subject)
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        Log.d("Debug", "Courses in database after insert/update: $result")
                        //更新curState
                        curState = curState.copy(subjects = curState.subjects.toMutableList()
                            .apply { add(action.subject) })
                        _state.onNext(curState)
                        Log.d(
                            "Debug",
                            "State updated with new subject in AddCourse: ${curState.subjects}"
                        )
                        Log.d("TimeTableStore", "Active subscribers: ${_state.hasObservers()}")

                    }, { error ->
                        Log.e("Debug", "Error add course", error)
                        error.printStackTrace()
                    })
            }

            is TimeTableAction.selectWeek -> {
                curState.weekInfo = action.weekInfo
                _state.onNext(curState)
            }

            is TimeTableAction.selectTerm -> {
                curState.term = action.term
                _state.onNext(curState)
                dispatch(
                    TimeTableAction.FetchCourses(
                        GetCourse(
                            "202301160231", "Cy@20050917", "", action.term
                        )
                    )
                )
            }

            is TimeTableAction.DeleteCourse -> {
                curState = curState.copy(
                    subjects = curState.subjects.filterNot {
                        it.term == action.term && it.start == action.start && it.weekday == action.day && action.curDisplayWeek in (it.weeks
                            ?: emptyList()) && it.isCustom
                    }.toMutableList()
                )
                Completable.fromAction {
                    courseDao.clearAllCourses()
                    courseDao.insertCourses(curState.subjects)
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    _state.onNext(curState)
                }
            }

//            is TimeTableAction.getStartTime -> {
//                val startTime = fetchCourseByDataFromNetwork(action)
//                curState.startTime = startTime
//                _state.onNext(curState)
//                Log.d("TimeTableStore", "startTime: $startTime")
//            }
        }
    }

//    fun extractWeekAndDay(input: String): List<String> {
//        val weekRegex = Regex("第\\d+周")  // 匹配第X周
//        val dayRegex = Regex("星期[一二三四五六日]") // 匹配星期X
//
//        val week = weekRegex.find(input)?.value ?: ""
//        val day = dayRegex.find(input)?.value ?: ""
//
//        return listOf(week, day)
//    }
//
//    private fun fetchCourseByDataFromNetwork(action: TimeTableAction.getStartTime): String {
//        var result = ""
//        val latch = CountDownLatch(1)
//        //获得网络请求
//        val httpUrlHelper = HttpUrlHelper.HttpRequest()
//            .get(PlanetApplication.ToolIp + "/courses/data")
//            .addQueryParam("stuNum", action.stuNum)
//            .addQueryParam("password", action.password)
//            .addQueryParam("data", action.data).build()
//
//        OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
//            override fun onSuccess(response: Response) {
//                val type = object : TypeToken<MyResponse<List<Course>>>() {}.type
//                val fromJson = OkHttpHelper.gson.fromJson<MyResponse<List<Course>>>(
//                    response.body?.string(), type
//                )
//                when (fromJson.code) {
//                    "200" -> {
//                        val weekAndDay = extractWeekAndDay(fromJson.data.last().weeks)
//                        result = calculateTime(action.data, weekAndDay[0], weekAndDay[1])
//                    }
//                    else -> {
//
//                    }
//                }
//                latch.countDown()
//            }
//
//            override fun onFailure(error: String) {
//                latch.countDown()
//            }
//        })
//        latch.await() // 阻塞线程直到 CountDownLatch 的计数为 0
//        return result
//    }

//    private fun calculateTime(dateInput: String, week: String, day: String): String {
//        val weekNum = week.replace("第", "").replace("周", "").toInt()
//        val dayMap = mapOf(
//            "星期一" to 1,
//            "星期二" to 2,
//            "星期三" to 3,
//            "星期四" to 4,
//            "星期五" to 5,
//            "星期六" to 6,
//            "星期日" to 7
//        )
//        val minusDay = dayMap[day]!! - 1 + (weekNum - 1) * 7
//        val sdf = SimpleDateFormat("yyyy-MM-dd")
//        // 将日期字符串解析为 Date 对象
//        val date = sdf.parse(dateInput)
//        // 使用 Calendar 进行日期计算
//        val calendar = Calendar.getInstance()
//        calendar.setTime(date);
//        calendar.add(Calendar.DAY_OF_YEAR, -minusDay)
//        // 得到计算后的日期
//        val resultDate = calendar.getTime()
//        // 格式化为目标格式 "yyyy-MM-dd HH:mm:ss"
//        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//        return outputFormat.format(resultDate)
//    }


    private fun fetchTimetableFromNetwork(action: TimeTableAction.FetchCourses): Single<MutableList<MySubject>> {
        return Single.create { emitter ->
            val subjects = mutableListOf<MySubject>()
            //获得网络请求
            val httpUrlHelper = HttpUrlHelper.HttpRequest()
                .get(PlanetApplication.ToolIp + "/courses")
                .addQueryParam("stuNum", action.getCourse.stuNum)
                .addQueryParam("password", action.getCourse.password)
                .addQueryParam("week", action.getCourse.week)
                .addQueryParam("termId", action.getCourse.termId).build()

            OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
                override fun onSuccess(response: Response) {
                    val type = object : TypeToken<MyResponse<List<Course>>>() {}.type
                    val fromJson = OkHttpHelper.gson.fromJson<MyResponse<List<Course>>>(
                        response.body?.string(), type
                    )
                    when (fromJson.code) {
                        "200" -> {
                            subjects.addAll(generateSubjects(fromJson.data))
                            emitter.onSuccess(subjects)
                        }
                    }
                }

                override fun onFailure(error: String) {
                    emitter.onError(Throwable(error))
                }
            })
        }

    }

    private fun parseWeeks(weekJson: String): weekJsonInfo {
        // 匹配周数范围、单双周（可选）、以及节次范围
        val pattern = java.util.regex.Pattern.compile(
            "(\\d+(?:-\\d+)?(?:,\\d+(?:-\\d+)?)*)\\((周|单周|双周)?\\)?\\[(\\d{2})(?:-(\\d{2}))?(?:-(\\d{2}))?(?:-(\\d{2}))?节\\]"
        )
        val matcher = pattern.matcher(weekJson)
        if (matcher.find()) {
            val weeksRange = matcher.group(1) // 周数范围
            val weekType = matcher.group(2)   // 单周、双周、周
            val startClass = matcher.group(3)?.toIntOrNull() ?: 0 // 起始节次
            val endClass = listOfNotNull(
                matcher.group(4)?.toIntOrNull(),
                matcher.group(5)?.toIntOrNull(),
                matcher.group(6)?.toIntOrNull(),
            ).lastOrNull() ?: startClass // 结束节次

            val step = endClass - startClass + 1 // 计算 step（持续节次数量）

            // 解析周数列表
            val weeks = try {
                weeksRange?.let { range ->
                    range.split(",").flatMap { part ->
                        if (part.contains("-")) {
                            // 范围周数处理，例如 "1-3"
                            val (weekStart, weekEnd) = part.split("-").map { it.toInt() }
                            when (weekType) {
                                "单周" -> (weekStart..weekEnd).filter { it % 2 != 0 } // 奇数周
                                "双周" -> (weekStart..weekEnd).filter { it % 2 == 0 } // 偶数周
                                else -> (weekStart..weekEnd).toList() // 全部周（包括 null 和 "周"）
                            }
                        } else {
                            // 单个周数处理，例如 "5"
                            listOf(part.toInt())
                        }
                    }
                } ?: listOf()
            } catch (e: Exception) {
                listOf() // 异常时返回空列表
            }

            // 返回解析结果
            return weekJsonInfo(weeks, startClass, step)
        }

        // 返回空的结果
        return weekJsonInfo(listOf(), 0, 0)
    }

    // 数据类
    data class weekJsonInfo(val weeks: List<Int>, val start: Int, val step: Int)

    private fun generateSubjects(courses: List<Course>): MutableList<MySubject> {
        val subjects = mutableListOf<MySubject>()
        courses.forEach {
            val weeks = parseWeeks(it.weeks).weeks
            val start = parseWeeks(it.weeks).start
            val step = parseWeeks(it.weeks).step
            subjects.add(
                MySubject(
                    courseName = it.courseName,
                    classroom = it.classroom,
                    teacher = it.teacher,
                    weeks = weeks,
                    start = start,
                    step = step,
                    weekday = it.weekday.toInt(),
                    term = curState.term
                )
            )
        }
        return subjects
    }


}