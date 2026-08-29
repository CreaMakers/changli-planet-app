package com.zhelearn.CSUSTPlanet.feature.common.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhelearn.CSUSTPlanet.common.cache.CommonInfo
import com.dcelysia.csust_spider.education.data.remote.error.EduHelperError
import com.dcelysia.csust_spider.education.data.remote.model.Campus
import com.dcelysia.csust_spider.education.data.remote.model.DayOfWeek
import com.dcelysia.csust_spider.education.data.remote.repository.EducationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class AvailableClassroomQueryMode {
    CURRENT,
    CUSTOM
}

data class ClassroomAvailabilityItem(
    val rawName: String,
    val availableUntilLabel: String? = null,
    val remainingDurationLabel: String? = null,
    val remainingMinutes: Int = 0
) {
    val searchNameWithoutWhitespace: String = rawName.filterNot { it.isWhitespace() }
}

data class AvailableClassroomUiState(
    val queryMode: AvailableClassroomQueryMode = AvailableClassroomQueryMode.CURRENT,
    val selectedCampus: Campus = Campus.JINPENLING,
    val selectedWeek: Int = 1,
    val selectedDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val selectedSection: Int = 1,
    val currentClassrooms: List<ClassroomAvailabilityItem>? = null,
    val customClassrooms: List<ClassroomAvailabilityItem>? = null,
    val currentContextLabel: String = "",
    val currentTimeLabel: String = "",
    val currentUpdatedAtLabel: String = "",
    val currentEmptyMessage: String = "",
    val searchText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val showError: Boolean = false
) {
    val displayedClassrooms: List<ClassroomAvailabilityItem>?
        get() = when (queryMode) {
            AvailableClassroomQueryMode.CURRENT -> currentClassrooms
            AvailableClassroomQueryMode.CUSTOM -> customClassrooms
        }

    val filteredClassrooms: List<ClassroomAvailabilityItem>?
        get() {
            val classrooms = displayedClassrooms ?: return null
            val searchTextWithoutWhitespace = searchText.filterNot { it.isWhitespace() }
            if (searchTextWithoutWhitespace.isEmpty()) return classrooms
            return buildList {
                for (classroom in classrooms) {
                    if (
                        classroom.searchNameWithoutWhitespace.contains(
                            searchTextWithoutWhitespace,
                            ignoreCase = true
                        )
                    ) {
                        add(classroom)
                    }
                }
            }
        }
}

class AvailableClassroomViewModel : ViewModel() {
    private val repository by lazy { EducationRepository.instance }

    private val _uiState = MutableStateFlow(AvailableClassroomUiState())
    val uiState: StateFlow<AvailableClassroomUiState> = _uiState.asStateFlow()

    init {
        val currentDateSnapshot = currentDateSnapshot()
        _uiState.update {
            it.copy(
                selectedWeek = currentDateSnapshot.week,
                selectedDayOfWeek = currentDateSnapshot.dayOfWeek,
                currentContextLabel = currentDateSnapshot.contextLabel,
                currentTimeLabel = currentDateSnapshot.timeLabel
            )
        }
    }

    fun updateQueryMode(queryMode: AvailableClassroomQueryMode) {
        _uiState.update { it.copy(queryMode = queryMode, searchText = "") }
    }

    fun updateCampus(campus: Campus) {
        _uiState.update {
            it.copy(
                selectedCampus = campus,
                currentClassrooms = null,
                customClassrooms = null,
                currentEmptyMessage = "",
                searchText = ""
            )
        }
    }

    fun updateWeek(week: Int) {
        _uiState.update { it.copy(selectedWeek = week, customClassrooms = null, searchText = "") }
    }

    fun updateDayOfWeek(dayOfWeek: DayOfWeek) {
        _uiState.update {
            it.copy(selectedDayOfWeek = dayOfWeek, customClassrooms = null, searchText = "")
        }
    }

    fun updateSection(section: Int) {
        _uiState.update { it.copy(selectedSection = section, customClassrooms = null, searchText = "") }
    }

    fun updateSearchText(searchText: String) {
        _uiState.update { it.copy(searchText = searchText) }
    }

    fun dismissError() {
        _uiState.update { it.copy(showError = false, errorMessage = "") }
    }

    fun queryAvailableClassrooms() {
        queryClassrooms(AvailableClassroomQueryMode.CUSTOM)
    }

    fun queryCurrentAvailableClassrooms() {
        queryClassrooms(AvailableClassroomQueryMode.CURRENT)
    }

    private fun queryClassrooms(queryMode: AvailableClassroomQueryMode) {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                queryMode = queryMode,
                isLoading = true,
                showError = false,
                errorMessage = ""
            )
        }

        viewModelScope.launch {
            val currentState = _uiState.value
            try {
                when (queryMode) {
                    AvailableClassroomQueryMode.CURRENT -> {
                        val result = withContext(Dispatchers.IO) {
                            queryCurrentClassrooms(currentState.selectedCampus)
                        }
                        _uiState.update { latestState ->
                            latestState.copy(
                                currentClassrooms = result.classrooms,
                                currentContextLabel = result.contextLabel,
                                currentTimeLabel = result.timeLabel,
                                currentUpdatedAtLabel = result.updatedAtLabel,
                                currentEmptyMessage = result.emptyMessage,
                                isLoading = false,
                                searchText = ""
                            )
                        }
                    }

                    AvailableClassroomQueryMode.CUSTOM -> {
                        val customClassrooms = withContext(Dispatchers.IO) {
                            queryCustomClassrooms(
                                campus = currentState.selectedCampus,
                                week = currentState.selectedWeek,
                                dayOfWeek = currentState.selectedDayOfWeek,
                                section = currentState.selectedSection
                            )
                        }
                        _uiState.update { latestState ->
                            latestState.copy(
                                customClassrooms = customClassrooms,
                                isLoading = false,
                                searchText = ""
                            )
                        }
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: EduHelperError.NotLoggedIn) {
                showQueryError("空教室查询暂时不可用，请稍后重试")
            } catch (error: Exception) {
                showQueryError(error.message ?: "查询失败，请稍后重试")
            }
        }
    }

    private suspend fun queryCustomClassrooms(
        campus: Campus,
        week: Int,
        dayOfWeek: DayOfWeek,
        section: Int
    ): List<ClassroomAvailabilityItem> {
        val classroomNames = repository.getAvailableClassrooms(
            campus = campus,
            week = week,
            dayOfWeek = dayOfWeek,
            section = section
        )
        val classrooms = ArrayList<ClassroomAvailabilityItem>(classroomNames.size)
        val addedClassroomNames = HashSet<String>(classroomNames.size)
        for (classroomName in classroomNames) {
            if (!addedClassroomNames.add(classroomName)) continue
            classrooms.add(classroomAvailabilityItem(classroomName))
        }
        classrooms.sortBy { it.rawName }
        return classrooms
    }

    private suspend fun queryCurrentClassrooms(campus: Campus): CurrentClassroomQueryResult {
        val dateSnapshot = currentDateSnapshot()
        val dayPeriod = CLASSROOM_DAY_PERIODS.firstOrNull { period ->
            dateSnapshot.minuteOfDay < period.endMinute
        }
        if (dayPeriod == null) {
            return CurrentClassroomQueryResult(
                classrooms = emptyList(),
                contextLabel = dateSnapshot.contextLabel,
                timeLabel = dateSnapshot.timeLabel,
                updatedAtLabel = "更新于 ${minuteLabel(dateSnapshot.minuteOfDay)}",
                emptyMessage = "今日课程已结束，可切换到按节次查询"
            )
        }

        val queriedSections = CLASSROOM_SECTIONS.filter { section ->
            section.section in dayPeriod.sections && dateSnapshot.minuteOfDay < section.endMinute
        }
        val classroomNamesBySection = coroutineScope {
            val classroomRequests = buildList {
                for (section in queriedSections) {
                    add(
                        async {
                            repository.getAvailableClassrooms(
                                campus = campus,
                                week = dateSnapshot.week,
                                dayOfWeek = dateSnapshot.dayOfWeek,
                                section = section.section
                            ).toHashSet()
                        }
                    )
                }
            }
            buildList {
                for (classroomRequest in classroomRequests) {
                    add(classroomRequest.await())
                }
            }
        }

        val currentClassroomNames = classroomNamesBySection.first()
        val currentSection = queriedSections.first()
        val classrooms = ArrayList<ClassroomAvailabilityItem>(currentClassroomNames.size)
        for (classroomName in currentClassroomNames) {
            var availableUntilMinute = currentSection.endMinute
            for (sectionIndex in 1 until queriedSections.size) {
                if (classroomName !in classroomNamesBySection[sectionIndex]) break
                availableUntilMinute = queriedSections[sectionIndex].endMinute
            }
            classrooms.add(
                classroomAvailabilityItem(
                    classroomName = classroomName,
                    availableUntilMinute = availableUntilMinute,
                    currentMinute = dateSnapshot.minuteOfDay
                )
            )
        }
        classrooms.sortWith(
            compareByDescending<ClassroomAvailabilityItem> { it.remainingMinutes }
                .thenBy { it.rawName }
        )

        return CurrentClassroomQueryResult(
            classrooms = classrooms,
            contextLabel = dateSnapshot.contextLabel,
            timeLabel = "现在 ${minuteLabel(dateSnapshot.minuteOfDay)}，" +
                "查看${dayPeriod.displayName}可用教室",
            updatedAtLabel = "更新于 ${minuteLabel(dateSnapshot.minuteOfDay)}",
            emptyMessage = "${dayPeriod.displayName}没有空闲教室"
        )
    }

    private fun showQueryError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = message,
                showError = true
            )
        }
    }

    private fun getCurrentTerm(): String = CommonInfo.getCurrentTerm()

    private fun getCurrentWeek(term: String): Int = CommonInfo.getCurrentWeekInt(term)

    private fun currentDateSnapshot(): CurrentDateSnapshot {
        val calendar = Calendar.getInstance(SHANGHAI_TIME_ZONE)
        val dayOfWeek = dayOfWeek(calendar)
        val minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * MINUTES_PER_HOUR +
            calendar.get(Calendar.MINUTE)
        val week = getCurrentWeek(getCurrentTerm())
        return CurrentDateSnapshot(
            week = week,
            dayOfWeek = dayOfWeek,
            minuteOfDay = minuteOfDay,
            contextLabel = "第 $week 周 · ${dayOfWeek.displayName()}",
            timeLabel = "现在 ${minuteLabel(minuteOfDay)}，查看能用多久"
        )
    }

    private fun dayOfWeek(calendar: Calendar): DayOfWeek {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            else -> DayOfWeek.SUNDAY
        }
    }

    private data class CurrentClassroomQueryResult(
        val classrooms: List<ClassroomAvailabilityItem>,
        val contextLabel: String,
        val timeLabel: String,
        val updatedAtLabel: String,
        val emptyMessage: String
    )

    private data class CurrentDateSnapshot(
        val week: Int,
        val dayOfWeek: DayOfWeek,
        val minuteOfDay: Int,
        val contextLabel: String,
        val timeLabel: String
    )

    private data class ClassroomSection(
        val section: Int,
        val endMinute: Int
    )

    private data class ClassroomDayPeriod(
        val displayName: String,
        val endMinute: Int,
        val sections: IntRange
    )

    companion object {
        private val SHANGHAI_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Shanghai")
        private val CLASSROOM_SECTIONS = listOf(
            ClassroomSection(section = 1, endMinute = 9 * 60 + 40),
            ClassroomSection(section = 2, endMinute = 11 * 60 + 50),
            ClassroomSection(section = 3, endMinute = 15 * 60 + 40),
            ClassroomSection(section = 4, endMinute = 17 * 60 + 50),
            ClassroomSection(section = 5, endMinute = 21 * 60 + 10)
        )
        private val CLASSROOM_DAY_PERIODS = listOf(
            ClassroomDayPeriod(
                displayName = "上午时段",
                endMinute = 11 * 60 + 50,
                sections = 1..2
            ),
            ClassroomDayPeriod(
                displayName = "下午时段",
                endMinute = 17 * 60 + 50,
                sections = 3..4
            ),
            ClassroomDayPeriod(
                displayName = "晚间时段",
                endMinute = 21 * 60 + 10,
                sections = 5..5
            )
        )
    }
}

private fun classroomAvailabilityItem(
    classroomName: String,
    availableUntilMinute: Int? = null,
    currentMinute: Int? = null
): ClassroomAvailabilityItem {
    val remainingMinutes = if (availableUntilMinute != null && currentMinute != null) {
        (availableUntilMinute - currentMinute).coerceAtLeast(0)
    } else {
        0
    }
    return ClassroomAvailabilityItem(
        rawName = classroomName,
        availableUntilLabel = availableUntilMinute?.let(::minuteLabel),
        remainingDurationLabel = if (availableUntilMinute == null) {
            null
        } else {
            durationLabel(remainingMinutes)
        },
        remainingMinutes = remainingMinutes
    )
}

private fun minuteLabel(minuteOfDay: Int): String {
    val hour = minuteOfDay / MINUTES_PER_HOUR
    val minute = minuteOfDay % MINUTES_PER_HOUR
    return String.format(Locale.CHINA, "%02d:%02d", hour, minute)
}

private fun durationLabel(minutes: Int): String {
    val hours = minutes / MINUTES_PER_HOUR
    val remainingMinutes = minutes % MINUTES_PER_HOUR
    return when {
        hours > 0 && remainingMinutes > 0 -> "约 ${hours}小时${remainingMinutes}分"
        hours > 0 -> "约 ${hours}小时"
        else -> "约 ${remainingMinutes}分钟"
    }
}

private fun DayOfWeek.displayName(): String = when (this) {
    DayOfWeek.MONDAY -> "星期一"
    DayOfWeek.TUESDAY -> "星期二"
    DayOfWeek.WEDNESDAY -> "星期三"
    DayOfWeek.THURSDAY -> "星期四"
    DayOfWeek.FRIDAY -> "星期五"
    DayOfWeek.SATURDAY -> "星期六"
    DayOfWeek.SUNDAY -> "星期天"
}

private const val MINUTES_PER_HOUR = 60
