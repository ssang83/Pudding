package com.enliple.pudding.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.PuddingApplication
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkConst
import com.enliple.pudding.commons.network.vo.API21
import com.enliple.pudding.commons.web.NormalWebViewClient
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_identification.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.*

/**
 * PG사 (KG INICIS, OK-NAME : 구 KCB) 결제, 본인인증 처리 Activity
 * @author hkcha
 * @since 2018.08.22
 */
class PGAuthActivity : AbsBaseActivity() {

    companion object {
        private const val TAG = "PGAuthActivity"

        const val INTENT_EXTRA_KEY_CALL_MODE = "mode"
        const val INTENT_EXTRA_VALUE_MODE_UNKNOWN = 0             // 알수없는 호출모드 (잘못된 요청)
        const val INTENT_EXTRA_VALUE_MODE_IDENTIFICATION = 1             // KCB 본인인증 모드
        const val INTENT_EXTRA_VALUE_MODE_PAYMENT = 2             // INICIS 결재 모드

        private const val INICIS_BILLING_REQUEST_URL = ""

        private const val DIALOG_TYPE_ISP = 2
        private const val DIALOG_TYPE_CARDAPP = 3

        private var DIALOG_CARDNM = ""
    }

    private var callingMode: Int = INTENT_EXTRA_VALUE_MODE_UNKNOWN
    private var alertIsp: AlertDialog? = null
    private var webViewClient: WebViewClient? = null

    private val jsInterfaceKcb = KCBCellPhoneAuthJsInterface()
    private val jsInterfaceInicis = INICISPaymentJsInterface()
    private var body: String = ""
    private var mAuthData:Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        body = intent.getStringExtra("PRODUCT_BODY")
        EventBus.getDefault().register(this)
        callingMode = intent.getIntExtra(INTENT_EXTRA_KEY_CALL_MODE, INTENT_EXTRA_VALUE_MODE_UNKNOWN)
        Logger.e("callingMode :: $callingMode")
        if (callingMode == INTENT_EXTRA_VALUE_MODE_UNKNOWN) {
            finish()
            return
        } else {
            setContentView(R.layout.activity_identification)
            initWebViewSettings()

            when (callingMode) {
                INTENT_EXTRA_VALUE_MODE_IDENTIFICATION -> webViewContent.loadUrl(NetworkConst.KCB_IDENTIFICATION_REQUEST_URL)
                INTENT_EXTRA_VALUE_MODE_PAYMENT -> webViewContent.loadUrl(INICIS_BILLING_REQUEST_URL)
            }
        }
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onBackPressed() {
        Logger.e("onBackPressed")
        EventBus.getDefault().unregister(this)
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        Logger.e("onDestroy")
        EventBus.getDefault().unregister(this)
        DIALOG_CARDNM = ""
        super.onDestroy()
    }

    override fun onCreateDialog(id: Int, bundle: Bundle): Dialog? {
        when (id) {
            DIALOG_TYPE_ISP -> {
                alertIsp = AlertDialog.Builder(this@PGAuthActivity)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("알림")
                        .setMessage("모바일 ISP 어플리케이션이 설치되어 있지 않습니다. \n설치를 눌러 진행 해 주십시요.\n취소를 누르면 결제가 취소 됩니다.")
                        .setPositiveButton("설치") { _, _ ->
                            val ispUrl = "http://mobile.vpay.co.kr/jsp/MISP/andown.jsp"
                            webViewContent.loadUrl(ispUrl)
                            Logger.e("alertIsp positive")
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                        .setNegativeButton("취소") { _, _ ->
                            Toast.makeText(this@PGAuthActivity, "(-1)결제를 취소 하셨습니다.", Toast.LENGTH_SHORT).show()
                            Logger.e("alertIsp negative")
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                        .create()

                return alertIsp
            }

            DIALOG_TYPE_CARDAPP -> return getCardInstallAlertDialog(DIALOG_CARDNM)
        }

        return super.onCreateDialog(id)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if("GET/user/${AppPreferences.getUserId(this)!!}" == data.arg1) {
            if("ok" == data.arg2) {
                val response: API21 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API21::class.java)
                PuddingApplication.mLoginUserData = response

                setResult(Activity.RESULT_OK, mAuthData)
                finish()
            }
        } else if("POST/cellphone/${AppPreferences.getUserId(this)!!}" == data.arg1) {
            if("ok" == data.arg2) {
                EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(this)))
            }
        }
    }

    /**
     * WebView 설정을 초기화
     */
    private fun initWebViewSettings() {
        // 3rd Party Cookie 허용
        var cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webViewContent, true)

        val settings = webViewContent.settings
        settings.javaScriptEnabled = true
        settings.displayZoomControls = false
        settings.setSupportZoom(false)
        settings.defaultTextEncodingName = "UTF-8"
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.setSupportMultipleWindows(true)

//        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        try {
            settings.savePassword = false
        } catch (ignore: Exception) {
        }

        if (webViewClient == null) {
            when (callingMode) {
                INTENT_EXTRA_VALUE_MODE_PAYMENT -> {
                    webViewClient = INICISWebViewClient()
                }

                INTENT_EXTRA_VALUE_MODE_IDENTIFICATION -> {
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
                }
            }
        }

        webViewContent.webViewClient = webViewClient
        webViewContent.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: android.webkit.JsResult): Boolean {
                AlertDialog.Builder(this@PGAuthActivity)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                        .setCancelable(false)
                        .create()
                        .show()

                return true
            }
        }

        webViewContent.isHorizontalScrollBarEnabled = false
        webViewContent.isVerticalScrollBarEnabled = true
        webViewContent.isDrawingCacheEnabled = true
        Logger.e("call")
        when (callingMode) {
            INTENT_EXTRA_VALUE_MODE_IDENTIFICATION -> {
                Logger.e("INTENT_EXTRA_VALUE_MODE_IDENTIFICATION")
                webViewContent.addJavascriptInterface(jsInterfaceKcb, "mobile")
            }

            INTENT_EXTRA_VALUE_MODE_PAYMENT -> {
                Logger.e("INTENT_EXTRA_VALUE_MODE_PAYMENT")
                webViewContent.addJavascriptInterface(jsInterfaceInicis, "mobile")
            }
        }
    }

    /**
     * (결재 모드 전용)
     * 카드사 앱카드 설치 안내 팝업을 표시하기 위한 AlertDialog 를 생성
     * @param coCardNm      카드사명
     */
    private fun getCardInstallAlertDialog(coCardNm: String): AlertDialog {
        Logger.e("getCardInstallAlertDialog")
        val cardNm = Hashtable<String, String>()
        cardNm["HYUNDAE"] = "현대 앱카드"
        cardNm["SAMSUNG"] = "삼성 앱카드"
        cardNm["LOTTE"] = "롯데 앱카드"
        cardNm["SHINHAN"] = "신한 앱카드"
        cardNm["KB"] = "국민 앱카드"
        cardNm["HANASK"] = "하나SK 통합안심클릭"
        //cardNm.put("SHINHAN_SMART",  "Smart 신한앱");

        val cardInstallUrl = Hashtable<String, String>()
        cardInstallUrl["HYUNDAE"] = "market://details?id=com.hyundaicard.appcard"
        cardInstallUrl["SAMSUNG"] = "market://details?id=kr.co.samsungcard.mpocket"
        cardInstallUrl["LOTTE"] = "market://details?id=com.lotte.lottesmartpay"
        cardInstallUrl["LOTTEAPPCARD"] = "market://details?id=com.lcacApp"
        cardInstallUrl["SHINHAN"] = "market://details?id=com.shcard.smartpay"
        cardInstallUrl["KB"] = "market://details?id=com.kbcard.cxh.appcard"
        cardInstallUrl["HANASK"] = "market://details?id=com.ilk.visa3d"
        //cardInstallUrl.put("SHINHAN_SMART",  "market://details?id=com.shcard.smartpay");//여기 수정 필요!!2014.04.01

        return AlertDialog.Builder(this@PGAuthActivity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("알림")
                .setMessage(cardNm[coCardNm] + " 어플리케이션이 설치되어 있지 않습니다. \n설치를 눌러 진행 해 주십시요.\n취소를 누르면 결제가 취소 됩니다.")
                .setPositiveButton("설치") { _, _ ->
                    val installUrl = cardInstallUrl[coCardNm]
                    val uri = Uri.parse(installUrl)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    Log.d("<INIPAYMOBILE>", "Call : " + uri.toString())
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(this@PGAuthActivity, cardNm[coCardNm] + "설치 URL 이 올바르지 않습니다", Toast.LENGTH_SHORT).show()
                    }

                    //finish();
                }
                .setNegativeButton("취소") { _, _ ->
                    Toast.makeText(this@PGAuthActivity, "(-1)결제를 취소 하셨습니다.", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                .create()

    }//end getCardInstallAlertDialog


    inner class INICISWebViewClient : WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            progressBar.visibility = View.INVISIBLE
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progressBar.visibility = View.VISIBLE
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            /*
	    	 * URL별로 분기가 필요합니다. 어플리케이션을 로딩하는것과
	    	 * WEB PAGE를 로딩하는것을 분리 하여 처리해야 합니다.
	    	 * 만일 가맹점 특정 어플 URL이 들어온다면
	    	 * 조건을 더 추가하여 처리해 주십시요.
	    	 */
            var url = request?.url?.toString()
            Logger.e("shouldOverrideUrlLoading url :: $url")
            if (url?.startsWith("http://") == false && !url?.startsWith("https://") && !url.startsWith("javascript:")) {
                var intent: Intent

                try {
                    Log.d("<INIPAYMOBILE>", "intent url : $url")
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)

                    Log.d("<INIPAYMOBILE>", "intent getDataString : " + intent.dataString!!)
                    Log.d("<INIPAYMOBILE>", "intent getPackage : " + intent.getPackage()!!)

                } catch (ex: URISyntaxException) {
                    Log.e("<INIPAYMOBILE>", "URI syntax error : " + url + ":" + ex.message)
                    return false
                }

                val uri = Uri.parse(intent.dataString)
                intent = Intent(Intent.ACTION_VIEW, uri)



                try {

                    startActivity(intent)

                    /* 가맹점의 사정에 따라 현재 화면을 종료하지 않아도 됩니다.
	    			    삼성카드 기타 안심클릭에서는 종료되면 안되기 때문에
	    			    조건을 걸어 종료하도록 하였습니다.*/
                    if (url.startsWith("ispmobile://")) {
                        Logger.e("ispmobile:// start finish")
                        finish()
                    }

                } catch (e: ActivityNotFoundException) {
                    Log.e("INIPAYMOBILE", "INIPAYMOBILE, ActivityNotFoundException INPUT >> $url")
                    Log.e("INIPAYMOBILE", "INIPAYMOBILE, uri.getScheme()" + intent.dataString!!)

                    //ISP
                    if (url.startsWith("ispmobile://")) {
                        view!!.loadData("<html><body></body></html>", "text/html", "euc-kr")
                        showDialog(DIALOG_TYPE_ISP)
                        return false
                    } else if (intent.dataString!!.startsWith("hdcardappcardansimclick://")) {
                        DIALOG_CARDNM = "HYUNDAE"
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 현대앱카드설치 ")
                        view!!.loadData("<html><body></body></html>", "text/html", "euc-kr")
                        showDialog(DIALOG_TYPE_CARDAPP)
                        return false
                    } else if (intent.dataString!!.startsWith("shinhan-sr-ansimclick://")) {
                        DIALOG_CARDNM = "SHINHAN"
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 신한카드앱설치 ")
                        view!!.loadData("<html><body></body></html>", "text/html", "euc-kr")
                        showDialog(DIALOG_TYPE_CARDAPP)
                        return false
                    } else if (intent.dataString!!.startsWith("mpocket.online.ansimclick://")) {
                        DIALOG_CARDNM = "SAMSUNG"
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 삼성카드앱설치 ")
                        view!!.loadData("<html><body></body></html>", "text/html", "euc-kr")
                        showDialog(DIALOG_TYPE_CARDAPP)
                        return false
                    } else if (intent.dataString!!.startsWith("lottesmartpay://")) {
                        DIALOG_CARDNM = "LOTTE"
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 롯데모바일결제 설치 ")
                        view!!.loadData("<html><body></body></html>", "text/html", "euc-kr")
                        showDialog(DIALOG_TYPE_CARDAPP)
                        return false
                    } else if (intent.dataString!!.startsWith("lotteappcard://")) {
                        DIALOG_CARDNM = "LOTTEAPPCARD"
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 롯데앱카드 설치 ")
                        view!!.loadData("<html><body></body></html>", "text/html", "euc-kr")
                        showDialog(DIALOG_TYPE_CARDAPP)
                        return false
                    } else if (intent.dataString!!.startsWith("kb-acp://")) {
                        DIALOG_CARDNM = "KB"
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, KB카드앱설치 ")
                        view!!.loadData("<html><body></body></html>", "text/html", "euc-kr")
                        showDialog(DIALOG_TYPE_CARDAPP)
                        return false
                    } else if (intent.dataString!!.startsWith("hanaansim://")) {
                        DIALOG_CARDNM = "HANASK"
                        Log.e("INIPAYMOBILE", "INIPAYMOBILE, 하나카드앱설치 ")
                        view!!.loadData("<html><body></body></html>", "text/html", "euc-kr")
                        showDialog(DIALOG_TYPE_CARDAPP)
                        return false
                    } else if (intent.dataString!!.startsWith("droidxantivirusweb")) {
                        /** */
                        Log.d("<INIPAYMOBILE>", "ActivityNotFoundException, droidxantivirusweb 문자열로 인입될시 마켓으로 이동되는 예외 처리: ")
                        /** */

                        val hydVIntent = Intent(Intent.ACTION_VIEW)
                        hydVIntent.data = Uri.parse("market://search?q=net.nshc.droidxantivirus")
                        startActivity(hydVIntent)

                    } else if (url.startsWith("intent://")) {

                        /**
                         *
                         * > 삼성카드 안심클릭
                         * - 백신앱 : 웹백신 - 인프라웨어 테크놀러지
                         * - package name : kr.co.shiftworks.vguardweb
                         * - 특이사항 : INTENT:// 인입될시 정상적 호출
                         *
                         * > 신한카드 안심클릭
                         * - 백신앱 : TouchEn mVaccine for Web - 라온시큐어(주)
                         * - package name : com.TouchEn.mVaccine.webs
                         * - 특이사항 : INTENT:// 인입될시 정상적 호출
                         *
                         * > 농협카드 안심클릭
                         * - 백신앱 : V3 Mobile Plus 2.0
                         * - package name : com.ahnlab.v3mobileplus
                         * - 특이사항 : 백신 설치 버튼이 있으며, 백신 설치 버튼 클릭시 정상적으로 마켓으로 이동하며, 백신이 없어도 결제가 진행이 됨
                         *
                         * > 외환카드 안심클릭
                         * - 백신앱 : TouchEn mVaccine for Web - 라온시큐어(주)
                         * - package name : com.TouchEn.mVaccine.webs
                         * - 특이사항 : INTENT:// 인입될시 정상적 호출
                         *
                         * > 씨티카드 안심클릭
                         * - 백신앱 : TouchEn mVaccine for Web - 라온시큐어(주)
                         * - package name : com.TouchEn.mVaccine.webs
                         * - 특이사항 : INTENT:// 인입될시 정상적 호출
                         *
                         * > 하나SK카드 안심클릭
                         * - 백신앱 : V3 Mobile Plus 2.0
                         * - package name : com.ahnlab.v3mobileplus
                         * - 특이사항 : 백신 설치 버튼이 있으며, 백신 설치 버튼 클릭시 정상적으로 마켓으로 이동하며, 백신이 없어도 결제가 진행이 됨
                         *
                         * > 하나카드 안심클릭
                         * - 백신앱 : V3 Mobile Plus 2.0
                         * - package name : com.ahnlab.v3mobileplus
                         * - 특이사항 : 백신 설치 버튼이 있으며, 백신 설치 버튼 클릭시 정상적으로 마켓으로 이동하며, 백신이 없어도 결제가 진행이 됨
                         *
                         * > 롯데카드
                         * - 백신이 설치되어 있지 않아도, 결제페이지로 이동
                         *
                         */

                        /** */
                        Log.d("<INIPAYMOBILE>", "Custom URL (intent://) 로 인입될시 마켓으로 이동되는 예외 처리: ")
                        /** */

                        try {

                            var excepIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                            val packageNm = excepIntent.getPackage()

                            Log.d("<INIPAYMOBILE>", "excepIntent getPackage : " + packageNm!!)

                            excepIntent = Intent(Intent.ACTION_VIEW)
                            /*
								가맹점별로 원하시는 방식으로 사용하시면 됩니다.
								market URL
								market://search?q="+packageNm => packageNm을 검색어로 마켓 검색 페이지 이동
								market://search?q=pname:"+packageNm => packageNm을 패키지로 갖는 앱 검색 페이지 이동
								market://details?id="+packageNm => packageNm 에 해당하는 앱 상세 페이지로 이동
							*/
                            excepIntent.data = Uri.parse("market://search?q=$packageNm")

                            startActivity(excepIntent)

                        } catch (e1: URISyntaxException) {
                            Log.e("<INIPAYMOBILE>", "INTENT:// 인입될시 예외 처리  오류 : $e1")
                        }

                    }//INTENT:// 인입될시 예외 처리
                    /*
	    			//신한카드 SMART신한 앱
	    			else if( intent.getDataString().startsWith("smshinhanansimclick://"))
	    			{
	    				DIALOG_CARDNM = "SHINHAN_SMART";
	    				Log.e("INIPAYMOBILE", "INIPAYMOBILE, Smart신한앱설치");
	    				view.loadData("<html><body></body></html>", "text/html", "euc-kr");
	    				showDialog(DIALOG_TYPE_CARDAPP);
				        return false;
	    			}
	    			*/
                    /**
                     * > 현대카드 안심클릭 droidxantivirusweb://
                     * - 백신앱 : Droid-x 안드로이이드백신 - NSHC
                     * - package name : net.nshc.droidxantivirus
                     * - 특이사항 : 백신 설치 유무는 체크를 하고, 없을때 구글마켓으로 이동한다는 이벤트는 있지만, 구글마켓으로 이동되지는 않음
                     * - 처리로직 : intent.getDataString()로 하여 droidxantivirusweb 값이 오면 현대카드 백신앱으로 인식하여
                     * 하드코딩된 마켓 URL로 이동하도록 한다.
                     *///현대카드 백신앱
                    //하나SK카드 통합안심클릭앱
                    //KB앱카드
                    //롯데앱카드(간편결제)
                    //롯데 모바일결제
                    //삼성앱카드
                    //신한앱카드
                    //현대앱카드
                }

            } else {
                view?.loadUrl(url)
                return false
            }

            return true
        }


        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            // SSL 에서 보안인증 관련 에러가 발생해도 계속 진행처리
            //        handler.proceed();

            val sb = StringBuilder()

            when (error.primaryError) {
                SslError.SSL_EXPIRED -> sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n")

                SslError.SSL_IDMISMATCH -> sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n")

                SslError.SSL_NOTYETVALID -> sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n")

                SslError.SSL_UNTRUSTED -> sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n")

                else -> sb.append("보안 인증서에 오류가 있습니다.\n")
            }

            val builder = androidx.appcompat.app.AlertDialog.Builder(webViewContent.context)
            builder.setTitle("SSL 보안경고")
                    .setMessage(sb.toString())
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        handler.proceed()
                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        handler.cancel()
                        dialog.dismiss()
                    }
                    .create().show()
        }
    }


    /**
     * 결제 관련 Javascript Native Interface
     */
    @Suppress("unused")
    inner class INICISPaymentJsInterface {
        /**
         * 인증 결과를 호출하는 Method
         */
        @JavascriptInterface
        fun onResult() {
            Logger.e("INICISPaymentJsInterface onResult")
            Logger.d(TAG, "INICIS onResult()")
            startActivity(Intent(this@PGAuthActivity, PurchaseActivity::class.java).apply {
                putExtra("PRODUCT_BODY", body)
            })
            finish()
        }

        /**
         * 인증 취소가 발생된 Method
         */
        @JavascriptInterface
        fun onCanceled() {
            Logger.e("INICISPaymentJsInterface onCanceled !~!!!!!")
        }
    }

    /**
     * 휴대폰 본인인증 관련 Javascript Native Interface
     */
    inner class KCBCellPhoneAuthJsInterface {
        /**
         * 인증 결과를 호출하는 Method
         */
        @JavascriptInterface
//        onResult('cert_type', 'mb_name', 'phone_no', 'hash_data', 'adult', 'birth', 'sex', 'dupinfo')
        fun onResult(cert_type: String?, mb_name: String?, phone_no: String?, hash_data: String?, adult: String?, birth: String?, sex: String?, dupinfo: String?) {
            Logger.e("KCB onResult() - cert_type : $cert_type, mb_name : $mb_name, phone_no : $phone_no, hash_data : $hash_data, adult : $adult, birth : $birth, sex : $sex. dupinfo: $dupinfo")

            mAuthData = Intent().apply {
                putExtra("cert_type", cert_type)
                putExtra("mb_name", mb_name)
                putExtra("phone_no", phone_no)
                putExtra("hash_data", hash_data)
                putExtra("adult", adult)
                putExtra("birth", birth)
                putExtra("sex", sex)
                putExtra("dupinfo", dupinfo)
            }

            val obj = JSONObject().apply {
                put("userName", mb_name)
                put("userHp", phone_no)
                put("userSex", sex)
                put("userBirth", birth)
                put("certify", cert_type)
                put("userAdult", adult)
                put("dupinfo", dupinfo)
            }

            AlertDialog.Builder(this@PGAuthActivity)
                    .setMessage(R.string.msg_signup_hp_cert_success)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
                        EventBus.getDefault().post(NetworkBus(NetworkApi.API153.name, body))
                    }
                    .show()
        }

        /**
         * 인증 취소가 발생된 Method
         */
        @JavascriptInterface
        fun onCanceled() {
            Logger.e("onCanceled()")
            AlertDialog.Builder(this@PGAuthActivity)
                    .setMessage(R.string.msg_signup_hp_cert_canceled)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                    .show()
        }
    }
}