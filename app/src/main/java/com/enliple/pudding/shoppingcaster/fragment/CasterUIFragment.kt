package com.enliple.pudding.shoppingcaster.fragment

import android.app.Activity
import android.content.*
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.PuddingApplication
import com.enliple.pudding.R
import com.enliple.pudding.activity.MainActivity
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.chat.ChatManager
import com.enliple.pudding.commons.chat.adapter.ChatAdapter
import com.enliple.pudding.commons.chat.dao.AdapterChatData
import com.enliple.pudding.commons.chat.dao.ChatUser
import com.enliple.pudding.commons.chat.widget.ChatAdminOptionDialog
import com.enliple.pudding.commons.data.ApiParams
import com.enliple.pudding.commons.data.DialogModel
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.*
import com.enliple.pudding.commons.shoppingcommons.widget.HeartView
import com.enliple.pudding.commons.shoppingcommons.widget.LiveBroadcastFinishedDialog
import com.enliple.pudding.commons.shoppingcommons.widget.LiveUserListDialog
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.ui_compat.PixelUtil
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.shoppingcaster.VCommerceStreamer
import com.enliple.pudding.shoppingcaster.activity.ShoppingCastActivity
import com.enliple.pudding.shoppingcaster.activity.VODSelectActivity
import com.enliple.pudding.shoppingcaster.data.*
import com.enliple.pudding.shoppingcaster.widget.*
import com.enliple.pudding.widget.LiveCookieSender
import com.enliple.pudding.widget.PlayerProductDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.share.Sharer
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.MessageDialog
import com.facebook.share.widget.ShareDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.*
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import kotlinx.android.synthetic.main.dialog_share.view.*
import kotlinx.android.synthetic.main.fragment_live_caster.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import pl.droidsonroids.gif.AnimationListener
import pl.droidsonroids.gif.GifDrawable
import java.util.*

/**
 * 방송 중 사용되는 UI 를 지원하는 Fragment
 * @author hkcha
 * @since 2018.05.21
 */
class CasterUIFragment : androidx.fragment.app.Fragment(),
        CasterExitDialog.Listener,
        CasterTitleModifyDialog.Listener,
        CasterNoticeDialog.Listener,
        ChatManager.ChatListener,
        CastEffectDialog.SelectionListener,
        ChatAdapter.Listener,
        ChatAdminOptionDialog.OptionCallback {

    companion object {
        const val VOD_SELECT_ACTIVITY_RESULT = 1000
        const val SHOW_COOKIE = 100
        const val HIDE_COOKIE = 101
        const val BUY_PRODUCT = 102
        const val REFRESH_BROADCAST = 103

        const val BUNDLE_EXTRA_KEY_ROOM_ID = "no"
        const val BUNDLE_EXTRA_KEY_ACCOUNT = "id"
        const val BUNDLE_EXTRA_KEY_NICKNAME = "nk"
        const val BUNDLE_EXTRA_KEY_CHAT_IP = "ip"
        const val BUNDLE_EXTRA_KEY_CHAT_PORT = "port"

        const val BUNDLE_EXTRA_KEY_CAST_SCHEDULE = "cast_schedule"

        const val SEND_CHAT = "SEND_CHAT_RECEIVER"

        private const val CHAT_INPUT_POSITION_OFFSET_DP = 100

        private const val HIDE_UI = 1000
        private const val HIDE_UI_TIME_OUT = 50000L  //5초
        private const val MESSAGE_HIDE_USER_ACTION_POPUP = 2005
        private const val MESSAGE_SHOW_USER_ACTION_POPUP = 2006
        private const val MESSAGE_HIDE_USER_JOIN_POPUP = 2007
        private const val MESSAGE_SHOW_USER_JOIN_POPUP = 2008
        private const val MESSAGE_SHOW_USER_ACTION_POPUP_CALL_TIME = 10000L * 6   // 1분
        private const val REFRESH_LIVE_INFO = 10000L * 3   // 30초

        // for share
        private const val TITLE_KAKAO = "kakao"
        private const val TITLE_FACEBOOK = "face"
        private const val TITLE_MESSENGER = "messenger"
        private const val TITLE_LINK = "link"
        private const val TITLE_SOURCECODE = "code"
        private const val TITLE_OTHER = "other"
    }

    private var systemOrientation: Int = android.content.res.Configuration.ORIENTATION_PORTRAIT

    private var chatManager: ChatManager? = null
    private var chatMyId: ChatUser? = null
    private var mUserMap: HashMap<String, String> = HashMap()  // id, json
    private lateinit var chatLayoutManager: androidx.recyclerview.widget.LinearLayoutManager
    private var chatAdapter: ChatAdapter? = null

    // Injection Break : 사용자가 채팅 내용을 입력하여 최종적으로 전송한 시간
    private var chatLatestSendTime = System.currentTimeMillis()

    private var noChatLayoutYPos: Int = 0
    private var chatInputPositionOffset = 0

    //private var exitDialog: CasterExitDialog? = null
    private var userListDialog: LiveUserListDialog? = null
    private var effectDialog: CastEffectDialog? = null
    private var stickerDialog: CastStickerDialog? = null
    private var chatOptionDialog: ChatAdminOptionDialog? = null
    private var titleModifyDialog: CasterTitleModifyDialog? = null
    private var noticeDialog: CasterNoticeDialog? = null

    private var duplicateNickAddHeader = 1

    private var mUIHandler = Handler(Looper.getMainLooper())

    //private var productListDialog: CasterLiveProductDialog? = null
    private var productListDialog: PlayerProductDialog? = null

    private var mKeyboardHeight = 0
    private var mCookieSum = 0
    private var mRoomID = ""
    private var mChatIP = ""
    private var mChatPort = 0

    private var mNickName: String = ""
    private var mNotifyString: String = ""

    private var mTempTitle: String = ""
    private var mTempNotice: String = ""
    private var mShareType = ""
    private var mStreamer: VCommerceStreamer? = null
    private var shareTitle = ""
    private var mIsSchedule = false
    private var productItems: List<API136.Products.ProductItem> = mutableListOf()
    private var mProductCount = 0
    private var mLiveUserJoinData: LiveUserJoinData = LiveUserJoinData()
    private var mLiveUserActionData: LiveUserActionData = LiveUserActionData()
    private var enterUserId = arrayListOf<String>()
    private var enterUserNick = arrayListOf<String>()
    private var isRoomJoinSolo = true
    private var enterRoomCount = 0
    private var mTimer: Timer? = null
    private var mRefreshTimer: Timer? = null
    private var likeCnt = 0
    private var beautyFilterSet = true
    private var isChatMode = false

    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == HIDE_UI) {
                //hideFullLayout()
            } else if (msg.what == SHOW_COOKIE) {
                receivedCookie()
            } else if (msg.what == HIDE_COOKIE) {
                hideCookie()
            } else if (msg.what == MESSAGE_SHOW_USER_ACTION_POPUP) {
                showUserActionPopup()
            } else if (msg.what == MESSAGE_HIDE_USER_ACTION_POPUP) {
                hideUserActionPopup()
            } else if (msg.what == MESSAGE_SHOW_USER_JOIN_POPUP) {
                showUserJoinPopup()
            } else if (msg.what == MESSAGE_HIDE_USER_JOIN_POPUP) {
                hideUserJoinPopup()
            } else if (msg.what == BUY_PRODUCT) {
                setProductCount()
            } else if (msg.what == REFRESH_BROADCAST) {
                getLiveInfo()
            }
        }
    }

    private val mTask = object : TimerTask() {
        override fun run() {
            mHandler.sendEmptyMessage(MESSAGE_SHOW_USER_JOIN_POPUP)
        }
    }

    private val mRefreshTask = object : TimerTask() {
        override fun run() {
            getLiveInfo()
        }
    }

    private var mCookieQueue: Queue<CookieData> = LinkedList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_live_caster, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mNickName = arguments?.getString(BUNDLE_EXTRA_KEY_NICKNAME) ?: ""
        mRoomID = arguments?.getString(BUNDLE_EXTRA_KEY_ROOM_ID) ?: ""
        mIsSchedule = arguments!!.getBoolean(BUNDLE_EXTRA_KEY_CAST_SCHEDULE)
        mChatIP = arguments?.getString(BUNDLE_EXTRA_KEY_CHAT_IP) ?: ""
        mChatPort = arguments!!.getInt(BUNDLE_EXTRA_KEY_CHAT_PORT)
        Logger.e("onCreate [Args] ${arguments.toString()}")

        if (!mIsSchedule) {
            var info = AppPreferences.getBroadcastInfo(context!!)
            try {
                Logger.e("getBroadcastInfo :: $info")
                if (!TextUtils.isEmpty(info)) {
                    mNotifyString = JSONObject(info).optString(BroadcastInfo.KEY_REGISTRATION)
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        } else {
            val key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API136.toString(), AppPreferences.getUserId(context!!)!!, "")
            val response: API136 = Gson().fromJson(DBManager.getInstance(context!!).get(key), API136::class.java)

            mNotifyString = response.noti
            productItems = response.relationPrd.data
        }

        var userId = AppPreferences.getUserId(context!!)
        if (!TextUtils.isEmpty(userId)) {
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

        var account = arguments?.getString(BUNDLE_EXTRA_KEY_ACCOUNT) ?: ""

        if (!TextUtils.isEmpty(mNickName) && !TextUtils.isEmpty(account) && !TextUtils.isEmpty(mRoomID)) {
            Logger.e("roodID:$mRoomID nickName:$mNickName")
            chatMyId = ChatUser("123", "5", "0", mRoomID, account, mNickName, "10", "1", "android${System.currentTimeMillis()}")
        }

        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        systemOrientation = resources.configuration.orientation

        chatLayoutManager = WrappedLinearLayoutManager(activity as? Context)
        chatLayoutManager.orientation = RecyclerView.VERTICAL
        recyclerViewChat.setHasFixedSize(false)
        chatAdapter = ChatAdapter().apply {
            setListener(this@CasterUIFragment)
        }
        recyclerViewChat.itemAnimator = null
        recyclerViewChat.layoutManager = chatLayoutManager
        recyclerViewChat.adapter = chatAdapter

//        editTextChat.imeOptions = EditorInfo.IME_ACTION_DONE
        editTextChat.setOnEditorActionListener(chatActionListener)
        editTextChat.addTextChangedListener(inputChangeListener)
        chatInput.setOnClickListener(clickListener)
        chatInput.isLongClickable = false
        buttonSendMsg.isEnabled = false

        buttonFilter.setOnClickListener(clickListener)
        fullLayer.setOnClickListener(clickListener)
        //fullLayer.setOnTouchListener(touchListener)
        clickLayout.setOnClickListener(clickListener)
        buttonSendMsg.setOnClickListener(clickListener)
        //buttonChatEmoticon.setOnClickListener(clickListener)
        buttonPause.setOnClickListener(clickListener)
        buttonExit.setOnClickListener(clickListener)
        buttonCameraToggle.setOnClickListener(clickListener)
        //buttonChatMode.setOnClickListener(clickListener)
        buttonProductManagement.setOnClickListener(clickListener)
        buttonRealTimeRank.setOnClickListener(clickListener)
//        buttonEffect.setOnClickListener(clickListener)
        buttonViewerList.setOnClickListener(clickListener)
        buttonMore.setOnClickListener(clickListener)
        layoutMore.setOnClickListener(clickListener)
        notification_close.setOnClickListener(clickListener)
//        buttonVideo.setOnClickListener(clickListener)
        // 빼려고 한다.
//        buttonLike.setOnClickListener(clickListener)
//        buttonLike.isSelected = true

        textViewBroadcastingTitleModify.setOnClickListener(clickListener)
        textViewBroadcastingNoticeModify.setOnClickListener(clickListener)
        textViewBroadcasting.setOnClickListener(clickListener)

        layoutChatInput.post {
            chatInputPositionOffset = PixelUtil.dpToPx(activity!!, CHAT_INPUT_POSITION_OFFSET_DP)
            noChatLayoutYPos = layoutChatInput.y.toInt()
            Logger.e("noChatLayoutYPos : $noChatLayoutYPos")
//            layoutChatInput.addOnLayoutChangeListener(chatInputChangeListener)
//            editTextChat.onFocusChangeListener = chatFocusListener
//            buttonChatEmoticon.bringToFront()
        }

        if (chatMyId != null) {
            chatManager = ChatManager().apply {
                setChatListener(this@CasterUIFragment)
                connect(chatMyId!!, mChatIP, mChatPort)
            }
        }

        textViewNotification.post {
            textViewNotification.isSelected = true
        }

        memberCount.text = "0"
        cookieCount.text = "0"
        productCount.text = mProductCount.toString()

        nickName.text = mNickName
        textViewNotification.text = mNotifyString

        val key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API21.toString(), AppPreferences.getUserId(context!!)!!, "")
        var json = DBManager.getInstance(context).get(key)
        if (!TextUtils.isEmpty(json)) {
            var response: API21 = Gson().fromJson(json, API21::class.java)
            if (!TextUtils.isEmpty(response.userIMG)) {
                ImageLoad.setImage(context, host_image, response.userIMG, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
            }

            if (!TextUtils.isEmpty(response.userNickname)) {
                nickName.text = response.userNickname
            }
        }

        // 첫번째 상품 이미지
        if (!mIsSchedule) {
            var array: List<API72.RelationPrdBean.ProductItem> = (activity as ShoppingCastActivity).productArray
            if (array.size > 0) {
                val prodImage = array[0].strPrdImg
                if (prodImage.isNotEmpty()) {
                    ImageLoad.setImage(context, buttonProductManagement, prodImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                }
            }
        } else {
            if(productItems.size > 0) {
                val prodImg = productItems[0].strPrdImg
                if (prodImg.isNotEmpty()) {
                    ImageLoad.setImage(context, buttonProductManagement, prodImg, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                }
            }
        }

        timer.base = SystemClock.elapsedRealtime()
        timer.start()
        timer.format = "LIVE %s"

        EventBus.getDefault().post(NetworkBus(NetworkApi.API82.name, (activity as ShoppingCastActivity).streamKey))

        startAnimation()
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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        registerChatBR(context)
    }

    override fun onDetach() {
        mHandler.removeMessages(HIDE_UI)

        try {
            chatManager?.disconnect()
            chatManager = null
        } catch (ignore: Exception) {
            ignore.printStackTrace()
        }

        super.onDetach()

        unregisterChatBR()
    }

    override fun onPause() {
        super.onPause()

        stopAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()

        mHandler.removeCallbacksAndMessages(null)
        mRefreshTimer?.cancel()
        mTimer?.cancel()

        try {
            SoftKeyboardUtils.hideKeyboard(editTextChat)
        } catch (e: Exception) {
        }

        EventBus.getDefault().unregister(this)
    }

    override fun onCastingFinish(body: RequestBody) {
        (activity as? ShoppingCastActivity)?.onCastStop(body)

        chatManager?.disconnect()

        startActivity(Intent(context, MainActivity::class.java)) // 메인으로 이동
        (activity as ShoppingCastActivity).activityFinish()    // MainActivity로 이동하면 스택에 쌓여 있는 모든 Activity를 종료한다.
    }

    override fun onChatDisconnected() {
        Logger.e("chat disconnected.")
        chatManager?.connect(chatMyId!!, mChatIP, mChatPort)
    }

    override fun onChatConnectError(errorCode: Int) {
        Logger.e("Connect Error!")
        when (errorCode) {
            ChatManager.ERROR_CODE_DUPLICATE_NAME -> {
                // 닉네임이 중복되는 경우 연결을 해제하고 닉네임을 변경하여 요청
                chatManager?.disconnect()
                chatManager = null
                chatMyId?.nickName?.replace("${duplicateNickAddHeader - 1}", "")
                chatMyId?.nickName = "${chatMyId?.nickName ?: ""}$duplicateNickAddHeader"
                ++duplicateNickAddHeader

                chatManager = ChatManager().apply {
                    setChatListener(this@CasterUIFragment)
                    connect(chatMyId!!, mChatIP, mChatPort)
                }
            }
        }
    }

    override fun onRoomConnected(users: List<String>) {
        Logger.e("onRoomConnected:")
        if (users.isNotEmpty()) {
            users.forEach {
                Logger.e("onRoomConnected: $it")
                try {
                    var user: ChatUser = Gson().fromJson<ChatUser>(it, ChatUser::class.java)
                    var id = user.id
                    if (!TextUtils.isEmpty(id)) {
                        mUserMap[id!!] = it
                    }
                } catch (e: Exception) {
                    Logger.p(e)
                }
            }
        }

        mHandler.post(Runnable {
            memberCount.text = mUserMap.size.toString()
        })

        recyclerViewChat.post {
            chatAdapter?.addItem(AdapterChatData(ChatManager.NORMAL_CHAT_CMD, null,
                    getString(R.string.msg_chat_started),
                    System.currentTimeMillis()))
        }

        if(mRefreshTimer == null) {
            mRefreshTimer = Timer().apply {
                schedule(mRefreshTask, 0, REFRESH_LIVE_INFO)
            }
        }
    }

    override fun onRoomJoinedUser(user: ChatUser, json: String) {
        var id = user.id
        if (TextUtils.isEmpty(id) || mUserMap.containsKey(id)) {
            return
        }

        Logger.d("onRoomJoinedUser:$id")
        mUserMap[id!!] = json

        enterUserId.add(user.id)
        enterUserNick.add(user.nickName)

        mHandler.post(Runnable {
            memberCount.text = mUserMap.size.toString()
        })

        // 방 입장을 채팅창에 표시
//        recyclerViewChat.post {
//            chatAdapter?.addItem(AdapterChatData(ChatManager.NORMAL_CHAT_CMD, null,
//                    String.format(getString(R.string.msg_chat_user_join_format), user.nickName),
//                    System.currentTimeMillis()))
//
//            chatLayoutManager.scrollToPositionWithOffset(0, 0)
//
//            // 채팅방 사용자 리스트가 열려있는 경우 사용자 리스트 팝업을 갱신
//            if (userListDialog?.isShowing == true) {
//                userListDialog?.setItems(mUserMap)
//            }
//        }

        mLiveUserJoinData.nickName = enterUserNick
        mLiveUserJoinData.userId = enterUserId

        if (isRoomJoinSolo) {
            ++enterRoomCount
            mHandler.sendEmptyMessage(MESSAGE_SHOW_USER_JOIN_POPUP)
        } else {
            if (mTimer == null) {
                mTimer = Timer().apply {
                    schedule(mTask, 0, MESSAGE_SHOW_USER_ACTION_POPUP_CALL_TIME)
                }
            }
        }
    }

    override fun onRoomLeavedUser(user: ChatUser, json: String) {
        var id = user.id
        if (TextUtils.isEmpty(id) || !mUserMap.containsKey(id)) {
            return
        }

        Logger.d("onRoomLeavedUser:${user.id}")

        mUserMap.remove(id)

        mHandler.post(Runnable {
            memberCount.text = mUserMap.size.toString()
        })

//        recyclerViewChat.post {
//            chatAdapter?.addItem(AdapterChatData(ChatManager.NORMAL_CHAT_CMD, null,
//                    String.format(getString(R.string.msg_chat_user_leave_format), user.nickName),
//                    System.currentTimeMillis()))
//
//            chatLayoutManager.scrollToPositionWithOffset(0, 0)
//
//            // 채팅방 사용자 리스트가 열려있는 경우 사용자 리스트 팝업을 갱신
//            if (userListDialog?.isShowing == true) {
//                userListDialog?.setItems(mUserMap)
//            }
//        }

        // 채팅방 사용자 리스트가 열려있는 경우 사용자 리스트 팝업을 갱신
//        if (userListDialog?.isShowing == true) {
//            userListDialog?.setItems(mUserMap)
//        }
    }

    override fun onChatFinished() {
        //buttonChatMode.visibility = View.GONE
    }

    override fun onChatReceived(chatMsg: Map<String, String>) {
        mUIHandler.post {
            var chatContent = if (chatMsg.containsKey("ChatContent")) chatMsg["ChatContent"] else null
            var dateContent = if (chatMsg.containsKey("DateContent")) chatMsg["DateContent"] else null
            var cmd = if (chatMsg.containsKey("Cmd")) chatMsg["Cmd"] else ChatManager.NORMAL_CHAT_CMD
            var color = if (chatMsg.containsKey("Color")) chatMsg["Color"] else ""
            if (cmd != ChatManager.HEART_CHAT_CMD) {
                Logger.e("onChatReceived chatContent :: $chatContent")
                Logger.e("onChatReceived dateContent :: $dateContent")
                Logger.e("onChatReceived cmd :: $cmd")
                Logger.e("onChatReceived color :: $color")
                if (chatContent != null && dateContent != null) {
                    try {
                        if (cmd == ChatManager.COOKIE_ETC_CHAT_CMD) {
                            Logger.e("chat cookie :: $chatContent")

                            val splitMessageRaw = chatContent.split(" say to ")
                            val from = splitMessageRaw[0]
                            val message = splitMessageRaw[1].split(":")[1]
                            Logger.e("from :: $from")
                            Logger.e("message :: $message")
                            val arr = message.split("|^|")
                            val gubun = arr[0].replace(" ", "")
                            if (gubun.equals(ChatManager.GUBUN_COOKIE)) {
                                val senderStr = arr[1]
                                val receivedCnt = arr[2].toInt()
                                Logger.e("receivedCnt == $receivedCnt")

                                val cookieData = CookieData().apply {
                                    cnt = receivedCnt
                                    sender = senderStr
                                    id = JSONObject(from).getString("id")
                                }

                                mCookieQueue.offer(cookieData)

                                mHandler.sendEmptyMessage(SHOW_COOKIE)

                                val insertData = AdapterChatData(cmd, from, message, dateContent.toLong())
                                chatAdapter?.addItem(insertData)
                                chatLayoutManager.scrollToPositionWithOffset(chatAdapter?.itemCount!!.minus(1), 0)
                            } else if (gubun == ChatManager.GUBUN_BUY) {
                                val insertData = AdapterChatData(cmd, from, message, dateContent.toLong())
                                chatAdapter?.addItem(insertData)
                                chatLayoutManager.scrollToPositionWithOffset(chatAdapter?.itemCount!!.minus(1), 0)
                                mHandler.sendEmptyMessage(BUY_PRODUCT)

//                                mLiveUserActionData.buyUserNick = arr[1]
//                                mLiveUserActionData.buyUserId = JSONObject(from).getString("id")
//                                mLiveUserActionData.gubun = gubun
//                                mHandler.sendEmptyMessage(MESSAGE_SHOW_USER_ACTION_POPUP)
                            } else if (gubun == ChatManager.GUBUN_CART) {
                                val insertData = AdapterChatData(cmd, from, message, dateContent.toLong())
                                chatAdapter?.addItem(insertData)
                                chatLayoutManager.scrollToPositionWithOffset(chatAdapter?.itemCount!!.minus(1), 0)

//                                mLiveUserActionData.cartUserNick = arr[1]
//                                mLiveUserActionData.cartUserId = JSONObject(from).getString("id")
//                                mLiveUserActionData.gubun = gubun
//                                mHandler.sendEmptyMessage(MESSAGE_SHOW_USER_ACTION_POPUP)
                            }
                        } else if (cmd == ChatManager.BROADCAST_INFO_CMD) {
                            mHandler.sendEmptyMessage(REFRESH_BROADCAST)
                        } else if (cmd == ChatManager.FORCE_EXIT_CMD) {
                            LiveBroadcastFinishedDialog(
                                    view!!.context,
                                    PuddingApplication.mLoginUserData?.userNickname!!,
                                    PuddingApplication.mLoginUserData?.userIMG!!,
                                    PuddingApplication.mLoginUserData?.userId!!, false, true).apply {
                                setOnDismissListener {
                                    (activity as? ShoppingCastActivity)?.onCastStop(null)
                                    chatManager?.disconnect()
                                }
                            }.show()
                        } else {
                            val splitMessageRaw = chatContent.split(" say to ")
                            val from = splitMessageRaw[0]
                            val message = splitMessageRaw[1].split(":")[1]

//                            if (message.length > 36) {
//                                message = message.substring(36)
//                            }

                            val insertData = AdapterChatData(cmd, from, message, dateContent.toLong())
                            chatAdapter?.addItem(insertData)
                            chatLayoutManager.scrollToPositionWithOffset(chatAdapter?.itemCount!!.minus(1), 0)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onHeartAdded(user: ChatUser) {
        if (context != null && isAdded) {
            mHandler.post(Runnable { HeartView(context, heartView) })
            //heartView.addHeart(Random().nextInt(HEART_IMAGE_COUNT))
        }
    }

    private fun toogleFullLayout() {
        if (clickLayout.isShown) {
            showFullLayout()
        } else {
            if(isChatMode) {
                hideChatMode()
            } else {
                hideFullLayout()
            }
        }
    }

    private fun hideFullLayout() {
        Logger.d("hideFullLayout")

        mHandler.removeMessages(HIDE_UI)

        stopAnimation()

        layoutMore.visibility = View.GONE
        fullLayer?.visibility = View.GONE
        clickLayout.visibility = View.VISIBLE
    }

    private fun showFullLayout() {
        Logger.d("showFullLayout")

        startAnimation()

        mHandler.sendEmptyMessageDelayed(HIDE_UI, HIDE_UI_TIME_OUT)

        fullLayer?.visibility = View.VISIBLE
        clickLayout.visibility = View.GONE
    }

    override fun onBeautyFilterSelected() {
        beautyFilterSet = !beautyFilterSet
        togglePrettyFilter(beautyFilterSet)
    }

    override fun onVideoFormationSelected() {
        (activity as ShoppingCastActivity)?.switchCameraOrientation()
    }

    override fun onChooseStickerSelected() {
        (activity as? ShoppingCastActivity)?.removeImageStickers()

        effectDialog?.dismiss()
        stickerDialog?.dismiss()
        stickerDialog = CastStickerDialog(view!!.context).apply {
            setListener(object : CastStickerDialog.Listener {
                override fun onStickerResourceClicked(stickerResourceId: Int) {
                    Logger.e("onStickerResourceClicked : $stickerResourceId")
                    (activity as? ShoppingCastActivity)?.addImageStickerByResource(stickerResourceId)
                }
            })
            show()
        }
    }

    override fun onUserOptionClicked(user: String?) {
        Logger.e("onUserOptionClicked - $user")

        var isAnotherUser = user != null && (chatMyId?.id ?: "") != JSONObject(user).getString("id")
        var isChatUser = mUserMap.contains(JSONObject(user).toString())

        if (isAnotherUser && isChatUser) {
            // 자기 자신이 아닌 타사용자 아이디를 클릭하였을 때에만 반응 처리
            chatOptionDialog?.dismiss()
            chatOptionDialog = ChatAdminOptionDialog(view!!.context, user!!,
                    this@CasterUIFragment).apply {
                show()
            }
        }
    }

    override fun onDeniedChat(user: String) {
        Logger.e("onDeniedChat : $user")
        var isChatUser = mUserMap.contains(JSONObject(user).toString())
        if (isChatUser) {
            // TODO : 사용자 채팅 차단 처리

        }
    }

    open fun onBackPressed() {
        Logger.e("onBackPressed")

        confirmExit()
    }

    override fun onKickUser(user: String) {
        Logger.e("onKickUser : $user")
        var isChatUser = mUserMap.contains(JSONObject(user).toString())
        if (isChatUser) {
            // TODO : 사용자 강제 퇴장처리(Chat 에서 Kick 시키고 API 를 통해 Kick 된 사용자 정보를 통보)

        }
    }

    override fun onDeniedAll(user: String) {
        Logger.e("onDeniedAll : $user")
        var isChatUser = mUserMap.contains(JSONObject(user).toString())
        if (isChatUser) {
            // TODO : 사용자 채팅 및 귓속말(문의) 차단 처리
        }
    }

    override fun onChangedTitleText(title: String) {
        mTempTitle = title

        var requestObj = ApiParams.TitleModifyParams(
                (activity as ShoppingCastActivity).streamKey,
                "LIVE",
                title,
                AppPreferences.getUserId(context!!))

        EventBus.getDefault().post(NetworkBus(NetworkApi.API15.name, requestObj))
    }

    override fun onNoticeText(text: String) {
        //textViewNotification.text = text
        mTempNotice = text

        var requestObj = ApiParams.NoticeModifyParams(
                (activity as ShoppingCastActivity).streamKey,
                "LIVE",
                text,
                AppPreferences.getUserId(context!!))

        EventBus.getDefault().post(NetworkBus(NetworkApi.API16.name, requestObj))
    }

    override fun onNoticeOnOff(isOnOff: Boolean) {
        if (isOnOff) {
            textViewNotification.visibility = View.VISIBLE
        } else {
            textViewNotification.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        Logger.e("onMessageEvent: ${data.arg1}")
        if (data.arg1.startsWith(NetworkApi.API48.toString())) { // for share
            val str = DBManager.getInstance(context!!).get(data.arg1)
            if (str.isNotEmpty()) {
                var link = Gson().fromJson(str, API48::class.java).url
                Logger.e("shareLink :: $link")

                shareTitle = (activity as? ShoppingCastActivity)?.title!!

                if (link.isNotEmpty()) {
                    when (mShareType) {
                        TITLE_KAKAO -> shareKakao(link)
                        TITLE_FACEBOOK -> shareFacebook(link)
                        TITLE_MESSENGER -> shareFacebookMessenger(link)
                        TITLE_LINK -> setClipBoardLink(link)
                        TITLE_OTHER -> shareEtc(link)
                        TITLE_SOURCECODE -> setClipBoardLink(link)
                    }
                }
            }
        } else {
            when (data.arg1) {
                NetworkApi.API15.toString() -> handleNetworkResultTitleModify(data)
                NetworkApi.API16.toString() -> handleNetworkResultNoticeModify(data)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusFastResponse) {
        val api82 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API82.toString(), (activity as ShoppingCastActivity).streamKey, "")
        if(data.arg1 == api82) {
            handleNetworkResultCookieSenderList(data)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
        Logger.e("SoftKeyboardBus height: ${bus.height}")
        mKeyboardHeight = bus.height

        if (bus.height > 100) {
            showChatMode()
        } else if (bus.height <= 0) {
            hideChatMode()
        } else {
            Logger.e("else")
        }
    }

    private fun setClipBoardLink(shareLink: String) {
        val clipboardManager = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clipData = ClipData.newPlainText("label", shareLink)
        clipboardManager!!.primaryClip = clipData
        Toast.makeText(context!!, resources.getString(R.string.clipboard_msg), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, context!!.getString(R.string.facebook_message_success), Toast.LENGTH_SHORT).show()
                }

                override fun onCancel() {
                    Toast.makeText(context, context!!.getString(R.string.facebook_message_cancel), Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    error.printStackTrace()
                    Toast.makeText(context, context!!.getString(R.string.facebook_message_fail), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, context!!.getString(R.string.facebook_success), Toast.LENGTH_SHORT).show()
            }

            override fun onCancel() {
                Toast.makeText(context, context!!.getString(R.string.facebook_cancel), Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                error.printStackTrace()
                Toast.makeText(context, context!!.getString(R.string.facebook_fail), Toast.LENGTH_SHORT).show()
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

//        try {
//            viewCount = viewCnt.toInt()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        try {
//            likeCount = favoriteCnt.toInt()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

        //TODO thumbImage 이거 어쩌냐

        var feedTemplate = FeedTemplate.newBuilder(ContentObject.newBuilder(shareTitle, "",
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
     * 방송 공지 수정 Response 처리
     */
    private fun handleNetworkResultNoticeModify(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            AppToast(context!!).showToastMessage("방송 공지사항이 변경 되었습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)

            noticeDialog!!.dismiss()

            mNotifyString = mTempNotice
            textViewNotification.text = mTempNotice
            (activity as? ShoppingCastActivity)?.notifyString = mTempNotice
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    /**
     * 방송 제목 수정 Response 처리
     */
    private fun handleNetworkResultTitleModify(data: NetworkBusResponse) {
        Logger.e("handleNetworkResultTitleModify")

        if ("ok" == data.arg2) {
            //var str = DBManager.getInstance(context!!).get(NetworkApi.API15.name)
            //var response: BaseAPI = Gson().fromJson(str, BaseAPI::class.java)

            AppToast(context!!).showToastMessage("방송 제목이 변경 되었습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)

            titleModifyDialog?.dismiss()

            (activity as? ShoppingCastActivity)?.title = mTempTitle
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    /**
     * 채팅 텍스트를 전송
     */
    private fun sendText(targetUser: String?) {
        Logger.e("sendText targetUser:$targetUser")

        var currentTime = System.currentTimeMillis()
        if (currentTime - chatLatestSendTime >= ChatManager.CHAT_SEND_INTERVAL) {
            var cmd: String = ChatManager.NORMAL_CHAT_CMD
            var color = ""
            var sendText = editTextChat?.text.toString()
            if (sendText.isNotEmpty()) {                // 텍스트가 있을 경우에만 처리내용을 동작
                var text = filterReturnKeyInject(sendText)
                Logger.e("filterReturnKeyInject:$text")
                if (text!!.isNotEmpty()) {            // ReturnKey Filtering 이후 텍스트 여부 재확인
                    // 채팅 내용 전송 및 UI 업데이트
                    if (!TextUtils.isEmpty(sendText!!.replace("\n", "", true).trim())) {
                        // 위의 조건을 모두 만족하였으면 채팅 내용을 전송
                        chatManager?.sendTextChat(cmd, color, sendText, if (TextUtils.isEmpty(targetUser)) "*" else targetUser, "")
                        editTextChat.setText("")
                        chatLatestSendTime = currentTime
                    }
                }
            }
        }
    }

    /**
     * 채팅창에 엔터키를 연속으로 입력하는 도배방지를 위한 ReturnKey 필터링
     * (현재는 정책상 입력자체를 한줄로 제한하나 향후를 대비하여 유지시키는 프로그램 정책의 일환)
     * @param sendText      사용자가 입력한 채팅글 내용
     * @return
     */
    private fun filterReturnKeyInject(sendText: String): String {
        if (sendText.isEmpty()) {
            return ""
        }

        var position = 0
        var length = sendText.length
        var result = sendText

        while (sendText.isNotEmpty() && position < length) {
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
     * 방송 종료 팝업을 표시
     */
    private fun confirmExit() {
        //exitDialog?.dismiss()
        //exitDialog = null

        var title = (activity as ShoppingCastActivity).title!!
        var tags = (activity as ShoppingCastActivity).tag!!
        var streamKey = (activity as ShoppingCastActivity).streamKey!!
        Logger.e("confirmExit title:$title tags:$tags streamKey:$streamKey")

        CasterExitDialog().apply {
            arguments = Bundle().apply {
                putString("stream", streamKey)
                putString("title", title)
                putString("tag", tags)
            }
            setListener(this@CasterUIFragment)
        }.show(fragmentManager!!, "finishDialog")
    }

    /**
     * 방송 제목 수정 팝업을 표시
     */
    private fun showTitleModify() {
        var title = (activity as ShoppingCastActivity).title!!
        titleModifyDialog?.dismiss()
        titleModifyDialog = null
        titleModifyDialog = CasterTitleModifyDialog(view!!.context, title).apply {
            setListener(this@CasterUIFragment)
            show()
        }
    }

    /**
     * 방송 공지사항 팝업을 표시
     */
    private fun showNotice() {
        noticeDialog?.dismiss()
        noticeDialog = null
        noticeDialog = CasterNoticeDialog(view!!.context, mNotifyString).apply {
            setListener(this@CasterUIFragment)
            show()
        }
    }

    private fun showBeauty() {
        effectDialog?.dismiss()
        effectDialog = null
        effectDialog = CastEffectDialog(view!!.context, this).apply {
            show()
        }
    }

    /**
     * 내 방송 알리기 팝업을 표시
     */
    private fun shareMyBroadcasting() {
        val view = layoutInflater.inflate(R.layout.dialog_share, null)
        view.buttonKakao.setOnClickListener(shareClickListener)
        view.buttonFacebook.setOnClickListener(shareClickListener)
//        view.buttonInsta.setOnClickListener(shareClickListener)
//        view.buttonMessenger.setOnClickListener(shareClickListener)
        view.buttonSourceCode.setOnClickListener(shareClickListener)
        view.buttonLink.setOnClickListener(shareClickListener)
        view.buttonMore.setOnClickListener(shareClickListener)

        val dialog = BottomSheetDialog(view!!.context)
        dialog.setContentView(view)
        dialog.show()
    }

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

//            R.id.buttonMessenger -> {
//                Logger.d("Messenger Button Clicked.")
//                share(TITLE_MESSENGER)
//            }

            R.id.buttonSourceCode -> {
                Logger.d("SourceCode Button Clicked.")
                share(TITLE_SOURCECODE)
            }

            R.id.buttonLink -> {
                Logger.d("Link Button Clicked.")
                share(TITLE_LINK)
            }

            R.id.buttonMore -> {
                Logger.d("More Button Clicked.")
                share(TITLE_OTHER)
            }
        }
    }


    private fun share(space: String) {
        Logger.e("share:$space")

        mShareType = space

        var builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("streamKey", (activity as ShoppingCastActivity).streamKey)
        builder.addFormDataPart("uploader", AppPreferences.getUserId(context!!))
        builder.addFormDataPart("shareId", AppPreferences.getUserId(context!!))
        builder.addFormDataPart("space", space)
        builder.addFormDataPart("vodType", "live")
        var body = builder.build()

        EventBus.getDefault().post(NetworkBus(NetworkApi.API48.name, body))
    }

    /**
     * 뷰티 모드 필터 활성상태를 토글링
     */
    private fun togglePrettyFilter(setFilter: Boolean) {
        (activity as? ShoppingCastActivity)?.setBeautyFilter(setFilter)
//        var render = (activity as ShoppingCastActivity).getCastingGLRender()
//
//        if (render != null) {
//            if (isFilterEnabled) {
//                //currentUseFilter?.gpuImageFilter?.destroy()
//                (activity as ShoppingCastActivity).clearCastingVideoFilter()
//                isFilterEnabled = false
//            } else {
//                currentUseFilter = GPUImageFilterManager
//                        .createImgTexFilter(GPUImageFilterManager.GPU_FILTER_BRIGHTNESS, render)
//                (activity as ShoppingCastActivity).setCastingVideoFilter(currentUseFilter!!)
//                isFilterEnabled = true
//            }
//        }
    }

    private fun showChatMode() {
        Logger.i("showChatMode()")
        isChatMode = true

        chatLayer.visibility = View.GONE
        chatInputLayer.visibility = View.VISIBLE

        chatGapView.visibility = View.VISIBLE
        chatGapView?.layoutParams?.height = mKeyboardHeight
        chatGapView?.invalidate()
        chatGapView?.requestLayout()

        editTextChat.requestFocus()
    }

    private fun hideChatMode() {
        Logger.i("hideChatMode()")
        isChatMode = false

        chatLayer.visibility = View.VISIBLE
        chatInputLayer.visibility = View.GONE
        chatGapView.visibility = View.GONE

        editTextChat.clearFocus()

        SoftKeyboardUtils.hideKeyboard(editTextChat)
    }

    /**
     * 30초마다 한번씩 라이브 영상 정보를 가져온다.
     */
    private fun getLiveInfo() {
        val streamKey = (activity as ShoppingCastActivity)?.streamKey
        val task = ShopTreeAsyncTask(context!!)
        task.getLiveInfo(streamKey, { result, obj ->
            try {
                val response = Gson().fromJson(obj.toString(), API98::class.java)
                if(response.data.size > 0) {
                    val json = Gson().toJson(response.data[0])  // 특정 라이브 정보 data 리스트 개수는 하나뿐임.
                    val data = Gson().fromJson(json, VOD.DataBeanX::class.java)

                    nickName.text = data?.userNick
                    memberCount.text = data?.viewerCount
                    textViewNotification.text = data?.strNoti
                    buttonLike?.text = StringUtils.getSnsStyleCountZeroBase(data?.favoriteCount!!.toInt())
                }
            } catch (e : java.lang.Exception) {
                Logger.p(e)
            }
        })
    }

    private fun startAnimation() {
        // circle animation
        var ani = AnimationUtils.loadAnimation(context, R.anim.rotate)
        ani.duration = 7000
        buttonProductManagement.startAnimation(ani)
    }

    private fun stopAnimation() {
        // circle animation
        if (buttonProductManagement != null) {
            buttonProductManagement.clearAnimation()
        }
    }

    private val chatActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            sendText("*")
            true
        }

        false
    }

    private val inputChangeListener = object:TextWatcher {
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

//    private val chatFocusListener = View.OnFocusChangeListener { v, hasFocus ->
//        Logger.e("Chat input UI focus changed. hasFocus? $hasFocus")
//        if (!hasFocus) {
//            SoftKeyboardUtils.hideKeyboard(v)
//        }
//
//        isChatModeEnabled = hasFocus
//        v.post {
//            try {
//                layoutChatInput?.visibility = if (hasFocus) View.VISIBLE else View.GONE
//
//                recyclerViewChat.visibility = if (hasFocus) View.GONE else View.VISIBLE
//                buttonChatMode.visibility = if (hasFocus) View.GONE else View.VISIBLE
//                buttonCameraToggle.visibility = if (hasFocus) View.GONE else View.VISIBLE
////                buttonEffect.visibility = if (hasFocus) View.GONE else View.VISIBLE
//                buttonRealTimeRank.visibility = if (hasFocus) View.GONE else View.VISIBLE
//                buttonProductManagement.visibility = if (hasFocus) View.GONE else View.VISIBLE
//            } catch (ignore: Exception) {
//            }
//        }
//    }

    private val clickListener: View.OnClickListener = View.OnClickListener {
        Logger.d("clickListener:")

        when (it?.id) {
            R.id.buttonFilter -> {
                showBeauty()
            }
            R.id.fullLayer -> toogleFullLayout()
            R.id.clickLayout -> showFullLayout()

            R.id.chatInput -> {
                editTextChat.requestFocus()
                editTextChat.postDelayed({
                    val inputMethodManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    inputMethodManager!!.showSoftInput(editTextChat, 0)
                }, 100)
            }

            R.id.buttonPause -> {
                if ((it as AppCompatCheckBox).isChecked) {
                    (activity as? ShoppingCastActivity)?.onCastPause()
                    bgPause.visibility = View.VISIBLE
                } else {
                    (activity as? ShoppingCastActivity)?.onCastResume()
                    bgPause.visibility = View.GONE
                }
            }

            R.id.buttonExit -> confirmExit()
            R.id.buttonCameraToggle -> (activity as? ShoppingCastActivity)?.switchCamera()
            R.id.buttonProductManagement -> broadCastProductItem()
            R.id.buttonRealTimeRank -> (activity as? ShoppingCastActivity)?.showProductDialog()

//            R.id.buttonChatMode -> {
//                editTextChat.requestFocus()
//                SoftKeyboardUtils.showKeyboard(editTextChat)
//            }

            R.id.buttonSendMsg -> {
                sendText("*")
                hideChatMode()
            }

            R.id.buttonViewerList -> {
                Logger.e("################ chatKey : $mRoomID")
                userListDialog?.dismiss()
                userListDialog = null
                userListDialog = LiveUserListDialog(view!!.context).apply {
                    setItems(mUserMap)
                    setChatKey(mRoomID)
                    show()
                }
            }

            R.id.buttonMore -> {
                if (layoutMore.visibility == View.VISIBLE) {
                    layoutMore.visibility = View.GONE
                } else {
                    layoutMore.visibility = View.VISIBLE
                    layoutMore.bringToFront()
                }
            }

            R.id.layoutMore -> layoutMore.visibility = View.GONE

            R.id.buttonVideo -> startActivityForResult(Intent(context, VODSelectActivity::class.java), VOD_SELECT_ACTIVITY_RESULT)
            R.id.textViewBroadcastingTitleModify -> {
                layoutMore.visibility = View.GONE
                showTitleModify()
            }
            R.id.textViewBroadcastingNoticeModify -> {
                layoutMore.visibility = View.GONE
                showNotice()
            }
            R.id.textViewBroadcasting -> {
                layoutMore.visibility = View.GONE
                shareMyBroadcasting()
            }
            R.id.notification_close -> castLayer.visibility = View.GONE
            R.id.likeButtonLayer -> chatManager?.sendHeartSignal()
        }
    }

    private var mSubTouchStartX: Float = 0f
    private var mSubTouchStartY: Float = 0f
    private var mLastRawX: Float = 0f
    private var mLastRawY: Float = 0f
    private var mLastX: Float = 0f
    private var mLastY: Float = 0f
    private var mSubMaxX: Float = 0f
    private var mSubMaxY: Float = 0f
    private var mIsSubMoved: Boolean = false
    private val SUB_TOUCH_MOVE_MARGIN = 26

    private val touchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            mLastRawX = event!!.rawX
            mLastRawY = event!!.rawY

            val width = v!!.width
            val height = v!!.height
            val subRect = mStreamer?.getSubScreenRect()
            var left = 0
            var right = 0
            var top = 0
            var bottom = 0
            if (subRect != null) {
                left = (subRect!!.left * width).toInt()
                right = (subRect!!.right * width).toInt()
                top = (subRect!!.top * height).toInt()
                bottom = (subRect!!.bottom * height).toInt()
            }
            val subWidth = right - left
            val subHeight = bottom - top

            when (event!!.action) {
                MotionEvent.ACTION_UP -> {
                    if (!mIsSubMoved && isSubScreenArea(event.x, event.y, left, right, top, bottom)) {
                        mStreamer?.switchMainScreen()
                    } else {
                        //hideFullLayout()
                    }

                    mIsSubMoved = false
                    mSubTouchStartX = 0f
                    mSubTouchStartY = 0f
                    mLastX = 0f
                    mLastY = 0f
                }

                MotionEvent.ACTION_DOWN -> {
                    if (isSubScreenArea(event.x, event.y, left, right, top, bottom)) {
                        mSubTouchStartX = event.x - left
                        mSubTouchStartY = event.y - top
                        mLastX = event.x
                        mLastY = event.y
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    val moveX = Math.abs(event.x - mLastX).toInt()
                    val moveY = Math.abs(event.y - mLastY).toInt()
                    if (mSubTouchStartX > 0f && mSubTouchStartY > 0f && (moveX > SUB_TOUCH_MOVE_MARGIN || moveY > SUB_TOUCH_MOVE_MARGIN)) {
                        mIsSubMoved = true
                        updateSubPosition(width, height, subWidth, subHeight)
                    }
                }
            }
            return true
        }
    }

    private fun isSubScreenArea(x: Float, y: Float, left: Int, right: Int, top: Int, bottom: Int): Boolean {
        return if (x > left && x < right && y > top && y < bottom) {
            true
        } else false
    }

    private val BG_GAP = 20
    private fun updateSubPosition(screenWidth: Int, screenHeight: Int, subWidth: Int, subHeight: Int) {
        mSubMaxX = (screenWidth - subWidth - BG_GAP).toFloat()
        mSubMaxY = (screenHeight - subHeight - BG_GAP).toFloat()

        var newX = mLastRawX - mSubTouchStartX
        var newY = mLastRawY - mSubTouchStartY

        if (newX < BG_GAP) {
            newX = BG_GAP.toFloat()
        }
        if (newY < BG_GAP) {
            newY = BG_GAP.toFloat()
        }
        if (newX > mSubMaxX) {
            newX = mSubMaxX
        }
        if (newY > mSubMaxY) {
            newY = mSubMaxY
        }

        val subRect = mStreamer?.getSubScreenRect()
        val width = subRect!!.width()
        val height = subRect!!.height()

        val left = newX / screenWidth
        val top = newY / screenHeight

        mStreamer?.setSubScreenRect(left, top, width, height)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VOD_SELECT_ACTIVITY_RESULT && resultCode == Activity.RESULT_OK) {
            val filePath = data?.getStringExtra("path")
            Logger.e("onActivityResult:$filePath")

            //mStreamer?.showBgPicture()
            //mPipVideoPath = filePath

            //(activity as? ShoppingCastActivity)?.startPip(filePath as String)
            mStreamer = (activity as? ShoppingCastActivity)?.getStream()
            mStreamer?.hideBgVideo()
            mStreamer?.showBgVideo(filePath as String)
            mStreamer?.setCameraPreviewRect(0.7f, 0.01f, 0.27f, 0.247f)
            mStreamer?.switchMainScreen()

//            MediaSource firstSource =
//            new ExtractorMediaSource.Factory(...).createMediaSource(firstVideoUri);
//            MediaSource secondSource =
//            new ExtractorMediaSource.Factory(...).createMediaSource(secondVideoUri);
//// Plays the first video, then the second video.
//            ConcatenatingMediaSource concatenatedSource =
//            new ConcatenatingMediaSource(firstSource, secondSource);
        }
    }

    private fun receivedCookie() {
        if (mCookieQueue.isEmpty()) {
            return
        }

        var cookieData = mCookieQueue.poll()
        var receivedCnt = cookieData.cnt
        var senderStr = cookieData.sender

        if (isAdded) {
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
                mHandler.sendEmptyMessageDelayed(HIDE_COOKIE, 3000)
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


        cookieLayer.visibility = View.VISIBLE

        mCookieSum += receivedCnt

        mHandler.post(Runnable {
            cookieCount.text = mCookieSum.toString()
        })

        EventBus.getDefault().post(NetworkBus(NetworkApi.API82.name, (activity as ShoppingCastActivity).streamKey))
    }

    private fun hideCookie() {
        mHandler.post(Runnable {
            cookieLayer.visibility = View.GONE

            if (cookieImg != null) {
                try {
                    (cookieImg.drawable as GifDrawable).recycle()
                } catch (e: Exception) {
                }
            }
        })

        if (!mCookieQueue.isEmpty()) {
            mHandler.sendEmptyMessageDelayed(SHOW_COOKIE, 300)
        }
    }

    /**
     * 장바구니 담기/구매하기 팝업 표시
     *
     * 사용자 큰 이미지 URL https://api.puddinglive.com:8080/v1/user/{유저 ID}/image
     * 사용자 작은 이미지 https://api.puddinglive.com:8080/v1/user/{userId}/image/small
     * 10명까지는 즉시 , 이후는 1분 단위로 누구 외 몇명이 입장하였습니다. 라는 형태로 진행
     */
    private fun showUserActionPopup() {
        if (context == null) {
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

                mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_USER_ACTION_POPUP, 5000)
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

                ++mProductCount
                productCount.text = mProductCount.toString()

                mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_USER_ACTION_POPUP, 5000)
            }
        }
    }

    private fun hideUserActionPopup() {
        userActionPopLayout.visibility = View.GONE
    }

    private fun showUserJoinPopup() {
        if (context == null) {
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

                mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_USER_JOIN_POPUP, 5000)
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

                    mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_USER_JOIN_POPUP, 5000)
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

    private fun setProductCount() {
        ++mProductCount
        productCount.text = mProductCount.toString()
    }

//    private val chatInputChangeListener = View.OnLayoutChangeListener { v, _, top, _, _, _, oldTop, _, _ ->
//        var currentY = top
//        var oldY = oldTop
//
////        Logger.e("ChatInput position : current($currentY), old($oldY)")
//
//        if (currentY != oldY) {
//            layoutChatInput.postDelayed({
//                if (layoutChatInput != null && editTextChat != null) {
//                    try {
//                        var posYDiff = noChatLayoutYPos - layoutChatInput.y
//                        var isFocused = editTextChat.isFocused
//                        Logger.e("ChatInput Position Changed : Diff Y($posYDiff), isUIFocus ? $isFocused")
//
//                        if (posYDiff < chatInputPositionOffset) {
//                            layoutChatInput.visibility = View.GONE
//                        }
//                    } catch (ignore: Exception) {
//                    }
//                }
//            }, SOFT_KEYBOARD_TRANSITION_WAIT_DURATION)
//        }
//    }

    private fun broadCastProductItem() {
        if (!mIsSchedule) {
            var array: List<API72.RelationPrdBean.ProductItem> = (activity as ShoppingCastActivity).productArray
            productListDialog?.dismiss()

            var modelArray = ArrayList<DialogModel>()
            if (array.isNotEmpty()) {
                for (bean in array) {
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
                    dialogModel.streamKey = (activity as ShoppingCastActivity).streamKey
                    dialogModel.wish_cnt = bean.wish_cnt
                    dialogModel.is_wish = bean.is_wish
                    dialogModel.is_cart = bean.is_cart
                    //dialogModel.vodType = mData.videoType
                    modelArray.add(dialogModel)
                }

                productListDialog = PlayerProductDialog(view!!.context, modelArray, null, true).apply { show() }
            }
        } else {
            var items: List<API136.Products.ProductItem> = (activity as ShoppingCastActivity).productItems
            productListDialog?.dismiss()

            var modelArray = ArrayList<DialogModel>()
            if (items.isNotEmpty()) {
                for (bean in items) {
                    var dialogModel = DialogModel()
                    dialogModel.idx = bean.idx
                    dialogModel.scCode = bean.sc_code
                    dialogModel.pcode = bean.pcode
                    dialogModel.type = bean.strType
                    dialogModel.thumbNail = bean.strPrdImg
                    dialogModel.linkUrl = bean.strLinkUrl
                    dialogModel.name = bean.strPrdName
                    dialogModel.custPrice = bean.nPrdCustPrice
                    dialogModel.sellPrice = bean.nPrdSellPrice
                    dialogModel.streamKey = (activity as ShoppingCastActivity).streamKey
                    //dialogModel.vodType = mData.videoType
                    modelArray.add(dialogModel)
                }

                productListDialog = PlayerProductDialog(view!!.context, modelArray, null, true).apply { show() }
            }
        }
    }

    private val sendChatReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Logger.e("sendChatReceiver receive action :: $action")
            if (SEND_CHAT == action) {
                var gubun = intent.getStringExtra("GUBUN")
                var productName = intent.getStringExtra("NAME")
                if (gubun == ChatManager.GUBUN_CART) {
                    if (chatManager != null)
                        chatManager!!.sendCartSignal(ChatManager.COOKIE_ETC_CHAT_CMD, "", productName, mNickName)
                } else if (gubun == ChatManager.GUBUN_BUY) {
                    chatManager!!.sendBuySignal(ChatManager.COOKIE_ETC_CHAT_CMD, "", productName, mNickName)
                }
            }
        }
    }

//    inner class CookieThread : Thread() {
//        init {
//            stopped = false
//        }
//
//        fun stopThread() {
//            stopped = true
//        }
//
//        override fun run() {
//            while (!stopped && !mCookieQueue.isEmpty()) {
//                mHandler.sendEmptyMessage(0)
//                try {
//                    Thread.sleep(1500)
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }
}