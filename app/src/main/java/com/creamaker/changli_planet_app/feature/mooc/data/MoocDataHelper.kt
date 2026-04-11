package com.creamaker.changli_planet_app.feature.mooc.data

import android.util.Log
import com.creamaker.changli_planet_app.feature.mooc.data.local.MoocLocalCache
import com.creamaker.changli_planet_app.feature.mooc.data.model.CourseItem
import com.creamaker.changli_planet_app.feature.mooc.data.model.HomeworkItem
import com.creamaker.changli_planet_app.feature.mooc.data.model.cleanCourseId
import com.creamaker.changli_planet_app.overview.ui.model.OverviewHomeworkUiModel
import com.creamaker.changli_planet_app.overview.ui.model.OverviewTestUiModel
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocHomework
import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocTest
import com.dcelysia.csust_spider.mooc.data.remote.dto.PendingAssignmentCourse
import com.dcelysia.csust_spider.mooc.data.remote.repository.MoocRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object MoocDataHelper {
    private const val TAG = "MoocDataHelper"
    private const val AUTO_REFRESH_INTERVAL_MS = 10800_000L

    private val repository by lazy { MoocRepository.instance }

    suspend fun clearMoocSession() {
        runCatching {
            RetrofitUtils.ClearClient("moocClient")
        }.onFailure {
            Log.e(TAG, "Failed to clear MOOC session", it)
        }
    }

    private suspend fun awaitLoginResult(username: String, password: String): Resource<Boolean> {
        return repository.login(username, password)
            .filter { it !is Resource.Loading }
            .first()
    }

    suspend fun <T> executeWithRetry(
        username: String,
        password: String,
        block: suspend () -> Resource<T>
    ): Resource<T> {
        val first = block()
        if (first !is Resource.Error || !shouldRetryNetworkError(first.msg)) {
            return first
        }
        withContext(Dispatchers.IO) {
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

    private fun shouldRetryNetworkError(message: String?): Boolean {
        return message?.contains("网络错误") == true
    }

    /**
     * 核心网络拉取逻辑，从 MoocViewModel.loginAndFetchCourses 拆出。
     * 只管数据获取和缓存写入，不管 UI 状态。
     * @return 成功时返回课程列表，失败时抛出异常
     */
    suspend fun fetchMoocCourses(
        username: String,
        password: String,
        forceRefresh: Boolean = false
    ): List<CourseItem> = withContext(Dispatchers.IO) {
        if (forceRefresh) {
            clearMoocSession()
            val loginResult = awaitLoginResult(username, password)
            if (loginResult !is Resource.Success || !loginResult.data) {
                val errorMsg = (loginResult as? Resource.Error)?.msg ?: "慕课登录失败"
                throw IllegalStateException(errorMsg)
            }
        }

        val courseResult = executeWithRetry(username, password) {
            repository.getCourseNamesWithPendingHomeworks()
                .filter { it !is Resource.Loading }
                .first()
        }

        when (courseResult) {
            is Resource.Success -> {
                val homeworkCourses = courseResult.data.map {
                    PendingAssignmentCourse(id = it.id.cleanCourseId(), name = it.name)
                }.distinctBy { it.id }

                val homeworkCourseData = coroutineScope {
                    homeworkCourses.map { course ->
                        async {
                            val homeworks = runCatching {
                                executeWithRetry(username, password) {
                                    repository.getCourseHomeworks(course.id)
                                        .filter { it !is Resource.Loading }
                                        .first()
                                }
                            }.getOrNull()

                            val tests = runCatching {
                                executeWithRetry(username, password) {
                                    repository.getCourseTests(course.id)
                                        .filter { it !is Resource.Loading }
                                        .first()
                                }
                            }.getOrNull()

                            CourseLoadResult(course, homeworks, tests)
                        }
                    }.awaitAll()
                }

                val allCoursesResult = executeWithRetry(username, password) {
                    repository.getCourses()
                        .filter { it !is Resource.Loading }
                        .first()
                }

                val homeworkCourseIds = homeworkCourses.map { it.id }.toSet()
                val allCourses = (allCoursesResult as? Resource.Success)?.data.orEmpty()
                val remainingCourses = allCourses
                    .map { it.copy(id = it.id.cleanCourseId()) }
                    .distinctBy { it.id }
                    .filter { it.id !in homeworkCourseIds }

                val testOnlyData = coroutineScope {
                    remainingCourses.map { rawCourse ->
                        async {
                            val tests = runCatching {
                                executeWithRetry(username, password) {
                                    repository.getCourseTests(rawCourse.id)
                                        .filter { it !is Resource.Loading }
                                        .first()
                                }
                            }.getOrNull()

                            val pendingTests = (tests as? Resource.Success)?.data
                                ?.filterActivePendingTests().orEmpty()

                            if (pendingTests.isNotEmpty()) {
                                CourseLoadResult(
                                    course = PendingAssignmentCourse(id = rawCourse.id, name = rawCourse.name),
                                    homeworks = Resource.Success(emptyList<MoocHomework>()),
                                    tests = tests
                                )
                            } else null
                        }
                    }.awaitAll()
                }.filterNotNull()

                val courseItems = buildCourseItems(homeworkCourseData + testOnlyData)

                MoocLocalCache.saveCourseItems(courseItems)
                MoocLocalCache.markSuccessfulRefresh()

                courseItems
            }

            is Resource.Error -> {
                throw IllegalStateException("作业课程列表刷新失败：${courseResult.msg}")
            }

            is Resource.Loading -> {
                throw IllegalStateException("意外的 Loading 状态")
            }
        }
    }

    private data class CourseLoadResult(
        val course: PendingAssignmentCourse,
        val homeworks: Resource<List<MoocHomework>>?,
        val tests: Resource<List<MoocTest>>?
    )

    private fun buildCourseItems(results: List<CourseLoadResult>): List<CourseItem> {
        return results.map { result ->
            val homeworkList = (result.homeworks as? Resource.Success)?.data.orEmpty()
            val testList = (result.tests as? Resource.Success)?.data.orEmpty()

            CourseItem(
                course = result.course,
                homeworks = homeworkList.map { hw ->
                    HomeworkItem(
                        homework = hw,
                        isDueSoon = isHomeworkDueWithinOneDay(hw.deadline)
                    )
                },
                tests = testList.filterActivePendingTests()
            )
        }.sortedBy { it.course.name }
    }

    fun extractPendingHomeworks(courseItems: List<CourseItem>): List<OverviewHomeworkUiModel> {
        return courseItems.flatMap { courseItem ->
            courseItem.homeworks
                .filter { it.homework.canSubmit && !it.homework.submitStatus }
                .map { hwItem ->
                    val deadlineDate = parseDateOrNull(hwItem.homework.deadline)
                    val remainingMillis = deadlineDate?.time?.minus(System.currentTimeMillis())
                    val isUrgent = remainingMillis != null && remainingMillis in 1 until TimeUnit.DAYS.toMillis(1)
                    OverviewHomeworkUiModel(
                        id = "${courseItem.course.id}_${hwItem.homework.id}",
                        title = hwItem.homework.title,
                        courseName = courseItem.course.name,
                        deadlineText = hwItem.homework.deadline,
                        urgencyText = buildUrgencyText(remainingMillis),
                        isUrgent = isUrgent
                    )
                }
        }.sortedBy { item ->
            parseDateOrNull(item.deadlineText)?.time ?: Long.MAX_VALUE
        }
    }

    fun extractPendingTests(courseItems: List<CourseItem>): List<OverviewTestUiModel> {
        return courseItems.flatMap { courseItem ->
            courseItem.tests.map { test ->
                val endDate = parseDateOrNull(test.endTime)
                val remainingMillis = endDate?.time?.minus(System.currentTimeMillis())
                val isUrgent = remainingMillis != null && remainingMillis in 1 until TimeUnit.DAYS.toMillis(1)
                OverviewTestUiModel(
                    id = "${courseItem.course.id}_${test.title}",
                    title = test.title,
                    courseName = courseItem.course.name,
                    timeText = "${test.startTime} - ${test.endTime}",
                    urgencyText = buildUrgencyText(remainingMillis),
                    isUrgent = isUrgent
                )
            }
        }.sortedBy { item ->
            parseDateOrNull(item.timeText.substringAfterLast(" - ", missingDelimiterValue = item.timeText))?.time ?: Long.MAX_VALUE
        }
    }

    fun shouldAutoRefresh(): Boolean {
        val cachedItems = MoocLocalCache.getCourseItems()
        if (cachedItems.isEmpty()) return true
        val lastRefreshTime = MoocLocalCache.getLastSuccessfulRefreshTime()
        if (lastRefreshTime <= 0L) return true
        return System.currentTimeMillis() - lastRefreshTime >= AUTO_REFRESH_INTERVAL_MS
    }

    fun buildUrgencyText(remainingMillis: Long?): String {
        if (remainingMillis == null) return ""
        if (remainingMillis <= 0L) return "已截止"
        if (remainingMillis < TimeUnit.HOURS.toMillis(1)) {
            val minutes = (remainingMillis / TimeUnit.MINUTES.toMillis(1)).coerceAtLeast(1)
            return "${minutes}分钟内截止"
        }
        if (remainingMillis < TimeUnit.DAYS.toMillis(1)) {
            val hours = (remainingMillis / TimeUnit.HOURS.toMillis(1)).coerceAtLeast(1)
            return "${hours}小时内截止"
        }
        val days = (remainingMillis / TimeUnit.DAYS.toMillis(1)).coerceAtLeast(1)
        return "${days}天内截止"
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

    fun isHomeworkDueWithinOneDay(deadline: String): Boolean {
        val deadlineDate = parseDateOrNull(deadline) ?: return false
        val remainingMillis = deadlineDate.time - System.currentTimeMillis()
        return remainingMillis in 1 until TimeUnit.DAYS.toMillis(1)
    }

    fun parseDateOrNull(dateString: String?): Date? {
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
