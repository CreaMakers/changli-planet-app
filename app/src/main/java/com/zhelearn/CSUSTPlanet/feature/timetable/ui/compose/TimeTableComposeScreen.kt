package com.zhelearn.CSUSTPlanet.feature.timetable.ui.compose

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.core.theme.AppSkinTheme
import com.zhelearn.CSUSTPlanet.core.theme.AppTheme
import com.zhelearn.CSUSTPlanet.feature.timetable.data.local.TimeTableDisplaySettings
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

internal val kCourseCardColors = listOf(
    Pair(Color(0xFFCDE8FF), Color(0xFF8EC9F5)),
    Pair(Color(0xFFB8F0CE), Color(0xFF6BDAA0)),
    Pair(Color(0xFFD8D0FF), Color(0xFFA896F5)),
    Pair(Color(0xFFFFE7B0), Color(0xFFFFCA60)),
    Pair(Color(0xFFFFCDD8), Color(0xFFFF9AB8)),
    Pair(Color(0xFFA8EFEC), Color(0xFF5DD5D0)),
    Pair(Color(0xFFFFD8B8), Color(0xFFFFAA72)),
    Pair(Color(0xFFCEF0A8), Color(0xFF8ED460)),
)
internal val kCourseCardTextColor = Color(0xFF1C2B3A)

internal fun courseCardColorPair(courseTitle: String): Pair<Color, Color> =
    kCourseCardColors[courseTitle.hashCode().absoluteValue % kCourseCardColors.size]

private fun generatedCourseColorPair(index: Int): Pair<Color, Color> {
    val hue = ((index * 37) % 360).toFloat()
    val top = Color.hsl(hue, saturation = 0.52f, lightness = 0.84f)
    val bottom = Color.hsl(hue, saturation = 0.62f, lightness = 0.70f)
    return top to bottom
}

private fun buildUniqueCourseColorMap(courses: List<TimeTableCourseUi>): Map<String, Pair<Color, Color>> {
    val titles = buildSet {
        for (course in courses) {
            val title = course.title.trim()
            if (title.isNotBlank()) add(title)
        }
    }.sorted()

    return buildMap(titles.size) {
        titles.forEachIndexed { index, title ->
            val colors = if (index < kCourseCardColors.size) {
                kCourseCardColors[index]
            } else {
                generatedCourseColorPair(index)
            }
            put(title, colors)
        }
    }
}

private const val TotalSections = 10
private const val TotalColumns = 7
private const val TotalWeeks = 20

private fun courseWeekForDisplaySlot(
    displayPageWeek: Int,
    dayOfWeek: Int,
    sundayFirst: Boolean,
): Int {
    return if (!sundayFirst && dayOfWeek == 7) displayPageWeek + 1 else displayPageWeek
}

private fun displayPageWeekForCourse(
    courseWeek: Int,
    dayOfWeek: Int,
    sundayFirst: Boolean,
): Int {
    return if (!sundayFirst && dayOfWeek == 7) courseWeek - 1 else courseWeek
}

enum class WeekBadgeState {
    NotStarted,
    Current,
    NotCurrent,
}

private fun lerpFloat(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

@Immutable
private data class TimetableSizeSpec(
    val timeColumnWidth: Dp = 48.dp,
    val cellHeight: Dp = 64.dp,
)

@Composable
fun TimeTableComposeScreen(
    modifier: Modifier = Modifier,
    termText: String,
    weekText: String,
    displayWeek: Int,
    currentWeek: Int,
    termStarted: Boolean,
    courses: List<TimeTableCourseUi>,
    remark: List<String> = emptyList(),
    displaySettings: TimeTableDisplaySettings,
    dateHeaderProvider: (Int) -> Pair<String, List<TimeTableDayHeaderUi>>,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    onTermClick: () -> Unit,
    onWeekClick: () -> Unit,
    onWeekChange: (Int) -> Unit,
    onDisplaySettingsChange: (TimeTableDisplaySettings) -> Unit,
    onEmptySlotClick: (dayOfWeek: Int, startSection: Int, courseWeek: Int) -> Unit,
    onCourseClick: (TimeTableCourseUi) -> Unit,
    onOverlapCoursesClick: (List<TimeTableCourseUi>) -> Unit,
    onCourseLongClick: (TimeTableCourseUi) -> Unit,
) {
    val colors = AppTheme.colors
    val sizeSpec = remember { TimetableSizeSpec() }
    val dayOrder = remember(displaySettings.sundayFirst) {
        if (displaySettings.sundayFirst) listOf(7, 1, 2, 3, 4, 5, 6) else (1..7).toList()
    }
    val showSettingsDialog = remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(
        initialPage = (displayWeek - 1).coerceIn(0, TotalWeeks - 1),
        pageCount = { TotalWeeks },
    )

    LaunchedEffect(displayWeek) {
        val targetPage = (displayWeek - 1).coerceIn(0, TotalWeeks - 1)
        if (!pagerState.isScrollInProgress && pagerState.currentPage != targetPage) {
            if (abs(pagerState.currentPage - targetPage) > 2) {
                pagerState.scrollToPage(targetPage)
            } else {
                pagerState.animateScrollToPage(targetPage)
            }
        }
    }

    val coursesByWeek = remember(courses, displaySettings.sundayFirst) {
        List(TotalWeeks) { mutableListOf<TimeTableCourseUi>() }.also { weekBuckets ->
            for (course in courses) {
                if (course.sectionSpan <= 0 || course.startSection <= 0) continue
                for (courseWeek in course.weeks) {
                    val displayPageWeek = displayPageWeekForCourse(
                        courseWeek = courseWeek,
                        dayOfWeek = course.dayOfWeek,
                        sundayFirst = displaySettings.sundayFirst,
                    )
                    if (displayPageWeek in 1..TotalWeeks) {
                        weekBuckets[displayPageWeek - 1].add(course)
                    }
                }
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .map { it + 1 }
            .distinctUntilChanged()
            .collect { week ->
                if (week != displayWeek) onWeekChange(week)
            }
    }

    val settledWeek = remember { derivedStateOf { pagerState.settledPage + 1 } }
    val effectiveWeek = settledWeek.value
    val weekBadgeState = remember(termStarted, currentWeek, effectiveWeek) {
        when {
            !termStarted -> WeekBadgeState.NotStarted
            effectiveWeek == currentWeek -> WeekBadgeState.Current
            else -> WeekBadgeState.NotCurrent
        }
    }
    val (monthText, dayHeaders) = dateHeaderProvider(settledWeek.value)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.bgTopBarColor.copy(alpha = 0.95f),
                        colors.bgPrimaryColor,
                        colors.bgPrimaryColor,
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TimeTableTopBar(
            isRefreshing = isRefreshing,
            onRefreshClick = onRefreshClick,
            onSettingsClick = { showSettingsDialog.value = true },
        )
        TermWeekBar(
            remark = remark,
            termText = termText,
            weekText = "第${effectiveWeek}周",
            weekBadgeState = weekBadgeState,
            onTermClick = onTermClick,
            onWeekClick = onWeekClick,
        )
        DateHeaderRow(
            monthText = monthText,
            dayHeaders = dayHeaders,
            timeColumnWidth = sizeSpec.timeColumnWidth,
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp),
            pageSpacing = 12.dp,
        ) { pageIndex ->
            val week = pageIndex + 1
            val pageCourses = coursesByWeek[pageIndex]
            val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
            val motion = (1f - pageOffset.coerceIn(0f, 1f))
            val cardScale = lerpFloat(0.93f, 1f, motion)
            val cardAlpha = lerpFloat(0.72f, 1f, motion)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        alpha = cardAlpha
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.bgCardColor.copy(alpha = 0.88f))
                    .padding(start = 2.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
            ) {
                val scrollState = rememberScrollState()
                TimeAxisColumn(
                    modifier = Modifier
                        .width(sizeSpec.timeColumnWidth)
                        .verticalScroll(scrollState),
                    cellHeight = sizeSpec.cellHeight,
                )

                TimeTableGrid(
                    modifier = Modifier.weight(1f),
                    cellHeight = sizeSpec.cellHeight,
                    scrollState = scrollState,
                    visibleCourses = pageCourses,
                    dayOrder = dayOrder,
                    courseFontScale = displaySettings.courseFontScale,
                    onEmptySlotClick = { dayOfWeek, startSection ->
                        val courseWeek = courseWeekForDisplaySlot(
                            displayPageWeek = week,
                            dayOfWeek = dayOfWeek,
                            sundayFirst = displaySettings.sundayFirst,
                        )
                        if (courseWeek in 1..TotalWeeks) {
                            onEmptySlotClick(dayOfWeek, startSection, courseWeek)
                        }
                    },
                    onCourseClick = onCourseClick,
                    onOverlapCoursesClick = onOverlapCoursesClick,
                    onCourseLongClick = onCourseLongClick,
                )
            }
        }
    }

    if (showSettingsDialog.value) {
        TimeTableSettingsDialog(
            currentSettings = displaySettings,
            onSave = onDisplaySettingsChange,
            onDismiss = { showSettingsDialog.value = false },
        )
    }
}

@Composable
private fun TimeTableTopBar(
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, start = 14.dp, end = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(96.dp))
        Text(
            text = "课程表",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 23.sp,
            color = colors.titleTopColor,
        )
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "课表设置",
                tint = colors.titleTopColor,
            )
        }
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = colors.titleTopColor,
                )
            } else {
                IconButton(onClick = onRefreshClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.coursetable_ic_refresh),
                        contentDescription = "刷新",
                        tint = colors.titleTopColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeTableSettingsDialog(
    currentSettings: TimeTableDisplaySettings,
    onSave: (TimeTableDisplaySettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val hapticFeedback = LocalHapticFeedback.current
    val pendingSettings = remember(currentSettings) { mutableStateOf(currentSettings) }
    val fontScalePercent = (pendingSettings.value.courseFontScale * 100).roundToInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSave(pendingSettings.value)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.commonColor,
                    contentColor = Color.White,
                ),
            ) {
                Text(text = "保存", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消", color = colors.secondaryTextColor)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = colors.commonColor.copy(alpha = 0.12f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = colors.commonColor,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                }
                Column {
                    Text(
                        text = "课表设置",
                        color = colors.primaryTextColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "按照你的习惯调整显示",
                        color = colors.secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = colors.outlineLowContrastColor.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(16.dp),
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.commonColor.copy(alpha = 0.06f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "周日显示在第一列",
                                color = colors.primaryTextColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "开启后按周日、周一…周六排列",
                                color = colors.secondaryTextColor,
                                fontSize = 12.sp,
                            )
                        }
                        Switch(
                            checked = pendingSettings.value.sundayFirst,
                            onCheckedChange = { sundayFirst ->
                                pendingSettings.value = pendingSettings.value.copy(sundayFirst = sundayFirst)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.commonColor,
                                checkedBorderColor = colors.commonColor,
                                uncheckedThumbColor = colors.secondaryTextColor,
                                uncheckedTrackColor = colors.bgCardColor,
                                uncheckedBorderColor = colors.outlineLowContrastColor,
                            ),
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = colors.outlineLowContrastColor.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(16.dp),
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.commonColor.copy(alpha = 0.06f),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "课程字体大小",
                                color = colors.primaryTextColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Surface(
                                shape = CircleShape,
                                color = colors.commonColor.copy(alpha = 0.13f),
                            ) {
                                Text(
                                    text = "$fontScalePercent%",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = colors.commonColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Slider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            value = pendingSettings.value.courseFontScale,
                            onValueChange = { fontScale ->
                                val snappedFontScale = TimeTableDisplaySettings.snapCourseFontScale(fontScale)
                                if (snappedFontScale != pendingSettings.value.courseFontScale) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    pendingSettings.value = pendingSettings.value.copy(
                                        courseFontScale = snappedFontScale,
                                    )
                                }
                            },
                            valueRange = TimeTableDisplaySettings.MIN_COURSE_FONT_SCALE..
                                TimeTableDisplaySettings.MAX_COURSE_FONT_SCALE,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = colors.commonColor,
                                activeTrackColor = colors.commonColor,
                                activeTickColor = Color.White,
                                inactiveTrackColor = colors.outlineLowContrastColor.copy(alpha = 0.65f),
                                inactiveTickColor = colors.commonColor.copy(alpha = 0.5f),
                            ),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(text = "小", color = colors.secondaryTextColor, fontSize = 11.sp)
                            Text(text = "标准", color = colors.secondaryTextColor, fontSize = 11.sp)
                            Text(text = "大", color = colors.secondaryTextColor, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            colors.commonColor.copy(alpha = 0.13f),
                                            colors.outlineLowContrastColor.copy(alpha = 0.42f),
                                        )
                                    )
                                )
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "高等数学",
                                    color = colors.primaryTextColor,
                                    fontSize = (11f * pendingSettings.value.courseFontScale).sp,
                                    lineHeight = (13f * pendingSettings.value.courseFontScale).sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "明理楼 A201",
                                    color = colors.secondaryTextColor,
                                    fontSize = (9f * pendingSettings.value.courseFontScale).sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = colors.bgCardColor,
        titleContentColor = colors.primaryTextColor,
        textContentColor = colors.secondaryTextColor,
    )
}

@Composable
private fun TermWeekBar(
    termText: String,
    weekText: String,
    weekBadgeState: WeekBadgeState,
    remark: List<String> = emptyList(),
    onTermClick: () -> Unit,
    onWeekClick: () -> Unit,
) {
    val colors = AppTheme.colors
    var showRemarkDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val badgeContainerColor = when (weekBadgeState) {
        WeekBadgeState.NotStarted -> colors.textHeighLightColor.copy(alpha = 0.14f)
        WeekBadgeState.Current -> colors.successGreenColor.copy(alpha = 0.16f)
        WeekBadgeState.NotCurrent -> colors.secondaryTextColor.copy(alpha = 0.12f)
    }
    val badgeTextColor = when (weekBadgeState) {
        WeekBadgeState.NotStarted -> colors.textHeighLightColor
        WeekBadgeState.Current -> colors.successGreenColor
        WeekBadgeState.NotCurrent -> colors.secondaryTextColor
    }
    val badgeText = when (weekBadgeState) {
        WeekBadgeState.NotStarted -> "未开学"
        WeekBadgeState.Current -> "本周"
        WeekBadgeState.NotCurrent -> "非本周"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, top = 1.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TimetableCapsule(
            text = termText,
            leadingIcon = R.drawable.coursetable_ic_calendar,
            onClick = onTermClick,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TimetableCapsule(
                text = weekText,
                trailingIcon = R.drawable.coursetable_ic_extend,
                onClick = onWeekClick,
            )
            IconButton(
                onClick = {
                    if (remark.isNotEmpty()) {
                        showRemarkDialog.value = true
                    } else {
                        Toast.makeText(context, "暂无备注", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "备注",
                    tint = colors.secondaryTextColor,
                    modifier = Modifier.size(20.dp),
                )
            }
            Surface(
                shape = CircleShape,
                color = badgeContainerColor,
            ) {
                Text(
                    text = badgeText,
                    color = badgeTextColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    if (showRemarkDialog.value) {
        RemarkDialog(
            remark = remark,
            onDismiss = { showRemarkDialog.value = false },
        )
    }

    }

}
@Composable
private fun TimetableCapsule(
    text: String,
    onClick: () -> Unit,
    leadingIcon: Int? = null,
    trailingIcon: Int? = null,
) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, colors.outlineLowContrastColor.copy(alpha = 0.5f), CircleShape)
            .combinedClickable(onClick = onClick),
        color = colors.bgCardColor.copy(alpha = 0.92f),
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (leadingIcon != null) {
                Icon(
                    modifier = Modifier.size(24.dp, 24.dp),
                    painter = painterResource(id = leadingIcon),
                    contentDescription = null,
                    tint = colors.iconSecondaryColor,
                )
            }
            Text(
                text = text,
                color = colors.primaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            if (trailingIcon != null) {
                Icon(
                    painter = painterResource(id = trailingIcon),
                    contentDescription = null,
                    tint = colors.iconSecondaryColor,
                )
            }
        }
    }
}

@Composable
private fun DateHeaderRow(
    monthText: String,
    dayHeaders: List<TimeTableDayHeaderUi>,
    timeColumnWidth: Dp,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.bgCardColor.copy(alpha = 0.35f))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = colors.bgSecondaryColor.copy(alpha = 0.8f),
        ) {
            Text(
                text = monthText,
                color = colors.primaryTextColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(timeColumnWidth)
                    .padding(vertical = 6.dp),
                textAlign = TextAlign.Center,
            )
        }
        dayHeaders.forEach { header ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = header.weekdayLabel,
                    color = if (header.isToday) colors.textHeighLightColor else colors.primaryTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = header.dayOfMonthLabel,
                    color = if (header.isToday) colors.textHeighLightColor else colors.greyTextColor,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun TimeAxisColumn(
    modifier: Modifier,
    cellHeight: Dp,
) {
    val colors = AppTheme.colors
    val times = remember {
        listOf(
            "8:00\n8:45", "8:55\n9:40", "10:10\n10:55", "11:05\n11:50",
            "14:00\n14:45", "14:55\n15:40", "16:10\n16:55", "17:05\n17:50",
            "19:30\n20:15", "20:25\n21:10"
        )
    }

    Column(modifier = modifier) {
        repeat(TotalSections) { index ->
            Box(
                modifier = Modifier
                    .height(cellHeight)
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
                    .drawBehind {
                        drawLine(
                            color = colors.dividerColor.copy(alpha = 0.08f),
                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            strokeWidth = 1f,
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = times[index],
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = colors.primaryTextColor.copy(alpha = 0.88f),
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun TimeTableGrid(
    modifier: Modifier,
    cellHeight: Dp,
    scrollState: androidx.compose.foundation.ScrollState,
    visibleCourses: List<TimeTableCourseUi>,
    dayOrder: List<Int>,
    courseFontScale: Float,
    onEmptySlotClick: (dayOfWeek: Int, startSection: Int) -> Unit,
    onCourseClick: (TimeTableCourseUi) -> Unit,
    onOverlapCoursesClick: (List<TimeTableCourseUi>) -> Unit,
    onCourseLongClick: (TimeTableCourseUi) -> Unit,
) {
    val colors = AppTheme.colors
    val totalHeight = cellHeight * TotalSections
    val courseColorMap = remember(visibleCourses) {
        buildUniqueCourseColorMap(visibleCourses)
    }
    val displayColumnByDay = remember(dayOrder) {
        IntArray(TotalColumns + 1).apply {
            dayOrder.forEachIndexed { columnIndex, dayOfWeek ->
                this[dayOfWeek] = columnIndex
            }
        }
    }
    val cellCoursesMap = remember(visibleCourses) {
        val result = mutableMapOf<Pair<Int, Int>, MutableList<TimeTableCourseUi>>()
        visibleCourses.forEach { course ->
            val endSection = (course.startSection + course.sectionSpan - 1).coerceAtMost(TotalSections)
            for (section in course.startSection..endSection) {
                val key = course.dayOfWeek to section
                result.getOrPut(key) { mutableListOf() }.add(course)
            }
        }
        result
    }
    val courseOverlapMap = remember(visibleCourses, cellCoursesMap) {
        val result = mutableMapOf<Int, List<TimeTableCourseUi>>()
        visibleCourses.forEach { course ->
            val endSection = (course.startSection + course.sectionSpan - 1).coerceAtMost(TotalSections)
            val overlaps = linkedSetOf<TimeTableCourseUi>()
            for (section in course.startSection..endSection) {
                cellCoursesMap[course.dayOfWeek to section].orEmpty().forEach { overlaps.add(it) }
            }
            result[course.id] = overlaps.toList()
        }
        result
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
    ) {
        val boardWidth = maxWidth
        val columnWidth = boardWidth / TotalColumns

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
                .padding(end = 2.dp),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val lineColor = colors.dividerColor.copy(alpha = 0.1f)
                val h = size.height
                val w = size.width
                val rowHeightPx = cellHeight.toPx()
                val colWidthPx = w / TotalColumns

                repeat(TotalSections + 1) { row ->
                    val y = row * rowHeightPx
                    drawLine(
                        lineColor,
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(w, y),
                        strokeWidth = 0.8f,
                    )
                }

                repeat(TotalColumns + 1) { col ->
                    val x = col * colWidthPx
                    drawLine(
                        lineColor,
                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                        end = androidx.compose.ui.geometry.Offset(x, h),
                        strokeWidth = 0.8f,
                    )
                }
            }

            Column(modifier = Modifier.matchParentSize()) {
                repeat(TotalSections) { section ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(TotalColumns) { day ->
                            val dayOfWeek = dayOrder[day]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(cellHeight)
                                    .combinedClickable(
                                        onClick = {
                                            val hitCourses =
                                                cellCoursesMap[dayOfWeek to section + 1].orEmpty()
                                            when (hitCourses.size) {
                                                0 -> onEmptySlotClick(dayOfWeek, section + 1)
                                                1 -> onCourseClick(hitCourses.first())
                                                else -> onOverlapCoursesClick(hitCourses)
                                            }
                                        },
                                        onLongClick = {
                                            val hitCourses =
                                                cellCoursesMap[dayOfWeek to section + 1].orEmpty()
                                            if (hitCourses.isNotEmpty()) {
                                                onCourseLongClick(hitCourses.first())
                                            }
                                        },
                                    ),
                            )
                        }
                    }
                }
            }

            visibleCourses.forEach { course ->
                val x = columnWidth * displayColumnByDay[course.dayOfWeek]
                val y = cellHeight * (course.startSection - 1)
                val cardHeight = cellHeight * course.sectionSpan

                TimeTableCourseCard(
                    course = course,
                    courseColors = courseColorMap[course.title] ?: courseCardColorPair(course.title),
                    fontScale = courseFontScale,
                    modifier = Modifier
                        .offset { IntOffset(x.roundToPx(), y.roundToPx()) }
                        .width(columnWidth)
                        .height(cardHeight)
                        .padding(horizontal = 1.5.dp, vertical = 1.5.dp),
                    onClick = {
                        val overlaps = courseOverlapMap[course.id].orEmpty()
                        when (overlaps.size) {
                            0, 1 -> onCourseClick(course)
                            else -> onOverlapCoursesClick(overlaps)
                        }
                    },
                    onLongClick = {
                        val overlaps = courseOverlapMap[course.id].orEmpty()
                        onCourseLongClick(overlaps.firstOrNull() ?: course)
                    },
                )
            }
        }
    }
}

@Composable
private fun TimeTableCourseCard(
    course: TimeTableCourseUi,
    courseColors: Pair<Color, Color>,
    fontScale: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val (topColor, bottomColor) = courseColors
    val cardBrush = remember(topColor, bottomColor) {
        Brush.verticalGradient(listOf(topColor, bottomColor))
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBrush)
                .padding(start = 2.dp, end = 2.dp, top = 13.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = course.title,
                color = kCourseCardTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = (11f * fontScale).sp,
                maxLines = 3,
                lineHeight = (12f * fontScale).sp,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (course.room.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.room,
                    color = kCourseCardTextColor.copy(alpha = 0.9f),
                    fontSize = (10f * fontScale).sp,
                    lineHeight = (11f * fontScale).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (course.teacher.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = course.teacher,
                    color = kCourseCardTextColor.copy(alpha = 0.72f),
                    fontSize = (9f * fontScale).sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@Composable
private fun RemarkDialog(
    remark: List<String>,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "我知道了", color = colors.commonColor)
            }
        },
        title = {
            Text(text = "备注", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                remark.forEachIndexed { index, item ->
                    Text(
                        text = "${index + 1}. $item",
                        color = colors.primaryTextColor,
                        fontSize = 14.sp,
                    )
                }
            }
        },
        containerColor = colors.bgCardColor,
        titleContentColor = colors.primaryTextColor,
        textContentColor = colors.secondaryTextColor,
    )
}

@Preview(showBackground = true)
@Composable
private fun TimeTableScreenPreview() {
    AppSkinTheme {
        TimeTableComposeScreen(
            termText = "2025-2026-2",
            weekText = "第5周",
            displayWeek = 5,
            currentWeek = 5,
            termStarted = true,
            isRefreshing = false,
            displaySettings = TimeTableDisplaySettings(),
            courses = listOf(
                TimeTableCourseUi(1, "高等数学", "张老师", "明理楼A201", 1, 1, 2, setOf(1, 2, 3, 4, 5), false),
                TimeTableCourseUi(2, "数据结构", "李老师", "综合楼C305", 3, 5, 2, setOf(5, 6, 7), true),
                TimeTableCourseUi(3, "英语", "王老师", "外语楼202", 5, 9, 2, setOf(5), false),
            ),
            dateHeaderProvider = {
                "3月" to listOf(
                    TimeTableDayHeaderUi("周一", "03日", false),
                    TimeTableDayHeaderUi("周二", "04日", false),
                    TimeTableDayHeaderUi("周三", "05日", true),
                    TimeTableDayHeaderUi("周四", "06日", false),
                    TimeTableDayHeaderUi("周五", "07日", false),
                    TimeTableDayHeaderUi("周六", "08日", false),
                    TimeTableDayHeaderUi("周日", "09日", false),
                )
            },
            onRefreshClick = {},
            onTermClick = {},
            onWeekClick = {},
            onWeekChange = {},
            onDisplaySettingsChange = {},
            onEmptySlotClick = { _, _, _ -> },
            onCourseClick = {},
            onOverlapCoursesClick = {},
            onCourseLongClick = {},
        )
    }
}
