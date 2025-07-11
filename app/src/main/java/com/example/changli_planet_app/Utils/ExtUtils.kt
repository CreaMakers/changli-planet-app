package com.example.changli_planet_app.Utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Message
import android.util.TypedValue
import android.view.View
import com.example.changli_planet_app.Cache.Room.entity.UserEntity
import com.example.changli_planet_app.Network.Response.UserProfile
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