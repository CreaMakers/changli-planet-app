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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.profileSettings.ui.model.SettingItemUiModel
import com.creamaker.changli_planet_app.utils.NetworkUtil
import com.creamaker.changli_planet_app.widget.dialog.GuestLimitedAccessDialog
import com.creamaker.changli_planet_app.widget.view.CustomToast

private const val FEI_SHU_URL =
    "https://creamaker.feishu.cn/share/base/form/shrcn6LjBK78JLJfLeKDMe3hczd?chunked=false"

@Composable
fun ProfileSettingsRoute(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isExpired = PlanetApplication.isExpired
    val username = if (isExpired) "长理学子~" else UserInfoManager.account
    val avatarUrl = UserInfoManager.userAvatar
    var showCacheDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    ProfileSettingsScreen(
        modifier = modifier,
        items = createSettingItems(),
        username = username,
        avatarData = avatarUrl,
        isTourist = isExpired,
        onItemClick = { item ->
            if (item.id == "4") {
                showCacheDialog = true
            } else {
                handleSettingItemClick(context, item)
            }
        },
        onLogoutClick = { showLogoutDialog = true },
        onEditProfileClick = {
            if (isExpired) return@ProfileSettingsScreen
            if (NetworkUtil.getNetworkType(context) != NetworkUtil.NetworkType.None) {
                Route.goUserProfile(context)
            } else {
                CustomToast.showMessage(context, "网络未连接")
            }
        },
        onAvatarClick = {
            if (isExpired) {
                showLogoutDialog = true
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

    if (showLogoutDialog) {
        ConfirmDialog(
            title = if (isExpired) "登录确认" else "将清除该账号缓存",
            content = if (isExpired) "现在进行登录吗" else "是否登出",
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                PlanetApplication.clearLocalCache()
                PlanetApplication.isExpired = true
                Route.goLoginForcibly(context)
                showLogoutDialog = false
            }
        )
    }
}

@Composable
private fun ProfileSettingsScreen(
    items: List<SettingItemUiModel>,
    username: String,
    avatarData: Any?,
    isTourist: Boolean,
    onItemClick: (SettingItemUiModel.Option) -> Unit,
    onLogoutClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.bgPrimaryColor)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeaderSection(
            username = username,
            avatarData = avatarData,
            isTourist = isTourist,
            onEditProfileClick = onEditProfileClick,
            onAvatarClick = onAvatarClick
        )
        SettingsListSection(
            items = items,
            isTourist = isTourist,
            onItemClick = onItemClick,
            onLogoutClick = onLogoutClick
        )
    }
}

@Composable
private fun ProfileHeaderSection(
    username: String,
    avatarData: Any?,
    isTourist: Boolean,
    onEditProfileClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
    ) {
        val (bgImage, avatar, usernameRow) = createRefs()

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
                .clickable { onAvatarClick() }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .constrainAs(usernameRow) {
                    top.linkTo(avatar.bottom, margin = 12.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .clickable(enabled = !isTourist) { onEditProfileClick() }
        ) {
            Text(
                text = username,
                color = AppTheme.colors.primaryTextColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (!isTourist) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit_white),
                    contentDescription = "Edit",
                    tint = AppTheme.colors.iconPrimaryColor,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsListSection(
    items: List<SettingItemUiModel>,
    isTourist: Boolean,
    onItemClick: (SettingItemUiModel.Option) -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-30).dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = AppTheme.colors.bgPrimaryColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
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
                    onClick = onLogoutClick,
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
                        text = if (isTourist) "登录账号" else stringResource(id = R.string.logout),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
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
            Spacer(modifier = Modifier.width(12.dp))
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
        "3" -> {
            if (PlanetApplication.isExpired) {
                GuestLimitedAccessDialog(context).show()
            } else {
                Route.goAccountSecurity(context)
            }
        }

        "5" -> Route.goBindingUser(context)
        "6" -> Route.goSkinSecletion(context)
        "9" -> Route.goAbout(context)
        "10" -> Route.goWebView(context, FEI_SHU_URL)
    }
}

private fun createSettingItems(): List<SettingItemUiModel> = listOf(
    SettingItemUiModel.Header("主要设置"),
    SettingItemUiModel.Option("2", "隐私设置", R.drawable.yingsi),
    SettingItemUiModel.Option("3", "账号安全", R.drawable.zhanghao),
    SettingItemUiModel.Header("常用功能"),
    SettingItemUiModel.Option("4", "清除缓存", R.drawable.qingchu),
    SettingItemUiModel.Option("5", "绑定学号", R.drawable.ic_bianji),
    SettingItemUiModel.Option("6", "主题设置", R.drawable.zhuti_tiaosepan),
    SettingItemUiModel.Header("帮助与支持"),
    SettingItemUiModel.Option("8", "帮助中心", R.drawable.ic_help),
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
            isTourist = true,
            onItemClick = {},
            onLogoutClick = {},
            onEditProfileClick = {},
            onAvatarClick = {}
        )
    }
}
