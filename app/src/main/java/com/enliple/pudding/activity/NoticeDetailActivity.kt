package com.enliple.pudding.activity

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.web.NormalWebViewClient
import kotlinx.android.synthetic.main.activity_notice_detail.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception


/**
 * 공지사항 상세내용 표시 Activity
 * @author hkcha
 * @since 2018.08.30
 */
class NoticeDetailActivity : AbsBaseActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_NOTICE_ID = "notice_id"           // 공지사항 아이디
        const val INTENT_EXTRA_KEY_SUBJECT = "subject"             // 공지사항 제목 (첫번째 라인)
        const val INTENT_EXTRA_KEY_SUB_TITLE = "sub_title"           // 보조제목으로 쓸 공간(두번째 라인)
        const val INTENT_EXTRA_KEY_HAS_MORE = "more"                // 더보기 사용여부
        const val INTENT_EXTRA_KEY_MORE_URL = "more_url"            // 더보기 버튼 이동시 사용할 URL
        const val INTENT_EXTRA_KEY_CONTENT_URL = "content_url"         // 공지사항 본문 URL
        const val INTENT_EXTRA_KEY_REG_DATE = "date"         // 공지사항 날짜
    }

    private var noticeId = ""
    private var hasMore: Boolean = false
    private var moreUrl: String? = null
    private var contentUrl: String? = null
    private var subject: String? = null
    private var subTitle: String? = null
    private var regDate:String = ""

    private var webViewClient: WebViewClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_detail)

        try {
            if (intent != null) {
                subject = intent.getStringExtra(INTENT_EXTRA_KEY_SUBJECT)
                noticeId = intent.getStringExtra(INTENT_EXTRA_KEY_NOTICE_ID) ?: ""
                hasMore = intent.getBooleanExtra(INTENT_EXTRA_KEY_HAS_MORE, false)
                moreUrl = intent.getStringExtra(INTENT_EXTRA_KEY_MORE_URL)
                contentUrl = intent.getStringExtra(INTENT_EXTRA_KEY_CONTENT_URL)
                subTitle = intent.getStringExtra(INTENT_EXTRA_KEY_SUB_TITLE)
                regDate = intent.getStringExtra(INTENT_EXTRA_KEY_REG_DATE)
            }
        } catch (e: Exception) {
            Logger.p(e)
        }

        initWebViewSettings()

        buttonBack.setOnClickListener(clickListener)
        buttonMore.setOnClickListener(clickListener)

        textViewSubject.text = subject
        textViewRegDate.text = regDate

        if (!TextUtils.isEmpty(contentUrl) && contentUrl!!.startsWith("http", true)) {
            webViewContent.loadUrl(contentUrl)
        }

        if ( !TextUtils.isEmpty(contentUrl) ) {
            tvContent.setHtmlText(contentUrl, Utils.ConvertDpToPx(this@NoticeDetailActivity, 30))
        }
        if ( !TextUtils.isEmpty(noticeId) ) {
            NetworkBus(NetworkApi.API123.name, "notice", noticeId).let {
                EventBus.getDefault().post(it)
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    private fun initWebViewSettings() {
        val settings = webViewContent.settings
        settings.javaScriptEnabled = true
        settings.displayZoomControls = false
        settings.setSupportZoom(false)
        settings.defaultTextEncodingName = "UTF-8"
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.setSupportMultipleWindows(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webViewClient = NormalWebViewClient(webViewContent,
                NormalWebViewClient.UIMode.MODE_ISOLATE,
                object : NormalWebViewClient.ClientCallback {
                    override fun onPageStarted(webView: WebView) {
                        progressBar.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(webView: WebView) {
                        progressBar.visibility = View.INVISIBLE
                    }
                })

        webViewContent.webViewClient = webViewClient
        webViewContent.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: android.webkit.JsResult): Boolean {
                AlertDialog.Builder(this@NoticeDetailActivity)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                        .setCancelable(false)
                        .create()
                        .show()

                return true
            }
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonMore -> {
                if (!TextUtils.isEmpty(moreUrl)
                        && (moreUrl!!.startsWith("http://", true)
                                || (moreUrl!!.startsWith("https://", true)))) {
                    var nextIntent = Intent(Intent.ACTION_VIEW)
                    nextIntent.addCategory(Intent.CATEGORY_BROWSABLE)
                    nextIntent.data = Uri.parse(moreUrl)
                    startActivity(nextIntent)
                }
            }
            R.id.buttonBack -> {
                onBackPressed()
            }

        }
    }
}