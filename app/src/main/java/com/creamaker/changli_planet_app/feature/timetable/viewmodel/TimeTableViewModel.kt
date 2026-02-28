package com.creamaker.changli_planet_app.feature.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.common.cache.CommonInfo
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.feature.common.data.local.entity.TimeTableMySubject
import com.creamaker.changli_planet_app.feature.common.data.local.room.database.CoursesDataBase
import com.creamaker.changli_planet_app.feature.common.data.remote.dto.Course
import com.creamaker.changli_planet_app.feature.timetable.ui.entity.TimeTableUiState
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.education.data.remote.EducationHelper
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.TimeZone
import java.util.regex.Pattern

class TimeTableViewModel : ViewModel() {

    private val TAG = "TimeTableViewModel"
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private val studentId by lazy { StudentInfoManager.studentId }
    private val studentPassword by lazy { StudentInfoManager.studentPassword }
    private val dataBase by lazy { CoursesDataBase.getDatabase(PlanetApplication.appContext) }
    private val courseDao by lazy { dataBase.courseDao() }

    // UI State
    private val _uiState = MutableLiveData<TimeTableUiState>()
    val uiState: LiveData<TimeTableUiState> = _uiState

    // API Response State
    private val _coursesResponse = MutableLiveData<ApiResponse<List<TimeTableMySubject>>>()
    val coursesResponse: LiveData<ApiResponse<List<TimeTableMySubject>>> = _coursesResponse

    // Add Course Response
    private val _addCourseResponse = MutableLiveData<ApiResponse<Unit>>()
    val addCourseResponse: LiveData<ApiResponse<Unit>> = _addCourseResponse

    // Delete Course Response
    private val _deleteCourseResponse = MutableLiveData<ApiResponse<Unit>>()
    val deleteCourseResponse: LiveData<ApiResponse<Unit>> = _deleteCourseResponse

    // Current Display Week
    private val _curDisplayWeek = MutableLiveData<Int>()
    val curDisplayWeek: LiveData<Int> = _curDisplayWeek

    init {
        val initState = TimeTableUiState(term = getCurrentTerm())
        _uiState.value = initState
        _curDisplayWeek.value = 1
    }

    fun initFirstLaunch() {
        if (mmkv.getBoolean("isFirstLaunch", true)) {
            viewModelScope.launch(Dispatchers.IO) {
                dataBase.clearAllTables()
                mmkv.encode("isFirstLaunch", false)
            }
        }
    }

    fun getCurrentTerm(): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"))
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        return when {
            currentMonth >= 7 -> "$currentYear-${currentYear + 1}-1"
            currentMonth >= 2 -> "${currentYear - 1}-${currentYear}-2"
            else -> "${currentYear - 1}-${currentYear}-1"
        }
    }

    fun loadCourses(term: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _coursesResponse.value = ApiResponse.Loading()

            try {
                val cur = System.currentTimeMillis()
                val count = withContext(Dispatchers.IO) {
                    courseDao.getCoursesCountByTerm(term)
                }

                val lastUpdate = mmkv.decodeLong("lastUpdate_$term", 0L)
                val needRefresh = count == 0 ||
                        forceRefresh ||
                        (cur - lastUpdate > 1000 * 60 * 60 * 24)

                if (needRefresh) {
                    // 从网络获取
                    fetchCoursesFromNetwork(term)
                } else {
                    val courses = withContext(Dispatchers.IO) {
                        courseDao.getCoursesByTerm(term, studentId, studentPassword)
                            .distinctBy {
                                "${it.courseName}${it.teacher}${it.weeks}${it.classroom}${it.start}${it.step}${it.term}${it.weekday}"
                            }.map { course ->
                                if (course.weekday == 7) {
                                    // 对weeks列表中的每个元素执行减1操作（处理可能的null情况）
                                    val adjustedWeeks = course.weeks?.map { week -> week - 1 }
                                    // 复制原对象，替换weeks为调整后的值
                                    course.copy(weeks = adjustedWeeks)
                                } else {
                                    // 其他情况保持不变
                                    course
                                }
                            }
                    }
                    if (courses.isEmpty()) {
                        _coursesResponse.value = ApiResponse.Error("该学期暂无课程数据")
                    } else {
                        updateUiState(courses.toMutableList(), term)
                        _coursesResponse.value = ApiResponse.Success(courses)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading courses", e)
                _coursesResponse.value = ApiResponse.Error("加载课程失败: ${e.message}")
            }
        }
    }

    private suspend fun fetchCoursesFromNetwork(term: String) {
        withContext(Dispatchers.IO) {
            try {
                val coursesResource = EducationHelper.getCourseScheduleByTerm("", term)

                when (coursesResource) {
                    is Resource.Success -> {
                        val localCourses = toLocalCourse(coursesResource.data)
                        val subjects = generateSubjects(localCourses, term)

                        if (subjects.isEmpty()) {
                            withContext(Dispatchers.Main) {
                                _coursesResponse.value = ApiResponse.Error("该学期暂无课程数据")
                            }
                            return@withContext
                        }

                        // 去重并处理数据
                        val mergedCourses = distinctSubjects(subjects)
                        // 保存到数据库
                        courseDao.clearAllCourses()
                        courseDao.insertCourses(mergedCourses)

                        // 更新最后更新时间
                        mmkv.encode("lastUpdate_$term", System.currentTimeMillis())

                        withContext(Dispatchers.Main) {
                            updateUiState(mergedCourses, term)
                            _coursesResponse.value = ApiResponse.Success(mergedCourses)
                        }
                    }

                    is Resource.Error -> {
                        withContext(Dispatchers.Main) {
                            _coursesResponse.value = ApiResponse.Error(
                                coursesResource.msg
                            )
                        }
                    }

                    is Resource.Loading -> {
                        // Loading state already set
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching from network", e)
                withContext(Dispatchers.Main) {
                    _coursesResponse.value = ApiResponse.Error("网络出现波动: ${e.message}")
                }
            }
        }
    }

    private fun distinctSubjects(subjects: MutableList<TimeTableMySubject>): MutableList<TimeTableMySubject> {
        return subjects.distinctBy {
            "${it.courseName}${it.teacher}${it.weeks}${it.classroom}${it.start}${it.step}${it.term}${it.weekday}"
        }.map { course ->
            if (course.weekday == 7) {
                // 对weeks列表中的每个元素执行减1操作（处理可能的null情况）
                val adjustedWeeks = course.weeks?.map { week -> week - 1 }
                // 复制原对象，替换weeks为调整后的值
                course.copy(weeks = adjustedWeeks)
            } else {
                // 其他情况保持不变
                course
            }
        }.toMutableList()
    }

    fun addCourse(course: TimeTableMySubject) {
        viewModelScope.launch {
            _addCourseResponse.value = ApiResponse.Loading()

            try {
                withContext(Dispatchers.IO) {
                    courseDao.insertCourse(course)
                }

                val currentState = _uiState.value ?: TimeTableUiState()
                val updatedSubjects = currentState.subjects.toMutableList().apply {
                    add(course)
                }

                updateUiState(updatedSubjects, currentState.term)
                _addCourseResponse.value = ApiResponse.Success(Unit)

            } catch (e: Exception) {
                Log.e(TAG, "Error adding course", e)
                _addCourseResponse.value = ApiResponse.Error("添加课程失败: ${e.message}")
            }
        }
    }

    fun deleteCourse(day: Int, start: Int, curDisplayWeek: Int, term: String) {
        viewModelScope.launch {
            _deleteCourseResponse.value = ApiResponse.Loading()

            try {
                val currentState = _uiState.value ?: TimeTableUiState()
                val updatedSubjects = currentState.subjects.filterNot {
                    it.term == term &&
                            it.start == start &&
                            it.weekday == day &&
                            curDisplayWeek in (it.weeks ?: emptyList()) &&
                            it.isCustom
                }.toMutableList()

                withContext(Dispatchers.IO) {
                    courseDao.clearAllCourses()
                    courseDao.insertCourses(updatedSubjects)
                }

                updateUiState(updatedSubjects, term)
                _deleteCourseResponse.value = ApiResponse.Success(Unit)

            } catch (e: Exception) {
                Log.e(TAG, "Error deleting course", e)
                _deleteCourseResponse.value = ApiResponse.Error("删除课程失败: ${e.message}")
            }
        }
    }

    fun selectWeek(weekInfo: String) {
        val weekNumber = extractWeekNumber(weekInfo)
        _curDisplayWeek.value = weekNumber
        val currentState = _uiState.value ?: TimeTableUiState()
        _uiState.value = currentState.copy(weekInfo = weekInfo)
    }

    fun selectTerm(term: String) {
        loadCourses(term)
    }

    private fun updateUiState(subjects: MutableList<TimeTableMySubject>, term: String) {
        val currentWeekInfo = _uiState.value?.weekInfo ?: "第1周"
        _uiState.value = TimeTableUiState(
            subjects = subjects,
            term = term,
            weekInfo = currentWeekInfo,
            lastUpdate = System.currentTimeMillis()
        )
    }

    private fun extractWeekNumber(weekString: String): Int {
        val regex = Regex("\\d+")
        val matchResult = regex.find(weekString)
        return matchResult?.value?.toInt() ?: 1
    }

    private fun parseWeeks(weekJson: String): WeekJsonInfo {
        val pattern = Pattern.compile(
            "(\\d+(?:-\\d+)?(?:,\\d+(?:-\\d+)?)*)\\((周|单周|双周)?\\)?\\[(\\d{2})(?:-(\\d{2}))?(?:-(\\d{2}))?(?:-(\\d{2}))?节\\]"
        )
        val matcher = pattern.matcher(weekJson)

        if (matcher.find()) {
            val weeksRange = matcher.group(1)
            val weekType = matcher.group(2)
            val startClass = matcher.group(3)?.toIntOrNull() ?: 0
            val endClass = listOfNotNull(
                matcher.group(4)?.toIntOrNull(),
                matcher.group(5)?.toIntOrNull(),
                matcher.group(6)?.toIntOrNull(),
            ).lastOrNull() ?: startClass

            val step = endClass - startClass + 1

            val weeks = try {
                weeksRange?.let { range ->
                    range.split(",").flatMap { part ->
                        if (part.contains("-")) {
                            val (weekStart, weekEnd) = part.split("-").map { it.toInt() }
                            when (weekType) {
                                "单周" -> (weekStart..weekEnd).filter { it % 2 != 0 }
                                "双周" -> (weekStart..weekEnd).filter { it % 2 == 0 }
                                else -> (weekStart..weekEnd).toList()
                            }
                        } else {
                            listOf(part.toInt())
                        }
                    }
                } ?: listOf()
            } catch (e: Exception) {
                listOf()
            }

            return WeekJsonInfo(weeks, startClass, step)
        }

        return WeekJsonInfo(listOf(), 0, 0)
    }

    private fun generateSubjects(
        courses: List<Course>,
        newTerm: String
    ): MutableList<TimeTableMySubject> {
        return courses.map {
            val weekInfo = parseWeeks(it.weeks)
            TimeTableMySubject(
                courseName = it.courseName,
                classroom = it.classroom,
                teacher = it.teacher,
                weeks = weekInfo.weeks,
                start = weekInfo.start,
                step = weekInfo.step,
                weekday = it.weekday.toInt(),
                term = newTerm,
                studentId = studentId,
                studentPassword = studentPassword
            )
        }.toMutableList()
    }

    private fun toLocalCourse(
        courses: List<com.dcelysia.csust_spider.education.data.remote.model.Course>
    ): List<Course> {
        return courses.map {
            Course(
                it.classroom,
                it.courseName,
                it.teacher,
                it.weekday,
                it.weeks
            )
        }
    }

    fun getCurWeek(term: String): Int {
        val startTime = CommonInfo.termMap[term]
        return startTime?.let {
            val zoneId = ZoneId.of("Asia/Shanghai")
            val startDate = runCatching { LocalDate.parse(it.substring(0, 10)) }.getOrNull() ?: return@let 1
            val today = LocalDate.now(zoneId)
            val daysBetween = ChronoUnit.DAYS.between(startDate, today)
            val diffInWeeks = if (daysBetween < 0) 1 else (daysBetween / 7 + 1).toInt()
            diffInWeeks.coerceIn(1, 20)
        } ?: 1
    }

    fun hasTermStarted(term: String): Boolean {
        val startTime = CommonInfo.termMap[term] ?: return true
        val zoneId = ZoneId.of("Asia/Shanghai")
        val startDate = runCatching { LocalDate.parse(startTime.substring(0, 10)) }.getOrNull() ?: return true
        return !LocalDate.now(zoneId).isBefore(startDate)
    }

    data class WeekJsonInfo(val weeks: List<Int>, val start: Int, val step: Int)
}
