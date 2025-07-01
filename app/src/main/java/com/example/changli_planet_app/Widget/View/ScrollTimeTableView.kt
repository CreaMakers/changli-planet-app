package com.example.changli_planet_app.Widget.View

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import com.example.changli_planet_app.Activity.Action.TimeTableAction
import com.example.changli_planet_app.Interface.ScrollController
import com.zhuangfei.timetable.TimetableView

class ScrollTimeTableView(
    context: Context,
    attrs: AttributeSet?,
) : TimetableView(context, attrs) {   //继承TimetableView可实现左右滑动的监听
    private lateinit var scroll: ScrollController

    fun setScrollInterface(scroll: ScrollController) {
        this.scroll = scroll
    }

    private val gesture by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100 // 最小滑动距离
            private val SWIPE_VELOCITY_THRESHOLD = 100 // 最小滑动速度

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || e2 == null) return false

                val diffX = e2.x - e1.x // 横向滑动距离
                val diffY = e2.y - e1.y // 纵向滑动距离

                if (Math.abs(diffX) > Math.abs(diffY)) { // 判断是横向滑动
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            scroll.onScrollLast()
                        } else {
                            scroll.onScrollNext()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return gesture.onTouchEvent(ev) || super.onInterceptTouchEvent(ev)
    }
}