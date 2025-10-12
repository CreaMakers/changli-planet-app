package com.creamaker.changli_planet_app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log

class NetworkUtil {


    sealed class NetworkType {


        object None : NetworkType()
        object Wifi : NetworkType()
        object Mobile2G : NetworkType()
        object Mobile3G : NetworkType()
        object Mobile4G : NetworkType()
        object Mobile5G : NetworkType()
        object MobileUnknown : NetworkType()

    }

    companion object {
        @Suppress("DEPRECATION")
        fun getNetworkType(context: Context): NetworkType {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return NetworkType.None

            // Android 10 (Q)及以上版本使用新API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val networkCapabilities = connectivityManager.activeNetwork ?: return NetworkType.None
                val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return NetworkType.None

                return when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.Wifi
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        // 获取电话服务
                        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                        //电话权限由调用方获取，在调用该方法前确保电话权限获取
                        val tm = try {
                            telephonyManager?.dataNetworkType
                        }catch (e:SecurityException){
                            Log.w("NetworkUtil","缺少权限，请在调用页面请求")
                        }
                        when (tm) {
                            TelephonyManager.NETWORK_TYPE_NR -> NetworkType.Mobile5G // 5G
                            TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.Mobile4G // 4G
                            TelephonyManager.NETWORK_TYPE_UMTS,
                            TelephonyManager.NETWORK_TYPE_EVDO_0,
                            TelephonyManager.NETWORK_TYPE_EVDO_A,
                            TelephonyManager.NETWORK_TYPE_HSDPA,
                            TelephonyManager.NETWORK_TYPE_HSUPA,
                            TelephonyManager.NETWORK_TYPE_HSPA,
                            TelephonyManager.NETWORK_TYPE_EVDO_B,
                            TelephonyManager.NETWORK_TYPE_EHRPD,
                            TelephonyManager.NETWORK_TYPE_HSPAP -> NetworkType.Mobile3G // 3G
                            TelephonyManager.NETWORK_TYPE_GPRS,
                            TelephonyManager.NETWORK_TYPE_EDGE,
                            TelephonyManager.NETWORK_TYPE_CDMA,
                            TelephonyManager.NETWORK_TYPE_1xRTT,
                            TelephonyManager.NETWORK_TYPE_IDEN -> NetworkType.Mobile2G // 2G
                            else -> NetworkType.MobileUnknown
                        }
                    }
                    else -> NetworkType.None
                }
            } else {
                // 兼容旧版本
                @Suppress("DEPRECATION")
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo == null || !activeNetworkInfo.isConnected) {
                    return NetworkType.None
                }

                @Suppress("DEPRECATION")
                return when (activeNetworkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> NetworkType.Wifi
                    ConnectivityManager.TYPE_MOBILE -> {
                        when (activeNetworkInfo.subtype) {
                            TelephonyManager.NETWORK_TYPE_NR -> NetworkType.Mobile5G // 5G
                            TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.Mobile4G // 4G
                            TelephonyManager.NETWORK_TYPE_UMTS,
                            TelephonyManager.NETWORK_TYPE_EVDO_0,
                            TelephonyManager.NETWORK_TYPE_EVDO_A,
                            TelephonyManager.NETWORK_TYPE_HSDPA,
                            TelephonyManager.NETWORK_TYPE_HSUPA,
                            TelephonyManager.NETWORK_TYPE_HSPA,
                            TelephonyManager.NETWORK_TYPE_EVDO_B,
                            TelephonyManager.NETWORK_TYPE_EHRPD,
                            TelephonyManager.NETWORK_TYPE_HSPAP -> NetworkType.Mobile3G // 3G
                            TelephonyManager.NETWORK_TYPE_GPRS,
                            TelephonyManager.NETWORK_TYPE_EDGE,
                            TelephonyManager.NETWORK_TYPE_CDMA,
                            TelephonyManager.NETWORK_TYPE_1xRTT,
                            TelephonyManager.NETWORK_TYPE_IDEN -> NetworkType.Mobile2G // 2G
                            else -> NetworkType.MobileUnknown
                        }
                    }
                    else -> NetworkType.None
                }
            }
        }




    }
}