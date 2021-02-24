@file:JvmName("PuddingApplication")

package com.enliple.pudding

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Process
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.crashlytics.android.Crashlytics
import com.enliple.pudding.adapter.login.KakaoSDKAdapter
import com.enliple.pudding.commons.BuildConfig
import com.enliple.pudding.commons.app.AppForegroundDetector
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API21
import com.enliple.pudding.model.LoginStatusData
import com.facebook.stetho.Stetho
import com.igaworks.v2.core.application.AbxActivityHelper
import com.igaworks.v2.core.application.AbxActivityLifecycleCallbacks
import com.kakao.auth.KakaoSDK
import io.fabric.sdk.android.Fabric
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


/**
 * Created by hkcha on 2018-01-08.
 * Application by kotlin
 */
class PuddingApplication : Application() {
    companion object {
        private var instance: PuddingApplication? = null
        private var foregroundDetector: AppForegroundDetector? = null
        private var currentActivity: Activity? = null
        var mLoginUserData: API21? = null               // 로그인 유저 정보를 글로벌하게 저장한다.
        var mLoginData:LoginStatusData? = null          // 로그아웃 시 Preference가 삭제 되기 때문에 로그인 status 정보를 글로벌하게 따로 저장해둔다.
        var observer: AppLifecycleListener? = null
        // Retrofit base HTTP Request Headers
        //private val retrofitCommonHeaders = HashMap<String, String?>()

        @JvmStatic
        fun getApplication(): PuddingApplication? {
            return instance
        }

        @Synchronized
        fun getCurrentActivity(): Activity? = currentActivity

        @JvmStatic
        fun addActivityLifecycleListener(listener: AppForegroundDetector.Listener) = foregroundDetector?.addListener(listener)

        @JvmStatic
        fun removeActivityLifecycleListener(listener: AppForegroundDetector.Listener) = foregroundDetector?.removeListener(listener)
    }

    private var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()

        Logger.setLevel(Logger.ALL)
        Logger.setTag("Logger")

        EventBus.getDefault().register(this)
        instance = this
//        initCommonHeaders()

//        AppEventsLogger.activateApp(this)              // Facebook 초기화
        KakaoSDK.init(KakaoSDKAdapter())                   // Kakao SDK 초기화

        if (BuildConfig.DEBUG) {
            Logger.i("Initialize Stetho (With Debugging)")

            Stetho.initializeWithDefaults(this)
        } else {
            // release 에서 crashlytics 를 on
            Fabric.with(this, Crashlytics())
        }


        foregroundDetector = AppForegroundDetector()
        registerActivityLifecycleCallbacks(foregroundDetector)

        // adbrix
        AbxActivityHelper.initializeSdk(applicationContext , getString(R.string.adbrix_remastered_app_key), getString(R.string.adbrix_remastered_secret_key))
        if (Build.VERSION.SDK_INT >= 14)
            registerActivityLifecycleCallbacks(AbxActivityLifecycleCallbacks())
        observer = AppLifecycleListener()
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer!!)
    }

    override fun onTerminate() {
        Logger.e("onTerminate")

        EventBus.getDefault().unregister(this)
        currentActivity = null
        unregisterActivityLifecycleCallbacks(foregroundDetector)
        if ( observer != null )
            ProcessLifecycleOwner.get().lifecycle.removeObserver(observer!!)
        foregroundDetector = null
        //retrofitCommonHeaders.clear()
        instance = null

        super.onTerminate()
    }

//    /**
//     * 공통 Header 데이터의 내용을 Update
//     */
//    fun updateCommonHeader(key: String, value: String?) {
//        if (!TextUtils.isEmpty(key) && TextUtils.isEmpty(value)) {
//            retrofitCommonHeaders.remove(key)
//        } else {
//            retrofitCommonHeaders[key] = value
//        }
//    }

    @Synchronized
    fun getCurrentActivity(): Activity? = currentActivity

    @Synchronized
    fun setCurrentActivity(activity: Activity) {
        currentActivity = activity
    }

    fun finishWithProcessKill() {
        EventBus.getDefault().unregister(this)

        var currentActivity = getCurrentActivity()
        if (currentActivity != null) {
            currentActivity.finish()
            Process.killProcess(Process.myPid())
        }
    }

    @Subscribe
    fun onMessageEvent(data: NetworkBus) {
        Logger.e("omMessageEvent: ${data.arg1}")

        NetworkHandler.getInstance(applicationContext).run(data)
    }


    class AppLifecycleListener : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onMoveToForeground() {
            Logger.e("move to foreground")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onMoveToBackground() {
            Logger.e("move to background")
            EventBus.getDefault().post("FINISH_PIP_MODE")
        }
    }
}