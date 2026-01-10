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

data class SkinColors(
    // ====================== 原始字段  ======================
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

    // ====================== 新增补充字段 ======================
    // 文字类补充
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
// 2. 正确的 CompositionLocal（不能放资源ID）
val LocalSkinColors = staticCompositionLocalOf { SkinColors.Default }

@Composable
fun AppSkinTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    var skinVersion by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val listener = object : SkinSupportable {
            override fun applySkin() {
                skinVersion++
            }
        }
        SkinManager.attach(listener)
        onDispose {
            SkinManager.detach(listener)
        }
    }

    // 当 skinVersion 更新时重新读取所有皮肤颜色
    val currentColors = remember(skinVersion) {
        SkinColors(
            // ====================== 文字类 ======================
            primaryTextColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_primary
            ) as Color,
            greyTextColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_grey
            ) as Color,
            secondaryTextColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_secondary
            ) as Color,
            textHeighLightColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_highlight
            ) as Color,
            functionalTextColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_functional
            ) as Color,
            widgetPrimaryTextColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_widget_primary
            ) as Color,
            disabledTextColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_disabled
            ) as Color,
            textButtonColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_text_button
            ) as Color,
            searchHintColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_search_hint
            ) as Color,

            // ====================== 背景类 ======================
            bgPrimaryColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_primary
            ) as Color,
            bgSecondaryColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_secondary
            ) as Color,
            bgSecondaryInverseColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_secondary_inverse
            ) as Color,
            bgCardColor = SkinComposeHelper.getSkinColor(context, R.color.color_bg_card) as Color,
            bgCardHighContrastColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_card_high_contrast
            ) as Color,
            bgTopBarColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_top_bar
            ) as Color,
            bgRecyclerViewColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_recycler_view
            ) as Color,
            bgLightGrayColor = SkinComposeHelper.getSkinColor(context, R.color.light_gray) as Color,

            // ====================== 按钮类 ======================
            bgButtonColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_button
            ) as Color,
            bgButtonLowlightColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_bg_button_lowlight
            ) as Color,

            // ====================== 图标类 ======================
            iconPrimaryColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_icon_primary
            ) as Color,
            iconSecondaryColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_icon_secondary
            ) as Color,
            iconSettingColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_icon_setting
            ) as Color,

            // ====================== 分隔线 / 描边类 ======================
            dividerColor = SkinComposeHelper.getSkinColor(context, R.color.color_divider) as Color,
            controlDividerColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_control_divider
            ) as Color,
            outlineColor = SkinComposeHelper.getSkinColor(context, R.color.color_outline) as Color,
            outlineLowContrastColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_outline_low_contrast
            ) as Color,

            // ====================== 帖子专用 ======================
            postTextPrimaryColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_post_text_primary
            ) as Color,
            postTextTitleColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_post_text_title
            ) as Color,
            postTextHintColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_post_text_hint
            ) as Color,
            postTextReplyColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_post_text_reply
            ) as Color,
            postHintBarColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_post_hint_bar
            ) as Color,

            // ====================== 功能 / 状态色 ======================
            loadingColor = SkinComposeHelper.getSkinColor(context, R.color.color_loading) as Color,
            titleTopColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_title_top
            ) as Color,
            commonColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_primary_blue
            ) as Color,
            tabRippedColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_tab_ripped
            ) as Color,
            errorRedColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_error_red
            ) as Color,
            successGreenColor = SkinComposeHelper.getSkinColor(
                context,
                R.color.color_success_green
            ) as Color
        )
    }

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