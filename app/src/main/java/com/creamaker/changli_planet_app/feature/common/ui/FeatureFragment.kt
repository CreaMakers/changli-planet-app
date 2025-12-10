package com.creamaker.changli_planet_app.feature.common.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.feature.common.compose_ui.FunctionItemData
import com.creamaker.changli_planet_app.feature.common.compose_ui.FunctionSection

class FeatureFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance() = FeatureFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppSkinTheme {
                    FeatureScreen()
                }
            }
        }
    }
}

object FunctionColors {
    val Schedule = Color(0xFF5C6BC0)
    val Grade = Color(0xFF26A69A)
    val Map = Color(0xFF66BB6A)
    val Homework = Color(0xFFFF7043)
    val Electric = Color(0xFFFFCA28)
    val Exam = Color(0xFFEF5350)
    val Calendar = Color(0xFF42A5F5)
    val CET = Color(0xFFAB47BC)
    val LostFound = Color(0xFF8D6E63)
    val Classroom = Color(0xFF78909C)
    val Account = Color(0xFF26C6DA)
    val Document = Color(0xFF9CCC65)
    val Mandarin = Color(0xFFEC407A)
}

@Composable
fun FeatureScreen(
    modifier: Modifier = Modifier,
) {
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
        )
    }
    val otherFunctionItems = remember {
        listOf(
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
                iconRes = R.drawable.ic_account_book,
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.bgPrimaryColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FeatureHeaderSection(avatarUrl = "https://pic.imgdb.cn/item/671e5e17d29ded1a8c5e0dbe.jpg")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                FunctionSection(
                    title = "常用功能",
                    items = mainFunctionItems,
                )
                Spacer(modifier = Modifier.height(16.dp))
                FunctionSection(
                    title = "其他",
                    items = otherFunctionItems
                )
            }
        }
    }
}

@Composable
private fun FeatureHeaderSection(avatarUrl: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .statusBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.planet_logo),
            contentDescription = "背景图片",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FeatureScreenPreview() {
    MaterialTheme {
        FeatureScreen()
    }
}