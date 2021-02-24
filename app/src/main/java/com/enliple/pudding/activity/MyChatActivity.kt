package com.enliple.pudding.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.MyChatAdapter
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusFastResponse
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API60
import com.enliple.pudding.commons.network.vo.API63
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.keyboard.KeyboardHeightProvider
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.fragment_review.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*

/**
 * Created by K,im Joonsung on 2018-10-24.
 */
class MyChatActivity : AbsBaseActivity(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {
    companion object {
        private const val RECYCLER_VIEW_TOP = -1
        private const val RECYCLER_VIEW_BOTTOM = 1

        private const val MESSAGE_MORE_API_CALL_TIME = 10000L    // 10초

        const val INTENT_EXTRA_KEY_CASTER_NICKNAME = "caster_nickname"
        const val INTENT_EXTRA_KEY_CASTER_PROFILE = "caster_profile"
        const val INTENT_EXTRA_KEY_CASTER_ID = "caster_id"
        const val INTENT_EXTRA_KEY_CASTER_FOLLOW = "is_follow"
        const val INTENT_EXTRA_KEY_CASTER_LEAVE = "is_user_leave"

        private lateinit var mKeyboardHeightProvider: KeyboardHeightProvider
    }

    private var casterId = ""
    private var profile = ""
    private var nickName = ""
    private var isFollow = false
    private var mIsBottom = true  // 리스트 bottom 일 경우만 position 이동을 해야 한다.
    private var mIsSendMessage = false
    private var mSendMessage = ""
    private var isUserLeave: String? = null

    private lateinit var mAdapter: MyChatAdapter
    private lateinit var mTimer: Timer
    private var layoutManager: androidx.recyclerview.widget.LinearLayoutManager? = null
    private val mTask = object : TimerTask() {
        override fun run() {
            if (mAdapter != null && mAdapter.itemCount > 0) {
                val item: API63.MyMessageItem? = mAdapter.getItem(mAdapter.itemCount - 1)
                if (item != null) {
                    EventBus.getDefault().post(NetworkBus(NetworkApi.API62.name, casterId, item?.no.toString()))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)

        EventBus.getDefault().register(this)

        emptyTouch.setOnTouchListener(object:View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if(event?.action == MotionEvent.ACTION_DOWN) {
                    editTextChat.clearFocus()
                    SoftKeyboardUtils.hideKeyboard(editTextChat)
                }

                return false
            }
        })

        buttonClose.setOnClickListener(clickListener)
        buttonSendMsg.setOnClickListener(clickListener)
        viewFollow.setOnClickListener(clickListener)
        viewFollowLater.setOnClickListener(clickListener)
        //buttonAttach.setOnClickListener(clickListener)
        //buttonChatEmoticon.setOnClickListener(clickListener)
        editTextChat.setOnEditorActionListener(editorActionListener)
        editTextChat.addTextChangedListener(inputTextWatcher)
        editTextChat.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event!!.action == MotionEvent.ACTION_DOWN) {
                    if ("Y" == isUserLeave) {
                        AppToast(this@MyChatActivity).showToastMessage("탈퇴한 회원입니다.",
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM)
                        return true
                    }
                }

                return false
            }
        })
        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@MyChatActivity)
        layoutManager!!.orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
        recyclerViewChat.layoutManager = layoutManager
        recyclerViewChat.setHasFixedSize(false)
        recyclerViewChat.addOnScrollListener(scrollListener)
        swipeLayout.setOnRefreshListener(this)
        mAdapter = MyChatAdapter(this@MyChatActivity)

        recyclerViewChat.adapter = mAdapter

        buttonSendMsg.isEnabled = false

        checkIntent(intent)

        mTimer = Timer()
        mTimer.schedule(mTask, MESSAGE_MORE_API_CALL_TIME, MESSAGE_MORE_API_CALL_TIME)

        // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
        mKeyboardHeightProvider = KeyboardHeightProvider(this)
        Handler().postDelayed(Runnable { mKeyboardHeightProvider.start() }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()

        mTimer.cancel()

        EventBus.getDefault().post(NetworkBus("refresh_follow"))

        EventBus.getDefault().unregister(this)

        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider.close()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        mTimer.cancel()
        finish()
    }

    override fun onRefresh() {
        val item: API63.MyMessageItem? = mAdapter.getItem(0)
        Logger.e("onRefresh list number:${item?.no}")
        EventBus.getDefault().post(NetworkBus(NetworkApi.API61.name, casterId, item?.no.toString()))
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:NetworkBusFastResponse) {
        if (data.arg1.startsWith(NetworkApi.API2.toString())) {   // Follow
            if (data.arg2 == "ok") {
                layoutFollow.visibility = View.GONE
                divider.visibility = View.GONE
            } else {
                Logger.e("error : ${data.arg3} ${data.arg4}")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if (data.arg1.startsWith(NetworkApi.API63.toString())) {         // 최근 메시지
            handleNetworkAPI63(data)
        } else if (data.arg1.startsWith(NetworkApi.API62.toString())) {  // 신규 메시지
            handleNetworkAPI62(data)
        } else if (data.arg1.startsWith(NetworkApi.API61.toString())) {  // 이전 메시지
            handleNetworkAPI61(data)
        } else if (data.arg1.startsWith(NetworkApi.API60.toString())) {  // 새로운 메시지 건수
            handleNetworkAPI60(data)
        } else if (data.arg1.startsWith(NetworkApi.API59.toString())) {  // 1:1 메시지 작성
            handleNetworkAPI59(data)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
        Logger.e("height:" + bus.height)

        // 키보드가 올라가면 리스트 제일 끝으로 스크롤
        if (bus.height > 100) {
//            var param = emptyView.layoutParams
//            param.height = bus.height
//            emptyView.layoutParams = param

            emptyTouch.visibility = View.VISIBLE
            emptyView?.layoutParams?.height = bus.height
            emptyView?.invalidate()
            emptyView?.requestLayout()

            if(editTextChat.text.toString().isNotEmpty()) {
                buttonSendMsg.isEnabled = true
                buttonSendMsg.setBackgroundResource(R.drawable.send_on_ico)
            }

            recyclerViewChat.scrollToPosition(mAdapter.itemCount - 1)
        } else {
            // 키보드가 내려가면 아무 동작도 하지 않고 키보드만 내려감
//            var param = emptyView.layoutParams
//            param.height = 0
//            emptyView.layoutParams = param

            emptyTouch.visibility = View.GONE
            emptyView?.layoutParams?.height = 0
            emptyView?.invalidate()
            emptyView?.requestLayout()
            buttonSendMsg.isEnabled = false
            buttonSendMsg.setBackgroundResource(R.drawable.send_off_ico)
        }
    }

    /**
     * 최근 메시지 Response
     */
    private fun handleNetworkAPI63(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API63 = Gson().fromJson(DBManager.getInstance(this@MyChatActivity).get(data.arg1), API63::class.java)
            Logger.e("message totalCount : ${response.nTotalCount}")

            if (response.nTotalCount > 0) {
                mAdapter.setItems(response.data)
                recyclerViewChat.scrollToPosition(response.data.size - 1)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }

        if (mIsSendMessage) {
            //editTextChat.hint = "메시지 입력.."
        }
    }

    /**
     * 신규 메시지 Response
     */
    private fun handleNetworkAPI62(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API63 = Gson().fromJson(DBManager.getInstance(this@MyChatActivity).get(data.arg1), API63::class.java)
            Logger.d("message totalCount : ${response.nTotalCount}")

            if (response.nTotalCount > 0) {
                mAdapter.addItems(response.data, true)

                if (mIsBottom || mIsSendMessage) {
                    recyclerViewChat.scrollToPosition(mAdapter.itemCount - 1)
                }
            }
        } else {
            //var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : ${data.arg3}")
        }

        if (mIsSendMessage) {
            //editTextChat.hint = "메시지 입력.."
            mIsSendMessage = false
        }
    }

    /**
     * 이전 메시지 Response
     */
    private fun handleNetworkAPI61(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API63 = Gson().fromJson(DBManager.getInstance(this@MyChatActivity).get(data.arg1), API63::class.java)
            Logger.e("message totalCount : ${response.nTotalCount}")

            if (response.nTotalCount > 0) {
                mAdapter.addItems(response.data, false)

                recyclerViewChat.scrollBy(0, -100)
            } else {
                //var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
                Logger.e("error : $data")

                AppToast(this).showToastMessage("이전 메세지가 없습니다..",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_MIDDLE)
            }

            swipeLayout.isRefreshing = false
        }
    }

    /**
     * 새로운 메시지 개수 Response
     */
    private fun handleNetworkAPI60(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API60 = Gson().fromJson(DBManager.getInstance(this@MyChatActivity).get(data.arg1), API60::class.java)
            Logger.e("message totalCount : ${response.nTotalCount}")

        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    /**
     * 1:1 메시지 작성 Response
     */
    private fun handleNetworkAPI59(data: NetworkBusResponse) {
        mIsSendMessage = false

        if ("ok" == data.arg2) {
            editTextChat.setText("")
            mIsSendMessage = true

            val item = mAdapter.getItem(mAdapter.itemCount - 1)
            EventBus.getDefault().post(NetworkBus(NetworkApi.API62.name, casterId, item?.no.toString()))
        } else {
            //val errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : ${data.arg3}")

            mIsSendMessage = false

            editTextChat.setText(mSendMessage)

            AppToast(this).showToastMessage("메시지 전송 실패!! 네트워크를 확인 해 주세요..",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_MIDDLE)
        }
    }

    private fun sendMessage() {
        mIsSendMessage = false

        mSendMessage = editTextChat.text.toString()
        var tempStr = mSendMessage.trim()

        if (tempStr != null && tempStr.length > 0) { // TextUtils.isEmpty 만으로는 공백으로만 이루어진 문자열 check가 안됨
            //editTextChat.setText("")
            //editTextChat.hint = "전송 중..."

            val requestBody = JSONObject().apply {
                put("user", AppPreferences.getUserId(this@MyChatActivity))
                put("partner", casterId)
                put("content", mSendMessage)
            }
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody.toString())
            EventBus.getDefault().post(NetworkBus(NetworkApi.API59.name, body))
        }
    }

    private fun checkIntent(intent: Intent?) {
        if (intent != null) {
            profile = intent.getStringExtra(INTENT_EXTRA_KEY_CASTER_PROFILE)
            nickName = intent.getStringExtra(INTENT_EXTRA_KEY_CASTER_NICKNAME)
            casterId = intent.getStringExtra(INTENT_EXTRA_KEY_CASTER_ID)
            isFollow = intent.getBooleanExtra(INTENT_EXTRA_KEY_CASTER_FOLLOW, isFollow)
            isUserLeave = intent.getStringExtra(INTENT_EXTRA_KEY_CASTER_LEAVE)

            if (!TextUtils.isEmpty(profile)) {
                ImageLoad.setImage(this@MyChatActivity,
                        imageViewThumbnail,
                        profile,
                        null,
                        ImageLoad.SCALE_CIRCLE_CROP,
                        DiskCacheStrategy.ALL)
            } else {
                imageViewThumbnail.setBackgroundResource(R.drawable.profile_default_img)
            }

            if (isFollow) {
                layoutFollow.visibility = View.GONE
                divider.visibility = View.GONE
            } else {
                layoutFollow.visibility = View.VISIBLE
                divider.visibility = View.VISIBLE
            }

            textViewNickName.text = nickName
        }

        EventBus.getDefault().post(NetworkBus(NetworkApi.API63.name, casterId))
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()
            R.id.viewFollow -> {
                var userId = AppPreferences.getUserId(this@MyChatActivity)
                if (!TextUtils.isEmpty(userId)) {
                    val requestObj = JSONObject()
                    requestObj.put("strUserId", userId)
                    requestObj.put("strToUserId", casterId)
                    requestObj.put("isFollow", "Y")
                    val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString())
                    EventBus.getDefault().post(NetworkBus(NetworkApi.API2.name, body))
                }
            }

            R.id.buttonSendMsg -> {
                sendMessage()
            }

            R.id.viewFollowLater -> {
                layoutFollow.visibility = View.GONE
                divider.visibility = View.GONE
            }
        }
    }

    private val editorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            sendMessage()
        }

        false
    }

    private val inputTextWatcher = object : TextWatcher {
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

    private val scrollListener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
            if (!recyclerViewChat.canScrollVertically(RECYCLER_VIEW_BOTTOM)) {
                Logger.d("BOTTOM POSITION !!")
                mIsBottom = true
            } else {
                Logger.d("LIST MOVED !!")
                mIsBottom = false
            }
        }
    }
}