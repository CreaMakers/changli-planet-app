package com.creamaker.changli_planet_app.feature.common.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creamaker.changli_planet_app.core.Route

data class SemesterItem(
    val semester: String,
    val title: String,
    val subtitle: String
)

class CalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                CalendarScreen()
            }
        }
    }
}

@Composable
@Preview
fun CalendarScreen() {
    val semesterList = listOf(
        SemesterItem("2025-2026-2", "2025-2026学年度校历", "（第二学期）"),
        SemesterItem("2025-2026-1", "2025-2026学年度校历", "（第一学期）"),
        SemesterItem("2024-2025-2", "2024-2025学年度校历", "（第二学期）"),
        SemesterItem("2024-2025-1", "2024-2025学年度校历", "（第一学期）"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = "学期校历",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(semesterList) { item ->
                SemesterItemCard(item = item)
            }
        }
    }
}

@Composable
fun SemesterItemCard(item: SemesterItem) {
    val preUrl = "https://api.csustplanet.zhelearn.com/static/school_calendar/index.html?config="
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { Route.goWebView(context, "${preUrl}${item.semester}") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black // 黑色文字
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "学期代码: ${item.semester}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}