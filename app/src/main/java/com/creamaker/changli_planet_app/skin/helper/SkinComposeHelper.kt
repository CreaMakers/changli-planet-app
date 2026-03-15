package com.creamaker.changli_planet_app.skin.helper

import androidx.compose.ui.graphics.Color
import com.creamaker.changli_planet_app.skin.SkinManager

object SkinComposeHelper {

    @Suppress("UNCHECKED_CAST")
    private fun <T> wrapColor(colorInt: Int, isInCompose: Boolean): T {
        return if (isInCompose) Color(colorInt) as T else colorInt as T
    }

    /**
     * 根据原始资源ID获取当前皮肤颜色
     * 如果是 Compose，建议在 Composable 函数中调用此方法
     */
    fun getSkinColor(context: android.content.Context, resId: Int, isInCompose: Boolean = true): Any {
        if (resId == 0) return if (isInCompose) Color.Unspecified else 0

        val skinRes = SkinManager.skinResources
        val skinPkg = SkinManager.skinPackageName
        val appRes = context.resources

        // 如果没有皮肤资源，直接返回原始颜色
        if (skinRes == null || skinPkg.isNullOrEmpty()) {
            return wrapColor<Int>(appRes.getColor(resId, context.theme), isInCompose)
        }

        return try {
            val resName = appRes.getResourceEntryName(resId)
            val resType = appRes.getResourceTypeName(resId)
            val skinResId = skinRes.getIdentifier(resName, resType, skinPkg)

            if (skinResId != 0) {
                wrapColor(skinRes.getColor(skinResId, null), isInCompose)
            } else {
                wrapColor(appRes.getColor(resId, context.theme), isInCompose)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            wrapColor<Int>(appRes.getColor(resId, context.theme), isInCompose)
        }
    }
}
