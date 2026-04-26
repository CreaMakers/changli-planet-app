package com.creamaker.changli_planet_app.feature.common.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.creamaker.changli_planet_app.feature.common.data.remote.dto.CampusMapFeature
import com.creamaker.changli_planet_app.feature.common.data.remote.dto.CampusMapGeoJson
import com.creamaker.changli_planet_app.feature.common.data.repository.CampusMapRepository
import com.creamaker.changli_planet_app.feature.common.map.Campus
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 校园地图页面的状态容器。
 *
 * 只持有"数据 + 过滤/搜索状态"，不直接持有地图实例；与地图的交互（Polygon 高亮、相机位移）
 * 由 UI 层基于状态 diff 驱动，保持 ViewModel 的无 View 纯粹性。
 */
class CampusMapViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * UI 单一数据源。
     *
     * @param allBuildings 两校全量建筑
     * @param selectedCampus 当前校区筛选：null 表示两校都显示
     * @param selectedCategory 当前分类筛选：null 表示全部
     * @param searchText 搜索关键字
     * @param selectedBuildingId 当前选中的建筑 [CampusMapFeature.stableId]
     * @param isLoading 是否正在拉取网络数据（影响顶部进度条）
     * @param errorMessage 一次性错误消息；UI 消费后调 [dismissError] 清空
     * @param cameraTarget 地图需要移动到的目标状态；一次性事件，UI 消费后调 [consumeCameraTarget] 清空
     */
    data class UiState(
        val allBuildings: List<CampusMapFeature> = emptyList(),
        val selectedCampus: Campus? = null,
        val selectedCategory: String? = null,
        val searchText: String = "",
        val selectedBuildingId: String? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val cameraTarget: CameraTarget? = null
    ) {
        val filteredBuildings: List<CampusMapFeature>
            get() {
                val afterCampus = if (selectedCampus != null) {
                    allBuildings.filter { it.properties.campus == selectedCampus.rawName }
                } else allBuildings

                val afterCategory = if (selectedCategory != null) {
                    afterCampus.filter { it.properties.category == selectedCategory }
                } else afterCampus

                val q = searchText.trim()
                return if (q.isEmpty()) afterCategory
                else afterCategory.filter { fuzzyMatches(it.properties.name, q) }
            }

        /**
         * 下拉筛选里的分类候选：null（=全部）永远在首位，后面是当前校区实际出现过的分类。
         */
        val availableCategories: List<String?>
            get() {
                val src = if (selectedCampus != null) {
                    allBuildings.filter { it.properties.campus == selectedCampus.rawName }
                } else allBuildings
                val uniq = linkedSetOf<String>()
                src.forEach { uniq += it.properties.category }
                return listOf<String?>(null) + uniq.toList()
            }
    }

    /** 相机目标状态：[span] 为纬/经跨度（度），越小越"放大"。 */
    data class CameraTarget(
        val lat: Double,
        val lon: Double,
        val span: Double,
        val token: Long = System.nanoTime()
    )

    private val mmkv by lazy { MMKV.mmkvWithID(MMKV_ID) }

    private val _uiState = MutableStateFlow(
        UiState(selectedCampus = loadPersistedCampus())
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    @Volatile
    private var hasInitialLoaded = false

    /** Activity `onCreate` 后首次调用；二次进入直接复用已有数据。 */
    fun loadInitialIfNeeded() {
        if (hasInitialLoaded) return
        hasInitialLoaded = true

        viewModelScope.launch {
            // 先显示本地缓存，避免白屏
            CampusMapRepository.loadCache()?.let { applyData(it, fromCache = true) }

            // 再拉取最新
            _uiState.update { it.copy(isLoading = true) }
            runCatching { CampusMapRepository.fetchRemoteAndCache() }
                .onSuccess { applyData(it, fromCache = false) }
                .onFailure { e ->
                    Log.w(TAG, "fetch campus map failed", e)
                    _uiState.update { cur ->
                        cur.copy(
                            // 只在当前还没任何数据时弹错误
                            errorMessage = if (cur.allBuildings.isEmpty()) e.message ?: "加载失败" else cur.errorMessage
                        )
                    }
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun applyData(geoJson: CampusMapGeoJson, fromCache: Boolean) {
        _uiState.update { cur ->
            if (geoJson.features == cur.allBuildings) cur
            else cur.copy(allBuildings = geoJson.features)
        }
        // 如首屏没 cameraTarget，则按当前 campus 居中
        if (_uiState.value.cameraTarget == null) centerOnCurrentCampus()
        Log.d(TAG, "applyData fromCache=$fromCache size=${geoJson.features.size}")
    }

    // ------- 用户操作 -------

    fun onCampusSelected(campus: Campus?) {
        if (_uiState.value.selectedCampus == campus) return
        persistCampus(campus)
        _uiState.update {
            it.copy(
                selectedCampus = campus,
                selectedCategory = null,
                selectedBuildingId = null
            )
        }
        centerOnCurrentCampus()
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onSearchTextChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchText = "") }
    }

    fun onBuildingSelected(featureId: String) {
        val feat = _uiState.value.allBuildings.firstOrNull { it.stableId == featureId } ?: return
        val center = centerOf(feat)
        _uiState.update {
            it.copy(
                selectedBuildingId = if (it.selectedBuildingId == featureId) null else featureId,
                // 只有中心点有效时才触发地图平移，避免空 polygon 把相机飞到 (0,0) 大西洋外海
                cameraTarget = if (center != null) {
                    CameraTarget(lat = center.first, lon = center.second, span = 0.004)
                } else it.cameraTarget
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeCameraTarget() {
        _uiState.update { it.copy(cameraTarget = null) }
    }

    // ------- 内部工具 -------

    private fun centerOnCurrentCampus() {
        val cur = _uiState.value
        val target = if (cur.selectedCampus != null) {
            CameraTarget(cur.selectedCampus.centerLat, cur.selectedCampus.centerLon, 0.009)
        } else {
            CameraTarget(Campus.JINPENLING.centerLat, Campus.JINPENLING.centerLon, 0.02)
        }
        _uiState.update { it.copy(cameraTarget = target) }
    }

    /**
     * 计算 Polygon 中心点（经纬度均值）。输入为 WGS-84，返回也是 WGS-84；
     * 交给地图前由 UI 侧做 WGS-84→GCJ-02 转换，避免 VM 里引入地图 SDK 依赖。
     *
     * @return 空 ring / 几何异常时返回 null；调用方需按"不触发相机动画"处理。
     */
    private fun centerOf(feat: CampusMapFeature): Pair<Double, Double>? {
        val ring = feat.geometry.coordinates.firstOrNull().orEmpty()
        if (ring.isEmpty()) return null
        var sumLat = 0.0; var sumLon = 0.0
        ring.forEach { pt ->
            // GeoJSON 规范：[lon, lat]
            sumLon += pt.getOrNull(0) ?: 0.0
            sumLat += pt.getOrNull(1) ?: 0.0
        }
        val n = ring.size
        return sumLat / n to sumLon / n
    }

    private fun loadPersistedCampus(): Campus? =
        mmkv.decodeString(KEY_CAMPUS, null)?.let(Campus::fromRawName)

    private fun persistCampus(campus: Campus?) {
        if (campus == null) mmkv.removeValueForKey(KEY_CAMPUS)
        else mmkv.encode(KEY_CAMPUS, campus.rawName)
    }

    companion object {
        private const val TAG = "CampusMapVM"
        private const val MMKV_ID = "campus_map"
        private const val KEY_CAMPUS = "selected_campus"

        /** 与 iOS 保持一致的模糊匹配：忽略大小写，按字符顺序逐一匹配。 */
        fun fuzzyMatches(source: String, pattern: String): Boolean {
            if (pattern.isEmpty()) return true
            val target = normalizeNumbers(source.lowercase())
            val query = normalizeNumbers(pattern.lowercase())
            var ti = 0; var qi = 0
            while (qi < query.length && ti < target.length) {
                if (query[qi] == target[ti]) qi++
                ti++
            }
            return qi == query.length
        }

        /** 把中文数字（一~十六）替换成阿拉伯数字，方便"5号"搜 "五号"。 */
        private fun normalizeNumbers(input: String): String {
            var r = input
            val pairs = listOf(
                "十一" to "11", "十二" to "12", "十三" to "13",
                "十四" to "14", "十五" to "15", "十六" to "16",
                "十" to "10", "一" to "1", "二" to "2", "三" to "3",
                "四" to "4", "五" to "5", "六" to "6", "七" to "7",
                "八" to "8", "九" to "9"
            )
            pairs.forEach { (k, v) -> r = r.replace(k, v) }
            return r
        }
    }
}
