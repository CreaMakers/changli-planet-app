package com.creamaker.changli_planet_app.widget.View

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class MaxHeightLinearLayout : LinearLayout {
    private var maxHeight: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setMaxHeight(maxHeight: Int) {
        this.maxHeight = maxHeight
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var finalHeightMeasureSpec = heightMeasureSpec
        if (maxHeight > 0) {
            val hSize = MeasureSpec.getSize(heightMeasureSpec)
            val hMode = MeasureSpec.getMode(heightMeasureSpec)
            val targetHeight = minOf(hSize, maxHeight)
            finalHeightMeasureSpec = MeasureSpec.makeMeasureSpec(targetHeight, hMode)
        }
        super.onMeasure(widthMeasureSpec, finalHeightMeasureSpec)
    }
}