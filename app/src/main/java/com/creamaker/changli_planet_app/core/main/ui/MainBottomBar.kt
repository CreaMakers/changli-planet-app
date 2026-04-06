package com.creamaker.changli_planet_app.core.main.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.core.main.navigation.MainTabDestination
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.core.theme.SkinColors
import com.creamaker.changli_planet_app.widget.view.FloatingTabBar
import com.creamaker.changli_planet_app.widget.view.FloatingTabBarDefaults
import com.creamaker.changli_planet_app.widget.view.FloatingTabBarScrollConnection
import com.creamaker.changli_planet_app.widget.view.rememberFloatingTabBarScrollConnection
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState

private val BottomBarGlassColor = Color(0xFFEFF2F6).copy(alpha = 0.84f)

@Composable
fun MainBottomBar(
    selectedDestination: MainTabDestination,
    onDestinationSelected: (MainTabDestination) -> Unit,
    scrollConnection: FloatingTabBarScrollConnection,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val selectedKey = selectedDestination.index
    val colors = AppTheme.colors
    val currentSelectedKey by rememberUpdatedState(selectedKey)
    val currentOnDestinationSelected by rememberUpdatedState(onDestinationSelected)
    val hazeModifier = remember(hazeState) {
        Modifier.hazeEffect(hazeState) { noiseFactor = 0f }
    }

    FloatingTabBar(
        selectedTabKey = selectedKey,
        scrollConnection = scrollConnection,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        tabBarContentModifier = hazeModifier,
        colors = FloatingTabBarDefaults.colors(
            backgroundColor = BottomBarGlassColor,
            accessoryBackgroundColor = BottomBarGlassColor,
            touchGlowColor = colors.commonColor
        ),
        sizes = FloatingTabBarDefaults.sizes(
            tabBarContentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            tabInlineContentPadding = PaddingValues(10.dp),
            tabExpandedContentPadding = PaddingValues(vertical = 12.dp, horizontal = 0.dp),
            tabSpacing = 4.dp
        )
    ) {
        bottomBarItems.forEach { item ->
            tab(
                key = item.index,
                indication = null,
                title = {
                    val selected = item.index == currentSelectedKey
                    Text(
                        text = stringResource(item.labelResId),
                        style = bottomBarLabelStyle(selected),
                        color = bottomBarTint(selected, colors)
                    )
                },
                icon = {
                    val selected = item.index == currentSelectedKey
                    Icon(
                        painter = painterResource(item.iconResId),
                        contentDescription = null,
                        tint = bottomIconBarTint(selected, colors),
                        modifier = Modifier.size(BottomBarIconSize)
                    )
                },
                onClick = {
                    MainTabDestination.fromIndex(item.index)?.let(currentOnDestinationSelected)
                }
            )
        }
    }
}

@Composable
private fun bottomBarTint(selected: Boolean, colors: SkinColors) =
    if (selected) colors.iconPrimaryColor else colors.iconSecondaryColor

@Composable
private fun bottomIconBarTint(selected: Boolean, colors: SkinColors) =
    if (selected) Color.Unspecified else colors.iconSecondaryColor

@Composable
private fun bottomBarLabelStyle(selected: Boolean): TextStyle =
    TextStyle(
        fontSize = BottomBarLabelSize,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
    )

private val BottomBarIconSize = 26.dp
private val BottomBarLabelSize = 13.sp

private val bottomBarItems = listOf(
    MainBottomBarItem(0, R.drawable.ic_overview, R.string.overview),
    MainBottomBarItem(1, R.drawable.nfeature, R.string.function),
    MainBottomBarItem(2, R.drawable.nnews, R.string.intel_station),
    MainBottomBarItem(3, R.drawable.nprofile, R.string.profile_home)
)

private data class MainBottomBarItem(
    val index: Int,
    val iconResId: Int,
    val labelResId: Int
)

@Preview(showBackground = true)
@Composable
private fun MainBottomBarPreview() {
    AppSkinTheme {
        MainBottomBar(
            selectedDestination = MainTabDestination.Overview,
            onDestinationSelected = {},
            scrollConnection = rememberFloatingTabBarScrollConnection(),
            hazeState = rememberHazeState()
        )
    }
}
