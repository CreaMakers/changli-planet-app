package com.example.changli_planet_app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.ui.platform.LocalFontLoader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.changli_planet_app.core.PlanetApplication
import com.example.changli_planet_app.core.network.HttpUrlHelper
import com.example.changli_planet_app.core.network.MyResponse
import com.example.changli_planet_app.core.network.OkHttpHelper
import com.example.changli_planet_app.core.network.listener.RequestCallback
import com.example.changli_planet_app.feature.common.ui.ElectronicActivity
import com.gradle.scan.agent.serialization.scan.serializer.kryo.by
import com.tencent.mmkv.MMKV
import okhttp3.Response
import kotlin.getValue
import kotlin.jvm.java


class ElectronicAppWidget: AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (appWidgetIds != null) {
            for (appWidgetId in appWidgetIds){
                updateAppWidget(context,appWidgetManager,appWidgetId)
            }
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Log.d("ele_widget","enabled")
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        Log.d("ele_widget","Disabled")
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        Log.d("ele_widget","Deleted")
    }
    private val  mmkv by lazy { MMKV.defaultMMKV() }

    private val TAG = "Electronic_Widget"

    private val time_refresh by lazy { getRefreshTime() }
    private val dor by lazy { mmkv.getString("dor",null) }
    private val dor_number by lazy { mmkv.getString("door_number",null) }
    private val school by lazy{mmkv.getString("school",null)}



    internal fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: Int
    ){
        val views = RemoteViews(context.packageName,R.layout.eletronic_app_widget)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetIds)
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val DensityDpi = context.resources.displayMetrics.densityDpi

        GetAdaptTextSize(minWidth,minHeight,DensityDpi){ smallSize,largeSize ->
            ApplyTextSize(smallSize,largeSize,views)
        }

        val intent = Intent(context, ElectronicActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            putExtra("ele_widget_id", appWidgetIds)
        }
        val  PendingIntent = PendingIntent.getActivity(context,appWidgetIds,intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.ele_widget_root_layout,PendingIntent)
        val time_refresh =  getRefreshTime()
        val dor  = mmkv.getString("dor",null)
        val dor_number = mmkv.getString("door_number",null)
        val school = mmkv.getString("school",null)

        updateData(appWidgetIds,appWidgetManager,views)
        Log.d(TAG, time_refresh)
        Log.d(TAG,"${school},${dor},${dor_number}")
    }

    private fun updateData(appWidgetIds: Int, appWidgetManager: AppWidgetManager,views: RemoteViews) {
        getEletronic(dor,dor_number,school) { ele_number ->
            views.setTextViewText(R.id.ele_widget_num,ele_number)
            Log.d(TAG, ele_number)
            views.setTextViewText(R.id.refresh_time,time_refresh)
            views.setTextViewText(R.id.dor_ele,dor)
            views.setTextViewText(R.id.door_ele,dor_number)
            views.setTextViewText(R.id.school_ele,school)
            appWidgetManager.updateAppWidget(appWidgetIds,views)
        }

    }

    private fun ApplyTextSize(smallSize: Int,largeSize : Int,views: RemoteViews){
        views.setTextViewTextSize(R.id.ele_title, TypedValue.COMPLEX_UNIT_DIP,smallSize.toFloat())
        views.setTextViewTextSize(R.id.door_ele, TypedValue.COMPLEX_UNIT_DIP,smallSize.toFloat())
        views.setTextViewTextSize(R.id.dor_ele, TypedValue.COMPLEX_UNIT_DIP,smallSize.toFloat())
        views.setTextViewTextSize(R.id.school_ele, TypedValue.COMPLEX_UNIT_DIP,smallSize.toFloat())
        views.setTextViewTextSize(R.id.refresh_time_text, TypedValue.COMPLEX_UNIT_DIP,smallSize.toFloat())
        views.setTextViewTextSize(R.id.refresh_time, TypedValue.COMPLEX_UNIT_DIP,smallSize.toFloat())

    }
    private fun GetAdaptTextSize(
        widgetWidth: Int,
        widgetHeight: Int,
        densityDpi : Int,
        callback: (Int,Int) -> Unit
    ) {
        val widgetArea = widgetWidth * widgetHeight

        Log.d(
            TAG,
            "Adaptation calc - densityDpi: $densityDpi, width: $widgetWidth, height: $widgetHeight, area: $widgetArea"
        )

        when {
            // vivo手机: densityDpi 489, 128x152, area 19456 - 高密度小空间
            densityDpi >= 480 && widgetArea < 20000 -> {
                Log.d(TAG, "Using high density small widget config")
                callback(12, 14)
            }

            // 小米手机: densityDpi 419, 146x164, area 23944 - 中密度大空间
            densityDpi in 400..479 && widgetArea > 23000 -> {
                Log.d(TAG, "Using medium density large widget config")
                callback(11, 13)
            }

            // 超高密度设备
            densityDpi >= 560 -> {
                if (widgetArea < 22000) callback(11, 12) else callback(12, 13)
            }

            // 中高密度设备
            densityDpi in 440..559 -> {
                if (widgetArea < 20000) callback(12, 13) else callback(13, 14)
            }

            // 中密度设备
            densityDpi in 320..439 -> {
                if (widgetArea > 25000) callback(13, 14) else callback(12, 13)
            }

            // 低密度设备
            densityDpi < 320 -> {
                callback(14, 15)
            }

            // 默认配置
            else -> {
                Log.d(TAG, "Using default config")
                if (widgetArea > 22000) callback(13, 14) else callback(12, 13)
            }
        }
    }


    private fun getEletronic(dor: String?, dorNumber: String?, school: String?,
                             callback: (ele_number : String) -> Unit) {
        if (dor != null && dorNumber != null && school != null) {
            val builder = HttpUrlHelper.HttpRequest()
                .get(PlanetApplication.ToolIp + "/dormitory-electricity")
                .addQueryParam("address", school)
                .addQueryParam("buildId", dor)
                .addQueryParam("nod", dorNumber)
                .build()
            OkHttpHelper.sendRequest(builder,object : RequestCallback{
                override fun onSuccess(response: Response) {
                    val Json = OkHttpHelper.gson.fromJson(
                        response.body?.string(),
                        MyResponse::class.java
                    )
                    callback(Json.msg)
                }

                override fun onFailure(error: String) {
                    callback("网络错误，无法获取当前电量喵~")
                }

            })
        }else{
            callback("当前宿舍信息不全，请点击前往查询页面绑定数据捏~")
        }
    }


}

private fun getRefreshTime(): String{
    val dateFormat = SimpleDateFormat("MM-dd | HH:mm", Locale.getDefault())
    return dateFormat.format(Date())

}
