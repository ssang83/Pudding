package com.enliple.pudding.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import com.enliple.commons.io.FileUtils
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.BuildConfig
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.db.VideoDBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API111
import com.enliple.pudding.commons.network.vo.API80
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.widget.SingleButtonDialog
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * SplashActivity
 * @author hkcha
 * @since 2018. 05. 11
 */
class SplashActivity : AbsBaseActivity() {
    companion object {
        const val FROM_SHARE = "from_share"
        const val SHARE_KEY = "share_key"

        private const val MAXIMUM_NETWORK_RETRY_COUNT = 5
    }

    @Volatile
    private var engModeClickCount = 0
    private var mNetworkRetryCount = 0
    private var isPublicJWTGen = true

//    private var alertDialog: Dialog? = null
    private var nLink: String? = null
    private var deepLinkCalled = false
    private var decorView: View? = null
    private var uiOption: Int? = -1
    private var mIntent: Intent? = null
    private fun dumpIntent(intent: Intent) {
        var bundle = intent.extras
        if (bundle != null) {
            var keys = bundle.keySet()
            var stringBuilder = StringBuilder()
            stringBuilder.append("IntentDump \n\r")
            stringBuilder.append("-------------------------------------------------------------\n\r");
            keys.forEach {
                stringBuilder.append(it).append("=").append(bundle.get(it)).append("\n\r")
            }
            stringBuilder.append("-------------------------------------------------------------\n\r");
            Logger.i(stringBuilder.toString())
        }
    }

    private fun getColorAlpha(percentage: Int, colorCode: String): String {
        val decValue = percentage.toDouble() / 100 * 255
        val rawHexColor = colorCode.replace("#", "")
        val str = StringBuilder(rawHexColor)
        if (Integer.toHexString(decValue.toInt()).length == 1) {
            str.insert(0, "#0" + Integer.toHexString(decValue.toInt()))
        } else {
            str.insert(0, "#" + Integer.toHexString(decValue.toInt()))
        }
        return str.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.e("onCreate")
        EventBus.getDefault().register(this)
        init()

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        var screenWidth = displayMetrics.widthPixels
        AppPreferences.setScreenWidth(this, screenWidth)
        AppPreferences.setScreenHeight(this, screenHeight)

        registerPushToken()

        dumpIntent(intent)

        Logger.e("color: ${getColorAlpha(70, "#ff6c6c")}")
        Logger.e("color: ${getColorAlpha(70, "#ea1919")}")

        Utils.checkResolution(windowManager, applicationContext)
    }

    private fun registerPushToken() {
        var token = AppPreferences.getPushToken(this@SplashActivity)
        Logger.i("registerPushToken getPushToken:$token")

        if (!TextUtils.isEmpty(token)) {
            var uuid = Utils.getUniqueID(this@SplashActivity)
            Logger.i("registerPushToken getUniqueID:$uuid")

            var body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("token", token)
                    .addFormDataPart("deviceId", uuid)
                    .addFormDataPart("user", AppPreferences.getUserId(this@SplashActivity)!!)
                    .addFormDataPart("deviceType", "android")
                    .build()

            EventBus.getDefault().post(NetworkBus(NetworkApi.API85.name, body))
        } else {
            Logger.e("registerPushToken token null error")
        }
    }

    private fun init() {
        var storage = "$cacheDir/temp_image/"
        var file = File(storage)
        if (file.exists()) {
            FileUtils.removeRecursive(file)
        }

        // db 모두 지운다.!!
        Logger.e("delete all video db")
        DBManager.getInstance(this).deleteAll()
        VideoDBManager.getInstance(this).deleteAll()

        when(AppPreferences.getUserLoginType(this)) {
            AppPreferences.LOGIN_TYPE_ALL,
            AppPreferences.LOGIN_TYPE_AUTO -> {
                getUserJWT()
            }

            AppPreferences.LOGIN_TYPE_SAVE_ID -> {
                AppPreferences.setLoginStatus(this@SplashActivity, false)
                getPublicJWT()
            }

            AppPreferences.LOGIN_TYPE_NORMAL -> {
                AppPreferences.setUserId(this, "")
                AppPreferences.setLoginStatus(this@SplashActivity, false)
                getPublicJWT()
            }
        }
    }

    override fun onDestroy() {
        engModeClickCount = 0
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {
        Logger.e("onNetworkStatusChanged: $status")

        when (status) {
            NetworkStatusUtils.NetworkStatus.MOBILE,
            NetworkStatusUtils.NetworkStatus.WIFI -> {
            }
            NetworkStatusUtils.NetworkStatus.UNKNOWN,
            NetworkStatusUtils.NetworkStatus.UNREACHABLE -> {
            }
        }
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
//        if (data.arg1 == NetworkApi.API111.toString()) {
//            handleNetworkVersionInfo(data)
//        }
        if ("fail" == data.arg2 && "-1" == data.arg3) {
            when (data.arg1) {
                "GET/auth/ptoken",
                "POST/auth/utoken" -> {
                    // 네트워크 연결이 안되는 경우 일까?
                    AppToast(this).showToastMessage("네트워크 상태를 확인해 주세요.",
                            AppToast.DURATION_MILLISECONDS_LONG,
                            AppToast.GRAVITY_MIDDLE)

                    Handler().postDelayed({ this.finish() }, 4000)
                    return
                }
            }
        }

        val api81 = NetworkHandler.getInstance(this@SplashActivity).getKey(NetworkApi.API81.toString(), "all", "")
        val api80 = NetworkHandler.getInstance(this@SplashActivity).getKey(NetworkApi.API80.toString(), "", "")
        val api79 = NetworkHandler.getInstance(this@SplashActivity).getKey(NetworkApi.API79.toString(), "", "")

        when (data.arg1) {
            api81 -> initCategoryData(data)
            api80 -> getUserJWTToken(data)
            api79 -> getPublicJWTToken(data)

            NetworkDBKey.getAPI49Key(if (nLink != null) nLink!! else "") -> {
                if (!TextUtils.isEmpty(nLink) && !deepLinkCalled) {
                    deepLinkCalled = true

                    mIntent = Intent(this, MainActivity::class.java)
                    mIntent!!.putExtra(FROM_SHARE, true)
                    mIntent!!.putExtra(SHARE_KEY, nLink)
//                    EventBus.getDefault().post(NetworkBus(NetworkApi.API111.name))
                    startActivity(mIntent)
                    finish()
                }
            }

            NetworkApi.API156.toString() -> getNotification(data)
        }

//        if (data.arg1.startsWith(NetworkApi.API49.toString())) {
//            val str = DBManager.getInstance(this@SplashActivity).get(data.arg1)
//            Logger.e("share data :: $str")
//            val nextIntent = Intent(this, MainActivity::class.java)
//            nextIntent.putExtra(FROM_SHARE, true)
//            nextIntent.putExtra(SHARE_KEY, link)
//            Logger.e(getActivityName(), "Start " + nextIntent.component.shortClassName)
//            startActivity(nextIntent)
//            finish()
//        }
    }

    private fun getVersionInfo(context: Context): String {
        var version = "Unknown"
        var packageInfo: PackageInfo

        if (context == null) {
            return version
        }
        try {
            packageInfo = context.applicationContext.packageManager.getPackageInfo(context.applicationContext.packageName, 0)
            version = packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return version;
    }

    private fun handleNetworkVersionInfo(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API111 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API111::class.java)

            var recentVersion = response.data.version
            var appVersion = BuildConfig.VERSION_CODE
            try {
                Logger.e("recentVersion :: $recentVersion")
                Logger.e("appVersion :: $appVersion")
                if (recentVersion.toInt() > appVersion) {
                    val dialogWidth = AppPreferences.getScreenWidth(this@SplashActivity) - Utils.ConvertDpToPx(this@SplashActivity, 40)
                    var dialog = SingleButtonDialog(this@SplashActivity, dialogWidth, "최신버전이 업데이트 되었습니다.\n아래 확인 버튼을 클릭하시기 바랍니다.", "확인",
                            SingleButtonDialog.SingleButtonDialogListener {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse("market://details?id=$packageName")
                                startActivity(intent)
                                finish()
                            })
                    dialog.setCancelable(false)
                    dialog.show()
                } else {
                    startActivity(mIntent)
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        } else {
            finish()
        }
    }

    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    //@Subscribe(threadMode = ThreadMode.MAIN)

    /**
     * 비회원 JWT 토큰을 획득
     */
    private fun getPublicJWT() {
        var timeStamp = SimpleDateFormat("yyyyMMdd").format(Date(System.currentTimeMillis()))
        var hmac = AppPreferences.generatePublicHMAC(timeStamp)
        EventBus.getDefault().post(NetworkBus(NetworkApi.API79.name, hmac, timeStamp))
    }

    /**
     * 회원 JWT 토큰을 획득
     */
    private fun getUserJWT() {
        when (AppPreferences.getUserAccountType(this)) {
            AppPreferences.USER_ACCOUNT_TYPE_ACCOUNT -> {
                var timeStamp = SimpleDateFormat("yyyyMMdd").format(Date(System.currentTimeMillis()))
                var requestObj = JSONObject()
                requestObj.put("id", AppPreferences.getUserId(this@SplashActivity) ?: "")
                requestObj.put("pw", AppPreferences.getUserPw(this@SplashActivity) ?: "")
                requestObj.put("timestamp", timeStamp)

                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString())
                var hmac = AppPreferences.generateUserHMAC(requestObj.toString())
                EventBus.getDefault().post(NetworkBus(NetworkApi.API80.name, hmac, timeStamp, body))
            }

            AppPreferences.USER_ACCOUNT_TYPE_FACEBOOK,
            AppPreferences.USER_ACCOUNT_TYPE_GOOGLE,
            AppPreferences.USER_ACCOUNT_TYPE_KAKAO -> {
                Logger.d("Auto Login Type : SOCIAL")
                // TODO : 소셜 로그인 API 가 나오면 처리
            }
        }
    }

    private fun initCategoryData(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            startNextActivity()
        } else {
            Logger.e("init category fail !!")

            ++mNetworkRetryCount
            if (mNetworkRetryCount > MAXIMUM_NETWORK_RETRY_COUNT) {
                AppToast(this).showToastMessage("서버 응답이 없습니다. 잠시 후 다시 시도해 주세요.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)

                finish()
            } else {
                // category 획득 재 시도
                EventBus.getDefault().post(NetworkBus(NetworkApi.API81.name, "all", ""))
            }
        }
    }

    private fun getUserJWTToken(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            mNetworkRetryCount = 0
            isPublicJWTGen = false

            val str = DBManager.getInstance(this@SplashActivity).get(data.arg1)
            val response: API80 = Gson().fromJson(str, API80::class.java)
            AppPreferences.setJWT(this@SplashActivity, response.JWT)
            Logger.e("getUserJWTToken:${response.JWT}")

            EventBus.getDefault().post(NetworkBus(NetworkApi.API156.name))
        } else {
            //val str = DBManager.getInstance(this@SplashActivity).get(data.arg1)
            //val response: API80 = Gson().fromJson(str, API80::class.java)
            Logger.e("getUserJWTToken:${data.arg2} ${data.arg3} ${data.arg4}")

            ++mNetworkRetryCount

            if ("401" == data.arg3/* && data.arg4.contains("탈퇴한")*/) {
                // 탈퇴한 ID 이거나 다른 기기에서 비밀번호를 변경 할 경우
                AppPreferences.setUserId(this@SplashActivity, "")
                AppPreferences.setUserPw(this@SplashActivity, "")
                AppPreferences.setLoginStatus(this@SplashActivity, false)

                getPublicJWT()
            } else if (mNetworkRetryCount > MAXIMUM_NETWORK_RETRY_COUNT) {
                getPublicJWT()
            } else {
                // 토큰 획득을 재시도
                Handler().postDelayed({ getUserJWT() }, 500)
            }
        }
    }

    private fun getPublicJWTToken(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            isPublicJWTGen = true

            val str = DBManager.getInstance(this@SplashActivity).get(data.arg1)
            val response: API80 = Gson().fromJson(str, API80::class.java)
            if (TextUtils.isEmpty(AppPreferences.getJWT(this))) {
                AppPreferences.setJWT(this, response.JWT)
                //PuddingApplication.getApplication()?.updateCommonHeader("jwt", response.JWT)
            }
            Logger.e("getPublicJWTToken:${AppPreferences.getJWT(this)}")

            EventBus.getDefault().post(NetworkBus(NetworkApi.API156.name))
        } else {
            val str = DBManager.getInstance(this@SplashActivity).get(data.arg1)
            val response: API80 = Gson().fromJson(str, API80::class.java)
            Logger.e("Public JWT Receive failed : ${response.message}")

            ++mNetworkRetryCount

            if (mNetworkRetryCount > MAXIMUM_NETWORK_RETRY_COUNT) {
//                alertDialog?.dismiss()
//                alertDialog = null
//                alertDialog = AlertDialog.Builder(this@SplashActivity)
//                        .setMessage(R.string.error_app_init_failed)
//                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
//                            dialog.dismiss()
//                            finish()
//                        }
//                        .setCancelable(false)
//                        .create().apply { show() }
                AppToast(this).showToastMessage(getString(R.string.error_app_init_failed),
                        AppToast.DURATION_MILLISECONDS_LONG,
                        AppToast.GRAVITY_MIDDLE)
                finish()

            } else {
                Handler().postDelayed({ getPublicJWT() }, 500)
            }
        }
    }

    private fun getNotification(data:NetworkBusResponse) {
        if("ok" == data.arg2) {
            val notShow = AppPreferences.getNotificationNotShowList(this)
            val notShowList: MutableList<JSONObject> = mutableListOf()
            try {
                val jsonArray = JSONArray(notShow)
                for (i in jsonArray.length() - 1 downTo 0) {
                    val json = jsonArray.optJSONObject(i)
                    if (json != null) {
                        notShowList.add(json)
                    }
                }
            } catch (e:Exception) {
                Logger.p(e)
            }

            try {
                val json = JSONObject(DBManager.getInstance(this).get(data.arg1))
                if(json.optString("result").toUpperCase() == "SUCCESS") {
                    val jsonArray = json.optJSONArray("data")
                    if(jsonArray != null && jsonArray.length() > 0) {
                        val showList = JSONArray()
                        for (i in jsonArray.length() - 1 downTo 0) {
                            val obj = jsonArray.getJSONObject(i)
                            if (obj != null && willShowNotiPopup(obj, notShowList)) {
                                showList.put(obj)
                            }
                        }

                        if(showList != null && showList.length() > 0) {
                            AppPreferences.setNotificationList(this, showList.toString())
                        } else {
                            AppPreferences.setNotificationList(this, "")
                        }
                    }
                }
            } catch (e:Exception) {
                Logger.p(e)
            }

            saveNotShowList(notShowList)
            EventBus.getDefault().post(NetworkBus(NetworkApi.API81.name, "all", ""))
        }
    }

    private fun saveNotShowList(notShowList: MutableList<JSONObject>) {
        val array = JSONArray()
        if(notShowList != null && notShowList.size > 0) {
            for (json in notShowList) {
                array.put(json)
            }
        }

        AppPreferences.setNotificationNotShowList(this, array.toString())
    }

    private fun willShowNotiPopup(noti:JSONObject, notShowList:MutableList<JSONObject>) : Boolean {
        val no = noti.optString("no", "")
        val updateDate = StringUtils.getDateTimeFromString(noti.optString("reserve_day", ""))

        if(notShowList != null && notShowList.size > 0) {
            val calendar = GregorianCalendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)

            val checkDate = calendar.timeInMillis
            var j = 0
            while (j < notShowList.size) {
                val json = notShowList[j]

                if (json != null && no.toUpperCase() == json.optString("no", "").toUpperCase()) {

                    val beforeUpdateDate = StringUtils.getDateTimeFromString(json.optString("reserve_day", ""))
                    val notShowDay = json.optLong("not_show", 0)

                    if (updateDate > beforeUpdateDate || notShowDay + AppConstants.NOTIPOPUP_NOT_SHOW_DAY < checkDate) {
                        notShowList.removeAt(j)
                        --j

                        return true
                    } else {
                        return false
                    }
                }
                ++j
            }
        }

        return true
    }

    /**
     * 상태에 따른 다음 화면 진입
     */
    private fun startNextActivity() {
        if (isFinishing) return

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this, OnSuccessListener<PendingDynamicLinkData> { pendingDynamicLinkData ->
                    Logger.e("onSuccess")
                    // Get deep link from result (may be null if no link is found)
                    try {
                        var deepLink: Uri? = null
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.link
                            if (deepLink != null) {
                                nLink = deepLink.toString()
                                Logger.e("deepLink :: $nLink")
//                                val bus = NetworkBus(NetworkApi.API49.name, Uri.encode(deepLink.toString(), "UTF-8").toString())
                                EventBus.getDefault().post(NetworkBus(NetworkApi.API49.name, deepLink.toString()))
                            } else {
                                Logger.e("deepLink is null")
                            }
                        } else {
                            Logger.e("pendingDynamicLinkData null")
                        }
                        if (deepLink == null) {
                            mIntent = Intent(this, MainActivity::class.java)
                            Logger.e(getActivityName(), "Start " + mIntent!!.component.shortClassName)
//                            EventBus.getDefault().post(NetworkBus(NetworkApi.API111.name))
                            startActivity(mIntent)
                            finish()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mIntent = Intent(this, MainActivity::class.java)
                        Logger.e(getActivityName(), "Start " + mIntent!!.component.shortClassName)
//                        EventBus.getDefault().post(NetworkBus(NetworkApi.API111.name))
                        startActivity(mIntent)
                        finish()
                    }
                })
                .addOnFailureListener(this) { e ->
                    e.printStackTrace()
                    Logger.e("onFailure")
                    if (intent != null) {
                        val uri = intent.data
                        Logger.e("uri :: $uri")
                        mIntent = Intent(this@SplashActivity, MainActivity::class.java)
                        Logger.e(getActivityName(), "Start " + mIntent!!.component.shortClassName)
//                        EventBus.getDefault().post(NetworkBus(NetworkApi.API111.name))
                        startActivity(mIntent)
                        finish()
                    }
                }
    }
}