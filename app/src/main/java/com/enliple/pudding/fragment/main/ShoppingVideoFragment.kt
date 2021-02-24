package com.enliple.pudding.fragment.main

import android.app.Activity
import android.app.KeyguardManager
import android.app.PictureInPictureParams
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.util.Rational
import android.view.*
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.couchbase.lite.MutableDocument
import com.enliple.commons.io.FileUtils
import com.enliple.pudding.R
import com.enliple.pudding.activity.*
import com.enliple.pudding.adapter.LastLiveChatAdapter
import com.enliple.pudding.bus.*
import com.enliple.pudding.common.VideoManager
import com.enliple.pudding.commons.app.*
import com.enliple.pudding.commons.chat.ChatManager
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
import com.enliple.pudding.commons.shoppingcommons.widget.LiveUserListDialog
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.keyboard.KeyboardHeightProvider
import com.enliple.pudding.model.*
import com.enliple.pudding.shoppingcaster.widget.CasterProductDialog
import com.enliple.pudding.shoppingcaster.widget.CasterTitleModifyDialog
import com.enliple.pudding.shoppingplayer.StreamingType
import com.enliple.pudding.widget.*
import com.enliple.pudding.widget.main.NickNameModifyDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.share.Sharer
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.MessageDialog
import com.facebook.share.widget.ShareDialog
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
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
import kotlinx.android.synthetic.main.fragment_play_list.*
import kotlinx.android.synthetic.main.fragment_shopping_video.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap

/**
 * 쇼핑 비디오 재생 전용 Fragment
 */

class ShoppingVideoFragment : androidx.fragment.app.Fragment(), CasterTitleModifyDialog.Listener, ShoppingVideoCheckDialog.ClickListener {
    companion object {
        private const val IS_TEST_PRODUCT_DIALOG = false
        private const val MESSAGE_HIDE_UI = 1000
        private const val MESSAGE_PIP = 1001
        private const val MESSAGE_VIEW_COUNT_ADD = 1002
        private const val MESSAGE_HIDE_COOKIE = 1004
        private const val MESSAGE_CHANGE_PRODUCT_IMAGE = 1005
        private const val MESSAGE_SHOW_PRODUCT_POPUP = 1006
        private const val MESSAGE_HIDE_PRODUCT_POPUP = 1007
        private const val MESSAGE_PROGRESS = 1008
        private const val MESSAGE_SHOW_HEART_VIEW = 2008 // heart view 전송

        private const val TIME_UNIT = 1000L     // 1초

        private const val PAGE_DATA_COUNT = 100

        private const val TITLE_KAKAO = "kakao"
        private const val TITLE_FACEBOOK = "face"
        private const val TITLE_MESSENGER = "messenger"
        private const val TITLE_LINK = "link"
        private const val TITLE_SOURCECODE = "code"
        private const val TITLE_OTHER = "other"

        const val ACTIVITY_REQUEST_CODE_LOGIN = 10000

        const val BUNDLE_EXTRA_KEY_VIDEO_URI = "videoUri"
        const val BUNDLE_EXTRA_KEY_VIDEO_TITLE = "title"
    }

    private lateinit var mGestureDetector: GestureDetector
    private var mPlayer: SimpleExoPlayer? = null

    private var videoUrl: String? = null
    private var videoType: StreamingType? = null
    private lateinit var videoTitle: String

    private var isPaused = false

    private lateinit var keyGuardManager: KeyguardManager
    private var isInPictureInPictureMode = false

    private var chatUserIds: MutableList<String> = ArrayList()
    private var userListDialog: LiveUserListDialog? = null
    private var cookieSendDialog: CookieSendDialog? = null
    private var videoReplyDialog: VideoReplyDialog? = null
    private var productListDialog: PlayerProductDialog? = null
    private var nickNameDialog: NickNameModifyDialog? = null
    private var videoTitleModifyDialog: CasterTitleModifyDialog? = null

    //private var mHomeData: API114? = null
    private var mData: VOD.DataBeanX? = null
    private var mPosition: Int = 0

    private var mIsVisibleToUser = false
    private var likeStatus: String = "N"
    private var scrapStatus: String = "N"
    private var myVODPosition = -1
    private var prodImage = ""
    private var mProdImageIndex = 0
    private var mIsInit: Boolean = false

    private var mHandler: MyHandler? = null

    private var mHideLayoutDelayTime: Long = 1L
    private var mHitDelayTime: Long = 0L

    private var mAudioVolume: Float = -1F

    private var streamKey = ""
    private var cookieSendId = ""
    private var cookieReceiveId = ""
    private var cookieCnt = 0
    private var casterProfileImg = ""
    private var uploader = ""
    private var viewCnt = ""
    //    private var favoriteCnt = ""
    private var scrapCnt = ""
    private var thumbImage = ""
    private var shareTitle = ""
    private var vodType = ""
    private var mShareType = ""
    private var playerFlag = -1
    private var isMyUpload = false
    private var pos = -1
    private var mStartedMoveUpAnimation = false
    private var mStartedMoveDownAnimation = false
    private lateinit var mMediaSource: MediaSource
    private var mTrackSelector: DefaultTrackSelector? = null
    private var mVideoFormatMap: MutableMap<Int, Format> = HashMap()
    private var mChangeResolution: String = ""
    private var mMyNick: String = ""
    private var mIsFollowing = false
    private var mShownProductPopup = false // product popup 은 한번만 띄운다.
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var shareKey = ""
    private var remainedCookie = ""
    private var chatNickName = ""
    private var likeCount = 0
    private var increaseLikeCount = 0
    private var strLikeCount = "0"
    private var previousSecond: Int = -1
    private var chatArray: ArrayList<ArrayList<ChatModel>>? = null
    private lateinit var chatLayoutManager: LinearLayoutManager
    private var chatAdapter: LastLiveChatAdapter? = null
    private var heartCount = 0
    private var isDrawHeartViewRun = false
    private var sDialog: BottomSheetDialog? = null
    private var productDialog: PlayerNewProductDialog? = null
    // Handler 메모리 릭 발생하니깐 항상 아래 처럼 만들자..
    private class MyHandler(fragment: ShoppingVideoFragment) : Handler() {
        private val mReference: WeakReference<ShoppingVideoFragment> = WeakReference(fragment)

        override fun handleMessage(msg: Message) {
            //Logger.d("handleMessage: ${msg.what}")

            when (msg.what) {
                //MESSAGE_HIDE_UI -> mReference.get()?.hideFullLayout()
                MESSAGE_PIP -> mReference.get()?.pictureInPictureMode()
                MESSAGE_VIEW_COUNT_ADD -> mReference.get()?.viewCountADD()
                //MESSAGE_SHOW_COOKIE -> mReference.get()?.showCookieAnimation()
                MESSAGE_HIDE_COOKIE -> mReference.get()?.hideCookie()
                MESSAGE_CHANGE_PRODUCT_IMAGE -> mReference.get()?.changeProductImage()
                MESSAGE_SHOW_PRODUCT_POPUP -> mReference.get()?.showProductPopup()
                MESSAGE_HIDE_PRODUCT_POPUP -> mReference.get()?.hideProductPopup()
                MESSAGE_PROGRESS -> mReference.get()?.drawProgress()
                MESSAGE_SHOW_HEART_VIEW -> mReference.get()?.drawHeartView()
                else -> super.handleMessage(msg)
            }
        }
    }

    interface Listener {
        fun scrapClick()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d("shoppingVideo onCreateView() $mPosition")
        return inflater.inflate(R.layout.fragment_shopping_video, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPosition = arguments!!.getInt("position")
        playerFlag = arguments!!.getInt("player_flag")
        myVODPosition = arguments!!.getInt("my_vod_position")
        shareKey = arguments!!.getString("share_key") ?: ""
        Logger.e("onCreate() position:$mPosition myVODPosition : $myVODPosition")

        if (playerFlag > 0) {
            handleFromOtherPlayerExt()
        } else {
            try {
                mData = (activity as ShoppingPlayerActivity).mVideoData[mPosition]
                Logger.e("mData set")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // DB 에 현재 Video 정보가 있는 경우 이것으로 사용한다.
        try {
            val dbKey = "GET/mui/vod/${mData?.id}?user=${AppPreferences.getUserId(context!!)}"
            Logger.e("dbKey :: " + dbKey)
            val data = Gson().fromJson(DBManager.getInstance(context!!).get(dbKey), API143::class.java)
            if (data == null)
                Logger.e("data null ~~~~~~")
            val vodJson = Gson().toJson(data.data[0])       // 특정 비디오 정보 data 리스트는 한개 뿐이다.
            mData = Gson().fromJson(vodJson, VOD.DataBeanX::class.java)
            Logger.e("mData db exist")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        videoUrl = if (mData != null) mData?.stream else ""

        videoTitle = if (mData != null) mData?.title ?: when {
            videoUrl == null -> ""
            videoUrl != null -> {
                var splitArg: List<String>? = videoUrl!!.split("/")
                splitArg?.get(splitArg.size - 1) ?: ""
            }
            else -> arguments!!.getString(BUNDLE_EXTRA_KEY_VIDEO_TITLE)
        } else ""

//        Logger.d("videoUrl : $videoUrl, videoTitle : $videoTitle")

        mHitDelayTime = AppPreferences.getHitTime(context!!) * TIME_UNIT
        mHideLayoutDelayTime = AppPreferences.getHideTime(context!!) * TIME_UNIT

        Logger.d("mHitDelayTime : $mHitDelayTime, mHideLayoutDelayTime : $mHideLayoutDelayTime")

        EventBus.getDefault().register(this)

        // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
        //keyboardHeightProvider = KeyboardHeightProvider(activity!!)
        //Handler().postDelayed(Runnable { keyboardHeightProvider!!.start() }, 1000)

        FacebookSdk.sdkInitialize(context!!)
        AppEventsLogger.activateApp(context!!)

        registerReceiver()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.d("onViewCreated() $mPosition")

        if (mHandler == null) {
            mHandler = MyHandler(this@ShoppingVideoFragment)
        }

        keyGuardManager = activity!!.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        mGestureDetector = GestureDetector(view.context, myGestureListener)

        chatLayoutManager = LinearLayoutManager(context)
        chatLayoutManager.orientation = RecyclerView.VERTICAL

        recyclerViewVODChat.setHasFixedSize(false)
        chatAdapter = LastLiveChatAdapter()
        recyclerViewVODChat.layoutManager = chatLayoutManager
        recyclerViewVODChat.adapter = chatAdapter

        clickLayout.setOnClickListener(clickListener)
        vodLayer.setOnTouchListener(myTouchListener)
        recyclerViewVODChat.setOnTouchListener(myTouchListener)
        //layerSendMsg.setOnClickListener(clickListener)
        videoType = VideoManager.getVideoType(videoUrl!!)
        Logger.d("videoType: $videoType")

        try {
            streamKey = mData?.id!!
            uploader = mData?.userId!!
            vodType = getVodType(mData?.videoType!!)
            viewCnt = mData?.viewerCount.toString()
            strLikeCount = mData?.favoriteCount.toString()
            likeCount = strLikeCount.toInt()
            scrapCnt = mData?.scrapCount!!
            thumbImage = mData?.thumbnailUrl!!
            shareTitle = mData?.title!!
            if (mData?.userId!! == AppPreferences.getUserId(context!!)!!) {
                isMyUpload = true
            }
        } catch (e: Exception) {
            Logger.p(e)
        }

        if (!isMyUpload) { // follow 정보 갱신
            EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, mData?.userId))
        }

        if (videoType != StreamingType.UNKNOWN) {
            //playerView.addOnAttachStateChangeListener(videoStateChangeListener)
            init()

            if (mIsVisibleToUser) {
                initVideo()
            }
        }

        try {
            val key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API21.toString(), AppPreferences.getUserId(context!!)!!, "")
            var response: API21 = Gson().fromJson(DBManager.getInstance(context).get(key), API21::class.java)
            mMyNick = response.userNickname
            remainedCookie = response.userCookie
            Logger.i("myNick $mMyNick, remainedCookie $remainedCookie")
        } catch (e: java.lang.Exception) {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Logger.d("onDestroyView()  $mPosition")

        releaseVideo()
        mTrackSelector = null

        unregisterReceiver()
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume() $mPosition")

        if (mIsInit) {
            mHandler?.postDelayed({
                if (mIsVisibleToUser) {
                    playVideoByMe()
                    seekTo()
                    startAnimation()
                }
            }, 800)
        }
    }

    override fun onPause() {
        super.onPause()
        Logger.d("onPause() $mPosition isVisible:$isVisible")
        pauseVideo()
        stopAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.e("onDestroy()  $mPosition")

        //saveData()

        AppToast.cancelAllToast()

        EventBus.getDefault().unregister(this)

//        if (keyboardHeightProvider != null) {
//            keyboardHeightProvider!!.close()
//        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var timePosition = mPlayer?.currentPosition ?: C.TIME_UNSET
        Logger.d("savedTimePosition : $timePosition")

        outState.putString(BUNDLE_EXTRA_KEY_VIDEO_URI, videoUrl)
//        outState.putLong(BUNDLE_EXTRA_KEY_VIDEO_POSITION, mPlayer?.currentPosition ?: C.TIME_UNSET)
        super.onSaveInstanceState(outState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Logger.d("onAttach $mPosition")

        //activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SECURE)

        //registerBR(context)
    }

    override fun onDetach() {
        super.onDetach()
        Logger.d("onDetach $mPosition")

        mHandler?.removeCallbacksAndMessages(null)

        //activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SECURE)

        //unRegisterBR()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Logger.e("onActivityResult: $requestCode")
        if (requestCode == ACTIVITY_REQUEST_CODE_LOGIN && resultCode == Activity.RESULT_OK) {
            likeButtonLayer.setOnClickListener(likeClickListener)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onChangedTitleText(title: String) {
        if (mData == null) {
            return
        }

        var type = when (mData?.videoType) {
            "LIVE",
            "MULTILIVE",
            "LASTLIVE" -> "LIVE"
            else -> "VOD"
        }

        var requestObj = ApiParams.TitleModifyParams(
                mData?.id,
                type,
                title,
                AppPreferences.getUserId(context!!))

        EventBus.getDefault().post(NetworkBus(NetworkApi.API15.name, requestObj))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
        Logger.e("SoftKeyboardBus height: ${bus.height}")

        if (mIsVisibleToUser) {
            Logger.e("ShoppingVideoFragment bus.height :: ${bus.height}")
            if (cookieSendDialog != null && cookieSendDialog!!.isShowing) {
                if (bus.height > 100) {
                    cookieSendDialog!!.isKeyboardShow(true)
                } else {
                    cookieSendDialog!!.isKeyboardShow(false)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusFastResponse) {
        if (!mIsVisibleToUser) {
            return
        }

        val api143Key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API143.toString(), mData?.id!!, "")

        if (data.arg1.startsWith(NetworkApi.API8.toString())) {
//            handleNetworkFavorResult(data)
        } else if (data.arg1.startsWith(api143Key)) {
            // 현재 비디오 정보 갱신 ( 우선은 공유 카운트를 가져오기 위해 )
            handleGetVideoInfo(data)
        } else if (data.arg1 == NetworkApi.API2.toString()) {
            mIsFollowing = !mIsFollowing
            if (mIsFollowing) {
                imageViewFollow?.setImageResource(R.drawable.vod_following_ico)
            } else {
                imageViewFollow?.setImageResource(R.drawable.vod_follow_ico)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val api21Key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API21.toString(), mData?.userId!!, "")

        if (data.arg1 == api21Key) { // 다음 비디오 판매자와의 follow 정보 확인
            handleNetworkUserInfo(data)
            return
        }

        if (!mIsVisibleToUser) {
            return
        }

        Logger.i("data.arg1 :: ${data.arg1}")

        if (data.arg1.startsWith(NetworkApi.API10.toString())) {
            handleNetworkResultViewCountADD(data)
        } else if (data.arg1.startsWith(NetworkApi.API15.toString())) {
            handleNetworkResultTitleModify(data)
        } else if (data.arg1.startsWith(NetworkApi.API95.toString())) {
            handleNetworkVODComment(data)
        } else if (data.arg1.startsWith(NetworkApi.API97.toString())) {
            handleNetworkVODPublic(data)
        } else if (data.arg1.startsWith(NetworkApi.API96.toString())) {
            handleNetworkVODShare(data)
        } else if (data.arg1.startsWith(NetworkApi.API19.toString())) {
            //VideoReplyDialog 에서 받아서 처리한다. (중요!!)
        } else if (data.arg1.startsWith(NetworkApi.API87.toString())) {
            handleNetworkDeleteVOD(data)
        } else if (data.arg1.startsWith(NetworkApi.API48.toString())) {
            handleVideoShare(data)
        }
    }

    private fun handleVideoShare(data: NetworkBusResponse) {
        if (data.arg2 == "ok") {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            if (str.isNotEmpty()) {
                var link = Gson().fromJson(str, API48::class.java).url
                Logger.e("shareLink :: $link")
                if (link.isNotEmpty()) {
                    when (mShareType) {
                        TITLE_KAKAO -> shareKakao(link)
                        TITLE_FACEBOOK -> shareFacebook(link)
                        TITLE_MESSENGER -> shareFacebookMessenger(link)
                        TITLE_LINK -> setClipBoardLink(link)
                        TITLE_OTHER -> shareEtc(link)
                        TITLE_SOURCECODE -> setClipBoardLink(link)
                    }

                    EventBus.getDefault().post(NetworkBus(NetworkApi.API143.name, mData?.id))
                }
            }
        }
    }

    private fun handleGetVideoInfo(data: NetworkBusFastResponse) {
        if (data.arg2 == "ok") {
            Logger.e("handleGetVideoInfo")

            val data = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API143::class.java)
            val vodJson = Gson().toJson(data.data[0])   // 특정 비디오 정보 data 리스트 개수는 하나 뿐임.
            mData = Gson().fromJson(vodJson, VOD.DataBeanX::class.java)
            drawCountText(shareCount, mData?.shareCount)
        } else {
            Logger.e("error : ${data.arg3} ${data.arg4}")
        }
    }

    private fun handlePostFollowInfo(data: NetworkBusFastResponse) {
        if (mIsVisibleToUser) {
            Logger.e("handlePostFollowInfo")

            if (data.arg2 == "ok") {
                mIsFollowing = !mIsFollowing
                if (mIsFollowing) {
                    imageViewFollow?.setImageResource(R.drawable.vod_following_ico)
                } else {
                    imageViewFollow?.setImageResource(R.drawable.vod_follow_ico)
                }
            } else {
                Logger.e("error : ${data.arg3} ${data.arg4}")
            }
        }
    }

    private fun setClipBoardLink(shareLink: String) {
        val clipboardManager = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clipData = ClipData.newPlainText("label", shareLink)
        clipboardManager!!.primaryClip = clipData

        AppToast(context!!).showToastMessage(resources.getString(R.string.clipboard_msg),
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM)
    }

    private fun setSourceCode(shareLink: String) {
        val clipboardManager = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clipData = ClipData.newPlainText("label", shareLink)
        clipboardManager!!.primaryClip = clipData

        AppToast(context!!).showToastMessage("공유 소스 코드가 복사 되었습니다.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM)
    }

    private fun shareEtc(shareLink: String) {
        val intent = Intent(android.content.Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareLink)
        startActivity(Intent.createChooser(intent, ""))
    }

    private fun shareFacebookMessenger(shareLink: String) {
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
                    AppToast(context!!).showToastMessage(resources.getString(R.string.facebook_message_success),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }

                override fun onCancel() {
//                    Toast.makeText(context, context!!.getString(R.string.facebook_message_cancel), Toast.LENGTH_SHORT).show()
                    AppToast(context!!).showToastMessage(resources.getString(R.string.facebook_message_cancel),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }

                override fun onError(error: FacebookException) {
                    error.printStackTrace()
//                    Toast.makeText(context, context!!.getString(R.string.facebook_message_fail), Toast.LENGTH_SHORT).show()
                    AppToast(context!!).showToastMessage(resources.getString(R.string.facebook_message_fail),
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

    private fun shareFacebook(shareLink: String) {
        val shareDialog = ShareDialog(this)
        val callbackManager = CallbackManager.Factory.create()

        shareDialog.registerCallback(callbackManager, object : FacebookCallback<Sharer.Result> {
            override fun onSuccess(result: Sharer.Result) {
//                Toast.makeText(context, context!!.getString(R.string.facebook_success), Toast.LENGTH_SHORT).show()
                AppToast(context!!).showToastMessage(resources.getString(R.string.facebook_success),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }

            override fun onCancel() {
//                Toast.makeText(context, context!!.getString(R.string.facebook_cancel), Toast.LENGTH_SHORT).show()
                AppToast(context!!).showToastMessage(resources.getString(R.string.facebook_cancel),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }

            override fun onError(error: FacebookException) {
                error.printStackTrace()
//                Toast.makeText(context, context!!.getString(R.string.facebook_fail), Toast.LENGTH_SHORT).show()
                AppToast(context!!).showToastMessage(resources.getString(R.string.facebook_fail),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        })

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareTitle + " " + shareLink)

        var facebookAppFound = false
        val matches = context?.packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (matches!!.isNotEmpty()) {
            for (info in matches!!) {
                var packageName = info.activityInfo.packageName.toLowerCase()
                if (packageName.startsWith("com.facebook.katana") || packageName.startsWith("com.facebook.lite")) {
                    intent.setPackage(info.activityInfo.packageName)
                    facebookAppFound = true
                    break
                }
            }
        }

        if (facebookAppFound) {
            startActivity(intent)
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

    private fun shareKakao(shareLink: String) {
        Logger.e("shareKakao")
        var viewCount = 0
        var likeCount = 0

        try {
            viewCount = viewCnt.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            likeCount = strLikeCount.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var feedTemplate = FeedTemplate.newBuilder(ContentObject.newBuilder(shareTitle, thumbImage,
                LinkObject.newBuilder().setWebUrl(shareLink).setMobileWebUrl(shareLink).build()).setDescrption("").build())
                .setSocial(SocialObject.newBuilder().setLikeCount(likeCount).setViewCount(viewCount).build())
                .addButton(ButtonObject("보러가기", LinkObject.newBuilder().setWebUrl(shareLink).setMobileWebUrl(shareLink).build())).build()

        KakaoLinkService.getInstance().sendDefault(context, feedTemplate, object : ResponseCallback<KakaoLinkResponse>() {
            override fun onFailure(errorResult: ErrorResult) {
                Logger.d("onFailure: $errorResult")
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: VideoMuteBus) {
        initVolumeButton()

        controlVolume()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: VideoReplyCountBus) {
        if (mIsVisibleToUser) {
            Logger.e("VideoReplyCountBus count:${bus.count}")
            drawCountText(replyCount, bus.count.toString())
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: ZzimStatusBus) {
        if (!mIsVisibleToUser) {
            return
        }

        Logger.e("############## status:${data.status}")
        for (i in 0 until mData?.relationPrd!!.data.size) {
            val item = mData?.relationPrd!!.data[i]
            if (data.productId == item.idx) {
                mData?.relationPrd!!.data[i].is_wish = data.status

                if (data.count.isNotEmpty()) {
                    mData?.relationPrd!!.data[i].wish_cnt = data.count
                } else {
                    val count = mData?.relationPrd!!.data[i].wish_cnt.toInt()
                    if ("Y" == data.status) {
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

//    @Subscribe
//    fun onMessageEvent(bus: String) {
//        if (mIsVisibleToUser) {
//            Logger.e("onMessageEvent: $bus")
//
//            when (bus) {
//                "onProductItemClicked",
//                "onLinkClicked",
//                "onProductCartClicked" ->
//            }
//        }
//    }

    /**
     * 쿠기 선물하기 Response
     */
    private fun handleNetworkCookieSend(data: NetworkBusFastResponse) {
        if ("ok" == data.arg2) {
            //CookieToast(view!!.context, cookieReceiveId, cookieSendId, cookieCnt).show(Toast(view!!.context))
            showCookieAnimation(cookieCnt)
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    private fun handleNetworkResultScrap(data: NetworkBusFastResponse) {
        Logger.e("handleNetworkResultScrap:$scrapStatus")

        if ("ok" == data.arg2) {
            AppToast.cancelAllToast()
            var count = if (mData != null) mData!!.scrapCount.toInt() else 0
            if ("Y" == scrapStatus) {
                buttonScrap.isSelected = true
                count++

                var view: View = LayoutInflater.from(context).inflate(R.layout.popup_scrap_on, null)
                AppToast(context!!).showToastAtView(view, AppToast.DURATION_MILLISECONDS_FAST, AppToast.GRAVITY_MIDDLE)
            } else {
                buttonScrap.isSelected = false
                count--

                var view: View = LayoutInflater.from(context).inflate(R.layout.popup_scrap_off, null)
                AppToast(context!!).showToastAtView(view, AppToast.DURATION_MILLISECONDS_FAST, AppToast.GRAVITY_MIDDLE)
            }
            if (mData != null) {
                mData?.scrapCount = count.toString()
                mData?.isScrap = scrapStatus
                drawCountText(scrapCount, mData?.scrapCount)
            }

            EventBus.getDefault().post(NetworkBus(NetworkApi.API143.name, mData?.id)) // 현재 Video 정보 갱신
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun handleNetworkVODPublic(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            if (textViewPublic.text.toString() == "영상 공개") {
                textViewPublic.text = "영상 비공개"
                mData!!.user_show_YN = "Y"

                AppToast(context!!).showToastMessage("영상이 공개 처리 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            } else {
                textViewPublic.text = "영상 공개"
                mData!!.user_show_YN = "N"

                AppToast(context!!).showToastMessage("영상이 비공개 처리 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun handleNetworkVODShare(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            if (textViewShare.text.toString() == "공유 비허용") {
                textViewShare.text = "공유 허용"
                mData!!.share_YN = "N"

                AppToast(context!!).showToastMessage("영상 공유가 비허용 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            } else {
                textViewShare.text = "공유 비허용"
                mData!!.share_YN = "Y"

                AppToast(context!!).showToastMessage("영상 공유가 허용 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun handleNetworkVODComment(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            if (textViewComment.text.toString() == "댓글 비허용") {
                textViewComment.text = "댓글 허용"
                mData!!.comment_YN = "N"

                AppToast(context!!).showToastMessage("영상 댓글이 비허용 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            } else {
                textViewComment.text = "댓글 비허용"
                mData!!.comment_YN = "Y"

                AppToast(context!!).showToastMessage("영상 댓글이 허용 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun handleNetworkDeleteVOD(data: NetworkBusResponse) {
        Logger.e("handleNetworkDeleteVOD")

        if ("ok" == data.arg2) {
            AppToast(context!!).showToastMessage("방송영상이 정상적으로 삭제 되었습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)

            (activity as ShoppingPlayerActivity).setResult(Activity.RESULT_OK)
            (activity as ShoppingPlayerActivity).finish()
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun handleNetworkUserInfo(data: NetworkBusResponse) {
        Logger.d("handleNetworkUserInfo")

        if ("ok" == data.arg2) {
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

    // pip mode 실행
    private fun pictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isInPictureInPictureMode = true

            try {
                var rational = Rational(playerView.width, playerView.height)
                var params = PictureInPictureParams.Builder().setAspectRatio(rational).build()
                activity!!.enterPictureInPictureMode(params)
            } catch (e: Exception) {
                Logger.p(e)

                isInPictureInPictureMode = false
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        Logger.e("onPictureInPictureModeChanged: $isInPictureInPictureMode")
        this.isInPictureInPictureMode = isInPictureInPictureMode

        if (isInPictureInPictureMode) {
            hideFullLayout()
        }
    }

    /**
     * 방송 제목 수정 Response 처리
     */
    private fun handleNetworkResultTitleModify(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            AppToast(context!!).showToastMessage("방송 제목이 정상적으로 변경 되었습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)

            videoTitleModifyDialog!!.dismiss()
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    /**
     * 좋아요 등록/해제 Response 처리
     */
//    private fun handleNetworkFavorResult(data: NetworkBusFastResponse) {
//        if ("fail" == data.arg2) {
//            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
//            Logger.e("error : $errorResult")
//
//            AppToast(view!!.context).showToastMessage("좋아요 등록이 실패하였습니다.",
//                    AppToast.DURATION_MILLISECONDS_DEFAULT,
//                    AppToast.GRAVITY_BOTTOM)
//        } else {
//            var count = if (mData != null) mData!!.favoriteCount.toInt() else 0
//            if ("Y" == likeStatus) {
//                count++
//                buttonLike.isSelected = true
//            } else {
//                count--
//                buttonLike.isSelected = false
//            }
//            if (mData != null) {
//                mData?.favoriteCount = count.toString()
//                mData?.isFavorite = likeStatus
//                buttonLike?.text = mData?.favoriteCount
//                //drawCountText(buttonLike, mData?.favoriteCount)
//            }
//
//            if(mIsVisibleToUser) {
//                EventBus.getDefault().post(LikeStatusBus(likeStatus, count, if (playerFlag > 0) myVODPosition else mPosition, streamKey))   // 좋아요 status 갱신
//                EventBus.getDefault().post(NetworkBus(NetworkApi.API143.name, mData?.id)) // 현재 Video 정보 갱신
//            }
//        }
//    }

    /**
     * 조회수 등록 Response 처리
     */
    private fun handleNetworkResultViewCountADD(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val data: API10 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API10::class.java)
            if (data.status) {
                Logger.d("handleNetworkResultViewCountADD ${data.nTotalCount}")
                viewCount.text = StringUtils.getSnsStyleCountZeroBase(data.nTotalCount)

                EventBus.getDefault().post(ViewCountBus(data.nTotalCount, data.strStreamKey, if (playerFlag > 0) myVODPosition else mPosition))
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

    private fun init() {
        if (isMyUpload) { // 내가 올린 영상은 follow 정보 표시 하지 않는다.
            imageViewFollow?.setImageResource(0)
        }

//        if ("Y" == mData?.isFavorite) {
//            // 내가 좋아요 눌렀었다..
//            buttonLike.isSelected = true
//            likeStatus = "Y"
//        }

        if ("Y" == mData?.isScrap) {
            buttonScrap.isSelected = true
            scrapStatus = "Y"
        }

        initVolumeButton()

        replyButton.visibility = View.VISIBLE
        replyButton.setOnClickListener(clickListener)

        textViewNickName.text = mData?.userNick

        //drawCountText(viewCount, mData?.viewerCount)
        viewCount?.text = StringUtils.getSnsStyleCountZeroBase(mData?.viewerCount!!.toInt())
        //drawCountText(buttonLike, mData?.favoriteCount)
        buttonLike?.text = StringUtils.getSnsStyleCountZeroBase(likeCount)
        drawCountText(shareCount, mData?.shareCount)
        drawCountText(replyCount, mData?.comment_cnt)

        casterProfileImg = mData?.userImage!!

        if ("h" == mData?.strContentSize) {
            rotateBtn.setBackgroundResource(R.drawable.rotate2_btn)
        }

        // 첫번째 상품 이미지
        prodImage = if (mData != null && mData!!.relationPrd.data.size > 0) mData!!.relationPrd.data[mProdImageIndex].strPrdImg else ""
        if (prodImage.isNotEmpty()) {
            ImageLoad.setImage(context, buttonProducts, prodImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
        }

        // 동영상 프로필 이미지
        if (!TextUtils.isEmpty(casterProfileImg)) {
            ImageLoad.setImage(context, imageViewThumbnail, casterProfileImg, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
        }

        buttonExit.setOnClickListener(clickListener)
        buttonShare.setOnClickListener(clickListener)
        buttonStar.setOnClickListener(clickListener)
        buttonProducts.setOnClickListener(clickListener)
        buttonScrap.setOnClickListener(clickListener)
        buttonMore.setOnClickListener(clickListener)
        textViewBroadcastReport.setOnClickListener(clickListener)
        textViewNickNameModify.setOnClickListener(clickListener)
        textViewCustomerCenter.setOnClickListener(clickListener)
        textViewModifyTitle.setOnClickListener(clickListener)
        textViewPublic.setOnClickListener(clickListener)
        textViewShare.setOnClickListener(clickListener)
        textViewComment.setOnClickListener(clickListener)
        textViewVODDel.setOnClickListener(clickListener)
        buttonMute.setOnClickListener(clickListener)
        buttonRealTimeRank.setOnClickListener(clickListener)
        imageViewFollow?.setOnClickListener(clickListener)

        if (AppPreferences.getLoginStatus(context!!)) {
            likeButtonLayer.setOnClickListener(likeClickListener)
        } else {
            likeButtonLayer.setOnClickListener(clickListener)
        }

        if (isMyUpload) {
            layoutLive.visibility = View.GONE
            layoutVOD.visibility = View.VISIBLE
            buttonStar.visibility = View.GONE
            buttonScrap.visibility = View.GONE
            scrapCount.visibility = View.GONE
            buttonRealTimeRank.visibility = View.VISIBLE
        } else {
            layoutLive.visibility = View.VISIBLE
            layoutVOD.visibility = View.GONE
            buttonStar.visibility = View.VISIBLE
            buttonScrap.visibility = View.VISIBLE
            scrapCount.visibility = View.VISIBLE
            buttonRealTimeRank.visibility = View.GONE

            drawCountText(scrapCount, mData?.scrapCount)
        }

        rotateBtn.setOnClickListener(clickListener)
        ratioBtn.setOnClickListener(clickListener)
        resolutionBtn.setOnClickListener(clickListener)
    }

    private fun drawCountText(textView: AppCompatTextView, count: String?) {
        textView.visibility = View.GONE
        if (!count.isNullOrEmpty()) {
            var countText = StringUtils.getSnsStyleCount(count)
            if (!countText.isNullOrEmpty()) {
                textView.visibility = View.VISIBLE
                textView.text = countText
            } else {
                Logger.d("drawCountText: 0")
            }
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
        if (mPlayer != null) {
            if (AppPreferences.getVideoMute(context!!)) {
                AppPreferences.setVideoVolumn(context!!, mPlayer?.volume!!)
                mPlayer?.volume = 0f
            } else {
                mPlayer?.volume = AppPreferences.getVideoVolumn(context!!)
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()

        Logger.e("onLowMemory !!!!")
    }

    // 현재 보여지지 않는 화면일 경우 pause 시킨다.
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint:$isVisibleToUser  pos:$mPosition")

        mIsVisibleToUser = isVisibleToUser

        if (isVisibleToUser) {
            // follow 정보 갱신 ( onViewCreated 에서 API 전송 했다 )
            if (mIsFollowing) {
                imageViewFollow?.setImageResource(R.drawable.vod_following_ico)
            } else {
                imageViewFollow?.setImageResource(R.drawable.vod_follow_ico)
            }

            mHandler?.postDelayed({
                if (mIsVisibleToUser) {
                    playVideo()
                    startAnimation()
                }
            }, 500) // 계속 scroll 하는 경우 이전 영상 재생을 하지 않기 위해 delay 500 필요 ( 이전 영상을 재생하는 경우 UI Thread를 사용하기 때문에 스크롤 속도에 문제가 생긴다 )
        } else {
            pauseVideo()

            stopAnimation()
        }
    }

    private fun startAnimation() {
        if (mIsVisibleToUser && context != null) {
            // circle animation
            var ani = AnimationUtils.loadAnimation(context, R.anim.rotate)
            ani.duration = 7000
            buttonProducts?.startAnimation(ani)

//            var animation = pudding.background as AnimationDrawable
//            animation.start()

            mHandler?.sendEmptyMessageDelayed(MESSAGE_CHANGE_PRODUCT_IMAGE, 10000)

            // 상품 팝업을 띄운다.
            if (!isMyUpload) {
                if (!mShownProductPopup) {
                    mShownProductPopup = true
                    mHandler?.sendEmptyMessageDelayed(MESSAGE_SHOW_PRODUCT_POPUP, 3000)
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

    private fun setChatting(jsonObject: JSONObject) {
        try {
            var startTime = jsonObject.optLong("start_time")
            startTime *= 1000
            var jsonArray = jsonObject.optJSONArray("data")
            if (jsonArray != null) {
                var arrayLength = jsonArray.length()
                if (arrayLength > 0) {
                    chatArray = ArrayList()
                    var pArray = ArrayList<ChatModel>()

                    val duration = jsonArray.getJSONObject(arrayLength - 1).optLong("created") - startTime
                    val minutes = duration / 1000 / 60
                    for (i in 0..minutes)
                        chatArray!!.add(ArrayList<ChatModel>())

                    for (i in 0 until arrayLength) {
                        var subObject = jsonArray.getJSONObject(i)
                        var created = subObject.optLong("created")
                        var second = ((created - startTime) / 1000).toInt()
                        var data = subObject.optString("data")
                        var from = subObject.optString("from")
                        var id = ""
                        var nk = ""
                        var cmd = ""
                        var msg = ""
                        if (!TextUtils.isEmpty(from)) {
                            var fromObject = JSONObject(from)
                            id = fromObject.optString("id")
                            nk = fromObject.optString("nk")
                        }
                        if (!TextUtils.isEmpty(data)) {
                            var dataObject = JSONObject(data)
                            cmd = dataObject.optString("cmd")
                            msg = dataObject.optString("msg")
                        }
                        var model = ChatModel()
                        model.setSec(second)
                        model.setCmd(cmd)
                        model.setMsg(msg)
                        model.setId(id)
                        model.setNickName(nk)

                        chatArray!![second / 60].add(model)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 요청된 DataSource 에 근거하여 StreamingPlayer 를 초기화
     */
    private fun initVideo() {
        if (videoType == null || videoType == StreamingType.UNKNOWN) {
            Logger.e("unknown video type !!")
            return
        }

        if (mPlayer != null) {
            Logger.e("mPlayer not null !!")
            return
        }

        Logger.c("initVideo pos:$mPosition url:$videoUrl")
        Logger.e("mData?.videoType :: " + mData?.videoType + " chat log :: " + mData?.strChatlog)

        if (mData?.videoType == "LASTLIVE") {

            if (!TextUtils.isEmpty(VideoDBManager.getInstance(context!!).get(streamKey))) {
                val jsonObject = JSONObject(VideoDBManager.getInstance(context!!).get(streamKey))
                setChatting(jsonObject)

            } else {
                val path = activity!!.externalCacheDir.absolutePath

                DownloadFileAsync("$path/log.zip", object : DownloadFileAsync.PostDownload {
                    override fun downloadDone(file: File?) {
                        if (file != null && file.length() > 0) {
                            val logFile = FileUtils.unzip(file, path)
                            file.delete()

                            val text = FileInputStream(logFile).bufferedReader().use(BufferedReader::readText)
                            logFile.delete()

                            val jsonObject = JSONObject(text)
                            val document = MutableDocument(streamKey)
                            document.setString(streamKey, jsonObject.toString())

                            VideoDBManager.getInstance(context).put(document)
                            setChatting(jsonObject)
                        }
                    }
                }).execute(mData?.strChatlog)
            }
        }

        mIsInit = true

        mTrackSelector = DefaultTrackSelector()

        mPlayer = VideoManager.getExoPlayer(context!!, mTrackSelector!!)
        var mediaSource = VideoManager.getMediaSource(context!!, videoUrl!!)
        mMediaSource = LoopingMediaSource(mediaSource, 1) // 반복 재생
        mPlayer?.prepare(mMediaSource)
//        mPlayer?.addAnalyticsListener(EventLogger(mTrackSelector))

        if (AppPreferences.getVideoMute(context!!)) {
            mPlayer?.volume = 0f
        }

        mPlayer?.addListener(playerEventListener)
        mPlayer?.addVideoListener(videoListener)

        controlView?.showTimeoutMs = 0 // 항상 controller 표시
        controlView?.player = mPlayer

        if (playerView != null) {
            playerView?.player = mPlayer
            playerView?.useController = false

            playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            if ((mData?.videoType == "LASTLIVE" || mData?.videoType == "LIVE") && mData?.strContentSize == "v") {
                playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
            }

            if (mIsVisibleToUser) {
                playVideo()
            }
        }
    }

    // 영상 처음 부터 재생 한다.
    fun startVideo() {
        Logger.c("startVideo: $mPosition")

        mPlayer?.seekTo(0)
        //mPlayer?.playWhenReady = true
        //mPlayer?.playbackState
    }

    fun soundOn() {
        Logger.c("soundOn: $mPosition  volume:$mAudioVolume")
        //mPlayer?.volume = 1.0f
    }

    fun soundOff() {
        Logger.c("soundOff: $mPosition")

        //mAudioVolume = mPlayer?.volume!!
        //mPlayer?.volume = 0f
    }

    // ShoppingVideoFragment 에서 호출 한 경우
    private fun playVideoByMe() {
        if (mIsVisibleToUser) {
            playVideo()
        }
    }

    /**
     * 비디오 재생
     */
    fun playVideo() {
        if (!mIsVisibleToUser) {
            return
        }

        var ready = mPlayer?.playWhenReady
        if (ready != null && ready) {
            Logger.e("playVideo playing now!! $mPosition")
            return
        }

        isPaused = false

        if (!mIsInit) {
            initVideo()
        } else {
            Logger.c("playVideo: $mPosition")

            mPlayer?.playWhenReady = true
            mPlayer?.playbackState
        }

        // 비디오 플레이가 된 후에 10초가 지나면 조회수를 증가 시킨다.
        mHandler?.removeMessages(MESSAGE_VIEW_COUNT_ADD)
        mHandler?.sendEmptyMessageDelayed(MESSAGE_VIEW_COUNT_ADD, mHitDelayTime)

        showFullLayout()
    }

    /**
     * 비디오 일시정지
     */
    fun pauseVideo() {
        mHandler?.postDelayed({

            var ready = mPlayer?.playWhenReady
            if (ready != null && !ready) {
                Logger.e("pauseVideo false playWhenReady return!! $mPosition") // 플레이중이 아님
                return@postDelayed
            }

            // 조회수 증가 취소
            mHandler?.removeMessages(MESSAGE_VIEW_COUNT_ADD)

            Logger.c("pauseVideo: $mPosition")

            mPlayer?.playWhenReady = false
            isPaused = true
        }, 800) // 권한 요청후 pip진입시 중복 재생되는 이슈로 play때와 같이 800ms딜레이
    }

    /**
     * 비디오 정지
     */
    private fun releaseVideo() {
        Logger.c("releaseVideo: $mPosition")

        mPlayer?.playWhenReady = false
        mPlayer?.release()
        mPlayer = null
        mIsInit = false
    }

    private fun seekTo() {
        if (context != null) {
            var position = AppPreferences.getPipPosition(context!!)
            var key = AppPreferences.getPipKey(context!!)
            Logger.e("streamKey :: " + streamKey + " key :: " + key)
            if ( key == streamKey ) {
                if (position > 0) {
                    mPlayer?.seekTo(position)
                    Logger.e("seekTo: $mPosition")
                }
            } else {
                if ( mPlayer?.currentPosition!! <= 0L)
                    mPlayer?.seekTo(0)
            }

            AppPreferences.setPipPosition(context!!, 0)
            AppPreferences.setPipKey(context!!, "")
            Logger.e("pip key empty")
        }
    }

    private fun getVideoResolutionMap() {
        if (mVideoFormatMap.isNotEmpty()) {
            return
        }

        val mappedTrackInfo = mTrackSelector?.currentMappedTrackInfo
        val trackGroups = mappedTrackInfo?.getTrackGroups(0)
        if (trackGroups != null) {
            var width = 0
            for (i in 0 until trackGroups!!.length) {
                var group = trackGroups!![i]
                for (j in 0 until group.length) {
                    var format = group.getFormat(j)

                    if (width != format.width) {
                        width = format.width
                        Logger.d("resolution: ${buildTrackName(format)}")

                        var key = when (format.width > format.height) {
                            true -> format.height
                            else -> format.width
                        }

                        mVideoFormatMap[key] = format
                    }
                }
            }
        }
    }

    /**
     * ExoPlayer VideoListener
     */
    private val videoListener: VideoListener = object : VideoListener {
        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            Logger.e("onVideoSizeChanged width:$width height:$height position:$mPosition")

            if (context == null) {
                return
            }

            var width = if (width > height) height else width
            resolutionBtn?.text = when (width) {
                in 1..240 -> "저화질"
                in 241..480 -> "일반화질"
                in 481..720 -> "고화질"
                else -> "최고화질"
            }

            // VOD실행시 저화질로 셋팅되는 이슈로 주석처리 - ko kyung eun
//            if (mChangeResolution.isEmpty()) {
//                var status = NetworkStatusUtils.getNetworkStatus(NetworkUtils.getConnectivityStatus(context!!))
//                var resolution = when (status) {
//                    NetworkStatusUtils.NetworkStatus.MOBILE -> AppPreferences.getMobileVideoResolution(context!!)
//                    else -> AppPreferences.getWiFiVideoResolution(context!!)
//                }
//
//                var current = resolutionBtn?.text.toString()
//                Logger.e("current:$current  setting:$resolution")
//
//                if (!resolution!!.contains(current)) {
//                    changeResolution(resolution!!) // 설정과 같은 해상도는 변경할 필요 없다.
//                } else {
//                    mChangeResolution = resolution!!
//                }
//            }
        }

        override fun onRenderedFirstFrame() {
            Logger.d("onRenderedFirstFrame:$mPosition")

            progressBar?.visibility = View.GONE

            getVideoResolutionMap()

            simpleControlView?.max = 1000

            drawProgress()
        }

        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            //Logger.d("onSurfaceSizeChanged: width $width, height : $height")
        }
    }

    private val playerEventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Logger.d("onPlayerStateChanged $playWhenReady  state: $playbackState")  // playWhenReady == playing
            if (playWhenReady && playbackState == Player.STATE_ENDED) {
                if (possibleGoNext()) {
                    mPlayer?.playWhenReady = false
                    mPlayer?.seekTo(0)
                    val viewPager = (activity as ShoppingPlayerActivity).playListFragment!!.verticalViewpager
                    viewPager.setCurrentItem(++viewPager.currentItem, false)
                }
            }
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            Logger.d("onTracksChanged() $mPosition")
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            //Logger.d("onLoadingChanged() $mPosition")
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            Logger.d("onPlaybackParametersChanged() $mPosition")
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            Logger.d("onPlayerError() $mPosition")
        }

        override fun onPositionDiscontinuity(reason: Int) {
            Logger.d("onPositionDiscontinuity() $mPosition")
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            Logger.d("onRepeatModeChanged() $mPosition")
        }

        override fun onSeekProcessed() {
            Logger.d("onSeekProcessed() $mPosition")
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            Logger.d("onShuffleModeEnabledChanged() $mPosition")
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            Logger.d("onTimelineChanged() $mPosition")
        }
    }

    /**
     * 스트리밍 미디어 소스 변경 이벤트
     */
//    private val mediaSourceEventListener = object : MediaSourceEventListener {
//
//        override fun onLoadStarted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
//                                   loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
//                                   mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
//            Logger.d("onLoadStarted() $mPosition")
//        }
//
//        override fun onDownstreamFormatChanged(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
//                                               mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
//            Logger.d("onDownstreamFormatChanged() $mPosition")
//        }
//
//        override fun onUpstreamDiscarded(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
//                                         mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
//            Logger.d("onUpstreamDiscarded() $mPosition")
//        }
//
//        override fun onMediaPeriodCreated(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
//            Logger.d("onMediaPeriodCreated() $mPosition")
//        }
//
//        override fun onLoadCanceled(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
//                                    loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
//                                    mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
//            Logger.d("onLoadCanceled() $mPosition")
//        }
//
//        override fun onMediaPeriodReleased(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
//            Logger.d("onMediaPeriodReleased() $mPosition")
//        }
//
//        override fun onReadingStarted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
//            Logger.d("onReadingStarted()$mPosition $mPosition")
//        }
//
//        override fun onLoadCompleted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
//                                     loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
//                                     mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
//            //Logger.d("onLoadCompleted() $mPosition")
//        }
//
//        override fun onLoadError(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
//                                 loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
//                                 mediaLoadData: MediaSourceEventListener.MediaLoadData?,
//                                 error: IOException?, wasCanceled: Boolean) {
//            Logger.e("onLoadError() - $mPosition  $error")
//        }
//    }

    /**
     * PlayerView 에서 발생되는 TouchEvent 에 대한 처리
     */
    private val myGestureListener: GestureDetector.SimpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            // 꼭 있어야 한다.
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
                //Logger.e("up or down: ${Math.abs(e1.y - e2.y)} x:${Math.abs(velocityX)} y:${Math.abs(velocityY)}")
            } else {
                //Logger.e("left or right: ${Math.abs(e1.x - e2.x)} x:${Math.abs(velocityX)} y:${Math.abs(velocityY)}")
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
//            if (!isPaused && isInPictureInPictureMode) {
//                Logger.d("Floating video detached..")
//                pauseVideo()
//                activity?.finish()
//            }
        }

        override fun onViewAttachedToWindow(v: View?) {
//            if (isPaused || isInPictureInPictureMode) {
//                Logger.d("Floating video attached")
//                playVideo()
//            }
        }
    }

    /**
     * 액정 스크린 상태(켜짐 / 꺼짐 / 잠금해제) 관련 BroadcastReceiver
     */
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Logger.e("screenStateReceiver called")
            if (isInPictureInPictureMode) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> pauseVideo()
                    Intent.ACTION_SCREEN_ON,
                    Intent.ACTION_USER_PRESENT -> {
                        if (!keyGuardManager.inKeyguardRestrictedInputMode()) {
                            playVideo()
                        }
                    }
                }
            }
        }
    }

    private fun showCookieDialog() {
        cookieSendDialog = CookieSendDialog(activity!!,
                AppPreferences.getUserId(activity!!) ?: "", "test", remainedCookie).apply {
            setCancelable(true)
            setListener(cookieSendDialogListener)
            setOnDismissListener {
                cookieSendDialog = null
            }
            show()
        }
    }

    /**
     * 젤리 전송 팝업 이벤트 처리
     */
    private val cookieSendDialogListener = object : CookieSendDialog.Listener {
        override fun onCookieTransferred(sendId: String, receiveId: String, quantity: Int) {
            Logger.e("onCookieTransferred : $receiveId, $quantity")

            if (view?.context != null) {
                try {
                    cookieSendId = sendId
                    cookieReceiveId = receiveId
                    cookieCnt = quantity
                    Logger.e("cookieSendId : $cookieSendId, cookieReceiveId : $cookieReceiveId, cookieCnt : $quantity")

                    sendCookie(sendId, receiveId, quantity)
                } catch (e: Exception) {
                }
            }
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
        } else JSONObject(jsonStr)?.getString("nk")
    }

    private fun broadCastProductItem() {
        Logger.e("broadCastProductItem()")

        if ( !IS_TEST_PRODUCT_DIALOG ) {
            productListDialog?.dismiss()
            if (mData == null || mPlayer == null) {
                return
            }

            if (mData!!.relationPrd.data != null && mData!!.relationPrd.data.size > 0) {
                var modelArray = ArrayList<DialogModel>()
                var beanData = mData!!.relationPrd.data
                var streamKey = mData!!.id
                var vodType = mData!!.videoType
                val recommendId = mData!!.share_user
                if (beanData.isNotEmpty()) {
                    for (bean in beanData) {
                        //var bean = beanData.get(i)
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
                        dialogModel.streamKey = streamKey
                        dialogModel.vodType = vodType
                        dialogModel.recommendId = recommendId
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
                    var bus = VideoPipBus(videoUrl, mPlayer?.currentPosition, mData?.strContentSize, mPlayer?.volume!!, playerInfo)
                    productListDialog = PlayerProductDialog(view!!.context, modelArray, bus, false).apply { show() }

                    var task = ShopTreeAsyncTask(context!!)
                    task.sendProductLinkView(streamKey)
                }
            }
        } else {
            if (mData == null || mPlayer == null) {
                return
            }

            if (mData!!.relationPrd.data != null && mData!!.relationPrd.data.size > 0) {
                var modelArray = ArrayList<DialogModel>()
                var beanData = mData!!.relationPrd.data
                var streamKey = mData!!.id
                var vodType = mData!!.videoType
                val recommendId = mData!!.share_user
                if (beanData.isNotEmpty()) {
                    for (bean in beanData) {
                        //var bean = beanData.get(i)
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
                        dialogModel.streamKey = streamKey
                        dialogModel.vodType = vodType
                        dialogModel.recommendId = recommendId
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
                    var bus = VideoPipBus(videoUrl, mPlayer?.currentPosition, mData?.strContentSize, mPlayer?.volume!!, playerInfo)

                    productDialog = PlayerNewProductDialog()
                    var bundle = Bundle()
                    bundle.putParcelableArrayList("list", modelArray)
                    bundle.putSerializable("bus", bus!!)
                    bundle.putBoolean("isCast", false)
                    productDialog!!.arguments = bundle

                    var ft = fragmentManager!!.beginTransaction()
                    productDialog!!.show(ft, PlayerNewProductDialog.TAG)

                    var task = ShopTreeAsyncTask(context!!)
                    task.sendProductLinkView(streamKey)
                }
            }
        }
    }

    fun isProductDialogShowing() : Boolean {
        Logger.e("isProductDialogShowing")
        if ( productDialog != null && productDialog!!.dialog != null ) {
            Logger.e("isProductDialogShowing not null " + productDialog!!.dialog!!.isShowing)
            return productDialog!!.dialog!!.isShowing
        } else {
            Logger.e("isProductDialogShowing null")
            return false
        }
    }

    fun closeSubPop() {
        if ( productDialog != null ) {
            productDialog!!.closeSubPop()
        }
    }

    fun getPipInfo(): PipInfo? {
        if ( mPlayer == null)
            return null
        var pipInfo = PipInfo()
        pipInfo.videoUrl = videoUrl
        pipInfo.videoType = mData?.videoType
        pipInfo.currentPosition = mPlayer!!.currentPosition
        pipInfo.contentSize = mData?.strContentSize
        pipInfo.volume = mPlayer!!.volume
        pipInfo.title = mData?.title
        pipInfo.streamKey = streamKey
        var prdName = ""
        if ( mData?.relationPrd != null && mData?.relationPrd!!.data != null && mData?.relationPrd!!.data.size > 0 )
            prdName = mData?.relationPrd!!.data[0].strPrdName
        pipInfo.productName = prdName
        return pipInfo
    }

    private fun showShareDialog() {
        if (mData?.share_YN == "Y") {
            val view = layoutInflater.inflate(R.layout.dialog_share, null)
            view.buttonKakao.setOnClickListener(shareClickListener)
            view.buttonFacebook.setOnClickListener(shareClickListener)
            view.buttonSourceCode.setOnClickListener(shareClickListener)
            view.buttonLink.setOnClickListener(shareClickListener)
            view.buttonMore.setOnClickListener(shareClickListener)

            sDialog = BottomSheetDialog(view!!.context, R.style.AppBottomSheetDialogTheme)
            sDialog!!.setContentView(view)
            sDialog!!.show()
        } else {
            AppToast(context!!).showToastMessage(R.string.deny_video_share,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

//    private fun handleReply() {
//        var streamKey = ""
//        streamKey = if (mData != null) mData!!.id else ""
//
//        replyDialog = ReplyDialog(view!!.context, activity!!, streamKey).apply {
//            setListener(this@ShoppingVideoFragment)
//            show()
//        }
//    }

    private fun showNickNameModifyDialog() {
        nickNameDialog = NickNameModifyDialog(view!!.context, object : NickNameModifyDialog.Listener {
            override fun onConfirm(nickName: String) {
                chatNickName = nickName
            }
        }).apply { show() }
    }

    /**
     * 방송중 상품 팝업을 표시
     * @param castId
     */
    private fun showProductDialog() {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager!!.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        var dialog = CasterProductDialog()
        var bundle = Bundle()
        bundle.putInt("screenHeight", screenHeight)
        bundle.putString("casterId", AppPreferences.getUserId(context!!)!!)
        bundle.putString("streamKey", streamKey)

        dialog.arguments = bundle
        dialog.show(childFragmentManager, CasterProductDialog::class.java.name)
    }

    /**
     * 채팅 레이아웃 변경 이벤트 처리
     */
    private val chatInputChangeListener = View.OnLayoutChangeListener { v, _, top, _, _, _, oldTop, _, _ ->
        var currentY = top
        var oldY = oldTop

//        if (currentY != oldY) {
//            inlive_layoutChatInput.postDelayed({
//                if (inlive_layoutChatInput != null && inlive_editTextChat != null) {
//                    try {
//                        var posYDiff = noChatLayoutYPos - inlive_layoutChatInput.y
//                        var isFocused = inlive_editTextChat.isFocused
//                        Logger.e("ChatInput Position Changed : Diff Y($posYDiff), isUIFocus ? $isFocused")
//
//                        if (posYDiff < chatInputPositionOffset) {
//                            inlive_layoutChatInput.visibility = View.GONE
//                            inlive_layoutChatSpace.visibility = View.VISIBLE
//                        }
//                    } catch (ignore: Exception) {
//                    }
//                }
//            }, SOFT_KEYBOARD_TRANSITION_WAIT_DURATION)
//        }
    }

    /**
     * VOD 좋아요 등록
     */
    private fun likeVOD(status: String) {
        var streamKey = ""
        var userId = ""
        var videoType = ""

        streamKey = if (mData != null) mData!!.id else ""
        userId = AppPreferences.getUserId(context!!)!!
        videoType = if (mData != null) mData!!.videoType else ""

        var requestObj = ApiParams.FavorParams(
                userId,
                videoType,
                status)

        var bus = NetworkBus(NetworkApi.API8.name, streamKey, requestObj)
        EventBus.getDefault().post(bus)
    }

    /**
     * 조회수 등록
     */
    private fun viewCountADD() {
        var streamKey = ""
        var videoType = ""
        if (mData != null) {
            streamKey = mData!!.id
            videoType = mData!!.videoType
        }

        if (AppPreferences.getLoginStatus(view!!.context) && mIsVisibleToUser) {
            var obj = ApiParams.HitsParams(AppPreferences.getUserId(context!!)!!, streamKey, videoType, "Y")
            EventBus.getDefault().post(NetworkBus(NetworkApi.API10.name, streamKey, obj))
        } else {
            var obj = ApiParams.HitsParams(Utils.getUniqueID(context!!), streamKey, videoType, "Y")
            EventBus.getDefault().post(NetworkBus(NetworkApi.API10.name, streamKey, obj))
        }
    }

    /**
     * 조회수를 요청한다.
     */
    private fun getViewCount() {
        var bus = NetworkBus(NetworkApi.API9.name, if (mData != null) mData!!.id else "")

        EventBus.getDefault().post(bus)
    }

    private fun handleScrap(status: String) {
        Logger.d("handleScrap:$status")
        if ("Y" == status) {
            val task = ShopTreeAsyncTask(context!!)
            task.deletetScrap(mData?.id) { result, obj ->
                if (result) {
                    var resultObj = obj as JSONObject
                    var rt = resultObj.optString("result")
                    Logger.e("scrap delete rt :: " + rt)
                    if ("success" == rt) {
                        try {
                            AppToast.cancelAllToast()
                            handleScrapUI()
                        } catch (e: Exception) {
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
                if (result) {
                    var resultObj = obj as JSONObject
                    var rt = resultObj.optString("result")
                    Logger.e("scrap rt :: " + rt)
                    if ("success" == rt) {
                        try {
                            AppToast.cancelAllToast()
                            handleScrapUI()
                        } catch (e: Exception) {
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

    private fun setMoreUI() {
        if (mData!!.user_show_YN == "Y") {
            textViewPublic.text = "영상 비공개"
        } else {
            textViewPublic.text = "영상 공개"
        }

        if (mData!!.share_YN == "Y") {
            textViewShare.text = "공유 비허용"
        } else {
            textViewShare.text = "공유 허용"
        }

        if (mData!!.comment_YN == "Y") {
            textViewComment.text = "댓글 비허용"
        } else {
            textViewComment.text = "댓글 허용"
        }
    }

    private fun handleVODOpen() {
        var requestObject = JSONObject()
        requestObject.put("streamKey", mData!!.id)
        requestObject.put("type", mData!!.videoType)
        requestObject.put("val", if (textViewPublic.text == "영상 공개") "Y" else "N")
        requestObject.put("user", AppPreferences.getUserId(context!!))

        var body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObject.toString())
        var bus = NetworkBus(NetworkApi.API97.name, body)

        EventBus.getDefault().post(bus)
    }

    private fun handleVODShare() {
        var requestObject = JSONObject()
        requestObject.put("streamKey", mData!!.id)
        requestObject.put("type", mData!!.videoType)
        requestObject.put("val", if (textViewShare.text == "공유 허용") "Y" else "N")
        requestObject.put("user", AppPreferences.getUserId(context!!))

        var body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObject.toString())
        var bus = NetworkBus(NetworkApi.API96.name, body)

        EventBus.getDefault().post(bus)
    }

    private fun handleVODComment() {
        var requestObject = JSONObject()
        requestObject.put("streamKey", mData!!.id)
        requestObject.put("type", mData!!.videoType)
        requestObject.put("val", if (textViewComment.text == "댓글 허용") "Y" else "N")
        requestObject.put("user", AppPreferences.getUserId(context!!))

        var body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObject.toString())
        var bus = NetworkBus(NetworkApi.API95.name, body)

        EventBus.getDefault().post(bus)
    }

    /**
     * 사용자 영상 삭제
     */
    private fun deleteVOD() {
        EventBus.getDefault().post(NetworkBus(NetworkApi.API87.name, mData!!.id))
    }

    private fun sendCookie(sendId: String, receiveId: String, quantity: Int) {
        var streamKey = ""
        var videoType = ""

        if (mData != null) {
            streamKey = mData!!.id
            videoType = mData!!.videoType
        }

        val requestObj = JSONObject()
        requestObj.put("streamKey", streamKey)
        requestObj.put("vod_type", videoType)
        requestObj.put("cnt", quantity.toString())
        requestObj.put("user", sendId)

        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString())
        val task = ShopTreeAsyncTask(context!!)
        task.postCookie(body,
                { result, obj ->
                    try {
                        showCookieAnimation(cookieCnt)
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                })
    }

    /**
     * 스크랩 UI 처리
     */
    private fun handleScrapUI() {
        Logger.e("handleScrapUI")
        var count = if (mData != null) mData!!.scrapCount.toInt() else 0
        if ("Y" == scrapStatus)
            scrapStatus = "N"
        else
            scrapStatus = "Y"
        if ("Y" == scrapStatus) {

            buttonScrap.isSelected = true
            count++
            Logger.e("where count ++ " + count)
            var view: View = LayoutInflater.from(context).inflate(R.layout.popup_scrap_on, null)
            AppToast(context!!).showToastAtView(view, AppToast.DURATION_MILLISECONDS_FAST, AppToast.GRAVITY_MIDDLE)
        } else {
            buttonScrap.isSelected = false
            count--
            Logger.e("where count -- " + count)
            var view: View = LayoutInflater.from(context).inflate(R.layout.popup_scrap_off, null)
            AppToast(context!!).showToastAtView(view, AppToast.DURATION_MILLISECONDS_FAST, AppToast.GRAVITY_MIDDLE)
        }
        if (mData != null) {
            mData?.scrapCount = count.toString()
            mData?.isScrap = scrapStatus
            drawCountText(scrapCount, mData?.scrapCount)
        }

        EventBus.getDefault().post(NetworkBus(NetworkApi.API143.name, mData?.id)) // 현재 Video 정보 갱신
    }

    /**
     * 좋아요 버튼 클릭 이벤트 처리(연타 허용)
     */
    private val likeClickListener = View.OnClickListener {
        when (it?.id) {
            R.id.likeButtonLayer -> {
                mHandler?.sendEmptyMessageDelayed(MESSAGE_SHOW_HEART_VIEW, 100) // 0.3초 동안 모아서 1번만 그리자!

                likeCount++
                increaseLikeCount++
                buttonLike.text = StringUtils.getSnsStyleCountZeroBase(likeCount)

                mData!!.favoriteCount = "$likeCount"
                if (shareKey.isEmpty()) {
                    (activity as ShoppingPlayerActivity).mVideoData.set(mPosition, mData!!)
                }

                var model = LikeModel()
                model.streamKey = mData!!.id
                model.increaseLikeCount = increaseLikeCount
                model.likeCount = likeCount
                model.category = mData!!.categoryCode
                Logger.e("mData!!. category :: " + mData!!.categoryCode)
                (activity as ShoppingPlayerActivity).setLike(model)
//                likeStatus = "Y"
//                if (it.isSelected) {
//                    likeStatus = "N"
//                }
//
//                likeVOD(likeStatus)
            }
        }
    }

    /**
     * 버튼 클릭 이벤트 처리
     */
    private val clickListener: OnSingleClickListener = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            mHandler?.removeMessages(MESSAGE_HIDE_UI)
            mHandler?.sendEmptyMessageDelayed(MESSAGE_HIDE_UI, mHideLayoutDelayTime)

            Logger.e("onClick: $v?")

            layoutMore?.visibility = View.GONE

            when (v?.id) {
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
                                            mIsFollowing = !mIsFollowing
                                            if (mIsFollowing) {
                                                imageViewFollow?.setImageResource(R.drawable.vod_following_ico)
                                            } else {
                                                imageViewFollow?.setImageResource(R.drawable.vod_follow_ico)
                                            }
                                        } catch (e: Exception) {
                                            Logger.p(e)
                                        }
                                    })
                        }
                    } else {
                        startActivityForResult(Intent(v.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                    }
                }

                R.id.replyButton -> {
                    if (!AppPreferences.getLoginStatus(v.context)) {
                        startActivityForResult(Intent(v.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                        return
                    }

                    showReplyDialog()

                    /*if ("N" == mData?.comment_YN) {
                        AppToast(context!!).showToastMessage(R.string.deny_video_comment,
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM)
                    } else {
                        showReplyDialog()
                    }*/
                }

                R.id.clickLayout -> showFullLayoutAnimation()
                //R.id.clickHideLayout -> hideFullLayout()

                R.id.buttonExit -> {
                    (activity as? ShoppingPlayerActivity)?.exitClicked()
                }

                R.id.productPopLayout -> {
                    v.visibility = View.GONE
                    broadCastProductItem()
                }

                R.id.buttonProducts -> broadCastProductItem()

                R.id.buttonStar -> {
                    if (!AppPreferences.getLoginStatus(v.context)) {
                        startActivityForResult(Intent(v.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                        return
                    }

                    showCookieDialog()
                }

                R.id.buttonShare -> {
                    if (!AppPreferences.getLoginStatus(v.context)) {
                        startActivityForResult(Intent(v.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                        return
                    }

                    showShareDialog()
                }

                R.id.buttonViewerList -> {
                    userListDialog?.dismiss()
                    userListDialog = null
                    userListDialog = LiveUserListDialog(view!!.context).apply {
                        setItems(chatUserIds)
                        show()
                    }
                }

                R.id.likeButtonLayer -> {
                    mHandler?.sendEmptyMessageDelayed(MESSAGE_SHOW_HEART_VIEW, 100) // 0.3초 동안 모아서 1번만 그리자!
                    startActivityForResult(Intent(v.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                }

                R.id.buttonMute -> {
                    postMuteBus()
                }

                R.id.buttonScrap -> {
                    if (!AppPreferences.getLoginStatus(v.context)) {
                        startActivityForResult(Intent(v.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                        return
                    }
                    var videoType = mData?.videoType
                    (activity as ShoppingPlayerActivity).scrapClicked(streamKey, videoType!!, scrapStatus)
//                handleScrap(scrapStatus)


//                if (it.isSelected) {
//                    it.isSelected = false
//                    scrapStatus = "N"
//                } else {
//                    it.isSelected = true
//                    scrapStatus = "Y"
//                }
//                handleScrap(scrapStatus)
                }

                R.id.buttonMore -> {
                    mHandler?.removeMessages(MESSAGE_HIDE_UI)
                    if (layoutMore?.visibility == View.VISIBLE) {
                        layoutMore?.visibility = View.GONE
                    } else {
                        layoutMore?.visibility = View.VISIBLE
                        setMoreUI()
                    }
                }

                R.id.textViewNickNameModify -> {
                    if (!AppPreferences.getLoginStatus(v.context)) {
                        startActivityForResult(Intent(v.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                        return
                    }

                    layoutMore.visibility = View.GONE
                    showNickNameModifyDialog()
                }

                R.id.textViewBroadcastReport -> {
                    if (!AppPreferences.getLoginStatus(v.context)) {
                        startActivityForResult(Intent(v.context, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN)
                        return
                    }

                    layoutMore.visibility = View.GONE
                    startActivity(Intent(view!!.context, BroadcastReportActivity::class.java).apply {
                        putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_REPORT_TYPE, false)
                        putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_USER_ID, mData?.userId)
                        putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_STREAM_KEY, mData?.id)
                        putExtra(BroadcastReportActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, mData?.videoType)
                    })
                }

                R.id.textViewCustomerCenter -> {
                    layoutMore.visibility = View.GONE
                    startActivity(Intent(view!!.context, CustomerCenterMainActivity::class.java))
                }

                R.id.textViewModifyTitle -> {
                    videoTitleModifyDialog?.dismiss()
                    videoTitleModifyDialog = null
                    videoTitleModifyDialog = CasterTitleModifyDialog(view!!.context, mData?.title!!).apply {
                        setListener(this@ShoppingVideoFragment)
                        show()
                    }
                }

                R.id.textViewPublic -> handleVODOpen()

                R.id.textViewVODDel -> deleteVOD()

                R.id.textViewShare -> handleVODShare()

                R.id.textViewComment -> handleVODComment()

                R.id.rotateBtn -> {
                    if (rotateLayout.angle == 270) {
                        rotateLayout.angle = 0
                        rotateBtn.setBackgroundResource(R.drawable.rotate2_btn)
                    } else {
                        rotateLayout.angle = 270
                        rotateBtn.setBackgroundResource(R.drawable.rotate_btn)
                    }
                }

                R.id.ratioBtn -> {
                }

                R.id.resolutionBtn -> {
                    var currentResolution = resolutionBtn.text
                    var selectedIndex = 0
                    var index = 0
                    var items: MutableList<String> = ArrayList()
                    var treeMap = TreeMap<Int, Format>(Collections.reverseOrder())
                    treeMap.putAll(mVideoFormatMap)
                    var iteratorKey = treeMap.keys.iterator()
                    var prevResolution = ""
                    while (iteratorKey.hasNext()) {
                        var txt = when (iteratorKey.next()) {
                            in 1..240 -> "저화질(240p)"
                            in 241..480 -> "일반화질(480p)"
                            in 481..720 -> "고화질(720p)"
                            else -> "최고화질(원본)"
                        }
                        if (prevResolution != txt) {
                            prevResolution = txt // 동일 해상도가 들어가는 경우 제외

                            items.add(txt)
                            if (txt.contains(currentResolution)) {
                                selectedIndex = index //처음 Dialog List 위치를 구한다.
                            }
                            index++
                        }
                    }

                    if (items.size > 0) {
                        ShoppingVideoCheckDialog(context!!, ShoppingVideoCheckDialog.MODE_RESOLUTION).apply {
                            setListener(this@ShoppingVideoFragment)
                            setData(items, selectedIndex)
                            show()
                        }
                    }
                }

                R.id.buttonRealTimeRank -> showProductDialog()
            }
        }
    }

    private fun buildTrackName(format: Format): String {
        var sb = StringBuilder()
        sb.append(format.width.toString() + "x" + format.height)
        sb.append(" " + String.format(Locale.US, "%.2fM", format.bitrate / 1000000f))
        return sb.toString()
    }

    override fun onSelected(mode: Int, position: Int, data: String) {
        Logger.e("onSelected mode:$mode  position:$position  data:$data")
        if (mode == ShoppingVideoCheckDialog.MODE_RESOLUTION) {
            changeResolution(data)
            mChangeResolution = data
        } else {

        }
    }

    // 근사치 값을 구한다.
    private fun nearNumber(target: Int, map: MutableMap<Int, Format>): Int {
        var near = 0 //가까운값을 저장할 변수
        var min = Integer.MAX_VALUE
        for (k in map.keys) {
            var a = Math.abs(k - target)
            if (min > a) {
                min = a
                near = k
            }
        }
        return near
    }

    private fun changeResolution(data: String) {
        if (mTrackSelector == null) {
            Logger.e("mTrackSelector null")
            return
        }

        Logger.e("changeResolution:$data  position:$mPosition")

        var key = when (data) {
            "저화질(240p)" -> 240
            "일반화질(480p)" -> 480
            "고화질(720p)" -> 720
            "최고화질(원본)" -> 1080
            else -> 240
        }

        getVideoResolutionMap()

        if (mVideoFormatMap != null) {
            var format: Format? = null

            if (mVideoFormatMap.containsKey(key)) {
                format = mVideoFormatMap[key]!!
            } else {
                var near = nearNumber(key, mVideoFormatMap)
                Logger.d("near key $near")
                if (mVideoFormatMap.containsKey(near)) {
                    format = mVideoFormatMap[near]!!
                }
            }

            val mappedTrackInfo = mTrackSelector?.currentMappedTrackInfo
            val trackGroups = mappedTrackInfo?.getTrackGroups(0)

            var params = mTrackSelector!!.parameters.buildUpon()
//            params.setMaxVideoSize(format!!.width, format!!.height)
            params.setSelectionOverride(0, trackGroups, DefaultTrackSelector.SelectionOverride(0, trackGroups!![0].indexOf(format)))
            mTrackSelector!!.setParameters(params)
            Logger.d("changed resolution: ${format!!.width} x ${format!!.height}")
        }
    }

    private fun showReplyDialog() {
        Logger.i("showReplyDialog()")
        if (mIsVisibleToUser) {
            videoReplyDialog = VideoReplyDialog()
            if (videoReplyDialog != null) {
                videoReplyDialog!!.apply {
                    arguments = Bundle().apply {
                        putString("stream", mData?.id)
                        putString("comment_YN", mData?.comment_YN)
                        putString("creatorNickname", mData?.userNick)
                    }
                }.show(fragmentManager!!, "dialog")
            }
        }
    }

//    private fun showReplyDialog() {
//        Logger.i("showReplyDialog()")
//        if (mIsVisibleToUser) {
//
////            var reply = VideoReplyDialog().apply {
////                arguments = Bundle().apply { putString("stream", mData?.id) }
////            }
//
//
//            //activity!!.supportFragmentManager.beginTransaction().add(R.id.videoFullLayout, reply, "reply").commit()
//
//            fragmentManager!!.beginTransaction().add(videoFullLayout.id, VideoReplyDialog()).commit()
//
//            //transaction.add(layoutContainer.id, allQnaFragment!!, allQnaFragment!!::class.java.name)
//        }
//    }

    private val myTouchListener = View.OnTouchListener { p0, event -> mGestureDetector.onTouchEvent(event) }

    /**
     * 공유 다이얼로그 클릭 이벤트 처리
     */
    private val shareClickListener: View.OnClickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonKakao -> {
                Logger.d("Kakao Button Clicked.")
                share(TITLE_KAKAO)
            }

            R.id.buttonFacebook -> {
                Logger.d("Facebook Button Clicked.")
                share(TITLE_FACEBOOK)
            }

            R.id.buttonSourceCode -> {
                Logger.d("SourceCode Button Clicked.")
                share(TITLE_SOURCECODE)
            }

            R.id.buttonLink -> {
                Logger.d("Link Button Clicked.")
                makeShareLink(TITLE_LINK)
            }

            R.id.buttonMore -> {
                Logger.d("More Button Clicked.")
                share(TITLE_OTHER)
            }
        }
    }

    private fun getVodType(type: String): String {
        if (TextUtils.isEmpty(type)) {
            return ""
        }

        return type.toLowerCase()
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
        mShareType = space
        Logger.e("mShareType :: $mShareType")
        Logger.e("streamKey :: $streamKey")
        Logger.e("uploader :: $uploader")
        Logger.e("space :: $space")
        Logger.e("vodType :: $vodType")
        var type = ""
        if (vodType.toLowerCase() == "lastlive")
            type = "live"
        else
            type = vodType
//        var builder = MultipartBody.Builder()
//        builder.setType(MultipartBody.FORM)
//        builder.addFormDataPart("streamKey", streamKey)
//        builder.addFormDataPart("uploader", uploader)
//        builder.addFormDataPart("shareId", AppPreferences.getUserId(context!!))
//        builder.addFormDataPart("space", space)
//        builder.addFormDataPart("vodType", type)
//        var body = builder.build()
//
//        EventBus.getDefault().post(NetworkBus(NetworkApi.API48.name, body))

        var task = ShopTreeAsyncTask(context)
        task.makeShareLink(streamKey, uploader, AppPreferences.getUserId(context!!), space, type, object : ShopTreeAsyncTask.OnDefaultObjectCallbackListener {
            override fun onResponse(result: Boolean, obj: Any?) {
                Logger.e("result :: " + result)
                if (result) {
                    var objet = obj as JSONObject
                    if ("success".equals(obj.optString("result").toLowerCase())) {
                        var link = obj.optString("url")
                        Logger.e("shareLink :: $link")
                        if (link.isNotEmpty()) {
                            when (mShareType) {
                                TITLE_KAKAO -> shareKakao(link)
                                TITLE_FACEBOOK -> shareFacebook(link)
                                TITLE_MESSENGER -> shareFacebookMessenger(link)
                                TITLE_LINK -> setClipBoardLink(link)
                                TITLE_OTHER -> shareEtc(link)
                                TITLE_SOURCECODE -> setSourceCode(link)
                            }

                            EventBus.getDefault().post(NetworkBus(NetworkApi.API143.name, mData?.id))
                        }
                    }
                }
            }
        })
    }

    private fun hideFullLayout() {
        if (mStartedMoveDownAnimation || !mIsVisibleToUser) {
            Logger.d("hideFullLayout return")
            return
        }

        Logger.d("hideFullLayout")

        if (layoutMore?.visibility == View.VISIBLE) {
            layoutMore?.visibility = View.GONE
            return
        }

        mStartedMoveDownAnimation = true

        simpleControlView?.visibility = View.GONE

        stopAnimation()
        buttonProducts?.visibility = View.GONE
        dark_bg?.visibility = View.GONE
        dark_bg1?.visibility = View.GONE
        vodLayer?.visibility = View.INVISIBLE
        likeButtonLayer?.visibility = View.INVISIBLE
        clickLayout?.visibility = View.VISIBLE
        heartView.visibility = View.GONE
        recyclerViewVODChat.visibility = View.GONE

        if (layoutChatInput != null) {
            controlView?.showTimeoutMs = 0 // 항상 controller 표시
            controlView?.player = mPlayer
            controlView?.show()

            layoutChatInput?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.move_bottom))
            layoutChatInput?.postDelayed({
                layoutChatInput?.visibility = View.INVISIBLE
                heartView?.visibility = View.GONE
                recyclerViewVODChat?.visibility = View.GONE

                if (mIsVisibleToUser) {
                    if (isMyUpload) {
                        buttonStar?.visibility = View.GONE
                    } else {
                        buttonStar?.visibility = View.VISIBLE
                    }
                }

                mHandler?.removeMessages(MESSAGE_HIDE_UI)

                mStartedMoveDownAnimation = false
            }, 500)
        }
    }

    private fun showFullLayout() {
        Logger.d("showFullLayout")
        mHandler?.sendEmptyMessageDelayed(MESSAGE_HIDE_UI, mHideLayoutDelayTime)

        layoutChatInput?.visibility = View.VISIBLE
        vodLayer?.visibility = View.VISIBLE
        clickLayout?.visibility = View.INVISIBLE
        buttonProducts?.visibility = View.VISIBLE
        dark_bg?.visibility = View.VISIBLE
        dark_bg1?.visibility = View.VISIBLE
        heartView.visibility = View.VISIBLE
        recyclerViewVODChat.visibility = View.VISIBLE
        //controlView?.hide()
    }

    private fun showFullLayoutAnimation() {
        if (mStartedMoveUpAnimation || !mIsVisibleToUser) {
            return
        }
        mStartedMoveUpAnimation = true

        Logger.d("showFullLayoutAnimation")
        mHandler?.sendEmptyMessageDelayed(MESSAGE_HIDE_UI, mHideLayoutDelayTime)

        if (layoutChatInput != null) {
            layoutChatInput?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.move_up))
            layoutChatInput?.postDelayed({
                layoutChatInput?.visibility = View.VISIBLE
                vodLayer?.visibility = View.VISIBLE
                clickLayout?.visibility = View.INVISIBLE
                buttonProducts?.visibility = View.VISIBLE
                dark_bg?.visibility = View.VISIBLE
                dark_bg1?.visibility = View.VISIBLE
                likeButtonLayer?.visibility = View.VISIBLE
                //controlView?.hide()
                simpleControlView?.visibility = View.VISIBLE
                heartView?.visibility = View.VISIBLE
                recyclerViewVODChat?.visibility = View.VISIBLE

                startAnimation()
                mStartedMoveUpAnimation = false
            }, 300)
        }
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

            AppConstants.MY_UPLOAD_VOD_PLAYER -> {
                mData = (activity as ShoppingPlayerActivity).mVideoData[myVODPosition]
                (activity as ShoppingPlayerActivity).selectedData = mData
                isMyUpload = true
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

    private fun showCookieAnimation(count: Int) {
        var gif = when (count) {
//            in 0..49 -> GifDrawable(resources, R.raw.cookie1)
//            in 50..99 -> GifDrawable(resources, R.raw.cookie2)
//            in 100..299 -> GifDrawable(resources, R.raw.cookie3)
//            in 300..499 -> GifDrawable(resources, R.raw.cookie4)
//            in 500..999 -> GifDrawable(resources, R.raw.cookie5)
//            else -> GifDrawable(resources, R.raw.cookie6)

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

        sender.text = "$mMyNick(${AppPreferences.getUserId(context!!)!!})"

        var str = "젤리 ${Utils.ToNumFormat(count)}개 선물"
        var font = R.font.notosanskr_bold
        var ssb = SpannableString(str)
        var typeface = ResourcesCompat.getFont(context!!, font)
        ssb.setSpan(StyleSpan(typeface!!.style), 2, str.length - 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(Color.parseColor("#9f56f2")), 2, str.length - 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        cookieStr.text = ssb

        cookieLayer.visibility = View.VISIBLE
    }

    private fun hideCookie() {
        mHandler?.post({
            if (cookieLayer != null) {
                cookieLayer.visibility = View.GONE

                if (cookieImg != null) {
                    try {
                        (cookieImg.drawable as GifDrawable).recycle()
                    } catch (e: Exception) {
                    }
                }
            }
        })
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

    private fun showProductPopup() {
        if (context == null || !mIsVisibleToUser || mData!!.relationPrd.data.size == 0) {
            Logger.e("showProductPopup fail")
            return
        }

        productPopLayout?.visibility = View.VISIBLE
        productPopLayout?.setOnClickListener(clickListener)

        var data = if (mData != null && mData!!.relationPrd.data.size > 0) mData!!.relationPrd.data[0] else null
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
            Glide.with(context!!).setDefaultRequestOptions(RequestOptions().circleCrop()).load(data.strPrdImg).listener(object : RequestListener<Drawable> {
                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
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

    private fun drawProgress() {
        try {
            var duration = mPlayer?.duration
            var currentPosition = mPlayer?.currentPosition

            if (!TextUtils.isEmpty(mData?.strChatlog) && mData?.videoType == "LASTLIVE" && chatArray != null && chatArray!!.size > 0) {
                try {
                    var currentSecond = (currentPosition!! / 1000).toInt()
                    if (previousSecond != currentSecond) {
                        // 현재 재생시간과 이전 재생시간의 차이가 1초가 아닌경우, 즉, seek 되어 이전 혹은 이후로 수 초가 변경된 경우
                        if (currentSecond - previousSecond != 1) {
                            chatAdapter!!.clearItem()
                            var value = (currentSecond / 60).toInt() //현재 시간을 가지고 chatArray의 몇번째 어레이 값을 사용할지 계산
                            if (chatArray!!.size > value) {
                                var arr = chatArray!!.get(value)
                                for (i in 0 until arr.size) {
                                    var model = arr.get(i)
                                    //현재 영상의 재생시간(currentSecond)과 채팅 내용의 초(model.getSec())가 같을 경우 해당 채팅내용들을 add
                                    if (model != null) {
                                        if (model.getSec() == currentSecond) {
                                            if (model.getCmd() == ChatManager.HEART_CHAT_CMD) {
                                                heartCount++
                                            } else {
                                                chatAdapter!!.addItem(LastLiveChatData(model.getCmd(), model.getId(), model.getNickName(), model.getMsg(), model.getSec()!!.toLong()))
                                                recyclerViewVODChat.scrollToPosition(chatAdapter!!.getItemCount() - 1)
                                            }
                                        } else {
                                            if (heartCount > 0 && !isDrawHeartViewRun && heartView.visibility == View.VISIBLE) {
                                                showHeartView(heartCount)
                                                heartCount = 0
                                            }
                                        }
                                    }
                                }
                            } else {
                                Logger.e("value is same or over chatArray size")
                            }
                        } else {
                            // SEEK 안한 상태
                            var value = (currentSecond / 60).toInt()
                            if (chatArray!!.size > value) {
                                var arr = chatArray!!.get(value)
                                //해당 분에 해당하는 어레이 값을 가져와서
                                for (i in 0 until arr.size) {
                                    var model = arr.get(i)
                                    if (model != null) {
                                        // 해당 초에 대한 값인 경우
                                        if (model.getSec() == currentSecond) {
                                            if (model.getCmd() == ChatManager.HEART_CHAT_CMD) {
                                                heartCount++
                                            } else {
                                                chatAdapter!!.addItem(LastLiveChatData(model.getCmd(), model.getId(), model.getNickName(), model.getMsg(), model.getSec()!!.toLong()))
                                                recyclerViewVODChat.scrollToPosition(chatAdapter!!.getItemCount() - 1)
                                            }
                                        } else {
                                            if (heartCount > 0 && !isDrawHeartViewRun && heartView.visibility == View.VISIBLE) {
                                                showHeartView(heartCount)
                                                heartCount = 0
                                            }
                                        }
                                    }
                                }
                            } else {
                                Logger.e("value is same or over chatArray size")
                            }
                        }
                        previousSecond = currentSecond
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


            if (duration!! > 0 && currentPosition!! > 0) {
                var progress = currentPosition.times(1000).div(duration).toInt()
//                Logger.e("progress: $progress"  )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    simpleControlView?.setProgress(progress, true)
                } else {
                    simpleControlView?.progress = progress
                }
            }
        } catch (e: Exception) {
        }

        mHandler?.removeMessages(MESSAGE_PROGRESS)
        mHandler?.sendEmptyMessageDelayed(MESSAGE_PROGRESS, 100)
    }

    // login 되어 있는 경우만 db 저장
    private fun saveData() {
        if (AppPreferences.getLoginStatus(context!!) && mData != null) {
            var json = Gson().toJson(mData)
            Logger.e("saveData: $json")

            var document = MutableDocument(mData!!.id)
            document.setString(mData!!.id, json)
            VideoDBManager.getInstance(context).put(document)
        }
    }

    private fun drawHeartView() {
        mHandler?.removeMessages(MESSAGE_SHOW_HEART_VIEW)
        if (context != null && isAdded) {
            mHandler?.post { HeartView(context, heartView) }
        }
    }

    public fun initIncreaseLikeCount() {
        increaseLikeCount = 0
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(context!!).registerReceiver(receiver, IntentFilter("likeChange"))
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
            if (action == "likeChange") {
                increaseLikeCount = 0
                var obj = intent.getStringExtra("change")
                var arr = JSONArray(obj)
                var arrsize = arr.length()
                if (arrsize > 0) {
                    var streamKey = mData!!.id
                    for (i in 0 until arr.length()) {
                        var modelObj = arr.optJSONObject(i)
                        if (streamKey.equals(modelObj.optString("streamKey"))) {
                            likeCount = modelObj.optInt("cnt")
                            Logger.e("likeCount :: " + likeCount)
                            buttonLike.text = StringUtils.getSnsStyleCountZeroBase(likeCount)
                            mData!!.favoriteCount = "$likeCount"
                        }
                    }
                }

                var stKey = intent.getStringExtra("streamKey")
                var status = intent.getStringExtra("status")
                Logger.e("stKey :: " + stKey)
                if (!TextUtils.isEmpty(stKey) && !TextUtils.isEmpty(status)) {
                    if (stKey == streamKey) {
                        Logger.e("scrap mIsVisibleToUser :: " + mIsVisibleToUser)
                        AppToast.cancelAllToast()
                        scrapStatus = status
                        handleScrapUI()
                    }
                }
            } else if (action == "muteCalled") {
                // 사용자가 음소거를 하지 않은 상태에서만 사운드 켜고 줄이고를 수행해야함
                if (!AppPreferences.getVideoMute(context!!)) {
                    if (mPlayer != null) {
                        var muteStatus = intent.getBooleanExtra("isMute", false)
                        if (muteStatus) {
                            Logger.e("mPlayer volume :: " + mPlayer!!.volume)
                            // setVolume 되기 전에 강제적으로 player 볼륨을 바꾸고 나면 아래 mPlayer?.volume = AppPreferences.getVideoVolumn(context!!) 에서 볼륨 값이 0으로 세팅되서 볼륨이 안나오므로
                            if (AppPreferences.getVideoVolumn(context!!) <= 0) {
                                Logger.e("player volume save")
                                AppPreferences.setVideoVolumn(context!!, mPlayer!!.volume)
                                Logger.e("after save volume :: " + AppPreferences.getVideoVolumn(context!!))
                            } else
                                Logger.e("player volume not save")

                            if (mPlayer?.playbackState ?: Player.STATE_IDLE != Player.STATE_IDLE) {
                                mPlayer?.playWhenReady = false
                                isPaused = true
                            }
//                            if (mData!!.relationPrd.data != null && mData!!.relationPrd.data.size > 0) {
//                                var modelArray = ArrayList<DialogModel>()
//                                var beanData = mData!!.relationPrd.data
//                                var streamKey = mData!!.id
//                                var vodType = mData!!.videoType
//                                val recommendId = mData!!.share_user
//                                if (beanData.isNotEmpty()) {
//                                    for (bean in beanData) {
//                                        //var bean = beanData.get(i)
//                                        var dialogModel = DialogModel()
//                                        dialogModel.idx = bean.idx
//                                        dialogModel.scCode = bean.sc_code
//                                        dialogModel.pcode = bean.pcode
//                                        dialogModel.storeName = bean.sc_name
//                                        dialogModel.type = bean.strType
//                                        dialogModel.thumbNail = bean.strPrdImg
//                                        dialogModel.linkUrl = bean.strLinkUrl
//                                        dialogModel.name = bean.strPrdName
//                                        dialogModel.custPrice = bean.nPrdCustPrice
//                                        dialogModel.sellPrice = bean.nPrdSellPrice
//                                        dialogModel.streamKey = streamKey
//                                        dialogModel.vodType = vodType
//                                        dialogModel.recommendId = recommendId
//                                        dialogModel.wish_cnt = bean.wish_cnt
//                                        dialogModel.is_wish = bean.is_wish
//                                        dialogModel.is_cart = bean.is_cart
//
//                                        modelArray.add(dialogModel)
//                                    }
//
//                                    var playerInfo = PlayerInfo()
//                                    var pipInfo = getPipInfo()
//                                    playerInfo.myVODPosition = myVODPosition
//                                    playerInfo.playerFlag = playerFlag
//                                    playerInfo.videoType = mData?.videoType
//                                    playerInfo.title = pipInfo!!.title
//                                    playerInfo.productName = pipInfo!!.productName
//                                    playerInfo.type = VideoPipBus.TYPE_SWIPE
//
//                                    var bus = VideoPipBus(videoUrl, mPlayer?.currentPosition, mData?.strContentSize, mPlayer?.volume!!, playerInfo)
//                                    EventBus.getDefault().post(bus)
//                                }
//                            }
                            mPlayer?.volume = 0f
                        } else {
                            EventBus.getDefault().post("FINISH_PIP_MODE")
                            mPlayer?.volume = AppPreferences.getVideoVolumn(context!!)
                            if (mIsInit) {
                                mHandler?.postDelayed({
                                    if (mIsVisibleToUser) {
                                        playVideoByMe()
                                        seekTo()
                                        startAnimation()
                                    }
                                }, 800)
                            }
                        }
                    }
                } else {
                    Logger.e("muteCalled but sound not setted")
                    var muteStatus = intent.getBooleanExtra("isMute", false)
                    if ( !muteStatus ) {
                        EventBus.getDefault().post("FINISH_PIP_MODE")
                        if (mIsInit) {
                            mHandler?.postDelayed({
                                if (mIsVisibleToUser) {
                                    playVideoByMe()
                                    seekTo()
                                    startAnimation()
                                }
                            }, 800)
                        }
                    }
                }
            }
        }
    }

    private fun showHeartView(count: Int) {
        var lCount = count
//        if ( lCount > 9 )
//            lCount = 9
        isDrawHeartViewRun = true
        var sleepCount = 0
        // count 갯수가 6보다 클 경우 ( 1초에 그려야하는 heart 수가 6보다 클 경우)  이를 150ms delay를 주면서 그릴 경우 1초 이내에 다 못그릴 수 있으므로
        // 이 경우에는 전체 갯수를 1초내에 그릴 수 있는 숫자로 delay를 계산해서 세팅해줘야한다.
        var delay = (1000 / (lCount + 1)).toLong()
        if (lCount < 6)
            delay = 150

        for (i in 0 until lCount) {
            if (i == 0) {
                sleepCount++
//                HeartView(context, heartView)
                mHandler?.sendEmptyMessageDelayed(MESSAGE_SHOW_HEART_VIEW, 50) // 0.3초 동안 모아서 1번만 그리자!
                if (lCount == 1) {
                    isDrawHeartViewRun = false
                }
            } else {
                object : AsyncTask<Integer, Void, Void>() {
                    override fun onPreExecute() {
                        sleepCount++
                    }

                    override fun doInBackground(vararg params: Integer?): Void? {
                        Thread.sleep(delay)
                        return null
                    }

                    override fun onPostExecute(result: Void?) {
//                        HeartView(context, heartView)
                        mHandler?.sendEmptyMessageDelayed(MESSAGE_SHOW_HEART_VIEW, 50) // 0.3초 동안 모아서 1번만 그리자!
                        if (sleepCount == lCount) {
                            isDrawHeartViewRun = false
                        }
                    }
                }.execute()
            }
        }
    }

    private fun possibleGoNext(): Boolean {
        var isPossible = true
        var isReplyDialogShow = false
        var isProductDialogShow = false
        var isShareDialogShow = false
        var isCookieSendDialogShow = false
        var isDrawerOpened = false
        var isMoreVisible = false
        var isNickNameModifyDialogShow = false
        var isVideoTitleModifyDialogShow = false
        // 댓글이 열려있는지 채크
        try {
            if (videoReplyDialog != null) {
                if (videoReplyDialog!!.dialog != null
                        && videoReplyDialog!!.dialog!!.isShowing
                        && !videoReplyDialog!!.isRemoving) {
                    //dialog is showing so do something
                    isReplyDialogShow = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 상품 다이얼로그가 열려 있는지 채크
        if (productListDialog != null) {
            if (productListDialog!!.isShowing) {
                isProductDialogShow = true
            }
        }

        // 공유 다이얼로그가 열려 있는지 채크
        if (sDialog != null) {
            if (sDialog!!.isShowing) {
                isShareDialogShow = true
            }
        }

        // 쿠키 선물 다이얼로그가 열려 있는지 채크
        if (cookieSendDialog != null) {
            if (cookieSendDialog!!.isShowing) {
                isCookieSendDialogShow = true
            }
        }

        // 닉네임 변경 다이얼로그가 열려 있는지 채크
        if (nickNameDialog != null) {
            if (nickNameDialog!!.isShowing) {
                isNickNameModifyDialogShow = true
            }
        }

        // 비디오 제목 변경 다이얼로그가 열려 있는지 채크
        if (videoTitleModifyDialog != null) {
            if (videoTitleModifyDialog!!.isShowing) {
                isVideoTitleModifyDialogShow = true
            }
        }


        isDrawerOpened = (activity as ShoppingPlayerActivity).isOpened
        isMoreVisible = layoutMore?.visibility == View.VISIBLE

        Logger.e("isReplyDialogShow :: " + isReplyDialogShow)
        Logger.e("isProductDialogShow :: " + isProductDialogShow)
        Logger.e("isShareDialogShow :: " + isShareDialogShow)
        Logger.e("isCookieSendDialogShow :: " + isCookieSendDialogShow)
        Logger.e("isNickNameModifyDialogShow :: " + isNickNameModifyDialogShow)
        Logger.e("isVideoTitleModifyDialogShow :: " + isVideoTitleModifyDialogShow)
        Logger.e("isDrawerOpened :: " + isDrawerOpened)
        Logger.e("isMoreVisible :: " + isMoreVisible)
        // 댓글 다이얼로그, 상품 다이얼로그, 공유 다이얼로그, 쿠키 전송 다이얼로그, 닉네임 변경 다이얼로그, 비디오 제목 변경 다이얼로그, 좌우Drawer, MORE 중 하나라도 열려 있다면 다음영상 재생을 하면 안됨
        if (isReplyDialogShow || isProductDialogShow || isShareDialogShow || isCookieSendDialogShow || isNickNameModifyDialogShow || isVideoTitleModifyDialogShow || isDrawerOpened || isMoreVisible) {
            isPossible = false
        }

        return isPossible
    }
}