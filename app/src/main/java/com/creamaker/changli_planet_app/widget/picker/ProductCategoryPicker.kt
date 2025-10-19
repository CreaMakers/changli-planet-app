package com.creamaker.changli_planet_app.widget.picker

import android.app.Activity
import android.graphics.Color
import android.view.View
import com.github.gzuliyujiang.wheelpicker.LinkagePicker
import com.github.gzuliyujiang.wheelpicker.contract.LinkageProvider

class ProductCategoryPicker(activity: Activity, defaultCategory: String = "数码产品", defaultSubcategory: String = "手机") {
    private val picker = LinkagePicker(activity)

    // 商品分类数据
    private val categories = listOf("数码产品", "家用电器", "服装", "美妆护肤", "食品", "图书音像")

    private val subcategories = mapOf(
        "数码产品" to listOf("手机", "平板电脑", "笔记本电脑", "智能手表", "耳机", "相机"),
        "娱乐" to listOf("游戏","游戏设备"),
        "家用电器" to listOf("电视", "冰箱", "洗衣机", "空调", "微波炉", "电饭煲"),
        "服装" to listOf("上衣", "裤子", "裙子", "鞋子", "配饰", "内衣"),
        "美妆护肤" to listOf("面部护理", "彩妆", "香水", "洗护", "工具", "防晒"),
        "食品" to listOf("零食", "饮料", "生鲜", "粮油", "速食", "调味品"),
        "图书音像" to listOf("小说", "教材", "童书", "杂志", "音乐", "影视")
    )

    init {
        val linkageProvider = object : LinkageProvider {
            override fun firstLevelVisible(): Boolean {
                return true
            }

            override fun thirdLevelVisible(): Boolean {
                return false
            }

            override fun provideFirstData(): List<String> {
                return categories
            }

            override fun linkageSecondData(firstIndex: Int): MutableList<*> {
                val category = categories[firstIndex]
                return subcategories[category]?.toMutableList() ?: mutableListOf<String>()
            }

            override fun linkageThirdData(firstIndex: Int, secondIndex: Int): MutableList<*> {
                return mutableListOf<String>() // 不需要第三级数据
            }

            override fun findFirstIndex(firstValue: Any?): Int {
                return firstValue?.toString()?.let {
                    categories.indexOf(it).coerceAtLeast(0)
                } ?: 0
            }

            override fun findSecondIndex(firstIndex: Int, secondValue: Any?): Int {
                val category = categories.getOrNull(firstIndex) ?: return 0
                return secondValue?.toString()?.let {
                    subcategories[category]?.indexOf(it)?.coerceAtLeast(0) ?: 0
                } ?: 0
            }

            override fun findThirdIndex(firstIndex: Int, secondIndex: Int, thirdValue: Any?): Int {
                return 0
            }
        }

        // 设置数据
        picker.setData(linkageProvider)

        // 设置默认值
        picker.setDefaultValue(defaultCategory, defaultSubcategory, null)

        // 设置选择监听
        picker.setOnLinkagePickedListener { first, second, _ ->
            val category = first.toString()
            val subcategory = second.toString()
            onCategorySelectedListener?.invoke(category, subcategory)
        }

        // 自定义UI
        picker.wheelLayout.apply {
            firstLabelView.visibility = View.GONE // 隐藏标签
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

    private var onCategorySelectedListener: ((category: String, subcategory: String) -> Unit)? = null

    fun setOnCategorySelectedListener(listener: (category: String, subcategory: String) -> Unit) {
        onCategorySelectedListener = listener
    }

    fun show() {
        picker.show()
    }

    fun dismiss() {
        picker.dismiss()
    }
}