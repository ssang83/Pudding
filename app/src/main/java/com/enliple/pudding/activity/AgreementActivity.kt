package com.enliple.pudding.activity

import android.app.AlertDialog
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkConst
import com.enliple.pudding.commons.web.NormalWebViewClient
import kotlinx.android.synthetic.main.activity_agreement.*

/**
 * 이용약관 / 개인정보 수집 및 이용안내 / 개인정보 수집 및 위탁안내 등
 * 정책표시전용 Activity
 * @author hkcha
 * @since 2018.08.08
 */
class AgreementActivity : AbsBaseActivity() {
    companion object {
        private const val TAG = "AgreementActivity"

        const val INTENT_EXTRA_KEY_MODE = "mode"
        const val INTENT_EXTRA_VALUE_TERM = 1                 // 이용약관
        const val INTENT_EXTRA_VALUE_PRIVACY_USE = 2                 // 개인정보 수집 및 이용정책
        const val INTENT_EXTRA_VALUE_PRIVACY_CONSIGNMENT = 3                 // 개인정보 취급 및 위탁동의
    }

    private var webViewClient: NormalWebViewClient? = null
    private var mode = INTENT_EXTRA_VALUE_TERM                   // init defaults
    private var callingUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agreement)

        initWebViewSettings()

        mode = intent.getIntExtra(INTENT_EXTRA_KEY_MODE, INTENT_EXTRA_VALUE_TERM)
        when (mode) {
            INTENT_EXTRA_VALUE_TERM -> {
                Logger.d(TAG, "Calling MODE : TERM OF USE")
                callingUrl = NetworkConst.TERM_OF_USE_URL
                textViewTitle.text = getString(R.string.msg_term_of_use)
            }

            INTENT_EXTRA_VALUE_PRIVACY_USE -> {
                Logger.d(TAG, "Calling MODE : PRIVACY POLICY USE")
                callingUrl = NetworkConst.PRIVACY_POLICY_URL
                textViewTitle.text = getString(R.string.msg_privacy_use)
            }

            INTENT_EXTRA_VALUE_PRIVACY_CONSIGNMENT -> {
                Logger.d(TAG, "Calling MODE : PRIVACY POLICY CONSIGNMENT")
                callingUrl = NetworkConst.PRIVACY_POLICY_URL
                textViewTitle.text = getString(R.string.msg_privacy_consignment)
            }

            else -> finish()
        }

        buttonClose.setOnClickListener {
            finish()
        }

        webViewContent.loadUrl(callingUrl)
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
                        override fun onPageStarted(webView: WebView) {}

                        override fun onPageFinished(webView: WebView) {}
                    })
        }

        webViewContent.webViewClient = webViewClient
        webViewContent.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: android.webkit.JsResult): Boolean {
                AlertDialog.Builder(this@AgreementActivity)
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


    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
}