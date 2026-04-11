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
    val bottomBarGlassColor: Color = Color(0xFFEFF2F6).copy(alpha = 0.84f),
    val bottomBarGlassEdgeColor: Color = Color(0xFFF7FAFF),

    // 帖子专用
    val postTextPrimaryColor: Color = DesignColors.PostTextPrimary,
    val postTextTitleColor: Color = DesignColors.PostTextTitle,
    val postTextHintColor: Color = DesignColors.PostTextHint,
    val postTextReplyColor: Color = DesignColors.PostTextReply,
    val postHintBarColor: Color = DesignColors.PostHintBar,

    // 状态/其他
    val tabRippedColor: Color = DesignColors.TabRipped,
    val errorRedColor: Color = DesignColors.ErrorRed,
    val successGreenColor: Color = DesignColors.SuccessGreen,

    // Overview
    val overviewPageBackgroundColor: Color = Color(0xFFF3F3F6),
    val overviewPromptBackgroundColor: Color = Color(0xFFE8EDF8),
    val overviewPromptTextColor: Color = Color(0xFF3D7AF7),
    val overviewScoreIconBackgroundColor: Color = Color(0xFFE2F4E6),
    val overviewElectricIconBackgroundColor: Color = Color(0xFFF7EFCB),
    val overviewExamBadgeBackgroundColor: Color = Color(0xFFF7E2CC),
    val overviewExamBadgeTextColor: Color = Color(0xFFD98A2B),
    val overviewUrgentBorderColor: Color = Color(0xFFE96A62),
    val overviewUrgentBackgroundColor: Color = Color(0xFFFFF4F2),

    // Notion-inspired design tokens (legacy)
    val notionBlackColor: Color = DesignColors.NotionBlack,
    val notionBlueColor: Color = DesignColors.NotionBlue,
    val notionBlueActiveColor: Color = DesignColors.NotionBlueActive,
    val notionBlueFocusColor: Color = DesignColors.NotionBlueFocus,
    val notionWarmWhiteColor: Color = DesignColors.NotionWarmWhite,
    val notionWarmDarkColor: Color = DesignColors.NotionWarmDark,
    val notionWarmGray500Color: Color = DesignColors.NotionWarmGray500,
    val notionWarmGray300Color: Color = DesignColors.NotionWarmGray300,
    val notionWhisperBorderColor: Color = DesignColors.NotionWhisperBorder,
    val notionBadgeBgColor: Color = DesignColors.NotionBadgeBg,
    val notionBadgeTextColor: Color = DesignColors.NotionBadgeText,
    val notionTealColor: Color = DesignColors.NotionTeal,
    val notionOrangeColor: Color = DesignColors.NotionOrange,

    // Stripe-inspired design tokens (legacy)
    val stripePurpleColor: Color = DesignColors.StripePurple,
    val stripePurpleHoverColor: Color = DesignColors.StripePurpleHover,
    val stripePurpleLightColor: Color = DesignColors.StripePurpleLight,
    val stripePurpleSoftColor: Color = DesignColors.StripePurpleSoft,
    val stripeDeepNavyColor: Color = DesignColors.StripeDeepNavy,
    val stripeSlateLabelColor: Color = DesignColors.StripeSlateLabel,
    val stripeSlateBodyColor: Color = DesignColors.StripeSlateBody,
    val stripeBorderDefaultColor: Color = DesignColors.StripeBorderDefault,
    val stripeBrandDarkColor: Color = DesignColors.StripeBrandDark,
    val stripeRubyColor: Color = DesignColors.StripeRuby,
    val stripeMagentaColor: Color = DesignColors.StripeMagenta,
    val stripeMagentaLightColor: Color = DesignColors.StripeMagentaLight,
    val stripeShadowBlueColor: Color = DesignColors.StripeShadowBlue,

    // Campus Fresh design tokens (清新文艺风)
    val campusSkyBlueColor: Color = DesignColors.CampusSkyBlue,
    val campusSkyBlueHoverColor: Color = DesignColors.CampusSkyBlueHover,
    val campusSkyBlueLightColor: Color = DesignColors.CampusSkyBlueLight,
    val campusSkyBlueGhostColor: Color = DesignColors.CampusSkyBlueGhost,
    val campusMintColor: Color = DesignColors.CampusMint,
    val campusMintLightColor: Color = DesignColors.CampusMintLight,
    val campusMintDeepColor: Color = DesignColors.CampusMintDeep,
    val campusInkColor: Color = DesignColors.CampusInk,
    val campusSlateColor: Color = DesignColors.CampusSlate,
    val campusMistColor: Color = DesignColors.CampusMist,
    val campusCloudColor: Color = DesignColors.CampusCloud,
    val campusSnowColor: Color = DesignColors.CampusSnow,
    val campusAmberColor: Color = DesignColors.CampusAmber,
    val campusCoralColor: Color = DesignColors.CampusCoral,
    val campusSageColor: Color = DesignColors.CampusSage,
    val campusLavenderColor: Color = DesignColors.CampusLavender,
    val campusDividerColor: Color = DesignColors.CampusDivider
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
    bottomBarGlassColor = Color(0xFFEFF2F6).copy(alpha = 0.84f),
    bottomBarGlassEdgeColor = Color(0xFFF7FAFF),
    postTextPrimaryColor = DesignColors.PostTextPrimary,
    postTextTitleColor = DesignColors.PostTextTitle,
    postTextHintColor = DesignColors.PostTextHint,
    postTextReplyColor = DesignColors.PostTextReply,
    postHintBarColor = DesignColors.PostHintBar,
    tabRippedColor = DesignColors.TabRipped,
    errorRedColor = DesignColors.ErrorRed,
    successGreenColor = DesignColors.SuccessGreen,
    overviewPageBackgroundColor = Color(0xFFF3F3F6),
    overviewPromptBackgroundColor = Color(0xFFE8EDF8),
    overviewPromptTextColor = Color(0xFF3D7AF7),
    overviewScoreIconBackgroundColor = Color(0xFFE2F4E6),
    overviewElectricIconBackgroundColor = Color(0xFFF7EFCB),
    overviewExamBadgeBackgroundColor = Color(0xFFF7E2CC),
    overviewExamBadgeTextColor = Color(0xFFD98A2B),
    overviewUrgentBorderColor = Color(0xFFE96A62),
    overviewUrgentBackgroundColor = Color(0xFFFFF4F2),
    notionBlackColor = DesignColors.NotionBlack,
    notionBlueColor = Color(0xFF0099FA),           // Campus blue override
    notionBlueActiveColor = DesignColors.NotionBlueActive,
    notionBlueFocusColor = DesignColors.NotionBlueFocus,
    notionWarmWhiteColor = DesignColors.NotionWarmWhite,
    notionWarmDarkColor = DesignColors.NotionWarmDark,
    notionWarmGray500Color = DesignColors.NotionWarmGray500,
    notionWarmGray300Color = DesignColors.NotionWarmGray300,
    notionWhisperBorderColor = DesignColors.NotionWhisperBorder,
    notionBadgeBgColor = DesignColors.NotionBadgeBg,
    notionBadgeTextColor = DesignColors.NotionBadgeText,
    notionTealColor = DesignColors.NotionTeal,
    notionOrangeColor = DesignColors.NotionOrange,
    stripePurpleColor = DesignColors.StripePurple,
    stripePurpleHoverColor = DesignColors.StripePurpleHover,
    stripePurpleLightColor = DesignColors.StripePurpleLight,
    stripePurpleSoftColor = DesignColors.StripePurpleSoft,
    stripeDeepNavyColor = DesignColors.StripeDeepNavy,
    stripeSlateLabelColor = DesignColors.StripeSlateLabel,
    stripeSlateBodyColor = DesignColors.StripeSlateBody,
    stripeBorderDefaultColor = DesignColors.StripeBorderDefault,
    stripeBrandDarkColor = DesignColors.StripeBrandDark,
    stripeRubyColor = DesignColors.StripeRuby,
    stripeMagentaColor = DesignColors.StripeMagenta,
    stripeMagentaLightColor = DesignColors.StripeMagentaLight,
    stripeShadowBlueColor = DesignColors.StripeShadowBlue,
    campusSkyBlueColor = DesignColors.CampusSkyBlue,
    campusSkyBlueHoverColor = DesignColors.CampusSkyBlueHover,
    campusSkyBlueLightColor = DesignColors.CampusSkyBlueLight,
    campusSkyBlueGhostColor = DesignColors.CampusSkyBlueGhost,
    campusMintColor = DesignColors.CampusMint,
    campusMintLightColor = DesignColors.CampusMintLight,
    campusMintDeepColor = DesignColors.CampusMintDeep,
    campusInkColor = DesignColors.CampusInk,
    campusSlateColor = DesignColors.CampusSlate,
    campusMistColor = DesignColors.CampusMist,
    campusCloudColor = DesignColors.CampusCloud,
    campusSnowColor = DesignColors.CampusSnow,
    campusAmberColor = DesignColors.CampusAmber,
    campusCoralColor = DesignColors.CampusCoral,
    campusSageColor = DesignColors.CampusSage,
    campusLavenderColor = DesignColors.CampusLavender,
    campusDividerColor = DesignColors.CampusDivider
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
    bottomBarGlassColor = Color(0xFF616772).copy(alpha = 0.92f),
    bottomBarGlassEdgeColor = Color(0xFFA7AFBC),
    postTextPrimaryColor = Color(0xFFF8F8F8),
    postTextTitleColor = Color(0xFF000000),
    postTextHintColor = Color(0xFFABABAB),
    postTextReplyColor = Color(0xFF1E4B94),
    postHintBarColor = Color(0xFF0997F8),
    tabRippedColor = Color(0xFFF2F2F2),
    errorRedColor = Color(0xFFFF3D00),
    successGreenColor = Color(0xFF00C853),
    overviewPageBackgroundColor = Color(0xFF000000),
    overviewPromptBackgroundColor = Color(0xFF272C32),
    overviewPromptTextColor = Color(0xFF4F7FED),
    overviewScoreIconBackgroundColor = Color(0xFF1E3522),
    overviewElectricIconBackgroundColor = Color(0xFF3B371F),
    overviewExamBadgeBackgroundColor = Color(0xFF4A3621),
    overviewExamBadgeTextColor = Color(0xFFFFC980),
    overviewUrgentBorderColor = Color(0xFFE96A62),
    overviewUrgentBackgroundColor = Color(0xFF2B1B1A),
    notionBlackColor = Color(0xF2FFFFFF),         // Near-white for dark mode
    notionBlueColor = Color(0xFF4FC3F7),           // Brighter campus blue for dark
    notionBlueActiveColor = Color(0xFF039BE5),
    notionBlueFocusColor = Color(0xFF42A5F5),
    notionWarmWhiteColor = Color(0xFF1A1918),       // Warm dark surface
    notionWarmDarkColor = Color(0xFFE8E6E3),       // Light text for dark bg
    notionWarmGray500Color = Color(0xFFA39E98),
    notionWarmGray300Color = Color(0xFF615D59),
    notionWhisperBorderColor = Color(0x1AFFFFFF),   // 10% white
    notionBadgeBgColor = Color(0xFF0D2744),
    notionBadgeTextColor = Color(0xFF64B5F6),
    notionTealColor = Color(0xFF4DB6AC),
    notionOrangeColor = Color(0xFFFF9800),
    stripePurpleColor = Color(0xFF7B6FFD),
    stripePurpleHoverColor = Color(0xFF6658E8),
    stripePurpleLightColor = Color(0xFF4A4680),
    stripePurpleSoftColor = Color(0xFF363366),
    stripeDeepNavyColor = Color(0xFFE8ECF4),
    stripeSlateLabelColor = Color(0xFFC4D0E0),
    stripeSlateBodyColor = Color(0xFF94A3B8),
    stripeBorderDefaultColor = Color(0xFF2A2D5E),
    stripeBrandDarkColor = Color(0xFF0D0F2E),
    stripeRubyColor = Color(0xFFFF5A8A),
    stripeMagentaColor = Color(0xFFD946EF),
    stripeMagentaLightColor = Color(0xFF4A1942),
    stripeShadowBlueColor = Color(0xFF1A1A3E),
    campusSkyBlueColor = Color(0xFF7BB8E8),
    campusSkyBlueHoverColor = Color(0xFF6AA8D8),
    campusSkyBlueLightColor = Color(0xFF1A2A3A),
    campusSkyBlueGhostColor = Color(0xFF1A2230),
    campusMintColor = Color(0xFF8AD4C8),
    campusMintLightColor = Color(0xFF1A2E2A),
    campusMintDeepColor = Color(0xFF6EB8AC),
    campusInkColor = Color(0xFFE8EAF0),
    campusSlateColor = Color(0xFF8A92A6),
    campusMistColor = Color(0xFF5A6272),
    campusCloudColor = Color(0xFF1A1C20),
    campusSnowColor = Color(0xFF12141A),
    campusAmberColor = Color(0xFFF0B86A),
    campusCoralColor = Color(0xFFE88A7A),
    campusSageColor = Color(0xFF8DD492),
    campusLavenderColor = Color(0xFFB5AAE8),
    campusDividerColor = Color(0x1AFFFFFF)
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
