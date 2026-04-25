package com.creamaker.changli_planet_app.feature.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.feature.calendar.data.repository.SemesterCalendarRepository
import com.creamaker.changli_planet_app.feature.calendar.ui.model.SemesterCalendarListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 校历列表页 ViewModel。
 *
 * 页面启动时先展示本地缓存（瞬时回显），再在后台刷新网络数据并回写。
 */
class SemesterCalendarListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SemesterCalendarListUiState())
    val uiState: StateFlow<SemesterCalendarListUiState> = _uiState.asStateFlow()

    init {
        loadLocalThenRefresh()
    }

    fun refresh() {
        loadLocalThenRefresh(forceRefresh = true)
    }

    private fun loadLocalThenRefresh(forceRefresh: Boolean = false) {
        // 先读本地
        val cached = SemesterCalendarRepository.getListSync()
        _uiState.update {
            it.copy(
                isLoading = cached.isNullOrEmpty(),
                items = cached.orEmpty(),
                errorMessage = ""
            )
        }

        // 再异步刷新
        viewModelScope.launch {
            val result = SemesterCalendarRepository.getList(forceRefresh = forceRefresh)
            result.onSuccess { list ->
                _uiState.update {
                    it.copy(isLoading = false, items = list, errorMessage = "")
                }
            }.onFailure { throwable ->
                // 错误展示策略：
                //  - 冷启动路径（forceRefresh=false）+ 有缓存 → 静默，避免打扰
                //  - 用户主动刷新（forceRefresh=true）→ 始终展示错误，让用户有反馈
                val shouldShowError = forceRefresh || cached.isNullOrEmpty()
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
}
