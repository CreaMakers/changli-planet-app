package com.zhelearn.CSUSTPlanet.overview.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.core.theme.AppSkinTheme
import com.zhelearn.CSUSTPlanet.core.theme.AppTheme
import com.zhelearn.CSUSTPlanet.overview.data.OverviewRepository
import com.zhelearn.CSUSTPlanet.overview.ui.model.OverviewAnnouncementUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnnouncementScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember(context) { OverviewRepository(context) }
    var announcements by remember { mutableStateOf(emptyList<OverviewAnnouncementUiModel>()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(repository, lifecycleOwner) {
        announcements = repository.loadCachedAnnouncements()
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            announcements = repository.refreshAnnouncements()
            isLoading = false
        }
    }

    AnnouncementList(announcements = announcements, isLoading = isLoading)
}

@Composable
private fun AnnouncementList(
    announcements: List<OverviewAnnouncementUiModel>,
    isLoading: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("yyyy年M月d日 · HH:mm", Locale.getDefault()) }
    val colors = AppTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overviewPageBackgroundColor)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.intel_station),
                    color = colors.primaryTextColor,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
        if (announcements.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = colors.bgCardColor
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = colors.overviewPromptTextColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = colors.overviewPromptBackgroundColor
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.nnews),
                                    contentDescription = null,
                                    tint = colors.overviewPromptTextColor,
                                    modifier = Modifier.padding(16.dp).size(28.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isLoading) "正在加载公告" else "暂无公告",
                            color = colors.primaryTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!isLoading) {
                            Text(
                                text = "新的通知会在这里与你见面",
                                color = colors.secondaryTextColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
        itemsIndexed(announcements, key = { _, announcement -> announcement.id }) { index, announcement ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = colors.bgCardColor
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = announcement.createdAtMillis?.let { dateFormat.format(Date(it)) } ?: "公告",
                            color = colors.secondaryTextColor,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (index == 0 && announcement.createdAtMillis != null) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = colors.overviewPromptBackgroundColor
                            ) {
                                Text(
                                    text = "最新",
                                    color = colors.overviewPromptTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = announcement.title,
                        color = colors.primaryTextColor,
                        fontSize = 18.sp,
                        lineHeight = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (announcement.content.isNotBlank()) {
                        Text(
                            text = announcement.content,
                            color = colors.secondaryTextColor,
                            fontSize = 14.sp,
                            lineHeight = 23.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnnouncementListPreview() {
    AppSkinTheme {
        AnnouncementList(
            announcements = listOf(
                OverviewAnnouncementUiModel(
                    id = "1",
                    title = "公告现在有了自己的小天地",
                    content = "校园通知、功能更新和重要消息，都可以在这里查看。感谢你一直以来的陪伴与支持。",
                    createdAtMillis = 1788650400000L
                ),
                OverviewAnnouncementUiModel(
                    id = "2",
                    title = "新学期，记得更新你的课表",
                    content = "前往课程表同步新学期课程，让每天的安排一目了然。",
                    createdAtMillis = 1788564000000L
                )
            ),
            isLoading = false
        )
    }
}
