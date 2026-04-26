package com.creamaker.changli_planet_app.feature.common.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.Polygon
import com.amap.api.maps.model.PolygonOptions
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.ComposeActivity
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.feature.common.data.remote.dto.CampusMapFeature
import com.creamaker.changli_planet_app.feature.common.map.Campus
import com.creamaker.changli_planet_app.feature.common.map.CampusCoordinateConverter
import com.creamaker.changli_planet_app.feature.common.viewModel.CampusMapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * 校园地图页面。
 *
 * 设计要点：
 * - 地图使用高德「轻量版地图 SDK」本地 jar（`app/libs/Lite3DMap.jar`）。
 * - 整个 Activity 基于 Compose，[MapView] 通过 [AndroidView] 嵌入；
 *   生命周期通过 [DisposableEffect] + [LifecycleEventObserver] 正确转接。
 * - 定位蓝点通过实现 [LocationSource] + 外部 [AMapLocationClient] 喂给地图（轻量版 SDK 官方要求）。
 * - WGS-84 坐标在 UI 层转 GCJ-02，避免把地图 SDK 依赖扩散到 ViewModel。
 * - Polygon 实例在 `onMapReady` 时一次性建好并缓存；过滤与选中态只调 `setVisible`/`setFillColor`，
 *   不做 `remove+addPolygon` 抖动，保证滚动/筛选时无卡顿。
 */
class CampusMapActivity : ComposeActivity() {

    private val viewModel: CampusMapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadInitialIfNeeded()
        setComposeContent {
            CampusMapScreen(
                viewModel = viewModel,
                onBack = ::finish
            )
        }
    }
}

// ============================== 顶层 UI ==============================


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampusMapScreen(
    viewModel: CampusMapViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // 限制 Sheet 展开高度：最大 55% 屏幕，避免"一滑到顶"遮挡整张地图
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetMaxHeight = screenHeight * 0.55f

    // 地图引用：Compose 内跨 recomposition 共享；AndroidView.factory 只执行一次
    var mapRef by remember { mutableStateOf<AMap?>(null) }
    val polygonMap = remember { mutableMapOf<String, Polygon>() }

    // polygonMap 本身不是 SnapshotState，无法驱动 Compose 重组；
    // 每次增删 polygon 后用 polygonVersion++ 通知下游 LaunchedEffect 重新跑一次刷新逻辑。
    var polygonVersion by remember { mutableIntStateOf(0) }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    )
    val scope = rememberCoroutineScope()

    // 主题切换时更新地图 day/night，不用销毁重建
    LaunchedEffect(mapRef, isDark) {
        mapRef?.mapType = if (isDark) AMap.MAP_TYPE_NIGHT else AMap.MAP_TYPE_NORMAL
    }

    // 相机目标：uiState.cameraTarget 变化时平移地图（一次性事件，消费后清空）
    LaunchedEffect(uiState.cameraTarget, mapRef) {
        val map = mapRef ?: return@LaunchedEffect
        val target = uiState.cameraTarget ?: return@LaunchedEffect
        val gcj = CampusCoordinateConverter.wgs84ToGcj02(target.lat, target.lon)
        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(LatLng(gcj[0], gcj[1]), spanToZoom(target.span))
            )
        )
        viewModel.consumeCameraTarget()
    }

    // 全量数据变化 → 建/删 Polygon
    LaunchedEffect(uiState.allBuildings, mapRef) {
        val map = mapRef ?: return@LaunchedEffect
        val changed = rebuildPolygonsIfNeeded(map, polygonMap, uiState.allBuildings)
        if (changed) polygonVersion++
    }

    // 过滤/选中 → 更新 Polygon 可见性与高亮
    LaunchedEffect(uiState.filteredBuildings, uiState.selectedBuildingId, polygonVersion) {
        if (polygonMap.isEmpty()) return@LaunchedEffect
        val visibleIds = uiState.filteredBuildings.mapTo(HashSet()) { it.stableId }
        polygonMap.forEach { (id, polygon) ->
            val feat = uiState.allBuildings.firstOrNull { it.stableId == id } ?: return@forEach
            val visible = id in visibleIds
            polygon.isVisible = visible
            if (visible) {
                val selected = id == uiState.selectedBuildingId
                val baseColor = colorForCategory(feat.properties.category)
                polygon.fillColor = withAlpha(baseColor, if (selected) 0xCC else 0x80)
                polygon.strokeColor = if (selected) 0xFF222222.toInt() else baseColor
                polygon.strokeWidth = if (selected) 6f else 3f
                polygon.zIndex = if (selected) 2f else 1f
            }
        }
    }

    // 错误一次性 Toast
    LaunchedEffect(uiState.errorMessage) {
        val msg = uiState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        viewModel.dismissError()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 220.dp,
        sheetContainerColor = colors.bgCardColor,
        sheetContentColor = colors.primaryTextColor,
        sheetContent = {
            // 关键：限制 sheet 内容最大高度 = 屏幕 55%，防止向上滑一下就全屏遮挡地图。
            // sheet 最多展开到内容高度，这里由 heightIn 兜底。
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = sheetMaxHeight)
            ) {
                BuildingsListSheet(
                    uiState = uiState,
                    onSearchTextChanged = viewModel::onSearchTextChanged,
                    onClearSearch = viewModel::clearSearch,
                    onCategorySelected = viewModel::onCategorySelected,
                    onBuildingSelected = viewModel::onBuildingSelected,
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AMapContent(
                isDark = isDark,
                onMapReady = { mapRef = it }
            )

            // 顶部控件：只吃 statusBars.top（避免底部被 Navigation bar 内边距影响），
            // 再额外留 8dp 呼吸感
            TopBar(
                selectedCampus = uiState.selectedCampus,
                onBack = onBack,
                onCampusSelected = viewModel::onCampusSelected,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp)
            )

            if (uiState.isLoading) {
                // TopBar 高度 ≈ 状态栏 + 8dp + 34dp(IconButton)，向下留 16dp 间隙再放进度条
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                        .padding(top = 58.dp)
                )
            }
        }
    }

    // 首次进入把 sheet 展开到 partial，便于看到建筑列表
    LaunchedEffect(Unit) {
        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
    }
}

/** 高德 zoom 级别 ≈ log2(360/span)，轻量地图可用 zoom ~3–19。 */
private fun spanToZoom(span: Double): Float {
    val zoom = (Math.log(360.0 / span) / Math.log(2.0)).toFloat()
    return zoom.coerceIn(3f, 19f)
}

// ============================== 地图容器 ==============================

@Composable
private fun AMapContent(
    isDark: Boolean,
    onMapReady: (AMap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    LaunchedEffect(Unit) {
        if (!locationGranted) {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val locationSource = remember { CampusLocationSource(context.applicationContext) }
    val mapView = remember {
        MapView(context).apply {
            // 轻量版地图底图由 WebView + WebGL 绘制，必须开启硬件加速。
            // 虽然主题已默认开启，但某些低端机 / 深色节电模式 / 自定义 Window 会降级成软件层，
            // 表现为"polygon / 定位蓝点可见但底图瓦片空白"。显式 setLayerType 作为兜底。
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    }

    // 异步拿到 AMap 实例后再做初始化；因为 getMapAsyn 可能在 onCreate 后才回调，
    // 这里用一个专门的 state 承接，避免在 factory 里反复调用 getMapAsyn
    var amapReady by remember { mutableStateOf<AMap?>(null) }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    mapView.onCreate(null)
                    mapView.getMapAsyn { aMap ->
                        configureMap(aMap, isDark)
                        applyMyLocation(aMap, locationSource, locationGranted)
                        amapReady = aMap
                    }
                }
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> {
                    locationSource.shutdown()
                    mapView.onDestroy()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            locationSource.shutdown()
        }
    }

    // AMap 就绪后向外抛
    LaunchedEffect(amapReady) {
        amapReady?.let(onMapReady)
    }

    // 权限变化后调整定位蓝点
    LaunchedEffect(locationGranted, amapReady) {
        val amap = amapReady ?: return@LaunchedEffect
        applyMyLocation(amap, locationSource, locationGranted)
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize()
    )
}

private fun configureMap(aMap: AMap, isDark: Boolean) {
    aMap.mapType = if (isDark) AMap.MAP_TYPE_NIGHT else AMap.MAP_TYPE_NORMAL
    aMap.uiSettings.apply {
        // 轻量版 UiSettings 仅支持这几个手势开关；zoom 按钮/指南针/比例尺由内置 UI 或默认行为提供
        isScrollGesturesEnabled = true
        isZoomGesturesEnabled = true
        isTiltGesturesEnabled = false
        isRotateGesturesEnabled = false
    }
}

private fun applyMyLocation(aMap: AMap, source: CampusLocationSource, granted: Boolean) {
    aMap.setLocationSource(source)
    aMap.setMyLocationEnabled(granted)
    if (granted) {
        aMap.myLocationStyle = MyLocationStyle().apply {
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
            showMyLocation(true)
        }
    }
}

/**
 * 按 id 增量同步 Polygon：新增的 feature 创建 polygon，已经消失的 feature 对应 polygon remove。
 * 常规情况下网络拉到的数据与缓存一致 → 两个 Set 相等 → 什么都不做。
 *
 * 性能要点：
 *  - 坐标转换 (WGS-84→GCJ-02) 放到 [Dispatchers.Default]，避免 124*~50 个点的循环阻塞主线程；
 *  - `aMap.addPolygon` 是跨 JSBridge 调用，单次 ~2-5ms，**必须回到主线程**，并且**按批 yield**
 *    让出主线程给 WebView JS 侧消化消息，否则低端机会出现 1-2 秒"白屏假死"。
 *
 * @return 是否实际发生了增删（供上层递增 polygonVersion 触发下游刷新）
 */
private suspend fun rebuildPolygonsIfNeeded(
    aMap: AMap,
    polygonMap: MutableMap<String, Polygon>,
    features: List<CampusMapFeature>
): Boolean {
    var changed = false
    val currentIds = features.mapTo(HashSet()) { it.stableId }
    val iter = polygonMap.entries.iterator()
    while (iter.hasNext()) {
        val (id, polygon) = iter.next()
        if (id !in currentIds) {
            polygon.remove()
            iter.remove()
            changed = true
        }
    }

    // 1. 在后台线程批量完成坐标转换，避免主线程卡顿
    val toAdd = features.filter { it.stableId !in polygonMap.keys }
    if (toAdd.isEmpty()) return changed

    data class Prepared(val id: String, val latLngs: List<LatLng>, val color: Int)
    val prepared = withContext(Dispatchers.Default) {
        toAdd.mapNotNull { feat ->
            val ring = feat.geometry.coordinates.firstOrNull().orEmpty()
            if (ring.isEmpty()) return@mapNotNull null
            val latLngs = ring.map { pt ->
                val gcj = CampusCoordinateConverter.wgs84ToGcj02(
                    lat = pt.getOrNull(1) ?: 0.0,
                    lon = pt.getOrNull(0) ?: 0.0
                )
                LatLng(gcj[0], gcj[1])
            }
            Prepared(feat.stableId, latLngs, colorForCategory(feat.properties.category))
        }
    }

    // 2. 回主线程批量 addPolygon；每 20 个 yield 一次让 WebView 消化 JSBridge 消息，
    //    否则 124 个 polygon 连续下发会把主线程锁死约 0.5-1s。
    val chunkSize = 20
    prepared.forEachIndexed { index, p ->
        val polygon = aMap.addPolygon(
            PolygonOptions()
                .addAll(p.latLngs)
                .fillColor(withAlpha(p.color, 0x80))
                .strokeColor(p.color)
                .strokeWidth(3f)
                .zIndex(1f)
        )
        polygonMap[p.id] = polygon
        changed = true
        if ((index + 1) % chunkSize == 0) yield()
    }
    return changed
}

// ============================== 定位源 ==============================

/**
 * 轻量版 SDK 地图本身不内置定位，需实现 [LocationSource] + 外接 [AMapLocationClient]。
 *
 * - 地图调 [activate] 时启动定位；调 [deactivate] 时停止。
 * - Activity 销毁必须调 [shutdown] 确保 Client 释放（地图自身调用 deactivate 也会走到）。
 */
private class CampusLocationSource(private val appContext: android.content.Context) : LocationSource,
    AMapLocationListener {

    private var listener: LocationSource.OnLocationChangedListener? = null
    private var client: AMapLocationClient? = null

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        this.listener = listener
        if (client == null) {
            runCatching {
                client = AMapLocationClient(appContext).apply {
                    setLocationListener(this@CampusLocationSource)
                    setLocationOption(
                        AMapLocationClientOption().apply {
                            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                            isOnceLocation = false
                            interval = 5000
                        }
                    )
                    startLocation()
                }
            }
        }
    }

    override fun deactivate() {
        listener = null
        client?.stopLocation()
        client?.onDestroy()
        client = null
    }

    override fun onLocationChanged(loc: AMapLocation?) {
        if (loc == null || loc.errorCode != 0) return
        listener?.onLocationChanged(loc)
    }

    fun shutdown() {
        runCatching { deactivate() }
    }
}

// ============================== 顶栏 / Sheet ==============================

@Composable
private fun TopBar(
    selectedCampus: Campus?,
    onBack: () -> Unit,
    onCampusSelected: (Campus?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 与「统一作业」页一致的返回图标：ic_arrow_right + 180° 旋转；
        // 叠在地图上阅读性差，因此保留 bgCardColor 圆形背景 + 阴影。
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(colors.bgCardColor, CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "返回",
                tint = colors.primaryTextColor,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(180f)
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(21.dp))
                .background(colors.bgCardColor, RoundedCornerShape(21.dp))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CampusChip(label = "全部", selected = selectedCampus == null) { onCampusSelected(null) }
            CampusChip(label = "金盆岭", selected = selectedCampus == Campus.JINPENLING) {
                onCampusSelected(Campus.JINPENLING)
            }
            CampusChip(label = "云塘", selected = selectedCampus == Campus.YUNTANG) {
                onCampusSelected(Campus.YUNTANG)
            }
        }
    }
}

@Composable
private fun CampusChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val bg = if (selected) colors.commonColor else Color.Transparent
    val fg = if (selected) Color.White else colors.primaryTextColor
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(17.dp))
            .background(bg, RoundedCornerShape(17.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuildingsListSheet(
    uiState: CampusMapViewModel.UiState,
    onSearchTextChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onBuildingSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
    ) {
        TextField(
            value = uiState.searchText,
            onValueChange = onSearchTextChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索建筑、地点...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.searchText.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(Icons.Filled.Cancel, contentDescription = null, tint = colors.secondaryTextColor)
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = colors.bgSecondaryColor,
                unfocusedContainerColor = colors.bgSecondaryColor
            )
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.availableCategories.forEach { cat ->
                CategoryChip(
                    label = cat ?: "全部",
                    selected = uiState.selectedCategory == cat,
                    onClick = { onCategorySelected(cat) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        val listState = rememberLazyListState()
        LaunchedEffect(uiState.selectedBuildingId) {
            val id = uiState.selectedBuildingId ?: return@LaunchedEffect
            val index = uiState.filteredBuildings.indexOfFirst { it.stableId == id }
            if (index >= 0) listState.animateScrollToItem(index)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.filteredBuildings, key = { it.stableId }) { feat ->
                BuildingRow(
                    feat = feat,
                    selected = uiState.selectedBuildingId == feat.stableId,
                    onClick = { onBuildingSelected(feat.stableId) }
                )
            }
            if (uiState.filteredBuildings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.searchText.isNotEmpty()) "未找到匹配的建筑" else "暂无数据",
                            color = colors.secondaryTextColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val bg = if (selected) colors.commonColor else colors.bgSecondaryColor
    val fg = if (selected) Color.White else colors.primaryTextColor
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BuildingRow(
    feat: CampusMapFeature,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    val catColor = Color(colorForCategory(feat.properties.category))
    val borderMod = if (selected) {
        Modifier.border(2.dp, colors.commonColor, RoundedCornerShape(16.dp))
    } else Modifier
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.bgSecondaryColor, RoundedCornerShape(16.dp))
            .then(borderMod)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconForCategory(feat.properties.category),
                contentDescription = null,
                tint = catColor
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = feat.properties.name,
                color = colors.primaryTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${feat.properties.campus}校区 · ${feat.properties.category}",
                color = colors.secondaryTextColor,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

// ============================== 颜色 / 图标映射 ==============================

/** 与 iOS 配色对齐，返回 ARGB int（含完整 alpha=FF）。 */
private fun colorForCategory(category: String): Int = when (category) {
    "教学楼" -> 0xFFF39C12.toInt()
    "图书馆" -> 0xFF3498DB.toInt()
    "体育" -> 0xFF1ABCAE.toInt()
    "食堂" -> 0xFFE74C3C.toInt()
    "宿舍", "东苑宿舍", "南苑宿舍", "西苑宿舍" -> 0xFF27AE60.toInt()
    "行政办公" -> 0xFF8E44AD.toInt()
    "生活休闲" -> 0xFFE91E63.toInt()
    else -> 0xFF7F8C8D.toInt()
}

/** 替换 color 的 alpha 通道。`alpha` 为 0..255。 */
private fun withAlpha(color: Int, alpha: Int): Int =
    (color and 0x00FFFFFF) or ((alpha and 0xFF) shl 24)

private fun iconForCategory(category: String): ImageVector = when (category) {
    "教学楼" -> Icons.Filled.Business
    "图书馆" -> Icons.Filled.Book
    "体育" -> Icons.Filled.SportsBasketball
    "食堂" -> Icons.Filled.LocalDining
    "宿舍", "东苑宿舍", "南苑宿舍", "西苑宿舍" -> Icons.Filled.Hotel
    "行政办公" -> Icons.Filled.Apartment
    "生活休闲" -> Icons.Filled.Coffee
    else -> Icons.Filled.Map
}
