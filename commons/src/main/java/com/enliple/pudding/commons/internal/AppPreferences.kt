@file:JvmName("AppPreferences")

package com.enliple.pudding.commons.internal

import android.content.Context
import android.text.TextUtils
import com.enliple.commons.io.FileUtils
import com.enliple.pudding.commons.BuildConfig
import com.enliple.pudding.commons.app.HMacUtil
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkConst
import net.grandcentrix.tray.TrayPreferences
import net.grandcentrix.tray.core.ItemNotFoundException
import net.grandcentrix.tray.core.TrayStorage
import java.io.File

/**
 * Created by hkcha on 2018-01-08.
 * 앱 환경설정 By Kotlin With TrayPreferences
 */
class AppPreferences : TrayPreferences {
    constructor(context: Context, module: String, version: Int) : super(context, module, version, TrayStorage.Type.DEVICE)

    companion object {
        private const val TAG = "AppPreferences"

        @Volatile
        private var instance: AppPreferences? = null

        // FCM SENDER_ID
        const val FCM_SENDER_ID = 544123002560

        // SPLASH DELAY
        const val SPLASH_TIMEOUT_INTERVAL: Long = 1000L

        // TrayPreferences
        private val PREFERENCE_NAME: String = "com.enliple.pudding" + "_pref"
        private val PREFERENCE_VER: Int = 1

        // App Markets install URL
        const val MARKETURL_PLAYSTORE_INSTALL = "market://details?id=" + "com.enliple.pudding"

        // PushScheme
        const val PUSH_APPLINK_SCHEME = "VCommercePush"

        // 사용자 계정상태 타입
        const val USER_ACCOUNT_TYPE_NOT_LOGIN = -1
        const val USER_ACCOUNT_TYPE_ACCOUNT = 1
        const val USER_ACCOUNT_TYPE_FACEBOOK = 2
        const val USER_ACCOUNT_TYPE_GOOGLE = 3
        const val USER_ACCOUNT_TYPE_KAKAO = 4

        // 사용자 자동 로그인 시나리오 타입
        const val LOGIN_TYPE_NORMAL = 0
        const val LOGIN_TYPE_AUTO = 1
        const val LOGIN_TYPE_SAVE_ID = 2
        const val LOGIN_TYPE_ALL = 3

        // 더블클릭 앱 종료 제한시간
        const val DOUBLE_BACK_PRESS_EXITING_TIME_LIMIT = 2000L

        private fun getInstance(context: Context): AppPreferences {
            if (instance == null) {
                instance = AppPreferences(context, PREFERENCE_NAME, PREFERENCE_VER)
            }

            return instance as AppPreferences
        }


////////////////////////////////////////////////////////////////////////////////////////////////////
// 사용자 계정
////////////////////////////////////////////////////////////////////////////////////////////////////

        fun generatePublicHMAC(timeStamp: String): String = HMacUtil.hashMac("v1/auth/ptoken$timeStamp", "EnlipleLieon2018")

        fun generateUserHMAC(data: String): String = HMacUtil.hashMac(data, "EnlipleLieon2018")

        fun setUserId(context: Context, id: String): Boolean = getInstance(context).put(AppConstants.PREF_KEY_USER_ID, id)

        fun getUserId(context: Context): String? = try {
            getInstance(context).getString(AppConstants.PREF_KEY_USER_ID) ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        fun setCustomerCenterPhone(context: Context, phone: String): Boolean = getInstance(context).put(AppConstants.PREF_CUSTOMER_CENTER_PHONE, phone)

        fun getCustomerCenterPhone(context: Context): String? = try {
            getInstance(context).getString(AppConstants.PREF_CUSTOMER_CENTER_PHONE) ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        fun setUserPw(context: Context, pw: String): Boolean = getInstance(context).put(AppConstants.PREF_KEY_USER_PW, pw)

        fun getUserPw(context: Context): String? = try {
            getInstance(context).getString(AppConstants.PREF_KEY_USER_PW) ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        /**
         * Public JWT를 설정
         * @param context
         * @param token
         */
        fun setJWT(context: Context, token: String?): Boolean = if (!TextUtils.isEmpty(token)) {
            getInstance(context).put(AppConstants.PREF_KEY_AUTH_PUBLIC_JWT_TOKEN, token)
        } else {
            getInstance(context).put(AppConstants.PREF_KEY_AUTH_PUBLIC_JWT_TOKEN, "")
        }

        /**
         * 저장된 Public JWT를 확인
         * @param context
         * @return
         */
        fun getJWT(context: Context): String = try {
            getInstance(context).getString(AppConstants.PREF_KEY_AUTH_PUBLIC_JWT_TOKEN, "") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        /**
         * 사용자 로그인 계정 타입을 지정
         * @param context
         * @param type
         */
        fun setUserAccountType(context: Context, type: Int): Boolean =
                getInstance(context).put(AppConstants.PREF_KEY_USER_TYPE, type)

        /**
         * 사용자 로그인 계정 타입을 확인
         * @param context
         * @return
         */
        fun getUserAccountType(context: Context): Int = try {
            getInstance(context).getInt(AppConstants.PREF_KEY_USER_TYPE, USER_ACCOUNT_TYPE_NOT_LOGIN)
        } catch (e: ItemNotFoundException) {
            USER_ACCOUNT_TYPE_NOT_LOGIN
        }

        /**
         * 자동로그인 시나리오 타입을 설정
         * @param context
         * @param type
         * @return
         */
        fun setUserLoginType(context: Context, type: Int): Boolean = getInstance(context)
                .put(AppConstants.PREF_KEY_LOGIN_TYPE, type)

        /**
         * 자동로그인 시나리오 타입을 확인
         * @param context
         * @return
         */
        fun getUserLoginType(context: Context): Int = try {
            getInstance(context).getInt(AppConstants.PREF_KEY_LOGIN_TYPE, LOGIN_TYPE_NORMAL)
        } catch (e: ItemNotFoundException) {
            LOGIN_TYPE_NORMAL
        }

        /**
         * 사용자 로그아웃 처리
         */
        fun logout(context: Context): Boolean {
            var backupToken = getFCMRegistrationKey(context)
            var result = clearPreferences(context) /*& FileUtils.removeRecursive(context.filesDir)*/
            if (backupToken != null) {
                setFCMKey(context, backupToken)
            }

            return result
        }

        /**
         * 로그인 여부를 저장한다.
         * true : 로그인 / false : 비 로그인
         */
        fun setLoginStatus(context: Context, type: Boolean): Boolean = getInstance(context)
                .put(AppConstants.PREF_KEY_LOGIN_STATUS, type)

        /**
         * 저장된 로그인 status를 가져온다.
         * true : 로그인 / false : 비 로그인
         */
        fun getLoginStatus(context: Context): Boolean = try {
            getInstance(context).getBoolean(AppConstants.PREF_KEY_LOGIN_STATUS, false)
        } catch (e: ItemNotFoundException) {
            false
        }

        /**
         * 모바일 네트워크 사용시 알림 기능 on / off
         */
        fun setUseMobileAlert(context: Context, type: Boolean): Boolean = getInstance(context)
                .put(AppConstants.PREF_KEY_USE_MOBILE_ALERT, type)

        fun getUseMobileAlert(context: Context): Boolean = try {
            getInstance(context).getBoolean(AppConstants.PREF_KEY_USE_MOBILE_ALERT, true)
        } catch (e: ItemNotFoundException) {
            false
        }

        /**
         * 모바일 네트워크 사용시 해상도 설정
         */
        fun setMobileVideoResolution(context: Context, data: String): Boolean = getInstance(context)
                .put("MobileVideoResolution", data)

        fun getMobileVideoResolution(context: Context): String? = try {
            getInstance(context).getString("MobileVideoResolution", "고화질(720P)")
        } catch (e: ItemNotFoundException) {
            ""
        }

        /**
         * WiFi 네트워크 사용시 해상도 설정
         */
        fun setWiFiVideoResolution(context: Context, data: String): Boolean = getInstance(context)
                .put("WiFiVideoResolution", data)

        fun getWiFiVideoResolution(context: Context): String? = try {
            getInstance(context).getString("WiFiVideoResolution", "고화질(720P)")
        } catch (e: ItemNotFoundException) {
            ""
        }

        const val USER_EXPERIENCES_NORMAL = 0
        const val USER_EXPERIENCES_CASE_A = 1
        const val USER_EXPERIENCES_CASE_B = 2

        /**
         * 앱 에서 사용자가 경험할 모드를 설정 (USER A-B TEST)
         */
        fun setUserExperiencesMode(context: Context, plan: Int) = getInstance(context)
                .put(AppConstants.PREF_KEY_APP_USER_EXPERIENCES_KEY, plan)

        /**
         * 앱 에서 사용자가 경험할 모드를 확인 (USER A-B TEST)
         */
        fun getUserExperiencesMode(context: Context): Int = try {
            getInstance(context).getInt(AppConstants.PREF_KEY_APP_USER_EXPERIENCES_KEY, USER_EXPERIENCES_NORMAL)
        } catch (e: ItemNotFoundException) {
            USER_EXPERIENCES_NORMAL
        }


////////////////////////////////////////////////////////////////////////////////////////////////////
// 푸시 관리
////////////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * Push Notification Client registration Key 를 설정
         */
        fun setFCMKey(context: Context, regId: String) = getInstance(context)
                .put(AppConstants.PREF_KEY_FCM_CLIENT_REGISTRATION_KEY, regId)

        /**
         * Push Notification Client registration Key 를 확인
         * @return
         */
        fun getFCMRegistrationKey(context: Context): String? = try {
            getInstance(context).getString(AppConstants.PREF_KEY_FCM_CLIENT_REGISTRATION_KEY, null)
        } catch (e: ItemNotFoundException) {
            ""
        }

        /**
         * Push Message 를 수신받았을때 Notification 발생여부에 대한 설정
         */
        fun setHasPushNotificationShowing(context: Context, enable: Boolean) = getInstance(context)
                .put(AppConstants.PREF_KEY_FCM_SHOW_NOTIFICATION, enable)

        /**
         * Push Message 를 수신받았을때 Notification 을 발생시켜도 되는지 확인
         */
        fun hasPushNotificationShowing(context: Context): Boolean = try {
            getInstance(context).getBoolean(AppConstants.PREF_KEY_FCM_SHOW_NOTIFICATION, true)
        } catch (e: Exception) {
            true
        }

        /**
         * FCM Registration KEY 가 등록된 Application 의 Version code 를 확인
         */
        fun getFcmRegisteredAppVersionCode(context: Context): Int = try {
            getInstance(context).getInt(AppConstants.PREF_KEY_FCM_REGISTERED_VERSION_CODE, Integer.MIN_VALUE)
        } catch (e: ItemNotFoundException) {
            Integer.MIN_VALUE
        }

        /**
         * FCM Registration KEY 가 등록된 Application 의 Version code 를 기록
         */
        fun setFcmRegisteredAppVersionCode(context: Context, appVersionCode: Int) = getInstance(context)
                .put(AppConstants.PREF_KEY_FCM_REGISTERED_VERSION_CODE, appVersionCode)

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        // 앱 기능 관리
        ////////////////////////////////////////////////////////////////////////////////////////////////////


        /**
         * 개발자 모드에서 AppPreferences 정보를 모두 초기화
         */
        private fun clearPreferences(context: Context): Boolean {
            var backupToken = getFCMRegistrationKey(context)
            var result = getInstance(context).clear()
            if (backupToken != null) {
                setFCMKey(context, backupToken)
            }

            return result
        }

        /**
         * 개발자 모드에서 앱과 관련된 캐시 데이터를 모두 제거
         */
        fun clearCache(context: Context): Boolean {
            var cache: File = context.cacheDir
            return FileUtils.removeRecursive(cache)
        }

        /**
         * API Server 주소를 획득
         */
        fun getServerAddress(context: Context): String = NetworkConst.SERVER_API_URL

        /**
         * 홈 화면의 Video 로딩을 위해 사용할 카테고리 필터를 로드
         * @param context
         * @return
         */
        fun getHomeVideoCategory(context: Context): String = try {
            getInstance(context)
                    .getString(AppConstants.PREF_KEY_SELECTED_CATEGORY, "전체") ?: ",,"
        } catch (e: ItemNotFoundException) {
            ",,"
        }

        /**
         * 홈 화면의 Video 로딩을 위해 사용할 카테고리 필터를 저장
         * @param context
         * @param category
         * @return
         */
        fun setHomeVideoCategory(context: Context, category: String): Boolean = getInstance(context)
                .put(AppConstants.PREF_KEY_SELECTED_CATEGORY, category)


        /**
         * 홈 화면의 Video 로딩을 위해 사용할 카테고리 필터를 저장
         * @param context
         * @param categorys
         * @return
         */
        fun getHomeVideoCategoryCode(context: Context): String = try {
            getInstance(context).getString(AppConstants.PREF_KEY_SELECTED_CATEGORY_CODE, "") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        /**
         * 홈 화면의 Video 로딩을 위해 사용할 카테고리 필터를 저장
         * @param context
         * @param categorys
         * @return
         */
        fun setHomeVideoCategoryCode(context: Context, code: String): Boolean = getInstance(context)
                .put(AppConstants.PREF_KEY_SELECTED_CATEGORY_CODE, code)

        /**
         * 연령대를 가져온다.
         */
        fun getCategoryAge(context: Context): String = try {
            getInstance(context)
                    .getString(AppConstants.PREF_KEY_SELECTED_AGE, "all") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        /**
         * 연령대를 저장한다.
         */
        fun setCategoryAge(context: Context, ages: String): Boolean = getInstance(context)
                .put(AppConstants.PREF_KEY_SELECTED_AGE, ages)

        /**
         * 성별을 가져온다.
         */
        fun getCategoryGender(context: Context): String = try {
            getInstance(context)
                    .getString(AppConstants.PREF_KEY_SELECTED_GENDER, "all") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        /**
         * 성별을 저장한다.
         */
        fun setCategoryGender(context: Context, gender: String): Boolean = getInstance(context)
                .put(AppConstants.PREF_KEY_SELECTED_GENDER, gender)

        /**
         * 메인 데이터 저장
         * @param context
         * @param type
         */
        fun setMainData(context: Context, data: String): Boolean =
                getInstance(context).put(AppConstants.PREF_KEY_MAIN_DATA, data)

        /**
         * 메인 데이터 가져오기
         * @param context
         * @return
         */
        fun getMainData(context: Context): String = try {
            getInstance(context)
                    .getString(AppConstants.PREF_KEY_MAIN_DATA, "") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        /**
         * 방송정보 저장
         * @param context
         * @param type
         */
        fun setBroadcastInfo(context: Context, data: String): Boolean =
                getInstance(context).put(AppConstants.PREF_KEY_BROADCAST_INFO, data)

        /**
         * 방송정보 가져오기
         * @param context
         * @return
         */
        fun getBroadcastInfo(context: Context): String = try {
            getInstance(context).getString(AppConstants.PREF_KEY_BROADCAST_INFO, "") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        /**
         * 쇼핑 동영상 음소거 버튼
         * @param context
         * @return
         */
        fun getVideoMute(context: Context): Boolean = try {
            getInstance(context).getBoolean(AppConstants.PREF_KEY_VIDEO_MUTE, false)
        } catch (e: ItemNotFoundException) {
            false
        }

        fun setVideoMute(context: Context, mute: Boolean): Boolean = getInstance(context).put(AppConstants.PREF_KEY_VIDEO_MUTE, mute)

        /**
         * 쇼핑 동영상 볼륨
         * @param context
         * @return
         */
        fun getVideoVolumn(context: Context): Float = try {
            getInstance(context).getFloat(AppConstants.PREF_KEY_VIDEO_VOLUME, 0f)
        } catch (e: ItemNotFoundException) {
            0f
        }

        fun setVideoVolumn(context: Context, volume: Float) {
            getInstance(context).put(AppConstants.PREF_KEY_VIDEO_VOLUME, volume)
        }

        /**
         * 쇼핑 동영상 조회수 등록 시간
         * @param context
         * @return
         */
        fun getHitTime(context: Context): Int = try {
            getInstance(context).getInt(AppConstants.PREF_KEY_HIT_TIME, -1)
        } catch (e: ItemNotFoundException) {
            -1
        }

        fun setHitTime(context: Context, time: Int) {
            getInstance(context).put(AppConstants.PREF_KEY_HIT_TIME, time)
        }

        /**
         * 쇼핑 동영상 UI 숨김 시간
         * @param context
         * @return
         */
        fun getHideTime(context: Context): Int = try {
            getInstance(context).getInt(AppConstants.PREF_KEY_UI_HIDE_TIME, -1)
        } catch (e: ItemNotFoundException) {
            -1
        }

        fun setHideTiem(context: Context, time: Int) {
            getInstance(context).put(AppConstants.PREF_KEY_UI_HIDE_TIME, time)
        }

        /**
         * FCM Push Token
         * @param context
         * @return
         */
        fun getPushToken(context: Context): String = try {
            getInstance(context).getString(AppConstants.PREF_KEY_FCM_PUSH_TOKEN, "") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        fun setPushToken(context: Context, token: String) {
            if (!token.isNullOrEmpty()) {
                Logger.e("setPushToken:$token")
                getInstance(context).put(AppConstants.PREF_KEY_FCM_PUSH_TOKEN, token)
            }
        }

        /**
         * Set Screen Width
         * @param context
         * @return
         */
        fun setScreenWidth(context: Context, width: Int) {
            getInstance(context).put(AppConstants.PREF_KEY_SCREEN_WIDTH, width)
        }

        /**
         * Get Screen Width
         * @param context
         * @return
         */
        fun getScreenWidth(context: Context): Int = try {
            getInstance(context).getInt(AppConstants.PREF_KEY_SCREEN_WIDTH, -1) ?: -1
        } catch (e: ItemNotFoundException) {
            -1
        }

        /**
         * Set Screen Height
         * @param context
         * @return
         */
        fun setScreenHeight(context: Context, height: Int) {
            getInstance(context).put(AppConstants.PREF_KEY_SCREEN_HEIGHT, height)
        }

        /**
         * Get Screen Width , status bar height는 포함 안되어 있음
         * @param context
         * @return
         */
        fun getScreenHeight(context: Context): Int = try {
            getInstance(context).getInt(AppConstants.PREF_KEY_SCREEN_HEIGHT, -1) ?: -1
        } catch (e: ItemNotFoundException) {
            -1
        }

        /**
         * 저장된 장바구니 갯수를 가져옴.
         */
        fun getCartCnt(context: Context): String = try {
            getInstance(context)
                    .getString(AppConstants.PREF_KEY_CART_CNT, "0") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        /**
         * 장바구니 갯수를 저장함.
         */
        fun setCartCnt(context: Context, count: String): Boolean = getInstance(context)
                .put(AppConstants.PREF_KEY_CART_CNT, count)

        /**
         * 앱 버전 설정
         * @param context
         * @return
         */
        fun getAppVersion(context: Context): String = try {
            getInstance(context).getString(AppConstants.PREF_KEY_APP_VERSION, "") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        fun setAppVersion(context: Context, version: String) {
            getInstance(context).put(AppConstants.PREF_KEY_APP_VERSION, version)
        }

        /**
         * 강제 업데이트 여부
         * @param context
         * @return
         */
        fun getForceUpdate(context: Context): String = try {
            getInstance(context).getString(AppConstants.PREF_KEY_FORCE_UPDATE, "") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        fun setForceUpdate(context: Context, isForce: String) {
            getInstance(context).put(AppConstants.PREF_KEY_FORCE_UPDATE, isForce)
        }

        /**
         * 마켓 URL
         * @param context
         * @return
         */
        fun getMarketUrl(context: Context): String = try {
            getInstance(context).getString(AppConstants.PREF_KEY_MARKET_URL, "") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        fun setMarketUrl(context: Context, url: String) {
            getInstance(context).put(AppConstants.PREF_KEY_MARKET_URL, url)
        }

        /**
         * 등록일
         * @param context
         * @return
         */
        fun getAppRegdate(context: Context): String = try {
            getInstance(context).getString(AppConstants.PREF_KEY_APP_REG_DATE, "") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        fun setAppRegdate(context: Context, regDate: String) {
            getInstance(context).put(AppConstants.PREF_KEY_APP_REG_DATE, regDate)
        }

        /**
         * Video Pip 현재 재생 위치
         * @param context
         * @return
         */
        fun getPipPosition(context: Context): Long = try {
            getInstance(context).getLong(AppConstants.PREF_KEY_VIDEO_PIP_POSITION, 0)
        } catch (e: ItemNotFoundException) {
            0
        }

        fun setPipPosition(context: Context, position: Long) {
            getInstance(context).put(AppConstants.PREF_KEY_VIDEO_PIP_POSITION, position)
        }

        fun getPipKey(context: Context): String = try {
            getInstance(context).getString(AppConstants.PREF_KEY_VIDEO_PIP_KEY) ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        fun setPipKey(context: Context, streamKey: String) {
            getInstance(context).put(AppConstants.PREF_KEY_VIDEO_PIP_KEY, streamKey)
        }

        /**
         * Video Pip 허용화면 출력여부
         * @param context
         * @return
         */
        fun getPipPermission(context: Context): Boolean = try {
            getInstance(context).getBoolean(AppConstants.PREF_KEY_VIDEO_PIP_PERMISSION, false)
        } catch (e: ItemNotFoundException) {
            false
        }

        fun setPipPermission(context: Context, isPipPermission: Boolean) {
            getInstance(context).put(AppConstants.PREF_KEY_VIDEO_PIP_PERMISSION, isPipPermission)
        }

        /**
         * Notification Not Show 리스트
         * @param context
         * @return
         */
        fun getNotificationNotShowList(context: Context): String = try {
            getInstance(context).getString(AppConstants.PREF_KEY_NOTIFICATION_NOTSHOW, "") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        fun setNotificationNotShowList(context: Context, notShow: String) {
            getInstance(context).put(AppConstants.PREF_KEY_NOTIFICATION_NOTSHOW, notShow)
        }

        /**
         * Notification 리스트
         * @param context
         * @return
         */
        fun getNotificationList(context: Context): String = try {
            getInstance(context).getString(AppConstants.PREF_KEY_NOTIFICATION, "") ?: ""
        } catch (e: ItemNotFoundException) {
            ""
        }

        fun setNotificationList(context: Context, noti: String) {
            getInstance(context).put(AppConstants.PREF_KEY_NOTIFICATION, noti)
        }
    }
}