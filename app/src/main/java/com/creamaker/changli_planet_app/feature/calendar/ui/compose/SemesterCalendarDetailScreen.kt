package com.creamaker.changli_planet_app.feature.calendar.ui.compose

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.creamaker.changli_planet_app.base.ComposeActivity
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarDetail
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarNote
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarWeekRange
import com.creamaker.changli_planet_app.feature.calendar.viewmodel.SemesterCalendarDetailViewModel
import kotlinx.coroutines.launch

/**
 * 校历详情页 Activity（Compose 壳）。
 *
 * 入参（Intent extras）：
 *  - [EXTRA_SEMESTER_CODE] 必填，如 `"2025-2026-2"`
 *  - [EXTRA_TITLE] / [EXTRA_SUBTITLE] 选填，网络未就绪时作为标题兜底
 *
 * 兼容性说明：所有日期运算使用 [java.util.Calendar]，**不依赖 java.time.\***，
 * 以适配项目 `minSdk = 24`（未开启 core library desugaring）。
 */
class SemesterCalendarDetailActivity : ComposeActivity() {

    companion object {
        const val EXTRA_SEMESTER_CODE = "semester_code"
        const val EXTRA_TITLE = "semester_title"
        const val EXTRA_SUBTITLE = "semester_subtitle"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val semesterCode = intent.getStringExtra(EXTRA_SEMESTER_CODE).orEmpty()
        val fallbackTitle = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val fallbackSubtitle = intent.getStringExtra(EXTRA_SUBTITLE).orEmpty()
        setComposeContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AppTheme.colors.bgPrimaryColor
            ) {
                SemesterCalendarDetailScreen(
                    semesterCode = semesterCode,
                    fallbackTitle = fallbackTitle,
                    fallbackSubtitle = fallbackSubtitle,
                    onBack = { finish() }
                )
            }
        }
    }
}

/**
 * 详情页主 Screen。负责：Top Bar、三态渲染（加载/错误/内容）、跳到今天 FAB。
 */
@Composable
fun SemesterCalendarDetailScreen(
    semesterCode: String,
    fallbackTitle: String = "",
    fallbackSubtitle: String = "",
    onBack: () -> Unit,
    viewModel: SemesterCalendarDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(semesterCode) { viewModel.load(semesterCode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        DetailTopBar(
            title = state.detail?.title?.takeIf { it.isNotBlank() }
                ?: fallbackTitle.ifBlank { "学期校历" },
            subtitle = state.detail?.subtitle?.takeIf { it.isNotBlank() } ?: fallbackSubtitle,
            onBack = onBack,
            onRefresh = viewModel::refresh
        )

        Box(modifier = Modifier.fillMaxSize()) {
            val detail = state.detail
            when {
                state.isLoading && detail == null -> CenterLoading()
                detail == null -> CenterTip(state.errorMessage.ifBlank { "暂无校历数据" })
                else -> CalendarDetailContent(detail = detail)
            }
        }
    }
}

@Composable
private fun CenterLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.colors.commonColor)
    }
}

@Composable
private fun CenterTip(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, color = AppTheme.colors.greyTextColor, fontSize = 14.sp)
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    subtitle: String,
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
                text = title,
                color = AppTheme.colors.primaryTextColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    color = AppTheme.colors.greyTextColor,
                    fontSize = 12.sp
                )
            }
        }
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "刷新",
                tint = AppTheme.colors.primaryTextColor
            )
        }
    }
}

/**
 * 校历主体内容（表格风格）：
 *  - 顶部学期概览紧凑条
 *  - 表格（表头 + N 行 + 蓝色教学周 overlay，整体是 LazyColumn 的 1 个 item）
 *  - 浮动按钮：学期内时显示"第 X 周"；学期外则显示"回到开学周"
 *
 * 进入页面自动滚到当前教学周（若不在学期范围内则滚到开学周作为兜底）。
 */
@Composable
private fun CalendarDetailContent(detail: SemesterCalendarDetail) {
    val weeks = remember(detail) { buildWeekRows(detail) }
    val noteMap = remember(detail) { groupNotes(detail.notes) }
    val rangeByRow = remember(detail) { flattenRanges(detail.customWeekRanges) }
    val semesterStartMs = remember(detail) { parseIsoDateMs(detail.semesterStart) }
    val semesterEndMs = remember(detail) { parseIsoDateMs(detail.semesterEnd) }
    val todayMs = remember { todayAtMidnightMs() }

    // 月份合并列：每个 row 显示的月份文字（与上一行相同则为 null）
    val monthLabels = remember(weeks) { buildMonthLabels(weeks) }

    // needNumber=true 的 notes 按 row 升序编号（排序后保证屏幕从上到下递增）
    val noteOrdinals = remember(detail.notes) { buildNoteOrdinals(detail.notes) }

    // 教学周序号：开学那周 = 1，之后 2、3...（开学前的周不在 map 中）
    val teachingWeekByRow = remember(weeks, semesterStartMs) {
        buildTeachingWeekIndex(weeks, semesterStartMs)
    }

    // 找到"今天"和"开学日"分别在 weeks 中的下标（0-based），用于滚动定位
    val todayWeekIdx = remember(weeks, todayMs) {
        weeks.indexOfFirst { row ->
            row.startMs != null && row.endMs != null && todayMs in row.startMs..row.endMs
        }
    }
    val semesterStartWeekIdx = remember(weeks, semesterStartMs) {
        if (semesterStartMs == null) -1 else weeks.indexOfFirst { row ->
            row.startMs != null && row.endMs != null && semesterStartMs in row.startMs..row.endMs
        }
    }

    // FAB 目标：优先"今天"，其次"开学周"
    val fabTargetIdx = when {
        todayWeekIdx >= 0 -> todayWeekIdx
        semesterStartWeekIdx >= 0 -> semesterStartWeekIdx
        else -> -1
    }
    val fabLabel = when {
        todayWeekIdx >= 0 -> {
            val tw = teachingWeekByRow[weeks[todayWeekIdx].row]
            if (tw != null) "第${tw}周" else "今天"
        }
        semesterStartWeekIdx >= 0 -> "开学周"
        else -> ""
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 滚到目标周需要在 LazyColumn 的 item 1（= 表格）内部再下滚的像素数
    // = 表头高度 + 目标周在 weeks 中的下标 × 行高
    val scrollOffsetPx = remember(fabTargetIdx, weeks, density) {
        if (fabTargetIdx < 0) 0 else with(density) {
            (HEADER_ROW_HEIGHT + TABLE_ROW_HEIGHT * fabTargetIdx).roundToPx()
        }
    }

    // 首帧自动滚到目标周
    LaunchedEffect(weeks, scrollOffsetPx) {
        if (fabTargetIdx >= 0) {
            listState.scrollToItem(index = 1, scrollOffset = scrollOffsetPx)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SemesterInfoCard(detail) }

            // 表格：整表宽度固定 TABLE_TOTAL_WIDTH，超屏由外层 horizontalScroll 承担
            item {
                CalendarTable(
                    weeks = weeks,
                    monthLabels = monthLabels,
                    teachingWeekByRow = teachingWeekByRow,
                    noteMap = noteMap,
                    rangeByRow = rangeByRow,
                    semesterStartMs = semesterStartMs,
                    semesterEndMs = semesterEndMs,
                    todayMs = todayMs,
                    currentRowIdx = todayWeekIdx,
                    noteOrdinals = noteOrdinals
                )
            }

            item { Spacer(modifier = Modifier.height(72.dp)) } // 给 FAB 留空间
        }

        if (fabTargetIdx >= 0 && fabLabel.isNotBlank()) {
            Button(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(index = 1, scrollOffset = scrollOffsetPx)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.campusSkyBlueColor,
                    contentColor = AppTheme.colors.textButtonColor
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Today,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = fabLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * 渲染完整表格：表头 + N 行 + 顶层蓝色折线。
 *
 * 表格整体宽度固定为 [TABLE_TOTAL_WIDTH]，超过屏幕宽时由外层 `horizontalScroll` 承担
 * "整张表格横滑"的体验（所有列的日期/备注随之一起滑动，视觉一致）。
 *
 * @param currentRowIdx 今天所在周的下标（0-based），用于整行高亮；`-1` 表示不在学期内
 */
@Composable
private fun CalendarTable(
    weeks: List<WeekRow>,
    monthLabels: Map<Int, String?>,
    teachingWeekByRow: Map<Int, Int>,
    noteMap: Map<Int, List<IndexedNote>>,
    rangeByRow: Map<Int, SemesterCalendarWeekRange>,
    semesterStartMs: Long?,
    semesterEndMs: Long?,
    todayMs: Long,
    currentRowIdx: Int,
    noteOrdinals: Map<Int, Int>
) {
    val horizontalScroll = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScroll)
            .background(AppTheme.colors.bgPrimaryColor)
    ) {
        // 底层：表头 + 所有周行
        Column(modifier = Modifier.width(TABLE_TOTAL_WIDTH)) {
            WeekdayHeaderRow()
            weeks.forEachIndexed { idx, row ->
                WeekRowCard(
                    row = row,
                    teachingWeekIndex = teachingWeekByRow[row.row],
                    monthLabel = monthLabels[row.row],
                    semesterStartMs = semesterStartMs,
                    semesterEndMs = semesterEndMs,
                    todayMs = todayMs,
                    isCurrentRow = idx == currentRowIdx,
                    notes = noteMap[row.row].orEmpty(),
                    range = rangeByRow[row.row],
                    noteOrdinals = noteOrdinals
                )
            }
        }

        // 上层：蓝色教学周折线（fillMaxSize 会撑到底层 Column 的宽高，坐标一致）
        TeachingPeriodOverlay(
            weeks = weeks,
            rangeByRow = rangeByRow,
            semesterStartMs = semesterStartMs,
            semesterEndMs = semesterEndMs,
            modifier = Modifier
                .width(TABLE_TOTAL_WIDTH)
                .height(HEADER_ROW_HEIGHT + TABLE_ROW_HEIGHT * weeks.size)
        )
    }
}

/**
 * 构建月份合并列：每周显示"其第一天所在的月"；与上一周同月则置 null（实现合并单元格效果）。
 */
private fun buildMonthLabels(weeks: List<WeekRow>): Map<Int, String?> {
    val result = HashMap<Int, String?>(weeks.size)
    var lastMonth = -1
    for (row in weeks) {
        val month = row.days.firstNotNullOfOrNull { it?.month } ?: continue
        result[row.row] = if (month != lastMonth) monthCn(month) else null
        lastMonth = month
    }
    return result
}

private val MONTH_CN_LABELS = arrayOf(
    "一月", "二月", "三月", "四月", "五月", "六月",
    "七月", "八月", "九月", "十月", "十一月", "十二月"
)

private fun monthCn(month: Int): String =
    MONTH_CN_LABELS.getOrNull(month - 1) ?: "$month 月"

/**
 * 为所有 `needNumber=true` 的 notes **按 `row` 升序** 分配 1-based 编号。
 *
 * 键是 note 在原 `notes` 列表中的全局 index，不用 `(row, content)` 拼接是为了避免
 * "同周同文"两条备注被当成同一条而丢失编号。
 *
 * 排序的理由：服务端返回的 `notes` 数组顺序无保证，按 row 升序编号后，
 * 屏幕上从上到下看到的圆圈数字才会是 ①②③... 递增。
 */
private fun buildNoteOrdinals(notes: List<SemesterCalendarNote>): Map<Int, Int> {
    val result = HashMap<Int, Int>()
    var ordinal = 0
    notes.withIndex()
        .filter { it.value.needNumber }
        .sortedBy { it.value.row }
        .forEach { (idx, _) ->
            ordinal += 1
            result[idx] = ordinal
        }
    return result
}
