package com.creamaker.changli_planet_app.skin.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import androidx.core.content.withStyledAttributes
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.delegate.SkinDelegate
import com.zhuangfei.timetable.TimetableView

class SkinTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), SkinSupportable, SkinAttributeProvider {
    var isSkinable = true
    private val TAG = "SkinTextView"
    private val delegate = SkinDelegate(this,this)

    init {
        delegate.loadSkinAttributes(context, attrs)
        applySkin()
    }
    @SuppressLint("ResourceType")
    override fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, intArrayOf(
            android.R.attr.textColor,
            android.R.attr.background
        )) {
            val textColorResId = getResourceId(0, 0)
            if (textColorResId != 0) {
                delegate.setAttr("tv_text_color", textColorResId)
            }

            val bgResId = getResourceId(1, 0)
            if (bgResId != 0) {
                delegate.setAttr("tv_background", bgResId)
            }
        }
    }
    override fun applySkin() {
        if (isSkinable) {
            delegate.applySkin()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (checkIsInsideTimetableView()) {
            isSkinable = false // 禁用换肤
            return // 不再注册 SkinManager，也不执行 applySkin
        }
        Log.d(TAG, "SkinTextView onAttachedToWindow")
        SkinManager.attach(this)
        applySkin()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "SkinTextView onDetachedFromWindow")
        SkinManager.detach(this)
    }
    private fun checkIsInsideTimetableView(): Boolean {
        var current: View? = this
        while (current != null) {
            val parent = current.parent
            if (parent != null && parent is TimetableView) {
                return true
            }
            if (parent is View) {
                current = parent
            } else {
                break
            }
        }
        return false
    }
}

