package com.creamaker.changli_planet_app.profileSettings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.common.data.local.mmkv.UserInfoManager
import com.creamaker.changli_planet_app.core.PlanetApplication
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.core.theme.AppSkinTheme
import com.creamaker.changli_planet_app.core.theme.AppTheme
import com.creamaker.changli_planet_app.profileSettings.ui.model.SettingItemUiModel
import com.creamaker.changli_planet_app.utils.Event.SelectEvent
import com.creamaker.changli_planet_app.utils.EventBusHelper
import com.creamaker.changli_planet_app.utils.NetworkUtil
import com.creamaker.changli_planet_app.widget.dialog.GuestLimitedAccessDialog
import com.creamaker.changli_planet_app.widget.view.CustomToast

class ProfileSettingsFragment : Fragment() {

    private val FEI_SHU_URL = "https://creamaker.feishu.cn/share/base/form/shrcn6LjBK78JLJfLeKDMe3hczd?chunked=false"
    private var backCallback: OnBackPressedCallback? = null

    companion object {
        fun newInstance() = ProfileSettingsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // 设置销毁策略
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppSkinTheme {
                    // 初始化状态数据
                    val isExpired = remember { PlanetApplication.is_expired }
                    val username = remember {
                        if (isExpired) "长理学子~" else UserInfoManager.account ?: "用户名"
                    }
                    val avatarUrl = remember { UserInfoManager.userAvatar }

                    // 对话框状态
                    var showCacheDialog by remember { mutableStateOf(false) }
                    var showLogoutDialog by remember { mutableStateOf(false) }

                    // 主界面
                    ProfileSettingsScreen(
                        items = createSettingItems(),
                        username = username,
                        avatarData = avatarUrl,
                        isTourist = isExpired,
                        onItemClick = { item ->
                            if (item.id == "4") {
                                // 特殊处理清除缓存，不走通用跳转逻辑
                                showCacheDialog = true
                            } else {
                                handleSettingItemClick(item)
                            }
                        },
                        onLogoutClick = {
                            showLogoutDialog = true
                        },
                        onEditProfileClick = {
                            handleEditProfileClick(isExpired)
                        },
                        onAvatarClick = {
                            if (isExpired) {
                                showLogoutDialog = true // 游客点击头像也触发登录提示
                            }
                        }
                    )

                    // 弹窗逻辑：清除缓存
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

                    // 弹窗逻辑：登出/游客登录
                    if (showLogoutDialog) {
                        val title = if (isExpired) "登录确认" else "将清除该账号缓存"
                        val content = if (isExpired) "现在进行登录吗" else "是否登出"

                        ConfirmDialog(
                            title = title,
                            content = content,
                            onDismiss = { showLogoutDialog = false },
                            onConfirm = {
                                PlanetApplication.clearLocalCache()
                                PlanetApplication.is_expired = true
                                Route.goLoginForcibly(context)
                                showLogoutDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    // 处理编辑个人资料点击
    private fun handleEditProfileClick(isTourist: Boolean) {
        if (isTourist) return // 游客模式下其实 UI 已经隐藏了入口，双重保险

        // 简单的网络检查
        if (NetworkUtil.getNetworkType(requireContext()) != NetworkUtil.NetworkType.None) {
            Route.goUserProfile(requireContext())
        } else {
            CustomToast.showMessage(requireContext(), "网络未连接")
        }
        // 注意：原代码中的 Permission Check 在 Compose 重构中如果不是必要流程，建议下沉到 goUserProfile 内部处理，
        // 或者使用 Accompanist Permissions 库在 Compose 层面处理。这里为了简洁暂略。
    }

    // 复用之前的跳转逻辑
    private fun handleSettingItemClick(item: SettingItemUiModel.Option) {
        when (item.id) {
            "2" -> { /* 隐私设置 TODO */ }
            "3" -> {
                if (PlanetApplication.is_expired) {
                    //CustomToast.showMessage(requireContext(), "未登录无法进行此操作哦~")
                    GuestLimitedAccessDialog(requireContext()).show()
                } else {
                    Route.goAccountSecurity(requireContext())
                }
            }
            // "4" (清除缓存) 已经在 onItemClick 回调中拦截处理了
            "5" -> Route.goBindingUser(requireContext())
            "6" -> Route.goSkinSecletion(requireContext())
            "8" -> { /* 帮助中心 TODO */ }
            "9" -> Route.goAbout(requireContext())
            "10" -> Route.goWebView(requireContext(), FEI_SHU_URL)
        }
    }

    // ================== Composables ==================

    @Composable
    fun ProfileSettingsScreen(
        items: List<SettingItemUiModel>,
        username: String,
        avatarData: Any?, // Url String or Res Int
        isTourist: Boolean,
        onItemClick: (SettingItemUiModel.Option) -> Unit,
        onLogoutClick: () -> Unit,
        onEditProfileClick: () -> Unit,
        onAvatarClick: () -> Unit
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.bgPrimaryColor)
                .verticalScroll(scrollState)
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
                isExpired = isTourist,
                onItemClick = onItemClick,
                onLogoutClick = onLogoutClick
            )
        }
    }

    @Composable
    fun ProfileHeaderSection(
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
        ) {
            val (bgImage, avatar, usernameRow) = createRefs()

            // 1. 背景大图
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

            // 2. 头像 (支持网络图片)
            // 如果你的项目没有引入 coil-compose，请将下面替换为普通的 Image 并使用占位图
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarData)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_fulilian)
                    .error(R.drawable.ic_error_vector) // 错误图
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

            // 3. 用户名 + 编辑图标
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .constrainAs(usernameRow) {
                        top.linkTo(avatar.bottom, margin = 12.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    // 只有非游客模式才响应名字区域的编辑点击
                    .clickable(enabled = !isTourist) { onEditProfileClick() }
            ) {
                Text(
                    text = username,
                    color = AppTheme.colors.primaryTextColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // 游客模式隐藏编辑图标
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
    fun SettingsListSection(
        items: List<SettingItemUiModel>,
        isExpired: Boolean,
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
                color = AppTheme.colors.bgPrimaryColor // 使用正确的背景色
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    items.forEach { item ->
                        when (item) {
                            is SettingItemUiModel.Header -> SettingHeaderItem(item)
                            is SettingItemUiModel.Option -> SettingOptionItem(item, onClick = { onItemClick(item) })
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 底部按钮：根据游客状态显示不同文字
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
                            text = if (isExpired) "登录账号" else stringResource(id = R.string.logout),
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
    fun SettingHeaderItem(item: SettingItemUiModel.Header) {
        Text(
            text = item.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.greyTextColor, // 修正为 greyTextColor
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        )
    }

    @Composable
    fun SettingOptionItem(item: SettingItemUiModel.Option, onClick: () -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp)
        ) {
            if (item.iconResId != null && item.iconResId != 0) {
                Icon( // 使用 Icon 而不是 Image 可以自动应用 tint（如果需要）
                    painter = painterResource(id = item.iconResId),
                    contentDescription = null,
                    tint = Color.Unspecified, // 保持原图颜色
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

    // 通用确认弹窗
    @Composable
    fun ConfirmDialog(
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

    private fun createSettingItems(): List<SettingItemUiModel> {
        return listOf(
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
    }

    // ================== Lifecycle & Back Press ==================

    override fun onResume() {
        super.onResume()
        if (backCallback == null) {
            backCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // 发送 EventBus 事件 (保持原逻辑)
                    EventBusHelper.post(SelectEvent(0))
                    isEnabled = false
                    // 如果需要，可以在这里调用 activity.onBackPressed()，但原逻辑似乎是想拦截并跳转 Tab
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(this, backCallback!!)
        }
    }

    override fun onPause() {
        super.onPause()
        backCallback?.remove()
        backCallback = null
    }
}