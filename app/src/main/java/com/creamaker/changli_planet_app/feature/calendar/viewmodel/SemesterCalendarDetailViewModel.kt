package com.creamaker.changli_planet_app.feature.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.feature.calendar.data.repository.SemesterCalendarRepository
import com.creamaker.changli_planet_app.feature.calendar.ui.model.SemesterCalendarDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 校历详情页 ViewModel。
 *
 * 按学期代码加载；同样遵循 **本地优先 + 后台静默刷新** 策略。
 */
class SemesterCalendarDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SemesterCalendarDetailUiState())
    val uiState: StateFlow<SemesterCalendarDetailUiState> = _uiState.asStateFlow()

    private var currentSemesterCode: String = ""

    fun load(semesterCode: String, forceRefresh: Boolean = false) {
        if (semesterCode.isBlank()) return
        currentSemesterCode = semesterCode

        val cached = SemesterCalendarRepository.getDetailSync(semesterCode)
        _uiState.update {
            it.copy(
                isLoading = cached == null,
                detail = cached,
                errorMessage = ""
            )
        }

        viewModelScope.launch {
            val result = SemesterCalendarRepository.getDetail(semesterCode, forceRefresh = forceRefresh)
            result.onSuccess { detail ->
                _uiState.update {
                    it.copy(isLoading = false, detail = detail, errorMessage = "")
                }
            }.onFailure { throwable ->
                // 错误展示策略：
                //  - 冷启动路径（forceRefresh=false）+ 有缓存 → 静默
                //  - 用户主动刷新（forceRefresh=true）→ 始终展示错误，让用户有反馈
                val shouldShowError = forceRefresh || cached == null
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (shouldShowError) {
                            throwable.message ?: "加载失败"
                        } else {
                            ""
                        }
                    )
                }
            }
        }
    }

    fun refresh() {
        if (currentSemesterCode.isNotBlank()) load(currentSemesterCode, forceRefresh = true)
    }
}
