package com.creamaker.changli_planet_app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView装饰器封装类
 * 支持设置item的margin和下划线
 * 只支持LinearLayoutManager!!!
 */
class ItemDecorationWrapper private constructor() : RecyclerView.ItemDecoration() {

    // Margin相关参数
    private var topMargin: Int = 0
    private var bottomMargin: Int = 0
    private var leftMargin: Int = 0
    private var rightMargin: Int = 0

    // 下划线相关参数
    private var dividerHeight: Int = 0
    private var dividerColor: Int = 0
    private var showDivider: Boolean = false
    private var paint: Paint? = null

    // 回调接口 - 用于条件控制
    /**
     * Margin显示条件回调
     * @param position 当前item的位置
     * @param parent RecyclerView实例
     * @return true表示需要显示margin，false表示不需要显示margin
     */
    private var marginCallback: ((position: Int, parent: RecyclerView) -> Boolean)? = null

    /**
     * 下划线显示条件回调
     * @param position 当前item的位置
     * @param parent RecyclerView实例
     * @return true表示需要显示下划线，false表示不需要显示下划线
     */
    private var dividerCallback: ((position: Int, parent: RecyclerView) -> Boolean)? = null

    /**
     * 设置item偏移量（margin效果）
     * @param outRect 用于设置偏移量的矩形区域
     * @param view 当前item view
     * @param parent RecyclerView
     * @param state RecyclerView状态
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        // 判断是否需要设置margin，默认为true（都需要显示）
        val needMargin = marginCallback?.invoke(position, parent) ?: true

        if (needMargin) {
            // 设置四个方向的margin
            outRect.left = leftMargin
            outRect.right = rightMargin
            outRect.top = topMargin

            // 如果需要显示下划线，则bottom margin要包含下划线高度
            val additionalBottomMargin = if (showDivider && dividerHeight > 0) dividerHeight else 0
            outRect.bottom = bottomMargin + additionalBottomMargin
        } else {
            // 不需要margin时，设置为0
            outRect.set(0, 0, 0, 0)
        }
    }

    /**
     * 绘制装饰内容（如下划线）
     * @param c Canvas画布
     * @param parent RecyclerView
     * @param state RecyclerView状态
     */
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        // 如果不显示下划线或下划线高度为0，则不绘制
        if (!showDivider || dividerHeight <= 0) return

        val layoutManager = parent.layoutManager
        // 只支持LinearLayoutManager
        if (layoutManager !is LinearLayoutManager) return

        val orientation = layoutManager.orientation

        // 遍历所有可见的child view
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)

            val needDivider = dividerCallback?.invoke(position, parent) ?: true
            if (!needDivider) continue

            if (orientation == LinearLayoutManager.VERTICAL) {
                drawVerticalDivider(c, child, parent)
            } else {
                drawHorizontalDivider(c, child, parent)
            }
        }
    }

    /**
     * 绘制垂直布局的下划线
     * @param canvas 画布
     * @param child 当前item view
     * @param parent RecyclerView
     */
    private fun drawVerticalDivider(canvas: Canvas, child: View, parent: RecyclerView) {
        val left = parent.paddingLeft + leftMargin
        val right = parent.width - parent.paddingRight - rightMargin
        val top = child.bottom + bottomMargin
        val bottom = top + dividerHeight

        paint?.let {
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), it)
        }
    }

    /**
     * 绘制水平布局的下划线
     * @param canvas 画布
     * @param child 当前item view
     * @param parent RecyclerView
     */
    private fun drawHorizontalDivider(canvas: Canvas, child: View, parent: RecyclerView) {
        val top = parent.paddingTop + topMargin
        val bottom = parent.height - parent.paddingBottom - bottomMargin
        val left = child.right + rightMargin
        val right = left + dividerHeight

        paint?.let {
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), it)
        }
    }

    /**
     * Builder构建器类 - 用于链式构建ItemDecoration
     */
    class Builder(private val context: Context) {
        private val itemDecoration = ItemDecorationWrapper()

        /**
         * 设置上下左右margin（单位：dp）
         * @param left 左边距（dp）
         * @param top 上边距（dp）
         * @param right 右边距（dp）
         * @param bottom 下边距（dp）
         * @return Builder实例，支持链式调用
         */
        fun setMarginDp(left: Float, top: Float, right: Float, bottom: Float): Builder {
            itemDecoration.leftMargin = dpToPx(left)
            itemDecoration.topMargin = dpToPx(top)
            itemDecoration.rightMargin = dpToPx(right)
            itemDecoration.bottomMargin = dpToPx(bottom)
            return this
        }

        /**
         * 设置统一margin（单位：dp）
         * @param margin 四边统一边距（dp）
         * @return Builder实例，支持链式调用
         */
        fun setMarginDp(margin: Float): Builder {
            return setMarginDp(margin, margin, margin, margin)
        }

        /**
         * 设置上下margin（单位：dp）
         * @param top 上边距（dp）
         * @param bottom 下边距（dp）
         * @return Builder实例，支持链式调用
         */
        fun setVerticalMarginDp(top: Float, bottom: Float): Builder {
            itemDecoration.topMargin = dpToPx(top)
            itemDecoration.bottomMargin = dpToPx(bottom)
            return this
        }

        /**
         * 设置左右margin（单位：dp）
         * @param left 左边距（dp）
         * @param right 右边距（dp）
         * @return Builder实例，支持链式调用
         */
        fun setHorizontalMarginDp(left: Float, right: Float): Builder {
            itemDecoration.leftMargin = dpToPx(left)
            itemDecoration.rightMargin = dpToPx(right)
            return this
        }

        /**
         * 设置下划线样式
         * @param heightDp 下划线高度（dp）
         * @param color 下划线颜色
         * @return Builder实例，支持链式调用
         */
        fun setDivider(heightDp: Float, color: Int): Builder {
            itemDecoration.dividerHeight = dpToPx(heightDp)
            itemDecoration.dividerColor = color
            itemDecoration.showDivider = true

            // 初始化画笔用于绘制下划线
            itemDecoration.paint = Paint().apply {
                this.color = color
                this.isAntiAlias = true  // 抗锯齿
                this.strokeWidth = itemDecoration.dividerHeight.toFloat()
            }

            return this
        }

        /**
         * 设置是否显示下划线
         * @param show true显示下划线，false不显示
         * @return Builder实例，支持链式调用
         */
        fun setShowDivider(show: Boolean): Builder {
            itemDecoration.showDivider = show
            return this
        }

        /**
         * 设置margin显示条件回调
         * 通过回调函数可以控制哪些item需要显示margin
         * @param callback 回调函数，参数为item位置和RecyclerView，返回true表示显示margin
         * @return Builder实例，支持链式调用
         *
         * 使用示例：
         * .setMarginCondition { position, parent ->
         *     position != 0  // 第一个item不显示margin
         * }
         */
        fun setMarginCondition(callback: (position: Int, parent: RecyclerView) -> Boolean): Builder {
            itemDecoration.marginCallback = callback
            return this
        }

        /**
         * 设置下划线显示条件回调
         * 通过回调函数可以控制哪些item需要显示下划线
         * @param callback 回调函数，参数为item位置和RecyclerView，返回true表示显示下划线
         * @return Builder实例，支持链式调用
         *
         * 使用示例：
         * .setDividerCondition { position, parent ->
         *     position != parent.adapter?.itemCount?.minus(1)  // 最后一个item不显示下划线
         * }
         */
        fun setDividerCondition(callback: (position: Int, parent: RecyclerView) -> Boolean): Builder {
            itemDecoration.dividerCallback = callback
            return this
        }

        /**
         * 构建最终的ItemDecoration实例
         * @return ItemDecorationWrapper实例
         */
        fun build(): ItemDecorationWrapper {
            return itemDecoration
        }

        /**
         * 将dp单位转换为px单位
         * @param dp dp值
         * @return 对应的px值
         */
        private fun dpToPx(dp: Float): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density + 0.5f).toInt()
        }
    }
}