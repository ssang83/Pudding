package com.enliple.pudding.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.data.ApiParams
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.ui_compat.AsteriskPasswordTransformationMethod
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_passwd_change.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * Created by Kim Joonsung on 2018-10-29.
 */
class PasswdChangeActivity : AbsBaseActivity() {

    companion object {
        private const val TAG = "PasswdChangeActivity"

        private val COMMON_TEXT_FIELD_BACKGROUND_DEFAULT = R.drawable.bg_passwd_change
        private val COMMON_TEXT_FIELD_BACKGROUND_WARNING = R.drawable.bg_passwd_change_error

        private val passwordPattern1 = Pattern.compile("([a-zA-Z0-9].*[!,@,#,$,%,^,&,*,?,_,~])|([!,@,#,$,%,^,&,*,?,_,~].*[a-zA-Z0-9])")     // 영문,숫자,특수문자 조합 패턴
        private val passwordPattern2 = Pattern.compile("\\d{5,}")       // 5자이상 연속된 숫자 체크 패턴
    }

    private var currentPw = ""
    private var isCurPwChecked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passwd_change)
        EventBus.getDefault().register(this)

        buttonClose.setOnClickListener(clickListener)
        buttonConfirm.setOnClickListener(clickListener)

        editTextPasswd.transformationMethod = AsteriskPasswordTransformationMethod()
        editTextNewPasswd.transformationMethod = AsteriskPasswordTransformationMethod()
        editTextPasswdConfirm.transformationMethod = AsteriskPasswordTransformationMethod()

        editTextPasswd.addTextChangedListener(curPwWatcher)
        editTextNewPasswd.addTextChangedListener(newPwWatcher)
        editTextPasswdConfirm.addTextChangedListener(checkWacher)

    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    @Subscribe()
    fun onMessageEvent(data: NetworkBusResponse) {
        var key = NetworkHandler.getInstance(this@PasswdChangeActivity)
                .getKey(NetworkApi.API14.toString(), AppPreferences.getUserId(this@PasswdChangeActivity)!!, "")

        if (data.arg1.startsWith(key)) {
            handleNetworkResult(data)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            AppToast(this@PasswdChangeActivity).showToastMessage("비밀번호가 정상적으로 변경되었습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)

            AppToast(this@PasswdChangeActivity).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)

            textViewPasswdNotMatch.visibility = View.VISIBLE
            editTextPasswd.setBackgroundResource(COMMON_TEXT_FIELD_BACKGROUND_WARNING)
        }
    }

    private val curPwWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            isCurPwChecked = s.toString() == currentPw

            textViewPasswdNotMatch.visibility = View.GONE
            editTextPasswd.setBackgroundResource(COMMON_TEXT_FIELD_BACKGROUND_DEFAULT)
        }

        override fun afterTextChanged(s: Editable) {

        }
    }

    private val newPwWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            textViewNewPasswdNotMatch.visibility = View.GONE

            editTextNewPasswd.setBackgroundResource(COMMON_TEXT_FIELD_BACKGROUND_DEFAULT)
            editTextPasswdConfirm.setBackgroundResource(COMMON_TEXT_FIELD_BACKGROUND_DEFAULT)
        }

        override fun afterTextChanged(s: Editable) {

        }
    }

    private val checkWacher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            textViewNewPasswdNotMatch.visibility = View.GONE

            editTextNewPasswd.setBackgroundResource(COMMON_TEXT_FIELD_BACKGROUND_DEFAULT)
            editTextPasswdConfirm.setBackgroundResource(COMMON_TEXT_FIELD_BACKGROUND_DEFAULT)
        }

        override fun afterTextChanged(s: Editable) {

        }
    }


    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()

            R.id.buttonConfirm -> {
                if (editTextNewPasswd.text.toString() != editTextPasswdConfirm.text.toString()) {
                    textViewNewPasswdNotMatch.visibility = View.VISIBLE

                    editTextNewPasswd.setBackgroundResource(COMMON_TEXT_FIELD_BACKGROUND_WARNING)
                    editTextPasswdConfirm.setBackgroundResource(COMMON_TEXT_FIELD_BACKGROUND_WARNING)
                } else {

                    var requestObj = ApiParams.PasswdParams(
                            "update",
                            "",
                            editTextPasswd.text.toString(),
                            editTextNewPasswd.text.toString(),
                            editTextPasswdConfirm.text.toString())

                    EventBus.getDefault().post(NetworkBus(NetworkApi.API14.name, AppPreferences.getUserId(this@PasswdChangeActivity)!!, requestObj))

//                    JSONObject().apply {
//                        put("strType", "update")
//                        put("strCertNum", "")
//                        put("strPwOld", editTextPasswd.text.toString())
//                        put("strPwNew", editTextNewPasswd.text.toString())
//                        put("strPwChk", editTextPasswdConfirm.text.toString())
//                    }.let {
//                        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
//                        EventBus.getDefault().post(NetworkBus(NetworkApi.API14.name, AppPreferences.getUserId(this@PasswdChangeActivity)!!, body))
//                    }
                }
            }
        }
    }
}