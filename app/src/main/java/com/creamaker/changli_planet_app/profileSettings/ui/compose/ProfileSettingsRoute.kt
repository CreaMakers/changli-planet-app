package com.creamaker.changli_planet_app.profileSettings.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.common.data.local.mmkv.StudentInfoManager
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.profileSettings.ui.model.SettingItemUiModel

private const val FEI_SHU_URL =
    "https://creamaker.feishu.cn/share/base/form/shrcn6LjBK78JLJfLeKDMe3hczd?chunked=false"

private const val CREAMAKER_URL=  "planet.zhelearn.com"

@Composable
fun ProfileSettingsRoute(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBound = remember { StudentInfoManager.studentId.isNotBlank() }
    val username = if (isBound) {
        UserInfoManager.account.takeIf { it.isNotBlank() } ?: StudentInfoManager.studentId
    } else {
        "长理学子~"
    }
    val avatarUrl = UserInfoManager.userAvatar
    var showCacheDialog by remember { mutableStateOf(false) }

    ProfileSettingsScreen(
        modifier = modifier,
        items = createSettingItems(),
        username = username,
        avatarData = avatarUrl,
        isBound = isBound,
        onItemClick = { item ->
            if (item.id == "4") {
                showCacheDialog = true
            } else {
                handleSettingItemClick(context, item)
            }
        },
        onPrimaryClick = {
            // 切换学号 / 绑定学号均直接跳转绑定页，旧数据等绑定成功后再清理
            if (isBound) {
                Route.goBindingUser(context, isSwitchAccount = true)
            } else {
                Route.goBindingUser(context)
            }
        }
    )

    if (showCacheDialog) {
        ConfirmDialog(
            title = "将清除实用工具的所有缓存",
            content = "确定要清除缓存嘛₍ᐢ.ˬ.⑅ᐢ₎",
            onDismiss = { showCacheDialog = false },
            onConfirm = {
                PlanetApplication.clearContentCache()
                showCacheDialog = false
            }
        )
    }
}

@Composable
private fun ProfileSettingsScreen(
    items: List<SettingItemUiModel>,
    username: String,
    avatarData: Any?,
    isBound: Boolean,
    onItemClick: (SettingItemUiModel.Option) -> Unit,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 结构：外层滚动容器 + 单个 ConstraintLayout。
    // 头图 / 头像 / 用户名 / 设置卡片 四个子项用约束彼此锚定，
    // 其中设置卡片顶边约束在"用户名底部 + 20dp"——由约束保证永远不会挡到用户名。
    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.bgPrimaryColor)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        val (bgImage, avatar, usernameText, settingsCard) = createRefs()

        Image(
            painter = painterResource(id = R.drawable.ic_profile_home_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .constrainAs(bgImage) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarData)
                .crossfade(true)
                .placeholder(R.drawable.ic_fulilian)
                .error(R.drawable.ic_error_vector)
                .build(),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(3.dp, AppTheme.colors.outlineLowContrastColor, CircleShape)
                .constrainAs(avatar) {
                    top.linkTo(parent.top, margin = 70.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = username,
            color = AppTheme.colors.primaryTextColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.constrainAs(usernameText) {
                top.linkTo(avatar.bottom, margin = 12.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        SettingsCard(
            items = items,
            isBound = isBound,
            onItemClick = onItemClick,
            onPrimaryClick = onPrimaryClick,
            modifier = Modifier.constrainAs(settingsCard) {
                top.linkTo(usernameText.bottom, margin = 20.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )
    }
}

@Composable
private fun SettingsCard(
    items: List<SettingItemUiModel>,
    isBound: Boolean,
    onItemClick: (SettingItemUiModel.Option) -> Unit,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = AppTheme.colors.bgCardColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            items.forEach { item ->
                when (item) {
                    is SettingItemUiModel.Header -> SettingHeaderItem(item)
                    is SettingItemUiModel.Option -> {
                        SettingOptionItem(item = item, onClick = { onItemClick(item) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onPrimaryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.bgButtonLowlightColor,
                    contentColor = AppTheme.colors.functionalTextColor
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = if (isBound) "切换学号" else "绑定学号",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingHeaderItem(item: SettingItemUiModel.Header) {
    Text(
        text = item.title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colors.greyTextColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingOptionItem(
    item: SettingItemUiModel.Option,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        item.iconResId?.takeIf { it != 0 }?.let { iconResId ->
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 4.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
        }

        Text(
            text = item.title,
            fontSize = 16.sp,
            color = AppTheme.colors.primaryTextColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        containerColor = AppTheme.colors.bgCardColor,
        titleContentColor = AppTheme.colors.primaryTextColor,
        textContentColor = AppTheme.colors.greyTextColor,
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = { Text(text = content, fontSize = 16.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定", color = AppTheme.colors.functionalTextColor, fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppTheme.colors.greyTextColor, fontSize = 16.sp)
            }
        }
    )
}

private fun handleSettingItemClick(
    context: android.content.Context,
    item: SettingItemUiModel.Option
) {
    when (item.id) {
        "5" -> Route.goBindingUser(context)
        "6" -> Route.goSkinSecletion(context)
        "9" -> Route.goWebView(context, CREAMAKER_URL)
        "10" -> Route.goWebView(context, FEI_SHU_URL)
    }
}

private fun createSettingItems(): List<SettingItemUiModel> = listOf(
    SettingItemUiModel.Header("主要设置"),
    SettingItemUiModel.Option("4", "清除缓存", R.drawable.qingchu),
    SettingItemUiModel.Option("5", "绑定学号", R.drawable.ic_bianji),
    SettingItemUiModel.Option("6", "主题设置", R.drawable.zhuti_tiaosepan),
    SettingItemUiModel.Option("9", "关于我们", R.drawable.ic_guanyuwomen),
    SettingItemUiModel.Option("10", "意见反馈", R.drawable.yijianfankui)
)

@Preview
@Composable
private fun ProfileSettingsRoutePreview() {
    AppSkinTheme {
        ProfileSettingsScreen(
            items = createSettingItems(),
            username = "长理学子~",
            avatarData = null,
            isBound = false,
            onItemClick = {},
            onPrimaryClick = {}
        )
    }
}
