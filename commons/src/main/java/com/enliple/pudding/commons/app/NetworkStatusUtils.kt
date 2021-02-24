@file:JvmName("NetworkStatusUtils")

package com.enliple.pudding.commons.app

/**
 * Created by hkcha on 2018-01-08.
 */
class NetworkStatusUtils {
    enum class NetworkStatus {
        WIFI,                                   // WIFI 네트워크 사용 중
        MOBILE,                                 // 모바일 네트워크 사용 중
        UNREACHABLE,                            // 연결불가능 상태
        UNKNOWN                                 // 알수없는 상태
    }

    companion object {
        fun getNetworkStatus(networkUtilsConst: Int): NetworkStatus {
            return when (networkUtilsConst) {
                NetworkUtils.TYPE_WIFI -> NetworkStatus.WIFI
                NetworkUtils.TYPE_MOBILE -> NetworkStatus.MOBILE
                NetworkUtils.TYPE_NOT_CONNECTED -> NetworkStatus.UNREACHABLE
                else -> NetworkStatus.UNKNOWN
            }
        }
    }
}