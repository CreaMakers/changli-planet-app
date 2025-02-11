package com.example.changli_planet_app.Widget.Picker

import android.app.Activity
import android.graphics.Color
import android.view.View
import com.github.gzuliyujiang.wheelpicker.LinkagePicker
import com.github.gzuliyujiang.wheelpicker.contract.LinkageProvider

class LessonPicker(activity: Activity) {
    private val picker = LinkagePicker(activity)
    private val totalLessons = 10

    init {
        val linkageProvider = object : LinkageProvider {
            override fun firstLevelVisible(): Boolean {
                return true
            }

            override fun thirdLevelVisible(): Boolean {
                return false // 不显示第三级选择器
            }

            override fun provideFirstData(): List<String> {
                return (1..totalLessons).map { "第${it}节" }
            }

            override fun linkageSecondData(firstIndex: Int): MutableList<*> {
                val start = firstIndex + 1 // 从选中节数的下一节开始
                return (start..totalLessons).map { "第${it}节" }.toMutableList()
            }

            override fun linkageThirdData(firstIndex: Int, secondIndex: Int): MutableList<*> {
                return mutableListOf<String>() // 不需要第三级数据
            }

            override fun findFirstIndex(firstValue: Any?): Int {
                return firstValue?.toString()?.let {
                    val lesson = it.replace("第", "").replace("节", "").toIntOrNull() ?: 1
                    (lesson - 1).coerceIn(0, totalLessons - 1)
                } ?: 0
            }

            override fun findSecondIndex(firstIndex: Int, secondValue: Any?): Int {
                return secondValue?.toString()?.let {
                    val lesson = it.replace("第", "").replace("节", "").toIntOrNull() ?: (1)
                    val start = firstIndex + 1
                    (lesson - start).coerceIn(0, totalLessons - start)
                } ?: 0
            }

            override fun findThirdIndex(firstIndex: Int, secondIndex: Int, thirdValue: Any?): Int {
                return 0
            }
        }

        // 设置数据
        picker.setData(linkageProvider)

        // 设置默认值
        picker.setDefaultValue("第1节", "第1节", null)

        // 设置选择监听
        picker.setOnLinkagePickedListener { first, second, _ ->
            val startLesson = first.toString().replace("第", "").replace("节", "").toInt()
            val endLesson = second.toString().replace("第", "").replace("节", "").toInt()
            onLessonSelectedListener?.invoke(startLesson, endLesson)
        }

        // 自定义UI
        picker.wheelLayout.apply {
            firstLabelView.text = "到"
            thirdLabelView.visibility = View.GONE

            firstWheelView.apply {
                textSize = 55
                textColor = Color.BLACK
                selectedTextColor = Color.BLUE
                isIndicatorEnabled = true
            }

            secondWheelView.apply {
                textSize = 55
                textColor = Color.BLACK
                selectedTextColor = Color.BLUE
                isIndicatorEnabled = true
            }
        }
    }

    private var onLessonSelectedListener: ((start: Int, end: Int) -> Unit)? = null

    fun setOnLessonSelectedListener(listener: (start: Int, end: Int) -> Unit) {
        onLessonSelectedListener = listener
    }

    fun show() {
        picker.show()
    }

    fun dismiss() {
        picker.dismiss()
    }
}