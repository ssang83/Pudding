package com.enliple.pudding.fragment.findaccount

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.FindAccountActivity
import com.enliple.pudding.api.AccountPolicy
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.data.ApiParams
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.ui_compat.AsteriskPasswordTransformationMethod
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.keyboard.KeyboardHeightProvider
import kotlinx.android.synthetic.main.fragment_pw_setting.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2019-04-26.
 */
class PasswdSettingFragment : Fragment() {

    companion object {
        private const val DRAWABLE_RES_INPUT_NORMAL = R.drawable.input_form_normal
        private const val DRAWABLE_RES_INPUT_ERROR = R.drawable.input_form_error
    }

    private var mCertNumber = ""
    private var userId = ""
    private lateinit var mKeyboardHeightProvider: KeyboardHeightProvider

    private val clickListener = View.OnClickListener {
        when(it?.id) {
            R.id.buttonConfirm -> {
                handleNext()
            }
        }
    }

    private val inputChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if(s!!.length > 0) {
                if ( !AccountPolicy.IsPasswordMatched(s.toString()) ) {
                    textViewPasswordError.visibility = View.VISIBLE
                    editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
                } else {
                    textViewPasswordError.visibility = View.GONE
                    editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
                }
            } else {
                textViewPasswordError.visibility = View.GONE
                editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
            }

            buttonConfirm.isEnabled = isPasswdInputValidation()
        }
    }

    private val pwConfirmChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            val pw = editTextPassword.text.toString()
            if(s!!.length > 0) {
                if(pw != s.toString()) {
                    textViewPasswordConfirmError.visibility = View.VISIBLE
                    editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
                } else {
                    textViewPasswordConfirmError.visibility = View.GONE
                    editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
                }
            } else {
                textViewPasswordConfirmError.visibility = View.GONE
                editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
            }

            buttonConfirm.isEnabled = isPasswdInputValidation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mCertNumber = arguments?.getString("certNum")?:""
        userId = arguments?.getString("userId")?:""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_pw_setting, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        noti_pw.text = getString(R.string.msg_find_account_passwd_fail_annotation, AppPreferences.getCustomerCenterPhone(activity!!))
        emptyTouch.setOnTouchListener(object:View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if(event?.action == MotionEvent.ACTION_DOWN) {
                    if(editTextPassword.isFocused) {
                        SoftKeyboardUtils.hideKeyboard(editTextPassword)
                    }

                    if(editTextPasswordConfirm.isFocused) {
                        SoftKeyboardUtils.hideKeyboard(editTextPasswordConfirm)
                    }
                }

                return false
            }
        })

        editTextPassword.addTextChangedListener(inputChangeListener)
        editTextPasswordConfirm.addTextChangedListener(pwConfirmChangeListener)

        editTextPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        editTextPasswordConfirm.transformationMethod = AsteriskPasswordTransformationMethod()

        buttonConfirm.setOnClickListener(clickListener)

        mKeyboardHeightProvider = KeyboardHeightProvider(activity!!)
        Handler().postDelayed(Runnable { mKeyboardHeightProvider.start() }, 1000)
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

    @Subscribe
    fun onMessageEvent(data:NetworkBusResponse) {
        if(data.arg1.startsWith("PUT/password/")){
            if(data.arg2 == "ok") {
                (activity as FindAccountActivity).showPasswordChangeComplete()
            } else {
                AppToast(context!!).showToastMessage("비밀번호를 형식에 맞게 입력해주세요.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        }
    }

    private fun handleNext() {
        var requestObj = ApiParams.PasswdParams(
                "reset",
                mCertNumber,
                "",
                editTextPassword.text.toString(),
                editTextPasswordConfirm.text.toString() )

        EventBus.getDefault().post(NetworkBus(NetworkApi.API14.name, userId, requestObj))

//        JSONObject().apply {
//            put("strType", "reset")
//            put("strCertNum", mCertNumber)
//            put("strPwOld", "")
//            put("strPwNew", editTextPassword.text.toString())
//            put("strPwChk", editTextPasswordConfirm.text.toString())
//            Logger.e("mCertNumber :: " + mCertNumber)
//            Logger.e("strPwNew :: " + editTextPassword.text.toString())
//            Logger.e("strPwChk :: " + editTextPasswordConfirm.text.toString())
//        }.let {
//            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
//            EventBus.getDefault().post(NetworkBus(NetworkApi.API14.name, body))
//        }
    }

    private fun isPasswdInputValidation() : Boolean {
        var result = false
        var password1 = !TextUtils.isEmpty(editTextPassword.text?.toString())
        var password2 = !TextUtils.isEmpty(editTextPasswordConfirm.text?.toString())

        result = password1 and password2
        if(result) {
            buttonConfirm.setBackgroundColor(Color.parseColor("#9f56f2"))
        } else {
            buttonConfirm.setBackgroundColor(Color.parseColor("#d9e1eb"))
        }

        return result
    }

    private fun passwdValidation() {
        val pw = editTextPassword.text.toString()
        val pwConfirm = editTextPasswordConfirm.text.toString()
        // 비밀번호 검증
//        if(pw.length < AccountPolicy.PASSWORD_MINIMUM_LENGTH
//                || pw.length > AccountPolicy.PASSWORD_MAXIMUM_LENGTH
//                || !AccountPolicy.numberPattern.matcher(pw).matches()
//                || !AccountPolicy.specialPattern.matcher(pw).matches()
//                || !(AccountPolicy.upperCasePattern.matcher(pw).matches()
//                        || AccountPolicy.lowerCasePattern.matcher(pw).matches())
//                || !AccountPolicy.anotherCharCheckPattern.matcher(pw).matches()) {
        if ( !AccountPolicy.IsPasswordMatched(pw) ) {
            textViewPasswordError.setText(R.string.msg_signup_error_bad_type_password)
            textViewPasswordError.visibility = View.VISIBLE
            editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
            editTextPassword.requestFocus()
        }

        // 비밀번호 - 비밀번호 확인 비교
        else if (pw != pwConfirm) {
            textViewPasswordConfirmError.setText(R.string.msg_signup_error_mismatch_password)
            textViewPasswordConfirmError.visibility = View.VISIBLE
            editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
            editTextPasswordConfirm.requestFocus()
        } else {
            handleNext()
        }
    }
}