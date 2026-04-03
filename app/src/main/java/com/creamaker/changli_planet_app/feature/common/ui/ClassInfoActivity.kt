package com.creamaker.changli_planet_app.feature.common.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.ComposeActivity
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.feature.common.viewModel.AvailableClassroomUiState
import com.creamaker.changli_planet_app.feature.common.viewModel.AvailableClassroomViewModel
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
        onQueryClick = viewModel::queryAvailableClassrooms,
        onDismissWarning = viewModel::dismissWarning,
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
    onQueryClick: () -> Unit,
    onDismissWarning: () -> Unit,
    onDismissError: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val colors = AppTheme.colors
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(pageBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (state.showWarning) {
                FeedbackBanner(
                    title = "警告",
                    message = state.warningMessage,
                    backgroundColor = Color(0xFFFFF3CD),
                    contentColor = Color(0xFF8A6200),
                    actionLabel = "知道了",
                    onDismiss = onDismissWarning
                )
            }

            if (state.showError && state.errorMessage.isNotBlank()) {
                ErrorDialog(
                    message = state.errorMessage,
                    onDismiss = onDismissError
                )
            }

            AvailableClassroomFilterCard(
                state = state,
                onCampusSelected = onCampusSelected,
                onWeekSelected = onWeekSelected,
                onDaySelected = onDaySelected,
                onSectionSelected = onSectionSelected,
                onQueryClick = {
                    focusManager.clearFocus(force = true)
                    onQueryClick()
                }
            )

            when (state.availableClassrooms) {
                null -> AvailableClassroomInitialState()
                else -> {
                    if (state.availableClassrooms.isNotEmpty()) {
                        SearchInput(
                            value = state.searchText,
                            onValueChange = onSearchTextChange
                        )
                    }
                    AvailableClassroomResultSection(state = state)
                }
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
    val filterCardColor =
        if (colors.bgPrimaryColor == Color(0xFF000000)) colors.bgSecondaryColor else Color.White

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = filterCardColor,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
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
                        text = "查询条件",
                        color = colors.primaryTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "按校区、周数、星期和节次筛选空闲教室",
                        color = colors.greyTextColor,
                        fontSize = 13.sp
                    )
                }
            }

            SelectionLabel("校区")
            CampusSegmentedPicker(
                selectedCampus = state.selectedCampus,
                enabled = !state.isLoading,
                onCampusSelected = onCampusSelected
            )

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
                    text = if (state.isLoading) "查询中..." else "查询空教室",
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
                    focusedContainerColor = colors.bgPrimaryColor,
                    unfocusedContainerColor = colors.bgPrimaryColor,
                    disabledContainerColor = colors.bgPrimaryColor,
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
                    inactiveContainerColor = colors.bgPrimaryColor,
                    inactiveContentColor = colors.primaryTextColor,
                    activeBorderColor = colors.commonColor,
                    inactiveBorderColor = colors.outlineColor,
                    disabledActiveContainerColor = colors.commonColor.copy(alpha = 0.5f),
                    disabledInactiveContainerColor = colors.bgPrimaryColor,
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
    val searchBorderColor = if (isDark) colors.outlineColor else colors.commonColor.copy(alpha = 0.7f)
    val searchHintColor = if (isDark) colors.searchHintColor else colors.greyTextColor.copy(alpha = 0.82f)
    val searchContainerColor = if (isDark) colors.bgSecondaryColor else Color.White

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
fun AvailableClassroomResultSection(
    state: AvailableClassroomUiState
) {
    val colors = AppTheme.colors
    val classrooms = state.filteredAvailableClassrooms.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "查询结果",
                color = colors.primaryTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "共 ${classrooms.count()} 间",
                color = colors.greyTextColor,
                fontSize = 14.sp
            )
        }

        when {
            classrooms.isEmpty() && state.searchText.isNotBlank() -> {
                AvailableClassroomEmptyState(
                    title = "没有找到匹配的教室",
                    subtitle = "试试其他关键词或清空搜索"
                )
            }

            classrooms.isEmpty() -> {
                AvailableClassroomEmptyState(
                    title = "该时间段无空闲教室",
                    subtitle = "可以换个校区、周数或节次再试试"
                )
            }

            else -> {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.bgSecondaryColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        userScrollEnabled = false,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(14.dp),
                        modifier = Modifier.height((((classrooms.size + 1) / 2) * 68).dp)
                    ) {
                        items(classrooms) { classroom ->
                            ClassroomChip(classroom = classroom)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassroomChip(classroom: String) {
    val colors = AppTheme.colors

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.bgPrimaryColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgPrimaryColor)
                .padding(horizontal = 12.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = classroom,
                color = colors.primaryTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AvailableClassroomInitialState() {
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
                text = "支持按校区、周数、星期和节次查询空闲教室",
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
fun FeedbackBanner(
    title: String,
    message: String,
    backgroundColor: Color,
    contentColor: Color,
    actionLabel: String,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismiss, contentPadding = PaddingValues(0.dp)) {
                    Text(text = actionLabel, color = contentColor, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(
                text = message,
                color = contentColor,
                fontSize = 13.sp,
                lineHeight = 18.sp
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
                availableClassrooms = listOf("云综教A101", "云综教A102", "云综教B201", "理科楼203")
            ),
            onBackClick = {},
            onCampusSelected = {},
            onWeekSelected = {},
            onDaySelected = {},
            onSectionSelected = {},
            onSearchTextChange = {},
            onQueryClick = {},
            onDismissWarning = {},
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
                availableClassrooms = emptyList(),
                showWarning = true,
                warningMessage = "请先登录教务系统后再查询数据"
            ),
            onBackClick = {},
            onCampusSelected = {},
            onWeekSelected = {},
            onDaySelected = {},
            onSectionSelected = {},
            onSearchTextChange = {},
            onQueryClick = {},
            onDismissWarning = {},
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
fun AvailableClassroomResultSectionPreview() {
    AppSkinTheme {
        AvailableClassroomResultSection(
            state = AvailableClassroomUiState(
                availableClassrooms = listOf("金综教101", "金综教102", "金综教201", "金综教202")
            )
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

@Preview(showBackground = true)
@Composable
fun FeedbackBannerPreview() {
    AppSkinTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeedbackBanner(
                title = "警告",
                message = "请先登录教务系统后再查询数据",
                backgroundColor = Color(0xFFFFF3CD),
                contentColor = Color(0xFF8A6200),
                actionLabel = "知道了",
                onDismiss = {}
            )
            FeedbackBanner(
                title = "错误",
                message = "网络出现波动，请稍后重试",
                backgroundColor = Color(0xFFFFE3E3),
                contentColor = Color(0xFFB3261E),
                actionLabel = "关闭",
                onDismiss = {}
            )
        }
    }
}
