import android.app.Activity
import android.view.View
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.skin.helper.SkinComposeHelper.getSkinColor
import com.github.gzuliyujiang.wheelpicker.LinkagePicker
import com.github.gzuliyujiang.wheelpicker.contract.LinkageProvider

class ProductCategoryPicker(
    private val activity: Activity, // 改为 val 以便在 getSkinColor 中使用
    defaultCategory: String = "数码产品",
    defaultSubcategory: String = "手机"
) {
    private val picker = LinkagePicker(activity)

    // 商品分类数据
    private val categories = listOf("数码产品", "家用电器", "服装", "美妆护肤", "食品", "图书音像")

    private val subcategories = mapOf(
        "数码产品" to listOf("手机", "平板电脑", "笔记本电脑", "智能手表", "耳机", "相机"),
        "娱乐" to listOf("游戏", "游戏设备"),
        "家用电器" to listOf("电视", "冰箱", "洗衣机", "空调", "微波炉", "电饭煲"),
        "服装" to listOf("上衣", "裤子", "裙子", "鞋子", "配饰", "内衣"),
        "美妆护肤" to listOf("面部护理", "彩妆", "香水", "洗护", "工具", "防晒"),
        "食品" to listOf("零食", "饮料", "生鲜", "粮油", "速食", "调味品"),
        "图书音像" to listOf("小说", "教材", "童书", "杂志", "音乐", "影视")
    )

    init {
        val linkageProvider = object : LinkageProvider {
            override fun firstLevelVisible() = true
            override fun thirdLevelVisible() = false
            override fun provideFirstData() = categories
            override fun linkageSecondData(firstIndex: Int): MutableList<*> {
                val category = categories[firstIndex]
                return subcategories[category]?.toMutableList() ?: mutableListOf<String>()
            }

            override fun linkageThirdData(firstIndex: Int, secondIndex: Int) = mutableListOf<String>()

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

            override fun findThirdIndex(firstIndex: Int, secondIndex: Int, thirdValue: Any?) = 0
        }

        // 设置数据
        picker.setData(linkageProvider)

        // 设置默认值
        picker.setDefaultValue(defaultCategory, defaultSubcategory, null)

        // 设置选择监听
        picker.setOnLinkagePickedListener { first, second, _ ->
            onCategorySelectedListener?.invoke(first.toString(), second.toString())
        }

        // --- 自定义 UI (使用 getSkinColor 获取颜色) ---

        // 1. 准备颜色
        val bgColor =
            getSkinColor(activity.applicationContext, R.color.color_bg_primary, false) as Int
        val okTextColor =
            getSkinColor(activity.applicationContext, R.color.color_text_functional, false) as Int
        val cancelTextColor =
            getSkinColor(activity.applicationContext, R.color.color_text_grey, false) as Int
        val titleTextColor =
            getSkinColor(activity.applicationContext, R.color.color_text_primary, false) as Int
        val wheelNormalColor =
            getSkinColor(activity.applicationContext, R.color.color_text_primary, false) as Int
        val wheelSelectedColor =
            getSkinColor(activity.applicationContext, R.color.color_text_highlight, false) as Int

        // 2. 设置 TopBar 样式
        picker.setBackgroundColor(bgColor) // 整体背景（包含 TopBar 和 Wheel）

        picker.okView.setTextColor(okTextColor)
        picker.cancelView.setTextColor(cancelTextColor)
        picker.titleView.setTextColor(titleTextColor)

        // 3. 设置 WheelLayout 样式
        picker.wheelLayout.apply {
            // 确保 WheelLayout 区域背景也是一致的
            setBackgroundColor(bgColor)

            firstLabelView.visibility = View.GONE // 隐藏标签
            thirdLabelView.visibility = View.GONE

            firstWheelView.apply {
                textSize = 55
                textColor = wheelNormalColor
                selectedTextColor = wheelSelectedColor
                isIndicatorEnabled = true
            }

            secondWheelView.apply {
                textSize = 55
                textColor = wheelNormalColor
                selectedTextColor = wheelSelectedColor
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