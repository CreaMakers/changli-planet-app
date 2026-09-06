package com.zhelearn.CSUSTPlanet.overview.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownCheckBox
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownAnnotatorConfig
import com.mikepenz.markdown.model.markdownPadding
import com.zhelearn.CSUSTPlanet.R
import com.zhelearn.CSUSTPlanet.core.theme.AppTheme
import com.zhelearn.CSUSTPlanet.overview.data.OverviewRepository
import com.zhelearn.CSUSTPlanet.overview.ui.model.OverviewAnnouncementUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnnouncementScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember(context) { OverviewRepository(context) }
    var announcements by remember { mutableStateOf(emptyList<OverviewAnnouncementUiModel>()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(repository, lifecycleOwner) {
        announcements = repository.loadCachedAnnouncements()
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            announcements = repository.refreshAnnouncements()
            isLoading = false
        }
    }

    AnnouncementList(announcements = announcements, isLoading = isLoading)
}

@Composable
private fun AnnouncementList(
    announcements: List<OverviewAnnouncementUiModel>,
    isLoading: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("yyyy年M月d日 · HH:mm", Locale.getDefault()) }
    val colors = AppTheme.colors
    val bodyStyle = TextStyle(color = colors.secondaryTextColor, fontSize = 14.sp, lineHeight = 23.sp)
    val headingStyle = bodyStyle.copy(color = colors.primaryTextColor, fontWeight = FontWeight.Bold)
    val markdownTextStyles = markdownTypography(
        h1 = headingStyle.copy(fontSize = 20.sp, lineHeight = 29.sp),
        h2 = headingStyle.copy(fontSize = 18.sp, lineHeight = 27.sp),
        h3 = headingStyle.copy(fontSize = 16.sp, lineHeight = 25.sp),
        h4 = headingStyle.copy(fontSize = 16.sp, lineHeight = 25.sp),
        h5 = headingStyle.copy(fontSize = 15.sp),
        h6 = headingStyle,
        text = bodyStyle,
        paragraph = bodyStyle,
        ordered = bodyStyle,
        bullet = bodyStyle,
        list = bodyStyle,
        quote = bodyStyle,
        code = bodyStyle.copy(fontFamily = FontFamily.Monospace),
        inlineCode = TextStyle(fontFamily = FontFamily.Monospace),
        table = bodyStyle,
        alertTitle = bodyStyle.copy(fontWeight = FontWeight.Bold),
        textLink = TextLinkStyles(
            style = SpanStyle(
                color = colors.overviewPromptTextColor,
                textDecoration = TextDecoration.Underline
            )
        )
    )
    // 保留已有纯文本公告的单行换行习惯。
    val announcementAnnotator = remember {
        markdownAnnotator(config = markdownAnnotatorConfig(eolAsNewLine = true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overviewPageBackgroundColor)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.intel_station),
                    color = colors.primaryTextColor,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
        if (announcements.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = colors.bgCardColor
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = colors.overviewPromptTextColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = colors.overviewPromptBackgroundColor
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.nnews),
                                    contentDescription = null,
                                    tint = colors.overviewPromptTextColor,
                                    modifier = Modifier.padding(16.dp).size(28.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isLoading) "正在加载公告" else "暂无公告",
                            color = colors.primaryTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!isLoading) {
                            Text(
                                text = "新的通知会在这里与你见面",
                                color = colors.secondaryTextColor,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
        itemsIndexed(announcements, key = { _, announcement -> announcement.id }) { index, announcement ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = colors.bgCardColor
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = announcement.createdAtMillis?.let { dateFormat.format(Date(it)) } ?: "公告",
                            color = colors.secondaryTextColor,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (index == 0 && announcement.createdAtMillis != null) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = colors.overviewPromptBackgroundColor
                            ) {
                                Text(
                                    text = "最新",
                                    color = colors.overviewPromptTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = announcement.title,
                        color = colors.primaryTextColor,
                        fontSize = 18.sp,
                        lineHeight = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (announcement.content.isNotBlank()) {
                        Markdown(
                            content = announcement.content,
                            modifier = Modifier.fillMaxWidth(),
                            colors = markdownColor(
                                text = colors.secondaryTextColor,
                                codeBackground = colors.overviewPageBackgroundColor,
                                dividerColor = colors.dividerColor,
                                tableBackground = colors.overviewPageBackgroundColor
                            ),
                            components = markdownComponents(checkbox = { component ->
                                MarkdownCheckBox(
                                    content = component.content,
                                    node = component.node,
                                    style = component.typography.text,
                                    checkedIndicator = { checked, modifier ->
                                        val checkboxColor = if (checked) colors.overviewPromptTextColor else colors.secondaryTextColor
                                        Box(
                                            modifier = modifier
                                                .size(16.dp)
                                                .background(
                                                    if (checked) checkboxColor else Color.Transparent,
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .border(1.5.dp, checkboxColor, RoundedCornerShape(3.dp))
                                                .semantics {
                                                    role = Role.Checkbox
                                                    toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
                                                }
                                        ) {
                                            if (checked) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.fillMaxSize().padding(2.dp)
                                                )
                                            }
                                        }
                                    }
                                )
                            }),
                            typography = markdownTextStyles,
                            padding = markdownPadding(
                                block = 6.dp,
                                listItemTop = 2.dp,
                                listItemBottom = 2.dp,
                                listIndent = 16.dp
                            ),
                            animations = markdownAnimations(animateTextSize = { this }),
                            annotator = announcementAnnotator,
                            imageTransformer = Coil3ImageTransformerImpl,
                            error = { modifier ->
                                Text(text = announcement.content, modifier = modifier, style = bodyStyle)
                            }
                        )
                    }
                }
            }
        }
    }
}
