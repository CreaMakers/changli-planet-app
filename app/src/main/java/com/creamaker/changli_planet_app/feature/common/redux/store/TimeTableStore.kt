package com.creamaker.changli_planet_app.feature.common.redux.store

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.ui.platform.DisableContentCapture
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.education.data.remote.EducationHelper
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Store
import com.creamaker.changli_planet_app.core.network.HttpUrlHelper
import com.creamaker.changli_planet_app.core.network.MyResponse
import com.creamaker.changli_planet_app.core.network.OkHttpHelper
import com.creamaker.changli_planet_app.core.network.listener.RequestCallback
import com.creamaker.changli_planet_app.feature.common.data.local.entity.TimeTableMySubject
import com.creamaker.changli_planet_app.feature.common.data.local.room.dao.CourseDao
import com.creamaker.changli_planet_app.feature.common.data.remote.dto.Course
import com.creamaker.changli_planet_app.feature.common.data.remote.dto.GetCourse
import com.creamaker.changli_planet_app.feature.common.redux.state.TimeTableState
import com.creamaker.changli_planet_app.feature.timetable.action.TimeTableAction
import com.creamaker.changli_planet_app.feature.timetable.ui.TimeTableActivity
import com.creamaker.changli_planet_app.utils.getMessage
import com.creamaker.changli_planet_app.widget.Dialog.ErrorStuPasswordResponseDialog
import com.creamaker.changli_planet_app.widget.Dialog.NormalResponseDialog
import com.creamaker.changli_planet_app.widget.View.CustomToast
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Response
import java.util.regex.Pattern

class TimeTableStore(private val courseDao: CourseDao, private val myHandler: Handler?= null) : Store<TimeTableState, TimeTableAction>() {
    private val TAG = "TimeTableStore"
    private val studentId by lazy { StudentInfoManager.studentId }
    private val studentPassword by lazy { StudentInfoManager.studentPassword }
    private val handler = Handler(Looper.getMainLooper())
//    private val mmkv by lazy { MMKV.defaultMMKV() }
//    private var cacheWeek: String
//        get() = mmkv.getString("cache_week", "") ?: ""
//        set(value) {
//            mmkv.putString("cache_week", value)
//        }

    companion object {
        @JvmStatic
        var curState = TimeTableState()
    }

    @SuppressLint("CheckResult")
    override fun handleEvent(action: TimeTableAction) {
        when (action) {
            is TimeTableAction.FetchCourses -> {   //在该函数中目标学期是action.getCourse.termId，本学期是curState.term，注意二者不相等
                val cur = System.currentTimeMillis()

                //courseDao.getAllCourseCount()
                courseDao.getCoursesCountByTerm(action.getCourse.termId)     //根据学期获取课程
                    .subscribeOn(Schedulers.io())
                    .flatMap { count ->
                        if (count == 0 || curState.lastUpdate - cur > 1000 * 60 * 60 * 24 || curState.lastUpdate == 0.toLong()) {
                            // 需要从网络获取数据
                            fetchTimetableFromNetwork(action)
                                .flatMap { networkResult ->
                                    courseDao.clearAllCourses()
                                    val mergedCourses = networkResult
                                        .distinctBy {
                                            "${it.courseName}${it.teacher}${it.weeks}${it.classroom}${it.start}${it.step}${it.term}${it.weekday}"
                                        }
                                        .filter {
                                            it.term == action.getCourse.termId &&
                                                    it.studentId == studentId &&
                                                    it.studentPassword == studentPassword
                                        }
                                        .toMutableList()
                                    courseDao.insertCourses(mergedCourses)
//                                    cacheWeek = action.getCourse.termId
                                    Single.just(mergedCourses)
                                }
                                .doOnSuccess { Log.d("TimeTableStore", "网络请求获得课表") }
                        } else {
                            // 从数据库获取数据
                            courseDao.getCoursesByTerm(action.getCourse.termId, studentId, studentPassword)
                                .map { dbResult ->
                                    // 对数据库结果也进行去重
                                    dbResult.distinctBy {
                                        "${it.courseName}${it.teacher}${it.weeks}${it.classroom}${it.start}${it.step}${it.term}${it.weekday}"
                                    }.toMutableList()
                                }
                                .doOnSuccess { Log.d("TimeTableStore", "从数据库获得课表") }
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        if (result.isNotEmpty()) {
                            curState.term = action.getCourse.termId    //成功获取课程后更新term
                            handleEvent(TimeTableAction.UpdateCourses(result))
                            _state.onNext(curState)
                            action.refreshSuccess?.invoke()
                        } else {
                            curState.term = action.getCourse.termId
                            _state.onNext(curState)             //
                            Log.w("Debug", "No courses found")
                        }
                    }, { error ->
                        Log.e("TimeTableStore", "Error fetching courses", error)
                        error.printStackTrace()
                    })
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
                //curState.term = action.term    //不直接更新curState.term 在获取课程数据成功后更新
                dispatch(
                    TimeTableAction.FetchCourses(
                        action.context,
                        GetCourse(
                            action.stuNum, action.password, "", action.term
                        ),
                        action.refresh
                    )
                )
                //_state.onNext(curState)
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


    private fun fetchTimetableFromNetwork(action: TimeTableAction.FetchCourses): Single<MutableList<TimeTableMySubject>> {
        return Single.create { emitter ->
            val subjects = mutableListOf<TimeTableMySubject>()
            CoroutineScope(Dispatchers.IO).launch {
                var courses :List<com.dcelysia.csust_spider.education.data.remote.model.Course> = emptyList()
                val coursesResource = EducationHelper
                    .getCourseScheduleByTerm(action.getCourse.week, action.getCourse.termId)
                when(coursesResource){
                    is Resource.Success->{
                        courses = coursesResource.data
                    }
                    is Resource.Loading ->{}
                    is Resource.Error ->{
                        Log.e(TAG,coursesResource.msg)
                    }
                }
                //映射到本地
                val localCourse = toLocalCourse(courses)
                if (courses.isEmpty())
                {
                    handler.post {
                        try {
                            ErrorStuPasswordResponseDialog(
                                action.context,
                                "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡",
                                "查询失败",
                                action.refresh
                            ).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    myHandler?.sendMessage(getMessage(TimeTableActivity.Companion.CANCEL_LOADING))
                }
                else{
                    subjects.addAll(generateSubjects(localCourse,action.getCourse.termId))
                    emitter.onSuccess(subjects)
                    handler.post {
                        CustomToast.Companion.showMessage(
                            PlanetApplication.Companion.appContext,
                            "刷新成功"
                        )
                    }
                }
            }
            //获得网络请求
//            val httpUrlHelper = HttpUrlHelper.HttpRequest()
//                .get(PlanetApplication.Companion.ToolIp + "/courses")
//                .addQueryParam("stuNum", action.getCourse.stuNum)
//                .addQueryParam("password", action.getCourse.password)
//                .addQueryParam("week", action.getCourse.week)
//                .addQueryParam("termId", action.getCourse.termId).build()
//
//            OkHttpHelper.sendRequest(httpUrlHelper, object : RequestCallback {
//                override fun onSuccess(response: Response) {
//                    try {
//                        val type = object : TypeToken<MyResponse<List<Course>>>() {}.type
//                        val fromJson = OkHttpHelper.gson.fromJson<MyResponse<List<Course>>>(
//                            response.body?.string(), type
//                        )
//
//                        when (fromJson.code) {
//                            "200" -> {
//                                subjects.addAll(generateSubjects(fromJson.data,action.getCourse.termId))
//                                emitter.onSuccess(subjects)
//                                handler.post{
//                                    CustomToast.Companion.showMessage(PlanetApplication.Companion.appContext,"刷新成功")
//                                }
//                            }
//
//                            "403" -> {
//                                handler.post {
//                                    try {
//                                        ErrorStuPasswordResponseDialog(
//                                            action.context,
//                                            "学号或密码错误ʕ⸝⸝⸝˙Ⱉ˙ʔ",
//                                            "查询失败",
//                                            action.refresh
//                                        ).show()
//                                    } catch (e: Exception) {
//                                        e.printStackTrace()
//                                    }
//                                }
//                                myHandler?.sendMessage(getMessage(TimeTableActivity.Companion.CANCEL_LOADING))
//                            }
//
//                            in listOf("404", "500") -> {
//                                handler.post {
//                                    try {
//                                        NormalResponseDialog(
//                                            action.context,
//                                            "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡",
//                                            "查询失败"
//                                        ).show()
//                                    } catch (e: Exception) {
//                                        e.printStackTrace()
//                                    }
//                                }
//                                myHandler?.sendMessage(getMessage(TimeTableActivity.Companion.CANCEL_LOADING))
//                            }
//
//                            else ->{
//                                emitter.onError(Exception("数据错误"))
//                                handler.post{
//                                    CustomToast.Companion.showMessage(PlanetApplication.Companion.appContext,"暂时没有该学期的数据哦")
//                                }
//                                myHandler?.sendMessage(getMessage(TimeTableActivity.Companion.CANCEL_LOADING))
//                            }
//                        }
//                    } catch (e: Exception) {
//                        handler.post {
//                            try {
//                                NormalResponseDialog(
//                                    action.context,
//                                    "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡",
//                                    "查询失败"
//                                ).show()
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
//                        myHandler?.sendMessage(getMessage(TimeTableActivity.Companion.CANCEL_LOADING))
//                        e.printStackTrace()
//                    }
//                }
//
//                override fun onFailure(error: String) {
//                    handler.post {
//                        try {
//                            NormalResponseDialog(
//                                action.context,
//                                "网络出现波动啦！请重新刷新~₍ᐢ..ᐢ₎♡",
//                                "查询失败"
//                            ).show()
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                    myHandler?.sendMessage(getMessage(TimeTableActivity.Companion.CANCEL_LOADING))
//                }
//            })
        }

    }

    private fun parseWeeks(weekJson: String): weekJsonInfo {
        // 匹配周数范围、单双周（可选）、以及节次范围
        val pattern = Pattern.compile(
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

    private fun generateSubjects(courses: List<Course>, newTerm:String): MutableList<TimeTableMySubject> {
        val subjects = mutableListOf<TimeTableMySubject>()
        courses.forEach {
            val weeks = parseWeeks(it.weeks).weeks
            val start = parseWeeks(it.weeks).start
            val step = parseWeeks(it.weeks).step
            subjects.add(
                TimeTableMySubject(
                    courseName = it.courseName,
                    classroom = it.classroom,
                    teacher = it.teacher,
                    weeks = weeks,
                    start = start,
                    step = step,
                    weekday = it.weekday.toInt(),
                    term = newTerm,
                    studentId = studentId,
                    studentPassword = studentPassword
                )
            )
        }
        return subjects
    }
    private fun toLocalCourse(courses: List<com.dcelysia.csust_spider.education.data.remote.model.Course>): List<Course>{
            val localCourses = courses.map {
                Course(
                    it.classroom,
                    it.courseName,
                    it.teacher,
                    it.weekday,
                    it.weeks
                )
            }
            return localCourses
    }


}