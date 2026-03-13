package com.creamaker.changli_planet_app.overview.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.common.data.local.room.database.UserDataBase
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.feature.common.compose_ui.FunctionDestination
import com.creamaker.changli_planet_app.feature.common.data.local.entity.Grade
import com.creamaker.changli_planet_app.feature.common.data.local.entity.TimeTableMySubject
import com.creamaker.changli_planet_app.feature.common.data.local.mmkv.ExamArrangementCache
import com.creamaker.changli_planet_app.feature.common.data.local.mmkv.ScoreCache
import com.creamaker.changli_planet_app.feature.common.data.local.room.database.CoursesDataBase
import com.creamaker.changli_planet_app.feature.common.data.repository.ElectricityRepository
import com.creamaker.changli_planet_app.feature.mooc.data.remote.dto.MoocHomework
import com.creamaker.changli_planet_app.feature.mooc.data.remote.dto.PendingAssignmentCourse
import com.creamaker.changli_planet_app.feature.mooc.data.remote.repository.MoocRepository
import com.creamaker.changli_planet_app.overview.data.local.OverviewLocalCache
import com.creamaker.changli_planet_app.overview.data.local.OverviewLocalCache.ElectricityHistoryEntry
import com.creamaker.changli_planet_app.overview.data.local.OverviewLocalCache.ElectricitySnapshot
import com.creamaker.changli_planet_app.overview.ui.model.OverviewCourseUiModel
import com.creamaker.changli_planet_app.overview.ui.model.OverviewExamUiModel
import com.creamaker.changli_planet_app.overview.ui.model.OverviewHomeworkUiModel
import com.creamaker.changli_planet_app.overview.ui.model.OverviewMetricUiModel
import com.creamaker.changli_planet_app.overview.ui.model.OverviewUiState
import com.creamaker.changli_planet_app.utils.NetworkUtil
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.education.data.remote.EducationHelper
import com.dcelysia.csust_spider.education.data.remote.model.ExamArrange
import com.dcelysia.csust_spider.education.data.remote.services.ExamArrangeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.abs

class OverviewRepository(
    context: Context
) {
    private val appContext = context.applicationContext
    private val courseDao by lazy { CoursesDataBase.getDatabase(appContext).courseDao() }
    private val userDao by lazy { UserDataBase.getInstance(appContext).itemDao() }
    private val examCache by lazy { ExamArrangementCache() }
    private val moocRepository by lazy { MoocRepository.instance }
    private val electricityRepository by lazy { ElectricityRepository() }

    suspend fun loadLocalState(): OverviewUiState = withContext(Dispatchers.IO) {
        buildState(
            courses = readLocalCourses(),
            grades = ScoreCache.getGrades().orEmpty(),
            exams = examCache.getExamArrangement().orEmpty().toUiExams(),
            homeworks = OverviewLocalCache.getPendingHomeworks(),
            localOnly = true
        )
    }

    suspend fun refreshState(): OverviewUiState = withContext(Dispatchers.IO) {
        coroutineScope {
            val currentTerm = getCurrentTerm()
            val courseDeferred = async(Dispatchers.IO) { fetchCourses(currentTerm) }
            val gradesDeferred = async(Dispatchers.IO) { fetchGrades() }
            val examsDeferred = async(Dispatchers.IO) { fetchExams(currentTerm) }
            val homeworkDeferred = async(Dispatchers.IO) { fetchPendingHomeworks() }
            val electricityDeferred = async(Dispatchers.IO) { refreshElectricityIfNeeded() }

            val courses = courseDeferred.await() ?: readLocalCourses()
            val grades = gradesDeferred.await() ?: ScoreCache.getGrades().orEmpty()
            val exams = examsDeferred.await() ?: examCache.getExamArrangement().orEmpty().toUiExams()
            val homeworks = homeworkDeferred.await() ?: OverviewLocalCache.getPendingHomeworks()
            electricityDeferred.await()

            buildState(
                courses = courses,
                grades = grades,
                exams = exams,
                homeworks = homeworks,
                localOnly = false
            ).copy(isSilentSyncing = false)
        }
    }

    private suspend fun buildState(
        courses: List<TimeTableMySubject>,
        grades: List<Grade>,
        exams: List<OverviewExamUiModel>,
        homeworks: List<OverviewHomeworkUiModel>,
        localOnly: Boolean
    ): OverviewUiState = withContext(Dispatchers.IO) {
        val studentId = StudentInfoManager.studentId
        val studentPassword = StudentInfoManager.studentPassword
        val currentTerm = getCurrentTerm()
        val currentWeek = getCurrentWeek(currentTerm)
        val currentUser = UserInfoManager.userId.takeIf { it > 0 }?.let { userDao.getUserById(it) }
        val todayCourses = courses.toTodayCourses(currentWeek)
        val electricitySnapshot = OverviewLocalCache.getElectricitySnapshot()
        val isElectricityBound = hasElectricityBinding() || electricitySnapshot != null

        OverviewUiState(
            isRefreshing = false,
            isSilentSyncing = !localOnly && hasNetwork(),
            isBoundStudent = studentId.isNotBlank() && studentPassword.isNotBlank(),
            isOnline = hasNetwork(),
            isElectricityBound = isElectricityBound,
            accountName = currentUser?.account?.takeIf { it.isNotBlank() } ?: UserInfoManager.account.ifBlank { "长理星球" },
            avatarUrl = currentUser?.avatarUrl?.takeIf { it.isNotBlank() } ?: UserInfoManager.userAvatar,
            studentId = studentId,
            dateText = buildDateText(currentTerm, currentWeek),
            currentTerm = currentTerm,
            currentWeek = currentWeek,
            dataSourceLabel = if (localOnly) "本地数据已上屏" else "已完成静默刷新",
            metrics = buildMetrics(grades, electricitySnapshot, isElectricityBound),
            todayCourses = todayCourses,
            todayCourseMessage = when {
                studentId.isBlank() || studentPassword.isBlank() -> "先绑定学号"
                todayCourses.isNotEmpty() -> ""
                else -> "没有数据"
            },
            pendingHomeworks = homeworks.take(3),
            pendingHomeworkMessage = when {
                studentId.isBlank() || studentPassword.isBlank() -> "先绑定学号"
                homeworks.isNotEmpty() -> ""
                else -> "没有数据"
            },
            upcomingExams = exams.take(3),
            examMessage = when {
                studentId.isBlank() || studentPassword.isBlank() -> "先绑定学号"
                exams.isNotEmpty() -> ""
                else -> "没有数据"
            }
        )
    }

    private suspend fun readLocalCourses(): List<TimeTableMySubject> {
        val studentId = StudentInfoManager.studentId
        val studentPassword = StudentInfoManager.studentPassword
        if (studentId.isBlank() || studentPassword.isBlank()) return emptyList()
        return withContext(Dispatchers.IO) {
            courseDao.getCoursesByTerm(getCurrentTerm(), studentId, studentPassword)
        }
    }

    private suspend fun fetchGrades(): List<Grade>? {
        val result = runCatching { EducationHelper.getCourseGrades() }.getOrNull() ?: return null
        if (result.code != "200") return null
        val grades = result.data?.map {
            Grade(
                id = it.courseID,
                item = it.semester,
                name = it.courseName,
                grade = it.grade.toString(),
                flag = it.gradeIdentifier,
                score = it.credit.toString(),
                timeR = it.totalHours.toString(),
                point = it.gradePoint.toString(),
                upperReItem = it.retakeSemester,
                method = it.assessmentMethod,
                property = it.examNature,
                attribute = it.courseAttribute,
                reItem = it.groupName,
                pscjUrl = it.gradeDetailUrl
            )
        }.orEmpty()
        val processedGrades = preprocessGrades(grades)
        if (processedGrades.isNotEmpty()) ScoreCache.saveGrades(processedGrades)
        return processedGrades
    }

    private suspend fun fetchCourses(term: String): List<TimeTableMySubject>? {
        return when (val result = runCatching { EducationHelper.getCourseScheduleByTerm("", term) }.getOrNull()) {
            is Resource.Success -> {
                val subjects = result.data.map {
                    val weekInfo = parseWeeks(it.weeks)
                    TimeTableMySubject(
                        courseName = it.courseName,
                        classroom = it.classroom,
                        teacher = it.teacher,
                        weeks = weekInfo.weeks,
                        start = weekInfo.start,
                        step = weekInfo.step,
                        weekday = it.weekday.toInt(),
                        term = term,
                        studentId = StudentInfoManager.studentId,
                        studentPassword = StudentInfoManager.studentPassword
                    )
                }.distinctBy {
                    "${it.courseName}${it.classroom}${it.teacher}${it.start}${it.step}${it.weekday}${it.term}"
                }
                if (subjects.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        courseDao.clearAllCourses()
                        courseDao.insertCourses(subjects.toMutableList())
                    }
                }
                subjects
            }

            else -> null
        }
    }

    private suspend fun fetchExams(term: String): List<OverviewExamUiModel>? {
        val examResponse = runCatching { ExamArrangeService.getExamArrange(term) }.getOrNull() ?: return null
        if (examResponse !is Resource.Success) return null
        val exams = examResponse.data
        if (exams.isNotEmpty()) {
            examCache.saveExamArrangement(exams)
        }
        return exams.toUiExams()
    }

    private suspend fun fetchPendingHomeworks(): List<OverviewHomeworkUiModel>? {
        val studentId = StudentInfoManager.studentId
        val studentPassword = StudentInfoManager.studentPassword
        if (studentId.isBlank() || studentPassword.isBlank()) return null

        runCatching { moocRepository.login(studentId, studentPassword).toList() }

        var final: ApiResponse<List<PendingAssignmentCourse>>? = null
        runCatching {
            moocRepository.getCourseNamesWithPendingHomeworks().collect {
                if (it !is ApiResponse.Loading) final = it
            }
        }
        val response = final as? ApiResponse.Success ?: return null
        val items = response.data.flatMap { course ->
            fetchCourseHomeworkItems(course)
        }.sortedBy { it.deadlineMillisForSort() }

        OverviewLocalCache.savePendingHomeworks(items)
        return items
    }

    private suspend fun fetchCourseHomeworkItems(course: PendingAssignmentCourse): List<OverviewHomeworkUiModel> {
        var final: ApiResponse<List<MoocHomework>>? = null
        runCatching {
            moocRepository.getCourseHomeworks(course.id).collect {
                if (it !is ApiResponse.Loading) final = it
            }
        }
        val response = final as? ApiResponse.Success ?: return emptyList()
        return response.data
            .filter { it.canSubmit && !it.submitStatus }
            .map { homework ->
                val deadline = parseDateOrNull(homework.deadline)
                val remainingMillis = deadline?.time?.minus(System.currentTimeMillis())
                val isUrgent = remainingMillis != null && remainingMillis in 1 until TimeUnit.DAYS.toMillis(1)
                OverviewHomeworkUiModel(
                    id = "${course.id}_${homework.id}",
                    title = homework.title,
                    deadlineText = homework.deadline,
                    urgencyText = buildHomeworkUrgencyText(remainingMillis),
                    isUrgent = isUrgent
                )
            }
    }

    private fun buildMetrics(
        grades: List<Grade>,
        electricitySnapshot: ElectricitySnapshot?,
        isElectricityBound: Boolean
    ): List<OverviewMetricUiModel> {
        val processedGrades = preprocessGrades(grades)
        val totalCredits = processedGrades.sumOf { it.score.toDoubleOrNull() ?: 0.0 }
        val gpa = if (totalCredits > 0) {
            processedGrades.sumOf { (it.score.toDoubleOrNull() ?: 0.0) * (it.point.toDoubleOrNull() ?: 0.0) } / totalCredits
        } else {
            0.0
        }
        val averageScore = if (totalCredits > 0) {
            processedGrades.sumOf { (it.grade.toDoubleOrNull() ?: 0.0) * (it.score.toDoubleOrNull() ?: 0.0) } / totalCredits
        } else {
            0.0
        }

        val electricitySubtitle = buildElectricitySubtitle(electricitySnapshot, isElectricityBound)
        return listOf(
            OverviewMetricUiModel(
                id = FunctionDestination.ScoreInquiry.name,
                title = "GPA",
                value = if (totalCredits > 0) String.format(Locale.CHINA, "%.2f", gpa) else "--",
                subtitle = if (totalCredits > 0) "平均分: ${String.format(Locale.CHINA, "%.1f", averageScore)}" else "去成绩查询加载数据",
                iconRes = R.drawable.ic_rank,
                accentColor = Color(0xFFE3B92C)
            ),
            OverviewMetricUiModel(
                id = FunctionDestination.Electronic.name,
                title = "电费",
                value = electricitySnapshot?.lastValue?.let { formatMetricNumber(it.toDouble()) } ?: "--",
                unit = if (electricitySnapshot != null) "kWh" else "",
                subtitle = electricitySubtitle,
                iconRes = R.drawable.ic_bill,
                accentColor = Color(0xFF62C466)
            )
        )
    }

    private fun formatMetricNumber(value: Double): String {
        return String.format(Locale.CHINA, "%.2f", value)
            .trimEnd('0')
            .trimEnd('.')
    }

    private fun buildHomeworkUrgencyText(remainingMillis: Long?): String {
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

    private fun OverviewHomeworkUiModel.deadlineMillisForSort(): Long =
        parseDateOrNull(deadlineText)?.time ?: Long.MAX_VALUE

    private fun preprocessGrades(rawData: List<Grade>): List<Grade> {
        return rawData
            .groupBy { it.name }
            .map { (_, grades) ->
                val retakeGrade = grades.find { grade ->
                    grade.upperReItem.isNotBlank() ||
                        grade.property.contains("重修", ignoreCase = true) ||
                        grade.property.contains("补考", ignoreCase = true)
                }

                retakeGrade ?: grades.maxByOrNull { it.grade.toDoubleOrNull() ?: 0.0 }!!
            }
    }

    private fun buildElectricitySubtitle(snapshot: ElectricitySnapshot?, isBound: Boolean): String {
        if (!isBound) return "去绑定电费"
        if (snapshot == null) return "还没有电费缓存"
        val estimate = estimateElectricityDays(snapshot.history, snapshot.lastValue.toDouble())
        if (estimate != null) {
            return "按近期用电，约${estimate}天后耗尽"
        }
        return "更新于 ${SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(snapshot.lastTime)}"
    }

    private fun estimateElectricityDays(
        history: List<ElectricityHistoryEntry>,
        currentValue: Double
    ): Int? {
        if (history.size < 3 || currentValue <= 0.0) return null

        val now = System.currentTimeMillis()
        val recentHistory = history
            .sortedBy { it.timestamp }
            .filter { now - it.timestamp <= TimeUnit.DAYS.toMillis(21) }
            .takeLast(12)

        if (recentHistory.size < 3) return null

        val dailyRates = recentHistory.zipWithNext()
            .mapNotNull { (previous, current) ->
                val deltaHours = (current.timestamp - previous.timestamp).toDouble() / TimeUnit.HOURS.toMillis(1)
                val deltaValue = previous.value - current.value
                when {
                    deltaHours !in 1.0..96.0 -> null
                    deltaValue <= 0.05f -> null
                    else -> UsageRate(
                        dailyUsage = deltaValue / deltaHours * 24.0,
                        weight = (deltaHours.coerceAtMost(24.0) / 24.0).coerceAtLeast(0.2)
                    )
                }
            }

        if (dailyRates.size < 2) return null

        val medianRate = dailyRates.map { it.dailyUsage }.sorted().let { sorted ->
            val mid = sorted.size / 2
            if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2.0 else sorted[mid]
        }
        val filteredRates = dailyRates.filter {
            abs(it.dailyUsage - medianRate) <= maxOf(0.5, medianRate * 0.4)
        }
        if (filteredRates.isEmpty()) return null

        val weightedUsage = filteredRates.sumOf { it.dailyUsage * it.weight } / filteredRates.sumOf { it.weight }
        if (weightedUsage !in 0.5..20.0) return null

        return kotlin.math.ceil(currentValue / weightedUsage).toInt().coerceAtLeast(1)
    }

    private fun hasElectricityBinding(): Boolean {
        return electricityRepository.hasBinding()
    }

    private suspend fun refreshElectricityIfNeeded() {
        if (!hasNetwork()) return
        electricityRepository.refreshIfNeeded()
    }

    private fun hasNetwork(): Boolean = NetworkUtil.getNetworkType(appContext) != NetworkUtil.NetworkType.None

    private fun buildDateText(term: String, week: Int): String {
        val now = Calendar.getInstance()
        val weekday = listOf("", "周日", "周一", "周二", "周三", "周四", "周五", "周六")[now.get(Calendar.DAY_OF_WEEK)]
        return "${now.get(Calendar.MONTH) + 1}月${now.get(Calendar.DAY_OF_MONTH)}日 $weekday  ·  $term 第${week}周"
    }

    private fun getCurrentTerm(): String {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        return when {
            currentMonth >= 9 -> "$currentYear-${currentYear + 1}-1"
            currentMonth >= 2 -> "${currentYear - 1}-${currentYear}-2"
            else -> "${currentYear - 1}-${currentYear}-1"
        }
    }

    private fun getCurrentWeek(term: String): Int {
        val startTime = com.creamaker.changli_planet_app.common.cache.CommonInfo.termMap[term] ?: return 1
        return runCatching {
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(startTime.take(10)) ?: return 1
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            (((today.time - startDate.time) / TimeUnit.DAYS.toMillis(1)) / 7 + 1).toInt().coerceAtLeast(1)
        }.getOrDefault(1)
    }

    private fun parseWeeks(weekJson: String): WeekJsonInfo {
        val pattern = Pattern.compile("(\\d+(?:-\\d+)?(?:,\\d+(?:-\\d+)?)*)\\((周|单周|双周)?\\)?\\[(\\d{2})(?:-(\\d{2}))?(?:-(\\d{2}))?(?:-(\\d{2}))?节\\]")
        val matcher = pattern.matcher(weekJson)
        if (!matcher.find()) return WeekJsonInfo(emptyList(), 0, 0)
        val weeksRange = matcher.group(1)
        val weekType = matcher.group(2)
        val startClass = matcher.group(3)?.toIntOrNull() ?: 0
        val endClass = listOfNotNull(
            matcher.group(4)?.toIntOrNull(),
            matcher.group(5)?.toIntOrNull(),
            matcher.group(6)?.toIntOrNull()
        ).lastOrNull() ?: startClass
        val weeks = weeksRange?.split(",")?.flatMap { part ->
            if (part.contains("-")) {
                val (start, end) = part.split("-").map { it.toInt() }
                when (weekType) {
                    "单周" -> (start..end).filter { it % 2 != 0 }
                    "双周" -> (start..end).filter { it % 2 == 0 }
                    else -> (start..end).toList()
                }
            } else {
                listOf(part.toInt())
            }
        }.orEmpty()
        return WeekJsonInfo(weeks, startClass, endClass - startClass + 1)
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
                return SimpleDateFormat(format, Locale.getDefault()).apply {
                    isLenient = false
                }.parse(dateString)
            } catch (_: ParseException) {
            }
        }
        return null
    }

    private fun List<ExamArrange>.toUiExams(): List<OverviewExamUiModel> =
        mapNotNull { exam ->
            if (exam.courseNameval.isBlank() || exam.examTime.isBlank()) null else {
                OverviewExamUiModel(
                    id = "${exam.courseNameval}_${exam.examTime}",
                    courseName = exam.courseNameval,
                    examTime = exam.examTime,
                    location = listOf(exam.campus, exam.examRoomval).filter { it.isNotBlank() }.joinToString(" · "),
                    badge = if (exam.examTime.contains("明天")) "明天" else "考试"
                )
            }
        }

    private fun List<TimeTableMySubject>.toTodayCourses(currentWeek: Int): List<OverviewCourseUiModel> {
        val todayWeekday = Calendar.getInstance().let {
            when (it.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> 7
                else -> it.get(Calendar.DAY_OF_WEEK) - 1
            }
        }
        val colors = listOf(Color(0xFF5E87F6), Color(0xFF45B979), Color(0xFFF08D3C), Color(0xFFB974F0))
        return filter { it.weekday == todayWeekday && (it.weeks.isNullOrEmpty() || currentWeek in it.weeks.orEmpty()) }
            .sortedBy { it.start }
            .mapIndexed { index, course ->
                OverviewCourseUiModel(
                    id = "${course.courseName}_${course.start}_${course.weekday}_$index",
                    courseName = course.courseName,
                    classroom = course.classroom.orEmpty().ifBlank { "教室待定" },
                    teacher = course.teacher.ifBlank { "教师待定" },
                    timeText = buildCourseTimeText(course.start, course.step),
                    accentLabel = if (course.isCustom) "自定义" else "校园课表",
                    accentColor = colors[index % colors.size]
                )
            }
    }

    private fun buildCourseTimeText(startSection: Int, sectionSpan: Int): String {
        val sectionTimes = listOf(
            "08:00" to "08:45",
            "08:55" to "09:40",
            "10:10" to "10:55",
            "11:05" to "11:50",
            "14:00" to "14:45",
            "14:55" to "15:40",
            "16:10" to "16:55",
            "17:05" to "17:50",
            "19:30" to "20:15",
            "20:25" to "21:10"
        )
        val safeStart = startSection.coerceIn(1, sectionTimes.size)
        val safeEnd = (startSection + sectionSpan - 1).coerceIn(safeStart, sectionTimes.size)
        val startTime = sectionTimes[safeStart - 1].first
        val endTime = sectionTimes[safeEnd - 1].second
        return "$startTime-$endTime · ${safeStart}-${safeEnd}节"
    }

    private data class WeekJsonInfo(
        val weeks: List<Int>,
        val start: Int,
        val step: Int
    )

    private data class UsageRate(
        val dailyUsage: Double,
        val weight: Double
    )
}
