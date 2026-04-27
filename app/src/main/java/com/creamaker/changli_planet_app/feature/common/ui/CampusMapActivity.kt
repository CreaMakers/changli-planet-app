package com.creamaker.changli_planet_app.feature.common.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.compose.foundation.layout.BoxScope
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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/** 校园地图页面（高德轻量版 SDK，基于 WebView+WebGL 渲染）。 */
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
    val filteredBuildings by viewModel.filteredBuildings.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val configuration = LocalConfiguration.current
    val sheetMaxHeight = configuration.screenHeightDp.dp * 0.55f

    var mapRef by remember { mutableStateOf<AMap?>(null) }
    val polygonMap = remember { mutableMapOf<String, Polygon>() }
    val polygonStyles = remember { mutableMapOf<String, PolygonStyle>() }
    val rebuildMutex = remember { Mutex() }

    // polygonMap 非 SnapshotState，用版本号驱动下游 LaunchedEffect 重跑
    var polygonVersion by remember { mutableIntStateOf(0) }

    // MapView 被销毁后 polygon 引用失效；mapRef 置 null 时一并清空本地缓存
    LaunchedEffect(mapRef) {
        if (mapRef == null) {
            polygonMap.clear()
            polygonStyles.clear()
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(mapRef, isDark) {
        mapRef?.mapType = if (isDark) AMap.MAP_TYPE_NIGHT else AMap.MAP_TYPE_NORMAL
    }

    // 一次性事件：消费后清空
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

    LaunchedEffect(uiState.allBuildings, mapRef) {
        val map = mapRef ?: return@LaunchedEffect
        rebuildMutex.withLock {
            if (rebuildPolygonsIfNeeded(map, polygonMap, uiState.allBuildings)) polygonVersion++
        }
    }

    LaunchedEffect(filteredBuildings, uiState.selectedBuildingId, polygonVersion) {
        if (polygonMap.isEmpty()) return@LaunchedEffect
        val visibleIds = filteredBuildings.mapTo(HashSet()) { it.stableId }
        val featureById = uiState.allBuildings.associateBy { it.stableId }
        rebuildMutex.withLock {
            polygonMap.forEach { (id, polygon) ->
                val feat = featureById[id] ?: return@forEach
                val visible = id in visibleIds
                val selected = visible && id == uiState.selectedBuildingId
                val baseColor = colorForCategory(feat.properties.category)
                val next = PolygonStyle(
                    visible = visible,
                    fillColor = withAlpha(baseColor, if (selected) 0xCC else 0x80),
                    strokeColor = if (selected) 0xFF222222.toInt() else baseColor,
                    strokeWidth = if (selected) 6f else 3f,
                    zIndex = if (selected) 2f else 1f,
                )
                val prev = polygonStyles[id]
                if (prev == next) return@forEach
                if (prev?.visible != next.visible) polygon.isVisible = next.visible
                if (next.visible) {
                    if (prev?.fillColor != next.fillColor) polygon.fillColor = next.fillColor
                    if (prev?.strokeColor != next.strokeColor) polygon.strokeColor = next.strokeColor
                    if (prev?.strokeWidth != next.strokeWidth) polygon.strokeWidth = next.strokeWidth
                    if (prev?.zIndex != next.zIndex) polygon.zIndex = next.zIndex
                }
                polygonStyles[id] = next
            }
        }
    }

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
            // heightIn 兜底 sheet 最大展开高度
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = sheetMaxHeight)
            ) {
                BuildingsListSheet(
                    uiState = uiState,
                    filteredBuildings = filteredBuildings,
                    availableCategories = availableCategories,
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

            // 5s 内底图仍未出现（WebView 可能被 Key/网络/NSC 拦）给出可感知提示
            MapLoadingHint(mapReady = mapRef != null)

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

    LaunchedEffect(Unit) {
        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
    }
}

/** 高德 zoom 级别 ≈ log2(360/span)，有效范围 3–19。 */
private fun spanToZoom(span: Double): Float =
    (kotlin.math.log2(360.0 / span)).toFloat().coerceIn(3f, 19f)

// ============================== 地图容器 ==============================

@Composable
private fun AMapContent(
    isDark: Boolean,
    onMapReady: (AMap?) -> Unit
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
    val mapView = remember { MapView(context) }

    // getMapAsyn 异步回调，用 state 承接，避免 factory 里反复调用
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
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            locationSource.shutdown()
            // MapView 生命周期必须在此处终结：否则 Compose 被移除但 Activity 还活时会泄漏
            amapReady = null
            runCatching { mapView.onDestroy() }
        }
    }

    LaunchedEffect(amapReady) {
        onMapReady(amapReady)
    }

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
 * 按 id 增量同步 Polygon：新增 feature 创建 polygon，已消失的 feature 移除。
 * - 坐标转换在 Default 线程批量完成；
 * - `addPolygon` 必须主线程，每 [POLYGON_BATCH_SIZE] 个 `yield()` 让 WebView 消化 JSBridge，避免卡顿；
 * - 全程维护 polygonMap 的幂等性：即便协程被取消，下一次重启也能从残留状态继续补齐。
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

    // 主线程 addPolygon 包 NonCancellable，协程取消时保证 polygonMap 写入幂等完整
    withContext(NonCancellable) {
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
            if ((index + 1) % POLYGON_BATCH_SIZE == 0) yield()
        }
    }
    return changed
}

private const val POLYGON_BATCH_SIZE = 8

/** Polygon 样式缓存；用于 diff 避免重复 setter 的 JSBridge 调用。 */
private data class PolygonStyle(
    val visible: Boolean,
    val fillColor: Int,
    val strokeColor: Int,
    val strokeWidth: Float,
    val zIndex: Float,
)

// ============================== 定位源 ==============================

/** 轻量版 SDK 地图不内置定位，需自建 [LocationSource] + [AMapLocationClient]。 */
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

/**
 * 地图首屏降级提示：超过 5s 底图仍未出现时显示，提示用户可能是 Key/NSC/网络问题。
 */
@Composable
private fun BoxScope.MapLoadingHint(mapReady: Boolean) {
    var showHint by remember { mutableStateOf(false) }
    LaunchedEffect(mapReady) {
        showHint = false
        if (!mapReady) {
            delay(5000)
            showHint = !mapReady
        }
    }
    if (showHint) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(12.dp))
                .background(AppTheme.colors.bgCardColor.copy(alpha = 0.92f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "地图加载较慢，请检查网络或稍后重试",
                color = AppTheme.colors.primaryTextColor,
                fontSize = 13.sp
            )
        }
    }
}

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
    filteredBuildings: List<CampusMapFeature>,
    availableCategories: List<String?>,
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
            availableCategories.forEach { cat ->
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
            val index = filteredBuildings.indexOfFirst { it.stableId == id }
            if (index >= 0) listState.animateScrollToItem(index)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredBuildings, key = { it.stableId }) { feat ->
                BuildingRow(
                    feat = feat,
                    selected = uiState.selectedBuildingId == feat.stableId,
                    onClick = { onBuildingSelected(feat.stableId) }
                )
            }
            if (filteredBuildings.isEmpty()) {
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
