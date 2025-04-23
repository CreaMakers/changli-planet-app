package com.example.changli_planet_app.Utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.example.changli_planet_app.R

val Float.dp: Float
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )

val Int.dp: Int
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()


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


/**
 * 设置View的圆角与颜色
 */
fun View.setRoundRectBg(color: Int = Color.RED, cornerRadius: Int = 15.dp) {
    background = GradientDrawable().apply {
        setColor(color)
        setCornerRadius(cornerRadius.toFloat())
        shape = GradientDrawable.RECTANGLE
    }
}

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