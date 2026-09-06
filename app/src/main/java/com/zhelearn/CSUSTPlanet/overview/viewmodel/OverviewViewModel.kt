package com.zhelearn.CSUSTPlanet.overview.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhelearn.CSUSTPlanet.common.data.local.mmkv.StudentInfoManager
import com.zhelearn.CSUSTPlanet.core.PlanetApplication
import com.zhelearn.CSUSTPlanet.feature.mooc.data.MoocDataHelper
import com.zhelearn.CSUSTPlanet.feature.mooc.data.local.MoocLocalCache
import com.zhelearn.CSUSTPlanet.overview.data.OverviewRepository
import com.zhelearn.CSUSTPlanet.overview.ui.model.OverviewMetricUiModel
import com.zhelearn.CSUSTPlanet.overview.ui.model.OverviewUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OverviewViewModel : ViewModel() {
    companion object {
        private const val TAG = "OverviewViewModel"
        private const val REFRESH_INTERVAL_MS = 30 * 60 * 1000L // 30 分钟
    }

    private val repository by lazy { OverviewRepository(PlanetApplication.appContext) }
    private var loadJob: Job? = null

    // 上次网络刷新成功的时间戳
    private var lastRefreshTimeMs: Long = 0L

    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

    // 首次加载也由页面 ON_RESUME 触发，避免创建时加载后立即被取消重启。
    fun onResume() {
        load(preserveVisibleContent = true)
    }

    private fun load(preserveVisibleContent: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val currentState = _uiState.value
            val localState = repository.loadLocalState()
            val displayState = mergeForDisplay(
                current = currentState,
                incoming = localState,
                preserveVisibleContent = preserveVisibleContent
            ).copy(
                isSilentSyncing = localState.isBoundStudent && localState.isOnline
            )
            _uiState.value = displayState

            loadMoocFromCache()

            val shouldRefresh = localState.isBoundStudent && localState.isOnline &&
                (lastRefreshTimeMs == 0L || System.currentTimeMillis() - lastRefreshTimeMs >= REFRESH_INTERVAL_MS)

            if (shouldRefresh) {
                val refreshedState = repository.refreshState()
                lastRefreshTimeMs = System.currentTimeMillis()
                _uiState.value = mergeForDisplay(
                    current = _uiState.value,
                    incoming = refreshedState,
                    preserveVisibleContent = true
                )
            } else if (localState.isBoundStudent && localState.isOnline) {
                Log.d(TAG, "Skip refresh, cache valid (lastRefresh=${lastRefreshTimeMs})")
            }
            if (localState.isBoundStudent && localState.isOnline) {
                // 教务和慕课共享登录会话，保持顺序；慕课按自己的缓存有效期刷新。
                refreshMoocData()
            }
        }
    }

    /**
     * 直接从 MMKV 缓存读取慕课数据并更新 UI
     */
    private suspend fun loadMoocFromCache() {
        val (homeworks, tests) = withContext(Dispatchers.IO) {
            val courseItems = MoocLocalCache.getCourseItems()
            MoocDataHelper.extractPendingHomeworks(courseItems) to
                MoocDataHelper.extractPendingTests(courseItems)
        }
        val isBound = StudentInfoManager.studentId.isNotBlank() && StudentInfoManager.studentPassword.isNotBlank()
        val current = _uiState.value
        _uiState.value = current.copy(
            pendingHomeworks = homeworks,
            pendingHomeworkMessage = when {
                !isBound -> "先绑定学号"
                homeworks.isNotEmpty() -> ""
                else -> "没有数据"
            },
            pendingTests = tests,
            pendingTestMessage = when {
                !isBound -> "先绑定学号"
                tests.isNotEmpty() -> ""
                else -> "没有数据"
            }
        )
    }

    /**
     * 绑定学号后，用 MoocDataHelper 刷新慕课网络数据
     */
    private suspend fun refreshMoocData() {
        val studentId = StudentInfoManager.studentId
        val studentPassword = StudentInfoManager.studentPassword
        if (studentId.isBlank() || studentPassword.isBlank()) return

        try {
            val refreshed = withContext(Dispatchers.IO) {
                if (!MoocDataHelper.shouldAutoRefresh(studentId)) return@withContext false
                MoocDataHelper.fetchMoocCourses(studentId, studentPassword, forceRefresh = false)
                true
            }
            // 未过期时首页已有缓存内容，无需再读取、解析一遍。
            if (refreshed) loadMoocFromCache()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "慕课数据刷新失败，使用缓存数据", e)
        }
    }

    private fun mergeForDisplay(
        current: OverviewUiState,
        incoming: OverviewUiState,
        preserveVisibleContent: Boolean
    ): OverviewUiState {
        if (!preserveVisibleContent) return incoming

        val keepCourses = current.todayCourses.isNotEmpty() && incoming.todayCourses.isEmpty()
        val keepExams = current.upcomingExams.isNotEmpty() && incoming.upcomingExams.isEmpty()

        return incoming.copy(
            metrics = mergeMetrics(current.metrics, incoming.metrics),
            todayCourses = if (keepCourses) current.todayCourses else incoming.todayCourses,
            todayCourseMessage = if (keepCourses) current.todayCourseMessage else incoming.todayCourseMessage,
            upcomingExams = if (keepExams) current.upcomingExams else incoming.upcomingExams,
            examMessage = if (keepExams) current.examMessage else incoming.examMessage,
            // 保留慕课数据不被 incoming 的空值覆盖
            pendingHomeworks = if (current.pendingHomeworks.isNotEmpty() && incoming.pendingHomeworks.isEmpty()) current.pendingHomeworks else incoming.pendingHomeworks,
            pendingHomeworkMessage = if (current.pendingHomeworks.isNotEmpty() && incoming.pendingHomeworks.isEmpty()) current.pendingHomeworkMessage else incoming.pendingHomeworkMessage,
            pendingTests = if (current.pendingTests.isNotEmpty() && incoming.pendingTests.isEmpty()) current.pendingTests else incoming.pendingTests,
            pendingTestMessage = if (current.pendingTests.isNotEmpty() && incoming.pendingTests.isEmpty()) current.pendingTestMessage else incoming.pendingTestMessage
        )
    }

    private fun mergeMetrics(
        current: List<OverviewMetricUiModel>,
        incoming: List<OverviewMetricUiModel>
    ): List<OverviewMetricUiModel> {
        if (current.isEmpty()) return incoming
        if (incoming.isEmpty()) return current

        val currentById = current.associateBy { it.id }
        return incoming.map { metric ->
            val previous = currentById[metric.id]
            if (previous != null && previous.hasVisibleValue() && !metric.hasVisibleValue()) previous else metric
        }
    }

    private fun OverviewMetricUiModel.hasVisibleValue(): Boolean =
        value.isNotBlank() && value != "--"
}
