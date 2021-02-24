package com.enliple.pudding.fragment.main

import android.app.Activity
import android.app.KeyguardManager
import android.content.*
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.couchbase.lite.MutableDocument
import com.enliple.pudding.R
import com.enliple.pudding.activity.*
import com.enliple.pudding.adapter.home.CookieSenderAdapter
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.bus.VideoMuteBus
import com.enliple.pudding.bus.VideoPipBus
import com.enliple.pudding.bus.ZzimStatusBus
import com.enliple.pudding.common.VideoManager
import com.enliple.pudding.commons.app.*
import com.enliple.pudding.commons.chat.ChatManager
import com.enliple.pudding.commons.chat.adapter.ChatAdapter
import com.enliple.pudding.commons.chat.dao.AdapterChatData
import com.enliple.pudding.commons.chat.dao.ChatUser
import com.enliple.pudding.commons.data.ApiParams
import com.enliple.pudding.commons.data.DialogModel
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.db.VideoDBManager
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.*
import com.enliple.pudding.commons.shoppingcommons.widget.HeartView
import com.enliple.pudding.commons.shoppingcommons.widget.LiveBroadcastFinishedDialog
import com.enliple.pudding.commons.shoppingcommons.widget.LiveUserListDialog
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.keyboard.KeyboardHeightProvider
import com.enliple.pudding.model.PipInfo
import com.enliple.pudding.model.PlayerInfo
import com.enliple.pudding.shoppingcaster.data.CookieData
import com.enliple.pudding.shoppingcaster.data.LiveUserActionData
import com.enliple.pudding.shoppingcaster.data.LiveUserJoinData
import com.enliple.pudding.widget.*
import com.enliple.pudding.widget.main.NickNameModifyDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.share.Sharer
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.MessageDialog
import com.facebook.share.widget.ShareDialog
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.*
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import kotlinx.android.synthetic.main.dialog_share.view.*
import kotlinx.android.synthetic.main.fragment_shopping_live.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap

/**
 * 라이브 재생 전용 Fragment
 */
class ShoppingLiveFragment : androidx.fragment.app.Fragment(), ChatManager.ChatListener {
    companion object {
        private const val HIDE_UI_TIME_OUT = 50000L  //5초
        private const val DELAY = 1000L
        private const val TIME_UNIT = 1000L     // 1초
        private const val REFRESH_LIVE_INFO = 10000L * 3   // 30초

        private const val SPACE_KAKAO = "kakao"
        private const val SPACE_FACEBOOK = "face"
        private const val SPACE_DIRECT = "direct"
        private const val SPACE_MESSENGER = "messenger"
        private const val SPACE_LINK = "link"
        private const val SPACE_OTHER = "other"
        private const val SPACE_SOURCECODE = "code"

        const val BUNDLE_EXTRA_KEY_VIDEO_URI = "videoUri"
        const val BUNDLE_EXTRA_KEY_VIDEO_TITLE = "title"

        // (https://github.com/ant-media/LibRtmp-Client-for-Android/blob/master/rtmp-client/src/main/java/net/butterflytv/rtmp_client/RtmpClient.java)
        private const val LIBRTMP_ERROR_CODE_OPEN_ALLOC = -1
        private const val LIBRTMP_ERROR_CODE_OPEN_SETUP_URL = -2
        private const val LIBRTMP_ERROR_CODE_OPEN_CONNECT = -3
        private const val LIBRTMP_ERROR_CODE_OPEN_STREAM = -4

        const val SEND_CHAT = "SEND_CHAT_RECEIVER"

        private const val ACTIVITY_REQUEST_CODE_LOGIN = 0xAB02

        private const val MESSAGE_SHOW_COOKIE = 2000
        private const val MESSAGE_HIDE_COOKIE = 2001
        private const val MESSAGE_CHANGE_PRODUCT_IMAGE = 2002
        private const val MESSAGE_SHOW_PRODUCT_POPUP = 2003
        private const val MESSAGE_HIDE_PRODUCT_POPUP = 2004
        private const val MESSAGE_HIDE_USER_ACTION_POPUP = 2005
        private const val MESSAGE_SHOW_USER_ACTION_POPUP = 2006
        private const val MESSAGE_VIEW_COUNT_ADD = 2007
        private const val MESSAGE_SHOW_HEART_VIEW = 2008 // heart view 전송
        private const val MESSAGE_HIDE_USER_JOIN_POPUP = 2009
        private const val MESSAGE_SHOW_USER_JOIN_POPUP = 2010

        private const val MESSAGE_SHOW_USER_ACTION_POPUP_CALL_TIME = 10000L * 6   // 1분

        private const val PAGE_DATA_COUNT = 100
    }

    private lateinit var gestureDetector: GestureDetector
    private var player: SimpleExoPlayer? = null

    private var videoUrl: String? = null
    private lateinit var videoTitle: String
    private var mVideoWidth: Int = 0
    private var mVideoHeight: Int = 0

    private var isPaused = false
    private var isStarted = false

    private lateinit var keyGuardManager: KeyguardManager

    private var chatManager: ChatManager? = null
    private var chatMyId: ChatUser? = null
    //private var chatUserIds: MutableList<String> = ArrayList()
    private var mUserMap: HashMap<String, String> = HashMap()  // id, nickName
    private var userListDialog: LiveUserListDialog? = null
    private var cookieSendDialog: CookieSendDialog? = null
    private var productListDialog: PlayerProductDialog? = null
    private var nickNameDialog: NickNameModifyDialog? = null

    private var mData: VOD.DataBeanX? = null

    private lateinit var chatLayoutManager: androidx.recyclerview.widget.LinearLayoutManager
    private var chatAdapter: ChatAdapter? = null
    private var duplicateNickAddHeader = 1

    // Injection Break : 사용자가 채팅 내용을 입력하여 최종적으로 전송한 시간
    private var chatLatestSendTime = System.currentTimeMillis()

    private var mUIHandler = Handler(Looper.getMainLooper())

    private var liveBroadcastFinished = false                           // 방송이 종료된 상태여부
    private var mPosition: Int = 0

    private var likeStatus: String = "N"
    private var scrapStatus: String = "N"

    private var mKeyboardHeight = 0

    private var mIsVisibleToUser = false

    private var mChatCommand = ChatManager.NORMAL_CHAT_CMD
    private var mNickName: String = ""

    private var mCookieQueue: Queue<CookieData> = LinkedList()

    private var mIsInit = false
    private var isInPictureInPictureMode = false
    private var cookieCnt = 0
    private var streamKey = ""
    private var uploader = ""
    private var viewCnt = ""
    private var favoriteCnt = ""
    private var scrapCnt = ""
    private var thumbImage = ""
    private var shareTitle = ""
    private var vodType = ""
    private var shareLink = ""
    private var linkGubun = ""
    private var mChatConnected = false

    private var prodImage = ""
    private var mProdImageIndex = 0

    private var mStartedMoveUpAnimation = false
    private var mStartedMoveDownAnimation = false
    private var mIsFollowing = false

    private lateinit var mTrackSelector: DefaultTrackSelector
    private lateinit var mAdapter: CookieSenderAdapter
    private var mTimer: Timer? = null
    private var mRefreshTimer:Timer? = null
    private var mHitDelayTime: Long = 0L

    private var mHandler: MyHandler? = null

    private var mShownProductPopup = false // product popup 은 한번만 띄운다.
    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private var chatIP = ""
    private var chatPort = -1
    private var playerFlag = -1
    private var myVODPosition = -1

    private var mLiveUserJoinData: LiveUserJoinData = LiveUserJoinData()
    private var mLiveUserActionData: LiveUserActionData = LiveUserActionData()
    private var enterUserId = arrayListOf<String>()
    private var enterUserNick = arrayListOf<String>()
    private var isRoomJoinSolo = true
    private var enterRoomCount = 0
    private var shareKey = ""
    private var sheetDialog: BottomSheetDialog? = null
    private var isScrapClicked = false
    private var remainCookie = ""
    private var chatNickName = ""
    private var iFavoriteCount = 0
    private var mOnAirDialog:AppConfirmDialog? = null

    // Handler 메모리 릭 발생하니깐 항상 아래 처럼 만들자..
    private class MyHandler(fragment: ShoppingLiveFragment) : Handler() {
        private val mReference: WeakReference<ShoppingLiveFragment> = WeakReference(fragment)

        override fun handleMessage(msg: Message) {
            Logger.e("handleMessage: ${msg.what}")

            when (msg.what) {
                //MESSAGE_HIDE_UI -> mReference.get()?.hideFullLayout()
                MESSAGE_SHOW_COOKIE -> mReference.get()?.receivedCookie()
                MESSAGE_HIDE_COOKIE -> mReference.get()?.hideCookie()
                MESSAGE_CHANGE_PRODUCT_IMAGE -> mReference.get()?.changeProductImage()
                MESSAGE_SHOW_PRODUCT_POPUP -> mReference.get()?.showProductPopup()
                MESSAGE_HIDE_PRODUCT_POPUP -> mReference.get()?.hideProductPopup()
                MESSAGE_HIDE_USER_ACTION_POPUP -> mReference.get()?.hideUserActionPopup()
                MESSAGE_SHOW_USER_ACTION_POPUP -> mReference.get()?.showUserActionPopup()
                MESSAGE_VIEW_COUNT_ADD -> mReference.get()?.viewCountADD()
                MESSAGE_SHOW_HEART_VIEW -> mReference.get()?.drawHeartView()
                MESSAGE_HIDE_USER_JOIN_POPUP -> mReference.get()?.hideUserJoinPopup()
                MESSAGE_SHOW_USER_JOIN_POPUP -> mReference.get()?.showUserJoinPopup()
            }
        }
    }

    private val mTask = object : TimerTask() {
        override fun run() {
            mHandler?.sendEmptyMessage(MESSAGE_SHOW_USER_JOIN_POPUP)
        }
    }

    private val mRefreshTask = object : TimerTask() {
        override fun run() {
            getLiveInfo()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_shopping_live, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPosition = arguments!!.getInt("position")
        playerFlag = arguments!!.getInt("player_flag")
        myVODPosition = arguments!!.getInt("my_vod_position")
        shareKey = arguments!!.getString("share_key") ?: ""
        Logger.d("onCreate() position:$mPosition myVODPosition:$myVODPosition playerFlag:$playerFlag shareKey:$shareKey")

        if (playerFlag > 0) {
            handleFromOtherPlayerExt()
        } else {
            mData = (activity as ShoppingPlayerActivity).mVideoData[mPosition]
        }

        val task = ShopTreeAsyncTask(context!!)
        task.getLiveInfo(mData?.id, { result, obj ->
            try {
                val response = Gson().fromJson(obj.toString(), API98::class.java)
                if(response.data.size > 0) {
                    val json = Gson().toJson(response.data[0])  // 특정 라이브 정보 data 리스트 개수는 하나뿐임.
                    mData = Gson().fromJson(json, VOD.DataBeanX::class.java)

                    if(mData != null) {
                        refreshLiveData(mData!!)
                    }
                }
            } catch (e : Exception) {
                Logger.p(e)
            }
        })

        mHitDelayTime = AppPreferences.getHitTime(context!!) * TIME_UNIT

        videoUrl = mData?.stream
        videoTitle = mData?.title ?: when {
            videoUrl == null -> ""
            videoUrl != null -> {
                var splitArg: List<String>? = videoUrl!!.split("/")
                splitArg?.get(splitArg.size - 1) ?: ""
            }
            else -> arguments!!.getString(BUNDLE_EXTRA_KEY_VIDEO_TITLE)
        }

        EventBus.getDefault().register(this)

        registerReceiver()

        // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
        keyboardHeightProvider = KeyboardHeightProvider(activity!!)
        Handler().postDelayed({ keyboardHeightProvider!!.start() }, 1000)

        // 로그인 사용자 정보 가져온다.
        EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(context!!)!!))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d("onViewCreated() $mPosition")

        if (mHandler == null) {
            mHandler = MyHandler(this@ShoppingLiveFragment)
        }

        init()

        // 라이브는 현재 보여지는 화면만 init 을 해야 속도에 문제가 안생긴다.. (매우 중요!!)
        if (isVisibleFragment()) {
            initPlayer()

            EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, mData?.userId))
        }

        //mHandler?.sendEmptyMessageDelayed(MESSAGE_HIDE_UI, HIDE_UI_TIME_OUT * 2)

        mChatCommand = ChatManager.NORMAL_CHAT_CMD
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume() $mPosition")

        // 재생
        mHandler?.postDelayed({
            playVideoByMe()
            player?.seekToDefaultPosition() //라이브 현재 위치로 이동
        }, 200)

        startAnimation()
    }

    override fun onPause() {
        super.onPause()
        Logger.d("onPause() $mPosition")

        if (!isInPictureInPictureMode) {
            pauseVideo()
        }
        stopAnimation()
    }

    override fun onDestroyView() {
        AppToast.cancelAllToast()
        super.onDestroyView()
        Logger.d("onDestroyView()  $mPosition")
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.e("onDestroy()  $mPosition")

        mHandler?.removeCallbacksAndMessages(null)
        mTimer?.cancel()
        mRefreshTimer?.cancel()

        SoftKeyboardUtils.hideKeyboard(editTextChat)

        EventBus.getDefault().unregister(this)

        if (keyboardHeightProvider != null) {
            keyboardHeightProvider!!.close()
        }

        unRegisterScreenReceiver()
        unregisterChatBR()
        unregisterReceiver()

        chatManager?.disconnect()

        releaseVideo()

        //saveData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var timePosition = player?.currentPosition ?: C.TIME_UNSET
        Logger.d("savedTimePosition : $timePosition")

        outState.putString(BUNDLE_EXTRA_KEY_VIDEO_URI, videoUrl)
//        outState.putLong(BUNDLE_EXTRA_KEY_VIDEO_POSITION, player?.currentPosition ?: C.TIME_UNSET)
        super.onSaveInstanceState(outState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        registerScreenReceiver()
        registerChatBR(context)
    }

    override fun onDetach() {
        super.onDetach()
        Logger.d("onDetach $mPosition")

//        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_REQUEST_CODE_LOGIN && resultCode == Activity.RESULT_OK) {
            likeButtonLayer.setOnClickListener(likeClickListener)

            mChatConnected = false
            chatManager?.disconnect()

            JSONObject().apply {
                put("streamKey", mData?.id)
                put("user", AppPreferences.getUserId(context!!)!!)
            }.let {
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
                postChatConnect(body)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: VideoMuteBus) {
        initVolumeButton()

        controlVolume()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus:ZzimStatusBus) {
        if (!mIsVisibleToUser) {
            return
        }

        Logger.e("############## status:${bus.status}")
        for(i in 0 until mData?.relationPrd!!.data.size) {
            val item = mData?.relationPrd!!.data[i]
            if(bus.productId == item.idx) {
                mData?.relationPrd!!.data[i].is_wish = bus.status

                if(bus.count.isNotEmpty()) {
                    mData?.relationPrd!!.data[i].wish_cnt = bus.count
                } else {
                    val count = mData?.relationPrd!!.data[i].wish_cnt.toInt()
                    if("Y" == bus.status) {
                        mData?.relationPrd!!.data[i].wish_cnt = count.inc().toString()
                        Logger.e("############## zzimCount:${count.inc()}")
                    } else {
                        mData?.relationPrd!!.data[i].wish_cnt = count.dec().toString()
                        Logger.e("############## zzimCount:${count.dec()}")
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusFastResponse) {
        if (!mIsVisibleToUser) {
            return
        }

        val api24Key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API24.toString(), AppPreferences.getUserId(context!!)!!, "")
        val api82 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API82.toString(), mData?.id!!, "")

        if (data.arg1.startsWith(NetworkApi.API26.toString()) || data.arg1.startsWith(api24Key)) {
//            handleNetworkResultScrap(data)
        } else if (data.arg1 == api82) {
            handleNetworkResultCookieSenderList(data)
        } else if (data.arg1 == NetworkApi.API142.toString()) {
            if ("ok" == data.arg2) {
                val response: API142 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API142::class.java)
                mHandler?.post { connectChat(response) }
            }
        } else if (data.arg1.startsWith(NetworkApi.API84.toString())) {
            handleNetworkResultCookieSend(data)
        } else if (data.arg1.startsWith(NetworkApi.API2.toString())) {
            handleFollowInfo(data)
        } else if (data.arg1.startsWith(NetworkApi.API8.toString())) {
            handleNetworkFavorResult(data)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if (!mIsVisibleToUser) {
            return
        }

        var api21Key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API21.toString(), mData?.userId!!, "")

        if (data.arg1 == api21Key) {
            handleNetworkUserInfo(data)
        } else if (data.arg1.startsWith(NetworkApi.API9.toString())) {
            handleNetworkResultViewCount(data)
        } else if (data.arg1.startsWith(NetworkApi.API10.toString())) {
            handleNetworkResultViewCountADD(data)
        } else if (data.arg1.startsWith(NetworkApi.API48.toString())) {
            handleVideoShare(data)
        } else if(data.arg1 == "GET/user/${AppPreferences.getUserId(context!!)!!}") {
            if("ok" == data.arg2) {
                val response: API21 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API21::class.java)

                remainCookie = response.userCookie
                ImageLoad.setImage(context!!, imageViewProfile, response.userIMG, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
            }
        }
    }

    private fun handleLiveInfo(data: NetworkBusFastResponse) {
        if ("ok" == data.arg2) {
            val response = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API98::class.java)
            val json = Gson().toJson(response.data[0])  // 특정 라이브 정보 data 리스트 개수는 하나뿐임.
            mData = Gson().fromJson(json, VOD.DataBeanX::class.java)

            Logger.e("handleLiveInfo: ${mData?.videoType}")

            if (TextUtils.isEmpty(mData?.strNoti)) {
                textViewNotification.visibility = View.GONE
            } else {
                textViewNotification.text = mData?.strNoti
                textViewNotification.isSelected = true
            }

            textViewNickName.text = mData?.userNick

            if(mData?.shareCount!!.toInt() == 0) {
                shareCount.visibility = View.GONE
            } else {
                shareCount.visibility = View.VISIBLE
                shareCount.text = "${mData?.shareCount}"
            }

            if(mData?.scrapCount!!.toInt() == 0) {
                scrapCount.visibility = View.GONE
            } else {
                scrapCount.visibility = View.VISIBLE
                scrapCount.text = mData?.scrapCount
            }
        } else {
            Logger.e("${data.arg3} ${data.arg4}")
        }
    }

    private fun handleVideoShare(data: NetworkBusResponse) {
        if (data.arg2 == "ok") {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            if (str.isNotEmpty()) {
                shareLink = Gson().fromJson(str, API48::class.java).url
                Logger.e("shareLink :: $shareLink")
                if (shareLink.isNotEmpty()) {
                    when {
                        SPACE_KAKAO == linkGubun -> shareKakao()
                        SPACE_FACEBOOK == linkGubun -> shareFacebook()
                        SPACE_MESSENGER == linkGubun -> shareFacebookMessenger()
                        SPACE_LINK == linkGubun -> setClipBoardLink()
                        SPACE_OTHER == linkGubun -> shareEtc()
                        SPACE_SOURCECODE == linkGubun -> setSourceCodeLink()
                    }
                }

                getLiveInfo()
            }
        }
    }

    private fun handleFollowInfo(data: NetworkBusFastResponse) {
        if (mIsVisibleToUser) {
            Logger.e("handleFollowInfo")

            if (data.arg2 == "ok") {
                if (mIsFollowing) {
                    imageViewFollow?.setImageResource(R.drawable.vod_follow_ico)
                } else {
                    imageViewFollow?.setImageResource(R.drawable.vod_following_ico)
                }
                mIsFollowing = !mIsFollowing
            } else {
                Logger.e("error : ${data.arg3} ${data.arg4}")
            }
        }
    }

    private fun handleNetworkUserInfo(data: NetworkBusResponse) {
        Logger.e("handleNetworkUserInfo")

        if ("ok" == data.arg2) {
            imageViewFollow?.setOnClickListener(clickListener)
            //userInfoLayout?.setOnClickListener(clickListener)

            var str = DBManager.getInstance(context!!).get(data.arg1)
            var response: API21 = Gson().fromJson(str, API21::class.java)
            if (response.followingCheck) {
                mIsFollowing = true
                imageViewFollow?.setImageResource(R.drawable.vod_following_ico)
            } else {
                mIsFollowing = false
                imageViewFollow?.setImageResource(R.drawable.vod_follow_ico)
            }
        } else {
            Logger.e("fail: ${data.arg2}, ${data.arg3}, ${data.arg4}")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
        if (mIsVisibleToUser) {
            Logger.e("SoftKeyboardBus height: ${bus.height}")
            mKeyboardHeight = bus.height

            if (bus.height > 100) {
                showChatMode()
            } else if (bus.height <= 0) {
                hideChatMode()
            } else {

            }

            if (cookieSendDialog != null && cookieSendDialog!!.isShowing) {
                if (bus.height > 100) {
                    cookieSendDialog!!.isKeyboardShow(true)
                } else {
                    cookieSendDialog!!.isKeyboardShow(false)
                }
            }
        }
    }

//    @Subscribe
//    fun onMessageEvent(bus: String) {
//        Logger.e("onMessageEvent: $bus")

//        if (isVisibleFragment()) {
//            when (bus) {
//                "onProductCartClicked" -> {
//                    EventBus.getDefault().post(VideoPipBus(videoUrl, 0, mData?.strContentSize))
//
//                    // refresh 가 필요한가?
//                    mIsInit = false
//                    releaseVideo()
//                }
//            }
//        }
//    }

    private fun getVodType(type: String): String {
        if ("VOD" == type)
            return "vod"
        else
            return "live"
    }

    private fun share(space: String) {
        var isLogin = AppPreferences.getLoginStatus(context!!)
        if (!isLogin) {
            val dialog = AppAlertDialog(context!!)
            dialog.setTitle("댓글 입력")
            dialog.setMessage("영상 댓글 입력은 로그인 사용자만 가능합니다.")
            dialog.setLeftButton("닫기", View.OnClickListener {
                dialog.dismiss()
            })
            dialog.setRightButton("로그인", View.OnClickListener {
                context!!.startActivity(Intent(context, LoginActivity::class.java))
                dialog.dismiss()
            })

            dialog.show()
        } else {
            makeShareLink(space)
        }
    }

    private fun makeShareLink(space: String) {
        linkGubun = space
        Logger.e("linkGubun :: $linkGubun")
        Logger.e("streamKey :: $streamKey")
        Logger.e("uploader :: $uploader")
        Logger.e("space :: $space")
        Logger.e("vodType :: $vodType")

        var type = ""
        if (vodType.toLowerCase() == "lastlive")
            type = "live"
        else {
            type = vodType
        }

//        var builder = MultipartBody.Builder()
//        builder.setType(MultipartBody.FORM)
//        builder.addFormDataPart("streamKey", streamKey)
//        builder.addFormDataPart("uploader", uploader)
//        builder.addFormDataPart("shareId", AppPreferences.getUserId(context!!))
//        builder.addFormDataPart("space", space)
//        builder.addFormDataPart("vodType", type)
//
//        EventBus.getDefault().post(NetworkBus(NetworkApi.API48.name, builder.build()))

        var task = ShopTreeAsyncTask(context)
        task.makeShareLink(streamKey, uploader, AppPreferences.getUserId(context!!), space, type, object: ShopTreeAsyncTask.OnDefaultObjectCallbackListener{
            override fun onResponse(result: Boolean, obj: Any?) {
                Logger.e("result :: " + result)
                if ( result ) {
                    var objet = obj as JSONObject
                    if ( "success".equals(obj.optString("result").toLowerCase())) {
                        shareLink = obj.optString("url")

                        Logger.e("shareLink :: $shareLink")
                        if (shareLink.isNotEmpty()) {
                            when {
                                SPACE_KAKAO == linkGubun -> shareKakao()
                                SPACE_FACEBOOK == linkGubun -> shareFacebook()
                                SPACE_MESSENGER == linkGubun -> shareFacebookMessenger()
                                SPACE_LINK == linkGubun -> setClipBoardLink()
                                SPACE_OTHER == linkGubun -> shareEtc()
                                SPACE_SOURCECODE == linkGubun -> setSourceCodeLink()
                            }
                        }

                        getLiveInfo()
                    }
                }
            }
        })
    }

    private fun setClipBoardLink() {
        val clipboardManager = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clipData = ClipData.newPlainText("label", shareLink)
        clipboardManager!!.primaryClip = clipData

//        Toast.makeText(context!!, resources.getString(R.string.clipboard_msg), Toast.LENGTH_SHORT).show()
        AppToast(context!!).showToastMessage(resources.getString(R.string.clipboard_msg),
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM)
    }

    private fun setSourceCodeLink() {
        val clipboardManager = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clipData = ClipData.newPlainText("label", shareLink)
        clipboardManager!!.primaryClip = clipData

//        Toast.makeText(context!!, "공유 소스 코드가 복사 되었습니다.", Toast.LENGTH_SHORT).show()

        AppToast(context!!).showToastMessage("공유 소스 코드가 복사 되었습니다.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM)
    }

    private fun shareInstagram() {
    }

    private fun shareEtc() {
        val i = Intent(android.content.Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(android.content.Intent.EXTRA_TEXT, shareLink)
        startActivity(Intent.createChooser(i, ""))
    }

    private fun shareFacebookMessenger() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareTitle + " " + shareLink)

        var facebookAppFound = false
        val matches = context!!.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (info in matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.orca")) {
                intent.setPackage(info.activityInfo.packageName)
                facebookAppFound = true
                break
            }
        }

        if (facebookAppFound) {
            Logger.e("facebook messanger app exist")
            startActivity(intent)
            return
        } else {
            Logger.e("facebook messanger app not exist")
            val messageDialog = MessageDialog(this)
            val callbackManager = CallbackManager.Factory.create()

            messageDialog.registerCallback(callbackManager, object : FacebookCallback<Sharer.Result> {
                override fun onSuccess(result: Sharer.Result) {
//                    Toast.makeText(context, context!!.getString(R.string.facebook_message_success), Toast.LENGTH_SHORT).show()
                    AppToast(context!!).showToastMessage(context!!.getString(R.string.facebook_message_success),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }

                override fun onCancel() {
//                    Toast.makeText(context, context!!.getString(R.string.facebook_message_cancel), Toast.LENGTH_SHORT).show()
                    AppToast(context!!).showToastMessage(context!!.getString(R.string.facebook_message_cancel),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }

                override fun onError(error: FacebookException) {
                    error.printStackTrace()
//                    Toast.makeText(context, context!!.getString(R.string.facebook_message_fail), Toast.LENGTH_SHORT).show()
                    AppToast(context!!).showToastMessage(context!!.getString(R.string.facebook_message_fail),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            })

            if (MessageDialog.canShow(ShareLinkContent::class.java)) {
                Logger.d("facebook can show message dialog")
                val linkContent = ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(shareLink))
                        .setQuote("${context!!.getString(R.string.app_name)} \n $shareLink").build()

                messageDialog.show(linkContent)
            } else {
                Logger.d("facebook can not show message dialog")
            }
        }
    }

    private fun shareFacebook() {
        val shareDialog = ShareDialog(this)
        val callbackManager = CallbackManager.Factory.create()

        shareDialog.registerCallback(callbackManager, object : FacebookCallback<Sharer.Result> {
            override fun onSuccess(result: Sharer.Result) {
//                Toast.makeText(context, context!!.getString(R.string.facebook_success), Toast.LENGTH_SHORT).show()
                AppToast(context!!).showToastMessage(context!!.getString(R.string.facebook_success),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }

            override fun onCancel() {
//                Toast.makeText(context, context!!.getString(R.string.facebook_cancel), Toast.LENGTH_SHORT).show()
                AppToast(context!!).showToastMessage(context!!.getString(R.string.facebook_cancel),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }

            override fun onError(error: FacebookException) {
                error.printStackTrace()
//                Toast.makeText(context, context!!.getString(R.string.facebook_fail), Toast.LENGTH_SHORT).show()
                AppToast(context!!).showToastMessage(context!!.getString(R.string.facebook_fail),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        })

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareTitle + " " + shareLink)

        var facebookAppFound = false
        val matches = context?.packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (info in matches!!) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana") || info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.lite")) {
                intent.setPackage(info.activityInfo.packageName)
                facebookAppFound = true
                break
            }
        }

        if (facebookAppFound) {
            startActivity(intent)
            return
        } else {
            if (ShareDialog.canShow(ShareLinkContent::class.java)) {
                Logger.d("facebook can show dialog")
                val linkContent = ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(shareLink))
                        .setQuote("${context!!.getString(R.string.app_name)} \n $shareLink").build()
                shareDialog.show(linkContent)
            } else {
                Logger.d("facebook can not show dialog")
            }
        }
    }

    private fun shareKakao() {
        Logger.e("shareKakao")
        //var scrapCount = 0
        var viewCount = 0
        var likeCount = 0

//        try {
//            scrapCount = scrapCnt.toInt()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

        try {
            viewCount = viewCnt.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            likeCount = favoriteCnt.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var feedTemplate = FeedTemplate
                .newBuilder(ContentObject.newBuilder(shareTitle, thumbImage,
                        LinkObject.newBuilder().setWebUrl(shareLink).setMobileWebUrl(shareLink).build())
                        .setDescrption("")
                        .build())

                .setSocial(SocialObject.newBuilder().setLikeCount(likeCount).setViewCount(viewCount).build())
                .addButton(ButtonObject("보러가기", LinkObject.newBuilder().setWebUrl(shareLink).setMobileWebUrl(shareLink).build())).build()

        KakaoLinkService.getInstance().sendDefault(context, feedTemplate, object : ResponseCallback<KakaoLinkResponse>() {
            override fun onFailure(errorResult: ErrorResult) {
                //Logger.d("onFailure error:" + errorResult.toString())
                if (errorResult.errorCode == -777) {
                    AppToast(context!!).showToastMessage("카카오톡이 설치되어 있지 않거나 최신버전으로 업데이트가 필요합니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            }

            override fun onSuccess(result: KakaoLinkResponse) {
                Logger.d("onSuccess argument :: " + result.argumentMsg.toString())
                Logger.d("onSuccess template :: " + result.templateMsg.toString())
                Logger.d("onSuccess warning :: " + result.warningMsg.toString())
            }
        })
    }

    /**
     * 좋아요 등록/해제 Response 처리
     */
    private fun handleNetworkFavorResult(data: NetworkBusFastResponse) {
        if ("fail" == data.arg2) {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(view!!.context).showToastMessage("좋아요 등록이 실패하였습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        } else {
            var count = mData?.favoriteCount!!.toInt()
            if ("Y" == likeStatus) {
                count++
            }
            mData?.favoriteCount = count.toString()
            mData?.isFavorite = likeStatus
            buttonLike?.text = StringUtils.getSnsStyleCountZeroBase(mData?.favoriteCount!!.toInt())

            getLiveInfo()
        }
    }

    /**
     * 조회수 등록 Response 처리
     */
    private fun handleNetworkResultViewCountADD(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val data: API10 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API10::class.java)
            if (data.status) {
                Logger.d("handleNetworkResultViewCountADD ${data.nTotalCount}")
                //viewCount.text = StringUtils.getSnsStyleCount(data.nTotalCount)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
            AppToast(view!!.context).showToastMessage("조회수 등록이 실패하였습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    /**
     * 조회수 요청 Response 처리
     */
    private fun handleNetworkResultViewCount(data: NetworkBusResponse) {
        if ("fail" == data.arg2) {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    /**
     * 스크랩 등록 Response 처리
     */
    private fun handleNetworkResultScrap(data: NetworkBusFastResponse) {
        if ("ok" == data.arg2) {
            var countText = mData?.scrapCount
            if (!countText.isNullOrEmpty()) {
                var count = countText.toInt()
                if ("Y" == scrapStatus) {
                    buttonScrap.isSelected = true
                    count++

                    var view: View = LayoutInflater.from(context).inflate(R.layout.popup_scrap_on, null)
                    AppToast(context!!).showToastAtView(view, AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_MIDDLE)
                } else {
                    buttonScrap.isSelected = false
                    count--

                    var view: View = LayoutInflater.from(context).inflate(R.layout.popup_scrap_off, null)
                    AppToast(context!!).showToastAtView(view, AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_MIDDLE)
                }
                mData?.scrapCount = count.toString()
                mData?.isScrap = scrapStatus

                if(count == 0) {
                    scrapCount.visibility = View.GONE
                } else {
                    scrapCount.visibility = View.VISIBLE
                    scrapCount.text = count.toString()
                }
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
//        Handler().postDelayed({
//            isScrapClicked = false
//        }, DELAY)

    }

    private fun changeProductImage() {
        try {
            var itemCount = mData?.relationPrd?.data?.size!!
            if (itemCount < 2) {
                return
            }

            mProdImageIndex++
            Logger.d("changeProductImage index:$mProdImageIndex  count:$itemCount")
            if (mProdImageIndex == itemCount) {
                mProdImageIndex = 0
            }

            prodImage = mData?.relationPrd?.data?.get(mProdImageIndex)!!.strPrdImg
            if (prodImage.isNotEmpty()) {
                ImageLoad.setImage(context, buttonProducts, prodImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
            }

            mHandler?.removeMessages(MESSAGE_CHANGE_PRODUCT_IMAGE)
            mHandler?.sendEmptyMessageDelayed(MESSAGE_CHANGE_PRODUCT_IMAGE, 10000)
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

    /**
     * 푸딩 선물하기 Response
     */
    private fun handleNetworkResultCookieSend(data: NetworkBusFastResponse) {
        if ("ok" == data.arg2) {
            try {
                chatManager?.sendCookieSignal(ChatManager.COOKIE_ETC_CHAT_CMD, "", cookieCnt, mNickName)
            } catch (e: Exception) {
                Logger.p(e)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    /**
     * 푸딩 보낸 사용자 목록 Response
     */
    private fun handleNetworkResultCookieSenderList(data: NetworkBusFastResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            if (!TextUtils.isEmpty(str) && cookie_sender != null) {
                cookie_sender.removeAllViews()

                val response: API82 = Gson().fromJson(str, API82::class.java)
                response.data.forEach {
                    cookie_sender.addView(LiveCookieSender(context, it.mb_nick, it.mb_user_img))
                }
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    /**
     * 비 로그인 사용자용 AccountID
     * guset 로그인 이라도 ID 가 바뀌지 않아야 한다.
     */
    private fun getAccountID(): String {
        var androidId = android.provider.Settings.Secure.getString(context!!.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        Logger.h("getAccountID: $androidId")
        return androidId
    }

    /**
     * 채팅 텍스트를 전송
     */
    private fun sendText(targetUser: String?) {
        var currentTime = System.currentTimeMillis()
        if (currentTime - chatLatestSendTime >= ChatManager.CHAT_SEND_INTERVAL) {
            var color = ""
            var sendText: String? = ""
            if (!TextUtils.isEmpty(editTextChat.text?.toString()))
                sendText = editTextChat.text.toString()
            if (!TextUtils.isEmpty(sendText)) {                // 텍스트가 있을 경우에만 처리내용을 동작
                sendText = filterReturnKeyInject(sendText)
                if (!TextUtils.isEmpty(sendText)) {            // ReturnKey Filtering 이후 텍스트 여부 재확인
                    // 채팅 내용 전송 및 UI 업데이트
                    if (!TextUtils.isEmpty(sendText!!.replace("\n", "", true).trim())) {
                        // 위의 조건을 모두 만족하였으면 채팅 내용을 전송
                        chatManager?.sendTextChat(mChatCommand, color, sendText, if (TextUtils.isEmpty(targetUser)) "*" else targetUser, chatNickName)
                        editTextChat.setText("")
                        chatLatestSendTime = currentTime
                    }
                }
            }
        }
    }

    /**
     * 채팅창에 엔터키를 연속으로 입력하는 도배방지를 위한 ReturnKey 필터링
     * @param sendText      사용자가 입력한 채팅글 내용
     * @return
     */
    private fun filterReturnKeyInject(sendText: String?): String? {
        var position = 0
        var length = sendText?.length ?: 0
        var result = sendText

        while (sendText!!.isNotEmpty() && position < length) {
            if (result!![position] == '\n' && position == 0) {
                result = sendText.removeRange(IntRange(position, position))
                --length
            } else if (result[position] == '\n' && position + 1 < length && sendText[position + 1] == '\n') {
                result = result.removeRange(IntRange(position, position))
                --length
                if (position + 2 < length) {
                    position += 2
                }
            } else if (result[position] == '\n' && position == length - 1) {
                result = result.removeRange(IntRange(position, position))
                break
            } else {
                ++position
            }
        }

        return result
    }

    /**
     * 현재 채팅방 내 방장 아이디를 획득
     */
//    private fun findRoomMakerUserId(): String? {
//        var roomMakerId = chatUserIds.filter { it != "*" }.filter { JSONObject(it)?.getString("an") == "1" }
//
//        return if (roomMakerId.isNotEmpty()) {
//            roomMakerId[0]
//        } else {
//            null
//        }
//    }

    // ShoppingPlayerActivity 에서 호출 된다.  finish 가 필요하지 않으면 true return
    fun onBackPressed(): Boolean {
//        if (mIsVisibleReplyDialog) {
//            hideReplyDialog()
//            return true
//        }

        return false
    }

    private fun showChatMode() {
        Logger.i("showChatMode()")

        chatLayer.visibility = View.GONE
        chatInputLayer.visibility = View.VISIBLE

        chatGapView?.visibility = View.VISIBLE
        chatGapView?.layoutParams?.height = mKeyboardHeight
        chatGapView?.invalidate()
        chatGapView?.requestLayout()
        //chatGapView.forceLayout()

        editTextChat.requestFocus()
    }

    private fun hideChatMode() {
        Logger.i("hideChatMode()")

        chatLayer.visibility = View.VISIBLE
        chatInputLayer.visibility = View.GONE
        chatGapView.visibility = View.GONE

        editTextChat.clearFocus()

        //문의 버튼 초기화
        mChatCommand = ChatManager.NORMAL_CHAT_CMD
        SoftKeyboardUtils.hideKeyboard(editTextChat)
    }

    /**
     * 채팅 입력창 SoftKeyboardBus Action 이벤트 처리
     */
    private val chatActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
        Logger.e("onEditorAction? $actionId")

        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendText("*")
        }
        false
    }

    private val inputChangeListener = object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(s?.length == 0) {
                buttonSendMsg.isEnabled = false
                buttonSendMsg.setBackgroundResource(R.drawable.send_off_ico)
            } else {
                buttonSendMsg.isEnabled = true
                buttonSendMsg.setBackgroundResource(R.drawable.send_on_ico)
            }
        }
    }

    private fun init() {
        initVolumeButton()

        keyGuardManager = activity!!.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        gestureDetector = GestureDetector(context, videoGestureListener)

        layoutMore.setOnClickListener(clickListener)
        clickLiveLayout.setOnClickListener(clickListener)
        //buttonInquiryMessage.setOnClickListener(clickListener)
        fullLayer.setOnTouchListener(videoTouchListener)
        textViewBroadcastReport.setOnClickListener(clickListener)
        textViewNickNameModify.setOnClickListener(clickListener)
        textViewCustomerCenter.setOnClickListener(clickListener)
        Notification_close.setOnClickListener(clickListener)
        //productPopLayoutClick.setOnClickListener(clickListener)

        playerView.addOnAttachStateChangeListener(videoStateChangeListener)

        chatLayoutManager = WrappedLinearLayoutManager(activity as? Context)
        chatLayoutManager.orientation = RecyclerView.VERTICAL

        recyclerViewChat.setHasFixedSize(false)
        chatAdapter = ChatAdapter()
        recyclerViewChat.itemAnimator = null
        recyclerViewChat.layoutManager = chatLayoutManager
        recyclerViewChat.adapter = chatAdapter

        //recyclerViewChat.setOnClickListener(clickListener)

        //textViewTitle.text = mData?.title
        //textViewTitle.isSelected = true

        viewCount?.text = mData?.viewerCount

        if (TextUtils.isEmpty(mData?.strNoti)) {
            textViewNotification.visibility = View.GONE
        } else {
            textViewNotification.text = mData?.strNoti
            textViewNotification.isSelected = true
        }

        //editTextChat.imeOptions = EditorInfo.IME_ACTION_DONE
        editTextChat.setOnEditorActionListener(chatActionListener)
        editTextChat.addTextChangedListener(inputChangeListener)
        chatInput.setOnClickListener(clickListener)
        chatInput.isLongClickable = false
        buttonSendMsg.isEnabled = false

        //buttonChatEmoticon.setOnClickListener(clickListener)
        buttonSendMsg.setOnClickListener(clickListener)
        //buttonChatMode.setOnClickListener(clickListener)
        buttonViewerList.setOnClickListener(clickListener)
        //buttonMute.setOnClickListener(clickListener)

        buttonExit.setOnClickListener(clickListener)
        buttonShare.setOnClickListener(clickListener)
        buttonStar.setOnClickListener(clickListener)
        buttonProducts.setOnClickListener(clickListener)
        buttonMore.setOnClickListener(clickListener)
        buttonScrap.setOnClickListener(clickListener)
        buttonMute.setOnClickListener(clickListener)

        if(AppPreferences.getLoginStatus(context!!)) {
            likeButtonLayer.setOnClickListener(likeClickListener)
        } else {
            likeButtonLayer.setOnClickListener(clickListener)
        }

        memberCount.text = "0"
        cookieCount.text = "0"

//        timer.base = SystemClock.elapsedRealtime()
//        timer.start()
//        timer.format = "LIVE %s"


        // 라이브 방송자 이미지
        if (!TextUtils.isEmpty(mData?.userImage)) {
            ImageLoad.setImage(context, imageViewUserThumbnail, mData?.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
        }

        // 첫번째 상품 이미지
        prodImage = if (mData != null && mData!!.relationPrd.data.size > 0) mData!!.relationPrd.data[mProdImageIndex].strPrdImg else ""
        if (prodImage.isNotEmpty()) {
            ImageLoad.setImage(context, buttonProducts, prodImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
        }

        EventBus.getDefault().post(NetworkBus(NetworkApi.API82.name, mData?.id))
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint:$isVisibleToUser  pos:$mPosition")

        mIsVisibleToUser = isVisibleToUser

        if (isVisibleToUser) {
            if (mData != null) {
                EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, mData!!.userId))
            }

            // 재생
            mHandler?.postDelayed({
                playVideo()

                startAnimation()
            }, 200)
        } else {
            pauseVideo()

            SoftKeyboardUtils.hideKeyboard(editTextChat)
            stopAnimation()
            layoutMore?.visibility = View.GONE
        }
    }

    private fun startAnimation() {
        if (mIsVisibleToUser && context != null) {
            // circle animation
            var ani = AnimationUtils.loadAnimation(context, R.anim.rotate)
            ani.duration = 7000
            buttonProducts.startAnimation(ani)

            mHandler?.sendEmptyMessageDelayed(MESSAGE_CHANGE_PRODUCT_IMAGE, 10000)

            // 상품 팝업을 띄운다.
            if (!mShownProductPopup) {
                mShownProductPopup = true
                mHandler?.sendEmptyMessageDelayed(MESSAGE_SHOW_PRODUCT_POPUP, 5000)
            }

            if(mRefreshTimer == null) {
                mRefreshTimer = Timer().apply {
                    schedule(mRefreshTask, 0, REFRESH_LIVE_INFO)
                }
            }
        }
    }

    private fun stopAnimation() {
        Logger.d("stopAnimation")

        mHandler?.removeMessages(MESSAGE_CHANGE_PRODUCT_IMAGE)

        // circle animation
        if (buttonProducts != null) {
            buttonProducts.clearAnimation()
        }
    }

    private fun postMuteBus() {
        var isMute = AppPreferences.getVideoMute(context!!)
        AppPreferences.setVideoMute(context!!, !isMute)
        EventBus.getDefault().post(VideoMuteBus(isMute))
    }

    private fun initVolumeButton() {
        try {
            if (AppPreferences.getVideoMute(context!!)) {
                (buttonMute as AppCompatImageButton).setImageResource(R.drawable.sound_off_btn)
            } else {
                (buttonMute as AppCompatImageButton).setImageResource(R.drawable.sound_on_btn)
            }
        } catch (e: Exception) {
        }
    }

    private fun controlVolume() {
        if (player != null) {
            if (AppPreferences.getVideoMute(context!!)) {
                AppPreferences.setVideoVolumn(context!!, player?.volume!!)
                player?.volume = 0f
            } else {
                player?.volume = AppPreferences.getVideoVolumn(context!!)
            }
        }
    }

    /**
     * 요청된 DataSource 에 근거하여 StreamingPlayer 를 초기화
     */
    private fun initPlayer() {
        if (playerView != null) {
            mTrackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory())
            player = VideoManager.getExoPlayer(context!!, mTrackSelector)

            playerView.player = player

            var mediaSource = VideoManager.getMediaSource(context!!, videoUrl!!)
            player?.prepare(mediaSource)

            if (AppPreferences.getVideoMute(context!!)) {
                player?.volume = 0f
            }

            player?.addListener(playerEventListener)
            player?.addVideoListener(videoListener)

            playerView.useController = false
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            mIsInit = true
        }
    }

    private val playerEventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Logger.e("onPlayerStateChanged:$playWhenReady  state:$playbackState")

            if (playWhenReady && playbackState == Player.STATE_ENDED) {
                if (isVisibleFragment()) {
                    // 영상 재생 종료 안내 팝업
                    showFinishLiveDialog()
                    //EventBus.getDefault().post(NetworkBus(NetworkApi.API98.name, mData?.id)) // 현재 Live 정보 갱신
                }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            Logger.e("onPlayerError $error")
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            Logger.e("onLoadingChanged $isLoading")
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            Logger.e("onPlaybackParametersChanged $playbackParameters")
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            // TODO 왜 이놈이 가끔 늦게 호출 되는지 알아 보자!!
            Logger.e("onTracksChanged")
        }
    }

    // 현재 보여지는 화면인지 체크 ( 재생 중인 fragment )
    private fun isVisibleFragment(): Boolean = mIsVisibleToUser

    // ShoppingLiveFragment 에서 호출 한 경우
    private fun playVideoByMe() {
        if (mIsVisibleToUser) {
            playVideo()
        }
    }

    /**
     * 비디오 재생
     */
    fun playVideo() {
        Logger.c("playVideo: $mPosition")

        //showFullLayout()

        if (isPaused) {
            isPaused = false
        } else {
            if (!mIsInit) {
                initPlayer()
            }
        }

        player?.playWhenReady = true
        player?.playbackState

        // 비디오 플레이가 된 후에 10초가 지나면 조회수를 증가 시킨다.
        //mHandler?.removeMessages(MESSAGE_VIEW_COUNT_ADD)
        //mHandler?.sendEmptyMessageDelayed(MESSAGE_VIEW_COUNT_ADD, mHitDelayTime)
    }

    /**
     * 비디오 재생을 일시정지
     */
    private fun pauseVideo() {
        Logger.d("pauseVideo: $mPosition")

        if (player?.playbackState ?: Player.STATE_IDLE != Player.STATE_IDLE) {
            player?.playWhenReady = false
            isPaused = true
        }
    }

    /**
     * 비디오 재생을 정지
     */
    private fun releaseVideo() {
        Logger.c("releaseVideo: $mPosition")

        player?.playWhenReady = false
        player?.release()
        player = null
    }

    private fun registerScreenReceiver() {
        activity?.registerReceiver(screenStateReceiver,
                IntentFilter().apply {
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_USER_PRESENT)
                })
    }

    private fun unRegisterScreenReceiver() {
        activity?.unregisterReceiver(screenStateReceiver)
    }

    /**
     * ExoPlayer VideoListener
     */
    private val videoListener: VideoListener = object : VideoListener {
        override fun onVideoSizeChanged(width: Int, height: Int,
                                        unappliedRotationDegrees: Int,
                                        pixelWidthHeightRatio: Float) {
            Logger.i("Video Size Changed : Width $mVideoWidth, Height : $mVideoHeight")
            if (mVideoWidth == 0 && mVideoHeight == 0) {
                mVideoWidth = width
                mVideoHeight = height
            }
        }

        override fun onRenderedFirstFrame() {
            Logger.d("FirstFrame Rendered.")

            progressBar?.visibility = View.GONE
            isStarted = true

            imageViewThumbnail?.visibility = View.GONE

            // rtmp 연결 후에 채팅 서버 연결 한다.!!
//            JSONObject().apply {
//                put("streamKey", mData?.id)
//                put("user", AppPreferences.getUserId(context!!)!!)
//            }.let {
//                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
//                NetworkBus(NetworkApi.API142.name, body).let { EventBus.getDefault().post(it) }
//            }

            // 사람이 많을 경우 채팅 서버에 연결이 늦는 경우 가 있어서 EventBus말고 Retrofit으로 API 호출
            JSONObject().apply {
                put("streamKey", mData?.id)
                put("user", AppPreferences.getUserId(context!!)!!)
            }.let {
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
                postChatConnect(body)
            }
        }
    }

    private fun connectChat(response: API142?) {
        Logger.c("connectChat")
        if (mChatConnected || context == null) {
            return
        }

        var userId = AppPreferences.getUserId(context!!)
        if (!userId.isNullOrEmpty()) {
            val key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API21.toString(), userId!!, "")
            if (!TextUtils.isEmpty(key)) {
                var json = DBManager.getInstance(context!!).get(key)
                if (!TextUtils.isEmpty(json)) {
                    var response = Gson().fromJson(json, API21::class.java)
                    if (response != null) {
                        mNickName = response.userNickname
                    }
                }
            }
        }

        if (userId.isNullOrEmpty()) {
            userId = getAccountID()
        }

        if (mNickName.isEmpty()) {
            if (!userId.isNullOrEmpty()) {
                var nick = userId
                if (nick.length > 6) {
                    nick = nick.substring(0, 5) // 너무 이름이 길어서 5자리까지만 표시 하자
                }
                mNickName = "guest$nick"
            }
        }

        var roomID: String
        if (response != null) {
            roomID = if(response.roomid != null) response.roomid else mData?.strChatKey.toString()
            chatPort = response.port
            chatIP = response.ip
        } else {
            roomID = mData?.strChatKey.toString()
        }

        Logger.e("connectChat: nickName:$mNickName  userId:$userId  roomId:$roomID, chatIP:$chatIP, chatPort:$chatPort")

        chatMyId = ChatUser("123", "5", "0", roomID, userId!!, mNickName, "3", "0", "android${System.currentTimeMillis()}")
        if (chatMyId != null) {
            chatManager = ChatManager().apply {
                setChatListener(this@ShoppingLiveFragment)
                connect(chatMyId!!, chatIP, chatPort)
            }
        }
    }

    /**
     * 스트리밍 미디어 소스 변경 이벤트
     */
    private val mediaSourceEventListener = object : MediaSourceEventListener {

        override fun onLoadStarted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
                                   loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
                                   mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
            Logger.d("onLoadStarted() $mPosition")
        }

        override fun onDownstreamFormatChanged(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
                                               mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
            Logger.d("onDownstreamFormatChanged() $mPosition")
        }

        override fun onUpstreamDiscarded(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
                                         mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
            Logger.d("onUpstreamDiscarded() $mPosition")
        }

        override fun onMediaPeriodCreated(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
            Logger.d("onMediaPeriodCreated() $mPosition")
        }

        override fun onLoadCanceled(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
                                    loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
                                    mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
            Logger.d("onLoadCanceled() $mPosition")
            progressBar?.visibility = View.GONE
        }

        override fun onMediaPeriodReleased(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
            Logger.d("onMediaPeriodReleased() $mPosition")
        }

        override fun onReadingStarted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
            Logger.d("onReadingStarted()$mPosition $mPosition")
            progressBar?.visibility = View.GONE
            isStarted = true
        }

        override fun onLoadCompleted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
                                     loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
                                     mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
            //Logger.d("onLoadCompleted() $mPosition")
        }

        override fun onLoadError(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
                                 loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
                                 mediaLoadData: MediaSourceEventListener.MediaLoadData?,
                                 error: IOException?, wasCanceled: Boolean) {
            Logger.e("onLoadError() - $mPosition  $error")

            if (isVisibleFragment()) {
                AppToast(view!!.context).showToastMessage(R.string.network_connection_error,
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_MIDDLE)
            }

//            try {
//                if (error is net.butterflytv.rtmp_client.RtmpClient.RtmpIOException) {
//                    var errorText: String
//
//                    if (error.errorCode != -1) {
//                        errorText = when (error.errorCode) {
//                            LIBRTMP_ERROR_CODE_OPEN_ALLOC -> getString(R.string.error_rtmp_open_alloc)
//                            LIBRTMP_ERROR_CODE_OPEN_SETUP_URL -> getString(R.string.error_rtmp_open_setup_url)
//                            LIBRTMP_ERROR_CODE_OPEN_CONNECT -> getString(R.string.error_rtmp_open_connect)
//                            LIBRTMP_ERROR_CODE_OPEN_STREAM -> getString(R.string.error_rtmp_open_stream)
//                            else -> ""
//                        }
//
//                        Logger.e("LoadError : $errorText")
//                    }
//                }
//
//                if (isVisibleFragment()) {
//                    AppToast(view!!.context).showToastMessage(R.string.network_connection_error,
//                            AppToast.DURATION_MILLISECONDS_DEFAULT,
//                            AppToast.GRAVITY_MIDDLE)
//                }
//            } catch (e: Exception) {
//                Logger.p(e)
//            }
        }
    }

    /**
     * PlayerView 에서 발생되는 TouchEvent 에 대한 처리
     */
    private val videoGestureListener: GestureDetector.SimpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean { // onDown() return true 꼭 해야 한다.
            //onHeartAdded() // test code
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            Logger.e("onSingleTapConfirmed")
            hideFullLayout()
            return true
        }

        val SWIPE_MIN_DISTANCE = 100
        val SWIPE_THRESHOLD_VELOCITY = 500

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (Math.abs(e1.y - e2.y) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                //Logger.e("up or down: ${Math.abs(e1.y - e2.y)}  ${Math.abs(velocityY)}")
                //return true
            }

            if (e1.x - e2.x > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                // 좌측으로 드래그
                Logger.e("onFling right open")

                val intent = Intent("open_drawer")
                intent.putExtra("isLeftOpen", false)
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            } else if (e2.x - e1.x > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                // 우측으로 드래그
                Logger.e("onFling left open")
                val intent = Intent("open_drawer")
                intent.putExtra("isLeftOpen", true)
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            } else {
                //Logger.e("what? x: ${Math.abs(e1.x - e2.x)}  ${Math.abs(velocityX)}")
                //Logger.e("what? y: ${Math.abs(e1.y - e2.y)}  ${Math.abs(velocityY)}")
            }

            return true
        }
    }

    /**
     * Floating Window 상에서 화면 닫기를 수행하였을 때의 대응
     */
    private val videoStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(v: View?) {
            Logger.d("Floating video detached..")
        }

        override fun onViewAttachedToWindow(v: View?) {
            Logger.d("Floating video attached")
        }
    }

    /**
     * 액정 스크린 상태(켜짐 / 꺼짐 / 잠금해제) 관련 BroadcastReceiver
     */
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Logger.e("screenStateReceiver called")
        }
    }

    private fun showCookieDialog() {
        // UI 테스트 용도 추후 원복예정
        cookieSendDialog = CookieSendDialog(activity!!, AppPreferences.getUserId(activity!!)
                ?: "", "test", remainCookie).apply {
            setListener(cookieSendDialogListener)
            setOnDismissListener {
                cookieSendDialog = null
            }
            show()
        }
    }

    /**
     * 푸딩 전송 팝업 이벤트 처리
     */
    private val cookieSendDialogListener = object : CookieSendDialog.Listener {
        override fun onCookieTransferred(sendId: String, receiveId: String, quantity: Int) {
            Logger.e("onCookieTransferred : $receiveId, $quantity")

            cookieCnt = quantity
            sendCookie(sendId, quantity)
        }

        override fun onNotEnoughCookie(enoughCookie: Int) {
            Logger.e("onNotEnoughCookie : $enoughCookie")

            AppToast(view!!.context).showToastMessage("젤리가 부족 합니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }

        override fun onCookieBuyRequested(userId: String) {
            Logger.e("onCookieBuyRequested : $userId")

            if (AppPreferences.getLoginStatus(context!!)) {
                //startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) test
                var url = "${NetworkConst.COOKIE_PAYMENT_API}${AppPreferences.getUserId(context!!)}"
                startActivity(Intent(context!!, LinkWebViewActivity::class.java).apply {
                    putExtra("LINK", url)
                    putExtra("TITLE", "젤리 구매")
                    putExtra("BUY_MODE", true)
                })
            } else {
                startActivityForResult(Intent(context!!, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
            }
        }

        /**
         * 현재 나의 닉네임을 확인
         */
        fun getMyNickName(jsonStr: String?): String? = if (TextUtils.isEmpty(jsonStr)) {
            null
        } else JSONObject(jsonStr).getString("nk")
    }

    private fun showShareDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_share, null)

        view.buttonKakao.setOnClickListener(shareClickListener)
        view.buttonFacebook.setOnClickListener(shareClickListener)
//        view.buttonInsta.setOnClickListener(shareClickListener)
//        view.buttonMessenger.setOnClickListener(shareClickListener)
        view.buttonSourceCode.setOnClickListener(shareClickListener)
        view.buttonLink.setOnClickListener(shareClickListener)
        view.buttonMore.setOnClickListener(shareClickListener)

        sheetDialog = BottomSheetDialog(view!!.context)
        sheetDialog!!.setContentView(view)
        sheetDialog!!.show()
    }

    private fun showFinishLiveDialog() {
        Logger.c("showFinishLiveDialog")

        if(nickNameDialog != null && nickNameDialog?.isShowing!!) {
            nickNameDialog?.dismiss()
        }

        if (isVisibleFragment()) {
            LiveBroadcastFinishedDialog(view!!.context, mData?.userNick!!, mData?.userImage!!, mData?.userId!!, mIsFollowing, false).apply {
                setOnDismissListener {
                    (activity as? ShoppingPlayerActivity)?.finish()
                }
            }.show()
        }
    }

    private fun showNickNameModifyDialog() {
        nickNameDialog = NickNameModifyDialog(view!!.context, object:NickNameModifyDialog.Listener {
            override fun onConfirm(nickName: String) {
                chatNickName = nickName
            }
        }).apply { show() }
    }

    /**
     * 방송중 상품 팝업을 표시
     */
    private fun showProductDialog() {
        Logger.e("showProductDialog")
        if (mData == null || mData?.relationPrd?.data!!.size == 0 || player == null) {
            return
        }

        productListDialog?.dismiss()

        var modelArray = ArrayList<DialogModel>()
        var beanData = mData?.relationPrd?.data
        if (beanData!!.isNotEmpty()) {
            for (i in 0 until beanData.size) {
                var bean = beanData.get(i)
                var dialogModel = DialogModel()
                dialogModel.idx = bean.idx
                dialogModel.scCode = bean.sc_code
                dialogModel.pcode = bean.pcode
                dialogModel.storeName = bean.sc_name
                dialogModel.type = bean.strType
                dialogModel.thumbNail = bean.strPrdImg
                dialogModel.linkUrl = bean.strLinkUrl
                dialogModel.name = bean.strPrdName
                dialogModel.custPrice = bean.nPrdCustPrice
                dialogModel.sellPrice = bean.nPrdSellPrice
                dialogModel.streamKey = mData?.id
                dialogModel.vodType = mData?.videoType
                dialogModel.wish_cnt = bean.wish_cnt
                dialogModel.is_wish = bean.is_wish
                dialogModel.is_cart = bean.is_cart

                modelArray.add(dialogModel)
            }

            var playerInfo = PlayerInfo()
            var pipInfo = getPipInfo()
            playerInfo.myVODPosition = myVODPosition
            playerInfo.playerFlag = playerFlag
            playerInfo.videoType = mData?.videoType
            playerInfo.title = pipInfo!!.title
            playerInfo.productName = pipInfo!!.productName
            playerInfo.type = VideoPipBus.TYPE_DETAIL
            playerInfo.streamKey = streamKey
            var bus = VideoPipBus(videoUrl, player?.currentPosition, mData?.strContentSize, player?.volume!!, playerInfo)
            productListDialog = PlayerProductDialog(view!!.context, modelArray, bus, false).apply { show() }

            var task = ShopTreeAsyncTask(context!!)
            task.sendProductLinkView(streamKey)
        }
    }

    fun getPipInfo(): PipInfo? {
        if ( player == null)
            return null
        var pipInfo = PipInfo()
        pipInfo.videoUrl = videoUrl
        pipInfo.videoType = mData?.videoType
        pipInfo.currentPosition = player!!.currentPosition
        pipInfo.contentSize = mData?.strContentSize
        pipInfo.volume = player!!.volume
        pipInfo.title = mData?.title
        pipInfo.streamKey = streamKey
        var prdName = ""
        if ( mData?.relationPrd != null && mData?.relationPrd!!.data != null && mData?.relationPrd!!.data.size > 0 )
            prdName = mData?.relationPrd!!.data[0].strPrdName
        pipInfo.productName = prdName
        return pipInfo
    }

    /**
     * 시청자 액션 표시(입장/좋아요/장바구니 담기/구매하기)
     *
     * 사용자 큰 이미지 URL https://api.puddinglive.com:8080/v1/user/{유저 ID}/image
     * 사용자 작은 이미지 URL https://api.puddinglive.com:8080/v1/user/{유저 ID}/image/small
     * 10명까지는 즉시 , 이후는 1분 단위로 누구 외 몇명이 입장하였습니다. 라는 형태로 진행
     */
    private fun showUserActionPopup() {
        if (context == null || !mIsVisibleToUser) {
            return
        }

        when (mLiveUserActionData.gubun) {
            ChatManager.GUBUN_CART -> {
                userActionPopLayout.visibility = View.VISIBLE
                userActionPopLayout.setOnClickListener(clickListener)

                profile2.visibility = View.GONE
                profile3.visibility = View.GONE
                profile4.visibility = View.GONE
                profile5.visibility = View.GONE
                ImageLoad.setImage(context, profile1, "https://api.puddinglive.com:8080/v1/user/${mLiveUserActionData.cartUserId}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                userActionPopLayout.setBackgroundResource(R.drawable.bg_cast_cart_add)

                val lastIndex = mLiveUserActionData.cartUserNick.length
                val sp = SpannableStringBuilder("${mLiveUserActionData.cartUserNick}님이 장바구니에 담았습니다.").apply {
                    setSpan(StyleSpan(Typeface.BOLD), 0, lastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                action.text = sp

                mHandler?.sendEmptyMessageDelayed(MESSAGE_HIDE_USER_ACTION_POPUP, 5000)
            }

            ChatManager.GUBUN_BUY -> {
                userActionPopLayout.visibility = View.VISIBLE
                userActionPopLayout.setOnClickListener(clickListener)

                profile2.visibility = View.GONE
                profile3.visibility = View.GONE
                profile4.visibility = View.GONE
                profile5.visibility = View.GONE
                ImageLoad.setImage(context, profile1, "https://api.puddinglive.com:8080/v1/user/${mLiveUserActionData.buyUserId}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                userActionPopLayout.setBackgroundResource(R.drawable.bg_cast_purchase)

                val lastIndex = mLiveUserActionData.buyUserNick.length
                val sp = SpannableStringBuilder("${mLiveUserActionData.buyUserNick}님이 구매완료 했습니다.").apply {
                    setSpan(StyleSpan(Typeface.BOLD), 0, lastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                action.text = sp

                mHandler?.sendEmptyMessageDelayed(MESSAGE_HIDE_USER_ACTION_POPUP, 5000)
            }
        }
    }

    private fun hideUserActionPopup() {
        userActionPopLayout.visibility = View.GONE
    }

    private fun showUserJoinPopup() {
        if (context == null || !mIsVisibleToUser) {
            return
        }

        if (mLiveUserJoinData.nickName.size > 0) {
            val lastPos = mLiveUserJoinData.nickName.size - 1
            val count = mLiveUserJoinData.nickName.size
            if (isRoomJoinSolo) {
                userActionPopLayout.visibility = View.VISIBLE
                userActionPopLayout.setOnClickListener(clickListener)
                userActionPopLayout.setBackgroundResource(R.drawable.bg_cast_enter_room)

                profile2.visibility = View.GONE
                profile3.visibility = View.GONE
                profile4.visibility = View.GONE
                profile5.visibility = View.GONE
                ImageLoad.setImage(context, profile1, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[lastPos]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                val lastIndex = mLiveUserJoinData.nickName[lastPos].length
                val sp = SpannableStringBuilder("${mLiveUserJoinData.nickName[lastPos]}님이 입장했습니다.").apply {
                    setSpan(StyleSpan(Typeface.BOLD), 0, lastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                action.text = sp

                mHandler?.sendEmptyMessageDelayed(MESSAGE_HIDE_USER_JOIN_POPUP, 5000)
            } else {
                if (count > 0) {
                    userActionPopLayout.visibility = View.VISIBLE
                    userActionPopLayout.setOnClickListener(clickListener)
                    userActionPopLayout.setBackgroundResource(R.drawable.bg_cast_enter_room)
                    if (count >= 5) {
                        ImageLoad.setImage(context, profile1, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[0]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                        ImageLoad.setImage(context, profile2, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[1]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                        ImageLoad.setImage(context, profile3, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[2]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                        ImageLoad.setImage(context, profile4, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[3]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                        ImageLoad.setImage(context, profile5, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[4]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                    } else {
                        when (count) {
                            4 -> {
                                profile5.visibility = View.GONE
                                ImageLoad.setImage(context, profile1, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[0]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                                ImageLoad.setImage(context, profile2, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[1]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                                ImageLoad.setImage(context, profile3, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[2]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                                ImageLoad.setImage(context, profile4, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[3]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                            }
                            3 -> {
                                profile5.visibility = View.GONE
                                profile4.visibility = View.GONE
                                ImageLoad.setImage(context, profile1, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[0]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                                ImageLoad.setImage(context, profile2, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[1]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                                ImageLoad.setImage(context, profile3, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[2]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                            }
                            2 -> {
                                profile5.visibility = View.GONE
                                profile4.visibility = View.GONE
                                profile3.visibility = View.GONE
                                ImageLoad.setImage(context, profile1, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[0]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                                ImageLoad.setImage(context, profile2, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[1]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                            }
                            1 -> {
                                profile5.visibility = View.GONE
                                profile4.visibility = View.GONE
                                profile3.visibility = View.GONE
                                profile2.visibility = View.GONE
                                ImageLoad.setImage(context, profile1, "https://api.puddinglive.com:8080/v1/user/${mLiveUserJoinData.userId[0]}/image/small", null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                            }
                        }
                    }
                    val lastIndex = mLiveUserJoinData.nickName[lastPos].length
                    val sp = SpannableStringBuilder("${mLiveUserJoinData.nickName[lastPos]}님 외 ${count - 1}명이 입장했습니다.").apply {
                        setSpan(StyleSpan(Typeface.BOLD), 0, lastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    action.text = sp

                    mHandler?.sendEmptyMessageDelayed(MESSAGE_HIDE_USER_JOIN_POPUP, 5000)
                }
            }
        }
    }

    private fun hideUserJoinPopup() {
        userActionPopLayout.visibility = View.GONE

        if (enterRoomCount > 10) {
            isRoomJoinSolo = false
        }

        enterUserId.clear()
        enterUserNick.clear()
    }

    /**
     * VOD 좋아요 등록
     */
    private fun likeLive(status: String) {
        var requestObj = ApiParams.FavorParams(
                AppPreferences.getUserId(context!!)!!,
                mData!!.videoType,
                status)

        EventBus.getDefault().post(NetworkBus(NetworkApi.API8.name, mData?.id, requestObj))
    }

    /**
     * 조회수 등록
     */
    private fun viewCountADD() {
        var isLogin = AppPreferences.getLoginStatus(view!!.context)
        if (isLogin && mData != null) {
            var requestObj = ApiParams.HitsParams(mData!!.userId, mData!!.id, mData!!.videoType, "Y")
            EventBus.getDefault().post(NetworkBus(NetworkApi.API10.name, mData?.id, requestObj))
        } else {
            //TODO 비 로그인 경우에는 UUID 값을 streamKey 적용
        }
    }

    /**
     * 조회수를 요청한다.
     */
    private fun getViewCount() {
        var bus = NetworkBus(NetworkApi.API9.name, mData?.id)
        EventBus.getDefault().post(bus)
    }

    /**
     * 스크랩을 등록한다.
     */
    private fun handleScrap(status: String) {
        if ( "Y" == status ) {
            val task = ShopTreeAsyncTask(context!!)
            task.deletetScrap(mData?.id) { result, obj ->
                if ( result ) {
                    var resultObj = obj as JSONObject
                    var rt = resultObj.optString("result")
                    if ( "success" == rt ) {
                        try {
                            AppToast.cancelAllToast()
                            handleScrapUI()
                        } catch (e:Exception) {
                            Logger.p(e)
                        }
                    }
                }
            }
        } else {
            val obj = JSONObject().apply {
                put("streamKey", mData?.id)
                put("user", AppPreferences.getUserId(context!!))
                put("vod_type", mData?.videoType)
            }

            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
            val task = ShopTreeAsyncTask(context!!)
            task.postScrap(body) { result, obj ->
                if ( result ) {
                    var resultObj = obj as JSONObject
                    var rt = resultObj.optString("result")
                    if ( "success" == rt ) {
                        try {
                            AppToast.cancelAllToast()
                            handleScrapUI()
                        } catch (e:Exception) {
                            Logger.p(e)
                        }
                    }
                }
            }
        }





//        if ("N" == status) {
//            // scrap delete!!
//            val task = ShopTreeAsyncTask(context!!)
//            task.deletetScrap(mData?.id, { result, obj ->
//                try {
//                    AppToast.cancelAllToast()
//                    handleScrapUI()
//                } catch (e:Exception) {
//                    Logger.p(e)
//                }
//            })
//
//            return
//        }
//
//        val obj = JSONObject().apply {
//            put("streamKey", mData?.id)
//            put("user", AppPreferences.getUserId(context!!))
//            put("vod_type", mData?.videoType)
//        }
//
//        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
//        val task = ShopTreeAsyncTask(context!!)
//        task.postScrap(body, { result, obj ->
//            try {
//                AppToast.cancelAllToast()
//                handleScrapUI()
//            } catch (e:Exception) {
//                Logger.p(e)
//            }
//        })
    }

    /**
     * 푸딩을 선물한다.
     */
    private fun sendCookie(sendId: String, quantity: Int) {
        val requestObj = JSONObject()
        requestObj.put("streamKey", mData?.id)
        requestObj.put("vod_type", "LIVE")
        requestObj.put("cnt", quantity.toString())
        requestObj.put("user", sendId)

        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString())
        val task = ShopTreeAsyncTask(context!!)
        task.postCookie(body,
                { result, obj ->
                    try {
                        try {
                            chatManager?.sendCookieSignal(ChatManager.COOKIE_ETC_CHAT_CMD, "", cookieCnt, mNickName)
                        } catch (e: Exception) {
                            Logger.p(e)
                        }
                    } catch (e:Exception) {
                        Logger.p(e)
                    }
                })
    }

    /**
     * 30초마다 한번씩 라이브 영상 정보를 가져온다.
     */
    private fun getLiveInfo() {
        Logger.e("getLiveInfo")
        val task = ShopTreeAsyncTask(context!!)
        task.getLiveInfo(mData?.id) { result, obj ->
            try {
                val response = Gson().fromJson(obj.toString(), API98::class.java)
                if(response.data.size > 0) {

                    if(mData?.isOnAir == "N") {
                        showOffAirPopup()
                        return@getLiveInfo
                    }

                    val json = Gson().toJson(response.data[0])  // 특정 라이브 정보 data 리스트 개수는 하나뿐임.
                    mData = Gson().fromJson(json, VOD.DataBeanX::class.java)

                    Logger.e("getLiveInfo: ${mData?.videoType}")

                    if (TextUtils.isEmpty(mData?.strNoti)) {
                        textViewNotification.visibility = View.GONE
                    } else {
                        textViewNotification.text = mData?.strNoti
                        textViewNotification.isSelected = true
                    }

                    textViewNickName.text = mData?.userNick

                    if(mData?.shareCount!!.toInt() == 0) {
                        shareCount.visibility = View.GONE
                    } else {
                        shareCount.visibility = View.VISIBLE
                        shareCount.text = "${mData?.shareCount}"
                    }

                    if(mData?.scrapCount!!.toInt() == 0) {
                        scrapCount.visibility = View.GONE
                    } else {
                        scrapCount.visibility = View.VISIBLE
                        scrapCount.text = mData?.scrapCount
                    }

                    viewCount.text = StringUtils.getSnsStyleCountZeroBase(mData?.viewerCount!!.toInt())
                    iFavoriteCount = mData?.favoriteCount!!.toInt()
                    buttonLike?.text = StringUtils.getSnsStyleCountZeroBase(iFavoriteCount)

                    if(timer.format.isNullOrEmpty()) {
                        timer.base = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - mData?.start_date!! * 1000)
                        timer.format = "LIVE %s"
                        timer.start()
                    }
                } else {
                    showOffAirPopup()
                }
            } catch (e : java.lang.Exception) {
                Logger.p(e)
            }
        }
    }

    /**
     * 채팅방 접속 API가 EventBus로 동작이 느린경우에 사용
     */
    private fun postChatConnect(body:RequestBody) {
        val task = ShopTreeAsyncTask(context!!)
        task.postChatConnect(body,  { result, obj ->
            try {
                val response: API142 = Gson().fromJson(obj.toString(), API142::class.java)
                mHandler?.post { connectChat(response) }
            } catch (e:Exception) {
                Logger.p(e)
            }
        })
    }

    /**
     * 스크랩 UI 처리
     */
    private fun handleScrapUI() {
        var countText = mData?.scrapCount
        if ( "Y" == scrapStatus  )
            scrapStatus = "N"
        else
            scrapStatus = "Y"
        if (!countText.isNullOrEmpty()) {
            var count = countText.toInt()
            if ("Y" == scrapStatus) {
                buttonScrap.isSelected = true
                count++

                var view: View = LayoutInflater.from(context).inflate(R.layout.popup_scrap_on, null)
                AppToast(context!!).showToastAtView(view, AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_MIDDLE)
            } else {
                buttonScrap.isSelected = false
                count--

                var view: View = LayoutInflater.from(context).inflate(R.layout.popup_scrap_off, null)
                AppToast(context!!).showToastAtView(view, AppToast.DURATION_MILLISECONDS_DEFAULT, AppToast.GRAVITY_MIDDLE)
            }
            mData?.scrapCount = count.toString()
            mData?.isScrap = scrapStatus

            if(count == 0) {
                scrapCount.visibility = View.GONE
            } else {
                scrapCount.visibility = View.VISIBLE
                scrapCount.text = count.toString()
            }

            getLiveInfo()
        }
    }

    /**
     * 라이브 정보를 갱신한다.
     */
    private fun refreshLiveData(mData:VOD.DataBeanX) {
        streamKey = mData.id
        uploader = mData.userId
        vodType = getVodType(mData.videoType)
        viewCnt = mData.viewerCount.toString()
        favoriteCnt = mData.favoriteCount.toString()
        scrapCnt = mData.scrapCount
        thumbImage = mData.thumbnailUrl
        shareTitle = mData.title

        if ("Y" == mData.isFavorite) {
            // 내가 좋아요 눌렀었다..
//                        buttonLike.isSelected = true
            likeStatus = "Y"
        }

        if ("Y" == mData.isScrap) {
            buttonScrap.isSelected = true
            scrapStatus = "Y"
        } else {
            buttonScrap.isSelected = false
            scrapStatus = "N"
        }

        if (mData.scrapCount.toInt() == 0) {
            scrapCount.visibility = View.GONE
        } else {
            scrapCount.visibility = View.VISIBLE
            scrapCount.text = mData.scrapCount
        }

        if (mData.shareCount.toInt() == 0) {
            shareCount.visibility = View.GONE
        } else {
            shareCount.visibility = View.VISIBLE
            shareCount.text = "${mData.shareCount}"
        }
        iFavoriteCount = mData.favoriteCount.toInt()
        buttonLike?.text = StringUtils.getSnsStyleCountZeroBase(iFavoriteCount)
        textViewNickName.text = mData.userNick
        textViewNotification.text = mData.strNoti
    }

    /**
     * 좋아요 버튼 클릭 이벤트 처리(연타 허용)
     */
    private val likeClickListener = View.OnClickListener {
        when(it?.id) {
            R.id.likeButtonLayer -> {
                if (!liveBroadcastFinished) {
                    try {
                        chatManager?.sendHeartSignal()
                        iFavoriteCount ++
                        buttonLike?.text = StringUtils.getSnsStyleCountZeroBase(iFavoriteCount)
                        if ("N" == likeStatus) {
                            likeStatus = "Y"
//                                it.isSelected = true

//                                likeLive(likeStatus)
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }
        }
    }

    /**
     * 버튼 클릭 이벤트 처리
     */
    private val clickListener:OnSingleClickListener = object:OnSingleClickListener() {
        override fun onSingleClick(it: View?) {
            Logger.e("onClick: $it?")

            when (it?.id) {
                R.id.productPopLayout -> {
                    showProductDialog()
                    it?.visibility = View.GONE
                }

                R.id.layoutMore -> layoutMore?.visibility = View.GONE

                R.id.chatInput -> {
                    if(!liveBroadcastFinished) {
                        if (!AppPreferences.getLoginStatus(it.context)) {
                            startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                            return
                        }

                        editTextChat.requestFocus()
                        editTextChat.postDelayed({
                            val inputMethodManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                            inputMethodManager!!.showSoftInput(editTextChat, 0)
                        }, 100)
                    }
                }

                R.id.imageViewFollow -> {
                    if (AppPreferences.getLoginStatus(context!!)) {
                        var userId = AppPreferences.getUserId(context!!)
                        if (!TextUtils.isEmpty(userId)) {
                            val requestObj = JSONObject()
                            requestObj.put("strUserId", userId)
                            requestObj.put("strToUserId", mData?.userId)
                            if (mIsFollowing) {
                                requestObj.put("isFollow", "N")
                            } else {
                                requestObj.put("isFollow", "Y")
                            }
                            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString())
                            val task = ShopTreeAsyncTask(context!!)
                            task.postFollow(body,
                                    { result, obj ->
                                        try {
                                            if (mIsFollowing) {
                                                imageViewFollow?.setImageResource(R.drawable.vod_follow_ico)
                                            } else {
                                                imageViewFollow?.setImageResource(R.drawable.vod_following_ico)
                                            }
                                            mIsFollowing = !mIsFollowing
                                        } catch (e:Exception) {
                                            Logger.p(e)
                                        }
                                    })
                        }
                    } else {
                        startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                    }
                }

                R.id.clickLiveLayout -> showFullLayoutAnimation()

                R.id.buttonMute -> postMuteBus()

                R.id.buttonExit -> (activity as? ShoppingPlayerActivity)?.finish()

                R.id.buttonProducts -> showProductDialog()

                R.id.productPopLayout -> {
                    it.visibility = View.GONE
                    showProductDialog()
                }

                R.id.buttonStar -> {
                    if (!liveBroadcastFinished) {
                        if (!AppPreferences.getLoginStatus(it.context)) {
                            startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                            return
                        }

                        showCookieDialog()
                    }
                }

                R.id.buttonShare -> {
                    if (!liveBroadcastFinished) {
                        if (!AppPreferences.getLoginStatus(it.context)) {
                            startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                            return
                        }

                        if ( sheetDialog != null && !sheetDialog!!.isShowing )
                            showShareDialog()
                        else
                            showShareDialog()
                    }
                }

                R.id.buttonSendMsg -> {
                    sendText("*")
                    hideChatMode()
                }

                R.id.buttonViewerList -> {
                    if (!liveBroadcastFinished) {
                        userListDialog?.dismiss()
                        userListDialog = null
                        userListDialog = LiveUserListDialog(view!!.context).apply {
                            show()
                        }
                    }
                }

                R.id.likeButtonLayer -> startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)

                R.id.buttonScrap -> {
                    if (!AppPreferences.getLoginStatus(it.context)) {
                        startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                        return
                    }

                    handleScrap(scrapStatus)






//                if ( !isScrapClicked ) {
//                    Logger.e(" scrap clicked ")
//                    isScrapClicked = true
//                    if (!liveBroadcastFinished) {
//                        if (AppPreferences.getLoginStatus(it.context)) {
////                            if (it.isSelected) {
////                                scrapStatus = "N"
////                            } else {
////                                scrapStatus = "Y"
////                            }
//                            handleScrap(scrapStatus)
//                        } else {
//                            Handler().postDelayed({
//                                isScrapClicked = false
//                            }, DELAY)
//                            startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
//                        }
//                    } else {
//                        Handler().postDelayed({
//                            isScrapClicked = false
//                        }, DELAY)
//                    }
//                }
                }

                R.id.buttonMore -> {
                    if (!liveBroadcastFinished) {
                        if (layoutMore.visibility == View.VISIBLE) {
                            layoutMore.visibility = View.GONE
                        } else {
                            layoutMore.visibility = View.VISIBLE
                            layoutMore.bringToFront()
                        }
                    }
                }

                R.id.textViewNickNameModify -> {
                    layoutMore?.visibility = View.GONE

                    if (!liveBroadcastFinished) {
                        if (AppPreferences.getLoginStatus(it.context)) {
                            showNickNameModifyDialog()
                        } else {
                            startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                        }
                    }
                }

                R.id.textViewBroadcastReport -> {
                    layoutMore?.visibility = View.GONE

                    if (!AppPreferences.getLoginStatus(it.context)) {
                        startActivityForResult(Intent(it.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                        return
                    }

                    if (!liveBroadcastFinished) {
                        startActivity(Intent(view!!.context, BroadcastReportActivity::class.java).apply {
                            putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_REPORT_TYPE, false)
                            putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_USER_ID, mData!!.userId)
                            putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_STREAM_KEY, mData!!.id)
                            putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, mData!!.videoType)
                        })
                    }
                }

                R.id.textViewCustomerCenter -> {
                    layoutMore?.visibility = View.GONE

                    if (!liveBroadcastFinished) {
                        startActivity(Intent(view!!.context, CustomerCenterMainActivity::class.java))
                    }
                }

//            R.id.buttonInquiryMessage -> {
//                toggleInquire()
//            }

                R.id.Notification_close -> {
                    castLayer.visibility = View.GONE
                }

                R.id.userActionPopLayout -> {
                    it.visibility = View.GONE
                }
            }
        }
    }

    /**
     * 공유 다이얼로그 클릭 이벤트 처리
     */
    private val shareClickListener: View.OnClickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonKakao -> {
                Logger.d("Kakao Button Clicked.")
//                makeShareLink(SPACE_KAKAO)
                share(SPACE_KAKAO)
            }

            R.id.buttonFacebook -> {
                Logger.d("Facebook Button Clicked.")
//                makeShareLink(SPACE_FACEBOOK)
                share(SPACE_FACEBOOK)
            }

//            R.id.buttonInsta -> {
//                Logger.d("Insta Button Clicked.")
//            }

//            R.id.buttonMessenger -> {
//                Logger.d("Messenger Button Clicked.")
////                makeShareLink(SPACE_MESSENGER)
//                share(SPACE_MESSENGER)
//            }

            R.id.buttonSourceCode -> {
                Logger.d("SourceCode Button Clicked.")
//                makeShareLink(SPACE_SOURCECODE)
                share(SPACE_SOURCECODE)
            }

            R.id.buttonLink -> {
                Logger.d("Link Button Clicked.")
//                makeShareLink(SPACE_LINK)
                share(SPACE_LINK)
            }

            R.id.buttonMore -> {
                Logger.d("More Button Clicked.")
//                makeShareLink(SPACE_OTHER)
                share(SPACE_OTHER)
            }
        }
    }

    private fun registerChatBR(context: Context?) {
        Logger.e("registerChatBR")

//        if (context != null) {
//            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).registerReceiver(sendChatReceiver, IntentFilter().apply { addAction(SEND_CHAT) })
//        }

        activity?.registerReceiver(sendChatReceiver, IntentFilter().apply { addAction(SEND_CHAT) })
    }

    private fun unregisterChatBR() {
        Logger.e("unregisterChatBR")

//        if (activity != null) {
//            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(sendChatReceiver)
//        }

        activity?.unregisterReceiver(sendChatReceiver)
    }

    /**
     * 채팅 연결이 의도치 않게 끊겼을 때
     */
    override fun onChatDisconnected() {
        Logger.e("onChatDisconnected")

        chatManager?.connect(chatMyId!!, chatIP, chatPort)
    }

    /**
     * 채팅 연결간 오류가 발생하였을 때
     */
    override fun onChatConnectError(errorCode: Int) {
        Logger.e("Connect Error!")

        mChatConnected = false

        when (errorCode) {
            ChatManager.ERROR_CODE_DUPLICATE_NAME -> {
                // 닉네임이 중복되는 경우 연결을 해제하고 닉네임을 변경하여 요청
                chatManager?.disconnect()
                chatManager = null
                chatMyId?.nickName?.replace("${duplicateNickAddHeader - 1}", "")
                chatMyId?.nickName = "${chatMyId?.nickName ?: ""}$duplicateNickAddHeader"
                ++duplicateNickAddHeader

                chatManager = ChatManager().apply {
                    setChatListener(this@ShoppingLiveFragment)
                    connect(chatMyId!!, chatIP, chatPort)
                }
            }
        }
    }

    /**
     * 채팅방에 정상적으로 접속이 완료됨
     * @param users     현재 접속중인 채팅방 사용자 리스트
     */
    override fun onRoomConnected(users: List<String>) {
        mChatConnected = true

        var nickName = ""
        if (users.isNotEmpty()) {
            users.forEach {
                Logger.e("onRoomConnected: $it")
                try {
                    var user: ChatUser = Gson().fromJson<ChatUser>(it, ChatUser::class.java)
                    var id = user.id
                    if (!TextUtils.isEmpty(id)) {
                        mUserMap[id] = user.nickName
                    }

                    if(id == AppPreferences.getUserId(context!!)!!) {
                        nickName = user.nickName
                    }
                } catch (e: Exception) {
                    Logger.p(e)
                }
            }
        }

        mHandler?.post {
            this.viewCount?.text = StringUtils.getSnsStyleCount(mUserMap.size)
        }

        // 본인 입장을 채팅창에 표시
        recyclerViewChat?.post {
            chatAdapter?.addItem(AdapterChatData(ChatManager.NORMAL_CHAT_CMD, null,
                    String.format(getString(R.string.msg_chat_user_join_format), if(nickName.isNotEmpty()) nickName else getAccountID()),
                    System.currentTimeMillis()))

            chatLayoutManager.scrollToPositionWithOffset(0, 0)
        }
    }

    /**
     * 채팅방에 신규 사용자가 추가 접속하였음
     * @param user
     */
    override fun onRoomJoinedUser(user: ChatUser, json: String) {
        var id = user.id
        if (TextUtils.isEmpty(id) || mUserMap.containsKey(id)) {
            return
        }

        Logger.d("onRoomJoinedUser:$id")
        mUserMap[id] = user.nickName

        enterUserId.add(user.id)
        enterUserNick.add(user.nickName)

        mHandler?.post {
            this.viewCount?.text = StringUtils.getSnsStyleCount(mUserMap.size)
        }

        // 방 입장을 채팅창에 표시
//        recyclerViewChat?.post {
//            chatAdapter?.addItem(AdapterChatData(ChatManager.NORMAL_CHAT_CMD, null,
//                    String.format(getString(R.string.msg_chat_user_join_format), user.nickName),
//                    System.currentTimeMillis()))
//
//            chatLayoutManager.scrollToPositionWithOffset(0, 0)
//        }

        mLiveUserJoinData.nickName = enterUserNick
        mLiveUserJoinData.userId = enterUserId

        if (isRoomJoinSolo) {
            ++enterRoomCount
            mHandler?.sendEmptyMessage(MESSAGE_SHOW_USER_JOIN_POPUP)
        } else {
            if (mTimer == null) {
                mTimer = Timer().apply {
                    schedule(mTask, 0, MESSAGE_SHOW_USER_ACTION_POPUP_CALL_TIME)
                }
            }
        }
    }

    /**
     * 채팅방에 사용자가 접속을 해제하였음
     * @param user
     */
    override fun onRoomLeavedUser(user: ChatUser, json: String) {
        var id = user.id
        if (TextUtils.isEmpty(id) || !mUserMap.containsKey(id)) {
            return
        }

        Logger.d("onRoomLeavedUser:${user.id} and user nick : ${user.nickName}")

        // 방 퇴장을 채팅창에 표시
//        recyclerViewChat?.post {
//            if (context != null) {
//                chatAdapter?.addItem(AdapterChatData(ChatManager.NORMAL_CHAT_CMD, null,
//                        String.format(getString(R.string.msg_chat_user_leave_format), user.nickName),
//                        System.currentTimeMillis()))
//
//                chatLayoutManager.scrollToPositionWithOffset(0, 0)
//            }
//        }

        mUserMap.remove(id)

        mHandler?.post {
            this.viewCount?.text = StringUtils.getSnsStyleCount(mUserMap.size)
        }
    }

    /**
     * 채팅방이 정상적으로 종료됨
     */
    override fun onChatFinished() {
        //if (buttonChatMode != null)
        //buttonChatMode.visibility = View.GONE
    }

    /**
     * 채팅 대화 내용을 수신함
     */
    override fun onChatReceived(chatMsg: Map<String, String>) {
        mUIHandler.post {
            var chatContent = if (chatMsg.containsKey("ChatContent")) chatMsg["ChatContent"] else null
            var dateContent = if (chatMsg.containsKey("DateContent")) chatMsg["DateContent"] else null
            var cmd = if (chatMsg.containsKey("Cmd")) chatMsg["Cmd"] else ChatManager.NORMAL_CHAT_CMD
            var color = if (chatMsg.containsKey("Color")) chatMsg["Color"] else ""
            if (cmd != ChatManager.HEART_CHAT_CMD) {
                Logger.e("onChatReceived chatContent :: $chatContent")
                Logger.e("onChatReceived dateContent :: $dateContent")
                Logger.e("onChatReceived mChatCommand :: $cmd")
                Logger.e("onChatReceived color :: $color")

                if (chatContent != null && dateContent != null) {
                    try {
                        if (cmd == ChatManager.COOKIE_ETC_CHAT_CMD) {
                            // {"l":"123","a":"5","dk":"0","no":"72fa6ab2a54e613feec54a8021fd9423","id":"jhan2019","nk":"진현이2","lv":"3","an":"0","dt":"1100P24095110552CuaB8zRnV"} say to * : COOKIE|^|진현이2|^|10
                            // 푸딩 : COOKIE|^|닉네임|^|푸딩 개수
                            // 구매시 : BUY|^|닉네임|^|상품이름
                            // 카트 담기 : CART|^|닉네임|^|상품이름
                            val splitMessageRaw = chatContent.split(" say to ")
                            val from = splitMessageRaw[0]
                            val message = splitMessageRaw[1].split(":")[1]
                            val arr = message.split("|^|")
                            val gubun = arr[0].replace(" ", "")
                            if (gubun == ChatManager.GUBUN_COOKIE) {
                                val cookieData = CookieData().apply {
                                    cnt = arr[2].toInt()
                                    sender = arr[1]
                                    id = JSONObject(from).getString("id")
                                }
                                mCookieQueue.offer(cookieData)
                                mHandler?.sendEmptyMessage(MESSAGE_SHOW_COOKIE)

                                val insertData = AdapterChatData(cmd, from, message, dateContent.toLong())
                                chatAdapter?.addItem(insertData)
                                chatLayoutManager.scrollToPositionWithOffset(chatAdapter?.itemCount!!.minus(1), 0)
                            } else if (gubun == ChatManager.GUBUN_CART) {
                                val insertData = AdapterChatData(cmd, from, message, dateContent.toLong())
                                chatAdapter?.addItem(insertData)
                                chatLayoutManager.scrollToPositionWithOffset(chatAdapter?.itemCount!!.minus(1), 0)

//                                mLiveUserActionData.cartUserNick = arr[1]
//                                mLiveUserActionData.cartUserId = JSONObject(from).getString("id")
//                                mLiveUserActionData.gubun = gubun
//                                mHandler?.sendEmptyMessage(MESSAGE_SHOW_USER_ACTION_POPUP)
                            } else if (gubun == ChatManager.GUBUN_BUY) {
                                val insertData = AdapterChatData(cmd, from, message, dateContent.toLong())
                                chatAdapter?.addItem(insertData)
                                chatLayoutManager.scrollToPositionWithOffset(chatAdapter?.itemCount!!.minus(1), 0)

//                                mLiveUserActionData.buyUserNick = arr[1]
//                                mLiveUserActionData.buyUserId = JSONObject(from).getString("id")
//                                mLiveUserActionData.gubun = gubun
//                                mHandler?.sendEmptyMessage(MESSAGE_SHOW_USER_ACTION_POPUP)
                            }

                            // message 안에 chat cmd 구분자가 있어서 message를 자르면 안된다..상품이름이 긴 경우 메시지가 잘리는 문제 때문에
//                            if (message.length > 36) {
//                                message = message.substring(36)
//                            }
                        } else if (cmd == ChatManager.BROADCAST_INFO_CMD) {
                            getLiveInfo()
                        } else if (cmd == ChatManager.FORCE_EXIT_CMD) {
                            if (isVisibleFragment()) {
                                LiveBroadcastFinishedDialog(view!!.context, mData?.userNick!!, mData?.userImage!!, mData?.userId!!, mIsFollowing, true).apply {
                                    setOnDismissListener {
                                        (activity as? ShoppingPlayerActivity)?.finish()
                                    }
                                }.show()
                            }
                        } else {
                            val splitMessageRaw = chatContent.split(" say to ")
                            val from = splitMessageRaw[0]
                            val message = splitMessageRaw[1].split(":")[1]
//                            if (message.length > 36) {
//                                message = message.substring(0, 36)
//                            }

                            var insertData = AdapterChatData(cmd, from, message, dateContent.toLong())
                            chatAdapter?.addItem(insertData)
                            chatLayoutManager.scrollToPositionWithOffset(chatAdapter?.itemCount!!.minus(1), 0)
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }
        }
    }

    /**
     * 채팅방 사용자가 Heart(좋아요) 아이콘을 눌렀음
     * (이를 받은 경우 HeartView 상에 푸딩을 1개 표시)
     */
    override fun onHeartAdded(user: ChatUser) {
        Logger.d("onHeartAdded()")
        mHandler?.sendEmptyMessageDelayed(MESSAGE_SHOW_HEART_VIEW, 300) // 0.3초 동안 모아서 1번만 그리자!

        //mHandler?.post { HeartViewExt(context, heartView) }

        // 필요한가??
//        var id = AppPreferences.getUserId(context!!)
//        if (user.id != id) {
        // 내가 좋아요 누르는 경우에는 처리하지 않는다.
//            mLiveActionData.likeUserNick = user.nickName
//            mLiveActionData.likeUserId = user.id
//            mLiveActionData.gubun = ChatManager.GUBUN_LIKE
//            mHandler?.sendEmptyMessage(MESSAGE_SHOW_USER_ACTION_POPUP)
//        }
    }

    private fun drawHeartView() {
        mHandler?.removeMessages(MESSAGE_SHOW_HEART_VIEW)

        if(context != null && isAdded) {
            mHandler?.post { HeartView(context, heartView) }
        }
    }

    private fun hideFullLayout() {
        if (mStartedMoveDownAnimation || !mIsVisibleToUser) {
            Logger.d("hideFullLayout return")
            return
        }

        Logger.d("hideFullLayout")

        if (mKeyboardHeight > 100) {
            SoftKeyboardUtils.hideKeyboard(editTextChat)
            return
        }

        mStartedMoveDownAnimation = true

        stopAnimation()
        buttonProducts?.visibility = View.GONE
        dark_bg?.visibility = View.GONE

        fullLayer?.visibility = View.INVISIBLE

        playerView?.hideController()

        if (layoutChatInput != null) {
            layoutChatInput?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.move_bottom))

            layoutChatInput?.postDelayed(Runnable {
                clickLiveLayout?.visibility = View.VISIBLE
                layoutChatInput?.visibility = View.INVISIBLE
                layoutMenu?.visibility = View.GONE

                if (mKeyboardHeight > 100) {
                    SoftKeyboardUtils.hideKeyboard(editTextChat)
                }

                mStartedMoveDownAnimation = false
            }, 500)
        }
    }

    private fun showFullLayout() {
        Logger.c("showFullLayout")

        if (playerView != null) {
            playerView.showController()

            layoutChatInput.visibility = View.VISIBLE
            fullLayer?.visibility = View.VISIBLE
            clickLiveLayout.visibility = View.INVISIBLE

            buttonProducts.visibility = View.VISIBLE
            chatRightMarginView.visibility = View.VISIBLE
            dark_bg.visibility = View.VISIBLE
        }
    }

    private fun showFullLayoutAnimation() {
        if (mStartedMoveUpAnimation) {
            return
        }
        mStartedMoveUpAnimation = true

        Logger.d("showFullLayoutAnimation")

        layoutChatInput?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.move_up))
        layoutChatInput?.postDelayed({
            layoutChatInput.visibility = View.VISIBLE
            fullLayer?.visibility = View.VISIBLE
            clickLiveLayout.visibility = View.INVISIBLE

            layoutMenu.visibility = View.VISIBLE
            buttonProducts.visibility = View.VISIBLE
            chatRightMarginView.visibility = View.VISIBLE
            dark_bg.visibility = View.VISIBLE

            startAnimation()
            mStartedMoveUpAnimation = false
        }, 300)
    }

    private fun receivedCookie() {
        if (mCookieQueue.isEmpty()) {
            return
        }

        val cookieData = mCookieQueue.poll()
        val receivedCnt = cookieData.cnt
        if(isAdded) {
            var gif = when (receivedCnt) {
//                in 0..49 -> GifDrawable(resources, R.raw.cookie1)
//                in 50..99 -> GifDrawable(resources, R.raw.cookie2)
//                in 100..299 -> GifDrawable(resources, R.raw.cookie3)
//                in 300..499 -> GifDrawable(resources, R.raw.cookie4)
//                in 500..999 -> GifDrawable(resources, R.raw.cookie5)
//                else -> GifDrawable(resources, R.raw.cookie6)
                in 0..49 -> GifDrawable(resources, R.raw.strawberry10)
                in 50..99 -> GifDrawable(resources, R.raw.grape50)
                in 100..299 -> GifDrawable(resources, R.raw.cherry100)
                in 300..499 -> GifDrawable(resources, R.raw.lemon300)
                in 500..999 -> GifDrawable(resources, R.raw.peach500)
                else -> GifDrawable(resources, R.raw.gom1000)
            }
            gif.loopCount = 4
            gif.addAnimationListener(AnimationListener {
                mHandler?.sendEmptyMessageDelayed(MESSAGE_HIDE_COOKIE, 3000)
            })

            cookieImg.setImageDrawable(gif)
        }

        sender.text = "${cookieData.sender}(${cookieData.id})"
//        cookieStr.text = "젤리 ${Utils.ToNumFormat(receivedCnt)}개 선물"

        var str = "젤리 ${Utils.ToNumFormat(receivedCnt)}개 선물"
        var font = R.font.notosanskr_bold
        var ssb = SpannableString(str)
        var typeface = ResourcesCompat.getFont(context!!, font)
        ssb.setSpan(StyleSpan(typeface!!.style), 2, str.length - 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(Color.parseColor("#9f56f2")), 2, str.length - 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        cookieStr.text = ssb


        if(cookieLayer != null) {
            cookieLayer.visibility = View.VISIBLE
        }

        val task = ShopTreeAsyncTask(context!!)
        task.getCookieInfo(mData?.id, { result, obj ->
            try {
                if (!TextUtils.isEmpty(obj.toString()) && cookie_sender != null) {
                    cookie_sender.removeAllViews()

                    val response: API82 = Gson().fromJson(obj.toString(), API82::class.java)
                    response.data.forEach {
                        cookie_sender.addView(LiveCookieSender(context, it.mb_nick, it.mb_user_img))
                    }
                }
            } catch (e : Exception) {
                Logger.p(e)
            }
        })
    }

    private fun hideCookie() {
        mHandler?.post(Runnable {
            cookieLayer.visibility = View.GONE

            if (cookieImg != null) {
                try {
                    (cookieImg.drawable as GifDrawable).recycle()
                } catch (e: Exception) {
                }
            }
        })

        if (!mCookieQueue.isEmpty()) {
            mHandler?.sendEmptyMessageDelayed(MESSAGE_SHOW_COOKIE, 300)
        }
    }

    private val videoTouchListener = object : View.OnTouchListener {
        override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
            return gestureDetector.onTouchEvent(event)
        }
    }

    private val sendChatReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Logger.e("sendChatReceiver receive action :: $action")
            if (SEND_CHAT == action) {
                var gubun = intent.getStringExtra("GUBUN")
                var productName = intent.getStringExtra("NAME")
                Logger.e("productName :: " + productName)
                Logger.e("gubun :: " + gubun)
                if (gubun == ChatManager.GUBUN_CART) {
                    if (chatManager != null) {
                        chatManager!!.sendCartSignal(ChatManager.COOKIE_ETC_CHAT_CMD, "", productName, mNickName)
                    }
                } else if (gubun == ChatManager.GUBUN_BUY) {
                    if ( chatManager != null )
                        chatManager!!.sendBuySignal(ChatManager.COOKIE_ETC_CHAT_CMD, "", productName, mNickName)
                }
            }
        }
    }

    fun getData(): VOD.DataBeanX {
        return mData!!
    }

    // login 되어 있는 경우만 db 저장
    private fun saveData() {
        if (AppPreferences.getLoginStatus(context!!)) {
            var json = Gson().toJson(mData)
            Logger.e("saveData: $json")

            var document = MutableDocument(mData?.id)
            document.setString(mData?.id, json)
            VideoDBManager.getInstance(context).put(document)
        }
    }

    private fun showProductPopup() {
        if (context == null || !mIsVisibleToUser ||  mData!!.relationPrd.data.size == 0) {
            Logger.e("showProductPopup fail")
            return
        }

        Logger.e("showProductPopup")

        productPopLayout?.visibility = View.VISIBLE
        productPopLayout?.setOnClickListener(clickListener)

        var data = if (mData != null) mData!!.relationPrd.data[0] else null
        if (data != null) {
            productName?.text = data.strPrdName

            var price = data?.nPrdSellPrice ?: ""
            price = PriceFormatter.getInstance()?.getFormattedValue(price) ?: "0"
            price = if (mData?.relationPrd?.data?.size == 1) {
                "${price}원"
            } else {
                "${price}원~"
            }
            productPrice?.text = price

            // 비동기로 이미지 그리기 위해
            Glide.with(context!!).load(data.strPrdImg).listener(object : RequestListener<Drawable> {
                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    //productImage.setImageDrawable(resource)
                    return false
                }

                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    return false
                }
            }).into(productImage)
        }

        mHandler?.sendEmptyMessageDelayed(MESSAGE_HIDE_PRODUCT_POPUP, 6000)
    }

    private fun hideProductPopup() {
        productPopLayout?.visibility = View.GONE
    }

    private fun handleFromOtherPlayerExt() {
        when (playerFlag) {
            AppConstants.EVENT_DETAIL_VOD_PLAYER -> {
                mData = (activity as ShoppingPlayerActivity).mVideoData[myVODPosition]
                (activity as ShoppingPlayerActivity).selectedData = mData
            }

            AppConstants.HASH_TAG_VOD_PLAYER -> {
                mData = (activity as ShoppingPlayerActivity).mVideoData[myVODPosition]
                (activity as ShoppingPlayerActivity).selectedData = mData
            }

            AppConstants.LATEST_VIEW_VOD_PLAYER -> {
                mData = (activity as ShoppingPlayerActivity).mVideoData[myVODPosition]
                (activity as ShoppingPlayerActivity).selectedData = mData
            }

            AppConstants.SCRAP_VOD_PLAYER -> {
                mData = (activity as ShoppingPlayerActivity).mVideoData[myVODPosition]
                (activity as ShoppingPlayerActivity).selectedData = mData
            }

            AppConstants.SHARE_VOD_PLAYER -> {
                Logger.e("SHARE_VOD_PLAYER dbKey = ${NetworkDBKey.getAPI49Key(shareKey)}")
                var str = DBManager.getInstance(context).get(NetworkDBKey.getAPI49Key(shareKey))
                Logger.e(" str :: ${str}")
                val response: API49 = Gson().fromJson(DBManager.getInstance(context).get(NetworkDBKey.getAPI49Key(shareKey)), API49::class.java)
                val json = Gson().toJson(response)
                mData = Gson().fromJson(json, VOD.DataBeanX::class.java)

                (activity as ShoppingPlayerActivity).selectedData = mData
            }

            AppConstants.SCHEDULE_PLAYER -> {
                val response: API98 = Gson().fromJson(DBManager.getInstance(context).get(shareKey), API98::class.java)
                val json = Gson().toJson(response.data[0])
                mData = Gson().fromJson(json, VOD.DataBeanX::class.java)

                (activity as ShoppingPlayerActivity).selectedData = mData
            }

            else -> mData = (activity as ShoppingPlayerActivity).mVideoData[myVODPosition]
        }
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(context!!).registerReceiver(receiver, IntentFilter("muteCalled"))
    }

    private fun unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(context!!).unregisterReceiver(receiver)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Logger.e("receiver called")
            val action = intent.action
            if ( action == "muteCalled") {
                // 사용자가 음소거를 하지 않은 상태에서만 사운드 켜고 줄이고를 수행해야함
                if ( !AppPreferences.getVideoMute(context!!) ) {
                    if ( player != null ) {
                        var muteStatus = intent.getBooleanExtra("isMute", false)
                        if ( muteStatus ) {
                            Logger.e("mPlayer volume :: " + player!!.volume)
                            // setVolume 되기 전에 강제적으로 player 볼륨을 바꾸고 나면 아래 mPlayer?.volume = AppPreferences.getVideoVolumn(context!!) 에서 볼륨 값이 0으로 세팅되서 볼륨이 안나오므로
                            if ( AppPreferences.getVideoVolumn(context!!) <= 0 ) {
                                Logger.e("player volume save")
                                AppPreferences.setVideoVolumn(context!!, player!!.volume)
                                Logger.e("after save volume :: " + AppPreferences.getVideoVolumn(context!!))
                            } else
                                Logger.e("player volume not save")

                            if (player?.playbackState ?: Player.STATE_IDLE != Player.STATE_IDLE) {
                                player?.playWhenReady = false
                                isPaused = true
                            }
                            player?.volume = 0f
                        } else {
                            EventBus.getDefault().post("FINISH_PIP_MODE")
                            player?.volume = AppPreferences.getVideoVolumn(context!!)
                            mHandler?.postDelayed({
                                playVideoByMe()
                                player?.seekToDefaultPosition() //라이브 현재 위치로 이동
                            }, 200)
                            startAnimation()
                        }
                    }
                } else {
                    var muteStatus = intent.getBooleanExtra("isMute", false)
                    if ( !muteStatus ) {
                        EventBus.getDefault().post("FINISH_PIP_MODE")
                        mHandler?.postDelayed({
                            playVideoByMe()
                            player?.seekToDefaultPosition() //라이브 현재 위치로 이동
                        }, 200)
                        startAnimation()
                    }
                }
            }
        }
    }

    private fun showOffAirPopup() {
        if(mIsVisibleToUser && (mOnAirDialog == null || ! mOnAirDialog!!.isShowing)) {
            mOnAirDialog = AppConfirmDialog(activity)
            mOnAirDialog!!.setTitle("알람")
            mOnAirDialog!!.setMessage("종료된 라이브 방송입니다.")
            mOnAirDialog!!.setButton("닫기", View.OnClickListener {
                mOnAirDialog!!.dismiss()
                (activity as? ShoppingPlayerActivity)?.finish()
            })
            mOnAirDialog!!.show()
        }
    }
}