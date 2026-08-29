package com.zhelearn.CSUSTPlanet.feature.common.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.BuildConfig
import com.zhelearn.CSUSTPlanet.base.ComposeActivity
import com.zhelearn.CSUSTPlanet.core.theme.AppTheme
import com.zhelearn.CSUSTPlanet.feature.common.data.remote.dto.CampusMapFeature
import com.zhelearn.CSUSTPlanet.feature.common.map.Campus
import com.zhelearn.CSUSTPlanet.feature.common.map.CAMPUS_MAP_DEFAULT_ZOOM
import com.zhelearn.CSUSTPlanet.feature.common.map.IllustratedCampusMap
import com.zhelearn.CSUSTPlanet.feature.common.viewModel.CampusMapViewModel
import kotlinx.coroutines.launch

/** 校园地图页面（本地 GeoJSON + Compose Canvas 渲染）。 */
class CampusMapActivity : ComposeActivity() {

    private val viewModel: CampusMapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadInitialIfNeeded()
        val debugInitialZoom = if (BuildConfig.DEBUG) {
            intent.getFloatExtra(DEBUG_MAP_ZOOM_EXTRA, Float.NaN).takeIf(Float::isFinite)
        } else {
            null
        }
        setComposeContent {
            CampusMapScreen(
                viewModel = viewModel,
                onBack = ::finish,
                debugInitialZoom = debugInitialZoom,
            )
        }
    }
}

// ============================== 顶层 UI ==============================


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampusMapScreen(
    viewModel: CampusMapViewModel,
    onBack: () -> Unit,
    debugInitialZoom: Float?,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredBuildings by viewModel.filteredBuildings.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val colors = AppTheme.colors
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val sheetMaxHeight = configuration.screenHeightDp.dp * 0.55f
    val interactiveBuildingIds = remember(filteredBuildings) {
        buildSet {
            for (building in filteredBuildings) add(building.stableId)
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    )
    val scope = rememberCoroutineScope()

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
            IllustratedCampusMap(
                allBuildings = uiState.allBuildings,
                allRoads = uiState.allRoads,
                interactiveBuildingIds = interactiveBuildingIds,
                selectedCampus = uiState.selectedCampus,
                selectedCategory = uiState.selectedCategory,
                searchText = uiState.searchText,
                selectedBuildingId = uiState.selectedBuildingId,
                cameraTarget = if (debugInitialZoom == null) uiState.cameraTarget else null,
                initialZoom = debugInitialZoom ?: CAMPUS_MAP_DEFAULT_ZOOM,
                onCameraTargetConsumed = viewModel::consumeCameraTarget,
                onBuildingSelected = viewModel::onBuildingSelected,
                modifier = Modifier.fillMaxSize(),
            )

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

private const val DEBUG_MAP_ZOOM_EXTRA = "debug_map_zoom"

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
    val searchSecondaryColor = if (isSystemInDarkTheme()) {
        Color(0xFFABB5BE)
    } else {
        colors.secondaryTextColor
    }
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
                        Icon(Icons.Filled.Cancel, contentDescription = null, tint = searchSecondaryColor)
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = TextFieldDefaults.colors(
                focusedTextColor = colors.primaryTextColor,
                unfocusedTextColor = colors.primaryTextColor,
                cursorColor = colors.primaryTextColor,
                focusedLeadingIconColor = searchSecondaryColor,
                unfocusedLeadingIconColor = searchSecondaryColor,
                focusedPlaceholderColor = searchSecondaryColor,
                unfocusedPlaceholderColor = searchSecondaryColor,
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
    "教学楼" -> 0xFFAF7E58.toInt()
    "图书馆" -> 0xFF5F86A8.toInt()
    "体育" -> 0xFF56889A.toInt()
    "食堂" -> 0xFFA7804D.toInt()
    "东苑宿舍" -> 0xFF719872.toInt()
    "西苑宿舍" -> 0xFFAB625E.toInt()
    "南苑宿舍" -> 0xFF876F94.toInt()
    "宿舍" -> 0xFF996F7E.toInt()
    "行政办公" -> 0xFF6D7F95.toInt()
    "生活休闲" -> 0xFF668E86.toInt()
    "水域" -> 0xFF4A9FA1.toInt()
    "绿地" -> 0xFF78A45E.toInt()
    else -> 0xFF7F8C8D.toInt()
}

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
