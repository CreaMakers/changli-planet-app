package com.creamaker.changli_planet_app.feature.common.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.common.cache.CommonInfo
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.dcelysia.csust_spider.education.data.remote.error.EduHelperError
import com.dcelysia.csust_spider.education.data.remote.model.Campus
import com.dcelysia.csust_spider.education.data.remote.model.DayOfWeek
import com.dcelysia.csust_spider.education.data.remote.repository.EducationRepository
import com.dcelysia.csust_spider.education.data.remote.services.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone

data class AvailableClassroomUiState(
    val selectedCampus: Campus = Campus.JINPENLING,
    val selectedWeek: Int = 1,
    val selectedDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val selectedSection: Int = 1,
    val availableClassrooms: List<String>? = null,
    val searchText: String = "",
    val isLoading: Boolean = false,
    val warningMessage: String = "",
    val errorMessage: String = "",
    val showWarning: Boolean = false,
    val showError: Boolean = false
) {
    val filteredAvailableClassrooms: List<String>?
        get() = availableClassrooms?.let { classrooms ->
            if (searchText.isBlank()) {
                classrooms
            } else {
                classrooms.filter { it.contains(searchText.trim(), ignoreCase = true) }
            }
        }
}

class AvailableClassroomViewModel : ViewModel() {
    private val repository by lazy { EducationRepository.instance }

    private val _uiState = MutableStateFlow(AvailableClassroomUiState())
    val uiState: StateFlow<AvailableClassroomUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                selectedWeek = getCurrentWeek(getCurrentTerm()),
                selectedDayOfWeek = getTodayDayOfWeek()
            )
        }
    }

    fun updateCampus(campus: Campus) {
        _uiState.update { it.copy(selectedCampus = campus) }
    }

    fun updateWeek(week: Int) {
        _uiState.update { it.copy(selectedWeek = week) }
    }

    fun updateDayOfWeek(dayOfWeek: DayOfWeek) {
        _uiState.update { it.copy(selectedDayOfWeek = dayOfWeek) }
    }

    fun updateSection(section: Int) {
        _uiState.update { it.copy(selectedSection = section) }
    }

    fun updateSearchText(searchText: String) {
        _uiState.update { it.copy(searchText = searchText) }
    }

    fun dismissWarning() {
        _uiState.update { it.copy(showWarning = false, warningMessage = "") }
    }

    fun dismissError() {
        _uiState.update { it.copy(showError = false, errorMessage = "") }
    }

    fun queryAvailableClassrooms() {
        if (_uiState.value.isLoading) return

        val studentId = StudentInfoManager.studentId
        val studentPassword = StudentInfoManager.studentPassword
        if (studentId.isBlank() || studentPassword.isBlank()) {
            showWarning("请先登录教务系统后再查询数据")
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                showWarning = false,
                warningMessage = "",
                showError = false,
                errorMessage = ""
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            try {
                val loggedIn = AuthService.CheckLoginStates()
                if (!loggedIn) {
                    withContext(Dispatchers.Main) {
                        _uiState.update { state ->
                            state.copy(isLoading = false)
                        }
                        showWarning("请先登录教务系统后再查询数据")
                    }
                    return@launch
                }

                val classrooms = repository.getAvailableClassrooms(
                    campus = currentState.selectedCampus,
                    week = currentState.selectedWeek,
                    dayOfWeek = currentState.selectedDayOfWeek,
                    section = currentState.selectedSection
                ).sorted()

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            availableClassrooms = classrooms,
                            searchText = ""
                        )
                    }
                }
            } catch (error: EduHelperError.NotLoggedIn) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false) }
                    showWarning("请先登录教务系统后再查询数据")
                }
            } catch (error: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isLoading = false) }
                    showError(error.message ?: "查询失败，请稍后重试")
                }
            }
        }
    }

    private fun showWarning(message: String) {
        _uiState.update {
            it.copy(
                warningMessage = message,
                showWarning = true,
                showError = false,
                errorMessage = ""
            )
        }
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(
                errorMessage = message,
                showError = true,
                showWarning = false,
                warningMessage = ""
            )
        }
    }

    private fun getCurrentTerm(): String = CommonInfo.getCurrentTerm()

    private fun getCurrentWeek(term: String): Int = CommonInfo.getCurrentWeekInt(term)

    private fun getTodayDayOfWeek(): DayOfWeek {
        return when (Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai")).get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            else -> DayOfWeek.SUNDAY
        }
    }
}
