package com.creamaker.changli_planet_app.profileSettings.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSkinTheme {
                AboutScreen(this)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun AboutScreen(activity: Activity? = null) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                },
                title = { Text("关于我们", color = AppTheme.colors.titleTopColor) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            painterResource(id = R.drawable.ic_back),
                            contentDescription = "返回",
                            tint = AppTheme.colors.titleTopColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.bgTopBarColor,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.colors.bgPrimaryColor)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "这是什么",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.primaryTextColor ,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "CreaMaker是由长沙理工大学计算机学院的学生自主创建的组织，旨在通过创新项目和实践活动，激发人的创造力和技术动手能力。在这个组织中，我们不仅会进行技术学习，还会开发有趣且实用的项目，致力于将创意转化为实际的产品和服务。我们的成员热衷于通过技术与创新解决问题，共同成长。",
                fontSize = 16.sp,
                color = AppTheme.colors.primaryTextColor ,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "组织特色",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.primaryTextColor ,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 特色列表
            FeatureItem(
                title = "创意驱动",
                description = "CreaMaker欢迎所有富有创意的想法，无论是软件、硬件，还是跨学科项目，我们都为创新者提供自由发展的空间。"
            )

            FeatureItem(
                title = "实践为主",
                description = "我们秉承“边做边学”的理念，所有成员都可以通过实际项目提升自己的技术和项目管理能力。"
            )

            FeatureItem(
                title = "协作与共享",
                description = "组织内部注重团队合作与资源共享，成员们通过互动与协作，激发更多灵感，推动项目进展。"
            )

            FeatureItem(
                title = "开放与包容",
                description = "CreaMaker鼓励多样化的背景和思维方式，欢迎来自不同领域的人才，共同参与项目开发。"
            )

            // 标题3
            Text(
                text = "我们的代表项目",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.primaryTextColor ,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )

            Text(
                text = "长理星球",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.primaryTextColor ,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "长理星球是一款由CreaMaker为长沙理工大学开发的社区交流App，专为校内学生提供互动与互助的平台。该应用的核心功能是通过社区讨论、答疑等方式，促进学生之间的交流与合作，帮助解决学习和生活中的问题。除了社交功能外，长理星球还集成了多种实用工具，如：",
                fontSize = 16.sp,
                color = AppTheme.colors.primaryTextColor ,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 功能列表
            BulletPoint(text = "成绩查询：学生可以直接在App内查看学期成绩。")
            BulletPoint(text = "课表定制：用户可以根据个人课表定制学习计划，方便日程管理。")
            BulletPoint(text = "学习资料分享：提供一个让学生分享笔记、复习资料、作业答案的平台。")

            Text(
                text = "此外，长理星球通过设立积分与奖励机制，激励用户参与讨论、解答问题，增强社区的活跃度与参与感。",
                fontSize = 16.sp,
                color = AppTheme.colors.primaryTextColor ,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
fun FeatureItem(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.primaryTextColor ,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = description,
            fontSize = 16.sp,
            color = AppTheme.colors.primaryTextColor
        )
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "• ",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.primaryTextColor
        )
        Text(
            text = text,
            fontSize = 16.sp,
            color = AppTheme.colors.primaryTextColor
        )
    }
}

