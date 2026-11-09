package com.zhelearn.CSUSTPlanet.feature.physicslab.ui.compose

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabCatalogItem
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabIndex
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabScore
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabSelectableItem
import com.dcelysia.csust_spider.physicsexperiment.data.remote.dto.PhysicsLabTask
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.core.network.ApiResponse
import com.zhelearn.CSUSTPlanet.core.theme.AppSkinTheme
import com.zhelearn.CSUSTPlanet.core.theme.AppTheme
import com.zhelearn.CSUSTPlanet.feature.physicslab.viewmodel.PhysicsLabTab
import com.zhelearn.CSUSTPlanet.feature.physicslab.viewmodel.PhysicsLabUiState
import com.zhelearn.CSUSTPlanet.feature.physicslab.viewmodel.PhysicsLabViewModel

/**
 * 大学物理实验页：实验项目 / 我的课表 / 我的成绩 三个标签页。
 *
 * 平台密码与统一认证密码不一定相同，缺失或不对时由 ViewModel 弹输入框，只存本地。
 */
@Composable
fun PhysicsLabScreen(
    onBack: () -> Unit,
    onGoBinding: () -> Unit,
    initialTab: PhysicsLabTab? = null,
    viewModel: PhysicsLabViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // 从概览摘要条进来时直接落到"我的课表"
    LaunchedEffect(initialTab) {
        initialTab?.let(viewModel::selectTab)
    }

    if (state.showPlatformPasswordDialog) {
        PlatformPasswordDialog(
            errorText = state.platformPasswordError,
            onDismiss = viewModel::dismissPlatformPasswordDialog,
            onConfirm = viewModel::submitPlatformPassword
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        PhysicsLabTopBar(
            refreshing = state.refreshing,
            onBack = onBack,
            onRefresh = viewModel::refresh
        )

        PrimaryTabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = state.selectedTab.ordinal,
            containerColor = AppTheme.colors.bgPrimaryColor,
            contentColor = AppTheme.colors.primaryTextColor,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(state.selectedTab.ordinal),
                    color = AppTheme.colors.commonColor
                )
            },
            divider = {}
        ) {
            PhysicsLabTab.entries.forEach { tab ->
                val selected = state.selectedTab == tab
                Tab(
                    selected = selected,
                    onClick = { viewModel.selectTab(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) {
                                AppTheme.colors.commonColor
                            } else {
                                AppTheme.colors.primaryTextColor
                            }
                        )
                    }
                )
            }
        }

        state.cacheHints[state.selectedTab]?.let { hint ->
            CacheHint(text = hint)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (state.selectedTab) {
                PhysicsLabTab.Catalog -> PhysicsLabTabContent(
                    response = state.catalog,
                    state = state,
                    onRetry = viewModel::refresh,
                    onInputPassword = viewModel::showPlatformPasswordDialog,
                    onGoBinding = onGoBinding
                ) {
                    CatalogContent(
                        index = it,
                        tasks = tasksOrEmpty(state.tasks),
                        selectableCourses = state.selectableCourses,
                        selectableQuota = state.selectableQuota,
                        selectableFailed = state.selectableFailed,
                        selectableLoading = state.selectableLoading
                    )
                }

                PhysicsLabTab.Tasks -> PhysicsLabTabContent(
                    response = state.tasks,
                    state = state,
                    onRetry = viewModel::refresh,
                    onInputPassword = viewModel::showPlatformPasswordDialog,
                    onGoBinding = onGoBinding
                ) { TasksContent(it) }

                PhysicsLabTab.Scores -> PhysicsLabTabContent(
                    response = state.scores,
                    state = state,
                    onRetry = viewModel::refresh,
                    onInputPassword = viewModel::showPlatformPasswordDialog,
                    onGoBinding = onGoBinding
                ) { ScoresContent(it) }
            }
        }
    }
}

@Composable
private fun PhysicsLabTopBar(
    refreshing: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "大学物理实验",
                color = AppTheme.colors.primaryTextColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = "物理实验教学管理系统",
                color = AppTheme.colors.greyTextColor,
                fontSize = 12.sp
            )
        }
        if (refreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AppTheme.colors.commonColor,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
        } else {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "刷新",
                    tint = AppTheme.colors.primaryTextColor
                )
            }
        }
    }
}

@Composable
private fun CacheHint(text: String) {
    Text(
        text = text,
        color = AppTheme.colors.commonColor,
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.commonColor.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

/**
 * 标签页三态渲染：加载中 / 失败（按错误类型给不同按钮）/ 内容。
 *
 * 失败时按会话层状态选动作：统一认证挂了只能重新绑定学号，缺平台密码则弹输入框。
 */
@Composable
private fun <T> PhysicsLabTabContent(
    response: ApiResponse<T>,
    state: PhysicsLabUiState,
    onRetry: () -> Unit,
    onInputPassword: () -> Unit,
    onGoBinding: () -> Unit,
    content: @Composable (T) -> Unit
) {
    when (response) {
        is ApiResponse.Loading -> CenterLoading()

        is ApiResponse.Error -> LoadFailedTip(
            message = response.msg,
            needsPlatformPassword = state.needsPlatformPassword,
            needsRebind = state.needsRebind,
            onRetry = onRetry,
            onInputPassword = onInputPassword,
            onGoBinding = onGoBinding
        )

        is ApiResponse.Success -> content(response.data)
    }
}

@Composable
private fun CenterLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.colors.commonColor)
    }
}

@Composable
private fun EmptyTip(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = AppTheme.colors.greyTextColor, fontSize = 14.sp)
    }
}

@Composable
private fun LoadFailedTip(
    message: String,
    needsPlatformPassword: Boolean,
    needsRebind: Boolean,
    onRetry: () -> Unit,
    onInputPassword: () -> Unit,
    onGoBinding: () -> Unit
) {
    val action = when {
        needsRebind -> "重新绑定学号" to onGoBinding
        needsPlatformPassword -> "输入平台密码" to onInputPassword
        else -> "重试" to onRetry
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message.ifBlank { "加载失败" },
            color = AppTheme.colors.greyTextColor,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { action.second() },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.commonColor,
                contentColor = AppTheme.colors.textButtonColor
            )
        ) {
            Text(text = action.first, fontSize = 14.sp)
        }
    }
}

// ----------------------------------------------------------------------
// 实验项目
// ----------------------------------------------------------------------

/** 校区推断要用"我的实验"，它可能还没加载出来 —— 那时按空处理，退化为不做校区过滤。 */
private fun tasksOrEmpty(response: ApiResponse<List<PhysicsLabTask>>): List<PhysicsLabTask> =
    when (response) {
        is ApiResponse.Success -> response.data
        else -> emptyList()
    }

@Composable
private fun CatalogContent(
    index: PhysicsLabIndex,
    tasks: List<PhysicsLabTask>,
    selectableCourses: List<PhysicsLabSelectableItem>,
    selectableQuota: String?,
    selectableFailed: Boolean,
    selectableLoading: Boolean
) {
    if (index.catalog.isEmpty()) {
        EmptyTip("暂无实验项目")
        return
    }
    // 目录里也可能带"已选"项，统一以 index.selected 为准，按 courseId 去重后再减掉
    val selectedIds = index.selected.map { it.courseId }.toSet()
    val unselected = index.catalog
        .distinctBy { it.courseId }
        .filterNot { it.courseId in selectedIds }

    val campus = inferCampus(tasks, index.selected)
    val campusUnselected = unselected.filter { campus == null || matchesCampus(it.campus, campus) }
    var unselectedExpanded by remember { mutableStateOf(false) }
    // 这组有二十来行，默认收起：进页面先看到的是"我选了什么"，不是可选池
    var selectableExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { SummaryBar("我的实验 ${index.selected.size} 项 · 项目目录共 ${index.catalog.size} 项") }

        item { SectionTitle("我已选的实验", trailing = "${index.selected.size} 项", emphasized = true) }
        if (index.selected.isEmpty()) {
            item { SectionTip("还没有已选实验") }
        } else {
            items(index.selected) { CatalogRow(it, showSelectedTag = true) }
        }

        item {
            SelectableSectionHeader(
                expanded = selectableExpanded,
                count = selectableCourses.size,
                quotaText = selectableQuota,
                loading = selectableLoading,
                onToggle = { selectableExpanded = !selectableExpanded }
            )
        }
        if (selectableExpanded) {
            // 加载失败与"确实没有"要分开说，别把网络问题说成没有项目
            when {
                selectableLoading -> item { SectionTip("加载中…") }
                selectableFailed -> item { SectionTip("本学期可选列表加载失败") }
                selectableCourses.isEmpty() -> item { SectionTip("本学期没有可选项目") }
                else -> items(selectableCourses) { SelectableRow(it) }
            }
        }

        item {
            CollapsibleSectionHeader(
                title = "其它实验项目",
                trailing = "${campusUnselected.size} 项",
                expanded = unselectedExpanded,
                onToggle = { unselectedExpanded = !unselectedExpanded }
            )
        }
        if (unselectedExpanded) {
            // 这里是项目的完整目录（含其它校区、其它学期），不代表本学期可选范围
            item {
                SectionTip(
                    if (campusUnselected.isEmpty()) "本校区没有其它项目"
                    else "完整项目目录，含其它校区与学期"
                )
            }
            items(campusUnselected) { CatalogRow(it, showSelectedTag = false) }
        }
    }
}

/**
 * 推断学生所在校区：先用"我的实验"里的上课地点（出现次数最多），再用已选项目的校区。
 * 两个来源都推断不出就返回 null，调用方据此不过滤 —— 不猜。
 */
private fun inferCampus(
    tasks: List<PhysicsLabTask>,
    selected: List<PhysicsLabCatalogItem>
): String? {
    val fromTasks = tasks.mapNotNull { campusKeyOf(it.location) }
    val fromSelected = selected.mapNotNull { campusKeyOf(it.campus) }
    return fromTasks.ifEmpty { fromSelected }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
}

/** 取到"校区"二字为止：`金盆岭校区三号教学楼402` → `金盆岭校区`；认不出返回 null。 */
private fun campusKeyOf(text: String): String? {
    val index = text.indexOf("校区")
    return if (index >= 0) text.substring(0, index + 2) else null
}

/** 校区未知的项目不参与过滤：宁可多显示一项，也不要让项目看不见。 */
private fun matchesCampus(itemCampus: String, campus: String): Boolean =
    campusKeyOf(itemCampus)?.let { it == campus } ?: true

@Composable
private fun CatalogRow(item: PhysicsLabCatalogItem, showSelectedTag: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.bgCardColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = AppTheme.colors.primaryTextColor,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.campus.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = item.campus, color = AppTheme.colors.greyTextColor, fontSize = 12.sp)
            }
        }
        if (showSelectedTag) {
            SelectedTag()
        }
    }
}

/** 平台「操作」列只有这三种原文，按字面用即可，别推别的含义。 */
private const val ACTION_SELECTABLE = "选取"
private const val ACTION_SELECTED = "退选"
private const val ACTION_UNAVAILABLE = "--"

/** 「本学期可选」折叠头：配额要常驻可见，所以放在副标题而不是展开后的内容里。 */
@Composable
private fun SelectableSectionHeader(
    expanded: Boolean,
    count: Int,
    quotaText: String?,
    loading: Boolean,
    onToggle: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "physicsLabSelectableArrow"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "本学期可选",
                color = AppTheme.colors.primaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                // 这个数字是"本学期开设的全部项目"，含不可操作项；写"开设"而不是"N 项可选"，避免和配额里的应选/已选混淆
                text = if (loading) "加载中…" else "开设 $count 项",
                color = AppTheme.colors.secondaryTextColor,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (expanded) "收起" else "展开",
                color = AppTheme.colors.commonColor,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_expand),
                contentDescription = if (expanded) "收起" else "展开",
                tint = AppTheme.colors.iconSecondaryColor,
                modifier = Modifier
                    .rotate(rotation)
                    .size(18.dp)
            )
        }
        if (!loading && !quotaText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = quotaText, color = AppTheme.colors.greyTextColor, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SelectableRow(item: PhysicsLabSelectableItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.bgCardColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = AppTheme.colors.primaryTextColor,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.hours.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.hours} 课时",
                    color = AppTheme.colors.greyTextColor,
                    fontSize = 12.sp
                )
            }
        }
        when (item.action) {
            ACTION_SELECTABLE -> ActionTag("可选", AppTheme.colors.commonColor)
            ACTION_SELECTED -> SelectedTag()
            ACTION_UNAVAILABLE -> ActionTag("不可操作", AppTheme.colors.secondaryTextColor)
            // 平台改了文案就原样显示，别吞掉也别崩
            else -> if (item.action.isNotBlank()) {
                Text(text = item.action, color = AppTheme.colors.secondaryTextColor, fontSize = 11.sp)
            }
        }
    }
}

/** 与 [SelectedTag] 同一套样式，只换文字与颜色。 */
@Composable
private fun ActionTag(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** 收起状态的未选组：标题弱化，但数量与展开/收起提示要明确，避免被当成没数据。 */
@Composable
private fun CollapsibleSectionHeader(
    title: String,
    trailing: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "physicsLabSectionArrow"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = AppTheme.colors.secondaryTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = trailing, color = AppTheme.colors.secondaryTextColor, fontSize = 12.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = if (expanded) "收起" else "展开",
            color = AppTheme.colors.commonColor,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_expand),
            contentDescription = if (expanded) "收起" else "展开",
            tint = AppTheme.colors.iconSecondaryColor,
            modifier = Modifier
                .rotate(rotation)
                .size(18.dp)
        )
    }
}

/** 分组内的空态提示（不能用 fillMaxSize 的 EmptyTip，它会撑破列表项）。 */
@Composable
private fun SectionTip(text: String) {
    Text(
        text = text,
        color = AppTheme.colors.greyTextColor,
        fontSize = 13.sp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
    )
}

@Composable
private fun SelectedTag() {
    Text(
        text = "已选",
        color = AppTheme.colors.commonColor,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AppTheme.colors.commonColor.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

// ----------------------------------------------------------------------
// 我的课表 / 我的成绩
// ----------------------------------------------------------------------

@Composable
private fun TasksContent(tasks: List<PhysicsLabTask>) {
    if (tasks.isEmpty()) {
        EmptyTip("暂无实验安排")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { SummaryBar("共 ${tasks.size} 条实验安排") }
        items(tasks) { TaskCard(it) }
    }
}

@Composable
private fun TaskCard(task: PhysicsLabTask) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.bgCardColor)
            .padding(12.dp)
    ) {
        Text(
            text = task.courseName,
            color = AppTheme.colors.primaryTextColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        InfoLine("时间", task.time)
        InfoLine("地点", task.location)
        InfoLine("教师", task.teacher)
        InfoLine("周次", task.weekday)
        InfoLine("批次", task.batch)
        InfoLine("课时", task.hours)
    }
}

@Composable
private fun ScoresContent(scores: List<PhysicsLabScore>) {
    if (scores.isEmpty()) {
        EmptyTip("暂无实验成绩")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { SummaryBar("共 ${scores.size} 条实验成绩") }
        items(scores) { ScoreCard(it) }
    }
}

@Composable
private fun ScoreCard(score: PhysicsLabScore) {
    // 未出分时平台给的是 "--"
    val published = score.totalScore.isNotBlank() && score.totalScore != "--"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.bgCardColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = score.projectName,
                color = AppTheme.colors.primaryTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = score.courseName, color = AppTheme.colors.greyTextColor, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "预习 ${score.previewScore} · 操作 ${score.operationScore} · 报告 ${score.reportScore}",
                color = AppTheme.colors.secondaryTextColor,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (published) score.totalScore else "未出分",
            color = if (published) AppTheme.colors.commonColor else AppTheme.colors.greyTextColor,
            fontSize = if (published) 22.sp else 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SummaryBar(text: String) {
    Text(
        text = text,
        color = AppTheme.colors.secondaryTextColor,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SectionTitle(text: String, trailing: String = "", emphasized: Boolean = false) {
    Row(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = AppTheme.colors.primaryTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (trailing.isNotBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = trailing,
                color = if (emphasized) {
                    AppTheme.colors.commonColor
                } else {
                    AppTheme.colors.secondaryTextColor
                },
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = AppTheme.colors.greyTextColor,
            fontSize = 12.sp,
            modifier = Modifier.width(40.dp)
        )
        Text(text = value, color = AppTheme.colors.secondaryTextColor, fontSize = 12.sp)
    }
}

// ----------------------------------------------------------------------
// 平台密码
// ----------------------------------------------------------------------

@Composable
private fun PlatformPasswordDialog(
    errorText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password) },
                enabled = password.isNotBlank()
            ) {
                Text(text = "确定", color = AppTheme.colors.commonColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消", color = AppTheme.colors.secondaryTextColor)
            }
        },
        title = {
            Text(
                text = "物理实验平台密码",
                color = AppTheme.colors.primaryTextColor,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "平台密码可能与统一认证密码不同，只需填写一次，之后自动登录。",
                    color = AppTheme.colors.secondaryTextColor,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = "平台密码") },
                    visualTransformation = if (visible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (password.isNotBlank()) onConfirm(password) }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                imageVector = if (visible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = if (visible) "隐藏密码" else "显示密码",
                                tint = AppTheme.colors.greyTextColor
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.colors.commonColor,
                        unfocusedBorderColor = AppTheme.colors.campusDividerColor,
                        cursorColor = AppTheme.colors.commonColor
                    )
                )
                if (errorText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText,
                        color = AppTheme.colors.errorRedColor,
                        fontSize = 12.sp
                    )
                }
            }
        },
        containerColor = AppTheme.colors.bgCardColor
    )
}

@Preview(showBackground = true, name = "本学期可选")
@Composable
private fun SelectableSectionPreview() {
    AppSkinTheme {
        Column(
            modifier = Modifier
                .background(AppTheme.colors.bgPrimaryColor)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelectableSectionHeader(
                expanded = true,
                count = 4,
                quotaText = "应选：20项目(64课时)， 已选：19项目(63课时)， 还需要选择1项目(1课时)",
                loading = false,
                onToggle = {}
            )
            // 三种平台状态各一行，最后一行演示未知文案的原样显示
            SelectableRow(PhysicsLabSelectableItem("14", "光速的测量（金盆岭校区）", "3", "选取"))
            SelectableRow(
                PhysicsLabSelectableItem("4", "PN结正向压降与温度特性的研究(金盆岭校区）", "3", "退选")
            )
            SelectableRow(PhysicsLabSelectableItem("84", "虚拟仿真实验项目1(金盆岭校区）", "6", "--"))
            SelectableRow(PhysicsLabSelectableItem("86", "平台新增状态的实验", "3", "待开放"))
        }
    }
}
