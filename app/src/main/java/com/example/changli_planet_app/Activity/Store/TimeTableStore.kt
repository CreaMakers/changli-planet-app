package com.example.changli_planet_app.Activity.Store

import android.annotation.SuppressLint
import android.util.Log
import com.example.changli_planet_app.Activity.Action.TimeTableAction
import com.example.changli_planet_app.Activity.State.TimeTableState
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.Cache.Room.CourseDao
import com.example.changli_planet_app.Cache.Room.MySubject
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.Course
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.Data.SampleData.SubjectRepertory
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

import okhttp3.Response

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
                    fetchTimetableFromNetwork(action)
//
//                    Log.d("TimeTableStore", "网络请求获得课表")
                } else {
                    //从数据库中获得课表
                    courseDao.getAllCourses()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ result ->
                            if (result.isNotEmpty()) {
                                curState.subjects = result
                                _state.onNext(curState)
                                Log.d("Debug", "Courses loaded from database: $result")
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
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        //更新curState
                        curState.lastUpdate = System.currentTimeMillis()
                        curState.subjects = action.subjects
                        _state.onNext(curState)
                        Log.d("Debug", "Courses in database update")
                    }, { error ->
                        Log.e("Debug", "Error updating course", error)
                        error.printStackTrace()
                    })

            }

            is TimeTableAction.AddCourse -> {
                Observable.fromCallable {
                    //更新数据库
                    courseDao.insertCourse(action.subject)
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        Log.d("Debug", "Courses in database after insert/update: $result")
                        //更新curState
                        curState = curState.copy(
                            subjects = curState.subjects.toMutableList()
                                .apply { add(action.subject) }
                        )
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
//                dispatch(
//                    TimeTableAction.FetchCourses(
//                        GetCourse(
//                            "202301160231",
//                            "Cy@20050917",
//                            " ",
//                            action.term
//                        )
//                    )
//                )
            }
        }
    }

    private fun fetchTimetableFromReposity(action: TimeTableAction.FetchCourses) {
        val subjects = mutableListOf<MySubject>()
        subjects.addAll(SubjectRepertory.loadDefaultSubjects() + SubjectRepertory.loadDefaultSubjects2())
        handleEvent(TimeTableAction.UpdateCourses(subjects))
    }

    private fun fetchTimetableFromNetwork(action: TimeTableAction.FetchCourses) {
        val subjects = mutableListOf<MySubject>()
        //获得网络请求
        val httpUrlHelper = HttpUrlHelper.HttpRequest()
            .header("Authorization", "${PlanetApplication.accessToken}")
            .get(PlanetApplication.ToolIp + "/courses")
            .addQueryParam("stuNum", action.getCourse.stuNum)
            .addQueryParam("password", action.getCourse.password)
            .addQueryParam("week", "")
            .addQueryParam("termId", action.getCourse.termId)
            .build()

        OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
            override fun onSuccess(response: Response) {
                val type = object : TypeToken<MyResponse<List<Course>>>() {}.type
                val fromJson = OkHttpHelper.gson.fromJson<MyResponse<List<Course>>>(
                    response.body?.string(),
                    type
                )
                when (fromJson.code) {
                    "200" -> {
                        fromJson.data.forEach {
                            val weeks = parseWeeks(it.weeks).weeks
                            val start = parseWeeks(it.weeks).start
                            val step = parseWeeks(it.weeks).step
                            subjects.add(
                                MySubject(
                                    term = action.getCourse.termId,
                                    courseName = it.courseName,
                                    classroom = it.classroom,
                                    teacher = it.teacher,
                                    weeks = weeks,
                                    start = start,
                                    step = step,
                                    weekday = it.weekday.toInt(),
                                )
                            )

                        }
                        handleEvent(TimeTableAction.UpdateCourses(subjects))
                    }
                }
            }

            override fun onFailure(error: String) {}

        })


    }

    private fun parseWeeks(weekJson: String): weekJsonInfo {
        // 匹配周数范围、单双周（可选）、以及节次范围
        val pattern = java.util.regex.Pattern.compile(
            "(\\d+(?:-\\d+)?)(\\((周|单周|双周)?\\))?\\[(\\d{2})(?:-(\\d{2}))?(?:-(\\d{2}))?(?:-(\\d{2}))?节\\]"
        )
        val matcher = pattern.matcher(weekJson)
        if (matcher.find()) {
            val weeksRange = matcher.group(1) // 周数范围，例如 "1-16" 或 "9"
            val weekType = matcher.group(3)   // "单周"、"双周"、"周"，也可能为 null
            val startClass = matcher.group(4)?.toIntOrNull() ?: 0 // 起始节次
            val endClass = matcher.group(7)?.toIntOrNull()
                ?: matcher.group(6)?.toIntOrNull()
                ?: matcher.group(5)?.toIntOrNull()
                ?: startClass // 结束节次，默认等于起始节次
            val step = endClass - startClass + 1 // 计算 step（持续节次数量）

            // 解析周数列表
            val weeks = try {
                weeksRange?.let { range ->
                    if (range.contains("-")) {
                        // 范围周数处理，例如 "1-16"
                        val (weekStart, weekEnd) = range.split("-").map { it.toInt() }
                        when (weekType) {
                            "单周" -> (weekStart..weekEnd).filter { it % 2 != 0 } // 奇数周
                            "双周" -> (weekStart..weekEnd).filter { it % 2 == 0 } // 偶数周
                            else -> (weekStart..weekEnd).toList() // 全部周（包括 null 和 "周"）
                        }
                    } else {
                        // 单个周数处理，例如 "9"
                        listOf(range.toInt())
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

}