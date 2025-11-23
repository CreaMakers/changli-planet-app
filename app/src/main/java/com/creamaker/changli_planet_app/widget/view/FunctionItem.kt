package com.creamaker.changli_planet_app.widget.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.utils.load

class FunctionItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val icon: ImageView
    private val title: TextView

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.item_function, this, true)
        icon = findViewById(R.id.icon)
        title = findViewById(R.id.title)
        // 获取自定义属性
        attrs?.let {
            val typedArray: TypedArray =
                context.obtainStyledAttributes(it, R.styleable.FunctionItemView)
            val titleText = typedArray.getString(R.styleable.FunctionItemView_itemTitle)
            if (!titleText.isNullOrEmpty()) {
                setTitle(titleText)
            }
            typedArray.recycle()
        }
    }

    fun setIcon(resId: Int) {
        icon.setImageResource(resId)
    }

    fun setIconWithGlide(
        @DrawableRes resId: Int
    ) {
        icon.load(resId)
    }

    private fun setTitle(text: String) {
        title.text = text
    }
}