package com.enliple.pudding.shoppingcaster.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.webkit.*
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API73
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_shop_outlink.*
import okhttp3.MultipartBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-10-01.
 */
class ShopOutlinkActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ShopOutlinkActivity"

        const val INTENT_EXTRA_KEY_URL = "url"
        const val INTENT_EXTRA_KEY_SHOP_NAME = "shop_name"
        const val INTENT_EXTRA_KEY_SC_CODE = "sc_code"
    }

    private var siteUrl: String = ""
    private var loadUrl: String = ""
    private var scCode: String = ""
    private var shopName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_outlink)
        EventBus.getDefault().register(this)

        buttonClose.setOnClickListener(clickListener)
        buttonLinkCopy.setOnClickListener(clickListener)
        buttonBack.setOnClickListener(clickListener)
        buttonFowrad.setOnClickListener(clickListener)

        initWebView()
        checkIntent()
    }

    override fun onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

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
                Logger.d(TAG, "Page Loading....")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Logger.d(TAG, "Page loaded : ${view?.title}")
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url
                loadUrl = url.toString()
                Logger.d(TAG, "Page Url : ${url}")

                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API73 = NetworkHandler.getInstance(this@ShopOutlinkActivity)
                .getKey(NetworkApi.API73.toString(), "", "")

        when (data.arg1) {
            API73 -> handlePostLink(data)
        }
    }

    private fun checkIntent() {
        siteUrl = intent?.getStringExtra(INTENT_EXTRA_KEY_URL) ?: ""
        scCode = intent?.getStringExtra(INTENT_EXTRA_KEY_SC_CODE) ?: ""
        shopName = intent?.getStringExtra(INTENT_EXTRA_KEY_SHOP_NAME) ?: ""

        try {
            webView.loadUrl(siteUrl)
        } catch (e: UnsupportedOperationException) {
            e.printStackTrace()
        }
    }

    private fun handlePostLink(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(this@ShopOutlinkActivity)
                    .get(data.arg1)

            val response: API73 = Gson().fromJson(str, API73::class.java)

            if (response.result == "success") {
                val intent = Intent()
                intent.putExtra("image", response.imageurl)
                intent.putExtra("url", loadUrl)
                intent.putExtra("storeName", shopName)
                intent.putExtra("linkid", response.linkid.toString())
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                AppToast(this@ShopOutlinkActivity).showToastMessage("상품 상세 페이지가 아닙니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(this@ShopOutlinkActivity).showToastMessage("($errorResult)",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun linkCopy(url: String) {
        var body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("strUrl", url)
                .addFormDataPart("strStore", shopName)
                .build()

        val bus = NetworkBus(NetworkApi.API73.name, body)
        EventBus.getDefault().post(bus)
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()
            R.id.buttonLinkCopy -> linkCopy(loadUrl)
            R.id.buttonBack -> {
                if (webView.canGoBack()) webView.goBack() else {
                }
            }
            R.id.buttonFowrad -> {
                if (webView.canGoForward()) webView.goForward() else {
                }
            }
        }
    }
}