package com.creamaker.changli_planet_app.feature.common.compose_ui


import androidx.annotation.DrawableRes
import androidx.compose. animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx. compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx. compose.foundation.layout.*
import androidx. compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx. compose.runtime.*
import androidx.compose. ui. Alignment
import androidx.compose.ui. Modifier
import androidx.compose.ui. draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui. res.painterResource
import androidx.compose. ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx. compose.ui.unit.dp
import androidx.compose.ui. unit.sp
import com.creamaker.changli_planet_app.core.theme.AppTheme

/**
 * 功能项数据类
 */
@Immutable
data class FunctionItemData(
    val id: String,
    val title: String,
    @DrawableRes val iconRes: Int,
    val tintColor: Color = Color.Unspecified
)
/**
 * 单个功能项组件
 */
@Composable
fun FunctionItem(
    item: FunctionItemData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring. StiffnessLow
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                . size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(item.tintColor. copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.title,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item. title,
            color = AppTheme.colors. primaryTextColor,
            textAlign = TextAlign. Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp
        )
    }
}