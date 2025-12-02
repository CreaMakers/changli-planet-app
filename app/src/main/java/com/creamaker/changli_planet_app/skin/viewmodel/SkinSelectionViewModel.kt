package com.creamaker.changli_planet_app.skin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.data.cache.SkinCache
import com.creamaker.changli_planet_app.skin.data.model.Skin
import com.creamaker.changli_planet_app.skin.data.remote.repository.SkinRepository
import com.creamaker.changli_planet_app.skin.helper.SkinFileHelper
import com.creamaker.changli_planet_app.widget.view.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// UI 状态类
data class SkinUiState(
    val skinList: List<Skin> = emptyList(),
    val isLoading: Boolean = false,
    val currentUsingSkin: String = "skin_default",
    val error: String? = null,
    val hasMore: Boolean = true
)

class SkinSelectionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SkinRepository.instance

    // 使用 update 闭包方式更新状态更安全
    private val _uiState = MutableStateFlow(SkinUiState())
    val uiState = _uiState.asStateFlow()

    private var currentPage = 1
    private val pageSize = 20 // 定义 pageSize

    init {
        val currentSkin = SkinCache.getIsUsingSkin()
        _uiState.value = _uiState.value.copy(currentUsingSkin = currentSkin)
        loadNextPage()
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        // 如果正在加载或者已经没有更多数据，直接返回，防止无限刷新
        if (currentState.isLoading || !currentState.hasMore) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)
            try {
                val response = repository.getSkinList(currentPage, pageSize)

                if (response.code == "200" && response.data != null) {
                    val newItems = response.data
                    val count = newItems.size

                    // --- 核心逻辑：判断是否还有更多数据 ---
                    // 逻辑：如果返回的数据个数小于 pageSize (20)，说明是最后一页或没有数据了
                    val isEnd = count < pageSize

                    _uiState.value = _uiState.value.copy(
                        skinList = _uiState.value.skinList + newItems,
                        hasMore = !isEnd, // 如果 isEnd 为 true，则 hasMore 为 false
                        isLoading = false
                    )

                    // 只有当还有更多数据时，才增加页码
                    if (!isEnd) {
                        currentPage++
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = PlanetApplication.appContext.getString(R.string.failure_process)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = PlanetApplication.appContext.getString(R.string.failure_internet)
                )
            }
        }
    }

    /**
     * 处理“使用”按钮点击
     */
    fun applySkin(skin: Skin) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val context = getApplication<Application>()
            val skinFileName = skin.name // 假设皮肤文件名规则

            // 1. 检查是否已下载
            val isDownloaded = SkinCache.getSkinDownloaded(skin.name)

            // 获取目标缓存文件路径
            try {
                if (!isDownloaded) {
                    // --- 未下载：执行下载 ---
                    val response = repository.downloadSkin(skin.name,skin.id)

                    if (response.isSuccessful && response.body() != null) {
                        withContext(Dispatchers.IO) {
                            // 保存流到缓存
                            val file = SkinFileHelper.getCacheFile(context,skin.name)
                            SkinFileHelper.saveStreamToCache(file,response.body()!!.byteStream())
                            SkinManager.setSkin(skin.name)
                        }
                        // 标记已下载
                        SkinCache.saveSkinDownloaded(skin.name)
                    } else {
                        CustomToast.showMessage(context,context.getString(R.string.skin_change_failure))
                    }
                }

                // --- 已下载/下载完成：应用皮肤 ---
                // 这里的 SkinManager.loadSkin 是假设的方法名，请替换为你实际的 loadSkin 方法
                withContext(Dispatchers.Main) {
                    // 调用皮肤管理类加载皮肤
                    SkinManager.setSkin(skin.name)
                    SkinCache.saveIsUsingSkin(skin.name)
                    _uiState.value = _uiState.value.copy(
                        currentUsingSkin = skin.name
                    )
                    CustomToast.showMessage(context,context.getString(R.string.skin_change_success))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                CustomToast.showMessage(context, context.getString(R.string.skin_change_failure))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}