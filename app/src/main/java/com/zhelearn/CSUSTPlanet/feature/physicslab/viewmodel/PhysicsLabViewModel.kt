package com.zhelearn.CSUSTPlanet.feature.physicslab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcelysia.csust_spider.physicsexperiment.PhysicsLabHelper
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabIndex
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabScore
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabSelectableItem
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabTask
import com.zhelearn.CSUSTPlanet.common.data.local.mmkv.StudentInfoManager
import com.zhelearn.CSUSTPlanet.core.network.ApiResponse
import com.zhelearn.CSUSTPlanet.feature.physicslab.data.remote.PhysicsLabQueryResult
import com.zhelearn.CSUSTPlanet.feature.physicslab.data.remote.PhysicsLabRemoteDataSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 物理实验页的三个标签页。 */
enum class PhysicsLabTab(val title: String) {
    Catalog("实验项目"),
    Tasks("我的课表"),
    Scores("我的成绩")
}

data class PhysicsLabUiState(
    val selectedTab: PhysicsLabTab = PhysicsLabTab.Catalog,
    val catalog: ApiResponse<PhysicsLabIndex> = ApiResponse.Loading(),
    val tasks: ApiResponse<List<PhysicsLabTask>> = ApiResponse.Loading(),
    val scores: ApiResponse<List<PhysicsLabScore>> = ApiResponse.Loading(),
    /** 本学期可选：与目录同一批取，但单独记失败态 —— 它挂了不影响整页 */
    val selectableCourses: List<PhysicsLabSelectableItem> = emptyList(),
    val selectableQuota: String? = null,
    val selectableFailed: Boolean = false,
    val selectableLoading: Boolean = false,
    /** 正在请求：首次加载由各标签页自己的 Loading 承担，这里只管"已有内容时的刷新" */
    val refreshing: Boolean = false,
    /** 展示的数据来自本地缓存（网络没通），UI 给一条提示 */
    val cacheTabs: Set<PhysicsLabTab> = emptySet(),
    /** 会话层状态，与具体标签页无关，由最近一次请求的结果决定 */
    val needsPlatformPassword: Boolean = false,
    val needsRebind: Boolean = false,
    val showPlatformPasswordDialog: Boolean = false,
    val platformPasswordError: String = ""
)

/**
 * 物理实验页 ViewModel：三个标签页各自独立请求、独立缓存，进页面只拉当前页。
 * 平台密码缺失/错误时弹输入框，密码由库自己保存。
 */
class PhysicsLabViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PhysicsLabUiState())
    val uiState: StateFlow<PhysicsLabUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load(PhysicsLabTab.Catalog)
    }

    fun selectTab(tab: PhysicsLabTab) {
        if (tab == _uiState.value.selectedTab) return
        // 已经有数据的页只切换、不重复请求；错误态或空态则重新拉一次
        if (responseOf(tab) is ApiResponse.Success) {
            _uiState.update { it.copy(selectedTab = tab) }
        } else {
            load(tab)
        }
    }

    /** 刷新当前标签页（网络优先，失败回退缓存）。 */
    fun refresh() {
        load(_uiState.value.selectedTab)
    }

    /** 用户填完平台密码：先存库，再用当前标签页验证一次。密码不对会再次弹框提示。 */
    fun submitPlatformPassword(password: String) {
        if (password.isBlank()) return
        PhysicsLabHelper.setPlatformPassword(password)
        _uiState.update { it.copy(showPlatformPasswordDialog = false, platformPasswordError = "") }
        load(_uiState.value.selectedTab)
    }

    fun showPlatformPasswordDialog() {
        _uiState.update { it.copy(showPlatformPasswordDialog = true) }
    }

    fun dismissPlatformPasswordDialog() {
        _uiState.update { it.copy(showPlatformPasswordDialog = false) }
    }

    private fun load(tab: PhysicsLabTab) {
        // 切页/连点刷新时丢掉上一次请求，避免旧结果盖住新页
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val studentId = StudentInfoManager.studentId
            markLoading(tab)

            when (tab) {
                PhysicsLabTab.Catalog -> {
                    // 先标记加载中，免得请求回来前折叠头显示"0 项"
                    _uiState.update { it.copy(selectableLoading = true) }
                    val result = PhysicsLabRemoteDataSource.loadIndex(studentId)
                    ensureActive()
                    // 统一认证是单处登录：与目录并发会互相踢掉会话，所以顺序取
                    val selectable = PhysicsLabRemoteDataSource.loadSelectable(studentId)
                    ensureActive()
                    val selectableData = when (selectable.response) {
                        is ApiResponse.Success -> selectable.response.data
                        else -> null
                    }
                    _uiState.update {
                        it.sessionFlags(result).copy(
                            catalog = result.response,
                            selectableCourses = selectableData?.courses.orEmpty(),
                            selectableQuota = selectableData?.quotaText,
                            selectableFailed = selectableData == null,
                            selectableLoading = false
                        )
                    }
                }

                PhysicsLabTab.Tasks -> {
                    val result = PhysicsLabRemoteDataSource.loadTasks(studentId)
                    ensureActive()
                    _uiState.update { it.sessionFlags(result).copy(tasks = result.response) }
                }

                PhysicsLabTab.Scores -> {
                    val result = PhysicsLabRemoteDataSource.loadScores(studentId)
                    ensureActive()
                    _uiState.update { it.sessionFlags(result).copy(scores = result.response) }
                }
            }
        }
    }

    /** 进请求前：标记刷新中；该页还没有成功数据时退回 Loading（否则保留旧内容，避免闪白）。 */
    private fun markLoading(tab: PhysicsLabTab) {
        _uiState.update { state ->
            state.copy(
                selectedTab = tab,
                refreshing = true,
                catalog = if (tab == PhysicsLabTab.Catalog && state.catalog !is ApiResponse.Success) {
                    ApiResponse.Loading()
                } else {
                    state.catalog
                },
                tasks = if (tab == PhysicsLabTab.Tasks && state.tasks !is ApiResponse.Success) {
                    ApiResponse.Loading()
                } else {
                    state.tasks
                },
                scores = if (tab == PhysicsLabTab.Scores && state.scores !is ApiResponse.Success) {
                    ApiResponse.Loading()
                } else {
                    state.scores
                }
            )
        }
    }

    /** 每次请求结束都刷新会话层状态与刷新标记；这些与标签页无关，所以三个分支共用。 */
    private fun PhysicsLabUiState.sessionFlags(result: PhysicsLabQueryResult<*>): PhysicsLabUiState = copy(
        refreshing = false,
        cacheTabs = if (result.fromCache) cacheTabs + selectedTab else cacheTabs - selectedTab,
        needsPlatformPassword = result.needsPlatformPassword,
        needsRebind = result.needsRebind,
        showPlatformPasswordDialog = result.needsPlatformPassword,
        platformPasswordError = if (result.needsPlatformPassword) result.error?.message.orEmpty() else ""
    )

    private fun responseOf(
        tab: PhysicsLabTab,
        state: PhysicsLabUiState = _uiState.value
    ): ApiResponse<*> = when (tab) {
        PhysicsLabTab.Catalog -> state.catalog
        PhysicsLabTab.Tasks -> state.tasks
        PhysicsLabTab.Scores -> state.scores
    }
}
