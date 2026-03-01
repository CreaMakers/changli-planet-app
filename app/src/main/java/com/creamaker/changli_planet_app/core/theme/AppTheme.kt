package com.creamaker.changli_planet_app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import com.creamaker.changli_planet_app.skin.SkinManager

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
    val commonColor: Color = DesignColors.CommonBlue,
    val outlineLowContrastColor: Color = DesignColors.OutlineLowContrast,

    val secondaryTextColor: Color = DesignColors.TextSecondary,
    val functionalTextColor: Color = DesignColors.TextFunctional,
    val widgetPrimaryTextColor: Color = DesignColors.TextWidgetPrimary,
    val disabledTextColor: Color = DesignColors.TextDisabled,
    val searchHintColor: Color = DesignColors.SearchHint,

    // 背景类补充
    val bgSecondaryInverseColor: Color = DesignColors.BgSecondaryInverse,
    val bgCardColor: Color = DesignColors.BgCard,
    val bgCardHighContrastColor: Color = DesignColors.BgCardHighContrast,
    val bgRecyclerViewColor: Color = DesignColors.BgRecyclerView,
    val bgLightGrayColor: Color = DesignColors.LightGray,

    // 按钮类补充
    val bgButtonLowlightColor: Color = DesignColors.BgButtonLowlight,

    // 图标类补充
    val iconPrimaryColor: Color = DesignColors.IconPrimary,
    val iconSettingColor: Color = DesignColors.IconSetting,

    // 分隔线/描边补充
    val controlDividerColor: Color = DesignColors.ControlDivider,
    val outlineColor: Color = DesignColors.Outline,

    // 帖子专用
    val postTextPrimaryColor: Color = DesignColors.PostTextPrimary,
    val postTextTitleColor: Color = DesignColors.PostTextTitle,
    val postTextHintColor: Color = DesignColors.PostTextHint,
    val postTextReplyColor: Color = DesignColors.PostTextReply,
    val postHintBarColor: Color = DesignColors.PostHintBar,

    // 状态/其他
    val tabRippedColor: Color = DesignColors.TabRipped,
    val errorRedColor: Color = DesignColors.ErrorRed,
    val successGreenColor: Color = DesignColors.SuccessGreen
) {
    companion object {
        val Default = SkinColors()
    }
}

val LocalSkinColors = staticCompositionLocalOf { SkinColors.Default }

private val LightSkinColors = SkinColors(
    primaryTextColor = DesignColors.TextPrimary,
    greyTextColor = DesignColors.TextGrey,
    bgPrimaryColor = DesignColors.BgPrimary,
    bgSecondaryColor = DesignColors.BgSecondary,
    iconSecondaryColor = DesignColors.IconSecondary,
    dividerColor = DesignColors.Divider,
    loadingColor = DesignColors.Loading,
    titleTopColor = DesignColors.TitleTop,
    bgTopBarColor = DesignColors.BgTopBar,
    bgButtonColor = DesignColors.BgButton,
    textButtonColor = DesignColors.TextButton,
    textHeighLightColor = DesignColors.TextHighlight,
    commonColor = DesignColors.CommonBlue,
    outlineLowContrastColor = DesignColors.OutlineLowContrast,
    secondaryTextColor = DesignColors.TextSecondary,
    functionalTextColor = DesignColors.TextFunctional,
    widgetPrimaryTextColor = DesignColors.TextWidgetPrimary,
    disabledTextColor = DesignColors.TextDisabled,
    searchHintColor = DesignColors.SearchHint,
    bgSecondaryInverseColor = DesignColors.BgSecondaryInverse,
    bgCardColor = DesignColors.BgCard,
    bgCardHighContrastColor = DesignColors.BgCardHighContrast,
    bgRecyclerViewColor = DesignColors.BgRecyclerView,
    bgLightGrayColor = DesignColors.LightGray,
    bgButtonLowlightColor = DesignColors.BgButtonLowlight,
    iconPrimaryColor = DesignColors.IconPrimary,
    iconSettingColor = DesignColors.IconSetting,
    controlDividerColor = DesignColors.ControlDivider,
    outlineColor = DesignColors.Outline,
    postTextPrimaryColor = DesignColors.PostTextPrimary,
    postTextTitleColor = DesignColors.PostTextTitle,
    postTextHintColor = DesignColors.PostTextHint,
    postTextReplyColor = DesignColors.PostTextReply,
    postHintBarColor = DesignColors.PostHintBar,
    tabRippedColor = DesignColors.TabRipped,
    errorRedColor = DesignColors.ErrorRed,
    successGreenColor = DesignColors.SuccessGreen
)

private val DarkSkinColors = SkinColors(
    primaryTextColor = Color(0xFFF8F8F8),
    greyTextColor = Color(0xFFD0D0D0),
    bgPrimaryColor = Color(0xFF000000),
    bgSecondaryColor = Color(0xFF191919),
    iconSecondaryColor = Color(0xFFD0D0D0),
    dividerColor = Color(0xFFD0D0D0),
    loadingColor = Color(0xFF0099FA),
    titleTopColor = Color(0xFF0E749C),
    bgTopBarColor = Color(0xFF223853),
    bgButtonColor = Color(0xFF1D57AD),
    textButtonColor = Color(0xFFFFFFFF),
    textHeighLightColor = Color(0xFF4285F4),
    commonColor = Color(0xFF0099FA),
    outlineLowContrastColor = Color(0xFFC4DFFC),
    secondaryTextColor = Color(0xFF808080),
    functionalTextColor = Color(0xFF4F7FED),
    widgetPrimaryTextColor = Color(0xFF000000),
    disabledTextColor = Color(0xFFA0A0A0),
    searchHintColor = Color(0xFFD3DDDF),
    bgSecondaryInverseColor = Color(0xFFD3D3D3),
    bgCardColor = Color(0xFF191919),
    bgCardHighContrastColor = Color(0xFF376AD3),
    bgRecyclerViewColor = Color(0xFF141414),
    bgLightGrayColor = Color(0xFFF2F2F2),
    bgButtonLowlightColor = Color(0xFF272C32),
    iconPrimaryColor = Color(0xFF4285F4),
    iconSettingColor = Color(0xFF0D57ED),
    controlDividerColor = Color(0xFF909AA7),
    outlineColor = Color(0xFFD0D0D0),
    postTextPrimaryColor = Color(0xFFF8F8F8),
    postTextTitleColor = Color(0xFF000000),
    postTextHintColor = Color(0xFFABABAB),
    postTextReplyColor = Color(0xFF1E4B94),
    postHintBarColor = Color(0xFF0997F8),
    tabRippedColor = Color(0xFFF2F2F2),
    errorRedColor = Color(0xFFFF3D00),
    successGreenColor = Color(0xFF00C853)
)

@Composable
fun AppSkinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isInPreview = LocalInspectionMode.current
    val skinName = if (isInPreview) {
        "skin_default"
    } else {
        val currentSkinName by SkinManager.currentSkinName.collectAsState()
        currentSkinName
    }

    val useDarkPalette = darkTheme || isDarkSkin(skinName)

    val currentColors = remember(useDarkPalette) {
        if (useDarkPalette) DarkSkinColors else LightSkinColors
    }

    CompositionLocalProvider(LocalSkinColors provides currentColors) {
        content()
    }
}

private fun isDarkSkin(skinName: String): Boolean {
    return skinName.contains("dark", ignoreCase = true)
}

// 6. 获取皮肤主题对象
object AppTheme {
    val colors: SkinColors
        @Composable
        get() = LocalSkinColors.current
}