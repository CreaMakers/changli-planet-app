package com.example.changli_planet_app.Activity.Store

import android.annotation.SuppressLint
import android.util.Log
import com.example.changli_planet_app.Activity.Action.TimeTableAction
import com.example.changli_planet_app.Activity.State.TimeTableState
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.Core.Store
import com.example.changli_planet_app.CourseDao
import com.example.changli_planet_app.MySubject
import com.example.changli_planet_app.Network.HttpUrlHelper
import com.example.changli_planet_app.Network.OkHttpHelper
import com.example.changli_planet_app.Network.RequestCallback
import com.example.changli_planet_app.Network.Response.Course
import com.example.changli_planet_app.Network.Response.MyResponse
import com.example.changli_planet_app.SubjectRepertory
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
//                    fetchTimetableFromNetwork(action)
                    fetchTimetableFromReposity(action)
                    Log.d("TimeTableStore", "网络请求获得课表")
                } else {
                    //从数据库中获得课表
                    courseDao.getAllCourse()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ result ->
                            curState.subjects = result
                            _state.onNext(curState)
                        }, { error ->
                            error.printStackTrace()
                        })
                    Log.d("TimeTableStore", "从数据库获得课表")
                }
            }

            is TimeTableAction.UpdateCourses -> {
                //更新数据库
                Observable.fromCallable {
                    courseDao.updateCourse(action.subjects)
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        //更新curState
                        curState.lastUpdate = System.currentTimeMillis()
                        curState.subjects = action.subjects
                        _state.onNext(curState)
                    }, { error ->
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
                    .subscribe({
                        //更新curState
                        curState.subjects.add(action.subject)
                        _state.onNext(curState)
                    }, { error ->
                        error.printStackTrace()
                    })
            }

            is TimeTableAction.selectWeek -> {
                curState.week = action.week
                _state.onNext(curState)
            }

            is TimeTableAction.selectTerm -> {
                curState.term = action.term
                _state.onNext(curState)
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
            .get(PlanetApplication.ToolIp + "/courses")
//            .header("Authorization", "Bearer ${PlanetApplication.accessToken}")
            .addQueryParam("stuNum",action.getCourse.stuNum)
            .addQueryParam("password",action.getCourse.password)
            .addQueryParam("week",action.getCourse.week)
            .addQueryParam("term",action.getCourse.termId)
            .build()

        OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
            override fun onSuccess(response: Response) {

                val fromJson = OkHttpHelper.gson.fromJson<MyResponse<List<Course>>>(
                    response.body?.string(),
                    MyResponse::class.java
                )
                when (fromJson.msg) {
                    "Timetable fetched successfully" -> {
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
                    }
                }
            }

            override fun onFailure(error: String) {}

        })
        handleEvent(TimeTableAction.UpdateCourses(subjects))

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