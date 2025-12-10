package com.creamaker.changli_planet_app.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import com.creamaker.changli_planet_app.skin.helper.SkinComposeHelper

// 1. 定义你的皮肤颜色集合（根据你的业务需求添加字段）
// 1. 定义颜色数据类
data class SkinColors(
    val primaryTextColor: Color = DesignColors.TextPrimary,
    val greyTextColor: Color = DesignColors.TextGrey,
    val bgPrimaryColor: Color = DesignColors.BgPrimary,
    val bgSecondaryColor: Color = DesignColors.BgSecondary,
    val iconSecondaryColor: Color = DesignColors.IconSecondary,
    val dividerColor: Color = DesignColors.Divider,
    val loadingColor: Color = DesignColors.Loading,
    val titleTopColor: Color = DesignColors.TitleTop,
    val bgTopBarColor: Color = DesignColors.BgTopBar,
    val bgButtonColor: Color = DesignColors.BgButton,
    val textButtonColor: Color = DesignColors.TextButton,
    val textHeighLightColor: Color = DesignColors.TextHighlight,
) {
    companion object {
        val Default = SkinColors() // 默认即设计规范
    }
}
// 2. 正确的 CompositionLocal（不能放资源ID）
val LocalSkinColors = staticCompositionLocalOf { SkinColors.Default }

@Composable
fun AppSkinTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    var skinVersion by remember { mutableIntStateOf(0) }

    // 3. 注册换肤监听器
    DisposableEffect(Unit) {
        val listener = object : SkinSupportable {
            override fun applySkin() {
                // 通知 Compose 重新计算
                skinVersion++
            }
        }

        SkinManager.attach(listener)

        onDispose {
            SkinManager.detach(listener)
        }
    }

    // 4. 当 skinVersion 更新时重新读取皮肤颜色
    val currentColors = remember(skinVersion) {
        SkinColors(
            primaryTextColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_primary
            ) as Color,
            greyTextColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_grey
            ) as Color,
            bgPrimaryColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_primary
            ) as Color,
            bgSecondaryColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_secondary
            ) as Color,
            iconSecondaryColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_icon_secondary
            ) as Color,
            dividerColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_divider
            ) as Color,
            loadingColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_loading
            ) as Color,
            titleTopColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_title_top
            ) as Color,
            bgTopBarColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_top_bar
            ) as Color,
            bgButtonColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_button
            ) as Color,
            textButtonColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_button
            ) as Color,
            textHeighLightColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_highlight
            ) as Color
        )
    }

    // 5. 注入到 CompositionLocal
    CompositionLocalProvider(LocalSkinColors provides currentColors) {
        content()
    }
}

// 6. 获取皮肤主题对象
object AppTheme {
    val colors: SkinColors
        @Composable
        get() = LocalSkinColors.current
}