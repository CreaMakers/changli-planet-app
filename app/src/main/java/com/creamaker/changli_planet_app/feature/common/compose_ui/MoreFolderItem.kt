package com.creamaker.changli_planet_app.feature.common.compose_ui

import androidx. compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose. foundation.clickable
import androidx.compose. foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose. foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx. compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose. ui.draw.scale
import androidx.compose.ui.graphics. Color
import androidx.compose.ui.layout.ContentScale
import androidx. compose.ui.res.painterResource
import androidx.compose. ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui. unit.sp
import com.creamaker.changli_planet_app.core.theme.AppTheme

/**
 * "更多"文件夹项 - 显示预览图标的文件夹样式
 */
@Composable
fun MoreFolderItem(
    items: List<FunctionItemData>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring. DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12. dp))
            . clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            . padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment. CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                . size(52.dp)
                .clip(RoundedCornerShape(14. dp))
                . background(AppTheme.colors.bgSecondaryColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    items.getOrNull(0)?.let { MiniIcon(item = it) }
                    items.getOrNull(1)?.let { MiniIcon(item = it) }
                }
                Row(horizontalArrangement = Arrangement. spacedBy(3.dp)) {
                    items.getOrNull(2)?.let { MiniIcon(item = it) }
                    items.getOrNull(3)?.let { MiniIcon(item = it) }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "更多",
            color = AppTheme. colors.primaryTextColor,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun MiniIcon(item: FunctionItemData) {
    Box(
        modifier = Modifier
            . size(17.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(item.tintColor. copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            modifier = Modifier.size(11.dp),
            contentScale = ContentScale.Fit
        )
    }
}