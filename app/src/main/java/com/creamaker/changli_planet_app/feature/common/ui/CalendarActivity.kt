package com.creamaker.changli_planet_app.feature.common.ui

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.creamaker.changli_planet_app.base.ComposeActivity
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarListItem
import com.creamaker.changli_planet_app.feature.calendar.ui.compose.SemesterCalendarDetailActivity
import com.creamaker.changli_planet_app.feature.calendar.viewmodel.SemesterCalendarListViewModel

/**
 * 校历列表页：展示 Go 服务端返回的所有可用学期，点击进入详情（Compose 全渲染）。
 *
 * 替换原先的 WebView 跳转方案（`https://api.csustplanet.zhelearn.com/static/school_calendar/...`）。
 */
class CalendarActivity : ComposeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setComposeContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AppTheme.colors.bgPrimaryColor
            ) {
                CalendarScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    viewModel: SemesterCalendarListViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val currentTerm = remember { com.creamaker.changli_planet_app.common.cache.CommonInfo.getCurrentTerm() }
    val pullState = rememberPullToRefreshState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ListTopBar(
            onBack = onBack,
            onRefresh = { viewModel.refresh() }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading && state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppTheme.colors.commonColor)
                    }
                }

                state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.errorMessage.ifBlank { "暂无校历数据" },
                            color = AppTheme.colors.greyTextColor,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    PullToRefreshBox(
                        state = pullState,
                        isRefreshing = state.isLoading,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item { HeaderTitle(totalCount = state.items.size) }

                            items(items = state.items, key = { it.semesterCode }) { item ->
                                SemesterListCard(
                                    item = item,
                                    isCurrent = item.semesterCode == currentTerm,
                                    onClick = {
                                        val intent = Intent(context, SemesterCalendarDetailActivity::class.java).apply {
                                            putExtra(SemesterCalendarDetailActivity.EXTRA_SEMESTER_CODE, item.semesterCode)
                                            putExtra(SemesterCalendarDetailActivity.EXTRA_TITLE, item.title)
                                            putExtra(SemesterCalendarDetailActivity.EXTRA_SUBTITLE, item.subtitle)
                                        }
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListTopBar(onBack: () -> Unit, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.bgPrimaryColor)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = AppTheme.colors.primaryTextColor
            )
        }
        Text(
            text = "学期校历",
            color = AppTheme.colors.primaryTextColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "刷新",
                tint = AppTheme.colors.primaryTextColor
            )
        }
    }
}

@Composable
private fun HeaderTitle(totalCount: Int) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "长沙理工大学",
            fontSize = 12.sp,
            color = AppTheme.colors.greyTextColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "校历列表",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.primaryTextColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "共 $totalCount 个学期 · 下拉可刷新",
            fontSize = 12.sp,
            color = AppTheme.colors.greyTextColor
        )
    }
}

@Composable
private fun SemesterListCard(
    item: SemesterCalendarListItem,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val accent = AppTheme.colors.campusSkyBlueColor
    val accentLight = AppTheme.colors.campusSkyBlueLightColor
    val bgColor = if (isCurrent) accentLight else AppTheme.colors.bgSecondaryColor
    val borderColor = if (isCurrent) accent.copy(alpha = 0.5f) else AppTheme.colors.campusDividerColor
    val borderWidth = if (isCurrent) 1.5.dp else 1.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SemesterAvatar(semesterCode = item.semesterCode, isCurrent = isCurrent)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title.ifBlank { "学期校历" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.primaryTextColor,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isCurrent) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CurrentBadge()
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle.ifBlank { item.semesterCode },
                fontSize = 12.sp,
                color = AppTheme.colors.greyTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "学期代码：${item.semesterCode}",
                fontSize = 11.sp,
                color = AppTheme.colors.greyTextColor
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.greyTextColor
        )
    }
}

@Composable
private fun SemesterAvatar(semesterCode: String, isCurrent: Boolean) {
    val accent = AppTheme.colors.campusSkyBlueColor
    val accentLight = AppTheme.colors.campusSkyBlueLightColor
    // 学期序号（末位数字 → "一" / "二"）
    val suffix = semesterCode.substringAfterLast('-').toIntOrNull()
    val chineseNumber = when (suffix) {
        1 -> "一"
        2 -> "二"
        else -> suffix?.toString() ?: "?"
    }
    // 学年前缀（如 "2025-2026" → "25-26"）
    val yearShort = runCatching {
        val parts = semesterCode.split('-')
        if (parts.size >= 2) "${parts[0].takeLast(2)}-${parts[1].takeLast(2)}" else ""
    }.getOrDefault("")

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (isCurrent) accent else accentLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = chineseNumber,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) Color.White else accent
            )
            if (yearShort.isNotBlank()) {
                Text(
                    text = yearShort,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isCurrent) {
                        Color.White.copy(alpha = 0.85f)
                    } else {
                        accent.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

@Composable
private fun CurrentBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(AppTheme.colors.commonColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "本学期",
            fontSize = 10.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
