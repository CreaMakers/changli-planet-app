package com.example.changli_planet_app.feature.mooc.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.changli_planet_app.R
import com.example.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.example.changli_planet_app.core.network.Resource
import com.example.changli_planet_app.feature.mooc.data.remote.dto.MoocHomework
import com.example.changli_planet_app.feature.mooc.data.remote.dto.PendingAssignmentCourse
import com.example.changli_planet_app.feature.mooc.viewmodel.MoocViewModel
import kotlin.collections.get


// 辅助函数：将字符串日期转换为Date对象


// 按截止时间排序作业列表


// 检查作业是否在一天内截止


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoocScreen(
    moocViewModel: MoocViewModel = viewModel()
) {
    val pendingCourses by moocViewModel.pendingCourse.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        moocViewModel.loginAndFetchCourses(
            StudentInfoManager.studentId,
            StudentInfoManager.studentPassword
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.pending_homework),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                ),
                modifier = Modifier
                    .background(Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when (pendingCourses) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(R.color.color_2878F3)
                        )
                    }
                }

                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "加载失败，${(pendingCourses as Resource.Error<List<PendingAssignmentCourse>>).msg}",
                            color = Color.Black,
                            fontSize = 15.sp,
                        )
                    }
                }

                is Resource.Success -> {
                    val courses = (pendingCourses as Resource.Success).data
                    if (courses.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_pending_assignments),
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(courses) { course ->
                                CourseItem(course = course, moocViewModel = moocViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun CourseItem(course: PendingAssignmentCourse, moocViewModel: MoocViewModel) {
    val isExpanded by moocViewModel.expandedCourseIds.collectAsStateWithLifecycle()
    val expanded = isExpanded.contains(course.id)
    val pendingHomeworksByCourse by moocViewModel.pendingHomeworksByCourse.collectAsStateWithLifecycle()
    val homeworks = pendingHomeworksByCourse[course.id] ?: Resource.Loading()
    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotationAngle")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(72.dp)
            .clickable {
                moocViewModel.handleCourseClick(course.id)
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = course.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "ID: ${course.id}", fontSize = 12.sp, color = Color.Gray)
            }

            androidx.compose.material3.Icon(
                painter = painterResource(id = R.drawable.ic_expand),
                contentDescription = if (expanded) "收起" else "展开",
                tint = Color.Gray,
                modifier = Modifier
                    .rotate(rotationAngle)
                    .size(24.dp) // 调整图标大小
            )
        }
    }

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp)
            .background(colorResource(R.color.divider_color_grey))
    )

    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)) + expandVertically(
            animationSpec = tween(durationMillis = 300),
            initialHeight = { 0 }
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 300)) + shrinkVertically(
            animationSpec = tween(durationMillis = 300),
            targetHeight = { 0 }
        )
    ) {
        when (homeworks) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.color_2878F3))
                }
            }

            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "作业加载失败", color = Color.Red, fontSize = 14.sp)
                }
            }

            is Resource.Success -> {
                val homeworkList = (homeworks as Resource.Success).data
                if (homeworkList.isNotEmpty()) {
                    // 统一左右内边距为 16.dp，使对齐一致
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        homeworkList.forEach { homework ->
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                HomeworkItem(homework = homework,moocViewModel = moocViewModel)
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = "暂无作业", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeworkItem(homework: MoocHomework, moocViewModel: MoocViewModel) {
    val isDueSoonMap by moocViewModel.isDueSoonMap.collectAsStateWithLifecycle()
    val key = homework.title
    val isDueSoon = when (val res = isDueSoonMap[key]) {
        is Resource.Success -> res.data
        else -> false
    }
    val titleColor = if (isDueSoon) Color(0xFFFF5252) else Color.Black // 使用亮红色 #FF5252

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.background_grey)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = homework.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "发布人: ${homework.publisher}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "截止时间: ${homework.deadline}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (homework.submitStatus) "已提交" else "未提交",
                    fontSize = 12.sp,
                    color = if (homework.submitStatus) Color.Green else Color.Red
                )
                Text(
                    text = if (homework.canSubmit) "可提交" else "不可提交",
                    fontSize = 12.sp,
                    color = if (homework.canSubmit) Color.Green else Color.Red
                )
            }
        }
    }
}