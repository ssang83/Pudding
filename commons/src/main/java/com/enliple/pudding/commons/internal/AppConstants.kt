package com.enliple.pudding.commons.internal

/**
 * Created by hkcha on 2018-01-08.
 * Application Constants (PREF, Etc...)
 */
interface AppConstants {
    companion object {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // PREFERENCE_KEY
        ////////////////////////////////////////////////////////////////////////////////////////////

        // App Management
        const val PREF_KEY_APP_USER_EXPERIENCES_KEY = "com.enliple.pudding.PREF_KEY_APP_USER_EXPERIENCES_KEY"
        const val PREF_KEY_SERVER_API_ADDR_IDX_KEY = "com.enliple.pudding.PREF_KEY_SERVER_API_ADDR_IDX_KEY"
        const val PREF_KEY_FCM_CLIENT_REGISTRATION_KEY = "com.enliple.pudding.PREF_KEY_FCM_CLIENT_REGISTRATION_KEY"
        const val PREF_KEY_FCM_SHOW_NOTIFICATION = "com.enliple.pudding.PREF_KEY_FCM_SHOW_NOTIFICATION"
        const val PREF_KEY_FCM_REGISTERED_VERSION_CODE = "com.enliple.pudding.PREF_KEY_FCM_REGISTERED_VERSION"

        // User Data
        const val PREF_KEY_USER_ID = "com.enliple.pudding.PREF_KEY_USER_ID"
        const val PREF_KEY_USER_PW = "com.enliple.pudding.PREF_KEY_USER_PW"
        const val PREF_KEY_AUTH_PUBLIC_JWT_TOKEN = "com.enliple.pudding.PREF_KEY_AUTH_PUBLIC_JWT_TOKEN"
        const val PREF_KEY_USER_TYPE = "com.enliple.pudding.PREF_KEY_USER_TYPE"
        const val PREF_KEY_LOGIN_TYPE = "com.enliple.pudding.PREF_LOGIN_TYPE"
        const val PREF_KEY_SELECTED_CATEGORY = "com.enliple.pudding.PREF_KEY_SELECTED_CATEGORY"
        const val PREF_KEY_SELECTED_CATEGORY_CODE = "com.enliple.pudding.PREF_KEY_SELECTED_CATEGORY_CODE"
        const val PREF_KEY_SELECTED_AGE = "com.enliple.pudding.PREF_KEY_SELECTED_AGE"
        const val PREF_KEY_SELECTED_GENDER = "com.enliple.pudding.PREF_KEY_SELECTED_GENDER"
        const val PREF_KEY_LOGIN_STATUS = "com.enliple.pudding.PREF_KEY_LOGIN_STATUS"
        const val PREF_KEY_USE_MOBILE_ALERT = "com.enliple.pudding.PREF_KEY_USE_MOBILE_ALERT"
        const val PREF_KEY_CART_CNT = "com.enliple.pudding.PREF_KEY_CART_CNT"
        const val PREF_KEY_VIDEO_MUTE = "com.enliple.pudding.PREF_KEY_VIDEO_MUTE"
        const val PREF_KEY_VIDEO_VOLUME = "com.enliple.pudding.PREF_KEY_VIDEO_VOLUME"
        const val PREF_CUSTOMER_CENTER_PHONE = "com.enliple.pudding.PREF_CUSTOMER_CENTER_PHONE"

        ////////////////////////////////////////////////////////////////////////////////////////////
        // BroadcastReceiver
        ////////////////////////////////////////////////////////////////////////////////////////////

        // UI Command
        const val BROADCAST_ACTION_REFRESH = "com.enliple.pudding.BROADCAST_ACTION_REFRESH"
        const val BROADCAST_ACTION_TAB_CHANGE = "com.enliple.pudding.BROADCAST_ACTION_TAB_CHANGE"
        const val BROADCAST_EXTRA_KEY_REFRESH_TYPE = "refresh_type"
        const val BROADCAST_EXTRA_KEY_TAB_POSITION = "tab_position"
        const val BROADCAST_EXTRA_VALUE_REFRESH_CATEGORY = 0x1001

        const val BROADCAST_ACTION_TERMINATE = "com.enliple.pudding.BROADCAST_ACTION_TERMINATE"
        const val BROADCAST_EXTRA_KEY_TERMINATE_TYPE = "terminate_type"
        const val BROADCAST_EXTRA_KEY_WITH_MAIN_FINISHING = "with_main_finishing"
        const val BROADCAST_EXTRA_VALUE_TERMINATE_ALL = 0x1000
        const val BROADCAST_EXTRA_VALUE_TERMINATE_ACTIVITY_ONLY = 0x1001
        const val BROADCAST_EXTRA_VALUE_TERMINATE_DIALOG_ONLY = 0x1002

        // Main Video List Control
        const val BROADCAST_ACTION_MAIN_LIST_VIDEO_PAUSE = "com.enliple.pudding.BROADCAST_ACTION_MAIN_PAUSED"
        const val BROADCAST_ACTION_MAIN_LIST_VIDEO_RESUME = "com.enliple.pudding.BROADCAST_ACTION_MAIN_LIST_VIDEO_RESUME"
        const val BROADCAST_ACTION_MAIN_LIST_VIDEO_RELEASE = "com.enliple.pudding.BROADCAST_ACTION_MAIN_LIST_VIDEO_RELEASE"
        const val BROADCAST_ACTION_MAIN_LIST_POSITION_CHANGED = "com.enliple.pudding.BROADCAST_ACTION_MAIN_LIST_POSITION_CHANGED"
        const val BROADCAST_EXTRA_KEY_LIST_POSITION = "position"


        const val TEST_BROADCAST = "TEST_BROADCAST_RECEIVER"

        // 메인 데이터 정보
        const val PREF_KEY_MAIN_DATA = "com.enliple.pudding.PREF_KEY_MAIN_DATA"
        const val PREF_KEY_BROADCAST_INFO = "com.enliple.pudding.PREF_KEY_BROADCAST_INFO"

        // 앱 기본 정보
        const val PREF_KEY_HIT_TIME = "com.enliple.pudding.PREF_KEY_HIT_TIME"                 // 조회수 등록 시간
        const val PREF_KEY_UI_HIDE_TIME = "com.enliple.pudding.PREF_KEY_UI_HIDE_TIME"         // 비디오 플레이어 UI 숨김 시간
        const val PREF_KEY_APP_VERSION = "com.enliple.pudding.PREF_KEY_APP_VERSION"         // 앱 버전
        const val PREF_KEY_FORCE_UPDATE = "com.enliple.pudding.PREF_KEY_FORCE_UPDATE"         // 강제 업데이트 여부
        const val PREF_KEY_MARKET_URL = "com.enliple.pudding.PREF_KEY_MARKET_URL"         // 마켓 URL
        const val PREF_KEY_APP_REG_DATE = "com.enliple.pudding.PREF_KEY_APP_REG_DATE"         // App 등록일
        const val PREF_KEY_VIDEO_PIP_POSITION = "com.enliple.pudding.PREF_KEY_VIDEO_PIP_POSITION"         // pip position
        const val PREF_KEY_VIDEO_PIP_KEY = "com.enliple.pudding.PREF_KEY_VIDEO_PIP_KEY"         // pip key
        const val PREF_KEY_VIDEO_PIP_PERMISSION = "com.enliple.pudding.PREF_KEY_VIDEO_PIP_PERMISSION"         // pip permission
        const val PREF_KEY_NOTIFICATION_NOTSHOW = "com.enliple.pudding.PREF_KEY_NOTIFICATION_NOTSHOW"         // notification not show 리스트
        const val PREF_KEY_NOTIFICATION = "com.enliple.pudding.PREF_KEY_NOTIFICATION"               // notification 리스트


        // FCM 푸쉬 토큰
        const val PREF_KEY_FCM_PUSH_TOKEN = "com.enliple.pudding.PREF_KEY_FCM_PUSH_TOKEN"

        // device width / height
        const val PREF_KEY_SCREEN_WIDTH = "com.enliple.pudding.PREF_KEY_SCREEN_WIDTH"
        const val PREF_KEY_SCREEN_HEIGHT = "com.enliple.pudding.PREF_KEY_SCREEN_HEIGHT"

        // App Link
        const val APP_LINK_SCHEME = "puddingPush"
        const val APP_LINK_HOST_EVENT = "Event"
        const val APP_LINK_PARAM_EV_ID = "ev_id"
        const val APP_LINK_PARAM_EV_TYPE = "ev_type"

        // 플레이어 진입 시 분기 처리 Flag
        const val EVENT_DETAIL_VOD_PLAYER   = 0
        const val SEARCH_VOD_PLAYER         = 1
        const val HASH_TAG_VOD_PLAYER       = 2
        const val HASH_TAG_MORE_VOD_PLAYER  = 3
        const val FOLLOW_VOD_PLAYER         = 4
        const val MY_UPLOAD_VOD_PLAYER      = 5
        const val SCRAP_VOD_PLAYER          = 6
        const val LATEST_VIEW_VOD_PLAYER    = 7
        const val CASTER_VOD_PLAYER         = 8
        const val RELATED_TAG_VOD_PLAYER    = 9
        const val SHARE_VOD_PLAYER          = 10
        const val SCHEDULE_PLAYER           = 11

        const val NOTIPOPUP_NOT_SHOW_DAY = (1 * 24 * 60 * 60 * 1000).toLong()      //공지 팝업창, 일주일동안 안보기 관련.
    }
}