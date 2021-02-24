package com.enliple.pudding.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.BuildConfig
import com.enliple.pudding.PuddingApplication
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.app.VersionCompare
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppBroadcastSender
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.internal.AppPreferences.Companion.clearCache
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API111
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.model.LoginStatusData
import com.enliple.pudding.widget.AppAlertDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_settings.*
import okhttp3.MultipartBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 설정 Activity
 * @author hkcha
 * @since 2018.08.30
 */
class SettingsActivity : AbsBaseActivity() {
    companion object {
        private const val TAG = "SettingsActivity"
        const val ACTIVITY_REQUEST_CODE_WITH_DRAWAL = 0xB001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        EventBus.getDefault().register(this)

        layoutLogout.visibility =
                if (AppPreferences.getUserLoginType(this) != AppPreferences.USER_ACCOUNT_TYPE_NOT_LOGIN) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

        buttonBack.setOnClickListener(clickListener)
        layoutDeniedList.setOnClickListener(clickListener)
        layoutReportDetails.setOnClickListener(clickListener)
        layoutFormulationDetails.setOnClickListener(clickListener)
        layoutClearCache.setOnClickListener(clickListener)
        layoutTermOfUseDetail.setOnClickListener(clickListener)
        layoutPrivacyPolicy.setOnClickListener(clickListener)
        layoutPermissionSettings.setOnClickListener(clickListener)
        layoutVersionInfo.setOnClickListener(clickListener)
        layoutWithDrawal.setOnClickListener(clickListener)
        layoutLogout.setOnClickListener(clickListener)
        layoutVideoSetting.setOnClickListener(clickListener)
        layoutAppSetting.setOnClickListener(clickListener)
        settingAlarm.setOnClickListener(clickListener)
        EventBus.getDefault().post(NetworkBus(NetworkApi.API111.name))
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if (data.arg1 == NetworkApi.API111.toString()) {
            handleNetworkVersionInfo(data)
        } else if (data.arg1.startsWith(NetworkApi.API122.toString())) {
            if ("ok" == data.arg2) {
                PuddingApplication.mLoginData = LoginStatusData(
                        AppPreferences.getUserLoginType(this),
                        AppPreferences.getUserId(this)!!,
                        AppPreferences.getUserPw(this)!!)

                AppPreferences.logout(this)
//                AppPreferences.clearCache(this)

                AppBroadcastSender.broadcastTerminate(this@SettingsActivity, AppConstants.BROADCAST_EXTRA_VALUE_TERMINATE_ALL, true)
                startActivity(Intent(this@SettingsActivity, SplashActivity::class.java))
                overridePendingTransition(0, 0)
            }
        }
    }

    private fun handleNetworkVersionInfo(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API111 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API111::class.java)

            // TODO 버전 정보 처리 어떻게 해야 하나..
            textViewVersion.text = "Ver. ${BuildConfig.VERSION_NAME}"
//            textViewVersion.text = "ver.${response.data.version}"
//
//            var currentVersion = BuildConfig.VERSION_NAME
//            when (VersionCompare.compareVersions(currentVersion, response.data.version)) {
//                1 -> textViewVersionNoticeStr.text = String.format(getString(R.string.msg_settings_latest_version_format), response.data.version)
//                0, -1 -> {
//                    textViewVersionNoticeStr.text = getString(R.string.msg_settings_currently_latest_version)
//                }
//
//                else -> textViewVersionNoticeStr.visibility = View.GONE
//            }
        } else {
            val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }

    private fun handleWithDrawal() {
        startActivityForResult(Intent(this, WithDrawalActivity::class.java),
                ACTIVITY_REQUEST_CODE_WITH_DRAWAL)
    }

    /**
     * FCM Push 토큰 삭제
     */
    private fun deletePushToken() {
        var uuid = Utils.getUniqueID(this)
        Logger.i("registerPushToken uuid:$uuid")

        NetworkBus(NetworkApi.API122.name, AppPreferences.getUserId(this)!!, uuid).let {
            EventBus.getDefault().post(it)
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.settingAlarm -> {startActivity(Intent(this, MyAlarmSettingActivity::class.java))}
            R.id.layoutDeniedList -> startActivity(Intent(it!!.context, BlockActivity::class.java))                        // 차단목록
            R.id.layoutReportDetails -> startActivity(Intent(it!!.context, ReportActivity::class.java))                        // 신고 내역
            R.id.layoutFormulationDetails -> startActivity(Intent(it!!.context, RestrictionActivity::class.java))                   // 제제 내역
            R.id.layoutPermissionSettings -> {                                                                                      // 권한설정
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + it!!.context.packageName))
                startActivity(intent)
            }

            R.id.layoutTermOfUseDetail -> startActivity(Intent(it!!.context, AgreementActivity::class.java).apply {
                putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE, AgreementActivity.INTENT_EXTRA_VALUE_TERM)
            })

            R.id.layoutPrivacyPolicy -> startActivity(Intent(it!!.context, AgreementActivity::class.java).apply {
                putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE, AgreementActivity.INTENT_EXTRA_VALUE_PRIVACY_USE)
            })

            R.id.buttonBack -> onBackPressed()
            R.id.layoutClearCache -> {
                val dialog = AppAlertDialog(this@SettingsActivity)
                dialog.setTitle("캐시삭제")
                dialog.setMessage("임시로 저장된 데이터를 삭제하여 저장용량을 확보하시겠습니까?")
                dialog.setLeftButton(getString(R.string.msg_my_follow_cancel), View.OnClickListener {
                    dialog.dismiss()
                })
                dialog.setRightButton(getString(R.string.msg_my_follow_confirm), View.OnClickListener {
                    clearCache(this@SettingsActivity)
                    dialog.dismiss()
                })

                dialog.show()
            }
            R.id.layoutVersionInfo -> {
                startActivity(Intent(it!!.context, VersionInfoActivity::class.java).apply {
                    putExtra(VersionInfoActivity.INTENT_EXTRA_KEY_LATEST_VERSION, textViewVersion.text.toString())
                })
            }

            R.id.layoutWithDrawal -> handleWithDrawal()
            R.id.layoutLogout -> {
                val dialog = AppAlertDialog(this@SettingsActivity)
                dialog.setTitle("로그아웃")
                dialog.setMessage("로그아웃을 하시겠습니까?")
                dialog.setLeftButton(getString(R.string.msg_my_follow_cancel), View.OnClickListener {
                    dialog.dismiss()
                })
                dialog.setRightButton(getString(R.string.msg_my_follow_confirm), View.OnClickListener {
                    deletePushToken()
                })

                dialog.show()
            }

            R.id.layoutVideoSetting -> startActivity(Intent(it!!.context, VideoSettingActivity::class.java))
            R.id.layoutAppSetting -> startActivity(Intent(it!!.context, DefaultSettingActivity::class.java))
        }
    }
}