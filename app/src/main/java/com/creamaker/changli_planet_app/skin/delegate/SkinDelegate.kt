package com.creamaker.changli_planet_app.skin.delegate

import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.creamaker.changli_planet_app.skin.SkinAttributeProvider
import com.creamaker.changli_planet_app.skin.SkinManager
import com.creamaker.changli_planet_app.skin.SkinSupportable
import androidx.core.graphics.drawable.toDrawable
import com.github.gzuliyujiang.wheelview.widget.WheelView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import org.xmlpull.v1.XmlPullParser

class SkinDelegate(
    view: View,
    private val provider: SkinAttributeProvider
) : SkinSupportable {
    private val TAG = "SkinDelegate"
    private val viewRef = java.lang.ref.WeakReference(view)
    private val skinAttrs = mutableMapOf<String, Int>()
    fun setAttr(attrName: String, resId: Int) {
        if (!skinAttrs.containsKey(attrName)) {
            skinAttrs[attrName] = resId
        }
    }

    fun loadSkinAttributes(context: Context, attrs: AttributeSet?) {
        skinAttrs.clear()
        provider.loadSkinAttributes(context, attrs)
    }

    override fun applySkin() {
        val view = viewRef.get() ?: return
        if (skinAttrs.isNotEmpty()) {
            val skinRes = SkinManager.skinResources
            val skinPkg = SkinManager.skinPackageName
            val appRes = SkinManager.appResources

            skinAttrs.forEach { (attrName, originalResId) ->
                val resName = appRes.getResourceEntryName(originalResId)
                val resType = appRes.getResourceTypeName(originalResId)
                val skinResId = skinRes?.getIdentifier(resName, resType, skinPkg ?: "") ?: 0
                val skinDrawableId = skinRes?.getIdentifier(resName, resType, skinPkg ?: "") ?: 0
                when (attrName) {
                    "tv_text_color" -> {
                        val color = if (skinResId != 0)
                            skinRes!!.getColor(skinResId, null)
                        else
                            appRes.getColor(originalResId, view.context.theme)

                        (view as TextView).setTextColor(color)
                    }

                    "tv_background", "view_background" -> {
                        if (skinDrawableId != 0) {
                            // 1. 皮肤包里有同名 drawable/xml，直接整体替换
                            val drawable = loadBackgroundDrawable(skinRes!!, skinDrawableId, view.context)
                            drawable?.let { view.background = it }
                        } else {
                            // 2. 【核心修改】皮肤包没 XML，解析原 XML 寻找引用的颜色
                            var replaced = false

                            // 只有当原资源是 XML 并且是 drawable 时才尝试解析
                            if (resType == "drawable" && isXmlResource(appRes, originalResId)) {
                                // A. 解析 XML 拿到内部引用的 colorResId (例如 R.color.main_color)
                                val colorResIdInsideXml = getColorIdInShapeTagFromDrawableXml(view.context, originalResId)

                                if (colorResIdInsideXml != 0) {
                                    // B. 拿到颜色名 (main_color)
                                    val colorName = try { appRes.getResourceEntryName(colorResIdInsideXml) } catch (e:Exception){ null }

                                    if (colorName != null) {
                                        // C. 去皮肤包找这个颜色名
                                        val skinColorId = skinRes?.getIdentifier(colorName, "color", skinPkg ?: "") ?: 0

                                        if (skinColorId != 0) {
                                            // D. 找到了！修改原 Drawable 的颜色
                                            val skinColor = skinRes!!.getColor(skinColorId, null)
                                            val originalDrawable = loadBackgroundDrawable(appRes, originalResId, view.context)

                                            originalDrawable?.mutate()?.let { targetDrawable ->
                                                if (targetDrawable is GradientDrawable) {
                                                    targetDrawable.setColor(skinColor)
                                                } else {
                                                    DrawableCompat.setTint(targetDrawable, skinColor)
                                                }
                                                view.background = targetDrawable
                                                replaced = true
                                                Log.d(TAG, "Changed shape color using skin color: $colorName")
                                            }
                                        }
                                    }
                                }
                            }

                            // 如果没替换成功，回退到默认加载
                            if (!replaced) {
                                val originalDrawable = loadBackgroundDrawable(appRes, originalResId, view.context)
                                originalDrawable?.let { view.background = it }
                            }
                        }
                    }
                    "card_background_color" -> {
                        // 1. 尝试获取 ColorStateList (支持 selector)
                        val colorStateList = if (skinResId != 0) {
                            try {
                                skinRes!!.getColorStateList(skinResId)
                            } catch (e: Exception) {
                                // 如果皮肤包里只是纯色值不是 xml，回退获取 int color
                                android.content.res.ColorStateList.valueOf(
                                    skinRes!!.getColor(skinResId, null)
                                )
                            }
                        } else {
                            appRes.getColorStateList(originalResId)
                        }

                        // 2. 使用 setCardBackgroundColor 而不是 setBackground
                        colorStateList.let {
                            (view as MaterialCardView).setCardBackgroundColor(it)
                        }
                    }
                    "tab_text_color" -> {
                        if (view is TabLayout) {
                            // 1. 获取皮肤包中的新颜色 (默认状态)
                            val skinColor = if (skinResId != 0) skinRes!!.getColor(skinResId, null)
                            else appRes.getColor(originalResId, view.context.theme)

                            // 2. 获取当前 TabLayout 的选中颜色 (保持不变)
                            // tabTextColors 是 ColorStateList，defaultColor 通常就是未选中颜色，
                            // 但我们需要选中的颜色，所以通过状态获取
                            val currentColors = view.tabTextColors
                            val currentSelectedColor = currentColors?.getColorForState(
                                intArrayOf(android.R.attr.state_selected),
                                skinColor // fallback
                            ) ?: skinColor

                            // 3. 重新设置两个颜色
                            view.setTabTextColors(skinColor, currentSelectedColor)
                        }
                    }

                    "tab_selected_text_color" -> {
                        if (view is TabLayout) {
                            // 1. 获取皮肤包中的新颜色 (选中状态)
                            val skinSelectedColor = if (skinResId != 0) skinRes!!.getColor(skinResId, null)
                            else appRes.getColor(originalResId, view.context.theme)

                            // 2. 获取当前 TabLayout 的默认颜色 (保持不变)
                            val currentColors = view.tabTextColors
                            val currentNormalColor = currentColors?.defaultColor ?: skinSelectedColor

                            // 3. 重新设置两个颜色
                            view.setTabTextColors(currentNormalColor, skinSelectedColor)
                        }
                    }
                    "btn_stroke_color" -> {
                        if (view is MaterialButton) {
                            val colorStateList = if (skinResId != 0) {
                                try { skinRes!!.getColorStateList(skinResId) }
                                catch (e: Exception) { android.content.res.ColorStateList.valueOf(skinRes!!.getColor(skinResId, null)) }
                            } else {
                                appRes.getColorStateList(originalResId)
                            }
                            view.strokeColor = colorStateList
                        }
                    }

                    "btn_icon_tint" -> {
                        if (view is MaterialButton) {
                            val colorStateList = if (skinResId != 0) {
                                try { skinRes!!.getColorStateList(skinResId) }
                                catch (e: Exception) { android.content.res.ColorStateList.valueOf(skinRes!!.getColor(skinResId, null)) }
                            } else {
                                appRes.getColorStateList(originalResId)
                            }
                            view.iconTint = colorStateList
                        }
                    }
                    "et_text_color" -> {
                        if (view is EditText) {
                            // 1. 获取 ColorStateList (支持 selector)
                            val colorStateList = if (skinResId != 0) {
                                try {
                                    skinRes!!.getColorStateList(skinResId)
                                } catch (e: Exception) {
                                    // 兼容纯色值
                                    android.content.res.ColorStateList.valueOf(skinRes!!.getColor(skinResId, null))
                                }
                            } else {
                                appRes.getColorStateList(originalResId)
                            }

                            // 2. 设置颜色
                            view.setTextColor(colorStateList)
                        }
                    }

                    "et_text_color_hint" -> {
                        if (view is EditText) {
                            // 1. 获取 ColorStateList
                            val colorStateList = if (skinResId != 0) {
                                try {
                                    skinRes!!.getColorStateList(skinResId)
                                } catch (e: Exception) {
                                    android.content.res.ColorStateList.valueOf(skinRes!!.getColor(skinResId, null))
                                }
                            } else {
                                appRes.getColorStateList(originalResId)
                            }

                            // 2. 设置 Hint 颜色
                            view.setHintTextColor(colorStateList)
                        }
                    }
                    "wheel_text_color" -> {
                        if (view is WheelView) {
                            // WheelView 的 setTextColor 通常只支持 int，不支持 ColorStateList
                            val color = if (skinResId != 0)
                                skinRes!!.getColor(skinResId, null)
                            else
                                appRes.getColor(originalResId, view.context.theme)

                            view.setTextColor(color)
                        }
                    }

                    "wheel_selected_text_color" -> {
                        if (view is WheelView) {
                            val color = if (skinResId != 0)
                                skinRes!!.getColor(skinResId, null)
                            else
                                appRes.getColor(originalResId, view.context.theme)

                            view.setSelectedTextColor(color)
                        }
                    }
                }
            }
        }
    }

    private fun loadBackgroundDrawable(
        res: Resources,
        resId: Int,
        context: Context
    ): Drawable? {
        return try {
            val type = res.getResourceTypeName(resId)

            when (type) {
                "color" -> {
                    // Background 是纯颜色时返回 ColorDrawable
                    val color = ResourcesCompat.getColor(res, resId, context.theme)
                    color.toDrawable()
                }

                "drawable", "mipmap", "xml" -> {
                    ResourcesCompat.getDrawable(res, resId, context.theme)
                }

                else -> {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun getColorIdInShapeTagFromDrawableXml(context: Context, drawableResId: Int): Int {
        var parser: XmlResourceParser? = null
        try {
            parser = context.resources.getXml(drawableResId)
            var eventType = parser.eventType

            // 遍历 XML 标签
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    val tagName = parser.name
                    // 我们只关心 <solid> 标签，如果你还需要支持 stroke，可以加判断
                    when (tagName) {
                        "solid" -> {
                            val count = parser.attributeCount
                            for (i in 0 until count) {
                                val attrName = parser.getAttributeName(i)
                                // 找到 android:color 属性
                                if ("color" == attrName) {
                                    // 获取属性值对应的资源 ID
                                    val resValue = parser.getAttributeValue(i)
                                    if (resValue.startsWith("@")) {
                                        // 这是一个引用资源
                                        return parser.getAttributeResourceValue(i, 0)
                                    }
                                }
                            }
                        }
                        "stroke" -> {
                            val count = parser.attributeCount
                            for (i in 0 until count) {
                                val attrName = parser.getAttributeName(i)
                                if("color" == attrName) {
                                    val resValue = parser.getAttributeValue(i)
                                    if (resValue.startsWith("@")) {
                                        return parser.getAttributeResourceValue(i, 0)
                                    }
                                }
                            }
                        }
                        "gradient" -> {
                            val count = parser.attributeCount
                            for(i in 0 until count ){
                                val attrName = parser.getAttributeName(i)
                                if("startColor" == attrName || "centerColor" == attrName || "endColor" == attrName) {
                                    val resValue = parser.getAttributeValue(i)
                                    if (resValue.startsWith("@")) {
                                        return parser.getAttributeResourceValue(i, 0)
                                    }
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // XML 解析失败，可能是由 bitmap 构成的 drawable，不是 shape xml
            // e.printStackTrace()
        } finally {
            parser?.close()
        }
        return 0
    }
    private fun isXmlResource(res: Resources, resId: Int): Boolean {
        val typeValue = android.util.TypedValue()
        res.getValue(resId, typeValue, true)
        // 如果是字符串并且以 .xml 结尾
        return typeValue.string?.toString()?.endsWith(".xml") == true
    }
}