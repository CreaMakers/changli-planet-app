package com.creamaker.changli_planet_app.feature.common.compose_ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.feature.common.ui.FunctionColors

/**
 * 主功能区 + 可展开文件夹
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun FunctionSection(
    modifier: Modifier = Modifier,
    title: String,
    items: List<FunctionItemData>,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 标题
        Text(
            text = title,
            color = AppTheme.colors.primaryTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
        )

        // 功能网格
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.chunked(4).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    rowItems.forEach { item ->
                        FunctionItem(
                            item = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // 补齐占位，确保每一列宽度一致
                    val remaining = 4 - rowItems.size
                    if (remaining > 0) {
                        Spacer(modifier = Modifier.weight(remaining.toFloat()))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ExpandableFolderSystemPreview() {
    val context = LocalContext.current
    val mainFunctionItems = remember {
        listOf(
            FunctionItemData(
                id = "schedule",
                title = "课表",
                iconRes = R.drawable.ic_timetable,
                tintColor = FunctionColors.Schedule
            ) {
                // 点击事件
                Route.goTimetable(context)
            },

            FunctionItemData(
                id = "grade",
                title = "成绩查询",
                iconRes = R.drawable.ic_exam,
                tintColor = FunctionColors.Grade
            ) {
                Route.goScoreInquiry(context)
            },

            FunctionItemData(
                id = "map",
                title = "校园地图",
                iconRes = R.drawable.ic_map,
                tintColor = FunctionColors.Map
            ) {
                Route.goCampusMap(context)
            },

            FunctionItemData(
                id = "homework",
                title = "作业查询",
                iconRes = R.drawable.ic_homework,
                tintColor = FunctionColors.Homework
            ) {
                Route.goMooc(context)
            },

            FunctionItemData(
                id = "electric",
                title = "电费查询",
                iconRes = R.drawable.ic_bill,
                tintColor = FunctionColors.Electric
            ) {
                Route.goElectronic(context)
            },

            FunctionItemData(
                id = "exam",
                title = "考试安排",
                iconRes = R.drawable.ic_schedule,
                tintColor = FunctionColors.Exam
            ) {
                Route.goExamArrangement(context)
            },

            FunctionItemData(
                id = "calendar",
                title = "校历",
                iconRes = R.drawable.ic_calendar,
                tintColor = FunctionColors.Calendar
            ) {
                Route.goCalendar(context)
            },

            FunctionItemData(
                id = "cet",
                title = "四六级",
                iconRes = R.drawable.ic_essay,
                tintColor = FunctionColors.CET
            ) {
                Route.goCet(context)
            },

            FunctionItemData(
                id = "lost_found",
                title = "失物招领",
                iconRes = R.drawable.ic_lost_and_found,
                tintColor = FunctionColors.LostFound
            ) {
                // 显示对话框
                // showNormalDialog("该功能暂时关闭")
            },

            FunctionItemData(
                id = "classroom",
                title = "空教室",
                iconRes = R.drawable.ic_classroom,
                tintColor = FunctionColors.Classroom
            ) {
                // 显示对话框
                // showNormalDialog("该功能暂时关闭")
            },

            FunctionItemData(
                id = "account",
                title = "记账本",
                iconRes = R.drawable.account_book,
                tintColor = FunctionColors.Account
            ) {
                Route.goAccountBook(context)
            },

            FunctionItemData(
                id = "document",
                title = "资料库",
                iconRes = R.drawable.ic_document,
                tintColor = FunctionColors.Document
            ) {
                Route.goContract(context)
            },

            FunctionItemData(
                id = "mandarin",
                title = "普通话",
                iconRes = R.drawable.ic_talking,
                tintColor = FunctionColors.Mandarin
            ) {
                Route.goMande(context)
            }
        )
    }
    FunctionSection(
        title = "常用功能",
        items = mainFunctionItems,
    )
}