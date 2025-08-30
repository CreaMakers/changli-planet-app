package com.example.changli_planet_app.Widget.View

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.example.changli_planet_app.R
import com.example.changli_planet_app.Utils.load

class FunctionItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val icon: AppCompatImageView
    private val title: AppCompatTextView

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