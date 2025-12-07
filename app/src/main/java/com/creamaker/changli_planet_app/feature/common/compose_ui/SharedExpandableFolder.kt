package com.creamaker.changli_planet_app.feature.common.compose_ui

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android. graphics. Shader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose. foundation.clickable
import androidx.compose. foundation.gestures.detectTapGestures
import androidx.compose. foundation.interaction.MutableInteractionSource
import androidx. compose.foundation.layout.*
import androidx. compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3. Text
import androidx. compose.runtime.*
import androidx.compose. ui.Alignment
import androidx.compose. ui.Modifier
import androidx.compose. ui.draw.blur
import androidx.compose.ui.draw. clip
import androidx. compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui. graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose. ui.input.pointer.pointerInput
import androidx. compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout. onGloballyPositioned
import androidx. compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform. LocalDensity
import androidx.compose.ui. res.painterResource
import androidx.compose. ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx. compose.ui.unit.dp
import androidx.compose.ui. unit.sp
import androidx.compose.ui. zIndex
import com.creamaker. changli_planet_app.core.theme.AppTheme
import kotlinx.coroutines.delay
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 可展开文件夹的状态
 */
@Stable
class ExpandableFolderState {
    var isExpanded by mutableStateOf(false)
        private set
    
    var folderPosition by mutableStateOf(Offset.Zero)
        internal set
    
    var folderSize by mutableStateOf(52f)
        internal set
    
    fun expand() {
        isExpanded = true
    }
    
    fun collapse() {
        isExpanded = false
    }
}

@Composable
fun rememberExpandableFolderState(): ExpandableFolderState {
    return remember { ExpandableFolderState() }
}

/**
 * 主功能区 + 可展开更多文件夹
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun FunctionGridWithExpandableMore(
    mainItems: List<FunctionItemData>,
    moreItems: List<FunctionItemData>,
    onItemClick: (FunctionItemData) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberExpandableFolderState()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp. toPx() }

    Box(modifier = modifier.fillMaxWidth()) {
        // 主内容
        Column(modifier = Modifier.fillMaxWidth()) {
            // 第一行：前4个
            Row(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement. SpaceEvenly
            ) {
                mainItems.take(4).forEach { item ->
                    FunctionItem(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 第二行：3个 + 更多
            Row(
                modifier = Modifier. fillMaxWidth(),
                horizontalArrangement = Arrangement. SpaceEvenly
            ) {
                mainItems.drop(4).take(3). forEach { item ->
                    FunctionItem(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 更多文件夹
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInRoot()
                            val size = coordinates.size
                            state.folderPosition = Offset(
                                x = position.x + size.width / 2f,
                                y = position.y + size.height / 2f
                            )
                            state.folderSize = size. width.toFloat()
                        }
                ) {
                    // 只在未展开时显示更多按钮
                    if (!state.isExpanded) {
                        MoreFolderItem(
                            items = moreItems,
                            onClick = { state.expand() }
                        )
                    } else {
                        // 占位保持布局
                        Spacer(
                            modifier = Modifier
                                . padding(vertical = 8.dp, horizontal = 4. dp)
                                .size(52.dp)
                        )
                    }
                }
            }
        }

        // 展开的覆盖层
        ExpandedOverlay(
            state = state,
            items = moreItems,
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx,
            onItemClick = { item ->
                state.collapse()
                onItemClick(item)
            },
            onDismiss = { state.collapse() }
        )
    }
}

@Composable
private fun ExpandedOverlay(
    state: ExpandableFolderState,
    items: List<FunctionItemData>,
    screenWidthPx: Float,
    screenHeightPx: Float,
    onItemClick: (FunctionItemData) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    
    // 主动画进度
    val expandProgress by animateFloatAsState(
        targetValue = if (state.isExpanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessLow
        ),
        label = "expandProgress"
    )

    if (expandProgress > 0.01f) {
        Box(
            modifier = Modifier
                . fillMaxSize()
                .zIndex(50f)
                . pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                }
        ) {
            // 毛玻璃背景
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.graphicsLayer {
                                renderEffect = RenderEffect
                                    .createBlurEffect(
                                        20f * expandProgress,
                                        20f * expandProgress,
                                        Shader.TileMode.CLAMP
                                    )
                                    .asComposeRenderEffect()
                            }
                        } else {
                            Modifier. blur((20 * expandProgress).dp)
                        }
                    )
                    .background(
                        AppTheme.colors. bgPrimaryColor. copy(
                            alpha = 0.7f * expandProgress
                        )
                    )
            )

            // 飞出的图标
            items.forEachIndexed { index, item ->
                FlyingIcon(
                    item = item,
                    index = index,
                    totalCount = items.size,
                    expandProgress = expandProgress,
                    folderPosition = state. folderPosition,
                    screenWidthPx = screenWidthPx,
                    screenHeightPx = screenHeightPx,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun FlyingIcon(
    item: FunctionItemData,
    index: Int,
    totalCount: Int,
    expandProgress: Float,
    folderPosition: Offset,
    screenWidthPx: Float,
    screenHeightPx: Float,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    
    // 计算目标位置（居中网格布局）
    val itemsPerRow = 4
    val row = index / itemsPerRow
    val col = index % itemsPerRow
    
    val itemWidth = with(density) { 80.dp.toPx() }
    val itemHeight = with(density) { 90.dp.toPx() }
    val horizontalSpacing = with(density) { 12.dp.toPx() }
    val verticalSpacing = with(density) { 16.dp.toPx() }
    
    val gridWidth = itemsPerRow * itemWidth + (itemsPerRow - 1) * horizontalSpacing
    val rows = (totalCount + itemsPerRow - 1) / itemsPerRow
    val gridHeight = rows * itemHeight + (rows - 1) * verticalSpacing
    
    val gridStartX = (screenWidthPx - gridWidth) / 2
    val gridStartY = (screenHeightPx - gridHeight) / 2
    
    val targetX = gridStartX + col * (itemWidth + horizontalSpacing) + itemWidth / 2
    val targetY = gridStartY + row * (itemHeight + verticalSpacing) + itemHeight / 2
    
    // 每个图标的动画延迟
    val itemDelay = index * 0.06f
    val itemProgress = ((expandProgress - itemDelay) / (1f - itemDelay * 0.5f)).coerceIn(0f, 1f)
    
    // 使用 easeOutBack 缓动函数
    val easedProgress = easeOutBack(itemProgress)
    
    // 当前位置插值
    val currentX = lerp(folderPosition.x, targetX, easedProgress)
    val currentY = lerp(folderPosition.y, targetY, easedProgress)
    
    // 缩放（从0到1）
    val scale = lerp(0f, 1f, itemProgress)
    
    // 透明度
    val alpha = itemProgress

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (currentX - itemWidth / 2). roundToInt(),
                    (currentY - itemHeight / 2).roundToInt()
                )
            }
            . size(
                width = with(density) { itemWidth.toDp() },
                height = with(density) { itemHeight. toDp() }
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clip(RoundedCornerShape(12. dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52. dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.tintColor. copy(alpha = 0.12f)),
                contentAlignment = Alignment. Center
            ) {
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.title,
                    modifier = Modifier.size(28. dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(8. dp))
            
            Text(
                text = item.title,
                color = AppTheme.colors.primaryTextColor,
                fontSize = 12.sp,
                textAlign = TextAlign. Center,
                maxLines = 1
            )
        }
    }
}

// easeOutBack 缓动函数 - 产生轻微回弹效果
private fun easeOutBack(x: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1
    return 1 + c3 * (x - 1).toDouble().pow(3.0). toFloat() +
           c1 * (x - 1).toDouble().pow(2.0).toFloat()
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}