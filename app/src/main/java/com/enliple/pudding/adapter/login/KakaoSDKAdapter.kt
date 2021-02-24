package com.enliple.pudding.adapter.login

import android.app.Activity
import android.content.Context
import com.enliple.pudding.PuddingApplication
import com.kakao.auth.*

/**
 * Created by Kim Joonsung on 2018-10-29.
 */
class KakaoSDKAdapter : KakaoAdapter() {

    val mActivity: Activity? = null

    /**
     * Session Config에 대해서는 default값들이 존재한다.
     * 필요한 상황에서만 override해서 사용하면 됨.
     * @return Session의 설정값.
     */
    override fun getSessionConfig(): ISessionConfig {
        return object : ISessionConfig {
            override fun isSecureMode(): Boolean {
                return false
            }

            override fun getAuthTypes(): Array<AuthType> {
                return arrayOf(AuthType.KAKAO_LOGIN_ALL)
            }

            override fun isUsingWebviewTimer(): Boolean {
                return false
            }

            override fun getApprovalType(): ApprovalType {
                return ApprovalType.INDIVIDUAL
            }

            override fun isSaveFormData(): Boolean {
                return true
            }
        }
    }

    override fun getApplicationConfig(): IApplicationConfig {
        return object : IApplicationConfig {
            val topActivity: Activity?
                get() = PuddingApplication.getCurrentActivity()

            override fun getApplicationContext(): Context {
                return PuddingApplication.getApplication() as Context
            }
        }
    }
}