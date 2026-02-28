package com.creamaker.changli_planet_app.feature.timetable.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.absoluteValue

// 课程卡片渐变配色：8 组柔和渐变，Pair(顶色, 底色)
// 同一课程名称始终映射同一颜色（hash by title）
internal val kCourseCardColors = listOf(
    Pair(Color(0xFFCDE8FF), Color(0xFF8EC9F5)), // 天蓝
    Pair(Color(0xFFB8F0CE), Color(0xFF6BDAA0)), // 薄荷
    Pair(Color(0xFFD8D0FF), Color(0xFFA896F5)), // 薰衣草
    Pair(Color(0xFFFFE7B0), Color(0xFFFFCA60)), // 琥珀
    Pair(Color(0xFFFFCDD8), Color(0xFFFF9AB8)), // 玫瑰
    Pair(Color(0xFFA8EFEC), Color(0xFF5DD5D0)), // 碧青
    Pair(Color(0xFFFFD8B8), Color(0xFFFFAA72)), // 蜜桃
    Pair(Color(0xFFCEF0A8), Color(0xFF8ED460)), // 草绿
)
// 卡片文字统一用深海军蓝，在所有浅色卡片上可读性最佳
internal val kCourseCardTextColor = Color(0xFF1C2B3A)

/** 根据课程名称取得 (渐变顶色, 渐变底色)，相同名称始终同色 */
internal fun courseCardColorPair(courseTitle: String): Pair<Color, Color> =
    kCourseCardColors[courseTitle.hashCode().absoluteValue % kCourseCardColors.size]

private fun generatedCourseColorPair(index: Int): Pair<Color, Color> {
    val hue = ((index * 37) % 360).toFloat()
    val top = Color.hsl(hue, saturation = 0.52f, lightness = 0.84f)
    val bottom = Color.hsl(hue, saturation = 0.62f, lightness = 0.70f)
    return top to bottom
}

private fun buildUniqueCourseColorMap(courses: List<TimeTableCourseUi>): Map<String, Pair<Color, Color>> {
    val titles = courses
        .map { it.title.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    return titles.mapIndexed { index, title ->
        val colors = if (index < kCourseCardColors.size) {
            kCourseCardColors[index]
        } else {
            generatedCourseColorPair(index)
        }
        title to colors
    }.toMap()
}

private const val TotalSections = 10
private const val TotalColumns = 7
private const val TotalWeeks = 20

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
    val timeColumnWidth: Dp = 58.dp,
    val cellHeight: Dp = 74.dp,
)

@Composable
fun TimeTableComposeScreen(
    termText: String,
    weekText: String,
    isCurrentWeek: Boolean,
    weekBadgeState: WeekBadgeState,
    displayWeek: Int,
    courses: List<TimeTableCourseUi>,
    dateHeaderProvider: (Int) -> Pair<String, List<TimeTableDayHeaderUi>>,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    onTermClick: () -> Unit,
    onWeekClick: () -> Unit,
    onWeekChange: (Int) -> Unit,
    onEmptySlotClick: (dayOfWeek: Int, startSection: Int) -> Unit,
    onCourseClick: (TimeTableCourseUi) -> Unit,
    onOverlapCoursesClick: (List<TimeTableCourseUi>) -> Unit,
    onCourseLongClick: (TimeTableCourseUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val sizeSpec = remember { TimetableSizeSpec() }
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

    val coursesByWeek = remember(courses) {
        (1..TotalWeeks).associateWith { week ->
            courses.filter { week in it.weeks && it.sectionSpan > 0 && it.startSection > 0 }
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
    ) {
        TimeTableTopBar(
            isRefreshing = isRefreshing,
            onRefreshClick = onRefreshClick,
        )
        TermWeekBar(
            termText = termText,
            weekText = "第${settledWeek.value}周",
            isCurrentWeek = settledWeek.value == displayWeek && isCurrentWeek,
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
                .padding(horizontal = 4.dp)
                .fillMaxWidth()
                .weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp),
            pageSpacing = 12.dp,
        ) { pageIndex ->
            val week = pageIndex + 1
            val pageCourses = coursesByWeek[week].orEmpty()
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
                    onEmptySlotClick = onEmptySlotClick,
                    onCourseClick = onCourseClick,
                    onOverlapCoursesClick = onOverlapCoursesClick,
                    onCourseLongClick = onCourseLongClick,
                )
            }
        }
    }
}

@Composable
private fun TimeTableTopBar(
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 14.dp, end = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(40.dp))
        Text(
            text = "课程表",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 23.sp,
            color = colors.titleTopColor,
        )
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp),
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

@Composable
private fun TermWeekBar(
    termText: String,
    weekText: String,
    isCurrentWeek: Boolean,
    weekBadgeState: WeekBadgeState,
    onTermClick: () -> Unit,
    onWeekClick: () -> Unit,
) {
    val colors = AppTheme.colors
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
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(cellHeight)
                                    .combinedClickable(
                                        onClick = {
                                            val hitCourses = cellCoursesMap[day + 1 to section + 1].orEmpty()
                                            when (hitCourses.size) {
                                                0 -> onEmptySlotClick(day + 1, section + 1)
                                                1 -> onCourseClick(hitCourses.first())
                                                else -> onOverlapCoursesClick(hitCourses)
                                            }
                                        },
                                        onLongClick = {
                                            val hitCourses = cellCoursesMap[day + 1 to section + 1].orEmpty()
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
                val x = columnWidth * (course.dayOfWeek - 1)
                val y = cellHeight * (course.startSection - 1)
                val cardHeight = cellHeight * course.sectionSpan

                TimeTableCourseCard(
                    course = course,
                    courseColors = courseColorMap[course.title] ?: courseCardColorPair(course.title),
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
                fontSize = 11.sp,
                maxLines = 3,
                lineHeight = 12.sp,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (course.room.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.room,
                    color = kCourseCardTextColor.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    lineHeight = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (course.teacher.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = course.teacher,
                    color = kCourseCardTextColor.copy(alpha = 0.72f),
                    fontSize = 9.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimeTableScreenPreview() {
    AppSkinTheme {
        TimeTableComposeScreen(
            termText = "2025-2026-2",
            weekText = "第5周",
            isCurrentWeek = true,
            weekBadgeState = WeekBadgeState.Current,
            displayWeek = 5,
            isRefreshing = false,
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
            onEmptySlotClick = { _, _ -> },
            onCourseClick = {},
            onOverlapCoursesClick = {},
            onCourseLongClick = {},
        )
    }
}
