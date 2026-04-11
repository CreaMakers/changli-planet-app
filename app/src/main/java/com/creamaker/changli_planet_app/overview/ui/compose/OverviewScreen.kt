package com.creamaker.changli_planet_app.overview.ui.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.overview.ui.model.OverviewCourseUiModel
import com.creamaker.changli_planet_app.overview.ui.model.OverviewExamUiModel
import com.creamaker.changli_planet_app.overview.ui.model.OverviewHomeworkUiModel
import com.creamaker.changli_planet_app.overview.ui.model.OverviewMetricUiModel
import com.creamaker.changli_planet_app.overview.ui.model.OverviewTestUiModel
import com.creamaker.changli_planet_app.overview.ui.model.OverviewUiState
import com.creamaker.changli_planet_app.overview.viewmodel.OverviewViewModel
import kotlinx.coroutines.delay

private val HomeworkAccent = Color(0xFFEF9442)
private val TestAccent = Color(0xFF4F7FED)
private val IconContainerShape = RoundedCornerShape(14.dp)

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel,
    onBindClick: () -> Unit,
    onQuickActionClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    OverviewScreen(
        state = state,
        onBindClick = onBindClick,
        onMetricClick = onQuickActionClick,
        onQuickActionClick = onQuickActionClick
    )
}

@Composable
private fun OverviewScreen(
    state: OverviewUiState,
    onBindClick: () -> Unit,
    onMetricClick: (String) -> Unit,
    onQuickActionClick: (String) -> Unit
) {
    val colors = AppTheme.colors
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overviewPageBackgroundColor)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(androidx.compose.foundation.layout.WindowInsetsSides.Top)),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            HeaderBlock(state = state, onBindClick = onBindClick)
        }

        item {
            SectionTitle(if (state.isShowingTomorrow) "明日课程" else "今日课程", "查看全部") { onQuickActionClick("schedule") }
        }

        item {
            TodayCourseSection(
                courses = state.todayCourses,
                emptyMessage = state.todayCourseMessage,
                onClick = { onQuickActionClick("schedule") }
            )
        }

        item {
            MetricRow(
                metrics = state.metrics,
                onMetricClick = onMetricClick
            )
        }

        item {
            SectionTitle("待提交作业", "查看全部") { onQuickActionClick("homework") }
        }

        if (state.pendingHomeworks.isEmpty()) {
            item {
                NeutralCard(state.pendingHomeworkMessage)
            }
        } else {
            items(state.pendingHomeworks, key = { it.id }) { homework ->
                HomeworkCard(homework)
            }
        }

        if (state.pendingTests.isNotEmpty()) {
            item {
                SectionTitle("待完成测试", "查看全部") { onQuickActionClick("homework") }
            }
            items(state.pendingTests, key = { it.id }) { test ->
                TestCard(test)
            }
        }

        item {
            SectionTitle("考试安排", "查看全部") { onQuickActionClick("exam") }
        }

        if (state.upcomingExams.isEmpty()) {
            item {
                NeutralCard(state.examMessage)
            }
        } else {
            items(state.upcomingExams, key = { it.id }) { exam ->
                ExamCard(exam)
            }
        }

    }
}

@Composable
private fun HeaderBlock(
    state: OverviewUiState,
    onBindClick: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "概况",
            color = colors.primaryTextColor,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = state.dateText,
            color = colors.secondaryTextColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
//        if (!state.isBoundStudent) {
//            Spacer(modifier = Modifier.height(14.dp))
//            BindPrompt("先绑定学号", onBindClick)
//        } else if (state.isSilentSyncing && state.todayCourses.isEmpty()) {
//            Spacer(modifier = Modifier.height(14.dp))
//            Text(
//                text = "没有数据，后台开始查询",
//                color = colors.secondaryTextColor,
//                fontSize = 14.sp
//            )
//        }
    }
}

@Composable
private fun BindPrompt(text: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.overviewPromptBackgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = text, color = colors.overviewPromptTextColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(
    title: String,
    actionText: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = colors.primaryTextColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f)
        )
        Row(
            modifier = Modifier.clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = actionText,
                color = colors.secondaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = actionText,
                tint = colors.secondaryTextColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TodayCourseSection(
    courses: List<OverviewCourseUiModel>,
    emptyMessage: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = colors.bgCardColor,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        if (courses.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_confirm),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    alpha = 0.95f
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = emptyMessage.ifBlank { "没有数据" },
                    color = colors.primaryTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                courses.forEach { course ->
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(999.dp))
                                .background(course.accentColor)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = course.courseName,
                                color = colors.primaryTextColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${course.timeText}  ·  ${course.classroom}",
                                color = course.accentColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = course.teacher,
                                color = colors.secondaryTextColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    metrics: List<OverviewMetricUiModel>,
    onMetricClick: (String) -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        metrics.forEach { metric ->
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = colors.bgCardColor,
                modifier = Modifier
                    .weight(1f)
                    .height(184.dp)
                    .clickable { onMetricClick(metric.id) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(IconContainerShape)
                                .background(
                                    when (metric.id) {
                                        "ScoreInquiry", "score" -> colors.overviewScoreIconBackgroundColor
                                        "Electronic", "electric" -> colors.overviewElectricIconBackgroundColor
                                        else -> metric.accentColor.copy(alpha = 0.14f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(metric.iconRes),
                                contentDescription = metric.title,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = metric.title,
                            color = colors.secondaryTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = metric.value,
                                color = metric.accentColor,
                                fontSize = metricValueFontSize(metric.value),
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                            if (metric.unit.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = metric.unit,
                                    color = colors.secondaryTextColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        MetricSubtitle(metric)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricSubtitle(metric: OverviewMetricUiModel) {
    val colors = AppTheme.colors
    val subtitlePages = remember(metric.subtitle, metric.secondarySubtitle) {
        listOf(metric.subtitle, metric.secondarySubtitle)
            .filter { it.isNotBlank() }
            .distinct()
    }
    val currentIndex = remember(metric.id, subtitlePages) { mutableIntStateOf(0) }

    LaunchedEffect(metric.id, subtitlePages) {
        if (subtitlePages.size <= 1) return@LaunchedEffect
        while (true) {
            delay(2800)
            currentIndex.intValue = (currentIndex.intValue + 1) % subtitlePages.size
        }
    }

    val currentSubtitle = subtitlePages.getOrElse(currentIndex.intValue) { metric.subtitle }
    AnimatedContent(
        targetState = currentSubtitle,
        transitionSpec = {
            (slideInVertically { it / 2 } + fadeIn()).togetherWith(
                slideOutVertically { -it / 2 } + fadeOut()
            )
        },
        label = "overview_metric_subtitle"
    ) { subtitle ->
        Text(
            text = subtitle,
            color = colors.secondaryTextColor,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeworkCard(homework: OverviewHomeworkUiModel) {
    val colors = AppTheme.colors
    val courseName = runCatching { homework.courseName }.getOrDefault("")
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (homework.isUrgent) colors.overviewUrgentBackgroundColor else colors.bgCardColor,
        border = if (homework.isUrgent) BorderStroke(1.5.dp, colors.overviewUrgentBorderColor) else null
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(HomeworkAccent)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = homework.title,
                    color = colors.primaryTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 23.sp
                )
                if (courseName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = courseName,
                        color = colors.secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (homework.deadlineText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = homework.deadlineText,
                        color = if (homework.isUrgent) colors.overviewUrgentBorderColor else colors.secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (homework.urgencyText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = homework.urgencyText,
                        color = if (homework.isUrgent) colors.overviewUrgentBorderColor else colors.secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(HomeworkAccent.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = homework.statusText,
                    color = HomeworkAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExamCard(exam: OverviewExamUiModel) {
    val colors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.bgCardColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.courseName,
                    color = colors.primaryTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = exam.examTime, color = colors.secondaryTextColor, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = exam.location, color = colors.secondaryTextColor, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.overviewExamBadgeBackgroundColor)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = exam.badge,
                    color = colors.overviewExamBadgeTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TestCard(test: OverviewTestUiModel) {
    val colors = AppTheme.colors
    val courseName = runCatching { test.courseName }.getOrDefault("")
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (test.isUrgent) colors.overviewUrgentBackgroundColor else colors.bgCardColor,
        border = if (test.isUrgent) BorderStroke(1.5.dp, colors.overviewUrgentBorderColor) else null
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(TestAccent)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = test.title,
                    color = colors.primaryTextColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 23.sp
                )
                if (courseName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = courseName,
                        color = colors.secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (test.timeText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = test.timeText,
                        color = if (test.isUrgent) colors.overviewUrgentBorderColor else colors.secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (test.urgencyText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = test.urgencyText,
                        color = if (test.isUrgent) colors.overviewUrgentBorderColor else colors.secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(TestAccent.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = test.statusText,
                    color = TestAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NeutralCard(text: String) {
    val colors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.bgCardColor
    ) {
        Text(
            text = if (text.isBlank()) "没有数据" else text,
            color = colors.secondaryTextColor,
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp)
        )
    }
}

private fun metricValueFontSize(value: String) = when {
    value.length >= 6 -> 28.sp
    value.length >= 5 -> 32.sp
    else -> 38.sp
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F3F6)
@Composable
private fun OverviewScreenPreview() {
    AppSkinTheme {
        OverviewScreen(
            state = OverviewUiState(
                isBoundStudent = true,
                isElectricityBound = true,
                dateText = "3月13日 周五  ·  2025-2026-2 第1周",
                metrics = listOf(
                    OverviewMetricUiModel("score", "GPA", "3.02", "", "平均分: 82.0", "", R.drawable.ic_rank, Color(0xFFE3B92C)),
                    OverviewMetricUiModel("electric", "电费", "207.61", "kWh", "预计163天后电量耗尽", "更新于 03-15 21:30", R.drawable.ic_bill, Color(0xFF62C466))
                ),
                todayCourses = listOf(
                    OverviewCourseUiModel("1", "高等数学", "云塘 A201", "陈老师", "1-2节", "校园课表", Color(0xFF5E87F6)),
                    OverviewCourseUiModel("2", "大学英语", "云塘 B305", "李老师", "3-4节", "校园课表", Color(0xFFF08D3C))
                ),
                pendingHomeworks = listOf(
                    OverviewHomeworkUiModel("1", "大数据存储与管理实验A", "计算机网络", "2026-03-13 23:59", "8小时内截止", true),
                    OverviewHomeworkUiModel("2", "数据库原理与技术", "数据库系统概论", "2026-03-15 20:00", "2天内截止", false)
                ),
                pendingTests = listOf(
                    OverviewTestUiModel("1", "第3章随堂测试", "数据库原理与技术", "2026-03-14 09:00 - 2026-03-14 22:00", "今天内截止", true)
                ),
                upcomingExams = listOf(
                    OverviewExamUiModel("1", "大学物理实验", "2026-03-14 09:00", "云塘校区 · 综合楼", "明天")
                )
            ),
            onBindClick = {},
            onMetricClick = {},
            onQuickActionClick = {}
        )
    }
}
