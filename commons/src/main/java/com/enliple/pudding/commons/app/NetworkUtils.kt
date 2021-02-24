package com.enliple.pudding.commons.app

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager

/**
 * Created by hkcha on 2017-12-29.
 */
open class NetworkUtils {
    companion object {
        const val TYPE_NOT_CONNECTED: Int = 0
        const val TYPE_WIFI: Int = 1
        const val TYPE_MOBILE: Int = 2

        /**
         * 네트워크 연결상태 체크
         * @param context
         * @return
         */
        @SuppressLint("MissingPermission")
        fun getConnectivityStatus(context: Context?): Int {
            if (context != null) {
                var cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                var activeNetwork = cm.activeNetworkInfo

                if (activeNetwork != null) {
                    return when {
                        activeNetwork.type == ConnectivityManager.TYPE_WIFI -> TYPE_WIFI
                        activeNetwork.type == ConnectivityManager.TYPE_MOBILE -> TYPE_MOBILE
                        else -> TYPE_NOT_CONNECTED
                    }
                }
            }

            return TYPE_NOT_CONNECTED
        }

        /**
         * Device 의 WIFI Mac Address 를 확인
         */
        @SuppressLint("MissingPermission")
        fun getWifiMacAddr(context: Context): String? {
            var wifiManager: WifiManager? = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

            if (wifiManager != null) {
                return wifiManager.connectionInfo.macAddress
            }

            return null
        }
    }
}