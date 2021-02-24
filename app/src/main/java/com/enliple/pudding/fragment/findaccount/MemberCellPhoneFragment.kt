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
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.keyboard.KeyboardHeightProvider
import kotlinx.android.synthetic.main.fragment_find_cellphone.*
import kotlinx.android.synthetic.main.fragment_find_cellphone.buttonConfirm
import kotlinx.android.synthetic.main.fragment_find_cellphone.buttonSend
import kotlinx.android.synthetic.main.fragment_find_cellphone.editTextAccount
import kotlinx.android.synthetic.main.fragment_find_cellphone.editTextAuthNumber
import kotlinx.android.synthetic.main.fragment_find_cellphone.editTextName
import kotlinx.android.synthetic.main.fragment_find_cellphone.emptyTouch
import kotlinx.android.synthetic.main.fragment_find_cellphone.textViewCountDown
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 휴대폰번호 찾기 Fragment
 * @author hkcha
 * @since 2018.08.16
 */
class MemberCellPhoneFragment : AbsBaseFragment() {
    companion object {
        private const val TAG = "MemberCellPhoneFragment"

        const val BUNDLE_EXTRA_KEY_CALL_MODE = "call_mode"
        const val BUNDLE_EXTRA_VALUE_FIND_ID = 1
        const val BUNDLE_EXTRA_VALUE_FIND_PW = 2

        private const val SECONDS = 1000L
        private const val MINUTE = SECONDS * 60
        private const val AUTH_NUMBER_REQUEST_COUNTDOWN_TIME = MINUTE * 3           // 인증번호 입력 제한시간
        private const val COUNT_DOWN_DEFAULT_TICK = SECONDS
        private const val AUTH_CODE_MIN_LENGTH = 6
    }

    private var callMode: Int = BUNDLE_EXTRA_VALUE_FIND_ID                           // init Defaults
    private var countDownTimer: CountDownTimer? = null
    private var phoneNuber = ""
    private lateinit var mKeyboardHeightProvider: KeyboardHeightProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callMode = arguments?.getInt(BUNDLE_EXTRA_KEY_CALL_MODE, BUNDLE_EXTRA_VALUE_FIND_ID)
                ?: BUNDLE_EXTRA_VALUE_FIND_ID
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_cellphone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        editTextAccount.visibility = if (callMode == BUNDLE_EXTRA_VALUE_FIND_PW) {
            View.VISIBLE
        } else View.GONE

        editTextAccount.clearFocus()
        textViewAnnotation.text = getString(R.string.msg_find_account_id_fail_annotation, AppPreferences.getCustomerCenterPhone(activity!!))
        editTextAccount.addTextChangedListener(authRequestChangeListener)
        editTextCellPhone.addTextChangedListener(authRequestChangeListener)
        editTextAuthNumber.addTextChangedListener(authInputChangeListener)

        buttonSend.setOnClickListener(clickListener)
        buttonConfirm.setOnClickListener(clickListener)

        emptyTouch.setOnTouchListener(object:View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if(event?.action == MotionEvent.ACTION_DOWN) {
                    if(editTextAccount.isFocused) {
                        SoftKeyboardUtils.hideKeyboard(editTextAccount)
                    }

                    if(editTextCellPhone.isFocused) {
                        SoftKeyboardUtils.hideKeyboard(editTextCellPhone)
                    }

                    if(editTextAuthNumber.isFocused) {
                        SoftKeyboardUtils.hideKeyboard(editTextAuthNumber)
                    }

                    if(editTextName.isFocused) {
                        SoftKeyboardUtils.hideKeyboard(editTextName)
                    }
                }

                return false
            }
        })

        mKeyboardHeightProvider = KeyboardHeightProvider(activity!!)
        Handler().postDelayed(Runnable { mKeyboardHeightProvider.start() }, 1000)
    }

    override fun onDetach() {
        cancelCountDown()
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

        if(mKeyboardHeightProvider != null) {
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

            NetworkApi.API121.toString() -> {
                if("ok" == data.arg2) {
                    val response = JSONObject(DBManager.getInstance(context!!).get(data.arg1))
                    Logger.e("response : ${response["mb_id"]}")
                    (activity as? FindAccountActivity)?.showFindIdResult(response["mb_id"].toString())
                } else {
                    val response = JSONObject(data.arg4)
                    AppToast(context!!).showToastMessage(response["message"].toString(),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            }

            NetworkApi.API134.toString() -> {
                if("ok" == data.arg2) {
                    editTextAuthNumber.isEnabled = true
                } else {
                    val response = JSONObject(data.arg4)
                    AppToast(context!!).showToastMessage(response["message"].toString(),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            }

            NetworkApi.API135.toString() -> {
                if ("ok" == data.arg2) {
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
     * 인증번호를 요청
     * @param cellPhoneNumber
     */
    private fun requestAuthNumber(cellPhoneNumber: String) {
        startAuthNumberCountDown()

        phoneNuber = cellPhoneNumber

        if(callMode == BUNDLE_EXTRA_VALUE_FIND_ID) {
            val obj = JSONObject().apply {
                put("cert_name", editTextName.text.toString())
                put("cert_val", cellPhoneNumber)
                put("cert_type", "hp")
            }

            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
            NetworkBus(NetworkApi.API120.name, body).let {
                EventBus.getDefault().post(it)
            }
        } else {
            val obj = JSONObject().apply {
                put("mb_id", editTextAccount.text.toString())
                put("cert_name", editTextName.text.toString())
                put("cert_val", cellPhoneNumber)
                put("cert_type", "hp")
            }

            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
            NetworkBus(NetworkApi.API134.name, body).let {
                EventBus.getDefault().post(it)
            }
        }
    }

    /**
     * 카운트 다운을 취소
     */
    private fun cancelCountDown() {
        countDownTimer?.cancel()
        countDownTimer = null

        textViewCountDown?.text = ""
    }

    /**
     * 인증번호를 요청하여 서버에서 응답이후 CountDown 을 수행하고 CountDown 이 종료되면 UI 상태를 변경
     */
    private fun startAuthNumberCountDown() {
        cancelCountDown()

        var countDownMinute = AUTH_NUMBER_REQUEST_COUNTDOWN_TIME / MINUTE
        var countDownSeconds = if (AUTH_NUMBER_REQUEST_COUNTDOWN_TIME - countDownMinute == 0L) {
            0
        } else {
            (AUTH_NUMBER_REQUEST_COUNTDOWN_TIME - countDownMinute) / SECONDS
        }
        textViewCountDown.text = "${String.format("%02d", countDownMinute)}:${String.format("%02d", countDownSeconds)}"

        countDownTimer = object : CountDownTimer(AUTH_NUMBER_REQUEST_COUNTDOWN_TIME, COUNT_DOWN_DEFAULT_TICK) {

            var timeRemain = 0L

            override fun onFinish() {
                Logger.d(TAG, "Time's up!")
                buttonSend.setText(R.string.msg_find_account_retry_auth_number)
                editTextAuthNumber.setText("")
                editTextAuthNumber.isEnabled = false
                textViewCountDown.text = ""
            }

            override fun onTick(millisUntilFinished: Long) {
                var seconds = (millisUntilFinished / SECONDS)
                var minute = seconds / 60

                seconds -= (minute * 60)

                var remain = "${String.format("%02d", minute)}:${String.format("%02d", seconds)}"
                textViewCountDown.text = remain
                timeRemain = millisUntilFinished
                Logger.d(TAG, "Time Remain is $remain")
            }
        }.start()
    }

    private val authRequestChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            var accountIsNotNull = if (callMode == BUNDLE_EXTRA_VALUE_FIND_PW) {
                !TextUtils.isEmpty(editTextAccount.text?.toString())
            } else {
                true
            }
            val cellPhoneNotNotNull = editTextCellPhone.text!!.toString().isNotEmpty()
            val nameIsNotMNull = editTextName.text!!.toString().isNotEmpty()

            buttonSend.isEnabled = accountIsNotNull and cellPhoneNotNotNull and nameIsNotMNull
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

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonSend -> {
                var cellPhoneNumber = editTextCellPhone.text?.toString() ?: ""
                if (!TextUtils.isEmpty(cellPhoneNumber)) {
                    requestAuthNumber(cellPhoneNumber)
                }
            }

            R.id.buttonConfirm -> {
                cancelCountDown()

                if(callMode == BUNDLE_EXTRA_VALUE_FIND_ID) {
                    val obj = JSONObject().apply {
                        put("cert_name", editTextName.text.toString())
                        put("cert_val", phoneNuber)
                        put("cert_type", "hp")
                        put("cert_number", editTextAuthNumber.text.toString())
                    }

                    val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
                    NetworkBus(NetworkApi.API121.name, body).let {
                        EventBus.getDefault().post(it)
                    }
                } else {
                    val obj = JSONObject().apply {
                        put("mb_id", editTextAccount.text.toString())
                        put("cert_name", editTextName.text.toString())
                        put("cert_val", phoneNuber)
                        put("cert_type", "hp")
                        put("cert_number", editTextAuthNumber.text.toString())
                    }

                    val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
                    NetworkBus(NetworkApi.API135.name, body).let {
                        EventBus.getDefault().post(it)
                    }
                }
            }
        }
    }
}