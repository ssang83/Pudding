package com.enliple.pudding.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppBroadcastSender
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.web.NormalWebViewClient
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_withdrawal.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * 회원탈퇴 Activity
 * @author hkcha
 * @since 2018.08.11
 */
class WithDrawalActivity : AbsBaseActivity() {

    private var webViewClient: NormalWebViewClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdrawal)
        EventBus.getDefault().register(this)

        initSuccessTitleSpan()
        initWebViewSettings()

        checkBoxAgree.setOnCheckedChangeListener { _, isChecked ->
            buttonWithDrawal.isEnabled = isChecked
        }

        buttonWithDrawal.setOnClickListener {
            requestWithDrawal()
        }

        buttonBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        buttonConfirm.setOnClickListener {
            AppBroadcastSender.broadcastTerminate(this@WithDrawalActivity, AppConstants.BROADCAST_EXTRA_VALUE_TERMINATE_ALL, true)
            startActivity(Intent(this@WithDrawalActivity, SplashActivity::class.java))
            overridePendingTransition(0, 0)

            setResult(Activity.RESULT_OK)
            finish()
        }

        webViewContent.loadUrl(NetworkConst.WITHDRAWAL_NOTIFICATION_URL)
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    @Subscribe()
    fun onMessageEvent(data: NetworkBusResponse) {
        var key = NetworkHandler.getInstance(this@WithDrawalActivity)
                .getKey(NetworkApi.API13.toString(), AppPreferences.getUserId(this@WithDrawalActivity)!!, "")

        if (data.arg1.startsWith(key)) {
            handleNetworkResult(data)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(this@WithDrawalActivity).get(data.arg1)
            var response: BaseAPI = Gson().fromJson(str, BaseAPI::class.java)

            if (response.status) {
                onWithDrawalSuccessful()            // UI 확인 임시

                AppPreferences.logout(this)
                AppPreferences.clearCache(this)
            } else {
                AppToast(this@WithDrawalActivity).showToastMessage(response.message,
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        } else {
            // error 처리
        }
    }

    /**
     * 회원탈퇴 완료에 대한 SpannableString 설정 초기화
     */
    private fun initSuccessTitleSpan() {
        var spanStr = SpannableString(textViewTitle2.text.toString())

        spanStr.setSpan(ForegroundColorSpan(0xFF9f56f2.toInt()), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanStr.setSpan(ForegroundColorSpan(0xFF546170.toInt()), 5, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanStr.setSpan(ForegroundColorSpan(0xFF9f56f2.toInt()), 6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanStr.setSpan(ForegroundColorSpan(0xFF546170.toInt()), 9, spanStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textViewTitle2.text = spanStr
    }

    /**
     * WebView 설정을 초기화
     */
    private fun initWebViewSettings() {
        val settings = webViewContent.settings
        settings.javaScriptEnabled = true
        settings.displayZoomControls = false
        settings.setSupportZoom(false)
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.defaultTextEncodingName = "UTF-8"
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        if (webViewClient == null) {
            webViewClient = NormalWebViewClient(webViewContent,
                    NormalWebViewClient.UIMode.MODE_LIST,
                    object : NormalWebViewClient.ClientCallback {
                        override fun onPageStarted(webView: WebView) {

                        }

                        override fun onPageFinished(webView: WebView) {}
                    })
        }

        webViewContent.webViewClient = webViewClient
        webViewContent.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: android.webkit.JsResult): Boolean {
                AlertDialog.Builder(this@WithDrawalActivity)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                        .setCancelable(false)
                        .create()
                        .show()

                return true
            }
        }

        webViewContent.isHorizontalScrollBarEnabled = false
        webViewContent.isVerticalScrollBarEnabled = false
        webViewContent.isFocusable = false
        webViewContent.isDrawingCacheEnabled = true
    }

    /**
     * 최종적으로 사용자에게 회원 탈퇴 사실이 확인된 이후 서버로 탈퇴 요청
     */
    private fun requestWithDrawal() {
        var bus = NetworkBus(NetworkApi.API13.name)
        EventBus.getDefault().post(bus)
    }

    /**
     * 회원탈퇴 완료
     */
    private fun onWithDrawalSuccessful() {
        webViewContent.visibility = View.GONE
        divider1.visibility = View.GONE
        buttonWithDrawal.visibility = View.GONE
        checkBoxAgree.visibility = View.GONE

        layoutSuccess.visibility = View.VISIBLE
    }
}