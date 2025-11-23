package com.creamaker.changli_planet_app.skin.helper

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.creamaker.changli_planet_app.skin.SkinManager

object SkinComposeHelper {

    /**
     * 根据原始资源ID获取当前皮肤颜色
     * 如果是 Compose，建议在 Composable 函数中调用此方法
     */
    fun getSkinColor(context: android.content.Context, resId: Int,isInCompose: Boolean = true): Any {
        if (resId == 0) return Color.Unspecified

        val skinRes = SkinManager.skinResources
        val skinPkg = SkinManager.skinPackageName
        val appRes = context.resources

        // 如果没有皮肤资源，直接返回原始颜色
        if (skinRes == null || skinPkg.isNullOrEmpty()) {
            return Color(appRes.getColor(resId, context.theme))
        }

        return try {
            val resName = appRes.getResourceEntryName(resId)
            val resType = appRes.getResourceTypeName(resId)
            val skinResId = skinRes.getIdentifier(resName, resType, skinPkg)

            if (skinResId != 0) {
                if (isInCompose){
                    Color(skinRes.getColor(skinResId, null))
                }
                else{
                    skinRes.getColor(skinResId,null)
                }
            } else {
                if (isInCompose){
                    Color(appRes.getColor(resId, context.theme))
                }
                else{
                    appRes.getColor(resId, context.theme)
                }

            }
        } catch (e: Exception) {
            Color(appRes.getColor(resId, context.theme))
        }
    }
}