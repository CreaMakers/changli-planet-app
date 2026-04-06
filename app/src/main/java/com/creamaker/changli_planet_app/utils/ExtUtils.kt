package com.creamaker.changli_planet_app.utils

import android.content.Context
import android.os.Message
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.common.data.local.room.entity.UserEntity
import com.creamaker.changli_planet_app.common.data.remote.dto.UserProfile

val Context.screenHeight
    get() = resources.displayMetrics.heightPixels

val Context.screenWidth
    get() = resources.displayMetrics.widthPixels

val Context.statusBarHeight
    get() = resources.getDimensionPixelSize(
        resources.getIdentifier(
            "status_bar_height",
            "dimen",
            "android"
        )
    )


fun View.isVisible() = this.visibility == View.VISIBLE

/**
 * 传入boolean来显示隐藏View，true = 显示 , false = 隐藏
 */
fun View.setVisible(b: Boolean) {
    this.visibility = if (b) View.VISIBLE else View.GONE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.singleClick(delay: Long = 1000, click: () -> Unit) {
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        val lastClickTime: Long = getTag(R.id.tag_last_click_time) as? Long ?: 0L
        if (currentTime - lastClickTime >= delay) {
            setTag(R.id.tag_last_click_time, currentTime)
            click()
        }
    }
}

fun UserProfile.toEntity(cacheTime: Long = System.currentTimeMillis()): UserEntity {
    return UserEntity(
        userId = this.userId,
        username = this.username,
        account = this.account,
        avatarUrl = this.avatarUrl,
        bio = this.bio,
        description = this.description,
        userLevel = this.userLevel,
        gender = this.gender,
        grade = this.grade,
        birthDate = this.birthDate,
        location = this.location,
        website = this.website,
        createTime = this.createTime,
        updateTime = this.updateTime,
        deleted = this.isDeleted,
        cacheTime = cacheTime
    )
}

fun UserEntity.toProfile(): UserProfile {
    return UserProfile(
        userId = this.userId,
        username = this.username,
        account = this.account,
        avatarUrl = this.avatarUrl,
        bio = this.bio,
        description = this.description,
        userLevel = this.userLevel,
        gender = this.gender,
        grade = this.grade,
        birthDate = this.birthDate,
        location = this.location,
        website = this.website,
        createTime = this.createTime,
        updateTime = this.updateTime,
        isDeleted = this.deleted
    )
}


fun dp2Px(context: Context, dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}


fun getMessage(what: Int, arg1: Int ?= null, arg2: Int? = null, obj: Any? = null) : Message{
    return Message.obtain().apply {
        this.what = what
        arg1?.let { this.arg1 = arg1 }
        arg2?.let { this.arg2 = arg2 }
        obj?.let { this.obj = obj }
    }
}

/**
 * 鸿蒙风格边缘柔和发光 / 光晕 Modifier
 *
 * @param shape 你的底部导航栏的形状（例如 CircleShape 或 RoundedCornerShape）
 * @param lightColor 光源的颜色（一般用纯白，或者与主题色相近的浅色）
 * @param width 边缘高光线的粗细
 */
fun Modifier.edgeLightingGlow(
    shape: Shape,
    lightColor: Color = Color(0xFFF7FAFF),
    width: Dp = 2.dp
) = this.drawBehind {
    // 1. 绘制外部柔和的弥散光晕 (Outer Glow)
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = Color.Transparent.toArgb()
        // 设置阴影发光，radius 控制光晕的扩散范围
        frameworkPaint.setShadowLayer(
            12.dp.toPx(),
            0f,
            0f,
            lightColor.copy(alpha = 0.42f).toArgb()
        )
        canvas.drawOutline(shape.createOutline(size, layoutDirection, this), paint)
    }

    // 2. 在边缘勾勒一条极细的内测高光边框（提升玻璃质感）
    drawOutline(
        outline = shape.createOutline(size, layoutDirection, this),
        color = lightColor.copy(alpha = 0.62f),
        style = Stroke(width = width.toPx())
    )
}

fun Modifier.glassTouchGlow(
    glowColor: Color,
    restingRadius: Dp = 26.dp,
    expandedRadius: Dp = 94.dp
) = if (glowColor == Color.Unspecified) {
    this
} else {
    composed {
        var glowCenter by remember { mutableStateOf(Offset.Unspecified) }
        var isPressed by remember { mutableStateOf(false) }
        val animatedAlpha by animateFloatAsState(
            targetValue = if (isPressed) 1f else 0f,
            animationSpec = tween(durationMillis = if (isPressed) 140 else 220),
            label = "glassTouchGlowAlpha"
        )
        val animatedRadius by animateDpAsState(
            targetValue = if (isPressed) expandedRadius else restingRadius,
            animationSpec = tween(durationMillis = if (isPressed) 220 else 260),
            label = "glassTouchGlowRadius"
        )

        this
            .motionEventSpy { event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        glowCenter = Offset(event.x, event.y)
                        isPressed = true
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        glowCenter = Offset(event.x, event.y)
                        isPressed = false
                    }
                }
            }
            .drawWithContent {
                if (glowCenter != Offset.Unspecified && animatedAlpha > 0.001f) {
                    val radiusPx = animatedRadius.toPx()
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.22f * animatedAlpha),
                                glowColor.copy(alpha = 0.12f * animatedAlpha),
                                glowColor.copy(alpha = 0.06f * animatedAlpha),
                                Color.Transparent
                            ),
                            center = glowCenter,
                            radius = radiusPx
                        )
                    )
                }
                drawContent()
            }
    }
}
