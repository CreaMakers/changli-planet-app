package com.creamaker.changli_planet_app.feature.common.compose_ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose. foundation.clickable
import androidx.compose. foundation.interaction.MutableInteractionSource
import androidx.compose. foundation.layout.*
import androidx.compose. foundation.lazy.grid.GridCells
import androidx. compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation. lazy.grid.itemsIndexed
import androidx.compose.foundation. shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose. runtime.*
import androidx.compose.ui. Alignment
import androidx.compose.ui. Modifier
import androidx.compose.ui. draw.alpha
import androidx. compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose. ui.graphics.graphicsLayer
import androidx.compose. ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window. DialogProperties
import kotlinx.coroutines.delay
import kotlin.math. PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 更多功能弹窗 - 带飞出展开动画
 */
@Composable
fun MoreFunctionsDialog(
    isVisible: Boolean,
    items: List<FunctionItemData>,
    onDismiss: () -> Unit,
    onItemClick: (FunctionItemData) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var animateItems by remember { mutableStateOf(false) }

    // 控制显示逻辑
    LaunchedEffect(isVisible) {
        if (isVisible) {
            showDialog = true
            delay(50)
            animateItems = true
        } else {
            animateItems = false
            delay(300)
            showDialog = false
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 弹窗内容卡片
                DialogContent(
                    items = items,
                    animateItems = animateItems,
                    onDismiss = onDismiss,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
private fun DialogContent(
    items: List<FunctionItemData>,
    animateItems: Boolean,
    onDismiss: () -> Unit,
    onItemClick: (FunctionItemData) -> Unit
) {
    val containerScale by animateFloatAsState(
        targetValue = if (animateItems) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring. StiffnessLow
        ),
        label = "containerScale"
    )

    val containerAlpha by animateFloatAsState(
        targetValue = if (animateItems) 1f else 0f,
        animationSpec = tween(200),
        label = "containerAlpha"
    )

    Surface(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .scale(containerScale)
            .alpha(containerAlpha)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // 阻止点击穿透
            ),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme. surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "全部功能",
                    style = MaterialTheme.typography. titleLarge,
                    color = MaterialTheme.colorScheme. onSurface
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 功能项网格 - 带飞入动画
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier. heightIn(max = 400.dp)
            ) {
                itemsIndexed(items) { index, item ->
                    AnimatedFunctionItem(
                        item = item,
                        index = index,
                        isVisible = animateItems,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

/**
 * 带飞入动画的功能项
 */
@Composable
private fun AnimatedFunctionItem(
    item: FunctionItemData,
    index: Int,
    isVisible: Boolean,
    onClick: () -> Unit
) {
    // 计算每个项的初始位置（从中心向外扩散）
    val angle = (index * 45f) * (PI / 180f)
    val radius = 200f
    val initialOffsetX = (cos(angle) * radius).toFloat()
    val initialOffsetY = (sin(angle) * radius).toFloat()

    // 错开动画延迟
    val delayMillis = index * 30

    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isVisible) 0f else initialOffsetX,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "offsetX"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else initialOffsetY,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "offsetY"
    )

    val itemScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val itemAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 250,
            delayMillis = delayMillis
        ),
        label = "alpha"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else 180f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = animatedOffsetX
                translationY = animatedOffsetY
                scaleX = itemScale
                scaleY = itemScale
                alpha = itemAlpha
                rotationZ = rotation
            }
    ) {
        FunctionItem(
            item = item,
            onClick = onClick
        )
    }
}