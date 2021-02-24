package com.enliple.pudding.adapter.login

import android.app.Activity
import com.enliple.pudding.activity.LoginActivity
import com.enliple.pudding.commons.log.Logger
import com.kakao.auth.ISessionCallback
import com.kakao.util.exception.KakaoException

/**
 * Created by Kim Joonsung on 2018-10-29.
 */
class KakaoSessionCallBack : ISessionCallback {

    var mActivity: Activity? = null
    var mLoginActivity: LoginActivity? = null

    constructor(activity: Activity, loginActivity: LoginActivity) {
        mActivity = activity
        mLoginActivity = loginActivity
    }

    override fun onSessionOpenFailed(exception: KakaoException?) {
        if (exception != null) {
            Logger.e("Exception : ${exception.message}")
        }
    }

    override fun onSessionOpened() {
        Logger.e("Kakao Session Open!")
        mLoginActivity?.KakaoRequestMe()
    }
}