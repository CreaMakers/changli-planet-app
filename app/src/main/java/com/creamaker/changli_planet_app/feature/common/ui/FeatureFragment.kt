package com.creamaker.changli_planet_app.feature.common.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose. foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation. shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw. clip
import androidx. compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui. res.painterResource
import androidx.compose. ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui. unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage
import com.creamaker. changli_planet_app.R
import com.creamaker.changli_planet_app. core.theme.AppSkinTheme
import com.creamaker. changli_planet_app.core.theme.AppTheme
import com.creamaker. changli_planet_app.feature.common.compose_ui.FunctionItemData
import com.creamaker.changli_planet_app.feature.common.compose_ui.MainFunctionSection

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
        return ComposeView(requireContext()). apply {
            setContent {
                AppSkinTheme {
                    FeatureScreen(
                        avatarUrl = "",
                        onFunctionClick = { id -> handleFunctionClick(id) }
                    )
                }
            }
        }
    }

    private fun handleFunctionClick(id: String) {
        // 处理点击
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
    avatarUrl: String = "",
    onFunctionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val mainFunctionItems = remember {
        listOf(
            FunctionItemData("schedule", "课表", R.drawable.ic_timetable, FunctionColors.Schedule),
            FunctionItemData("grade", "成绩查询", R.drawable.ic_exam, FunctionColors. Grade),
            FunctionItemData("map", "校园地图", R.drawable.ic_map, FunctionColors.Map),
            FunctionItemData("homework", "作业查询", R.drawable.ic_homework, FunctionColors. Homework),
            FunctionItemData("electric", "电费查询", R. drawable.ic_bill, FunctionColors. Electric),
            FunctionItemData("exam", "考试安排", R.drawable.ic_schedule, FunctionColors. Exam),
            FunctionItemData("calendar", "校历", R.drawable.ic_calendar, FunctionColors.Calendar)
        )
    }

    val moreFunctionItems = remember {
        listOf(
            FunctionItemData("cet", "四六级", R.drawable.ic_essay, FunctionColors. CET),
            FunctionItemData("lost_found", "失物招领", R. drawable.ic_lost_and_found, FunctionColors.LostFound),
            FunctionItemData("classroom", "空教室", R.drawable.ic_classroom, FunctionColors. Classroom),
            FunctionItemData("account", "记账本", R.drawable. account_book, FunctionColors. Account),
            FunctionItemData("document", "资料库", R.drawable. ic_document, FunctionColors.Document),
            FunctionItemData("mandarin", "普通话", R. drawable.ic_talking, FunctionColors. Mandarin)
        )
    }

    val onItemClick = remember(onFunctionClick) {
        { item: FunctionItemData -> onFunctionClick(item.id) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme. colors.bgPrimaryColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(avatarUrl = avatarUrl)

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                MainFunctionSection(
                    mainItems = mainFunctionItems,
                    moreItems = moreFunctionItems,
                    onItemClick = onItemClick
                )

                Spacer(modifier = Modifier. height(32.dp))

                TodayOverviewSection()
            }

            Spacer(modifier = Modifier. height(24.dp))
        }
    }
}

@Composable
private fun HeaderSection(avatarUrl: String) {
    Box(
        modifier = Modifier
            . fillMaxWidth()
            .height(220.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable. planet_logo),
            contentDescription = "背景图片",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale. Crop
        )

        AsyncImage(
            model = avatarUrl,
            contentDescription = "用户头像",
            modifier = Modifier
                .padding(start = 16.dp, top = 48.dp)
                .size(42.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun TodayOverviewSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "今日概览",
            color = AppTheme.colors.primaryTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8. dp, bottom = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AppTheme. colors.bgSecondaryColor)
                .padding(16. dp)
        ) {
            Text(
                text = "今日课表、考试等信息将在这里显示.. .",
                color = AppTheme.colors.greyTextColor,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier. height(12.dp))

            TextButton(
                onClick = { },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "查看更多",
                    color = AppTheme. colors.textHeighLightColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FeatureScreenPreview() {
    MaterialTheme {
        FeatureScreen(
            avatarUrl = "",
            onFunctionClick = {}
        )
    }
}