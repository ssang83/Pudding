package com.enliple.pudding.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enliple.pudding.R
import com.enliple.pudding.bus.VideoPipBus
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.NetworkUtils
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkConst
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.fragment.main.PreviousProductFragment
import com.enliple.pudding.fragment.main.ShoppingMainFragment
import com.enliple.pudding.fragment.main.VideoCasterProfileFragment
import com.enliple.pudding.model.LikeModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.enliple.pudding.model.PlayerInfo
import kotlinx.android.synthetic.main.activity_video_play.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/**
 * Streaming VideoPlayer Activity
 * @author hkcha
 * @since 2018.05.15
 */
class ShoppingPlayerActivity : AppCompatActivity() {

    companion object {
        const val FROM_NONE = -1
        const val FROM_RIGHT = 0
        const val FROM_LEFT = 1
        const val FROM_WHERE = "from_where"
        const val CASTER_WHAT = "caster_what"
        const val CASTER_VOD = "vod"

        const val FLAG_DEFAULT = "0"
        const val FLAG_FINISH = "1"
        const val FLAG_LEFT_OPENED = "2"
        const val FLAG_RIGHT_OPENED = "3"
        const val FLAG_SCRAP = "4"

        const val INTENT_EXTRA_KEY_POSITION = "current_position"
        const val INTENT_EXTRA_KEY_CHAT_ROOM_ID = "chat_room_id"
        const val INTENT_EXTRA_KEY_CHAT_ACCOUNT = "chat_account"
        const val INTENT_EXTRA_KEY_CHAT_NICKNAME = "chat_nickname"
        const val INTENT_EXTRA_KEY_MY_VOD_POSITION = "my_vod_position"
        const val INTENT_EXTRA_KEY_CASTER_ID = "caster_id"
        const val INTENT_EXTRA_KEY_PLAYER_FLAG = "player_flag"
        const val INTENT_EXTRA_KEY_VIDEO_TYPE = "video_type"
        const val INTENT_EXTRA_KEY_SHARE_KEY = "share_key" // 편성표 관련 리스트 화면에서는 dbKey를 던져서 사용함.

        private const val DRAWER_LEFT_ID = 1001
        private const val DRAWER_CENTER_ID = 1002
        private const val DRAWER_RIGHT_ID = 1003

        const val INTENT_EXTRA_KEY_SCREEN_ORIENTATION_VERTICAL_ONLY = "vertical_only"
        const val INTENT_EXTRA_KEY_FINISH_NO_PENDING_TRANSITION = "finish_no_pending_transition"

        const val ACTIVITY_REQUEST_AUTH = 8227

        var activateIntent: Intent? = null
        var isPlayerLaunched = false
        private var instance: ShoppingPlayerActivity? = null
        //private lateinit var mKeyboardHeightProvider: KeyboardHeightProvider
    }

    lateinit var chatRoomId: String
    lateinit var chatAccount: String
    lateinit var chatNickName: String

    private lateinit var drawerLayoutCenter: FrameLayout
    private lateinit var drawerLayoutLeft: FrameLayout
    private lateinit var drawerLayoutRight: FrameLayout

    var selectedData: VOD.DataBeanX? = null
    var mVideoData: MutableList<VOD.DataBeanX> = mutableListOf()
    var playListFragment: ShoppingMainFragment? = null
    var isOpened: Boolean = false

    private var previousProductFragment: PreviousProductFragment? = null
    private var videoCasterProfileFragment: VideoCasterProfileFragment? = null
    private var likeArray = ArrayList<LikeModel>()
    var itemPosition: Int = 0
    var myVODPosition: Int = -1
    var casterId: String = ""
    var fromWhere = -1
    var casterWhat = ""
    private var playerFlag = -1
    private var videoType = ""


    private var mStartDrawerView = false
    private var shareKey = ""
    private var isVerticalOnly = true
    private var leftOffset = -1.0f
    private var rightOffset = -1.0f
    //    private var timer: Timer? = null
    private var isShoppingContained = false
    private var isFashionContained = false
    private var isBeautyContained = false
    private var isFoodContained = false
    private var isTravelContained = false
    private var handler: Handler? = null
    private var isOverlayPermission: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        Logger.e("ShoppingPlayerActivity onCreate")
        instance = this
        isPlayerLaunched = true

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_video_play)

        layoutRoot.post {
            // Floating 상황에서는 onNewIntent 가 정상적으로 호출되지 않아 정적 변수로 intent 를 관리하여 onResume() cycle 에서
            // activate Intent 가 null 이 아닐경우 재초기화를 수행하게 한다
            activateIntent = intent
            checkIntent(intent)
            activateIntent = null
            initDrawers()
        }

        // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
        //mKeyboardHeightProvider = KeyboardHeightProvider(this)
        //Handler().postDelayed({ mKeyboardHeightProvider.start() }, 1000)
        EventBus.getDefault().register(this)

        checkNetworkStatus()

        // activity에서 영상 데이터 리스트 관리
        mVideoData = VideoDataContainer.getInstance().mVideoData

//        timer = Timer()
//        timer!!.schedule(object : TimerTask() {
//            override fun run() {
//                var size = likeArray.size
//                if (size > 0 ) {
//                    var array = JSONArray()
//                    for ( i in 0 until size ) {
//                        var model = likeArray[i]
//                        var obj = JSONObject()
//                        obj.put("streamKey", model.streamKey)
//                        obj.put("cnt", model.likeCount)
//                        array.put(obj)
//                    }
//                    Logger.e("array val :: " + array.toString())
//                }
//
////                var task = ShopTreeAsyncTask(this@ShoppingPlayerActivity)
////                task.sendLike()
//            }
//        }, 10, 1000 * 1 * 10)
    }

    override fun onNewIntent(intent: Intent?) {
        Logger.d("newIntent()")
        super.onNewIntent(intent)

        activateIntent = intent
        checkIntent(intent)
        activateIntent = null
    }

    override fun onResume() {
        super.onResume()
        Logger.e("onResume")

        if (activateIntent != null) {
            checkIntent(intent)
        }

        registerBR(this@ShoppingPlayerActivity)

        // pip 제거
        if( ! isOverlayPermission) {
            if (leftOffset == 1.0f || rightOffset == 1.0f) {

            } else {
                EventBus.getDefault().post("FINISH_PIP_MODE")
            }
        } else {
            isOverlayPermission = false
        }

        // 영상화면에서 좌우 슬라이드 -> 영상 클릭 -> 영상화면에서 좌우 슬라이드 -> back ( 이때 mute 해제 broadcast 날림 -> back -> 영상 해제 broadcast 날렸으므로 좌우 슬라이드 되어 있는데 재생이 되는 현상이 발생하므로
        // onResume으로 돌아왔을 때 좌우 둘 중 하나 화면이 노출되어 있는 상태라면 다시 mute broadcast를 날려야 한다.
        if (leftOffset == 1.0f || rightOffset == 1.0f) { // 좌우 슬라이드 화면이 둘 중 하나라도 화면에 보여지고 있다면
            Logger.e(" not null and show")
            if ( drawerLayoutMain!!.isDrawerVisible(drawerLayoutLeft as FrameLayout) || drawerLayoutMain!!.isDrawerVisible(drawerLayoutRight as FrameLayout) ) {
                Logger.e(" not null and really show")
                var intent = Intent("muteCalled")
                intent.putExtra("isMute", true)
                LocalBroadcastManager.getInstance(this@ShoppingPlayerActivity).sendBroadcast(intent)
            } else {
                Logger.e(" not null and not really show")
            }
        } else {
            Logger.e(" not null and not show")
        }
    }

    override fun onPause() {
        super.onPause()
        Logger.e("onPause")
        //overridePendingTransition(0, 0) // animation off
        unRegisterBR()
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.e("onDestroy")
        var size = mVideoData.size
        for ( i in 0 until size ) {
            var data = mVideoData.get(i)
            Logger.e("data name :: " + data.title)
            Logger.e("data name :: " + data.favoriteCount)
        }

//        var intent = Intent()
//        intent!!.putExtra("VIDEOS", ArrayList(mVideoData))
//        setResult(RESULT_OK, intent)


        // pip 제거
//        EventBus.getDefault().post("FINISH_PIP_MODE")

        EventBus.getDefault().unregister(this)

        instance = null
        isPlayerLaunched = false

//        if (mKeyboardHeightProvider != null) {
//            mKeyboardHeightProvider.close()
//        }
    }

    override fun onBackPressed() {
        Logger.e("onBackPressed")

        if (isOpened) {
            if (fromWhere == FROM_NONE) {
                drawerLayoutMain.closeDrawer(Gravity.LEFT)
                drawerLayoutMain.closeDrawer(Gravity.RIGHT)
            } else if (fromWhere == FROM_LEFT) {
                drawerLayoutMain.closeDrawer(Gravity.RIGHT)
            }
        } else {
//            playListFragment?.getCurrentPosition()

            //Main 화면과 동영상 싱크 맞추기 위해 현재 위치를 보낸다.
//            var pos = playListFragment?.getCurrentPosition()
//            var bus = ActivityResultBus(what, pos!!, null)
//            EventBus.getDefault().post(bus)
            if ( playListFragment != null )
                Logger.e("productdialog show : " + playListFragment!!.isProductDialogShow())
            if ( playListFragment != null && playListFragment!!.isProductDialogShow() ) {
                playListFragment!!.closeSubPop()
            } else {
                sendLikes(FLAG_FINISH, "", "", "")
            }
            //overridePendingTransition(0, R.anim.fade_out)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: String) {
        if(bus == "startActivityForResult_OVERLAY") {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${packageName}"))
            startActivityForResult(intent, 1234)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234) {
            isOverlayPermission = true
            Logger.e("onActivityResult requst 1234")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Logger.e("canoverlay value :: " + Settings.canDrawOverlays(this))
                EventBus.getDefault().post("canDrawOverlays==true")
            }
        } else if ( requestCode == ACTIVITY_REQUEST_AUTH ) {
            if ( resultCode == RESULT_OK ) {
                var url = "${NetworkConst.COOKIE_PAYMENT_API}${AppPreferences.getUserId(this@ShoppingPlayerActivity)}"
                startActivity(Intent(this@ShoppingPlayerActivity, LinkWebViewActivity::class.java).apply {
                    putExtra("LINK", url)
                    putExtra("TITLE", "젤리 구매")
                    putExtra("BUY_MODE", true)
                })
            }
        }
    }

//    @Subscribe
//    fun onMessageEvent(bus: SoftKeyboardBus) {
//        Logger.e("SoftKeyboardBus height: ${bus.height}")
//    }

    /**
     * 호출된 AppLink 체크
     */
    private fun checkIntent(intent: Intent?) {
        if (intent == null) return

        Logger.d("checkIntent - ${intent.data}")
        //videoUrl = intent.data.getQueryParameter("url")
        fromWhere = intent.getIntExtra(FROM_WHERE, FROM_NONE)
        casterWhat = intent.getStringExtra(CASTER_WHAT) ?: ""
        chatAccount = intent.getStringExtra(INTENT_EXTRA_KEY_CHAT_ACCOUNT) ?: ""
        chatNickName = intent.getStringExtra(INTENT_EXTRA_KEY_CHAT_NICKNAME) ?: ""
        chatRoomId = intent.getStringExtra(INTENT_EXTRA_KEY_CHAT_ROOM_ID) ?: ""
        itemPosition = intent.getIntExtra(INTENT_EXTRA_KEY_POSITION, 0)
        //item = intent.getParcelableExtra(INTENT_EXTRA_KEY_VOD_DATA)

        // 내영상에서 호출 하는 경우 ( 1개의 영상만 play 하면 된다 )
        myVODPosition = intent.getIntExtra(INTENT_EXTRA_KEY_MY_VOD_POSITION, myVODPosition)
        casterId = intent.getStringExtra(INTENT_EXTRA_KEY_CASTER_ID) ?: ""
        playerFlag = intent.getIntExtra(INTENT_EXTRA_KEY_PLAYER_FLAG, playerFlag)
        videoType = intent.getStringExtra(INTENT_EXTRA_KEY_VIDEO_TYPE) ?: ""
        shareKey = intent.getStringExtra(INTENT_EXTRA_KEY_SHARE_KEY) ?: ""
        isVerticalOnly = intent.getBooleanExtra(INTENT_EXTRA_KEY_SCREEN_ORIENTATION_VERTICAL_ONLY, true)
        if (isVerticalOnly) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun initDrawers() {
        drawerLayoutLeft = FrameLayout(this@ShoppingPlayerActivity).apply {
            id = DRAWER_LEFT_ID
            layoutParams = androidx.drawerlayout.widget.DrawerLayout.LayoutParams(androidx.drawerlayout.widget.DrawerLayout.LayoutParams.MATCH_PARENT,
                    androidx.drawerlayout.widget.DrawerLayout.LayoutParams.MATCH_PARENT).apply {
                gravity = Gravity.START
                isFocusableInTouchMode = false
            }
        }

        drawerLayoutCenter = FrameLayout(this@ShoppingPlayerActivity).apply {
            id = DRAWER_CENTER_ID
            layoutParams = androidx.drawerlayout.widget.DrawerLayout.LayoutParams(androidx.drawerlayout.widget.DrawerLayout.LayoutParams.MATCH_PARENT,
                    androidx.drawerlayout.widget.DrawerLayout.LayoutParams.MATCH_PARENT).apply {
                gravity = Gravity.NO_GRAVITY
            }
        }

        drawerLayoutRight = FrameLayout(this@ShoppingPlayerActivity).apply {
            id = DRAWER_RIGHT_ID
            layoutParams = androidx.drawerlayout.widget.DrawerLayout.LayoutParams(androidx.drawerlayout.widget.DrawerLayout.LayoutParams.MATCH_PARENT,
                    androidx.drawerlayout.widget.DrawerLayout.LayoutParams.MATCH_PARENT).apply {
                gravity = Gravity.END
                isFocusableInTouchMode = false
            }
        }

        drawerLayoutMain.addView(drawerLayoutCenter)
//        drawerLayoutMain.addView(drawerLayoutLeft)
//        drawerLayoutMain.addView(drawerLayoutRight)
        if (fromWhere == FROM_NONE) {
            drawerLayoutMain.addView(drawerLayoutLeft)
            drawerLayoutMain.addView(drawerLayoutRight)
        } else if (fromWhere == FROM_LEFT) {
            drawerLayoutMain.addView(drawerLayoutRight)
        }

        drawerLayoutMain.addDrawerListener(drawerListener)

        drawerLayoutMain.post {
            initFragments(fromWhere)
        }
    }

    private fun checkNetworkStatus() {
        if (!AppPreferences.getUseMobileAlert(this)) {
            return
        }

        var status = NetworkStatusUtils.getNetworkStatus(NetworkUtils.getConnectivityStatus(applicationContext))
        Logger.e("checkNetworkStatus: $status")

        var toastString = when (status) {
            NetworkStatusUtils.NetworkStatus.MOBILE -> "3G/4G로 시청하실 경우 데이터 요금이 부과될 수 있습니다."
            else -> ""
        }

        if (toastString.isNotEmpty()) {
            AppToast(this@ShoppingPlayerActivity).showToastMessage(toastString,
                    AppToast.DURATION_MILLISECONDS_FAST,
                    AppToast.GRAVITY_MIDDLE)
        }
    }

    private val drawerListener = object : androidx.drawerlayout.widget.DrawerLayout.DrawerListener {
        override fun onDrawerStateChanged(p0: Int) {
            if (p0 == 0) { // 아이들 상태이면서
                if (leftOffset == 1.0f || rightOffset == 1.0f) { // 왼쪽이나 오른쪽이 둘 중 하나는 화면에 한번은 다 보여저 있는 상태
                    Logger.e("sttta 오프셋 상으로 left나 right 둘 중 하나는 보여짐")
                    // 뮤트를 시켜줘야 하는데 이게 실제로 화면에 둘 중 하나가 보여지고 있는지 체크
                    if ( drawerLayoutMain!!.isDrawerVisible(drawerLayoutLeft as FrameLayout) || drawerLayoutMain!!.isDrawerVisible(drawerLayoutRight as FrameLayout) ) {
                        Logger.d("sttta left right 둘 중 하나라도 보여지고 있음 뮤트시킴")
                        // 실제 둘 중 하나가 보여지고 있으면 뮤트
                        var intent = Intent("muteCalled")
                        intent.putExtra("isMute", true)
                        LocalBroadcastManager.getInstance(this@ShoppingPlayerActivity).sendBroadcast(intent)
                        sendPip(VideoPipBus.TYPE_SWIPE)
//                        AppPreferences.setVideoMute(this@ShoppingPlayerActivity, true)
//                        EventBus.getDefault().post(VideoMuteBus(true))
                    } else {
                        Logger.d("sttta left right 둘 중 하나라도 true 가 없음")
                    }
                } else if ( leftOffset == 0.0f || rightOffset == 0.0f ) {
                    Logger.e("sttta 오프셋 상으로 left나 right 둘 중 하나는 사라짐")
                    if (!drawerLayoutMain!!.isDrawerVisible(drawerLayoutLeft as FrameLayout) && !drawerLayoutMain!!.isDrawerVisible(drawerLayoutRight as FrameLayout)) {
                        Logger.d("sttta left right 둘 다 실제로 보여지지 않음 소리 키움")
                        var intent = Intent("muteCalled")
                        intent.putExtra("isMute", false)
                        LocalBroadcastManager.getInstance(this@ShoppingPlayerActivity).sendBroadcast(intent)
//                        AppPreferences.setVideoMute(this@ShoppingPlayerActivity, false)
//                        EventBus.getDefault().post(VideoMuteBus(false))
                    } else {
                        Logger.d("sttta left right 둘 중 하나라도 보여지고 있음")
                    }
                }
            }
        }

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            if (drawerView.id == 1001) {
                leftOffset = slideOffset
            }
            if (drawerView.id == 1003) {
                rightOffset = slideOffset
            }
        }

        override fun onDrawerClosed(drawerView: View) {
            Logger.e("onDrawerClosed")
            isOpened = false
            mStartDrawerView = false
//            var intent = Intent("muteCalled")
//            intent.putExtra("isMute", false)
//            LocalBroadcastManager.getInstance(this@ShoppingPlayerActivity).sendBroadcast(intent)
//            AppPreferences.setVideoMute(this@ShoppingPlayerActivity, false)
//            EventBus.getDefault().post(VideoMuteBus(false))

            when (drawerView?.id) {
                drawerLayoutLeft.id -> previousProductFragment?.init()
                else -> videoCasterProfileFragment?.init()
            }

            //drawerLayoutMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, drawerLayoutLeft)
            //drawerLayoutMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, drawerLayoutRight)
        }

        override fun onDrawerOpened(drawerView: View) {
            Logger.e("onDrawerOpened")
            isOpened = true
            mStartDrawerView = true
            drawerLayoutCenter.isFocusableInTouchMode = false

//            AppPreferences.setVideoMute(this@ShoppingPlayerActivity, true)
//            EventBus.getDefault().post(VideoMuteBus(true))
//
            startDrawer(drawerView)
        }
    }

    private fun startDrawer(drawerView: View) {
        when (drawerView?.id) {
            drawerLayoutLeft.id -> {
                Logger.e("onDrawerOpened drawerLayoutLeft")
                //drawerLayoutMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, drawerLayoutLeft)

                sendLikes(FLAG_LEFT_OPENED, "", "", "")
                FirebaseAnalytics.getInstance(this).logEvent(previousProductFragment!!.javaClass.simpleName, Bundle())
            }

            drawerLayoutRight.id -> {
                Logger.e("onDrawerOpened drawerLayoutRight")
                sendLikes(FLAG_RIGHT_OPENED, "", "", "")
//                videoCasterProfileFragment?.loadData(selectedData?.userId!!)

                FirebaseAnalytics.getInstance(this).logEvent(videoCasterProfileFragment!!.javaClass.simpleName, Bundle())
            }
        }
    }

    private fun initFragments(fromWhere: Int) {
        var transaction = supportFragmentManager.beginTransaction()

        playListFragment = supportFragmentManager.findFragmentByTag(ShoppingMainFragment::class.java.name) as? ShoppingMainFragment
        if (playListFragment == null) {
            playListFragment = ShoppingMainFragment()
            playListFragment!!.arguments = Bundle().apply {
                this.putInt(INTENT_EXTRA_KEY_POSITION, itemPosition)
                this.putInt(FROM_WHERE, fromWhere)
                this.putString(CASTER_WHAT, casterWhat)
                this.putInt(INTENT_EXTRA_KEY_MY_VOD_POSITION, myVODPosition)
                this.putInt(INTENT_EXTRA_KEY_PLAYER_FLAG, playerFlag)
                this.putString(INTENT_EXTRA_KEY_VIDEO_TYPE, videoType)
                this.putString(INTENT_EXTRA_KEY_SHARE_KEY, shareKey)
            }
        }

        if (fromWhere == FROM_NONE) {
            previousProductFragment = supportFragmentManager.findFragmentByTag(PreviousProductFragment::class.java.name) as? PreviousProductFragment
            if (previousProductFragment == null) {
                previousProductFragment = PreviousProductFragment()
            }

            videoCasterProfileFragment = supportFragmentManager.findFragmentByTag(VideoCasterProfileFragment::class.java.name) as? VideoCasterProfileFragment
            if (videoCasterProfileFragment == null) {
                videoCasterProfileFragment = VideoCasterProfileFragment()
            }
        } else if (fromWhere == FROM_LEFT) {
            videoCasterProfileFragment = supportFragmentManager.findFragmentByTag(VideoCasterProfileFragment::class.java.name) as? VideoCasterProfileFragment
            if (videoCasterProfileFragment == null) {
                videoCasterProfileFragment = VideoCasterProfileFragment()
            }
        }

        if (fromWhere == FROM_NONE) {
            transaction.replace(drawerLayoutCenter.id, playListFragment!!, ShoppingMainFragment::class.java.name)
            transaction.replace(drawerLayoutLeft.id, previousProductFragment!!, PreviousProductFragment::class.java.name)
            transaction.replace(drawerLayoutRight.id, videoCasterProfileFragment!!, VideoCasterProfileFragment::class.java.name)
        } else if (fromWhere == FROM_RIGHT) {
            transaction.replace(drawerLayoutCenter.id, playListFragment!!, ShoppingMainFragment::class.java.name)
        } else if (fromWhere == FROM_LEFT) {
            transaction.replace(drawerLayoutCenter.id, playListFragment!!, ShoppingMainFragment::class.java.name)
            transaction.replace(drawerLayoutRight.id, videoCasterProfileFragment!!, VideoCasterProfileFragment::class.java.name)
        }

        transaction.commitAllowingStateLoss()
    }

    private fun registerBR(context: Context?) {
        if (context != null) {
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).registerReceiver(drawerOpenReceiver,
                    IntentFilter().apply {
                        addAction("open_drawer")
                        addAction("close_drawer")
                    })
        }
    }

    private fun unRegisterBR() {
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this@ShoppingPlayerActivity).unregisterReceiver(drawerOpenReceiver)
    }

    private val drawerOpenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "open_drawer") {
                var isLeftOpen = intent!!.getBooleanExtra("isLeftOpen", false)
                Logger.e("isLeftOpen:$isLeftOpen fromWhere:$fromWhere")
                if (isLeftOpen) {
                    if (fromWhere == FROM_NONE) {
                        drawerLayoutMain.openDrawer(Gravity.LEFT)
                    } else {
                        AppToast(this@ShoppingPlayerActivity).showToastMessage("이동 할 수 없습니다.",
                                AppToast.DURATION_MILLISECONDS_FAST,
                                AppToast.GRAVITY_MIDDLE)
                    }
                } else {
                    if (fromWhere == FROM_NONE || fromWhere == FROM_LEFT) {
                        drawerLayoutMain.openDrawer(Gravity.RIGHT)
                    } else {
                        AppToast(this@ShoppingPlayerActivity).showToastMessage("이동 할 수 없습니다.",
                                AppToast.DURATION_MILLISECONDS_FAST,
                                AppToast.GRAVITY_MIDDLE)
                    }
                }
            } else if (intent?.action == "close_drawer") {
                var isRightClose = intent!!.getBooleanExtra("isRightClose", false)
                if (isRightClose) {
                    drawerLayoutMain.closeDrawer(Gravity.RIGHT)
                } else {
                    drawerLayoutMain.closeDrawer(Gravity.LEFT)
                }
            }
        }
    }

    fun exitClicked() {
        sendLikes(FLAG_FINISH, "", "", "")
    }

    fun setLike(model: LikeModel) {
        Logger.e("setLike clicked steamKey :: "  + model.streamKey)
        var size = likeArray.size
        var isExist = false;
        if ( size <= 0 ) { // 현재 좋아요 클릭한 것이 없을 경우
            likeArray.add(model)
        } else { // 좋아요 클릭이 있을 경우
            for ( i in 0 until size ) {
                var streamKey = likeArray[i].streamKey
                Logger.e("streamKey")
                if ( streamKey.equals(model.streamKey) ) { // 이전에 좋아요 했던 영상을 다시 좋아요 했을 경우에는 기존 데이터의 값만 갱신한다.
                    likeArray.set(i, model)
                    isExist = true
                }
            }
            if ( !isExist ) { // 이전 좋아요 했던 영상이 없을 경우
                likeArray.add(model)
            }
        }

//        if ( timer == null ) {
//            Logger.e("timer null")
//            timer = Timer()
//            timer!!.schedule(object : TimerTask() {
//                override fun run() {
//                    Logger.e("run")
//                    sendLikes(FLAG_DEFAULT) // 서버에 좋아요 카운트 전달하고 LocalBroadcast로 UI 갱신을 위해 값을 전달
//                    likeArray = ArrayList<LikeModel>() // 새로데이터를 세팅해야하므로 Array를 초기화
//                    this.cancel() // 데이터 전달완료 했으므로 timer task는 cancel, 이후 좋아요 클릭이 들어올 경우 이때 타이머를 다시 실행시킴
//                    timer = null
//
//
//
//                }
//            }, 1000 * 1 * 10, 1000 * 1 * 10)
//        }

        if ( handler == null ) {
            handler = Handler()
            handler!!.postDelayed(object: Runnable{
                override fun run() {
                    sendLikes(FLAG_DEFAULT, "", "", "") // 서버에 좋아요 카운트 전달하고 LocalBroadcast로 UI 갱신을 위해 값을 전달
                    likeArray = ArrayList<LikeModel>() // 새로데이터를 세팅해야하므로 Array를 초기화
                    handler = null
                }
            }, 1000 * 1 * 10)
        }

//        if ( timer == null ) {
//            Logger.e("timer null")
//            timer = Timer()
//            timer!!.schedule(timeTask, 1000 * 1 * 10, 1000 * 1 * 10)
//        }
    }

//    private val timeTask = object : TimerTask() {
//        override fun run() {
//            Logger.e("run")
//            sendLikes(FLAG_DEFAULT) // 서버에 좋아요 카운트 전달하고 LocalBroadcast로 UI 갱신을 위해 값을 전달
//            likeArray = ArrayList<LikeModel>() // 새로데이터를 세팅해야하므로 Array를 초기화
//            this.cancel() // 데이터 전달완료 했으므로 timer task는 cancel, 이후 좋아요 클릭이 들어올 경우 이때 타이머를 다시 실행시킴
//            timer = null
//        }
//    }

    private fun sendLikes( flag: String, streamKey: String, videoType: String, status: String) {
        var task = ShopTreeAsyncTask(this@ShoppingPlayerActivity)
        var size = likeArray.size
        if (size > 0 ) {
            var array = JSONArray() // 서버에 전달하기 위한 증가분 카운트
            var tArray = JSONArray() // ui 갱신을 위한 전체 카운트
            for ( i in 0 until size ) {
                var model = likeArray[i]

                var obj = JSONObject()
                obj.put("streamKey", model.streamKey)
                obj.put("cnt", model.increaseLikeCount)
                array.put(obj)

                var tObj = JSONObject()
                tObj.put("streamKey", model.streamKey)
                tObj.put("cnt", model.likeCount)
                tObj.put("category", model.category)
                tArray.put(tObj)
            }
            Logger.e("array val :: " + array.toString())
            Logger.e("tArray val :: " + tArray.toString())
            task.sendLikeInfos(array.toString(), object: ShopTreeAsyncTask.OnDefaultObjectCallbackListener{
                override fun onResponse(result: Boolean, obj: Any?) {
                    Logger.e("sendLikeInfos result :: " + result )
                    if ( result ) {
                        Logger.e("tArray.length() :: " + tArray.length())
                        if ( tArray != null && tArray.length() == 1 ) {
                            var zObject = tArray.getJSONObject(0)
                            var sKey = zObject.optString("streamKey")
                            Logger.e("streamKey")
                            EventBus.getDefault().post(NetworkBus(NetworkApi.API143.name, sKey)) // 현재 Video 정보 갱신
                        }

                        var objectValue = obj as JSONObject
                        Logger.e("objectValue :: " + objectValue.toString())
                        var intent = Intent("likeChange")
                        intent.putExtra("change", tArray.toString())
                        intent.putExtra("streamKey", streamKey)
                        LocalBroadcastManager.getInstance(this@ShoppingPlayerActivity).sendBroadcast(intent)
                    }
                    if ( flag == FLAG_FINISH ) {
                        Logger.e("fromWhere :: " + fromWhere)
                        if ( fromWhere == FROM_LEFT || fromWhere == FROM_RIGHT )
                            sendPip(VideoPipBus.TYPE_SWIPE)
                        else
                            sendPip(VideoPipBus.TYPE_MAIN)

                        likeArray = ArrayList<LikeModel>()
                        if ( handler != null ) {
                            handler!!.removeCallbacksAndMessages(null)
                            handler = null
                        }
                        finish()
                    } else if ( flag == FLAG_LEFT_OPENED ) {
                        // 왼쪽 화면 열기 전에 좋아요 쌓였던 값들 모두 서버에 전달하기 때문에 데이터 초기화 하고 timer stop 해서 더이상 데이터전달하지 못하도록 함
                        likeArray = ArrayList<LikeModel>() // 새로데이터를 세팅해야하므로 Array를 초기화
                        if ( handler != null ) {
                            handler!!.removeCallbacksAndMessages(null)
                            handler = null
                        }
                        if (previousProductFragment != null) {
                            previousProductFragment?.loadData(selectedData!!.id, selectedData!!.strTag, "1") // 여기서는 정렬을 1로 보내지만 실제로는 loadData내에서 isFavorite 변수로 적용하게 변경함. loadData에서 여기서넘기는 변수 세팅하는 부분 주석처리함
                        } else {
                            Logger.e("previousProductFragment null")
                        }
                    } else if ( flag == FLAG_RIGHT_OPENED ) {
                        // 오른쪽 화면 열기 전에 좋아요 쌓였던 값들 모두 서버에 전달하기 때문에 데이터 초기화 하고 timer stop 해서 더이상 데이터전달하지 못하도록 함
                        likeArray = ArrayList<LikeModel>() // 새로데이터를 세팅해야하므로 Array를 초기화
                        if ( handler != null ) {
                            handler!!.removeCallbacksAndMessages(null)
                            handler = null
                        }
                        videoCasterProfileFragment?.loadData(selectedData?.userId!!)
                    } else if ( flag == FLAG_SCRAP ) {
                        likeArray = ArrayList<LikeModel>()
                        if ( handler != null ) {
                            handler!!.removeCallbacksAndMessages(null)
                            handler = null
                        }
                        handleScrap(streamKey, videoType, status)
                    }
                }
            })
        } else {
            Logger.e("size less zero")
            if ( flag == FLAG_FINISH ) {
                Logger.e("fromWhere :: " + fromWhere)
                if ( fromWhere == FROM_LEFT || fromWhere == FROM_RIGHT )
                    sendPip(VideoPipBus.TYPE_SWIPE)
                else
                    sendPip(VideoPipBus.TYPE_MAIN)

                likeArray = ArrayList<LikeModel>()
                if ( handler != null ) {
                    handler!!.removeCallbacksAndMessages(null)
                    handler = null
                }
                finish()
            } else if ( flag == FLAG_LEFT_OPENED ) {
                if (previousProductFragment != null) {
                    previousProductFragment?.loadData(selectedData!!.id, selectedData!!.strTag, "1")
                } else {
                    Logger.e("previousProductFragment null")
                }
            } else if ( flag == FLAG_RIGHT_OPENED ) {
                videoCasterProfileFragment?.loadData(selectedData?.userId!!)
            } else if ( flag == FLAG_SCRAP ) {
                Logger.e("scrap")
                likeArray = ArrayList<LikeModel>()
                if ( handler != null ) {
                    handler!!.removeCallbacksAndMessages(null)
                    handler = null
                }
                handleScrap(streamKey, videoType, status)
//                var tArray = JSONArray()
//                var intent = Intent("likeChange")
//                intent.putExtra("change", tArray.toString())
//                intent.putExtra("streamKey", streamKey)
//                LocalBroadcastManager.getInstance(this@ShoppingPlayerActivity).sendBroadcast(intent)
            }
        }
    }

    fun scrapClicked(streamKey: String, videoType: String, status: String) {
        sendLikes(FLAG_SCRAP, streamKey, videoType, status)
    }

    private fun handleScrap(streamKey: String, videoType: String, status: String) {
        Logger.d("handleScrap:$status")
        if ( "Y" == status ) {
            val task = ShopTreeAsyncTask(this@ShoppingPlayerActivity)
            task.deletetScrap(streamKey) { result, obj ->
                if ( result ) {
                    var resultObj = obj as JSONObject
                    var rt = resultObj.optString("result")
                    Logger.e("scrap delete rt :: " + rt)
                    if ( "success" == rt ) {
                        try {
                            var tArray = JSONArray()
                            var intent = Intent("likeChange")
                            intent.putExtra("change", tArray.toString())
                            intent.putExtra("streamKey", streamKey)
                            intent.putExtra("status", status)
                            LocalBroadcastManager.getInstance(this@ShoppingPlayerActivity).sendBroadcast(intent)
                        } catch (e:Exception) {
                            Logger.p(e)
                        }
                    }
                }
            }
        } else {
            val obj = JSONObject().apply {
                put("streamKey", streamKey)
                put("user", AppPreferences.getUserId(this@ShoppingPlayerActivity))
                put("vod_type", videoType)
            }

            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
            val task = ShopTreeAsyncTask(this@ShoppingPlayerActivity)
            task.postScrap(body) { result, obj ->
                if ( result ) {
                    var resultObj = obj as JSONObject
                    var rt = resultObj.optString("result")
                    Logger.e("scrap rt :: " + rt)
                    if ( "success" == rt ) {
                        try {
                            var tArray = JSONArray()
                            var intent = Intent("likeChange")
                            intent.putExtra("change", tArray.toString())
                            intent.putExtra("streamKey", streamKey)
                            intent.putExtra("status", status)
                            LocalBroadcastManager.getInstance(this@ShoppingPlayerActivity).sendBroadcast(intent)
                        } catch (e:Exception) {
                            Logger.p(e)
                        }
                    }
                }
            }
        }
    }

    private fun sendPip(type: String) {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if ( Settings.canDrawOverlays(this@ShoppingPlayerActivity) ) {
                if ( playListFragment != null ) {
                    var pipInfo = playListFragment!!.getCurrentPipInfo()
                    if ( pipInfo != null ) {
                        Logger.e("videoUrl :: " + pipInfo!!.videoUrl)
                        Logger.e("currentPosition :: " + pipInfo!!.currentPosition)
                        Logger.e("contentSize :: " + pipInfo!!.contentSize)
                        Logger.e("volume :: " + pipInfo!!.volume)

                        var playerInfo = PlayerInfo()
                        playerInfo.fromWhere = fromWhere
                        playerInfo.casterWhat = casterWhat
                        playerInfo.chatAccount = chatAccount
                        playerInfo.chatNickName = chatNickName
                        playerInfo.chatRoomId = chatRoomId
                        playerInfo.itemPosition = playListFragment!!.getCurrentPosition()
                        playerInfo.myVODPosition = myVODPosition
                        playerInfo.casterId = casterId
                        playerInfo.playerFlag = playerFlag
                        playerInfo.videoType = pipInfo!!.videoType
                        playerInfo.shareKey = shareKey
                        playerInfo.isRequestedOrientation = isVerticalOnly
                        playerInfo.title = pipInfo!!.title
                        playerInfo.productName = pipInfo!!.productName
                        playerInfo.type = type
                        playerInfo.streamKey = pipInfo!!.streamKey
                        var bus = VideoPipBus(pipInfo!!.videoUrl, pipInfo!!.currentPosition, pipInfo!!.contentSize, pipInfo!!.volume, playerInfo)

                        EventBus.getDefault().post(bus)
                    }

                }
            }
//            if( ! AppPreferences.getPipPermission(this@ShoppingPlayerActivity)) {
//                AppPreferences.setPipPermission(this@ShoppingPlayerActivity, true)
//                EventBus.getDefault().post("startActivityForResult_OVERLAY")
//            } else {
//                if ( Settings.canDrawOverlays(this@ShoppingPlayerActivity) ) {
//                    if ( playListFragment != null ) {
//                        var pipInfo = playListFragment!!.getCurrentPipInfo()
//                        if ( pipInfo != null ) {
//                            Logger.e("videoUrl :: " + pipInfo!!.videoUrl)
//                            Logger.e("currentPosition :: " + pipInfo!!.currentPosition)
//                            Logger.e("contentSize :: " + pipInfo!!.contentSize)
//                            Logger.e("volume :: " + pipInfo!!.volume)
//
//                            var playerInfo = PlayerInfo()
//                            playerInfo.fromWhere = fromWhere
//                            playerInfo.casterWhat = casterWhat
//                            playerInfo.chatAccount = chatAccount
//                            playerInfo.chatNickName = chatNickName
//                            playerInfo.chatRoomId = chatRoomId
//                            playerInfo.itemPosition = playListFragment!!.getCurrentPosition()
//                            playerInfo.myVODPosition = myVODPosition
//                            playerInfo.casterId = casterId
//                            playerInfo.playerFlag = playerFlag
//                            playerInfo.videoType = videoType
//                            playerInfo.shareKey = shareKey
//                            playerInfo.isRequestedOrientation = isVerticalOnly
//                            playerInfo.title = pipInfo!!.title
//                            playerInfo.productName = pipInfo!!.productName
//                            playerInfo.type = type
//                            var bus = VideoPipBus(pipInfo!!.videoUrl, pipInfo!!.currentPosition, pipInfo!!.contentSize, pipInfo!!.volume, playerInfo)
//
//                            EventBus.getDefault().post(bus)
//                        }
//
//                    }
//                }
//            }
        }
    }
}