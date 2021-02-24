package com.enliple.pudding.activity

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.web.NormalWebViewClient
import kotlinx.android.synthetic.main.activity_statistics.*

/**
 * Created by Kim Joonsung on 2018-10-25.
 */
class StatisticsActivity : AbsBaseActivity() {

    companion object {
        private const val TAG = "StatisticsActivity"
    }

    private var url: String? = null
    private var title: String? = null
    private var webViewClient: NormalWebViewClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("onCreate")

        setContentView(R.layout.activity_statistics)

        var it = intent
        if (it != null) {
            url = intent.getStringExtra("URL")
            if (url.isNullOrEmpty()) {
                Logger.e("url null error !!")
                finish()
                return
            }

            url = "$url${AppPreferences.getUserId(this)}"
            Logger.e("url: $url")

            title = intent.getStringExtra("TITLE")
            if (TextUtils.isEmpty(url) || TextUtils.isEmpty(title))
                finish()

            titleBarTitle.text = title
        }


        buttonClose.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                finish()
            }
        })

        initWebViewSettings()

        webViewContent.loadUrl(url)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

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
                    NormalWebViewClient.UIMode.MODE_ISOLATE,
                    object : NormalWebViewClient.ClientCallback {
                        override fun onPageStarted(webView: WebView) {}

                        override fun onPageFinished(webView: WebView) {}
                    })
        }

        webViewContent.webViewClient = webViewClient
        webViewContent.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: android.webkit.JsResult): Boolean {
                AlertDialog.Builder(this@StatisticsActivity)
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
}