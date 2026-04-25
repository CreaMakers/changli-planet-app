package com.creamaker.changli_planet_app.feature.calendar.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarDetail
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarNote
import com.creamaker.changli_planet_app.feature.calendar.data.remote.dto.SemesterCalendarWeekRange
import java.util.Calendar

/**
 * 校历表格布局相关的 Composable（参考 Excel 表格 + 蓝色教学周路径）。
 *
 *  - [SemesterInfoCard]       顶部"学期概览"紧凑条
 *  - [WeekdayHeaderRow]       表头：周 | 月 | 一二三四五六日 | 备注
 *  - [WeekRowCard]            单周一行：细描边表格行
 *  - [DayCell]                单个日期单元
 *  - [TeachingPeriodOverlay]  蓝色折线路径（开学→结课穿过所有教学周）
 *
 * 设计目标：
 *  - 表格所有列宽固定（不使用 weight），整表是一个定宽 `TABLE_TOTAL_WIDTH`；
 *    超出屏幕时由外层 `horizontalScroll` 承担"整张表格横滑"体验
 *  - 月份列用主色 + 淡底块强调，与周次列形成明显视觉区分
 *  - 周次列显示**教学周序号**（从开学那周起 1,2,3...），开学前的周显示"准备周/假期"琥珀色标签
 *  - 假期 / 准备周整行使用淡黄色底，Light/Dark 两种主题都清晰可见
 *  - 仅周日数字用 `errorRedColor`（符合中国日历习惯）
 *  - 今天、开学日、结课日分别用"实心圆点 / 空心圆圈"标识
 *  - 所有颜色取自 [AppTheme.colors] token，自动 Dark Mode 适配
 */

// ---------------- 尺寸常量 ----------------

/** 周次列宽（左起第 1 列，需容纳"准备周"3 字标签）。 */
internal val WEEK_INDEX_WIDTH: Dp = 48.dp

/** 月份列宽（左起第 2 列，跨多行合并；需容纳"十一月/十二月"3 个汉字）。 */
internal val MONTH_COL_WIDTH: Dp = 44.dp

/** 单个日期单元宽度（日期圆圈 24dp + 左右呼吸）。 */
internal val DAY_COL_WIDTH: Dp = 40.dp

/** 备注列宽（固定足够放下完整的一条备注文字 + 圆圈编号）。 */
internal val NOTE_COL_WIDTH: Dp = 200.dp

/** 整表格总宽 = 周次 + 月份 + 7×日期 + 备注。超出屏幕宽时由外层横向滚动承担。 */
internal val TABLE_TOTAL_WIDTH: Dp =
    WEEK_INDEX_WIDTH + MONTH_COL_WIDTH + DAY_COL_WIDTH * 7 + NOTE_COL_WIDTH

/** 表格行高（整行对齐，便于蓝色路径贯穿）。 */
internal val TABLE_ROW_HEIGHT: Dp = 44.dp

/** 表头行高（暴露给 Screen 侧保证与 overlay 坐标一致）。 */
internal val HEADER_ROW_HEIGHT: Dp = 32.dp

/** 表格网格线宽度。 */
private val GRID_BORDER: Dp = 0.5.dp

/** 开学/结课/今天 标记圆圈尺寸。 */
private val DAY_MARK_SIZE: Dp = 26.dp

/** 备注圆圈数字尺寸。 */
private val NOTE_CIRCLE_SIZE: Dp = 14.dp

/** 开学/结课 空心圆圈描边宽度。 */
private val RING_STROKE: Dp = 1.2.dp

/** 教学周蓝色折线描边宽度。 */
private val TEACHING_PATH_STROKE: Dp = 1.6.dp

/** 假期 / 准备周整行背景 alpha。
 *  - Light（`#F0A855` 琥珀）× 0.20 叠白 ≈ `#FBEBD8`，淡黄清楚
 *  - Dark（`#F0B86A` 暖黄）× 0.20 叠黑 ≈ `#302415`，深棕但可识别；再配合周次列 `campusAmberColor` 文字即可明确辨识
 */
private const val HOLIDAY_ROW_ALPHA: Float = 0.20f

// ---------------- 顶部学期概览 ----------------

/**
 * 顶部"学期概览"紧凑条。
 * 参考图：图标 + "学期概览" 标题 + 下方两行 "学期：xxx" / "周期：yyyy-MM-dd 至 zzzz-MM-dd"。
 */
@Composable
internal fun SemesterInfoCard(detail: SemesterCalendarDetail) {
    val accent = AppTheme.colors.campusSkyBlueColor

    val startText = remember(detail.semesterStart) { formatDisplayShort(detail.semesterStart) }
    val endText = remember(detail.semesterEnd) { formatDisplayShort(detail.semesterEnd) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.colors.bgSecondaryColor)
            .border(1.dp, AppTheme.colors.campusDividerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "学期概览",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.primaryTextColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "学期：${detail.title.ifBlank { detail.semesterCode }}",
            fontSize = 12.sp,
            color = AppTheme.colors.greyTextColor
        )
        Text(
            text = "周期：$startText  至  $endText",
            fontSize = 12.sp,
            color = AppTheme.colors.greyTextColor
        )
    }
}

// ---------------- 表头 ----------------

/**
 * 表格表头：`周 | 月 | 日 | 一 | 二 | 三 | 四 | 五 | 六 | 备注`。
 *
 * 所有列宽固定（见常量），整体宽度 [TABLE_TOTAL_WIDTH]，与每行对齐。
 * 采用周日→周六的顺序（与服务端 row 计数一致），周日列标签使用 `errorRedColor`。
 */
@Composable
internal fun WeekdayHeaderRow() {
    val dividerColor = AppTheme.colors.campusDividerColor
    val headerBg = AppTheme.colors.bgSecondaryColor
    val labelColor = AppTheme.colors.greyTextColor
    val sundayColor = AppTheme.colors.errorRedColor

    Row(
        modifier = Modifier
            .width(TABLE_TOTAL_WIDTH)
            .height(HEADER_ROW_HEIGHT)
            .background(headerBg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell(text = "周", width = WEEK_INDEX_WIDTH, color = labelColor, dividerColor = dividerColor)
        HeaderCell(text = "月", width = MONTH_COL_WIDTH, color = labelColor, dividerColor = dividerColor)
        listOf("日", "一", "二", "三", "四", "五", "六").forEachIndexed { index, label ->
            HeaderCell(
                text = label,
                width = DAY_COL_WIDTH,
                color = if (index == 0) sundayColor else labelColor,
                dividerColor = dividerColor
            )
        }
        HeaderCell(
            text = "备注",
            width = NOTE_COL_WIDTH,
            color = labelColor,
            dividerColor = dividerColor,
            drawEndDivider = false
        )
    }
}

@Composable
private fun HeaderCell(
    text: String,
    width: Dp,
    color: Color,
    dividerColor: Color,
    drawEndDivider: Boolean = true
) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .then(
                if (drawEndDivider) {
                    Modifier.border(width = GRID_BORDER, color = dividerColor)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------- 表格行 ----------------

/**
 * 单周一行。
 *
 * @param row                    周数据（`row.row` 是校历行号，与服务端 notes/ranges 对齐）
 * @param teachingWeekIndex      该周对应的"教学周序号"，`null` 表示开学前的周（准备周/假期）
 * @param monthLabel             "月份"列要显示的文本；传 `null` 表示与上一周同月，不重复显示
 * @param semesterStartMs        学期开始日 0 点毫秒（用于开学日标记）
 * @param semesterEndMs          学期结束日 0 点毫秒（用于结课日标记）
 * @param todayMs                今天 0 点毫秒
 * @param isCurrentRow           是否当前周（整行浅蓝底）
 * @param notes                  本周备注（带全局 index，用于取 ordinal）
 * @param range                  本周的 customWeekRange（假期/准备周），`null` 表示是普通教学周
 * @param noteOrdinals           备注的全局编号映射 `globalIndex → ordinal`，用于渲染圆圈数字
 */
@Composable
internal fun WeekRowCard(
    row: WeekRow,
    teachingWeekIndex: Int?,
    monthLabel: String?,
    semesterStartMs: Long?,
    semesterEndMs: Long?,
    todayMs: Long,
    isCurrentRow: Boolean,
    notes: List<IndexedNote>,
    range: SemesterCalendarWeekRange?,
    noteOrdinals: Map<Int, Int>
) {
    val dividerColor = AppTheme.colors.campusDividerColor
    val accent = AppTheme.colors.campusSkyBlueColor
    val isHolidayRow = range != null

    // 行底色优先级：当前周 > 假期/准备周 > 默认透明
    val rowBg = when {
        isCurrentRow -> accent.copy(alpha = 0.10f)
        isHolidayRow -> AppTheme.colors.campusAmberColor.copy(alpha = HOLIDAY_ROW_ALPHA)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .width(TABLE_TOTAL_WIDTH)
            .height(TABLE_ROW_HEIGHT)
            .background(rowBg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 周次列：教学周号（有教学周号 → 数字；否则 → 假期/准备周标签）
        WeekIndexCell(
            teachingWeekIndex = teachingWeekIndex,
            range = range,
            isCurrentRow = isCurrentRow,
            dividerColor = dividerColor
        )

        // 月份列
        MonthCell(monthLabel = monthLabel, dividerColor = dividerColor)

        // 7 天单元（days[0]=周日, days[6]=周六；仅周日列红字）
        row.days.forEachIndexed { index, day ->
            DayCell(
                day = day,
                semesterStartMs = semesterStartMs,
                semesterEndMs = semesterEndMs,
                todayMs = todayMs,
                isSunday = index == 0,
                modifier = Modifier
                    .width(DAY_COL_WIDTH)
                    .fillMaxHeight()
                    .border(width = GRID_BORDER, color = dividerColor)
            )
        }

        // 备注列：200dp 足够放完整文字，最多 2 行
        NoteCell(notes = notes, noteOrdinals = noteOrdinals, dividerColor = dividerColor)
    }
}

/**
 * 周次列单元。
 *
 *  - 有 [teachingWeekIndex]（开学以后）→ 显示数字
 *  - 有 [range]（开学前的准备周 / 假期）→ 显示 range.content 琥珀色标签
 *  - 都没有 → 空白（理论不会触发）
 */
@Composable
private fun WeekIndexCell(
    teachingWeekIndex: Int?,
    range: SemesterCalendarWeekRange?,
    isCurrentRow: Boolean,
    dividerColor: Color
) {
    val accent = AppTheme.colors.campusSkyBlueColor
    Box(
        modifier = Modifier
            .width(WEEK_INDEX_WIDTH)
            .fillMaxHeight()
            .border(width = GRID_BORDER, color = dividerColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            teachingWeekIndex != null -> Text(
                text = "$teachingWeekIndex",
                fontSize = 13.sp,
                color = if (isCurrentRow) accent else AppTheme.colors.primaryTextColor,
                fontWeight = if (isCurrentRow) FontWeight.Bold else FontWeight.Medium
            )
            range != null -> Text(
                text = shortRangeLabel(range.content),
                fontSize = 10.sp,
                color = AppTheme.colors.campusAmberColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/** 月份列单元：14sp Bold 主色，有 label 的行加淡主色底。 */
@Composable
private fun MonthCell(monthLabel: String?, dividerColor: Color) {
    Box(
        modifier = Modifier
            .width(MONTH_COL_WIDTH)
            .fillMaxHeight()
            .background(
                if (monthLabel != null) {
                    AppTheme.colors.campusSkyBlueColor.copy(alpha = 0.08f)
                } else {
                    Color.Transparent
                }
            )
            .border(width = GRID_BORDER, color = dividerColor),
        contentAlignment = Alignment.Center
    ) {
        if (monthLabel != null) {
            Text(
                text = monthLabel,
                fontSize = 14.sp,
                color = AppTheme.colors.campusSkyBlueColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * 备注列单元：列宽 [NOTE_COL_WIDTH]=200dp，可放 1~2 条完整 note（不截断）。
 *
 * - 每条 note 竖向排列，文字 `maxLines = 2` + Ellipsis（极长文字仍截断，但 200dp 宽度下覆盖 99% 场景）
 * - `needNumber=true` 的 note 显示圆圈编号 ①②③...
 * - ≥ 3 条 note 的场景下仅显示前 2 条，文字末尾补"…"提示更多
 */
@Composable
private fun NoteCell(
    notes: List<IndexedNote>,
    noteOrdinals: Map<Int, Int>,
    dividerColor: Color
) {
    Box(
        modifier = Modifier
            .width(NOTE_COL_WIDTH)
            .fillMaxHeight()
            .border(width = GRID_BORDER, color = dividerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (notes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.Center) {
                notes.take(2).forEach { indexed ->
                    NoteLine(
                        note = indexed.note,
                        ordinal = noteOrdinals[indexed.globalIndex]
                    )
                }
                if (notes.size > 2) {
                    Text(
                        text = "…还有 ${notes.size - 2} 条",
                        fontSize = 10.sp,
                        color = AppTheme.colors.greyTextColor
                    )
                }
            }
        }
    }
}

/**
 * 把 customWeekRange 的 content 压成 ≤ 3 个字的展示（如"假期"、"准备周"）。
 * 超过 3 个字取首 2 个字 + "…"，保证周次列不会折行。
 */
private fun shortRangeLabel(content: String): String {
    if (content.isBlank()) return ""
    return when {
        content.length <= 3 -> content
        else -> content.take(2) + "…"
    }
}

/**
 * 备注单行：圆圈编号 + 文字（竖向列表中的一项）。
 *
 * `maxLines = 2`：列宽 200dp 下 2 行足够放下绝大多数备注；
 * 超长文字仍会截断加 Ellipsis，但按业务约束概率极低。
 */
@Composable
private fun NoteLine(note: SemesterCalendarNote, ordinal: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (note.needNumber && ordinal != null) {
            Box(
                modifier = Modifier
                    .size(NOTE_CIRCLE_SIZE)
                    .clip(CircleShape)
                    .border(1.dp, AppTheme.colors.campusSkyBlueColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = circledNumber(ordinal),
                    fontSize = 9.sp,
                    color = AppTheme.colors.campusSkyBlueColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = note.content,
            fontSize = 11.sp,
            color = AppTheme.colors.primaryTextColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (note.needNumber) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * 将 1..20 转成 Unicode 圆圈数字字符；超出则回退为纯数字。
 *
 * 备注：若单学期 `needNumber` 备注超过 20 条，排版将退化；按当前业务量不会触发，无需处理。
 */
private fun circledNumber(n: Int): String {
    return when (n) {
        in 1..20 -> ('①' + (n - 1)).toString()
        else -> n.toString()
    }
}

// ---------------- 单元格 ----------------

/**
 * 单日单元。
 *  - 今天：实心圆点底 + 白字
 *  - 开学日 / 结课日：空心圆圈边框（不抢走"今天"的视觉）
 *  - 普通日：数字；仅周日（[isSunday]=true）用 `errorRedColor`
 */
@Composable
internal fun DayCell(
    day: DayCellData?,
    semesterStartMs: Long?,
    semesterEndMs: Long?,
    todayMs: Long,
    isSunday: Boolean,
    modifier: Modifier = Modifier
) {
    val isToday = day != null && day.dateMs == todayMs
    val isSemesterStart = day != null && semesterStartMs != null && day.dateMs == semesterStartMs
    val isSemesterEnd = day != null && semesterEndMs != null && day.dateMs == semesterEndMs

    val textColor = when {
        day == null -> Color.Transparent
        isToday -> Color.White
        isSunday -> AppTheme.colors.errorRedColor
        else -> AppTheme.colors.primaryTextColor
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            day == null -> Unit
            isToday -> {
                Box(
                    modifier = Modifier
                        .size(DAY_MARK_SIZE)
                        .clip(CircleShape)
                        .background(AppTheme.colors.commonColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${day.dayOfMonth}",
                        fontSize = 13.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            isSemesterStart || isSemesterEnd -> {
                Box(
                    modifier = Modifier
                        .size(DAY_MARK_SIZE)
                        .clip(CircleShape)
                        .border(RING_STROKE, AppTheme.colors.campusSkyBlueColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${day.dayOfMonth}",
                        fontSize = 13.sp,
                        color = AppTheme.colors.campusSkyBlueColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            else -> {
                Text(
                    text = "${day.dayOfMonth}",
                    fontSize = 13.sp,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ---------------- 教学周蓝色折线 ----------------

/**
 * 在表格区域顶层绘制"学期开始日 → 学期结束日"的蓝色外框折线，
 * 贯穿所有**教学周**（跳过假期 / 准备周整行）。
 *
 * 坐标系（内部按 [HEADER_ROW_HEIGHT] / [TABLE_ROW_HEIGHT] / [DAY_COL_WIDTH] 常量计算，
 * 无需调用方传参）：
 *  - y 起点：[HEADER_ROW_HEIGHT]（表头底部）
 *  - 每行高度：[TABLE_ROW_HEIGHT]
 *  - 日期区 7 列等宽，列宽 [DAY_COL_WIDTH]，起点 = [WEEK_INDEX_WIDTH] + [MONTH_COL_WIDTH]
 *
 * 绘制策略：
 *  - **多行段**（段高 ≥ 2 行）：画 8 点折线矩形，顶部从 startCol 开始露出缺口，
 *    底部在 endCol 处收口
 *  - **单行段**（段高 = 1 行）：简化为开口矩形，避免退化路径自交
 */
@Composable
internal fun TeachingPeriodOverlay(
    weeks: List<WeekRow>,
    rangeByRow: Map<Int, SemesterCalendarWeekRange>,
    semesterStartMs: Long?,
    semesterEndMs: Long?,
    modifier: Modifier = Modifier
) {
    val stroke = AppTheme.colors.campusSkyBlueColor

    val segments = remember(weeks, rangeByRow) {
        buildTeachingSegments(weeks, rangeByRow)
    }
    if (segments.isEmpty() || semesterStartMs == null || semesterEndMs == null) return

    Canvas(modifier = modifier) {
        val rowPx = TABLE_ROW_HEIGHT.toPx()
        val headerPx = HEADER_ROW_HEIGHT.toPx()
        val colWidth = DAY_COL_WIDTH.toPx()
        val halfBorder = GRID_BORDER.toPx() / 2f
        val leftPx = (WEEK_INDEX_WIDTH + MONTH_COL_WIDTH).toPx() + halfBorder
        val rightPx = leftPx + colWidth * 7 - halfBorder * 2

        val paintStroke = Stroke(width = TEACHING_PATH_STROKE.toPx())

        segments.forEachIndexed { segIndex, seg ->
            val segTopY = headerPx + seg.startIndex * rowPx
            val segBottomY = headerPx + (seg.endIndex + 1) * rowPx

            // 首段的起始列 = 学期开始日在段内首周的列 index；否则 0
            val startColIndex = if (segIndex == 0) {
                indexOfDayInWeek(weeks[seg.startIndex], semesterStartMs) ?: 0
            } else 0
            // 末段的结束列 = 学期结束日在段内末周的列 index；否则 6
            val endColIndex = if (segIndex == segments.lastIndex) {
                indexOfDayInWeek(weeks[seg.endIndex], semesterEndMs) ?: 6
            } else 6

            val startX = leftPx + startColIndex * colWidth
            val endX = leftPx + (endColIndex + 1) * colWidth

            val path = Path().apply {
                if (seg.endIndex > seg.startIndex) {
                    // 多行段
                    moveTo(startX, segTopY)
                    lineTo(rightPx, segTopY)
                    lineTo(rightPx, segBottomY - rowPx)
                    lineTo(endX, segBottomY - rowPx)
                    lineTo(endX, segBottomY)
                    lineTo(leftPx, segBottomY)
                    lineTo(leftPx, segTopY + rowPx)
                    lineTo(startX, segTopY + rowPx)
                    close()
                } else {
                    // 单行段：开口矩形
                    moveTo(startX, segTopY)
                    lineTo(rightPx, segTopY)
                    lineTo(rightPx, segBottomY)
                    lineTo(leftPx, segBottomY)
                    lineTo(leftPx, segTopY)
                    close()
                }
            }
            drawPath(path = path, color = stroke, style = paintStroke)
        }
    }
}

private data class TeachingSegment(val startIndex: Int, val endIndex: Int)

private fun buildTeachingSegments(
    weeks: List<WeekRow>,
    rangeByRow: Map<Int, SemesterCalendarWeekRange>
): List<TeachingSegment> {
    if (weeks.isEmpty()) return emptyList()
    val result = ArrayList<TeachingSegment>()
    var i = 0
    while (i < weeks.size) {
        if (rangeByRow[weeks[i].row] == null) {
            val start = i
            while (i < weeks.size && rangeByRow[weeks[i].row] == null) i++
            result.add(TeachingSegment(startIndex = start, endIndex = i - 1))
        } else {
            i++
        }
    }
    return result
}

/** 返回 targetMs 在 [week] 中的列 index（0..6），不在本周返回 null。 */
private fun indexOfDayInWeek(week: WeekRow, targetMs: Long): Int? {
    week.days.forEachIndexed { index, day ->
        if (day != null && day.dateMs == targetMs) return index
    }
    return null
}

// ---------------- 工具 ----------------

/** ISO → `yyyy-MM-dd`，失败返回 `--`。 */
private fun formatDisplayShort(iso: String): String {
    val ms = parseIsoDateMs(iso) ?: return "--"
    val cal = Calendar.getInstance().apply { timeInMillis = ms }
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH) + 1
    val d = cal.get(Calendar.DAY_OF_MONTH)
    return "%04d-%02d-%02d".format(y, m, d)
}
