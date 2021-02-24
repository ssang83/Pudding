package com.enliple.pudding.fragment.findaccount

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.FindAccountActivity
import com.enliple.pudding.api.AccountPolicy
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.keyboard.KeyboardHeightProvider
import kotlinx.android.synthetic.main.fragment_find_email.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 아이디 찾기 - 본인명의 인증 Fragment
 * @author hkcha
 * @since 2018.08.16
 */
class MemberEmailFragment : AbsBaseFragment() {

    companion object {
        private const val TAG = "MemberEmailFragment"

        const val BUNDLE_EXTRA_KEY_CALL_MODE = "call_mode"
        const val BUNDLE_EXTRA_VALUE_FIND_ID = 1
        const val BUNDLE_EXTRA_VALUE_FIND_PW = 2

        private const val SECONDS = 1000L
        private const val MINUTE = SECONDS * 60
        private const val AUTH_NUMBER_REQUEST_COUNTDOWN_TIME = MINUTE * 3                               // 인증번호 입력 제한시간
        private const val COUNT_DOWN_DEFAULT_TICK = SECONDS
        private const val AUTH_CODE_MIN_LENGTH = 6
    }

    private var callMode: Int = MemberEmailFragment.BUNDLE_EXTRA_VALUE_FIND_ID                      // init Defaults
    private var countDownTimer: CountDownTimer? = null
    private var email = ""
    private lateinit var mKeyboardHeightProvider: KeyboardHeightProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callMode = arguments?.getInt(BUNDLE_EXTRA_KEY_CALL_MODE, BUNDLE_EXTRA_VALUE_FIND_ID)
                ?: BUNDLE_EXTRA_VALUE_FIND_ID
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        if(callMode == BUNDLE_EXTRA_VALUE_FIND_PW) {
            editTextAccount.visibility = View.VISIBLE
            textViewAnnotation.text = getString(R.string.msg_find_account_passwd_fail_annotation, AppPreferences.getCustomerCenterPhone(activity!!))
            layoutAuthGuide.visibility = View.VISIBLE
            layoutAuthConfirm.visibility = View.VISIBLE
            buttonSend.visibility = View.VISIBLE
        } else {
            textViewAnnotation.text = getString(R.string.msg_find_account_id_fail_annotation, AppPreferences.getCustomerCenterPhone(activity!!))
            editTextAccount.visibility = View.GONE
            layoutAuthGuide.visibility = View.GONE
            layoutAuthConfirm.visibility = View.GONE
            buttonSend.visibility = View.GONE
        }

        editTextEmail.addTextChangedListener(authRequestChangeListener)
        editTextAuthNumber.addTextChangedListener(authInputChangeListener)

        buttonSend.setOnClickListener(clickListener)
        buttonConfirm.setOnClickListener(clickListener)

        emptyTouch.setOnTouchListener(object:View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if(event?.action == MotionEvent.ACTION_DOWN) {
                    if(editTextEmail.isFocused) {
                        SoftKeyboardUtils.hideKeyboard(editTextEmail)
                    }

                    if(editTextAuthNumber.isFocused) {
                        SoftKeyboardUtils.hideKeyboard(editTextAuthNumber)
                    }

                    if(editTextAccount.isFocused) {
                        SoftKeyboardUtils.hideKeyboard(editTextAccount)
                    }
                }

                return false
            }
        })

        mKeyboardHeightProvider = KeyboardHeightProvider(activity!!)
        Handler().postDelayed(Runnable { mKeyboardHeightProvider.start() }, 1000)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider.close()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
        if ( bus.height > 100 ) {
            emptyTouch.visibility = View.VISIBLE
        } else {
            emptyTouch.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:NetworkBusResponse) {
        when(data.arg1) {
            NetworkApi.API120.toString() -> {
                if("ok" == data.arg2) {
                    editTextAuthNumber.isEnabled = true
                } else {
                    val response = JSONObject(data.arg4)
                    AppToast(context!!).showToastMessage(response["message"].toString(),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            }

            NetworkApi.API152.toString() -> {
                if("ok" == data.arg2) {
                    val response = JSONObject(DBManager.getInstance(context!!).get(data.arg1))
                    Logger.e("response : ${response["mb_id"]}")
                    (activity as? FindAccountActivity)?.showFindIdResult(response["mb_id"].toString())
                } else {
                    (activity as? FindAccountActivity)?.showFindIdResult("")
                }
            }

            NetworkApi.API134.toString() -> {
                if("ok" == data.arg2) {
                    editTextAuthNumber.isEnabled = true
                    AppToast(context!!).showToastMessage("해당 이메일 주소로 인증번호를 전송하였습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                } else {
                    val response = JSONObject(data.arg4)
                    AppToast(context!!).showToastMessage(response["message"].toString(),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            }

            NetworkApi.API135.toString() -> {
                if("ok" == data.arg2) {
                    (activity as? FindAccountActivity)?.showPasswordChange(editTextAccount.text.toString(), editTextAuthNumber.text.toString())
                } else {
                    val response = JSONObject(data.arg4)
                    AppToast(context!!).showToastMessage(response["message"].toString(),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            }
        }
    }

    /**
     * 인증메일을 요청
     * @param emailAddress
     */
    private fun requestAuthNumber(emailAddress: String) {
        email = emailAddress

        if(callMode == BUNDLE_EXTRA_VALUE_FIND_ID) {
            JSONObject().apply {
                put("cert_name", "")
                put("cert_val", emailAddress)
                put("cert_type", "email")
            }.let {
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
                NetworkBus(NetworkApi.API120.name, body).let { EventBus.getDefault().post(it) }
            }
        } else {
            JSONObject().apply {
                put("mb_id", editTextAccount.text.toString())
                put("cert_name", "")
                put("cert_val", emailAddress)
                put("cert_type", "email")
            }.let {
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
                NetworkBus(NetworkApi.API134.name, body).let { EventBus.getDefault().post(it) }
            }
        }
    }

    private val authRequestChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(s?.length == 0) {
                buttonConfirm.isEnabled = false
                buttonSend.isEnabled = false
                editTextEmail.setBackgroundResource(R.drawable.input_form_focused)
                layoutEmailError.visibility = View.GONE
            } else {
                buttonConfirm.isEnabled = true
                buttonSend.isEnabled = true
            }
        }
    }

    private val authInputChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            buttonConfirm.isEnabled = (editTextAuthNumber.text?.toString()?.length
                    ?: 0) >= AUTH_CODE_MIN_LENGTH
        }
    }

    private val clickListener = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            when (v?.id) {
                R.id.buttonSend -> {
                    var emailAddress = editTextEmail.text?.toString()
                    if (!TextUtils.isEmpty(emailAddress)) {
                        requestAuthNumber(emailAddress!!)
                    }
                }

                R.id.buttonConfirm -> {
                    if (callMode == BUNDLE_EXTRA_VALUE_FIND_ID) {
                        if (AccountPolicy.IsEMailMatched(editTextEmail.text.toString())) {
                            JSONObject().apply {
                                put("cert_val", editTextEmail.text?.toString())
                            }.let {
                                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
                                NetworkBus(NetworkApi.API152.name, body).let { EventBus.getDefault().post(it) }
                            }
                        } else {
                            editTextEmail.setBackgroundResource(R.drawable.input_form_error)
                            layoutEmailError.visibility = View.VISIBLE
                            buttonConfirm.isEnabled = false
                        }
                    } else {
                        JSONObject().apply {
                            put("mb_id", editTextAccount.text.toString())
                            put("cert_name", "")
                            put("cert_val", editTextEmail.text?.toString())
                            put("cert_type", "email")
                            put("cert_number", editTextAuthNumber.text.toString())
                        }.let {
                            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
                            NetworkBus(NetworkApi.API135.name, body).let { EventBus.getDefault().post(it) }
                        }
                    }
                }
            }
        }
    }
}