package com.creamaker.changli_planet_app.widget.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import com.creamaker.changli_planet_app.R

class CodeEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {
    // 边框宽度
    private var mStrokeWidth: Int = 0

    // 边框高度
    private var mStrokeHeight: Int = 0

    // 边框个数
    private var mStrokeLength: Int = 4

    // 边框之间的padding
    private var mStrokePadding: Int = 0

    // 最大边框数目
    private var mMaxLength: Int = 0

    private var mTextColor: Int = 0

    interface OnTextFinishListener {
        fun onTextFinish(text: CharSequence, length: Int)
    }

    //输入结束后监听
    private var mOnInputFinishListener: OnTextFinishListener? = null

    // 边框的背景颜色
    private var mStrokeDrawable: Drawable? = null

    private val mRect = Rect()

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CodeEditText)
        val indexCount = typedArray.indexCount
        for (i in 0 until indexCount) {
            val index = typedArray.getIndex(i)
            when (index) {
                R.styleable.CodeEditText_strokeWidth -> mStrokeWidth =
                    typedArray.getDimension(index, 60f).toInt()

                R.styleable.CodeEditText_strokeHeight -> mStrokeHeight =
                    typedArray.getDimension(index, 60f).toInt()

                R.styleable.CodeEditText_strokePadding -> mStrokePadding =
                    typedArray.getDimension(index, 20f).toInt()

                R.styleable.CodeEditText_strokeBackground -> mStrokeDrawable =
                    typedArray.getDrawable(index)

                R.styleable.CodeEditText_strokeLength -> mMaxLength =
                    typedArray.getInteger(index, 4)
            }
        }
        typedArray.recycle()
        mStrokeDrawable ?: throw NullPointerException("stroke drawable not allow to null")

        setMaxLength(mStrokeLength)

        isLongClickable = false
        isCursorVisible = false
        setBackgroundColor(Color.TRANSPARENT)
    }

    private fun setMaxLength(maxLength: Int) {
        filters = if (maxLength >= 0) {
            arrayOf(InputFilter.LengthFilter(maxLength))
        } else {
            arrayOf()
        }
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = measuredWidth
        var height = measuredHeight

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (height < mStrokeHeight) {
            height = mStrokeHeight
        }

        val recommendWidth = mStrokeWidth * mMaxLength + mStrokePadding * (mMaxLength - 1)
        if (width < recommendWidth) {
            width = recommendWidth
        }

        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(width, widthMode),
            MeasureSpec.makeMeasureSpec(height, heightMode)
        )
    }

    override fun onDraw(canvas: Canvas) {
        mTextColor = currentTextColor
        setTextColor(Color.TRANSPARENT)
        super.onDraw(canvas)
        setTextColor(mTextColor)
        drawStrokeBackground(canvas)
        drawText(canvas)

    }

    private fun drawStrokeBackground(canvas: Canvas) {
        mRect.set(0, 0, mStrokeWidth, mStrokeHeight)
        val count = canvas.saveCount
        canvas.save()
        for (i in 0 until mMaxLength) {
            mStrokeDrawable?.bounds = mRect
            mStrokeDrawable?.setState(intArrayOf(android.R.attr.state_enabled))
            mStrokeDrawable?.draw(canvas)

            val dx = mRect.right + mStrokePadding
            canvas.save()
            canvas.translate(dx.toFloat(), 0f)
        }
        canvas.restoreToCount(count)
    }


    private fun drawText(canvas: Canvas) {
        val count = canvas.saveCount
        val length = editableText.length
        for (i in 0 until length) {
            val text = editableText[i].toString()
            val textPaint = paint
            textPaint.color = mTextColor
            textPaint.getTextBounds(text, 0, 1, mRect)

            val x = mStrokeWidth / 2 + (mStrokeWidth + mStrokePadding) * i - mRect.centerX()
            val y = canvas.height / 2 + mRect.centerY()
            canvas.drawText(text, x.toFloat(), y.toFloat(), textPaint)
        }
        canvas.restoreToCount(count)
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        val length = editableText.length

        if (length == mStrokeLength) {
            hideSoftInput()
            mOnInputFinishListener?.onTextFinish(editableText.toString(), mMaxLength)
        }
    }

    private fun hideSoftInput() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun setOnTextFinishListener(onInputFinishListener: OnTextFinishListener) {
        this.mOnInputFinishListener = onInputFinishListener
    }
}