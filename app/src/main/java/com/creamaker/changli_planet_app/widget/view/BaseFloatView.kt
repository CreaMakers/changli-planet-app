package com.creamaker.changli_planet_app.widget.view

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.utils.ResourceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

abstract class BaseFloatView : FrameLayout, View.OnTouchListener {

    private var mViewWidth = 0
    private var mViewHeight = 0
    private val mToolBarHeight by lazy { ResourceUtil.getDimen(R.dimen.dp56) }
    private var mDragDistance = 0.5 // 默认吸边需要的拖拽距离为屏幕的一半

    // 吸边所需的高度和宽度阈值
    private val adsorbHeight get() = screenHeight * mDragDistance
    private val adsorbWidth get() = screenWidth * mDragDistance


    /*
    上下左右的坐标
     */
    private val topEdgeY get() = mToolBarHeight.toFloat()
    private val bottomEdgeY get() = (getContentHeight() - mViewHeight).toFloat()
    private val leftEdgeX get() = 0f
    private val rightEdgeX get() = (screenWidth - mViewWidth).toFloat()

    /*
    吸附类型
     */
    companion object {
        var ADSORB_VERTICAL = 1001
        var ADSORB_HORIZONTAL = 1002
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        initView()
    }

    private fun initView() {
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        lp.topMargin = mToolBarHeight.toInt()
        layoutParams = lp

        val childView = getChildView()
        addView(childView)
        setOnTouchListener(this)

        post {
            mViewWidth = this.width
            mViewHeight = this.height
        }
    }

    /**
     * 获取子view
     */
    protected abstract fun getChildView(): View

    /**
     * 是否可以拖拽
     */
    protected abstract fun getIsCanDrag(): Boolean

    /**
     * 吸边的方式
     */
    protected abstract fun getAdsorbType(): Int

    /**
     * 多久自动缩一半
     * 默认：3000，单位：毫秒，小于等于0则不自动缩
     */
    open fun getAdsorbTime(): Long {
        return 3000
    }

    /*
    获取内容高度
     */
    private fun getContentHeight(): Int {
        val view =
            (context as? android.app.Activity)?.window?.decorView?.findViewById<FrameLayout>(android.R.id.content)
        return view?.bottom ?: screenHeight
    }

    /*
    点击事件
     */
    var onFloatClick: ((View) -> Unit)? = null

    fun setOnFloatClickListener(listener: (View) -> Unit) {
        onFloatClick = listener
    }

    /**
     * 设置吸边需要的拖拽距离，默认半屏修改吸边方向，取值0-1
     */
    var dragDistance: Double = 0.5
        set(value) {
            field = value.coerceIn(0.0, 1.0) // 自动限制在0-1范围内
        }
    private var isHalfHidden = false

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var hideJob: Job? = null


    /*
    屏幕宽
     */
    private val screenWidth: Int
        get() = Resources.getSystem().displayMetrics.widthPixels

    /*
    *屏幕高
    */
    private val screenHeight: Int
        get() = Resources.getSystem().displayMetrics.heightPixels

    /*
    是否在右边
     */
    private val isOnRightSide: Boolean get() = x > screenWidth / 2

    /*
    是否在下边
     */

    private val isOnBottomSide: Boolean get() = y > screenHeight / 2

    /**
     * 初始位置是否在顶部
     */
    private fun isOriginalFromTop(): Boolean {
        return mFirstY < screenHeight / 2
    }

    /**
     * 初始位置是否在左边
     */
    private fun isOriginalFromLeft(): Boolean {
        return mFirstX < screenWidth / 2
    }

    /*
    吸边动画
     */
    private fun animateTo(x: Float? = null, y: Float? = null, alpha: Float? = null) {
        animate()
            .setInterpolator(DecelerateInterpolator())
            .setDuration(300)
            .apply {
                x?.let { this.x(it) }
                y?.let { this.y(it) }
                alpha?.let { this.alpha(it) }
            }
            .start()
    }


    // 左右半隐藏视图函数
    private fun hidePartially() {
        val targetX = if (isOnRightSide) {
            (screenWidth - mViewWidth / 2).toFloat()
        } else {
            (-width / 2).toFloat()
        }
        animateTo(targetX, null, 0.5f)
        isHalfHidden = true
    }

    private fun resetStatus() {
        if (!isHalfHidden) return

        val targetX = if (isOnRightSide) {
            (screenWidth - mViewWidth).toFloat()
        } else {
            0f
        }
        animateTo(targetX, null, 1f)
        isHalfHidden = false
    }

    private var mDownX = 0F
    private var mDownY = 0F
    private var mFirstY: Int = 0
    private var mFirstX: Int = 0
    private var isMove = false


    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.x
                mDownY = event.y
                // 记录第一次在屏幕上坐标，用于计算初始位置
                mFirstY = event.rawY.roundToInt()
                mFirstX = event.rawX.roundToInt()

                hideJob?.cancel()
                resetStatus()
            }

            MotionEvent.ACTION_MOVE -> {
                isMove = true
                offsetTopAndBottom((y - mDownY).toInt())
                offsetLeftAndRight((x - mDownX).toInt())
            }

            MotionEvent.ACTION_UP -> {
                if (isMove) {
                    if (getAdsorbType() == ADSORB_VERTICAL) {
                        adsorbTopAndBottom(event)
                    } else if (getAdsorbType() == ADSORB_HORIZONTAL) {
                        adsorbLeftAndRight(event)
                    }
                } else {
                    onFloatClick?.invoke(v)
                }
                isMove = false

                if (getAdsorbTime() > 0) {
                    hideJob = coroutineScope.launch {
                        delay(getAdsorbTime())
                        hidePartially()
                    }
                }
            }
        }
        return getIsCanDrag()
    }

    private fun adsorbTopAndBottom(event: MotionEvent) {
        val dragDistance = mViewHeight / 2 + kotlin.math.abs(event.rawY - mFirstY)
        val shouldStickToTop = if (isOriginalFromTop()) {
            dragDistance < adsorbHeight
        } else {
            dragDistance >= adsorbHeight
        }

        val targetY = if (shouldStickToTop) topEdgeY else bottomEdgeY

        val targetX = when {
            event.rawX < mViewWidth -> leftEdgeX
            event.rawX > screenWidth - mViewWidth -> rightEdgeX
            else -> null
        }
        animateTo(targetX, targetY)
    }

    private fun adsorbLeftAndRight(event: MotionEvent) {
        val dragDistance = mViewWidth / 2 + kotlin.math.abs(event.rawX - mFirstX)
        val shouldStickToLeft = if (isOriginalFromLeft()) {
            dragDistance < adsorbWidth // 左半屏：拖拽少 = 吸左
        } else {
            dragDistance >= adsorbWidth // 右半屏：拖拽多 = 吸左
        }

        // 决定X坐标
        val targetX = if (shouldStickToLeft) leftEdgeX else rightEdgeX

        // 处理纵向越界
        val targetY = when {
            event.rawY < mViewHeight -> topEdgeY
            event.rawY > getContentHeight() - mViewHeight -> bottomEdgeY
            else -> null
        }
        animateTo(targetX, targetY)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope.cancel()
    }

}