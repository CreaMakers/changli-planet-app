package com.creamaker.changli_planet_app.freshNews.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.theme.AppTheme

@Composable
fun NewsTopBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onAvatarClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.bgPrimaryColor)
    ) {
        // Search & Avatar Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Box (Fake)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp)) // Assuming edit_text_background is rounded
                    .background(Color(0xFFF2F2F2)) // Assuming edit_text_background color (LightGray)
                    .clickable { onSearchClick() }
                    .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.component2), // Search icon
                    contentDescription = "Search",
                    modifier = Modifier.size(25.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = "点击搜索想要的群组",
                    color = AppTheme.colors.greyTextColor,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Avatar
            AsyncImage(
                model = UserInfoManager.userAvatar,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable { onAvatarClick() },
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_fulilian),
                error = painterResource(id = R.drawable.ic_fulilian)
            )
        }

        // Tabs
        val tabs = listOf("推荐", "热门")
        PrimaryTabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = currentTab,
            containerColor = AppTheme.colors.bgPrimaryColor,
            contentColor = AppTheme.colors.primaryTextColor,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(currentTab),
                    color = AppTheme.colors.commonColor,
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = currentTab == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = if (currentTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentTab == index) AppTheme.colors.commonColor else AppTheme.colors.primaryTextColor
                        )
                    }
                )
            }
        }
    }
}

@Preview(name = "News Top Bar Preview")
@Composable
fun PreviewNewsTopBar() {
    MaterialTheme {
        NewsTopBar(
            currentTab = 0,
            onTabSelected = {},
            onSearchClick = {},
            onAvatarClick = {}
        )
    }
}
