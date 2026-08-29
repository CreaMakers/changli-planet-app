package com.zhelearn.CSUSTPlanet.feature.common.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.base.ComposeActivity
import com.zhelearn.CSUSTPlanet.core.theme.AppSkinTheme
import com.zhelearn.CSUSTPlanet.core.theme.AppTheme
import com.zhelearn.CSUSTPlanet.feature.common.viewModel.AvailableClassroomQueryMode
import com.zhelearn.CSUSTPlanet.feature.common.viewModel.AvailableClassroomUiState
import com.zhelearn.CSUSTPlanet.feature.common.viewModel.AvailableClassroomViewModel
import com.zhelearn.CSUSTPlanet.feature.common.viewModel.ClassroomAvailabilityItem
import com.dcelysia.csust_spider.education.data.remote.model.Campus
import com.dcelysia.csust_spider.education.data.remote.model.DayOfWeek

/**
 * 空教室查询页面。
 */
class ClassInfoActivity : ComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setComposeContent {
            Surface(color = AppTheme.colors.bgPrimaryColor) {
                ClassInfoRoute(onBackClick = { finish() })
            }
        }
    }
}

@Composable
private fun ClassInfoRoute(
    onBackClick: () -> Unit,
    viewModel: AvailableClassroomViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onBackClick)

    ClassInfoScreen(
        state = state,
        onBackClick = onBackClick,
        onCampusSelected = viewModel::updateCampus,
        onWeekSelected = viewModel::updateWeek,
        onDaySelected = viewModel::updateDayOfWeek,
        onSectionSelected = viewModel::updateSection,
        onSearchTextChange = viewModel::updateSearchText,
        onQueryModeSelected = viewModel::updateQueryMode,
        onCurrentQueryClick = viewModel::queryCurrentAvailableClassrooms,
        onCustomQueryClick = viewModel::queryAvailableClassrooms,
        onDismissError = viewModel::dismissError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassInfoScreen(
    state: AvailableClassroomUiState,
    onBackClick: () -> Unit,
    onCampusSelected: (Campus) -> Unit,
    onWeekSelected: (Int) -> Unit,
    onDaySelected: (DayOfWeek) -> Unit,
    onSectionSelected: (Int) -> Unit,
    onSearchTextChange: (String) -> Unit,
    onQueryModeSelected: (AvailableClassroomQueryMode) -> Unit,
    onCurrentQueryClick: () -> Unit,
    onCustomQueryClick: () -> Unit,
    onDismissError: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val colors = AppTheme.colors
    val displayedClassrooms = state.displayedClassrooms
    val filteredClassrooms = remember(displayedClassrooms, state.searchText) {
        state.filteredClassrooms.orEmpty()
    }
    val pageBackground =
        if (colors.bgPrimaryColor == Color(0xFF000000)) colors.bgPrimaryColor else colors.overviewPageBackgroundColor

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "空教室查询",
                        color = colors.titleTopColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_back_blue),
                            contentDescription = "返回",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.bgTopBarColor,
                    titleContentColor = colors.titleTopColor,
                    navigationIconContentColor = colors.titleTopColor
                )
            )
        },
        containerColor = pageBackground
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.bgTopBarColor.copy(alpha = 0.38f),
                            pageBackground,
                            pageBackground
                        )
                    )
                ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                AvailableClassroomQueryModePicker(
                    selectedMode = state.queryMode,
                    enabled = true,
                    onModeSelected = onQueryModeSelected
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                AvailableClassroomFilterCard(
                    state = state,
                    onCampusSelected = onCampusSelected,
                    onWeekSelected = onWeekSelected,
                    onDaySelected = onDaySelected,
                    onSectionSelected = onSectionSelected,
                    onQueryClick = {
                        focusManager.clearFocus(force = true)
                        when (state.queryMode) {
                            AvailableClassroomQueryMode.CURRENT -> onCurrentQueryClick()
                            AvailableClassroomQueryMode.CUSTOM -> onCustomQueryClick()
                        }
                    }
                )
            }

            if (state.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.commonColor,
                        trackColor = colors.commonColor.copy(alpha = 0.12f)
                    )
                }
            }

            when (displayedClassrooms) {
                null -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        AvailableClassroomInitialState(queryMode = state.queryMode)
                    }
                }

                else -> {
                    if (displayedClassrooms.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SearchInput(
                                value = state.searchText,
                                onValueChange = onSearchTextChange
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        AvailableClassroomResultHeader(
                            queryMode = state.queryMode,
                            classroomCount = filteredClassrooms.size,
                            updatedAtLabel = state.currentUpdatedAtLabel
                        )
                    }

                    if (filteredClassrooms.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            AvailableClassroomEmptyState(
                                title = availableClassroomEmptyTitle(state),
                                subtitle = availableClassroomEmptySubtitle(state)
                            )
                        }
                    } else if (state.queryMode == AvailableClassroomQueryMode.CURRENT) {
                        items(
                            items = filteredClassrooms,
                            key = { it.rawName },
                            span = { GridItemSpan(maxLineSpan) }
                        ) { classroom ->
                            CurrentClassroomCard(classroom = classroom)
                        }
                    } else {
                        items(
                            items = filteredClassrooms,
                            key = { it.rawName }
                        ) { classroom ->
                            ClassroomChip(classroom = classroom)
                        }
                    }
                }
            }
        }
    }

    if (state.showError && state.errorMessage.isNotBlank()) {
        ErrorDialog(
            message = state.errorMessage,
            onDismiss = onDismissError
        )
    }
}

@Composable
private fun AvailableClassroomQueryModePicker(
    selectedMode: AvailableClassroomQueryMode,
    enabled: Boolean,
    onModeSelected: (AvailableClassroomQueryMode) -> Unit
) {
    val colors = AppTheme.colors
    val queryModes = listOf(
        AvailableClassroomQueryMode.CURRENT to "现在可用",
        AvailableClassroomQueryMode.CUSTOM to "按节次查询"
    )

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        queryModes.forEachIndexed { index, (queryMode, label) ->
            SegmentedButton(
                selected = selectedMode == queryMode,
                onClick = { onModeSelected(queryMode) },
                enabled = enabled,
                shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = queryModes.size
                ),
                colors = androidx.compose.material3.SegmentedButtonDefaults.colors(
                    activeContainerColor = colors.commonColor,
                    activeContentColor = Color.White,
                    inactiveContainerColor = colors.bgSecondaryColor,
                    inactiveContentColor = colors.primaryTextColor,
                    activeBorderColor = colors.commonColor,
                    inactiveBorderColor = colors.outlineColor,
                    disabledActiveContainerColor = colors.commonColor.copy(alpha = 0.5f),
                    disabledInactiveContainerColor = colors.bgSecondaryColor,
                    disabledActiveContentColor = Color.White.copy(alpha = 0.8f),
                    disabledInactiveContentColor = colors.greyTextColor
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AvailableClassroomFilterCard(
    state: AvailableClassroomUiState,
    onCampusSelected: (Campus) -> Unit,
    onWeekSelected: (Int) -> Unit,
    onDaySelected: (DayOfWeek) -> Unit,
    onSectionSelected: (Int) -> Unit,
    onQueryClick: () -> Unit
) {
    val colors = AppTheme.colors
    val filterCardColor = colors.commonColor.copy(
        alpha = if (colors.bgPrimaryColor == Color(0xFF000000)) 0.09f else 0.055f
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = filterCardColor,
        border = BorderStroke(1.dp, colors.outlineLowContrastColor.copy(alpha = 0.65f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.commonColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_classroom),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = if (state.queryMode == AvailableClassroomQueryMode.CURRENT) {
                            "现在可用"
                        } else {
                            "查询条件"
                        },
                        color = colors.primaryTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (state.queryMode == AvailableClassroomQueryMode.CURRENT) {
                            state.currentContextLabel
                        } else {
                            "按校区、周数、星期和节次筛选空闲教室"
                        },
                        color = colors.greyTextColor,
                        fontSize = 13.sp
                    )
                }
            }

            if (state.queryMode == AvailableClassroomQueryMode.CURRENT) {
                Text(
                    text = state.currentTimeLabel,
                    color = colors.primaryTextColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                Text(
                    text = "自动匹配当前教学时段，结果按连续空闲时长排序",
                    color = colors.greyTextColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            SelectionLabel("校区")
            CampusSegmentedPicker(
                selectedCampus = state.selectedCampus,
                enabled = !state.isLoading,
                onCampusSelected = onCampusSelected
            )

            if (state.queryMode == AvailableClassroomQueryMode.CUSTOM) {
                SelectionDropdownField(
                    label = "周数",
                    selectedText = "第 ${state.selectedWeek} 周",
                    options = (1..20).toList(),
                    optionLabel = { "第 $it 周" },
                    enabled = !state.isLoading,
                    onSelected = onWeekSelected
                )

                SelectionDropdownField(
                    label = "星期",
                    selectedText = state.selectedDayOfWeek.toDisplayName(),
                    options = DayOfWeek.entries.toList(),
                    optionLabel = { it.toDisplayName() },
                    enabled = !state.isLoading,
                    onSelected = onDaySelected
                )

                SelectionDropdownField(
                    label = "节次",
                    selectedText = state.selectedSection.toSectionLabel(),
                    options = (1..5).toList(),
                    optionLabel = { it.toSectionLabel() },
                    enabled = !state.isLoading,
                    onSelected = onSectionSelected
                )
            }

            Button(
                onClick = onQueryClick,
                enabled = !state.isLoading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.commonColor,
                    contentColor = Color.White,
                    disabledContainerColor = colors.commonColor.copy(alpha = 0.55f),
                    disabledContentColor = Color.White.copy(alpha = 0.85f)
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = when {
                        state.isLoading -> "查询中..."
                        state.queryMode == AvailableClassroomQueryMode.CURRENT &&
                            state.currentClassrooms != null -> "刷新现在可用教室"
                        state.queryMode == AvailableClassroomQueryMode.CURRENT -> "查看现在可用教室"
                        else -> "查询空教室"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun SelectionLabel(text: String) {
    Text(
        text = text,
        color = AppTheme.colors.greyTextColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SelectionDropdownField(
    label: String,
    selectedText: String,
    options: List<T>,
    optionLabel: (T) -> String,
    enabled: Boolean,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = AppTheme.colors
    val fieldContainerColor = colors.commonColor.copy(
        alpha = if (colors.bgPrimaryColor == Color(0xFF000000)) 0.08f else 0.045f
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectionLabel(label)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.primaryTextColor),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colors.commonColor,
                    unfocusedBorderColor = colors.outlineColor,
                    focusedTextColor = colors.primaryTextColor,
                    unfocusedTextColor = colors.primaryTextColor,
                    disabledTextColor = colors.greyTextColor,
                    cursorColor = colors.commonColor,
                    focusedContainerColor = fieldContainerColor,
                    unfocusedContainerColor = fieldContainerColor,
                    disabledContainerColor = fieldContainerColor,
                    disabledBorderColor = colors.outlineColor.copy(alpha = 0.6f)
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = enabled
                    )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = colors.bgSecondaryColor
            ) {
                options.forEach { option ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = {
                            Text(
                                text = optionLabel(option),
                                color = colors.primaryTextColor
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelected(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CampusSegmentedPicker(
    selectedCampus: Campus,
    enabled: Boolean,
    onCampusSelected: (Campus) -> Unit
) {
    val colors = AppTheme.colors
    val campuses = listOf(Campus.JINPENLING, Campus.YUNTANG)
    val inactiveCampusColor = colors.commonColor.copy(
        alpha = if (colors.bgPrimaryColor == Color(0xFF000000)) 0.08f else 0.045f
    )

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        campuses.forEachIndexed { index, campus ->
            SegmentedButton(
                selected = campus == selectedCampus,
                onClick = { onCampusSelected(campus) },
                enabled = enabled,
                shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = campuses.size
                ),
                colors = androidx.compose.material3.SegmentedButtonDefaults.colors(
                    activeContainerColor = colors.commonColor,
                    activeContentColor = Color.White,
                    inactiveContainerColor = inactiveCampusColor,
                    inactiveContentColor = colors.primaryTextColor,
                    activeBorderColor = colors.commonColor,
                    inactiveBorderColor = colors.outlineColor,
                    disabledActiveContainerColor = colors.commonColor.copy(alpha = 0.5f),
                    disabledInactiveContainerColor = inactiveCampusColor,
                    disabledActiveContentColor = Color.White.copy(alpha = 0.8f),
                    disabledInactiveContentColor = colors.greyTextColor
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = campus.toDisplayName(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    val colors = AppTheme.colors
    val isDark = colors.bgPrimaryColor == Color(0xFF000000)
    val searchBorderColor = if (isDark) {
        colors.outlineLowContrastColor.copy(alpha = 0.55f)
    } else {
        colors.outlineLowContrastColor.copy(alpha = 0.85f)
    }
    val searchHintColor = if (isDark) colors.searchHintColor else colors.greyTextColor.copy(alpha = 0.82f)
    val searchContainerColor = colors.commonColor.copy(alpha = if (isDark) 0.08f else 0.045f)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        placeholder = {
            Text(
                text = "搜索查询结果",
                color = searchHintColor
            )
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.primaryTextColor),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = searchBorderColor,
            unfocusedBorderColor = searchBorderColor,
            focusedTextColor = colors.primaryTextColor,
            unfocusedTextColor = colors.primaryTextColor,
            cursorColor = colors.commonColor,
            focusedContainerColor = searchContainerColor,
            unfocusedContainerColor = searchContainerColor
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AvailableClassroomResultHeader(
    queryMode: AvailableClassroomQueryMode,
    classroomCount: Int,
    updatedAtLabel: String
) {
    val colors = AppTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (queryMode == AvailableClassroomQueryMode.CURRENT) {
                    "现在可用"
                } else {
                    "查询结果"
                },
                color = colors.primaryTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "共 $classroomCount 间",
                color = colors.greyTextColor,
                fontSize = 14.sp
            )
        }

        if (
            queryMode == AvailableClassroomQueryMode.CURRENT &&
            updatedAtLabel.isNotBlank()
        ) {
            Text(
                text = updatedAtLabel,
                color = colors.greyTextColor,
                fontSize = 12.sp
            )
        }
    }
}

private fun availableClassroomEmptyTitle(state: AvailableClassroomUiState): String {
    if (state.searchText.isNotBlank()) return "没有找到匹配的教室"
    return if (state.queryMode == AvailableClassroomQueryMode.CURRENT) {
        state.currentEmptyMessage.ifBlank { "现在没有找到空闲教室" }
    } else {
        "该时间段无空闲教室"
    }
}

private fun availableClassroomEmptySubtitle(state: AvailableClassroomUiState): String {
    if (state.searchText.isNotBlank()) return "试试其他关键词或清空搜索"
    return if (state.queryMode == AvailableClassroomQueryMode.CURRENT) {
        "可以刷新或切换到按节次查询"
    } else {
        "可以换个校区、周数或节次再试试"
    }
}

@Composable
private fun CurrentClassroomCard(classroom: ClassroomAvailabilityItem) {
    val colors = AppTheme.colors

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.commonColor.copy(alpha = 0.055f),
        border = BorderStroke(1.dp, colors.outlineLowContrastColor.copy(alpha = 0.58f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(colors.commonColor, RoundedCornerShape(999.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = classroom.rawName,
                    color = colors.primaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                val availableUntilLabel = classroom.availableUntilLabel
                if (availableUntilLabel != null) {
                    Text(
                        text = "空到 $availableUntilLabel",
                        color = colors.secondaryTextColor,
                        fontSize = 13.sp
                    )
                }
            }

            val remainingDurationLabel = classroom.remainingDurationLabel
            if (remainingDurationLabel != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.commonColor.copy(alpha = 0.13f)
                ) {
                    Text(
                        text = remainingDurationLabel,
                        color = colors.commonColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassroomChip(classroom: ClassroomAvailabilityItem) {
    val colors = AppTheme.colors

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.commonColor.copy(alpha = 0.07f),
        border = BorderStroke(1.dp, colors.outlineLowContrastColor.copy(alpha = 0.45f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = classroom.rawName,
                color = colors.primaryTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AvailableClassroomInitialState(
    queryMode: AvailableClassroomQueryMode = AvailableClassroomQueryMode.CURRENT
) {
    val colors = AppTheme.colors

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colors.commonColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_classroom),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = "点击上方按钮开始查询",
                color = colors.primaryTextColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = if (queryMode == AvailableClassroomQueryMode.CURRENT) {
                    "自动匹配当前教学时段，查看教室可用时长"
                } else {
                    "支持按校区、周数、星期和节次查询空闲教室"
                },
                color = colors.greyTextColor,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AvailableClassroomEmptyState(
    title: String,
    subtitle: String
) {
    val colors = AppTheme.colors

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.bgSecondaryColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(colors.bgButtonLowlightColor, CircleShape)
            )
            Text(
                text = title,
                color = colors.primaryTextColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = colors.greyTextColor,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.bgSecondaryColor,
        title = {
            Text(
                text = "错误",
                color = colors.primaryTextColor,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = message,
                color = colors.greyTextColor,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "确定",
                    color = colors.commonColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

private fun Campus.toDisplayName(): String = when (this) {
    Campus.JINPENLING -> "金盆岭校区"
    Campus.YUNTANG -> "云塘校区"
}

private fun DayOfWeek.toDisplayName(): String = when (this) {
    DayOfWeek.MONDAY -> "星期一"
    DayOfWeek.TUESDAY -> "星期二"
    DayOfWeek.WEDNESDAY -> "星期三"
    DayOfWeek.THURSDAY -> "星期四"
    DayOfWeek.FRIDAY -> "星期五"
    DayOfWeek.SATURDAY -> "星期六"
    DayOfWeek.SUNDAY -> "星期天"
}

private fun Int.toSectionLabel(): String = "第${this}大节 (${this * 2 - 1}-${this * 2})"

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ClassInfoScreenPreview() {
    AppSkinTheme {
        ClassInfoScreen(
            state = AvailableClassroomUiState(
                currentContextLabel = "第 3 周 · 星期二",
                currentTimeLabel = "现在 10:30，查看能用多久",
                currentUpdatedAtLabel = "更新于 10:30",
                currentClassrooms = listOf(
                    previewClassroom("云综教A101", "11:50", "约 1小时20分", 80),
                    previewClassroom("云综教A102", "11:50", "约 1小时20分", 80),
                    previewClassroom("金12", "11:50", "约 1小时20分", 80)
                )
            ),
            onBackClick = {},
            onCampusSelected = {},
            onWeekSelected = {},
            onDaySelected = {},
            onSectionSelected = {},
            onSearchTextChange = {},
            onQueryModeSelected = {},
            onCurrentQueryClick = {},
            onCustomQueryClick = {},
            onDismissError = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ClassInfoScreenDarkPreview() {
    AppSkinTheme {
        ClassInfoScreen(
            state = AvailableClassroomUiState(
                currentContextLabel = "第 3 周 · 星期二",
                currentTimeLabel = "现在 10:30，查看能用多久",
                currentClassrooms = emptyList()
            ),
            onBackClick = {},
            onCampusSelected = {},
            onWeekSelected = {},
            onDaySelected = {},
            onSectionSelected = {},
            onSearchTextChange = {},
            onQueryModeSelected = {},
            onCurrentQueryClick = {},
            onCustomQueryClick = {},
            onDismissError = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AvailableClassroomFilterCardPreview() {
    AppSkinTheme {
        AvailableClassroomFilterCard(
            state = AvailableClassroomUiState(),
            onCampusSelected = {},
            onWeekSelected = {},
            onDaySelected = {},
            onSectionSelected = {},
            onQueryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AvailableClassroomInitialStatePreview() {
    AppSkinTheme {
        AvailableClassroomInitialState()
    }
}

@Preview(showBackground = true)
@Composable
fun AvailableClassroomEmptyStatePreview() {
    AppSkinTheme {
        AvailableClassroomEmptyState(
            title = "该时间段无空闲教室",
            subtitle = "可以换个校区、周数或节次再试试"
        )
    }
}

private fun previewClassroom(
    rawName: String,
    availableUntilLabel: String? = null,
    remainingDurationLabel: String? = null,
    remainingMinutes: Int = 0
): ClassroomAvailabilityItem {
    return ClassroomAvailabilityItem(
        rawName = rawName,
        availableUntilLabel = availableUntilLabel,
        remainingDurationLabel = remainingDurationLabel,
        remainingMinutes = remainingMinutes
    )
}
