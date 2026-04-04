package com.creamaker.changli_planet_app.feature.mooc.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.core.network.ApiResponse
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.feature.mooc.viewmodel.MoocViewModel
import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocHomework
import com.dcelysia.csust_spider.mooc.data.remote.dto.MoocTest
import com.dcelysia.csust_spider.mooc.data.remote.dto.PendingAssignmentCourse

private val HomeworkAccent = Color(0xFFFF8A50)
private val TestAccent = Color(0xFF4F7FED)

@Composable
fun MoocScreen(
    moocViewModel: MoocViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val pendingCourses by moocViewModel.pendingCourse.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        moocViewModel.loginAndFetchCourses(
            StudentInfoManager.studentId,
            StudentInfoManager.studentPassword
        )
    }

    MoocScreenContent(
        pendingCourses = pendingCourses,
        moocViewModel = moocViewModel,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoocScreenContent(
    pendingCourses: ApiResponse<List<PendingAssignmentCourse>>,
    moocViewModel: MoocViewModel,
    onBack: () -> Unit
) {
    val colors = AppTheme.colors
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.pending_homework),
                            color = colors.primaryTextColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "展开课程后可查看待提交作业和待测试",
                            color = colors.secondaryTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = "返回",
                            tint = colors.primaryTextColor,
                            modifier = Modifier.rotate(180f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.overviewPageBackgroundColor,
                    titleContentColor = colors.primaryTextColor,
                    navigationIconContentColor = colors.primaryTextColor
                )
            )
        },
        containerColor = colors.overviewPageBackgroundColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.overviewPageBackgroundColor)
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SummaryCard(pendingCourses = pendingCourses)
            }

            when (pendingCourses) {
                is ApiResponse.Loading -> {
                    item { LoadingCard() }
                }

                is ApiResponse.Error -> {
                    item {
                        MessageCard(
                            title = "加载失败",
                            message = pendingCourses.msg.ifBlank { "当前无法获取慕课数据，请稍后再试" },
                            accent = colors.errorRedColor
                        )
                    }
                }

                is ApiResponse.Success -> {
                    val courses = pendingCourses.data
                    if (courses.isEmpty()) {
                        item {
                            MessageCard(
                                title = "当前没有待处理事项",
                                message = stringResource(R.string.no_pending_assignments),
                                accent = colors.successGreenColor
                            )
                        }
                    } else {
                        items(courses, key = { it.id }) { course ->
                            CourseCard(course = course, moocViewModel = moocViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(pendingCourses: ApiResponse<List<PendingAssignmentCourse>>) {
    val colors = AppTheme.colors
    val count = (pendingCourses as? ApiResponse.Success)?.data?.size ?: 0
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = colors.bgCardColor,
        tonalElevation = 0.dp,
        shadowElevation = if (isSystemInDarkTheme()) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "待办总览",
                color = colors.secondaryTextColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (count > 0) "当前有 $count 门课程需要处理" else "正在整理课程待办",
                color = colors.primaryTextColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    val colors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.bgCardColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colors.loadingColor)
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    message: String,
    accent: Color
) {
    val colors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.bgCardColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = message,
                color = colors.secondaryTextColor,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun CourseCard(
    course: PendingAssignmentCourse,
    moocViewModel: MoocViewModel
) {
    val colors = AppTheme.colors
    val expandedIds by moocViewModel.expandedCourseIds.collectAsStateWithLifecycle()
    val pendingHomeworksByCourse by moocViewModel.pendingHomeworksByCourse.collectAsStateWithLifecycle()
    val pendingTestsByCourse by moocViewModel.pendingTestsByCourse.collectAsStateWithLifecycle()
    val expanded = expandedIds.contains(course.id)
    val homeworks = pendingHomeworksByCourse[course.id] ?: ApiResponse.Loading()
    val tests = pendingTestsByCourse[course.id] ?: ApiResponse.Loading()
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "courseExpansionAngle"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = colors.bgCardColor,
        tonalElevation = 0.dp,
        shadowElevation = if (isSystemInDarkTheme()) 0.dp else 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { moocViewModel.handleCourseClick(course.id) }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusPill(text = "课程待办", accent = HomeworkAccent, filled = false)
                    Text(
                        text = course.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.primaryTextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = "课程 ID: ${course.id}",
                        fontSize = 12.sp,
                        color = colors.secondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_expand),
                    contentDescription = if (expanded) "收起" else "展开",
                    tint = colors.iconSecondaryColor,
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .size(22.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(260)) + expandVertically(animationSpec = tween(260)),
                exit = fadeOut(animationSpec = tween(220)) + shrinkVertically(animationSpec = tween(220))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HorizontalDivider(color = colors.dividerColor.copy(alpha = 0.3f))
                    TaskSection(
                        title = "待提交作业",
                        subtitle = "按截止时间排序",
                        accent = HomeworkAccent
                    ) {
                        HomeworkContent(homeworks = homeworks, moocViewModel = moocViewModel)
                    }
                    TaskSection(
                        title = "待测试",
                        subtitle = "展示未完成的课程测试",
                        accent = TestAccent
                    ) {
                        TestContent(tests = tests)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskSection(
    title: String,
    subtitle: String,
    accent: Color,
    content: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusPill(text = title, accent = accent, filled = true)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = subtitle,
                color = colors.secondaryTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        content()
    }
}

@Composable
private fun HomeworkContent(
    homeworks: ApiResponse<List<MoocHomework>>,
    moocViewModel: MoocViewModel
) {
    val colors = AppTheme.colors
    when (homeworks) {
        is ApiResponse.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.loadingColor,
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 2.5.dp
                )
            }
        }

        is ApiResponse.Error -> {
            SectionMessage(text = "作业加载失败：${homeworks.msg}", accent = colors.errorRedColor)
        }

        is ApiResponse.Success -> {
            if (homeworks.data.isEmpty()) {
                SectionMessage(text = "当前没有待提交作业", accent = colors.secondaryTextColor)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    homeworks.data.forEach { homework ->
                        HomeworkItem(homework = homework, moocViewModel = moocViewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun TestContent(tests: ApiResponse<List<MoocTest>>) {
    val colors = AppTheme.colors
    when (tests) {
        is ApiResponse.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.loadingColor,
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 2.5.dp
                )
            }
        }

        is ApiResponse.Error -> {
            SectionMessage(text = "测试加载失败：${tests.msg}", accent = colors.errorRedColor)
        }

        is ApiResponse.Success -> {
            val pendingTests = tests.data.filterNot { it.isSubmitted }
            if (pendingTests.isEmpty()) {
                SectionMessage(text = "当前没有待完成测试", accent = colors.secondaryTextColor)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pendingTests.forEach { test ->
                        TestItem(test = test)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionMessage(
    text: String,
    accent: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AppTheme.colors.bgSecondaryColor
    ) {
        Text(
            text = text,
            color = accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun HomeworkItem(
    homework: MoocHomework,
    moocViewModel: MoocViewModel
) {
    val colors = AppTheme.colors
    val isDueSoonMap by moocViewModel.isDueSoonMap.collectAsStateWithLifecycle()
    val isDueSoon = when (val res = isDueSoonMap[homework.title]) {
        is ApiResponse.Success -> res.data
        else -> false
    }
    val titleColor = if (isDueSoon) colorResource(R.color.color_base_red) else colors.primaryTextColor

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.bgSecondaryColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = homework.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                lineHeight = 21.sp
            )
            Text(
                text = "截止时间: ${homework.deadline}",
                fontSize = 12.sp,
                color = colors.secondaryTextColor,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(
                    text = if (homework.submitStatus) "已提交" else "未提交",
                    accent = if (homework.submitStatus) colorResource(R.color.color_base_green) else colorResource(R.color.color_base_red)
                )
                StatusPill(
                    text = if (homework.canSubmit) "可提交" else "不可提交",
                    accent = if (homework.canSubmit) colorResource(R.color.color_base_green) else colorResource(R.color.color_base_red)
                )
            }
            Text(
                text = "发布人: ${homework.publisher}",
                fontSize = 12.sp,
                color = colors.secondaryTextColor
            )
        }
    }
}

@Composable
private fun TestItem(test: MoocTest) {
    val colors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.bgSecondaryColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = test.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryTextColor,
                lineHeight = 21.sp
            )
            Text(
                text = "测试时间: ${test.startTime} - ${test.endTime}",
                fontSize = 12.sp,
                color = colors.secondaryTextColor,
                lineHeight = 18.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(text = "时长 ${test.timeLimit} 分钟", accent = TestAccent)
                StatusPill(
                    text = test.allowRetake?.let { "可重考 $it 次" } ?: "不限重考",
                    accent = HomeworkAccent,
                    filled = false
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    accent: Color,
    filled: Boolean = true
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (filled) accent.copy(alpha = 0.14f) else Color.Transparent,
        border = if (filled) null else BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            color = accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F3F6)
@Composable
private fun MoocScreenPreview() {
    AppSkinTheme {
        Column(
            modifier = Modifier
                .background(AppTheme.colors.overviewPageBackgroundColor)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                pendingCourses = ApiResponse.Success(
                    listOf(
                        PendingAssignmentCourse("10842", "数据库原理与技术"),
                        PendingAssignmentCourse("20491", "大数据平台开发")
                    )
                )
            )
            MessageCard(
                title = "待测试 UI",
                message = "课程展开后会显示待提交作业和待测试双区块。",
                accent = TestAccent
            )
        }
    }
}
