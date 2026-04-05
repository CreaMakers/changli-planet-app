package com.creamaker.changli_planet_app.feature.mooc.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.feature.mooc.data.local.MoocLocalCache
import com.creamaker.changli_planet_app.utils.ResourceUtil
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocCourse
import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocHomework
import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocTest
import com.dcelysia.csust_spider.mooc.data.remote.dto.PendingAssignmentCourse
import com.dcelysia.csust_spider.mooc.data.remote.repository.MoocRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MoocViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MoocViewModel"
    }

    private val _isSuccessLogin = MutableStateFlow<ApiResponse<Boolean>>(ApiResponse.Loading())
    val isSuccessLogin = _isSuccessLogin.asStateFlow()

    private val _pendingCourse =
        MutableStateFlow<ApiResponse<List<PendingAssignmentCourse>>>(ApiResponse.Loading())
    val pendingCourse = _pendingCourse.asStateFlow()
    private val _pendingHomeworksByCourse = MutableStateFlow<Map<String, ApiResponse<List<MoocHomework>>>>(mapOf())
    val pendingHomeworksByCourse = _pendingHomeworksByCourse.asStateFlow()
    private val _pendingTestsByCourse = MutableStateFlow<Map<String, ApiResponse<List<MoocTest>>>>(mapOf())
    val pendingTestsByCourse = _pendingTestsByCourse.asStateFlow()
    
    private val _expandedCourseIds = MutableStateFlow<Set<String>>(setOf())
    val expandedCourseIds = _expandedCourseIds.asStateFlow()
    
    private val _preloadedCourseIds = MutableStateFlow<Set<String>>(setOf())
    val preloadedCourseIds = _preloadedCourseIds.asStateFlow()
    private val _homeworkCourseIds = MutableStateFlow<Set<String>>(setOf())
    // 新增：每个作业是否一天内到期的状态 map（key = homeworkId）
    private val _isDueSoonMap = MutableStateFlow<Map<String, ApiResponse<Boolean>>>(mapOf())
    val isDueSoonMap = _isDueSoonMap.asStateFlow()

    init {
        restoreFromCache()
    }

    fun toggleCourseExpanded(courseId: String) {
        _expandedCourseIds.value = if (_expandedCourseIds.value.contains(courseId)) {
            _expandedCourseIds.value.minus(courseId)
        } else {
            _expandedCourseIds.value.plus(courseId)
        }
    }
    
    fun markCourseAsPreloaded(courseId: String) {
        _preloadedCourseIds.value = _preloadedCourseIds.value.plus(courseId)
    }
    
    fun isCourseExpanded(courseId: String): Boolean {
        return _expandedCourseIds.value.contains(courseId)
    }
    
    fun isCoursePreloaded(courseId: String): Boolean {
        return _preloadedCourseIds.value.contains(courseId)
    }
    private val repository by lazy { MoocRepository.instance }

    private fun <T> Resource<T>.toApiResponse(): ApiResponse<T> = when (this) {
        is Resource.Success -> ApiResponse.Success(data)
        is Resource.Error -> ApiResponse.Error(msg)
        is Resource.Loading -> ApiResponse.Loading()
    }

    private fun Resource<List<MoocHomework>>.toHomeworkApiResponse(): ApiResponse<List<MoocHomework>> = when (this) {
        is Resource.Success -> ApiResponse.Success(data.toList())
        is Resource.Error -> ApiResponse.Error(msg)
        is Resource.Loading -> ApiResponse.Loading()
    }

    private fun Resource<List<MoocTest>>.toTestApiResponse(): ApiResponse<List<MoocTest>> = when (this) {
        is Resource.Success -> ApiResponse.Success(data.toList())
        is Resource.Error -> ApiResponse.Error(msg)
        is Resource.Loading -> ApiResponse.Loading()
    }

    private fun restoreFromCache() {
        val cachedCourses = MoocLocalCache.getPendingCourses()
        if (cachedCourses.isNotEmpty()) {
            _pendingCourse.value = ApiResponse.Success(cachedCourses)
        }

        val cachedHomeworks = MoocLocalCache.getPendingHomeworksByCourse()
        if (cachedHomeworks.isNotEmpty()) {
            _pendingHomeworksByCourse.value = cachedHomeworks.mapValues { ApiResponse.Success(it.value) }
            cachedHomeworks.values.flatten().forEach { homework ->
                checkIsDueSoon(homework.title, homework.deadline)
            }
        }

        val cachedTests = MoocLocalCache.getPendingTestsByCourse()
        if (cachedTests.isNotEmpty()) {
            _pendingTestsByCourse.value = cachedTests.mapValues { ApiResponse.Success(it.value) }
        }

        val cachedIds = (cachedHomeworks.keys + cachedTests.keys).toSet()
        if (cachedIds.isNotEmpty()) {
            _preloadedCourseIds.value = cachedIds
        }
        if (cachedHomeworks.isNotEmpty()) {
            _homeworkCourseIds.value = cachedHomeworks.keys
        }
    }

    private fun updatePendingCourses(courses: List<PendingAssignmentCourse>) {
        val normalized = courses
            .map {
                val cleanId = it.id.substringBefore("&").replace(Regex("[^0-9]"), "")
                PendingAssignmentCourse(id = cleanId, name = it.name)
            }
            .distinctBy { it.id }
            .sortedBy { it.name }
        _pendingCourse.value = ApiResponse.Success(normalized)
        MoocLocalCache.savePendingCourses(normalized)
    }

    private fun updateHomeworksForCourse(courseId: String, response: ApiResponse<List<MoocHomework>>) {
        _pendingHomeworksByCourse.value = _pendingHomeworksByCourse.value.toMutableMap().apply {
            this[courseId] = response
        }
        if (response is ApiResponse.Success) {
            persistHomeworkCache()
        }
    }

    private fun updateTestsForCourse(courseId: String, response: ApiResponse<List<MoocTest>>) {
        _pendingTestsByCourse.value = _pendingTestsByCourse.value.toMutableMap().apply {
            this[courseId] = response
        }
        if (response is ApiResponse.Success) {
            persistTestCache()
        }
    }

    private fun persistHomeworkCache() {
        val cache = LinkedHashMap<String, List<MoocHomework>>()
        _pendingHomeworksByCourse.value.forEach { (courseId, response) ->
            if (response is ApiResponse.Success) {
                cache[courseId] = response.data
            }
        }
        MoocLocalCache.savePendingHomeworksByCourse(cache)
    }

    private fun persistTestCache() {
        val cache = LinkedHashMap<String, List<MoocTest>>()
        _pendingTestsByCourse.value.forEach { (courseId, response) ->
            if (response is ApiResponse.Success) {
                cache[courseId] = response.data
            }
        }
        MoocLocalCache.savePendingTestsByCourse(cache)
    }

    fun login(account: String, password: String) {
        val loginResult = repository.login(account, password)
        loginResult.onEach {
            _isSuccessLogin.value = it.toApiResponse()
        }.launchIn(viewModelScope)
    }

    fun loginAndFetchCourses(account: String, password: String) {
        viewModelScope.launch {
            try {
                if (account.isEmpty() || password.isEmpty()) {
                    val message = ResourceUtil.getStringRes(R.string.school_account_not_bound_cannot_query)
                    _pendingCourse.value = ApiResponse.Error(message)
                    _isSuccessLogin.value = ApiResponse.Error(message)
                    return@launch
                }
                _isSuccessLogin.value = ApiResponse.Loading()
                if (_pendingCourse.value !is ApiResponse.Success) {
                    _pendingCourse.value = ApiResponse.Loading()
                }
                val loginResult = repository.login(account, password)
                    .filter { it !is Resource.Loading }
                    .first()
                val loginApiResponse = loginResult.toApiResponse()
                _isSuccessLogin.value = loginApiResponse
                if (loginResult is Resource.Success && loginResult.data) {
                    val courseResult = repository.getCourseNamesWithPendingHomeworks()
                        .filter { it !is Resource.Loading }
                        .first()
                    Log.d(TAG, courseResult.toString())
                    when (courseResult) {
                        is Resource.Success -> {
                            val homeworkCourses = courseResult.data.map {
                                val cleanId = it.id.substringBefore("&").replace(Regex("[^0-9]"), "")
                                PendingAssignmentCourse(id = cleanId, name = it.name)
                            }.distinctBy { it.id }
                            _homeworkCourseIds.value = homeworkCourses.map { it.id }.toSet()
                            updatePendingCourses(
                                mergeCourses(
                                    (pendingCourse.value as? ApiResponse.Success)?.data.orEmpty(),
                                    homeworkCourses
                                )
                            )
                            scanCoursesForTests(homeworkCourses)
                        }

                        is Resource.Error -> {
                            _pendingCourse.value = ApiResponse.Error(courseResult.msg)
                        }

                        is Resource.Loading -> Unit
                    }
                } else {
                    val errorMessage = (loginApiResponse as? ApiResponse.Error)?.msg
                        ?: ResourceUtil.getStringRes(R.string.error_unknown)
                    _pendingCourse.value = ApiResponse.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login and fetch courses failed", e)
                _isSuccessLogin.value =
                    ApiResponse.Error(e.message ?: ResourceUtil.getStringRes(R.string.error_unknown))
                _pendingCourse.value =
                    ApiResponse.Error(e.message ?: ResourceUtil.getStringRes(R.string.error_unknown))
            }
        }
    }

    private suspend fun scanCoursesForTests(homeworkCourses: List<PendingAssignmentCourse>) {
        val mergedCourses = LinkedHashMap<String, PendingAssignmentCourse>()
        homeworkCourses.forEach { mergedCourses[it.id] = it }
        val homeworkCourseIds = homeworkCourses.map { it.id }.toSet()

        val courseListResult = repository.getCourses()
            .filter { it !is Resource.Loading }
            .first()

        if (courseListResult !is Resource.Success) {
            if (courseListResult is Resource.Error) {
                Log.e(TAG, "Failed to scan courses for tests: ${courseListResult.msg}")
            }
            return
        }

        courseListResult.data.forEach { rawCourse: MoocCourse ->
            val cleanCourseId = rawCourse.id.substringBefore("&").replace(Regex("[^0-9]"), "")
            val rawResponse = repository.getCourseTests(cleanCourseId)
                .filter { it !is Resource.Loading }
                .first()
            val pendingTests = (rawResponse as? Resource.Success)?.data.orEmpty()
                .filterActivePendingTests()
            if (pendingTests.isNotEmpty()) {
                updateTestsForCourse(cleanCourseId, ApiResponse.Success(pendingTests))
            }
            if (pendingTests.isNotEmpty()) {
                mergedCourses.putIfAbsent(
                    cleanCourseId,
                    PendingAssignmentCourse(id = cleanCourseId, name = rawCourse.name)
                )
                if (cleanCourseId !in homeworkCourseIds && _pendingHomeworksByCourse.value[cleanCourseId] == null) {
                    updateHomeworksForCourse(cleanCourseId, ApiResponse.Success(emptyList()))
                }
            }
        }

        updatePendingCourses(mergedCourses.values.toList())
    }

    fun getCourseHomeworks(courseId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanCourseId = courseId.substringBefore("&").replace(Regex("[^0-9]"), "")
            _pendingHomeworksByCourse.value = _pendingHomeworksByCourse.value.toMutableMap().apply {
                this[courseId] = ApiResponse.Loading()
            }
            try {
                val result: ApiResponse<List<MoocHomework>> = repository.getCourseHomeworks(cleanCourseId)
                    .filter { it !is Resource.Loading }
                    .first()
                    .toHomeworkApiResponse()

                updateHomeworksForCourse(courseId, result)

                if (result is ApiResponse.Success) {
                    result.data.forEach { hw ->
                        val existing = _isDueSoonMap.value[hw.title]
                        if (existing == null || existing is ApiResponse.Error) {
                            checkIsDueSoon(hw.title, hw.deadline)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "failed to get Homeworks:$e")
                updateHomeworksForCourse(courseId, ApiResponse.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun getCourseTests(courseId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanCourseId = courseId.substringBefore("&").replace(Regex("[^0-9]"), "")
            _pendingTestsByCourse.value = _pendingTestsByCourse.value.toMutableMap().apply {
                this[courseId] = ApiResponse.Loading()
            }
            try {
                val rawResult = repository.getCourseTests(cleanCourseId)
                rawResult.collect { result ->
                    val apiResponse = when (result) {
                        is Resource.Success -> ApiResponse.Success(result.data.filterActivePendingTests())
                        is Resource.Error -> ApiResponse.Error(result.msg)
                        is Resource.Loading -> ApiResponse.Loading()
                    }
                    updateTestsForCourse(courseId, apiResponse)
                }
            } catch (e: Exception) {
                Log.e(TAG, "failed to get Tests:$e")
                updateTestsForCourse(courseId, ApiResponse.Error(e.message ?: "Unknown error"))
            }
        }
    }
    
    fun handleCourseClick(courseId: String) {
        val isCurrentlyExpanded = isCourseExpanded(courseId)
        toggleCourseExpanded(courseId)
        
        if (!isCurrentlyExpanded) {
            markCourseAsPreloaded(courseId)
            if (courseId in _homeworkCourseIds.value) {
                if (_pendingHomeworksByCourse.value[courseId] == null || _pendingHomeworksByCourse.value[courseId] is ApiResponse.Error) {
                    getCourseHomeworks(courseId)
                }
            } else if (_pendingHomeworksByCourse.value[courseId] == null) {
                updateHomeworksForCourse(courseId, ApiResponse.Success(emptyList()))
            }
            val cachedTests = _pendingTestsByCourse.value[courseId]
            if (cachedTests == null || cachedTests is ApiResponse.Error) {
                getCourseTests(courseId)
            }
        }
    }

    private fun mergeCourses(
        first: List<PendingAssignmentCourse>,
        second: List<PendingAssignmentCourse>
    ): List<PendingAssignmentCourse> {
        val merged = LinkedHashMap<String, PendingAssignmentCourse>()
        first.forEach { merged[it.id] = it }
        second.forEach { merged[it.id] = it }
        return merged.values.sortedBy { it.name }
    }

    private fun List<MoocTest>.filterActivePendingTests(): List<MoocTest> {
        return filterNot { it.isSubmitted }
            .filter { isWithinCurrentWindow(it.startTime, it.endTime) }
    }

    private fun isWithinCurrentWindow(startTime: String, endTime: String): Boolean {
        val startDate = parseDateOrNull(startTime) ?: return false
        val endDate = parseDateOrNull(endTime) ?: return false
        val now = System.currentTimeMillis()
        return now in startDate.time..endDate.time
    }
    fun checkIsDueSoon(homeworkId: String, deadline: String) {
        viewModelScope.launch {
            _isDueSoonMap.value = _isDueSoonMap.value.toMutableMap().apply {
                this[homeworkId] = ApiResponse.Loading()
            }

            try {
                val res = ApiResponse.Success(isHomeworkDueWithinOneDay(deadline))
                val updated = _isDueSoonMap.value.toMutableMap()
                updated[homeworkId] = res
                _isDueSoonMap.value = updated
            } catch (e: Exception) {
                Log.e(TAG, "checkIsDueSoon failed: ${e.message}", e)
                val updated = _isDueSoonMap.value.toMutableMap()
                updated[homeworkId] = ApiResponse.Error(e.message ?: "计算截止时间失败")
                _isDueSoonMap.value = updated
            }
        }
    }

    private fun isHomeworkDueWithinOneDay(deadline: String): Boolean {
        val deadlineDate = parseDateOrNull(deadline) ?: return false
        val remainingMillis = deadlineDate.time - System.currentTimeMillis()
        return remainingMillis in 1 until TimeUnit.DAYS.toMillis(1)
    }

    private fun parseDateOrNull(dateString: String?): Date? {
        if (dateString.isNullOrBlank()) return null
        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "MM/dd/yyyy HH:mm",
            "MM/dd/yyyy"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.isLenient = false
                return sdf.parse(dateString)
            } catch (_: Exception) {
            }
        }
        return null
    }
}
