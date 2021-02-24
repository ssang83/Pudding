package com.enliple.pudding

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.enliple.pudding.commons.app.AppForegroundDetector
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.NetworkUtils
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.widget.toast.AppToast

/**
 * Created by hkcha on 2018-01-08.
 */
abstract class AbsBaseActivity : AppCompatActivity(), AppForegroundDetector.Listener {
    companion object {
        // Android M (6.0) 이상부터 적용된 Doze MODE 에 대한 Broadcast Action
        private val DOZE_MODE_ACTION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED
        } else {
            "android.os.action.DEVICE_IDLE_MODE_CHANGED"
        }

        var mActivityList:MutableList<Activity> = mutableListOf()       // Activity 종료 시킬 리스트
    }

    private var pwrManager: PowerManager? = null
    private var currentNetworkStatus: NetworkStatusUtils.NetworkStatus? = null
    private var isDozeMode = false

    /**
     * 네트워크 상태가 변경되었을 때 상속된 Activity 에서 이벤트 발생
     */
    abstract fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus)

    /**
     * Doze Mode 상태가 변경되었을 때 상속된 Activity 에서 이벤트 발생
     */
    abstract fun onDozeModeStateChanged(dozeEnable: Boolean)

    override fun onResumed(activityClassName: String?) {}
    override fun onStarted(activityClassName: String?) {}
    override fun onStopped(activityClassName: String?) {}
    override fun onCreated(activityClassName: String?) {}
    override fun onPaused(activityClassName: String?) {}
    override fun onDestroyed(activityName: String?) {}

    override fun onStart() {
        Logger.d("onStart()")
        super.onStart()
    }

    override fun onStop() {
        Logger.d("onStop()")
        super.onStop()
    }

    override fun onResume() {
        Logger.d("onResume()")
        super.onResume()
        PuddingApplication.getApplication()?.setCurrentActivity(this)
    }

    override fun onPause() {
        Logger.d("onPause()")
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("onCreate()")

        pwrManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        PuddingApplication.addActivityLifecycleListener(this)
        registerBR()
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        checkNetworkStatus()
    }

    override fun onDestroy() {
        Logger.d("onDestroy()")
        PuddingApplication.removeActivityLifecycleListener(this)
        unRegisterBR()
        AppToast.cancelAllToast()
        super.onDestroy()
    }

    /**
     * StatusBar Color 를 변경시 상속받은 onCreate 문에서 호출할 것
     */
    open fun setStatusBarColor(color: Int) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.statusBarColor = color
    }

    /**
     * 현재 Activity 가 표시되고 있는 상태인지 확인
     */
    open fun isVisible(): Boolean {
        var result = false

        var application = PuddingApplication.getApplication()
        if (application != null && application.getCurrentActivity() != null) {
            result = application.getCurrentActivity()?.equals(this)!!
        }

        return result
    }

    /**
     * Activity간 전환 시 여러 Activity 한번에 강제종료 시킨다.
     */
    fun activityFinish() {
        for(i in mActivityList.indices) {
            mActivityList[i].finish()
        }
    }

    /**
     * Google Analytics 에서 참조 가능한 화면 이름을 부여
     */
    private fun getGoogleAnalyticsScreenName(): String {
        return getActivityName()
    }

    /**
     * 네트워크 연결 상태를 체크
     */
    private fun checkNetworkStatus() {
        var connStatus: Int = NetworkUtils.getConnectivityStatus(this)
        var netStatus: NetworkStatusUtils.NetworkStatus = NetworkStatusUtils.getNetworkStatus(connStatus)
        if (currentNetworkStatus == null || currentNetworkStatus != netStatus) {
            currentNetworkStatus = netStatus
            onNetworkStatusChanged(currentNetworkStatus!!)
        }
    }

    protected fun getActivityName(): String {
        return javaClass.simpleName
    }

    private fun registerBR() {
        var filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(AppConstants.BROADCAST_ACTION_TERMINATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            filter.addAction(DOZE_MODE_ACTION)
        }

        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter)
        registerReceiver(broadcastReceiver, filter)
    }

    private fun unRegisterBR() {
        unregisterReceiver(broadcastReceiver)
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun processDozeState() {
        if (pwrManager != null) {
            isDozeMode = if ((pwrManager?.isInteractive!! || pwrManager?.isDeviceIdleMode!!) != isDozeMode) {
                onDozeModeStateChanged(true)
                true
            } else {
                onDozeModeStateChanged(false)
                false
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null) return

            val action = intent.action
            if (action != null) {
                if (action == ConnectivityManager.CONNECTIVITY_ACTION || action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                    checkNetworkStatus()
                } else if (action == DOZE_MODE_ACTION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {              // Doze mode changed
                    processDozeState()
                } else if (action == AppConstants.BROADCAST_ACTION_TERMINATE) {
                    var terminateType = intent.getIntExtra(AppConstants.BROADCAST_EXTRA_KEY_TERMINATE_TYPE, AppConstants.BROADCAST_EXTRA_VALUE_TERMINATE_ALL);
                    if (terminateType == AppConstants.BROADCAST_EXTRA_VALUE_TERMINATE_ALL
                            || terminateType == AppConstants.BROADCAST_EXTRA_VALUE_TERMINATE_ACTIVITY_ONLY) {

                        var withMainFinishing = intent.getBooleanExtra(AppConstants.BROADCAST_EXTRA_KEY_WITH_MAIN_FINISHING, false)
                        if (withMainFinishing || "MainActivity" != getActivityName()) {
                            Logger.d(getActivityName(), "Received Terminate Signal to finishing : " + javaClass.simpleName)
                            finish()
                            overridePendingTransition(0, 0)
                        }
                    }
                }
            } // end if (action != null)
        }
    }
}