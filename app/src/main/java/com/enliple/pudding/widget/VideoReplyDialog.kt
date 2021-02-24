package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.adapter.home.ExpandReplyAdapter
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.bus.VideoReplyCountBus
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.data.ApiParams
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API17
import com.enliple.pudding.commons.network.vo.API21
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.keyboard.KeyboardHeightProvider
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_reply.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * VOD 댓글
 */
open class VideoReplyDialog : androidx.fragment.app.DialogFragment(), View.OnClickListener, ExpandReplyAdapter.OnPopupPositionListener {
    private var isEditMode = false
    private var selectedItem: API17.BroadcastReplyItem? = null
    private var selectedChildItem:API17.BroadcastReplyItem.ChildReplyItem? = null
    private var selectedPosition = -1
    private var selectedChidPosition = -1
    private var popTopMargin = 0
    private var mStreamId = ""
    private var mCommentYN = ""
    private var mKeyboardHeight = 0
    private var mTotalCount = 0
    private var mCreatorNickname = ""
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var mGroupIdx = ""
    private var mReplyUser = ""
    private var mIsRelpyClicked = false
    private var isDialogClose = false

    private lateinit var mReplyAdapter:ExpandReplyAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_reply, container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_FRAME, R.style.VideoReplyDialog)

        mStreamId = arguments!!.getString("stream")
        mCommentYN = arguments!!.getString("comment_YN")
        mCreatorNickname = arguments!!.getString("creatorNickname")
        Logger.e("onCreate: id$mStreamId, comment_YN : $mCommentYN")

        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        
        mReplyAdapter = ExpandReplyAdapter(context!!).apply {
            setListener(this@VideoReplyDialog)
            setCreatorNickName(mCreatorNickname)
        }

        expandReplyList.setAdapter(mReplyAdapter)

        editTextChat.setOnEditorActionListener(chatActionListener)
        editTextChat.addTextChangedListener(inputChangeListener)

        buttonClose.setOnClickListener(this)
        buttonSend.setOnClickListener(this)
        editBtn.setOnClickListener(this)
        deleteBtn.setOnClickListener(this)
        mainLayer.setOnClickListener(this)
        editTextView.setOnClickListener(this)
        mainLayer.setOnClickListener(this)
        replyCancel.setOnClickListener(this)

        buttonSend.isEnabled = false

        if("Y" == mCommentYN) {
            progressBar.visibility = View.VISIBLE
            val task = ShopTreeAsyncTask(context!!)
            task.getReplyList(mStreamId, {
                result, obj ->
                    try {
                        handleNetworkResultReply(obj as JSONObject)
                        progressBar.visibility = View.GONE
                    } catch (e:Exception) {
                        Logger.p(e)
                    }
            })

            EventBus.getDefault().post(NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(context!!)))

            keyboardHeightProvider = KeyboardHeightProvider(activity!!)
            Handler().postDelayed({ keyboardHeightProvider!!.start() }, 1000)
        } else {
            expandReplyList.visibility = View.GONE
            input.visibility = View.GONE
            replyDisable.visibility = View.VISIBLE

            textViewReplyCnt?.text = "0"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.e("onDestroy")

        if (keyboardHeightProvider != null) {
            keyboardHeightProvider!!.close()
        }

        EventBus.getDefault().post(VideoReplyCountBus(mTotalCount))

        EventBus.getDefault().unregister(this)
    }

    override fun onReplyClicked(item: API17.BroadcastReplyItem, position: Int) {
        isEditMode = false
        mIsRelpyClicked = true
        popupLayer?.visibility = View.GONE

        mGroupIdx = item.idx
        mReplyUser = item.mb_nick

        showKeyboard()

        editTextChat?.setText("")
    }

    override fun onReplyDel(idx: String) {

    }

    override fun getChildPopupStartY(y: Int, item: API17.BroadcastReplyItem.ChildReplyItem, gropPos:Int,childPos: Int) {
        Logger.e("############# childPos:$childPos, gropuPos:$gropPos")
        if (popupLayer.visibility == View.GONE) {
            Logger.e("visibility gone y :: $y")
            selectedChildItem = item
            selectedChidPosition = childPos
            selectedPosition = gropPos
            popTopMargin = y

            val params = RelativeLayout.LayoutParams(Utils.ConvertDpToPx(context, 80), Utils.ConvertDpToPx(context, 98))
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            params.setMargins(0, y, Utils.ConvertDpToPx(context, 40), 0)
            popup.layoutParams = params
            popupLayer.visibility = View.VISIBLE
        } else {
            Logger.e("visibility visible")
            popupLayer.visibility = View.GONE
            isEditMode = false
            selectedChildItem = null
            selectedChidPosition = -1
            selectedPosition = -1
        }
    }

    override fun getPopupStartY(y: Int, item: API17.BroadcastReplyItem, position: Int) {
        Logger.e("############# groupPos:$position")
        if (popupLayer.visibility == View.GONE) {
            Logger.e("visibility gone y :: $y")
            selectedItem = item
            selectedPosition = position
            popTopMargin = y

            val params = RelativeLayout.LayoutParams(Utils.ConvertDpToPx(context, 80), Utils.ConvertDpToPx(context, 98))
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            params.setMargins(0, y, Utils.ConvertDpToPx(context, 40), 0)
            popup.layoutParams = params
            popupLayer.visibility = View.VISIBLE
        } else {
            Logger.e("visibility visible")
            popupLayer.visibility = View.GONE
            isEditMode = false
            selectedItem = null
            selectedPosition = -1
        }
    }

    override fun getGroupCount(count: Int) {
        textViewReplyCnt.text = String.format(view!!.context.getString(R.string.msg_reply_count), count)
    }

    @Subscribe
    fun onMessageEvent(data: NetworkBusResponse) {
        if (data.arg1.startsWith(NetworkApi.API18.toString())) {
            handleNetworkResultReplyInput(data)
        } else if (data.arg1.startsWith(NetworkApi.API19.toString())) {
            handleNetworkResultReplyDel(data)
        } else if (data.arg1.startsWith(NetworkApi.API20.toString())) {
            handleNetworkResultReplyModify(data)
        } else if(data.arg1 == "GET/user/${AppPreferences.getUserId(context!!)!!}") {
            if("ok" == data.arg2) {
                val response:API21 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API21::class.java)

                if (TextUtils.isEmpty(response.userIMG)) {
                    imageViewProfile.setBackgroundResource(R.drawable.profile_default_img)
                    imageViewProfile1.setBackgroundResource(R.drawable.profile_default_img)
                } else {
                    ImageLoad.setImage(context!!, imageViewProfile, response.userIMG, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                    ImageLoad.setImage(context!!, imageViewProfile1, response.userIMG, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
        if (mKeyboardHeight == bus.height) {
            Logger.d("same height")
            return
        }

        Logger.e("SoftKeyboardBus height: ${bus.height}")

        mKeyboardHeight = bus.height

        if (mKeyboardHeight > 100) {
            expandReplyList?.layoutParams?.height = AppPreferences.getScreenHeight(context!!) - mKeyboardHeight - Utils.ConvertDpToPx(context!!, 130)
            replyEmptyLayer?.layoutParams?.height = AppPreferences.getScreenHeight(context!!) - mKeyboardHeight - Utils.ConvertDpToPx(context!!, 130)
            chatGapView?.layoutParams?.height = mKeyboardHeight
            //chatGapView?.forceLayout()
            chatGapView?.requestLayout()
            empty?.layoutParams?.height = mKeyboardHeight
        } else {
            chatLayout?.visibility = View.GONE
            expandReplyList?.layoutParams?.height = Utils.ConvertDpToPx(context!!, 320)
            replyEmptyLayer?.layoutParams?.height = Utils.ConvertDpToPx(context!!, 320)
            empty?.layoutParams?.height = 0
            editTextChat?.setText("")
            buttonSend.isEnabled = false
            buttonSend.setBackgroundResource(R.drawable.send_off_ico)
            mIsRelpyClicked = false

            if(isEditMode && !isDialogClose) {
                val sb = SpannableStringBuilder("입력을 취소 하시겠습니까?").apply {
                    setSpan(ForegroundColorSpan(Color.parseColor("#ff6c6c")), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                val dialog = AppAlertDialog(context!!)
                dialog.setTitle("")
                dialog.setMessage(sb)
                dialog.setLeftButton("취소", View.OnClickListener {
                    dialog.dismiss()
                    Handler().postDelayed({editMessage()}, 500)
                })
                dialog.setRightButton("확인", View.OnClickListener {
                    isEditMode = false
                    selectedItem = null
                    selectedPosition = -1
                    selectedChildItem = null
                    selectedChidPosition = -1
                    selectedPosition = -1
                    dialog.dismiss()
                })

                dialog!!.show()
            }
        }
    }

    /**
     * VOD 댓글 수정 Response 처리
     */
    private fun handleNetworkResultReplyModify(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val task = ShopTreeAsyncTask(context!!)
            task.getReplyList(mStreamId, {
                result, obj ->
                try {
                    handleNetworkResultReply(obj as JSONObject)

                    AppToast(context!!).showToastMessage("댓글이 수정 되었습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    selectedChildItem = null
                    selectedItem = null
                    selectedChidPosition = -1
                    selectedPosition = -1
                } catch (e:Exception) {
                    Logger.p(e)
                }
            })
        } else {
            Logger.e("error : ${data.arg1} ${data.arg2} ${data.arg3}")
        }
    }

    /**
     * VOD 댓글 등록 Response 처리
     */
    private fun handleNetworkResultReplyInput(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {

            val task = ShopTreeAsyncTask(context!!)
            task.getReplyList(mStreamId, {
                result, obj ->
                try {
                    handleNetworkResultReply(obj as JSONObject)

                    AppToast(context!!).showToastMessage("댓글이 등록 되었습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    EventBus.getDefault().post(NetworkBus(NetworkApi.API143.name, mStreamId)) // 현재 Video 정보 갱신

                    selectedChildItem = null
                    selectedItem = null
                    selectedChidPosition = -1
                    selectedPosition = -1
                } catch (e:Exception) {
                    Logger.p(e)
                }
            })
        } else {
            Logger.e("error : ${data.arg1} ${data.arg2} ${data.arg3}")
        }
    }

    /**
     * VOD 댓글 삭제 Response
     */
    private fun handleNetworkResultReplyDel(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val task = ShopTreeAsyncTask(context!!)
            task.getReplyList(mStreamId, {
                result, obj ->
                try {
                    handleNetworkResultReply(obj as JSONObject)

                    AppToast(context!!).showToastMessage("댓글이 삭제 되었습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    EventBus.getDefault().post(NetworkBus(NetworkApi.API143.name, mStreamId)) // 현재 Video 정보 갱신

                    selectedChildItem = null
                    selectedItem = null
                    selectedChidPosition = -1
                    selectedPosition = -1
                } catch (e:Exception) {
                    Logger.p(e)
                }
            })
        } else {
            Logger.e("error : ${data.arg1} ${data.arg2} ${data.arg3}")
        }
    }

    /**
     * VOD 댓글 목록을 가져온다.
     */
    private fun handleNetworkResultReply(obj:JSONObject) {
        var response: API17 = Gson().fromJson(obj.toString(), API17::class.java)
        if (response.result == "success") {
            mTotalCount = response.nTotalCount

            textViewReplyCnt?.text = mTotalCount.toString()

            if(mTotalCount > 0) {
                expandReplyList.visibility = View.VISIBLE
                replyEmptyLayer.visibility = View.GONE
                mReplyAdapter.setItems(response.data)
            } else {
                expandReplyList.visibility = View.GONE
                replyEmptyLayer.visibility = View.VISIBLE
            }

            mGroupIdx = ""
        } else {
            Logger.e("handleNetworkResultReply response.result not success")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonClose, R.id.mainLayer -> {
                if(isEditMode) {
                    val sb = SpannableStringBuilder("입력을 취소 하시겠습니까?").apply {
                        setSpan(ForegroundColorSpan(Color.parseColor("#ff6c6c")), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    val dialog = AppAlertDialog(context!!)
                    dialog.setTitle("")
                    dialog.setMessage(sb)
                    dialog.setLeftButton("취소", View.OnClickListener {
                        dialog.dismiss()
                        Handler().postDelayed({editMessage()}, 500)
                    })
                    dialog.setRightButton("확인", View.OnClickListener {
                        dialog.dismiss()
                        dismiss()
                    })

                    dialog.show()

                    isDialogClose = true
                } else {
                    hideKeyboard()
                    Handler().postDelayed({dismiss()}, 200)
                }
            }

            R.id.buttonSend -> {
                hideKeyboard()
                sendMessage()
            }

            R.id.editBtn -> editMessage()
            R.id.deleteBtn -> deleteMessage()
//            R.id.mainLayer -> {
//                hideKeyboard()
//                Handler().postDelayed({dismiss()}, 200)
//            }
            R.id.editTextView -> showKeyboard()
            R.id.replyCancel -> hideKeyboard()
        }
    }

    private fun deleteMessage() {
        isEditMode = false
        popupLayer?.visibility = View.GONE

        Logger.e("##################### selectedPosition:$selectedPosition, selectedChidPosition:$selectedChidPosition")

        var idx = ""
        if(selectedChidPosition == -1) {
            idx = mReplyAdapter.getGroup(selectedPosition).idx
        } else {
            idx = mReplyAdapter.getChild(selectedPosition, selectedChidPosition).idx
        }

        EventBus.getDefault().post(NetworkBus(NetworkApi.API19.name, idx))
    }

    private fun editMessage() {
        isEditMode = true
        popupLayer?.visibility = View.GONE

        if(selectedItem != null) {
            editTextChat?.setText(selectedItem?.comment)
        } else {
            editTextChat?.setText(selectedChildItem?.comment)
        }

        editTextChat?.setSelection(editTextChat.length())

        showKeyboard()
    }

    private fun sendMessage() {
        Logger.e("sendMessage")

        if (isEditMode!!) {
            var curStr = editTextChat.text.toString()
            var originStr = ""
            if(selectedItem != null) {
                originStr = selectedItem!!.comment
            } else if(selectedChildItem != null) {
                originStr = selectedChildItem!!.comment
            }

            Logger.e("string:$curStr , $originStr")

            if (TextUtils.isEmpty(curStr)) {
                AppToast(view!!.context).showToastMessage("수정할 내용을 입력해주세요",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            } else {
                if (curStr == originStr) {
                    AppToast(view!!.context).showToastMessage("값이 변경되지 않았습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                } else {
                    var idx = ""
                    if(selectedItem != null) {
                        idx = selectedItem!!.idx
                    } else if(selectedChildItem != null) {
                        idx = selectedChildItem!!.idx
                    }

                    var requestObj = ApiParams.ReplyModifyParams(
                            idx,
                            editTextChat.text.toString(),
                            AppPreferences.getUserId(context!!))

                    EventBus.getDefault().post(NetworkBus(NetworkApi.API20.name, requestObj))

                    isEditMode = false
                    editTextChat.setText("")
                }
            }
        } else {
            isEditMode = false

            var requestObj = ApiParams.ReplyInputParams(
                    mStreamId,
                    editTextChat.text.toString(),
                    AppPreferences.getUserId(context!!),
                    mGroupIdx)

            EventBus.getDefault().post(NetworkBus(NetworkApi.API18.name, requestObj))
            editTextChat.setText("")
        }
    }

    private val chatActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
        Logger.e("onEditorAction? $actionId")

        when (actionId) {
            EditorInfo.IME_ACTION_UNSPECIFIED,
            EditorInfo.IME_ACTION_SEND -> {
                sendMessage()
                true
            }
        }
        false
    }

    private val inputChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if(s?.length == 0) {
                buttonSend.isEnabled = false
                buttonSend.setBackgroundResource(R.drawable.send_off_ico)
            } else {
                buttonSend.isEnabled = true
                buttonSend.setBackgroundResource(R.drawable.send_on_ico)
            }
        }
    }

    private fun hideKeyboard() {
        chatLayout?.visibility = View.GONE
        replyInput.visibility = View.GONE
        mIsRelpyClicked = false

        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(editTextChat?.windowToken, 0)
    }

    private fun showKeyboard() {
        chatLayout?.visibility = View.VISIBLE

        if(!mIsRelpyClicked) {
            replyInput.visibility = View.GONE
        } else {
            replyInput.visibility = View.VISIBLE
            replyUser.text = mReplyUser
        }

        editTextChat?.requestFocus()

        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(editTextChat, 0)
    }
}