package com.creamaker.changli_planet_app.feature.mooc.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.feature.mooc.data.local.MoocLocalCache
import com.creamaker.changli_planet_app.utils.ResourceUtil
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocCourse
import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocHomework
import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocTest
import com.dcelysia.csust_spider.mooc.data.remote.dto.PendingAssignmentCourse
import com.dcelysia.csust_spider.mooc.data.remote.repository.MoocRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MoocViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MoocViewModel"
        private const val TEST_SCAN_DELAY_MS = 40L
        private const val AUTO_REFRESH_INTERVAL_MS = 86_400_000L
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
    private val _dialogMessage = MutableStateFlow<String?>(null)
    val dialogMessage = _dialogMessage.asStateFlow()
    private val _showForceRefreshPrompt = MutableStateFlow(false)
    val showForceRefreshPrompt = _showForceRefreshPrompt.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
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
        val cachedCourses = MoocLocalCache.getPendingCourses().map {
            PendingAssignmentCourse(
                id = it.id.substringBefore("&").replace(Regex("[^0-9]"), ""),
                name = it.name
            )
        }.distinctBy { it.id }
        if (cachedCourses.isNotEmpty()) {
            _pendingCourse.value = ApiResponse.Success(cachedCourses)
        }

        val cachedHomeworks = MoocLocalCache.getPendingHomeworksByCourse()
            .mapKeys { it.key.substringBefore("&").replace(Regex("[^0-9]"), "") }
        if (cachedHomeworks.isNotEmpty()) {
            _pendingHomeworksByCourse.value = cachedHomeworks.mapValues { ApiResponse.Success(it.value) }
            cachedHomeworks.values.flatten().forEach { homework ->
                checkIsDueSoon(homework.title, homework.deadline)
            }
        }

        val cachedTests = MoocLocalCache.getPendingTestsByCourse()
            .mapKeys { it.key.substringBefore("&").replace(Regex("[^0-9]"), "") }
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

    fun dismissDialog() {
        _dialogMessage.value = null
    }

    fun requestForceRefresh() {
        _showForceRefreshPrompt.value = true
    }

    fun dismissForceRefreshPrompt() {
        _showForceRefreshPrompt.value = false
    }

    fun confirmForceRefresh() {
        _showForceRefreshPrompt.value = false
        loginAndFetchCourses(
            StudentInfoManager.studentId,
            StudentInfoManager.studentPassword,
            forceRefresh = true
        )
    }

    private fun showDialog(message: String) {
        if (message.isBlank()) return
        _dialogMessage.value = message
    }

    private fun hasCachedCourses(): Boolean {
        return (pendingCourse.value as? ApiResponse.Success)?.data?.isNotEmpty() == true
    }

    fun shouldAutoRefreshOnEnter(): Boolean {
        if (!hasCachedCourses()) return true
        val lastRefreshTime = MoocLocalCache.getLastSuccessfulRefreshTime()
        if (lastRefreshTime <= 0L) return true
        return System.currentTimeMillis() - lastRefreshTime >= AUTO_REFRESH_INTERVAL_MS
    }

    private fun shouldRetryNetworkError(message: String?): Boolean {
        return message?.contains("网络错误") == true
    }

    private suspend fun awaitLoginResult(username: String, password: String): Resource<Boolean> {
        return repository.login(username, password)
            .filter { it !is Resource.Loading }
            .first()
    }

    private suspend fun clearMoocSession() {
        runCatching {
            RetrofitUtils.ClearClient("moocClient")
        }.onFailure {
            Log.e(TAG, "Failed to clear MOOC session", it)
        }
    }

    private fun clearLocalMoocCache() {
        runCatching {
            MoocLocalCache.clear()
        }.onFailure {
            Log.e(TAG, "Failed to clear local MOOC cache", it)
        }
    }

    private suspend fun loginWithRetry(username: String, password: String, forceRefresh: Boolean): Resource<Boolean> {
        if (forceRefresh) {
            withContext(Dispatchers.IO) {
                clearMoocSession()
            }

            clearLocalMoocCache()
        }
        val first = awaitLoginResult(username, password)
        if (first !is Resource.Error || !shouldRetryNetworkError(first.msg)) {
            return first
        }
        withContext(Dispatchers.IO) {
            clearMoocSession()
        }
        return awaitLoginResult(username, password)
    }

    private suspend fun <T> executeWithRetry(
        username: String,
        password: String,
        block: suspend () -> Resource<T>
    ): Resource<T> {
        val first = block()
        if (first !is Resource.Error || !shouldRetryNetworkError(first.msg)) {
            return first
        }
        withContext(Dispatchers.IO){
            clearMoocSession()
        }
        val relogin = awaitLoginResult(username, password)
        if (relogin is Resource.Success && relogin.data) {
            return block()
        }
        return if (relogin is Resource.Error) {
            Resource.Error(relogin.msg)
        } else {
            first
        }
    }

    fun loginAndFetchCourses(account: String, password: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                if (account.isEmpty() || password.isEmpty()) {
                    val message = ResourceUtil.getStringRes(R.string.school_account_not_bound_cannot_query)
                    _isSuccessLogin.value = ApiResponse.Error(message)
                    showDialog(message)
                    if (!hasCachedCourses()) {
                        _pendingCourse.value = ApiResponse.Error(message)
                    }
                    return@launch
                }
                _isRefreshing.value = true
                _isSuccessLogin.value = ApiResponse.Loading()
                if (_pendingCourse.value !is ApiResponse.Success) {
                    _pendingCourse.value = ApiResponse.Loading()
                }

                if (forceRefresh) {
                    val loginResult = loginWithRetry(account, password, forceRefresh = true)
                    _isSuccessLogin.value = loginResult.toApiResponse()
                    if (loginResult !is Resource.Success || !loginResult.data) {
                        val errorMessage = (loginResult as? Resource.Error)?.msg
                            ?: ResourceUtil.getStringRes(R.string.error_unknown)
                        showDialog("慕课登录失败：$errorMessage")
                        if (!hasCachedCourses()) {
                            _pendingCourse.value = ApiResponse.Error(errorMessage)
                        }
                        return@launch
                    }
                }

                val courseResult = executeWithRetry(account, password) {
                    repository.getCourseNamesWithPendingHomeworks()
                        .filter { it !is Resource.Loading }
                        .first()
                }
                Log.d(TAG, courseResult.toString())
                when (courseResult) {
                    is Resource.Success -> {
                        _isSuccessLogin.value = ApiResponse.Success(true)
                        val homeworkCourses = courseResult.data.map {
                            val cleanId = it.id.substringBefore("&").replace(Regex("[^0-9]"), "")
                            PendingAssignmentCourse(id = cleanId, name = it.name)
                        }.distinctBy { it.id }
                        _homeworkCourseIds.value = homeworkCourses.map { it.id }.toSet()
                        val mergedCourses = scanCoursesForTests(account, password, homeworkCourses)
                        updatePendingCourses(mergedCourses)
                        MoocLocalCache.markSuccessfulRefresh()
                    }

                    is Resource.Error -> {
                        _isSuccessLogin.value = ApiResponse.Error(courseResult.msg)
                        showDialog("作业课程列表刷新失败：${courseResult.msg}")
                        if (!hasCachedCourses()) {
                            _pendingCourse.value = ApiResponse.Error(courseResult.msg)
                        }
                    }

                    is Resource.Loading -> Unit
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login and fetch courses failed", e)
                val message = e.message ?: ResourceUtil.getStringRes(R.string.error_unknown)
                _isSuccessLogin.value = ApiResponse.Error(message)
                showDialog("慕课数据刷新失败：$message")
                if (!hasCachedCourses()) {
                    _pendingCourse.value = ApiResponse.Error(message)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun scanCoursesForTests(
        account: String,
        password: String,
        homeworkCourses: List<PendingAssignmentCourse>
    ): List<PendingAssignmentCourse> {
        val mergedCourses = LinkedHashMap<String, PendingAssignmentCourse>()
        homeworkCourses.forEach { mergedCourses[it.id] = it }
        val homeworkCourseIds = homeworkCourses.map { it.id }.toSet()
        val scannedTestsByCourse = LinkedHashMap<String, List<MoocTest>>()
        val testOnlyCourseIds = LinkedHashSet<String>()

        val courseListResult = executeWithRetry(account, password) {
            repository.getCourses()
                .filter { it !is Resource.Loading }
                .first()
        }

        if (courseListResult !is Resource.Success) {
            if (courseListResult is Resource.Error) {
                Log.e(TAG, "Failed to scan courses for tests: ${courseListResult.msg}")
                showDialog("测试课程扫描失败：${courseListResult.msg}")
            }
            return mergeCourses((pendingCourse.value as? ApiResponse.Success)?.data.orEmpty(), homeworkCourses)
        }

        courseListResult.data.forEach { rawCourse: MoocCourse ->
            val cleanCourseId = rawCourse.id.substringBefore("&").replace(Regex("[^0-9]"), "")
            delay(TEST_SCAN_DELAY_MS)
            val rawResponse = executeWithRetry(account, password) {
                repository.getCourseTests(cleanCourseId)
                    .filter { it !is Resource.Loading }
                    .first()
            }
            val pendingTests = (rawResponse as? Resource.Success)?.data.orEmpty()
                .filterActivePendingTests()
            if (pendingTests.isNotEmpty()) {
                scannedTestsByCourse[cleanCourseId] = pendingTests
            }
            if (pendingTests.isNotEmpty()) {
                mergedCourses.putIfAbsent(
                    cleanCourseId,
                    PendingAssignmentCourse(id = cleanCourseId, name = rawCourse.name)
                )
                if (cleanCourseId !in homeworkCourseIds) {
                    testOnlyCourseIds += cleanCourseId
                }
            }
        }

        mergedCourses.keys.forEach { courseId ->
            updateTestsForCourse(
                courseId,
                ApiResponse.Success(scannedTestsByCourse[courseId].orEmpty())
            )
        }
        testOnlyCourseIds.forEach { courseId ->
            updateHomeworksForCourse(courseId, ApiResponse.Success(emptyList()))
        }

        return mergeCourses((pendingCourse.value as? ApiResponse.Success)?.data.orEmpty(), mergedCourses.values.toList())
    }

    fun getCourseHomeworks(courseId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanCourseId = courseId.substringBefore("&").replace(Regex("[^0-9]"), "")
            val previous = _pendingHomeworksByCourse.value[courseId]
            _pendingHomeworksByCourse.value = _pendingHomeworksByCourse.value.toMutableMap().apply {
                this[courseId] = if (previous is ApiResponse.Success) previous else ApiResponse.Loading()
            }
            try {
                val result = executeWithRetry(
                    StudentInfoManager.studentId,
                    StudentInfoManager.studentPassword
                ) {
                    Log.d(TAG, "Fetching homeworks for courseId=$cleanCourseId")
                    repository.getCourseHomeworks(cleanCourseId)
                        .filter { it !is Resource.Loading }
                        .first()
                }

                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "Successfully fetched homeworks for courseId=$cleanCourseId: ${result.data.size} items")
                        val apiResponse = ApiResponse.Success(result.data.toList())
                        updateHomeworksForCourse(courseId, apiResponse)
                        result.data.forEach { hw ->
                            val existing = _isDueSoonMap.value[hw.title]
                            if (existing == null || existing is ApiResponse.Error) {
                                checkIsDueSoon(hw.title, hw.deadline)
                            }
                        }
                    }

                    is Resource.Error -> {
                        Log.d(TAG, "Failed to fetch homeworks for courseId=$cleanCourseId: ${result.msg}")
                        showDialog("作业加载失败：${result.msg}")
                        if (previous is ApiResponse.Success) {
                            updateHomeworksForCourse(courseId, previous)
                        } else {
                            updateHomeworksForCourse(courseId, ApiResponse.Error(result.msg))
                        }
                    }

                    is Resource.Loading -> Unit
                }
            } catch (e: Exception) {
                Log.e(TAG, "failed to get Homeworks:$e")
                showDialog("作业加载失败：${e.message ?: "Unknown error"}")
                if (previous == null) {
                    updateHomeworksForCourse(courseId, ApiResponse.Error(e.message ?: "Unknown error"))
                } else {
                    updateHomeworksForCourse(courseId, previous)
                }
            }
        }
    }

    fun getCourseTests(courseId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanCourseId = courseId.substringBefore("&").replace(Regex("[^0-9]"), "")
            val previous = _pendingTestsByCourse.value[courseId]
            _pendingTestsByCourse.value = _pendingTestsByCourse.value.toMutableMap().apply {
                this[courseId] = previous as? ApiResponse.Success ?: ApiResponse.Loading()
            }
            try {
                val rawResult = executeWithRetry(
                    StudentInfoManager.studentId,
                    StudentInfoManager.studentPassword
                ) {
                    repository.getCourseTests(cleanCourseId)
                        .filter { it !is Resource.Loading }
                        .first()
                }

                when (rawResult) {
                    is Resource.Success -> {
                        updateTestsForCourse(
                            courseId,
                            ApiResponse.Success(rawResult.data.filterActivePendingTests())
                        )
                    }

                    is Resource.Error -> {
                        showDialog("测试加载失败：${rawResult.msg}")
                        if (previous is ApiResponse.Success) {
                            updateTestsForCourse(courseId, previous)
                        } else {
                            updateTestsForCourse(courseId, ApiResponse.Error(rawResult.msg))
                        }
                    }

                    is Resource.Loading -> Unit
                }
            } catch (e: Exception) {
                Log.e(TAG, "failed to get Tests:$e")
                showDialog("测试加载失败：${e.message ?: "Unknown error"}")
                if (previous == null) {
                    updateTestsForCourse(courseId, ApiResponse.Error(e.message ?: "Unknown error"))
                } else {
                    updateTestsForCourse(courseId, previous)
                }
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
                    Log.d(TAG, "Loading homeworks for course $courseId")
                    getCourseHomeworks(courseId)
                }
                else{
                    Log.d(TAG, "Homeworks for course $courseId already loaded or loading")
                }
            } else if (_pendingHomeworksByCourse.value[courseId] == null) {
                Log.d(TAG, "Course $courseId has no homework, setting empty list")
                updateHomeworksForCourse(courseId, ApiResponse.Success(emptyList()))
            }
            else{
                Log.d(TAG, "Homeworks for course $courseId already loaded or loading")
            }
            val cachedTests = _pendingTestsByCourse.value[courseId]
            if (cachedTests == null || cachedTests is ApiResponse.Error) {
                getCourseTests(courseId)
            }
        }
        else{
            Log.d(TAG, "Course $courseId collapsed by user")
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
