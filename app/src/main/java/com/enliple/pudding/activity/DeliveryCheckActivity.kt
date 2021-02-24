package com.enliple.pudding.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.*
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.activity_delivery_check.*

/**
 * Created by Kim Joonsung on 2018-12-04.
 */
class DeliveryCheckActivity : AbsBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_URL = "url"
    }

    private var siteUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_delivery_check)

        initWebView()

        checkIntent(intent)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    private fun initWebView() {
        webView.clearHistory()
        webView.clearCache(true)

        val webSetting = webView.settings
        webSetting.javaScriptEnabled = true
        webSetting.builtInZoomControls = false
        webSetting.javaScriptCanOpenWindowsAutomatically = false
        webSetting.setSupportMultipleWindows(true)
        webSetting.cacheMode = WebSettings.LOAD_NO_CACHE
        webSetting.domStorageEnabled = true
        webSetting.useWideViewPort = true
        webSetting.loadWithOverviewMode = true
        webSetting.allowUniversalAccessFromFileURLs = true

        webSetting.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Logger.d("Page Loading....")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Logger.d("Page loaded : ${view?.title}")
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url
                Logger.d("Page Url : ${url}")

                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }
        }
    }

    private fun checkIntent(intent: Intent) {
        siteUrl = intent.getStringExtra(INTENT_EXTRA_KEY_URL) ?: ""

        try {
            webView.loadUrl(siteUrl)
        } catch (e: UnsupportedOperationException) {
            e.printStackTrace()
        }
    }
}