package com.creamaker.changli_planet_app.feature.common.compose_ui

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics. Shader
import android. os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose. foundation.clickable
import androidx. compose.foundation.gestures.detectTapGestures
import androidx.compose. foundation.interaction.MutableInteractionSource
import androidx. compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose. material3.Text
import androidx.compose. runtime.*
import androidx.compose.ui. Alignment
import androidx.compose.ui. Modifier
import androidx.compose.ui. draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui. graphics.asComposeRenderEffect
import androidx.compose. ui.graphics.graphicsLayer
import androidx.compose.ui. input.pointer.pointerInput
import androidx. compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout. onGloballyPositioned
import androidx. compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform. LocalDensity
import androidx.compose. ui.res.painterResource
import androidx.compose. ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit. dp
import androidx. compose.ui.unit.sp
import androidx.compose.ui. window. Popup
import androidx. compose.ui.window.PopupProperties
import com.creamaker.changli_planet_app.core.theme.AppTheme
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 文件夹展开状态
 */
@Stable
class FolderExpandState {
    var isExpanded by mutableStateOf(false)
        private set

    var folderCenterInWindow by mutableStateOf(Offset.Zero)
        internal set

    var miniIconPositions by mutableStateOf<List<Offset>>(emptyList())
        internal set

    fun expand() { isExpanded = true }
    fun collapse() { isExpanded = false }
    fun toggle() { isExpanded = !isExpanded }
}

@Composable
fun rememberFolderExpandState(): FolderExpandState = remember { FolderExpandState() }

/**
 * 主功能区 + 可展开文件夹
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MainFunctionSection(
    mainItems: List<FunctionItemData>,
    moreItems: List<FunctionItemData>,
    onItemClick: (FunctionItemData) -> Unit,
    modifier: Modifier = Modifier
) {
    val folderState = rememberFolderExpandState()

    Column(modifier = modifier.fillMaxWidth()) {
        // 标题
        Text(
            text = "常用功能",
            color = AppTheme.colors. primaryTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
        )

        // 功能网格
        Column {
            // 第一行
            Row(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement. SpaceEvenly
            ) {
                mainItems.take(4).forEach { item ->
                    FunctionItem(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier. weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 第二行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                mainItems.drop(4).take(3).forEach { item ->
                    FunctionItem(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier. weight(1f)
                    )
                }

                MoreFolderButton(
                    items = moreItems,
                    isExpanded = folderState.isExpanded,
                    onClick = { folderState.toggle() },
                    onPositioned = { center, miniPositions ->
                        folderState.folderCenterInWindow = center
                        folderState.miniIconPositions = miniPositions
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (folderState. isExpanded) {
        ExpandedFolderPopup(
            state = folderState,
            items = moreItems,
            onItemClick = { item ->
                folderState.collapse()
                onItemClick(item)
            },
            onDismiss = { folderState.collapse() }
        )
    }
}

/**
 * 更多文件夹按钮 - 与其他功能项相同的布局结构
 */
@Composable
private fun MoreFolderButton(
    items: List<FunctionItemData>,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onPositioned: (center: Offset, miniIconPositions: List<Offset>) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val miniIconOffsets = remember { mutableStateListOf<Offset>() }
    var folderCenter by remember { mutableStateOf(Offset.Zero) }

    val scale by animateFloatAsState(
        targetValue = when {
            isExpanded -> 0.8f
            isPressed -> 0.92f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "folderScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 1f,
        animationSpec = tween(200),
        label = "folderAlpha"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            . clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 4. dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppTheme.colors. bgSecondaryColor)
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInWindow()
                    val size = coords.size
                    folderCenter = Offset(
                        pos.x + size.width / 2f,
                        pos.y + size. height / 2f
                    )
                    onPositioned(folderCenter, miniIconOffsets. toList())
                },
            contentAlignment = Alignment.Center
        ) {
            // 2x2 小图标预览
            Column(
                modifier = Modifier.padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3. dp)) {
                    items.getOrNull(0)?.let { item ->
                        MiniIconWithPosition(
                            item = item,
                            index = 0,
                            onPositioned = { pos ->
                                if (miniIconOffsets.size > 0) miniIconOffsets[0] = pos
                                else miniIconOffsets. add(pos)
                            }
                        )
                    }
                    items.getOrNull(1)?.let { item ->
                        MiniIconWithPosition(
                            item = item,
                            index = 1,
                            onPositioned = { pos ->
                                if (miniIconOffsets.size > 1) miniIconOffsets[1] = pos
                                else miniIconOffsets.add(pos)
                            }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement. spacedBy(3.dp)) {
                    items.getOrNull(2)?.let { item ->
                        MiniIconWithPosition(
                            item = item,
                            index = 2,
                            onPositioned = { pos ->
                                if (miniIconOffsets.size > 2) miniIconOffsets[2] = pos
                                else miniIconOffsets.add(pos)
                            }
                        )
                    }
                    items.getOrNull(3)?. let { item ->
                        MiniIconWithPosition(
                            item = item,
                            index = 3,
                            onPositioned = { pos ->
                                if (miniIconOffsets.size > 3) miniIconOffsets[3] = pos
                                else miniIconOffsets.add(pos)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier. height(8.dp))

        Text(
            text = "更多",
            color = AppTheme.colors.primaryTextColor,
            textAlign = TextAlign. Center,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun MiniIconWithPosition(
    item: FunctionItemData,
    index: Int,
    onPositioned: (Offset) -> Unit
) {
    Box(
        modifier = Modifier
            . size(17.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(item.tintColor. copy(alpha = 0.15f))
            . onGloballyPositioned { coords ->
                val pos = coords. positionInWindow()
                val size = coords.size
                onPositioned(Offset(pos.x + size.width / 2f, pos. y + size.height / 2f))
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            modifier = Modifier.size(11.dp),
            contentScale = ContentScale. Fit
        )
    }
}

/**
 * 展开的文件夹 Popup - 确保在最高层级
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun ExpandedFolderPopup(
    state: FolderExpandState,
    items: List<FunctionItemData>,
    onItemClick: (FunctionItemData) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp. toPx() }

    // 动画控制
    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationStarted = true
    }

    val expandProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 200f
        ),
        label = "expandProgress"
    )

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = false // 我们自己处理点击
        )
    ) {
        Box(
            modifier = Modifier
                . fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                }
        ) {
            // 毛玻璃背景
            Box(
                modifier = Modifier
                    . fillMaxSize()
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.graphicsLayer {
                                renderEffect = RenderEffect
                                    .createBlurEffect(
                                        30f * expandProgress,
                                        30f * expandProgress,
                                        Shader.TileMode.MIRROR
                                    )
                                    .asComposeRenderEffect()
                            }
                        } else {
                            Modifier
                        }
                    )
                    .background(Color.Black.copy(alpha = 0.4f * expandProgress))
            )

            // 展开的面板背景
            val panelWidth = with(density) { (screenWidthPx - 64. dp.toPx()) }
            val panelHeight = with(density) { 280.dp.toPx() }
            val panelX = with(density) { 32.dp.toPx() }
            val panelY = state.folderCenterInWindow.y - panelHeight - with(density) { 20.dp.toPx() }
            val safePanelY = panelY.coerceIn(with(density) { 100.dp.toPx() }, screenHeightPx - panelHeight - with(density) { 100.dp.toPx() })

            // 面板从文件夹位置展开
            val currentPanelWidth = lerp(52f, panelWidth, expandProgress)
            val currentPanelHeight = lerp(52f, panelHeight, expandProgress)
            val currentPanelX = lerp(state.folderCenterInWindow.x - 26f, panelX, expandProgress)
            val currentPanelY = lerp(state.folderCenterInWindow.y - 26f, safePanelY, expandProgress)
            val currentRadius = lerp(14f, 28f, expandProgress)

            Box(
                modifier = Modifier
                    .offset { IntOffset(currentPanelX. roundToInt(), currentPanelY. roundToInt()) }
                    .size(
                        width = with(density) { currentPanelWidth. toDp() },
                        height = with(density) { currentPanelHeight.toDp() }
                    )
                    .graphicsLayer {
                        alpha = expandProgress
                        shadowElevation = 24f * expandProgress
                        shape = RoundedCornerShape(with(density) { currentRadius.toDp() })
                        clip = true
                    }
                    .background(
                        AppTheme.colors. bgSecondaryColor.copy(alpha = 0.98f),
                        RoundedCornerShape(with(density) { currentRadius.toDp() })
                    )
                    .pointerInput(Unit) {
                        detectTapGestures { /* 阻止穿透 */ }
                    }
            )

            // 飞出的图标 - 每个图标独立绘制和动画
            items.forEachIndexed { index, item ->
                FlyingIconFromFolder(
                    item = item,
                    index = index,
                    totalCount = items.size,
                    expandProgress = expandProgress,
                    folderCenter = state.folderCenterInWindow,
                    panelX = panelX,
                    panelY = safePanelY,
                    panelWidth = panelWidth,
                    panelHeight = panelHeight,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

/**
 * 从文件夹飞出的单个图标
 */
@Composable
private fun FlyingIconFromFolder(
    item: FunctionItemData,
    index: Int,
    totalCount: Int,
    expandProgress: Float,
    folderCenter: Offset,
    panelX: Float,
    panelY: Float,
    panelWidth: Float,
    panelHeight: Float,
    onClick: () -> Unit
) {
    val density = LocalDensity.current

    // 计算目标位置 - 在面板内的网格位置
    val itemsPerRow = 3
    val row = index / itemsPerRow
    val col = index % itemsPerRow

    val itemWidth = with(density) { 90.dp.toPx() }
    val itemHeight = with(density) { 100.dp.toPx() }
    val horizontalPadding = with(density) { 24.dp.toPx() }
    val verticalPadding = with(density) { 24.dp.toPx() }
    val horizontalSpacing = (panelWidth - horizontalPadding * 2 - itemsPerRow * itemWidth) / (itemsPerRow - 1). coerceAtLeast(1)
    val verticalSpacing = with(density) { 20.dp.toPx() }

    val targetX = panelX + horizontalPadding + col * (itemWidth + horizontalSpacing) + itemWidth / 2
    val targetY = panelY + verticalPadding + row * (itemHeight + verticalSpacing) + itemHeight / 2

    // 每个图标有不同的动画延迟
    val itemDelay = index * 0.05f
    val itemProgress = ((expandProgress - itemDelay) / (1f - itemDelay * 0.8f)).coerceIn(0f, 1f)

    // 使用更好的缓动
    val easedProgress = easeOutCubic(itemProgress)

    // 从文件夹中心飞向目标位置
    val currentX = lerp(folderCenter.x, targetX, easedProgress)
    val currentY = lerp(folderCenter.y, targetY, easedProgress)

    // 缩放：从小变大
    val scale = lerp(0.2f, 1f, easedProgress)

    // 透明度
    val alpha = itemProgress

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )

    Box(
        modifier = Modifier
            . offset {
                IntOffset(
                    (currentX - itemWidth / 2). roundToInt(),
                    (currentY - itemHeight / 2).roundToInt()
                )
            }
            . size(
                width = with(density) { itemWidth.toDp() },
                height = with(density) { itemHeight.toDp() }
            )
            . graphicsLayer {
                scaleX = scale * pressScale
                scaleY = scale * pressScale
                this.alpha = alpha
            }
            . clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(item.tintColor. copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = item. iconRes),
                    contentDescription = item.title,
                    modifier = Modifier.size(30.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier. height(10.dp))

            Text(
                text = item.title,
                color = AppTheme.colors. primaryTextColor,
                fontSize = 13.sp,
                textAlign = TextAlign. Center,
                maxLines = 1
            )
        }
    }
}

// easeOutCubic 缓动函数
private fun easeOutCubic(x: Float): Float {
    return 1 - (1 - x). toDouble().pow(3.0).toFloat()
}

// easeOutBack 缓动函数 - 带回弹
private fun easeOutBack(x: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1
    return 1 + c3 * (x - 1). toDouble().pow(3.0).toFloat() +
            c1 * (x - 1).toDouble().pow(2.0). toFloat()
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}