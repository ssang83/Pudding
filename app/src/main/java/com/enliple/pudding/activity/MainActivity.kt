package com.enliple.pudding.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.crashlytics.android.Crashlytics
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.BuildConfig
import com.enliple.pudding.PuddingApplication
import com.enliple.pudding.R
import com.enliple.pudding.bus.VideoPipBus
import com.enliple.pudding.commons.app.BuildHashUtils
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.*
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.fragment.main.pudding.*
import com.enliple.pudding.widget.NotiAlertDialog
import com.enliple.pudding.widget.ShoppingFloatingView
import com.enliple.pudding.widget.SingleButtonDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main_pudding.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*

/**
 * 라이온 메인화면 Activity
 */
class MainActivity : AbsBaseActivity() {
    companion object {
        private const val ACTIVITY_REQUEST_CODE_CASTING = 0xAB01
        private const val ACTIVITY_REQUEST_CODE_LOGIN = 0xAB02
        private const val ACTIVITY_REQUEST_CODE_LOGIN_MY = 0xAB03
        private const val ACTIVITY_REQUEST_CODE_LOGIN_FOLLOWING = 0xAB04
        private const val ACTIVITY_REQUEST_CODE_LOGIN_MESSAGE = 0xAB05
        private const val ACTIVITY_REQUEST_CODE_LIKE = 0xAB06
        private const val ACTIVITY_REQUEST_CODE_LIVE_BUTTON = 0xAB07

        private const val HIT_TIME = "hit_time"
        private const val UI_HIDE_TIME = "ui_hide_time"
        private const val ANDROID_VERSION = "android_version"

        private const val TAB_HOME = 0
        private const val TAB_FOLLOWING = 1
        private const val TAB_ITEM = 2
        private const val TAB_SCHEDULE = 3
        private const val TAB_SHOPPING = 4
    }
    private var pressStartTime: Long? = 0
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var mTimer: Timer? = null
    private var mPressFirstBackKey = false

    private lateinit var mPagerAdapter: MainPagerAdapter

    private var isFromShare = false
    private var shareKey = ""
    private var notiType = ""
    private var streamKey = ""

    private var mFloatView: ShoppingFloatingView? = null // pip
    private var isPause = true
    private var isEnded = false // 영상 재생이 끝난 상태인지 확인 ( play 클릭 시 구분하기 위해 )
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) {
            isFromShare = intent.getBooleanExtra(SplashActivity.FROM_SHARE, false)
            shareKey = intent.getStringExtra(SplashActivity.SHARE_KEY) ?: ""
            notiType = intent.getStringExtra("noti_type") ?: ""
            streamKey = intent.getStringExtra("streamKey") ?: ""
            Logger.e("isFromShare :: $isFromShare")
            Logger.e("shareKey :: $shareKey")
            Logger.e("notiType:$notiType")
            Logger.e("streamKey:$streamKey")
        }

        if (isFromShare) {
            handleShare()
        } else {
            handlePush()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        Logger.e("MainActivity onUserLeaveHint")
        EventBus.getDefault().post("FINISH_PIP_MODE")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overridePendingTransition(0, 0)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_main_pudding)

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)  {
            if( ! AppPreferences.getPipPermission(this@MainActivity)) { // 최초 한번만 띄우기 위해
                if ( !Settings.canDrawOverlays(this@MainActivity) ) {
                            AppPreferences.setPipPermission(this@MainActivity, true)
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${packageName}"))
                            startActivity(intent)
                 }
            }
        }

        if (intent != null) {
            isFromShare = intent.getBooleanExtra(SplashActivity.FROM_SHARE, false)
            shareKey = intent.getStringExtra(SplashActivity.SHARE_KEY) ?: ""
            notiType = intent.getStringExtra("noti_type") ?: ""
            streamKey = intent.getStringExtra("streamKey") ?: ""
            Logger.e("isFromShare :: $isFromShare")
            Logger.e("shareKey :: $shareKey")
            Logger.e("notiType:$notiType")
            Logger.e("streamKey:$streamKey")
        }

        mPagerAdapter = MainPagerAdapter(applicationContext, supportFragmentManager)
        viewPager.adapter = mPagerAdapter
        viewPager.currentItem = TAB_HOME

        btnItem.setOnClickListener(clickListener)
        btnSchedule.setOnClickListener(clickListener)
        btnFollow.setOnClickListener(clickListener)
        btnProfile.setOnClickListener(clickListener)
        //btnMyShopping.setOnClickListener(clickListener)
        btnHome.setOnClickListener(clickListener)

        imageViewShoppingBag.setOnClickListener(clickListener)
        imageViewLive.setOnClickListener(clickListener)
        textViewSearch.setOnClickListener(clickListener)
        imageViewLogo.setOnClickListener(clickListener)

        EventBus.getDefault().register(this) // network 관련 eventBus 를 받는다.

        EventBus.getDefault().post(NetworkBus(NetworkApi.API94.name))

        if (AppPreferences.getLoginStatus(this)) {// 로그인 한 경우 User 정보를 가져온다.
            EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(this)))

            if (!BuildConfig.DEBUG) {
                Crashlytics.setUserName(AppPreferences.getUserId(this))
            }
        }

        Logger.h(AppPreferences.getJWT(this))

        if (isFromShare) {
            handleShare()
        } else {
            handlePush()
        }

        var hash = BuildHashUtils.getKeyHash(this, packageName)
        Logger.e("hash key: $hash")

        btnHome.isSelected = true

        Logger.h("registerPushToken token:${AppPreferences.getPushToken(this)}")

        EventBus.getDefault().post(NetworkBus(NetworkApi.API111.name))

        getNotification()

        // 최악의 경우 8초 뒤에는 무조건 splash 화면을 gone 시킨다.
        Handler().postDelayed({ mainBg?.visibility = View.GONE }, 8000)

        FirebaseAnalytics.getInstance(this).logEvent(javaClass.simpleName, Bundle())
    }

    override fun onResume() {
        super.onResume()
        Logger.e("onResume")

        if (AppPreferences.getLoginStatus(this)) {
            // 로그인 한 경우만 message Count 를 가져온다.
            EventBus.getDefault().post(NetworkBus(NetworkApi.API60.name, AppPreferences.getUserId(this)))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_REQUEST_CODE_CASTING) {
//            if (data != null) {
//                var showDialog = data.getBooleanExtra("IS_DIALOG_SHOW", false)
//                if (showDialog) {
//                    handleVideoCastClicked()
//                }
//            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE_LOGIN) {
//            var userId = AppPreferences.getUserId(applicationContext)
//            if (!TextUtils.isEmpty(userId)) {
//                goCastActivity(userId)
//            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE_LOGIN_MY) {
            if (resultCode == Activity.RESULT_OK) {
                layout_header.visibility = View.GONE
                divider.visibility = View.GONE

                viewPager.setCurrentItem(TAB_SHOPPING, false)
                handleBottomButtonImage(btnProfile)
            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE_LOGIN_FOLLOWING) {
            if (resultCode == Activity.RESULT_OK) {
                layout_header.visibility = View.GONE
                divider.visibility = View.GONE

                viewPager.setCurrentItem(TAB_FOLLOWING, false)
                handleBottomButtonImage(btnFollow)
            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE_LOGIN_MESSAGE) {
            if (resultCode == Activity.RESULT_OK) {
                //startActivity(Intent(this@MainActivity, MessageActivity::class.java))
            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE_LIKE) {
            if (mPagerAdapter != null && mPagerAdapter.puddingItemFragment != null) {
                if (data != null) {
                    val likeStatus = data.getBooleanExtra("LIKE_STATUS", false)
                    val likeStatusIdx = data.getStringExtra("idx")
                    Logger.e("likeStatus :: $likeStatus")
                    Logger.e("idx :: $likeStatusIdx")
                    var strStatus = "N"
                    if (likeStatus)
                        strStatus = "Y"
                    mPagerAdapter.puddingItemFragment!!.setLikeChaged(likeStatusIdx, strStatus)

                } else {
                    Logger.e("data is null")
                }
            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE_LIVE_BUTTON) {
            if (resultCode == Activity.RESULT_OK) {
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun togglePlay() {
        isPause = !isPause
    }

    // 공유로 들어오는 경우
    private fun handleShare() {
        Logger.e("handleShare")

        mainBg?.visibility = View.GONE

        var sKey = NetworkDBKey.getAPI49Key(shareKey)
        Logger.e("sKey :: $sKey")
        var str = DBManager.getInstance(this@MainActivity).get(sKey)
        Logger.e("str :: $str")
        var response = Gson().fromJson(str, API49::class.java)

        var url = response?.stream ?: ""
        var userId = response?.userId

        Logger.e("response :: $response")
        Logger.e("userId :: $userId")
        Logger.d("URL : $url")
        if (!TextUtils.isEmpty(url)) {
            startActivity(Intent(this@MainActivity, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(this@MainActivity))
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_SCREEN_ORIENTATION_VERTICAL_ONLY, true)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_SHARE_KEY, shareKey)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.SHARE_VOD_PLAYER)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_MY_VOD_POSITION, 0)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, 0)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, response.videoType)
                data = Uri.parse("vcommerce://shopping?url=$url")
            })
        } else {
            AppToast(this@MainActivity).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    // 푸쉬로 들어오는 경우
    private fun handlePush() {
        when (notiType) {
            "live" -> {
                if (streamKey.isNotEmpty()) {
                    NetworkBus(NetworkApi.API98.name, streamKey, AppPreferences.getUserId(this)!!)
                            .let { EventBus.getDefault().post(it) }
                }
            }

            "system" -> Logger.d("시스템 관련 Push")

            "noti_follow" -> {
                if (AppPreferences.getLoginStatus(this)) {
                    layout_header.visibility = View.VISIBLE
                    divider.visibility = View.VISIBLE

                    viewPager.setCurrentItem(TAB_FOLLOWING, false)
                    handleBottomButtonImage(btnFollow)
                } else {
                    Logger.e("btnProfile not login")
                    startActivityForResult(Intent(this, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN_FOLLOWING)
                }
            }

            "noti_order" -> {
                handleMyShoppingClicked(btnProfile)
            }

            "noti_format" -> {
                viewPager.setCurrentItem(TAB_SCHEDULE, false)
                handleBottomButtonImage(btnSchedule)
            }

            "noti_sanction" -> startActivity(Intent(this, RestrictionActivity::class.java))
        }
    }

//    private fun handleVideoCastClicked() {
//        var isLogin = AppPreferences.getLoginStatus(applicationContext!!)
//        Logger.e("isLogin :: $isLogin")
//        if (isLogin) {
//            var userId = AppPreferences.getUserId(applicationContext!!)
//            goCastActivity(userId)
//        } else {
//            startActivityForResult(Intent(applicationContext!!, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
//        }
//    }
//
//    private fun goCastActivity(uId: String?) {
//        if (uId == null)
//            return
//        val displayMetrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(displayMetrics)
//        var width = displayMetrics.widthPixels
//        var height = displayMetrics.heightPixels
//
//        CastSelectDialog(this@MainActivity, width, height, object : CastSelectDialog.Listener {
//            override fun liveClicked() {
//                startActivityForResult(Intent(this@MainActivity, BroadcastSettingActivity::class.java)
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, uId) }
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_LIVE) }
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, uId) },
//                        ACTIVITY_REQUEST_CODE_CASTING)
//            }
//
//            override fun multiLiveClicked() {
//                startActivityForResult(Intent(this@MainActivity, BroadcastSettingActivity::class.java)
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, uId) }
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_MULTI_LIVE) }
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, uId) },
//                        ACTIVITY_REQUEST_CODE_CASTING)
//            }
//
//            override fun vodClicked() {
//                startActivityForResult(Intent(this@MainActivity, BroadcastSettingActivity::class.java)
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, uId) }
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "") }
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_VOD) }
//                        .apply { putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, uId) },
//                        ACTIVITY_REQUEST_CODE_CASTING)
//            }
//
//        }).show()
//    }

    private fun handleMyShoppingClicked(view: View) {
        var isLogin = AppPreferences.getLoginStatus(applicationContext)
        if (isLogin) {
            layout_header.visibility = View.GONE
            divider.visibility = View.GONE

            viewPager.setCurrentItem(TAB_SHOPPING, false)
            handleBottomButtonImage(view)
        } else {
            startActivityForResult(Intent(applicationContext, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN_MY)
        }
    }

    /**
     * 방송시작 알림 푸쉬를 눌렀을 경우 방송 라이브 화면으로 이동
     */
    private fun goLiveBroadcast(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            Logger.e("goLiveBroadcast :: " + data.arg2)
            val response = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API98::class.java)
            if (response.data.size > 0) {
                Logger.e("URL : ${response.data[0].stream}")
                if ("Y" == response.data[0].isOnAir) {
                    startActivity(Intent(this, ShoppingPlayerActivity::class.java).apply {
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.SCHEDULE_PLAYER)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_SHARE_KEY, data.arg1)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, response.data[0].userId)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, response.data[0].videoType)

                        setData(Uri.parse("vcommerce://shopping?url=${response.data[0].stream}"))
                    })
                } else {
                    AppToast(this).showToastMessage("종료된 라이브 방송입니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            } else {
                AppToast(this).showToastMessage("종료된 라이브 방송입니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        } else {
            Logger.e("error : ${data.arg3}, ${data.arg4}")
        }
    }

    /**
     * Layer Popup 공지사항의 확인 및 처리
     */
    private fun getNotification() {
        val notiString = AppPreferences.getNotificationList(this)
        try {
            if (notiString.isEmpty()) {
                return
            }

            val noticeData: MutableList<API156.NotiItem> = Gson().fromJson(notiString, object : TypeToken<MutableList<API156.NotiItem>>() {}.type)
            if (noticeData != null) {
                Collections.reverse(noticeData)
                com.annimon.stream.Stream.of(noticeData).forEach { data ->
                    if (data != null && data.hasShowing()) {
                        val dialog = NotiAlertDialog(this@MainActivity,
                                data.title, data.content, data.link,
                                data.no, data.reserve_day)
                        dialog.show()
                    }
                }
            }
        } catch (e: Exception) {
            Logger.p(e)
        }

        AppPreferences.setNotificationList(this, "")
    }

    private val clickListener = View.OnClickListener {
        if (it != null) {
            when (it.id) {
                R.id.imageViewLogo -> {
                    layout_header.visibility = View.VISIBLE
                    divider.visibility = View.VISIBLE

                    viewPager.setCurrentItem(TAB_HOME, false)
                    handleBottomButtonImage(it)
                    btnHome?.isSelected = true
                }

                R.id.btnHome -> {
                    layout_header.visibility = View.VISIBLE
                    divider.visibility = View.VISIBLE

                    viewPager.setCurrentItem(TAB_HOME, false)
                    handleBottomButtonImage(it)
                }

                R.id.btnItem -> {
                    layout_header.visibility = View.VISIBLE
                    divider.visibility = View.VISIBLE

                    viewPager.setCurrentItem(TAB_ITEM, false)
                    handleBottomButtonImage(it)
                }

                R.id.btnProfile -> handleMyShoppingClicked(it)

                R.id.btnFollow -> {
                    if (AppPreferences.getLoginStatus(it.context)) {
                        layout_header.visibility = View.VISIBLE
                        divider.visibility = View.VISIBLE

                        viewPager.setCurrentItem(TAB_FOLLOWING, false)
                        handleBottomButtonImage(it)
                    } else {
                        Logger.e("btnChannel not login")
                        startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN_FOLLOWING)
                    }
                }

                R.id.btnSchedule -> {
                    layout_header.visibility = View.VISIBLE
                    divider.visibility = View.VISIBLE

                    viewPager.setCurrentItem(TAB_SCHEDULE, false)
                    handleBottomButtonImage(it)
                }

                R.id.textViewSearch -> startActivity(Intent(it.context, SearchActivity::class.java))

                R.id.imageViewShoppingBag -> {
                    if (AppPreferences.getLoginStatus(it.context)) {
                        startActivity(Intent(it.context, ProductCartActivity::class.java))
                    } else {
                        startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN_MESSAGE)
                    }
                }

                R.id.imageViewLive -> {
                    if (AppPreferences.getLoginStatus(it.context)) {
                        startActivity(Intent(it.context, LiveBroadcastActivity::class.java))
                    } else {
                        startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LIVE_BUTTON)
                    }
                }
            }
        }
    }

    private fun handleBottomButtonImage(view: View) {
        btnHome.isSelected = false
        btnItem.isSelected = false
        btnSchedule.isSelected = false
        btnFollow.isSelected = false
        //btnProfile.isSelected = false
        view.isSelected = true
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onBackPressed() {
        if (!mPressFirstBackKey) {
            showSecondBackKey()
        } else {
            PuddingApplication.getApplication()?.finishWithProcessKill()
            finish()
        }
    }

    private fun handleNetworkAPI60(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var sumCount = 0
            val response = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API60::class.java)
            if (response?.data != null && response.data.size > 0) {
                for (data in response.data) {
                    sumCount += data.cnt
                }
            }

            showMessageBadgeCount(sumCount)
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private fun handleNetworkAPI21(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            try {
                var response: API21 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API21::class.java)
                if (!response.userIMG.isNullOrEmpty()) {
                    ImageLoad.setImage(this, btnProfile, response.userIMG, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                }

                if (response.cartCount.isNullOrEmpty() || "0" == response.cartCount) {
                    shoppingBagCount?.visibility = View.GONE
                } else {
                    shoppingBagCount?.visibility = View.VISIBLE
                    shoppingBagCount?.text = response.cartCount
                }

                // 로그인 사용자 정보를 저장한다.
                PuddingApplication.mLoginUserData = response
            } catch (e: java.lang.Exception) {
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private fun showMessageBadgeCount(count: Int) {
        Logger.e("showMessageBadgeCount:$count")

        badgeCount.visibility = View.GONE
        if (count > 0) {
            badgeCount.visibility = View.VISIBLE
            badgeCount.text = "$count"
        }
    }

    private fun handleNetworkAppInfo(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response = JSONObject(DBManager.getInstance(this@MainActivity).get(data.arg1))
            if (response["result"] == "success") {
                val jsonArray = response.getJSONArray("data")
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    if (obj["key"] == HIT_TIME) {
                        Logger.d("############### hit_time : ${obj["value"]}")
                        AppPreferences.setHitTime(this@MainActivity, obj["value"].toString().toInt())
                    } else if (obj["key"] == UI_HIDE_TIME) {
                        Logger.d("############### ui_hide_time : ${obj["value"]}")
                        AppPreferences.setHideTiem(this@MainActivity, obj["value"].toString().toInt())
                    } else if (obj["key"] == ANDROID_VERSION) {
                        val obj = JSONObject(obj["value"].toString())
                        Logger.d("############### version : ${obj["version"]}")
                        Logger.d("############### is_force : ${obj["is_force"]}")
                        Logger.d("############### url : ${obj["url"]}")
                        Logger.d("############### reg_date : ${obj["reg_date"]}")

                        AppPreferences.setAppVersion(this, obj["version"].toString())
                        AppPreferences.setForceUpdate(this, obj["is_force"].toString())
                        AppPreferences.setMarketUrl(this, obj["url"].toString())
                        AppPreferences.setAppRegdate(this, obj["reg_date"].toString())
                    }
                }
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(this@MainActivity).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    /**
     * '뒤로가기' 버튼 2회 연속 입력을 통한 종료를 사용자에게 안내하고 처리
     */
    private fun showSecondBackKey() {
        AppToast(this).showToastMessage(R.string.msg_exit_program_double_back,
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM)

        mPressFirstBackKey = true
        val cancelTask: TimerTask = object : TimerTask() {
            override fun run() {
                mTimer?.cancel()
                mTimer = null
                mPressFirstBackKey = false
            }
        }

        mTimer?.cancel()
        mTimer = null

        mTimer = Timer()
        mTimer!!.schedule(cancelTask, AppPreferences.DOUBLE_BACK_PRESS_EXITING_TIME_LIMIT)
    }

    class MainPagerAdapter(context: Context, fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentStatePagerAdapter(fm) {
        private val mFragmentList: MutableList<androidx.fragment.app.Fragment> = ArrayList()
        private var mContext: Context = context
        var puddingItemFragment: PuddingItemFragment? = null

        init {
            puddingItemFragment = PuddingItemFragment()
            mFragmentList.add(PuddingHomeTabFragment())
            mFragmentList.add(PuddingFollowingFragment())
            mFragmentList.add(puddingItemFragment!!)
            mFragmentList.add(PuddingScheduleFragment())
            mFragmentList.add(PuddingShoppingFragment())
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//            super.destroyItem(container, position, `object`)
        }

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int = mFragmentList.size

        override fun getPageTitle(position: Int): CharSequence {
            return ""
        }
    }

//    private fun refreshAllVideoData() {
//        var categories = AppPreferences.getHomeVideoCategoryCode(this)
//        Logger.e("refreshAllVideoData: $categories")
//
//        NetworkHandler.getInstance(this).run(NetworkBus(NetworkApi.VOD0.name, "top", "1", categories))
//        NetworkHandler.getInstance(this).run(NetworkBus(NetworkApi.VOD0.name, "follow", "1", categories))
//        NetworkHandler.getInstance(this).run(NetworkBus(NetworkApi.VOD0.name, "live", "1", categories))
//        NetworkHandler.getInstance(this).run(NetworkBus(NetworkApi.VOD0.name, "vod", "1", categories))
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        var api94 = NetworkHandler.getInstance(this).getKey(NetworkApi.API94.toString(), "", "")
        var api21 = NetworkHandler.getInstance(this).getKey(NetworkApi.API21.toString(), AppPreferences.getUserId(this)!!, "")
        when {
            data.arg1 == api94 -> handleNetworkAppInfo(data)
            data.arg1.startsWith(NetworkApi.API60.toString()) -> handleNetworkAPI60(data) // 새로운 메시지 건수
            data.arg1 == api21 -> handleNetworkAPI21(data) // 내 정보
            data.arg1 == "GET/mui/live/${streamKey}?user=${AppPreferences.getUserId(this)}" -> goLiveBroadcast(data)
            data.arg1 == NetworkApi.API111.toString() -> handleNetworkVersionInfo(data)
            data.arg1 == NetworkApi.API56.toString() -> {
                var str = DBManager.getInstance(this@MainActivity).get(NetworkApi.API56.toString())
                if (!TextUtils.isEmpty(str)) {
                    val response: API56 = Gson().fromJson(str, API56::class.java)
                    Logger.e("customer call :: " + response.data.tel)
                    AppPreferences.setCustomerCenterPhone(this@MainActivity, response.data.tel)
                }
            }
        }
    }

    @Subscribe
    fun onMessageEvent(data: NetworkMidiBus) {
        MidiBusHandler.getInstance(applicationContext, data.arg5).run(data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: VideoPipBus) {
        Logger.e("onMessageEvent: $bus")

        AppPreferences.setPipPosition(this, 0)
        showFloatingView(bus)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: String) {
        if ("FINISH_PIP_MODE" == bus) {
            Logger.e("onMessageEvent: $bus")
            closeFloatingView()
        } else if ("FINISH_MAIN_LOADING" == bus) {
            mainBg?.visibility = View.GONE
            val bus = NetworkBus(NetworkApi.API56.name)
            EventBus.getDefault().post(bus)
        }
    }

    //Video PIP
    private fun showFloatingView(bus: VideoPipBus) {
        if (mFloatView != null) {
            Logger.e("floatingView exist return")
            return
        }
        isEnded = false
        mFloatView = LayoutInflater.from(application).inflate(R.layout.shopping_pip, null) as ShoppingFloatingView

        Logger.e("showFloatingView:${bus?.url}")
        var params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        var playerParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        var emptyParam = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_TOAST
        }
        params.format = PixelFormat.RGBA_8888
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        //params.gravity = Gravity.RIGHT or Gravity.BOTTOM
        //params.x = 0
        //params.y = 0

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        var screenHeight = displayMetrics.heightPixels
        var screenWidth = displayMetrics.widthPixels
        Logger.e("bus.strContentSize :: " + bus.strContentSize)
//        if ("h" == bus.strContentSize) {
//            screenWidth = displayMetrics.heightPixels
//            screenHeight = displayMetrics.widthPixels
//        }

//        val isLandscape = getDisplayRotation() % 180 != 0
//        if (isLandscape && screenWidth < screenHeight || !isLandscape && screenWidth > screenHeight) {
//            screenWidth = displayMetrics.heightPixels
//            screenHeight = displayMetrics.widthPixels
//        }

        screenWidth = AppPreferences.getScreenWidth(this@MainActivity)
        screenHeight = AppPreferences.getScreenHeight(this@MainActivity)

        var width: Int
        var height: Int
        width = screenWidth
        height = Utils.ConvertDpToPx(this@MainActivity, 78)

//        width = align(screenWidth / 3, 8)
//        height = align(screenHeight / 4, 8)

//        if (screenWidth < screenHeight) {
//            width = align(screenWidth / 3, 8)
//            height = align(screenHeight / 4, 8)
//        } else {
//            width = align(screenWidth / 4, 8)
//            height = align(screenHeight / 3, 8)
//        }
        Logger.e("screenWidth :: $screenWidth and width :: $width")
        Logger.e("screenHeight :: $screenWidth and height :: $height")
        params.gravity = Gravity.RIGHT or Gravity.BOTTOM
        params.height = height
        params.width = screenWidth
//        playerParams.width = width
//        playerParams.height = height
//
//        emptyParam.width = width - Utils.ConvertDpToPx(this@MainActivity, 10)
//        emptyParam.height = 0
        var btnPlay = mFloatView?.findViewById<RelativeLayout>(R.id.btnPlay)
        var imgPlay = mFloatView?.findViewById<AppCompatImageView>(R.id.imgPlay)
        if ( isPause ) {
            imgPlay!!.setBackgroundResource(R.drawable.pip_pause_ico)
        } else {
            imgPlay!!.setBackgroundResource(R.drawable.pip_play_btn)
        }

        if ( bus.videoType == "LIVE" )
            btnPlay!!.visibility = View.GONE
        else
            btnPlay!!.visibility = View.VISIBLE
        mFloatView?.findViewById<AppCompatTextView>(R.id.product)?.text = bus.productName
        mFloatView?.findViewById<AppCompatTextView>(R.id.title)?.text = bus.title
        btnPlay!!.setOnClickListener {
            if ( isPause ) {
                if ( isEnded ) {
                    Logger.e("setStream key bus :: " + bus.streamKey)
                    mFloatView?.setData(bus.url, 0, bus.streamKey) // 영상이 끝났으면 0부터 다시 재생해야함
                } else {
                    mFloatView?.playVideo() // 영상이 재생이 다 끝난상태에서 play 눌러도 재생이 안됨
                }
                imgPlay!!.setBackgroundResource(R.drawable.pip_pause_ico)
            } else {
                mFloatView?.pauseVideo()
                imgPlay!!.setBackgroundResource(R.drawable.pip_play_btn)
            }
            togglePlay()
        }
//        mFloatView?.findViewById<View>(R.id.empty)!!.layoutParams = emptyParam
//        mFloatView?.findViewById<PlayerView>(R.id.playerView)!!.layoutParams = playerParams
        mFloatView?.findViewById<RelativeLayout>(R.id.close_button)?.setOnClickListener {
            if (mFloatView != null) {
                var position = mFloatView?.getCurrentPosition()
                var key = mFloatView?.getStreamKey()
                Logger.e("closeFloatingView: $position")
                // main에서 pip 닫을 때는 pip 포지션을 초기화 해줘야 함 그렇지 않으면 ShoppingPlayerActivity의 onResume에서 position이 초기화 되지 않아 계속 중간부터 재생이 시작됨
                if ( bus.type ==  VideoPipBus.TYPE_MAIN ) {
                    AppPreferences.setPipPosition(this, 0)
                    AppPreferences.setPipKey(this, "")
                    Logger.e("pip key empty")
                } else {
                    AppPreferences.setPipPosition(this, position!!)
                    AppPreferences.setPipKey(this, key!!)
                    Logger.e("pip key is :: " + key!!)
                }
                mFloatView?.releaseVideo()
                windowManager.removeView(mFloatView)
                mFloatView = null
                isPause = true
                // auto play 다시 재생가능상태로 만들기위한 broadcast
                val intent = Intent("closeFloatingView")
                LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
            }
//            closeFloatingView()
        }
        mFloatView?.setData(bus.url, bus.position, bus.streamKey)
        Logger.e("setStream key bus1 :: " + bus.streamKey)
        var contentLayer = mFloatView?.findViewById<RelativeLayout>(R.id.contentLayer)
        var btnVisible = mFloatView?.findViewById<AppCompatTextView>(R.id.btnVisible)
        btnVisible!!.setOnClickListener {
            if (contentLayer!!.visibility == View.VISIBLE) {
                contentLayer.visibility = View.GONE
                params.width = Utils.ConvertDpToPx(this@MainActivity, 130)
                btnVisible!!.setBackgroundResource(R.drawable.pip_spread_btn)
                mFloatView?.setParams(params, displayMetrics.widthPixels, displayMetrics.heightPixels, getStatusBarHeight())
            } else {
                contentLayer.visibility = View.VISIBLE
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                btnVisible!!.setBackgroundResource(R.drawable.pip_fold_btn)
                mFloatView?.setParams(params, displayMetrics.widthPixels, displayMetrics.heightPixels, getStatusBarHeight())
            }
        }

        mFloatView?.setCListener(object: ShoppingFloatingView.CListener{
            override fun ended() {
                isEnded = true
                isPause = true
                imgPlay!!.setBackgroundResource(R.drawable.pip_play_btn)
            }

            override fun clicked() {
                if ( bus.type == VideoPipBus.TYPE_MAIN ) {
                    startActivity(Intent(this@MainActivity, ShoppingPlayerActivity::class.java).apply {
                        putExtra("progress", mFloatView?.getCurrentPosition())
                        putExtra(ShoppingPlayerActivity.FROM_WHERE, bus.fromWhere)
                        putExtra(ShoppingPlayerActivity.CASTER_WHAT, bus.casterWhat)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, bus.chatAccount)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, bus.chatNickName)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, bus.chatRoomId)

                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, bus.itemPosition)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_MY_VOD_POSITION, bus.myVODPosition)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, bus.casterId)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, bus.playerFlag)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, bus.videoType)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_SHARE_KEY, bus.shareKey)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_SCREEN_ORIENTATION_VERTICAL_ONLY, bus.requestedOrientation)
                        data = Uri.parse("vcommerce://shopping?url=${bus.url}")
                    })
                }
            }

        })
        mFloatView?.setParams(params, displayMetrics.widthPixels, displayMetrics.heightPixels, getStatusBarHeight())
        mFloatView?.setMute(AppPreferences.getVideoMute(this), bus.volume)
        if (windowManager != null) {
            windowManager.addView(mFloatView, params)
        }

        if ( bus.type == VideoPipBus.TYPE_MAIN || bus.type == VideoPipBus.TYPE_SWIPE ) {
            btnVisible!!.visibility = View.VISIBLE
            contentLayer!!.visibility = View.VISIBLE
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            btnVisible!!.setBackgroundResource(R.drawable.pip_fold_btn)
            if ( bus.type == VideoPipBus.TYPE_MAIN )
                params.y = Utils.ConvertDpToPx(this@MainActivity, 48)
            mFloatView?.setParams(params, displayMetrics.widthPixels, displayMetrics.heightPixels, getStatusBarHeight())
        } else {
            btnVisible!!.visibility = View.VISIBLE
            contentLayer!!.visibility = View.GONE
            btnVisible!!.setBackgroundResource(R.drawable.pip_spread_btn)
            params.width = Utils.ConvertDpToPx(this@MainActivity, 130)
            mFloatView?.setParams(params, displayMetrics.widthPixels, displayMetrics.heightPixels, getStatusBarHeight())
        }
    }

    private fun closeFloatingView() {
        if (mFloatView != null) {
            var position = mFloatView?.getCurrentPosition()
            var key = mFloatView?.getStreamKey()
            Logger.e("closeFloatingView: $position")
            AppPreferences.setPipPosition(this, position!!)
            AppPreferences.setPipKey(this, key!!)
            Logger.e("pip key is :: " + key!!)
            mFloatView?.releaseVideo()
            windowManager.removeView(mFloatView)
            mFloatView = null
            isPause = true
            // auto play 다시 재생가능상태로 만들기위한 broadcast
            val intent = Intent("closeFloatingView")
            LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
        }
    }

    fun getPipStatus(): Boolean{
        if ( mFloatView != null && mFloatView!!.isShown )
            return true
        else
            return false
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun align(value: Int, align: Int): Int {
        return (value + align - 1) / align * align
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
                    val dialogWidth = AppPreferences.getScreenWidth(this) - Utils.ConvertDpToPx(this, 40)
                    var dialog = SingleButtonDialog(this, dialogWidth, "최신버전이 업데이트 되었습니다.\n아래 확인 버튼을 클릭하시기 바랍니다.", "확인",
                            SingleButtonDialog.SingleButtonDialogListener {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse("market://details?id=$packageName")
                                startActivity(intent)
                                finish()
                            })
                    dialog.setCancelable(false)
                    dialog.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        } else {
            finish()
        }
    }
}