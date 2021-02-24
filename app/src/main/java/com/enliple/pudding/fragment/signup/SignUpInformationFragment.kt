package com.enliple.pudding.fragment.signup

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.AgreementActivity
import com.enliple.pudding.activity.SignUpActivity
import com.enliple.pudding.api.AccountPolicy
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.ui_compat.AsteriskPasswordTransformationMethod
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_signup_information.*
import kotlinx.android.synthetic.main.layout_signup_form.*
import kotlinx.android.synthetic.main.layout_signup_level_status.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 회원 가입 정보입력 Fragment
 * @author hkcha
 * @since 2018.08.14
 */
class SignUpInformationFragment : AbsBaseFragment() {
    companion object {
        private const val TAG = "SignUpInformationFragment"

        const val BUNDLE_EXTRA_KEY_CERTIFICATION_TYPE = "cert_type"
        const val BUNDLE_EXTRA_VALUE_CERTIFICATION_TYPE_NONE = -1
        const val BUNDLE_EXTRA_VALUE_CERTIFICATION_TYPE_CELLPHONE = 1
        const val BUNDLE_EXTRA_VALUE_CERTIFICATION_TYPE_IPIN = 2

        const val BUNDLE_EXTRA_KEY_CERTIFICATION_NAME = "cert_name"
        const val BUNDLE_EXTRA_KEY_CERTIFICATION_CELLPHONE = "cert_cellphone"
        const val BUNDLE_EXTRA_KEY_CERTIFICATION_NUMBER = "cert_number"
        const val BUNDLE_EXTRA_KEY_CERTIFICATION_HASH_DATA = "hash_data"
        const val BUNDLE_EXTRA_KEY_CERTIFICATION_ADULT = "adult"
        const val BUNDLE_EXTRA_KEY_CERTIFICATION_BIRTH = "birth"
        const val BUNDLE_EXTRA_KEY_CERTIFICATION_SEX = "sex"
        const val BUNDLE_EXTRA_KEY_CERTIFICATION_DUP_INFO = "dup_info"

        private const val DRAWABLE_RES_INPUT_NORMAL = R.drawable.input_form_normal
        private const val DRAWABLE_RES_INPUT_ERROR = R.drawable.input_form_error
    }

    private var alertDialog: Dialog? = null
    private var errorFocusView: View? = null

    private var certType: Int = -1                                       // -1 is not certificated
    private var certName: String? = null
    private var nAdult: String? = null

    private var certCellPhone: String? = null
    private var certAuthNumber: String? = null
    private var strBirth: String? = null
    private var strSex: String? = null
    private var strCheckHash: String? = null
    private var dupInfo: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)

        certType = arguments?.getInt(BUNDLE_EXTRA_KEY_CERTIFICATION_TYPE, BUNDLE_EXTRA_VALUE_CERTIFICATION_TYPE_NONE)
                ?: BUNDLE_EXTRA_VALUE_CERTIFICATION_TYPE_NONE
        certName = arguments?.getString(BUNDLE_EXTRA_KEY_CERTIFICATION_NAME)
        certCellPhone = arguments?.getString(BUNDLE_EXTRA_KEY_CERTIFICATION_CELLPHONE)
        certAuthNumber = arguments?.getString(BUNDLE_EXTRA_KEY_CERTIFICATION_NUMBER)
        strCheckHash = arguments?.getString(BUNDLE_EXTRA_KEY_CERTIFICATION_HASH_DATA)
        nAdult = arguments?.getString(BUNDLE_EXTRA_KEY_CERTIFICATION_ADULT)
        strBirth = arguments?.getString(BUNDLE_EXTRA_KEY_CERTIFICATION_BIRTH)
        strSex = arguments?.getString(BUNDLE_EXTRA_KEY_CERTIFICATION_SEX)
        dupInfo = arguments?.getString(BUNDLE_EXTRA_KEY_CERTIFICATION_DUP_INFO)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageViewStatus2.isSelected = true

        editTextPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        editTextPasswordConfirm.transformationMethod = AsteriskPasswordTransformationMethod()

        buttonSubmit.setOnClickListener(clickListener)
        checkBoxAll.setOnCheckedChangeListener(checkedChangeListener)
        toggleOnCheckedChangedListener(true)

        layoutTermOfUseDetail.setOnClickListener(clickListener)
        layoutPrivacyUseDetail.setOnClickListener(clickListener)
        layoutPrivacyConsignmentDetail.setOnClickListener(clickListener)

//        editTextAccount.onFocusChangeListener = accountFocusChangeListener
//        editTextPassword.onFocusChangeListener = pwFocusChangeListener
//        editTextPasswordConfirm.onFocusChangeListener = pwConfirmFocusChangeListener
//        editTextCellPhone.onFocusChangeListener = phoneFocusChangeListener
//        editTextEmail.onFocusChangeListener = mailFocusChangeListener

        when (certType) {
            BUNDLE_EXTRA_VALUE_CERTIFICATION_TYPE_CELLPHONE -> {
                if (!TextUtils.isEmpty(certCellPhone)) {
                    editTextCellPhone.setText(certCellPhone!!.replace("-", ""))
                    editTextCellPhone.isEnabled = false
                }
            }

            BUNDLE_EXTRA_VALUE_CERTIFICATION_TYPE_IPIN -> {

            }

            else -> {
                AlertDialog.Builder(view.context)
                        .setMessage("잘못된 호출입니다.")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
//                            (activity as? SignUpActivity)?.replaceIdentificationFragment()
                        }
                        .show()
            }
        }

        editTextAccount.addTextChangedListener(accountChangeListener)
        editTextPassword.addTextChangedListener(pwChangeListener)
        editTextPasswordConfirm.addTextChangedListener(pwConfirmListener)
        editTextCellPhone.addTextChangedListener(inputChangeListener)
        editTextEmail.addTextChangedListener(mailConfirmListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API76 = "${NetworkHandler.getInstance(context!!).getKey(NetworkApi.API76.toString(), "", "")}${editTextAccount.text.toString()}"
        Logger.e("onMessageEvent API76 :: $API76")
        Logger.e("onMessageEvent data.arg1 :: ${data.arg1}")
        when (data.arg1) {
            API76 -> handleNetworkResult(data)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        Logger.e("handleNetworkResult data.arg2 :: ${data.arg2}")
        if ("ok" == data.arg2) {
            AppPreferences.setUserId(view!!.context, editTextAccount.text.toString())
            AppPreferences.setUserPw(view!!.context, editTextPassword.text.toString())
            AppPreferences.setUserLoginType(view!!.context, AppPreferences.LOGIN_TYPE_AUTO)
            AppPreferences.setUserAccountType(view!!.context, AppPreferences.USER_ACCOUNT_TYPE_ACCOUNT)

            progressLoading.visibility = View.INVISIBLE
            (activity as? SignUpActivity)?.replaceCompleteFragment()
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("$errorResult")

            progressLoading.visibility = View.INVISIBLE

            alertDialog?.dismiss()
            alertDialog = AlertDialog.Builder(view!!.context)
                    .setMessage(errorResult.message)
                    .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .create().apply { show() }
        }
    }

    /**
     * 회원가입 정보가 모두 입력된 상태인지 확인
     * @return
     */
    private fun isAllInputInformation(): Boolean {
        var result: Boolean
        var accountInput = !TextUtils.isEmpty(editTextAccount.text?.toString())
        var password1 = !TextUtils.isEmpty(editTextPassword.text?.toString())
        var password2 = !TextUtils.isEmpty(editTextPasswordConfirm.text?.toString())
        var cellphone = !TextUtils.isEmpty(editTextCellPhone.text?.toString())
        var email = !TextUtils.isEmpty(editTextEmail.text?.toString())
        var isCheckedAgreement = checkBoxTerms.isChecked and checkBoxPrivacyUse.isChecked and checkBoxPrivacyConsignment.isChecked
        Logger.e("accountInput :: $accountInput, password1 :: $password1, password2 :: $password2, cellphone :: $cellphone, email :: $email, isCheckedAgreement :: $isCheckedAgreement")
        result = accountInput and password1 and password2 and cellphone and email and isCheckedAgreement
        if (result) {
            buttonSubmit.setBackgroundColor(Color.parseColor("#9f56f2"))
        } else {
            buttonSubmit.setBackgroundColor(Color.parseColor("#d9e1eb"))
        }
        return result
    }

    /**
     * 회원가입 처리 핸들링 및 입력 양식 검사, 요청
     */
    private fun handleSubmit() {
        Logger.e("handleSubmit")
        resetErrorText()

        var inputAccount = editTextAccount.text.toString()
        var pw = editTextPassword.text.toString()
        var pwConfirm = editTextPasswordConfirm.text.toString()
        var cellphone = editTextCellPhone.text.toString()
        var email = editTextEmail.text.toString()

        // 아이디 형식검증
//        if (inputAccount!!.length < AccountPolicy.ACCOUNT_MINIMUM_LENGTH
//                || inputAccount!!.length > AccountPolicy.ACCOUNT_MAXIMUM_LENGTH
//                || AccountPolicy.accountSpecialPattern.matcher(inputAccount).matches()
//                || !AccountPolicy.numberPattern.matcher(inputAccount).matches()
//                || !(AccountPolicy.lowerCasePattern.matcher(inputAccount).matches()
//                        || AccountPolicy.upperCasePattern.matcher(inputAccount).matches())) {
        if ( !AccountPolicy.IsIdMatched(inputAccount) ) {
            Logger.e("id not matched")
            textViewAccountError.setText(R.string.msg_signup_error_bad_type_account)
            textViewAccountError.visibility = View.VISIBLE
            editTextAccount.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
            editTextAccount.requestFocus()
            errorFocusView = editTextAccount
        }
        // 비밀번호 검증
//        else if (pw!!.length < AccountPolicy.PASSWORD_MINIMUM_LENGTH
//                || pw!!.length > AccountPolicy.PASSWORD_MAXIMUM_LENGTH
//                || !AccountPolicy.numberPattern.matcher(pw).matches()
//                || !AccountPolicy.specialPattern.matcher(pw).matches()
//                || !(AccountPolicy.upperCasePattern.matcher(pw).matches()
//                        || AccountPolicy.lowerCasePattern.matcher(pw).matches())
//                || !AccountPolicy.anotherCharCheckPattern.matcher(pw).matches()) {
        else if ( !AccountPolicy.IsPasswordMatched(pw) ) {
            Logger.e("pw not matched")
            textViewPasswordError.setText(R.string.msg_signup_error_bad_type_password)
            textViewPasswordError.visibility = View.VISIBLE
            editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
            editTextPassword.requestFocus()
            errorFocusView = editTextPassword
        }

        // 비밀번호 - 비밀번호 확인 비교
        else if (pw != pwConfirm) {
            Logger.e("pw not same")
            textViewPasswordConfirmError.setText(R.string.msg_signup_error_mismatch_password)
            textViewPasswordConfirmError.visibility = View.VISIBLE
            editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
            editTextPasswordConfirm.requestFocus()
            errorFocusView = editTextPasswordConfirm
        }

        // 휴대폰번호 확인
        else if (!AccountPolicy.IsPhoneMatched(cellphone) ) {
            Logger.e("phone no not matched")
            textViewCellPhoneError.setText(R.string.msg_signup_error_bad_type_cellphone)
            textViewCellPhoneError.visibility = View.VISIBLE
            editTextCellPhone.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
            editTextCellPhone.requestFocus()
            errorFocusView = editTextCellPhone
        }

        // 이메일 확인
//        else if (!AccountPolicy.emailPattern.matcher(email).matches()) {
    else if ( !AccountPolicy.IsEMailMatched(email) ) {
            Logger.e("email no not matched")
            textViewEmailError.setText(R.string.msg_signup_error_bad_type_email)
            textViewEmailError.visibility = View.VISIBLE
            editTextEmail.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
            editTextEmail.requestFocus()
            errorFocusView = editTextEmail
        }

        // 약관동의 상태 확인
        else if (!(checkBoxTerms.isChecked && checkBoxPrivacyUse.isChecked && checkBoxPrivacyConsignment.isChecked)) {
            Logger.e("agree not checked")
            alertDialog?.dismiss()
            alertDialog = null
            alertDialog = AlertDialog.Builder(view!!.context)
                    .setMessage(R.string.msg_signup_error_not_agree_mandatory)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create().apply { show() }
        } else {
            requestSignUp()
        }
    }

    /**
     * 양식내 입력된 데이터를 이용한 회원가입 요청
     */
    private fun requestSignUp() {
        progressLoading.visibility = View.VISIBLE

        var requestObj = JSONObject()
        requestObj.put("userId", editTextAccount.text.toString())
        requestObj.put("passw", editTextPassword.text.toString())
        requestObj.put("userEmail", editTextEmail.text.toString())
        requestObj.put("userHp", certCellPhone)
        requestObj.put("certify", if (certType == BUNDLE_EXTRA_VALUE_CERTIFICATION_TYPE_CELLPHONE) "hp" else "")
        requestObj.put("userName", certName)
        requestObj.put("nAdult", nAdult)
        requestObj.put("strBirth", strBirth)
        requestObj.put("strSex", strSex)
        requestObj.put("strCheckHash", strCheckHash)
        requestObj.put("strAuthKey", dupInfo)

        var body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString())
        val bus = NetworkBus(NetworkApi.API76.name, editTextAccount.text.toString(), body)
        EventBus.getDefault().post(bus)
    }

    /**
     * CheckedChangedListener 가 오동작하지 않기 위해 필요에 맞춰 토글링
     * @param enabled
     */
    private fun toggleOnCheckedChangedListener(enabled: Boolean) {
        if (enabled) {
            checkBoxTerms.setOnCheckedChangeListener(checkedChangeListener)
            checkBoxPrivacyUse.setOnCheckedChangeListener(checkedChangeListener)
            checkBoxPrivacyConsignment.setOnCheckedChangeListener(checkedChangeListener)
        } else {
            checkBoxTerms.setOnCheckedChangeListener(null)
            checkBoxPrivacyUse.setOnCheckedChangeListener(null)
            checkBoxPrivacyConsignment.setOnCheckedChangeListener(null)
        }
    }

    /**
     * 현재 표시되고 있는 Error Text 문구를 모두 초기화
     */
    private fun resetErrorText() {
        textViewAccountError.text = ""
        textViewEmailError.text = ""
        textViewCellPhoneError.text = ""
        textViewPasswordError.text = ""
        textViewPasswordConfirmError.text = ""

        textViewAccountError.visibility = View.INVISIBLE
        textViewEmailError.visibility = View.INVISIBLE
        textViewCellPhoneError.visibility = View.INVISIBLE
        textViewPasswordError.visibility = View.INVISIBLE
        textViewPasswordConfirmError.visibility = View.INVISIBLE

        editTextAccount.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
        editTextEmail.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
        editTextCellPhone.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
        editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
        editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)

        errorFocusView?.clearFocus()
        errorFocusView = null
    }

    private val inputChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            buttonSubmit.isEnabled = isAllInputInformation()
        }
    }

    private val accountChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            buttonSubmit.isEnabled = isAllInputInformation()
            var inputAccount = editTextAccount.text.toString()
//            if (inputAccount!!.length < AccountPolicy.ACCOUNT_MINIMUM_LENGTH
//                    || inputAccount!!.length > AccountPolicy.ACCOUNT_MAXIMUM_LENGTH
//                    || AccountPolicy.accountSpecialPattern.matcher(inputAccount).matches()
//                    || !AccountPolicy.numberPattern.matcher(inputAccount).matches()
//                    || !(AccountPolicy.lowerCasePattern.matcher(inputAccount).matches()
//                            || AccountPolicy.upperCasePattern.matcher(inputAccount).matches())) {
            if ( !AccountPolicy.IsIdMatched(inputAccount) ) {
                Logger.e("id not matched")
                textViewAccountError.setText(R.string.msg_signup_error_bad_type_account)
                textViewAccountError.visibility = View.VISIBLE
                editTextAccount.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
            } else {
                textViewAccountError.visibility = View.INVISIBLE
                editTextAccount.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
            }
        }
    }

    private val pwChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            buttonSubmit.isEnabled = isAllInputInformation()
            var pw = editTextPassword.text.toString()
//            if (pw!!.length < AccountPolicy.PASSWORD_MINIMUM_LENGTH
//                    || pw!!.length > AccountPolicy.PASSWORD_MAXIMUM_LENGTH
//                    || !AccountPolicy.numberPattern.matcher(pw).matches()
//                    || !AccountPolicy.specialPattern.matcher(pw).matches()
//                    || !(AccountPolicy.upperCasePattern.matcher(pw).matches()
//                            || AccountPolicy.lowerCasePattern.matcher(pw).matches())
//                    || !AccountPolicy.anotherCharCheckPattern.matcher(pw).matches()) {
            if ( !AccountPolicy.IsPasswordMatched(pw) ) {
                Logger.e("pw not matched")
                textViewPasswordError.setText(R.string.msg_signup_error_bad_type_password)
                textViewPasswordError.visibility = View.VISIBLE
                editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
            } else {
                textViewPasswordError.visibility = View.INVISIBLE
                editTextPassword.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
            }
        }
    }

    private val pwConfirmListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            buttonSubmit.isEnabled = isAllInputInformation()
            var pw = editTextPassword.text.toString()
            var pwConfirm = editTextPasswordConfirm.text.toString()
            if (pw != pwConfirm) {
                Logger.e("pw not same")
                textViewPasswordConfirmError.setText(R.string.msg_signup_error_mismatch_password)
                textViewPasswordConfirmError.visibility = View.VISIBLE
                editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
            } else {
                textViewPasswordConfirmError.visibility = View.INVISIBLE
                editTextPasswordConfirm.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
            }
        }
    }

    private val mailConfirmListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            buttonSubmit.isEnabled = isAllInputInformation()
            var email = editTextEmail.text.toString()
//            if (!AccountPolicy.emailPattern.matcher(email).matches()) {
            if ( !AccountPolicy.IsEMailMatched(email) ) {
                Logger.e("email no not matched")
                textViewEmailError.setText(R.string.msg_signup_error_bad_type_email)
                textViewEmailError.visibility = View.VISIBLE
                editTextEmail.setBackgroundResource(DRAWABLE_RES_INPUT_ERROR)
            } else {
                textViewEmailError.visibility = View.INVISIBLE
                editTextEmail.setBackgroundResource(DRAWABLE_RES_INPUT_NORMAL)
            }
        }
    }

    private val checkedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        when (buttonView?.id) {
            R.id.checkBoxAll -> {
                toggleOnCheckedChangedListener(false)

                checkBoxTerms.isChecked = isChecked
                checkBoxPrivacyUse.isChecked = isChecked
                checkBoxPrivacyConsignment.isChecked = isChecked

                toggleOnCheckedChangedListener(true)
            }
        }

        buttonSubmit.isEnabled = isAllInputInformation()
    }

    private val clickListener = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            when (v?.id) {
                R.id.buttonSubmit -> handleSubmit()

                R.id.layoutTermOfUseDetail -> {
                    startActivity(Intent(v.context, AgreementActivity::class.java).apply {
                        putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE,
                                AgreementActivity.INTENT_EXTRA_VALUE_TERM)
                    })
                }

                R.id.layoutPrivacyUseDetail -> {
                    startActivity(Intent(v.context, AgreementActivity::class.java).apply {
                        putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE,
                                AgreementActivity.INTENT_EXTRA_VALUE_PRIVACY_USE)
                    })
                }

                R.id.layoutPrivacyConsignmentDetail -> {
                    startActivity(Intent(v.context, AgreementActivity::class.java).apply {
                        putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE,
                                AgreementActivity.INTENT_EXTRA_VALUE_PRIVACY_CONSIGNMENT)
                    })
                }
            }
        }
    }

}