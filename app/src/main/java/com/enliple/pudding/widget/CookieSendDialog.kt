package com.enliple.pudding.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDialog
import com.enliple.pudding.R
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API21
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.keyboard.KeyboardHeightProvider
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_cookie_send.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 방송 도중 푸딩 전송하기 팝업
 * @author hkcha
 * @since 2018.08.27
 */
class CookieSendDialog : AppCompatDialog {

    private val userId: String
    private val receiveId: String
    private var remainedCookie: Int = 0
    private var transferCookie: Int = 0

    private var isRequestTransfer = false
    private var mListener: Listener? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var mKeyboardHeight = 0

    constructor(context: Context, userId: String, receiveId: String, remainCookie:String) : super(context, R.style.CookieSendDialogStyle) {
        this.userId = userId
        this.receiveId = receiveId

        if(remainCookie.isNotEmpty()) {
            this.remainedCookie = remainCookie.toInt()
        } else {
            this.remainedCookie = 0
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

//        var windowParams = window.attributes
//        windowParams.gravity = Gravity.BOTTOM
//        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
//        windowParams.height = context.resources.getDimensionPixelSize(R.dimen.cookie_send_dialog_height)
//        window.attributes = windowParams

        setCancelable(true)
        setCanceledOnTouchOutside(true)

        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_cookie_send, null, false))

        keyboardHeightProvider = KeyboardHeightProvider(context as Activity)
        Handler().postDelayed({ keyboardHeightProvider!!.start() }, 1000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editTextCookieCount.addTextChangedListener(textChangeListener)
        editTextCookieCount.onFocusChangeListener = manualInputFocusChangeListener

        imageViewCookie10.setOnClickListener(clickListener)
        imageViewCookie50.setOnClickListener(clickListener)
        imageViewCookie100.setOnClickListener(clickListener)
        imageViewCookie300.setOnClickListener(clickListener)
        imageViewCookie500.setOnClickListener(clickListener)
        imageViewCookie1000.setOnClickListener(clickListener)
        empty.setOnClickListener(clickListener)
        buttonSend.setOnClickListener(clickListener)
        buttonBuy.setOnClickListener(clickListener)

        enableCheckedChangedListener()

        EventBus.getDefault().register(this)

//        if(remainedCookie != 0) {
//            cookieCount.text = PriceFormatter.getInstance()?.getFormattedValue(remainedCookie)
//        } else {
//            EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(context)))
//        }
        // 다이얼로그 열릴때마다 쿠키 갯수 갱신하도록 수정
//        EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(context)))
        var task = ShopTreeAsyncTask(context!!)
        task.getUserInfo(object : ShopTreeAsyncTask.OnDefaultObjectCallbackListener{
            override fun onResponse(result: Boolean, obj: Any?) {
                Logger.e("result :: " + result)
                if ( result ) {
                    if ( obj != null ) {
                        var objectJson = obj as JSONObject
                        Logger.e("objectJson :: "  + objectJson.toString())
                        if (  objectJson != null ) {
                            try {
                                remainedCookie = objectJson.optInt("userCookie")
                                Logger.e("remainedCookie :: " + remainedCookie)
                                cookieCount.text = PriceFormatter.getInstance()?.getFormattedValue(remainedCookie)
                            } catch (e: Exception ) {
                                e.printStackTrace()
                            }
                        }
                    } else
                        Logger.e("obj null")
                }
            }
        })
    }

    override fun dismiss() {
        super.dismiss()
        Logger.e("dismiss")

        if (keyboardHeightProvider != null) {
            keyboardHeightProvider!!.close()
        }

        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val key = NetworkHandler.getInstance(context).getKey(NetworkApi.API21.toString(), AppPreferences.getUserId(context)!!, "")
        if (data.arg1 == key) {
            if ("ok" == data.arg2) {
                var response: API21 = Gson().fromJson(DBManager.getInstance(context).get(data.arg1), API21::class.java)
                remainedCookie = response.userCookie.toInt()
                cookieCount.text = PriceFormatter.getInstance()?.getFormattedValue(remainedCookie)
            } else {
                var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
                Logger.e("error : $errorResult")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus:SoftKeyboardBus) {
        if(mKeyboardHeight == bus.height) {
            Logger.d("same height")
            return
        }

        Logger.e("SoftKeyboardBus height: ${bus.height}")
        mKeyboardHeight = bus.height

        if(mKeyboardHeight > 100) {
            keybordGapView.layoutParams.height = mKeyboardHeight
            keybordGapView.invalidate()
            keybordGapView.requestLayout()
        } else {
            keybordGapView.layoutParams.height = 0
        }
    }

    fun setListener(listener: Listener?) {
        mListener = listener
    }

    fun isKeyboardShow(isKeyboardShow: Boolean) {
        if (isKeyboardShow)
            empty.visibility = View.GONE
        else
            empty.visibility = View.VISIBLE
    }

    private fun enableCheckedChangedListener() {
        radioButtonCookie10.setOnCheckedChangeListener(checkedChangedListener)
        radioButtonCookie50.setOnCheckedChangeListener(checkedChangedListener)
        radioButtonCookie100.setOnCheckedChangeListener(checkedChangedListener)
        radioButtonCookie300.setOnCheckedChangeListener(checkedChangedListener)
        radioButtonCookie500.setOnCheckedChangeListener(checkedChangedListener)
        radioButtonCookie1000.setOnCheckedChangeListener(checkedChangedListener)
    }

    private fun disableCheckedChangedListener() {
        radioButtonCookie10.setOnCheckedChangeListener(null)
        radioButtonCookie50.setOnCheckedChangeListener(null)
        radioButtonCookie100.setOnCheckedChangeListener(null)
        radioButtonCookie300.setOnCheckedChangeListener(null)
        radioButtonCookie500.setOnCheckedChangeListener(null)
        radioButtonCookie1000.setOnCheckedChangeListener(null)
    }

    private fun releaseCheckedState() {
        radioButtonCookie10.isChecked = false
        radioButtonCookie50.isChecked = false
        radioButtonCookie100.isChecked = false
        radioButtonCookie300.isChecked = false
        radioButtonCookie500.isChecked = false
        radioButtonCookie1000.isChecked = false
    }

    /**
     * 푸딩 잔여 내역을 조회
     */
    private fun getCookieCount() {


//        cookieService.requestRemainedCookie(AppPreferences.getJWT(context),
//                AppPreferences.getUserId(context) ?: "")
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ response ->
//                    Logger.e("response : $response")
//                    Handler(Looper.getMainLooper()).post {
//                        Logger.e("Remained cookie : $remainedCookie")
//                        cookieCount.text = PriceFormatter.getInstance()?.getFormattedValue(remainedCookie)
//

//                        //cookieCount.text = PriceFormatter.getInstance()?.getFormattedValue(TEST_COOKIE_CNT)
//                        remainedCookie = response.remain
//                    }
//                }, { error ->
//                    Logger.e("User remained cookie send failed.\n($error)")
//                })
    }

    /**
     * 푸딩 전송 요청 , 차후 푸딩전송 api 이후 result 결과에 따라 호출하도록 해야함
     */
    private fun requestCookieTransfer(quantity: Int) {
        if (!isRequestTransfer) {
            isRequestTransfer = true
            transferCookie = quantity
            Handler().postDelayed({
                if (this != null && isShowing) {
                    dismiss()
                }
                mListener?.onCookieTransferred(userId, receiveId, transferCookie)
            }, 100L)
            pendingTransferUI()
        }
    }

//    private fun sendCookieToChat(transferCookie:Int) {
//        var cmd: String = VCommerceChatManager.NORMAL_CHAT_CMD
//        var color = ""
//        var sendText: String? = ""
//        if ( !TextUtils.isEmpty(live_editTextChat.text?.toString()) )
//            sendText = live_editTextChat.text.toString()
//        if (!TextUtils.isEmpty(sendText)) {                // 텍스트가 있을 경우에만 처리내용을 동작
//            sendText = filterReturnKeyInject(sendText)
//
//            if (!TextUtils.isEmpty(sendText)) {            // ReturnKey Filtering 이후 텍스트 여부 재확인
//                // 채팅 내용 전송 및 UI 업데이트
//                if (!TextUtils.isEmpty(sendText!!.replace("\n", "", true).trim())) {
//
//                    // 위의 조건을 모두 만족하였으면 채팅 내용을 전송
//                    chatManager?.sendTextChat(cmd, color, sendText, if (TextUtils.isEmpty(targetUser)) "*" else targetUser)
//                    live_editTextChat.setText("")
//                    chatLatestSendTime = currentTime
//                }
//            }
//        }
//    }

    /**
     * 푸딩 전송중 상태의 UI 상태를 설정
     */
    private fun pendingTransferUI() {
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        layoutStatus.visibility = View.INVISIBLE
        layoutCookie.visibility = View.INVISIBLE
        layoutDivider1.visibility = View.INVISIBLE
        layoutSending.visibility = View.INVISIBLE
    }

    /**
     * 푸딩 전송이 성공함
     * @param remainedCookie        전송 후 잔여 푸딩
     */
    private fun transferSuccessedUI(remainedCookie: Int) {
        layoutTransfer.visibility = View.INVISIBLE

        layoutStatus.visibility = View.INVISIBLE
        layoutCookie.visibility = View.INVISIBLE
        layoutDivider1.visibility = View.INVISIBLE
        layoutSending.visibility = View.INVISIBLE
        layoutTransferResult.visibility = View.VISIBLE

        textViewResultTitle.text = context.getString(R.string.msg_cookie_transfer_successfully)
        textViewResultContent.text = String.format(context.getString(R.string.msg_cookie_remained_format), remainedCookie)

        val transferredCookie = Integer.valueOf(transferCookie)
        this.transferCookie = 0

        Handler().postDelayed({
            if (this != null && isShowing) {
                dismiss()
            }
            mListener?.onCookieTransferred(userId, receiveId, transferredCookie)
        }, 1000L)
    }

    /**
     * 푸딩 전송을 실패함
     * @param cause         전송실패 사유
     */
    private fun transferFailedUI(cause: String) {
        layoutTransfer.visibility = View.INVISIBLE

        layoutStatus.visibility = View.INVISIBLE
        layoutCookie.visibility = View.INVISIBLE
        layoutDivider1.visibility = View.INVISIBLE
        layoutSending.visibility = View.INVISIBLE
        layoutTransferResult.visibility = View.VISIBLE

        textViewResultTitle.text = context.getString(R.string.msg_cookie_transfer_failed)
        textViewResultContent.text = "($cause)"

        this.transferCookie = 0

    }

    /**
     * 푸딩 팝업의 레이아웃 상태를 초기화
     */
    private fun reset() {
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        layoutStatus.visibility = View.VISIBLE
        layoutCookie.visibility = View.VISIBLE
        layoutDivider1.visibility = View.VISIBLE
        layoutSending.visibility = View.VISIBLE

        layoutTransferResult.visibility = View.INVISIBLE
        layoutTransfer.visibility = View.INVISIBLE
    }

    private fun getUserId(): String = userId

    private val manualInputFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        editTextCookieCount.isCursorVisible = hasFocus
    }

    private val textChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s?.toString()?.length ?: 0 > 0) {
                PriceFormatter().getFormattedValue(s!!.toString().replace(",", "").toInt())
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            var isFirstByZero = false
            for (number in 0 until (s?.toString()?.length ?: 0)) {
                if (number == '0'.toInt()) {
                    isFirstByZero = true
                    break
                }
            }

            if (isFirstByZero) {
                try {
                    editTextCookieCount.setText(s?.substring(1))
                } catch (e: Exception) {
                }
            }
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.imageViewCookie10 -> radioButtonCookie10.isChecked = true
            R.id.imageViewCookie50 -> radioButtonCookie50.isChecked = true
            R.id.imageViewCookie100 -> radioButtonCookie100.isChecked = true
            R.id.imageViewCookie300 -> radioButtonCookie300.isChecked = true
            R.id.imageViewCookie500 -> radioButtonCookie500.isChecked = true
            R.id.imageViewCookie1000 -> radioButtonCookie1000.isChecked = true
            R.id.buttonBuy -> {
                dismiss()
                mListener?.onCookieBuyRequested(getUserId())
            }

            R.id.buttonSend -> {
                Logger.e("val ::::::::: ${editTextCookieCount?.text.toString()}")
                var cookieCnt = editTextCookieCount?.text.toString()
                if (TextUtils.isEmpty(cookieCnt) || cookieCnt == "0") {
                    AppToast(it.context).showToastMessage("선물 할 푸딩 갯수를 확인해주세요",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                    return@OnClickListener
                }

                cookieCnt = cookieCnt.replace(",", "")
                var cookie = cookieCnt.toIntOrNull()
                if (cookie != null) {
                    if (cookie <= 0) {
                        return@OnClickListener
                    }

                    if (cookie > remainedCookie) {
                        mListener?.onNotEnoughCookie(remainedCookie - cookie)
                    } else {
                        requestCookieTransfer(cookie)
                    }
                }
            }

            R.id.empty -> {
                dismiss()
            }
        }
    }

    private val checkedChangedListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

        disableCheckedChangedListener()
        releaseCheckedState()

        if (isChecked) {
            when (buttonView?.id) {
                R.id.radioButtonCookie10 -> {
                    radioButtonCookie10.isChecked = true
                    editTextCookieCount.setText("10")
                }

                R.id.radioButtonCookie50 -> {
                    radioButtonCookie50.isChecked = true
                    editTextCookieCount.setText("50")
                }

                R.id.radioButtonCookie100 -> {
                    radioButtonCookie100.isChecked = true
                    editTextCookieCount.setText("100")
                }

                R.id.radioButtonCookie300 -> {
                    radioButtonCookie300.isChecked = true
                    editTextCookieCount.setText("300")
                }

                R.id.radioButtonCookie500 -> {
                    radioButtonCookie500.isChecked = true
                    editTextCookieCount.setText("500")
                }

                R.id.radioButtonCookie1000 -> {
                    radioButtonCookie1000.isChecked = true
                    editTextCookieCount.setText("1000")
                }
            }
        }

        enableCheckedChangedListener()
    }


    interface Listener {
        /**
         * 사용자로 부터 푸딩가 전송됨
         * @param sendUserId        푸딩를 보낸 사람의 아이디
         * @param receiveId         푸딩를 받은 사람의 아이디
         * @param quantity          전송 푸딩 수량
         */
        fun onCookieTransferred(sendUserId: String, receiveId: String, quantity: Int)

        /**
         * 푸딩를 보내기 위한 잔액이 부족함
         * @param enoughCookie      부족한 푸딩 수량
         */
        fun onNotEnoughCookie(enoughCookie: Int)

        /**
         * 사용자로 부터 푸딩 구매 요청이 들어옴
         * @param userId            구매를 진행할 현재 사용자 아이디
         */
        fun onCookieBuyRequested(userId: String)
    }
}