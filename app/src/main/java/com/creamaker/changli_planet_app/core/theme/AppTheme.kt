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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable

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

@Composable
fun AppSkinTheme(
    content: @Composable () -> Unit
) {
    val isInPreview = LocalInspectionMode.current

    var skinVersion by remember { mutableIntStateOf(0) }

    DisposableEffect(isInPreview) {
        if (isInPreview) {
            onDispose { }
        } else {
            val listener = object : SkinSupportable {
                override fun applySkin() {
                    skinVersion++
                }
            }
            runCatching {
                SkinManager.attach(listener)
            }
            onDispose {
                runCatching {
                    SkinManager.detach(listener)
                }
            }
        }
    }

    // 强制依赖 skinVersion，收到换肤回调后重组
    @Suppress("UNUSED_VARIABLE")
    val observeSkinVersion = skinVersion

    val currentColors =
        SkinColors(
            // ====================== 文字类 ======================
            primaryTextColor = colorResource(id = R.color.color_text_primary),
            greyTextColor = colorResource(id = R.color.color_text_grey),
            secondaryTextColor = colorResource(id = R.color.color_text_secondary),
            textHeighLightColor = colorResource(id = R.color.color_text_highlight),
            functionalTextColor = colorResource(id = R.color.color_text_functional),
            widgetPrimaryTextColor = colorResource(id = R.color.color_text_widget_primary),
            disabledTextColor = colorResource(id = R.color.color_text_disabled),
            textButtonColor = colorResource(id = R.color.color_text_button),
            searchHintColor = colorResource(id = R.color.color_search_hint),

            // ====================== 背景类 ======================
            bgPrimaryColor = colorResource(id = R.color.color_bg_primary),
            bgSecondaryColor = colorResource(id = R.color.color_bg_secondary),
            bgSecondaryInverseColor = colorResource(id = R.color.color_bg_secondary_inverse),
            bgCardColor = colorResource(id = R.color.color_bg_card),
            bgCardHighContrastColor = colorResource(id = R.color.color_bg_card_high_contrast),
            bgTopBarColor = colorResource(id = R.color.color_bg_top_bar),
            bgRecyclerViewColor = colorResource(id = R.color.color_bg_recycler_view),
            bgLightGrayColor = colorResource(id = R.color.light_gray),

            // ====================== 按钮类 ======================
            bgButtonColor = colorResource(id = R.color.color_bg_button),
            bgButtonLowlightColor = colorResource(id = R.color.color_bg_button_lowlight),

            // ====================== 图标类 ======================
            iconPrimaryColor = colorResource(id = R.color.color_icon_primary),
            iconSecondaryColor = colorResource(id = R.color.color_icon_secondary),
            iconSettingColor = colorResource(id = R.color.color_icon_setting),

            // ====================== 分隔线 / 描边类 ======================
            dividerColor = colorResource(id = R.color.color_divider),
            controlDividerColor = colorResource(id = R.color.color_control_divider),
            outlineColor = colorResource(id = R.color.color_outline),
            outlineLowContrastColor = colorResource(id = R.color.color_outline_low_contrast),

            // ====================== 帖子专用 ======================
            postTextPrimaryColor = colorResource(id = R.color.color_post_text_primary),
            postTextTitleColor = colorResource(id = R.color.color_post_text_title),
            postTextHintColor = colorResource(id = R.color.color_post_text_hint),
            postTextReplyColor = colorResource(id = R.color.color_post_text_reply),
            postHintBarColor = colorResource(id = R.color.color_post_hint_bar),

            // ====================== 功能 / 状态色 ======================
            loadingColor = colorResource(id = R.color.color_loading),
            titleTopColor = colorResource(id = R.color.color_title_top),
            commonColor = colorResource(id = R.color.color_primary_blue),
            tabRippedColor = colorResource(id = R.color.color_tab_ripped),
            errorRedColor = colorResource(id = R.color.color_error_red),
            successGreenColor = colorResource(id = R.color.color_success_green)
        )

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