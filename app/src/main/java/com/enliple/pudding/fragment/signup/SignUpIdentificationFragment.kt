package com.enliple.pudding.fragment.signup

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.PGAuthActivity
import com.enliple.pudding.activity.SignUpActivity
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API75
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_signup_identification.*
import kotlinx.android.synthetic.main.layout_signup_level_status.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 회원가입 1단계 본인인증 Fragment
 * @author hkcha
 * @since 2018.08.14
 */
class SignUpIdentificationFragment : AbsBaseFragment() {

    companion object {
        private const val TAG = "SignUpIdentificationFragment"
        private const val REQUEST_CODE_CELLPHONE_IDENTIFICATION = 0xAB01
        private const val RETRY_CELLPHONE_VALIDATION_MAXIMUM_COUNT = 3
    }

    private var isRequestIdentification = false
    private var retryCellPhoneValidationCount = 0
    //    private var name:String = ""
//    private var phoneNumber:String = ""
    private var certType = ""
    private var mbName = ""
    private var phoneNo = ""
    private var hashData = ""
    private var adult = ""
    private var birth = ""
    private var sex = ""
    private var dupInfo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup_identification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        imageViewStatus1.isSelected = true
        buttonCellPhoneAuth.setOnClickListener(clickListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_CELLPHONE_IDENTIFICATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Success
                    if (data != null) {
//                        putExtra("cert_type", cert_type)
//                        putExtra("mb_name", mb_name)
//                        putExtra("phone_no", phone_no)
//                        putExtra("hash_data", hash_data)
//                        putExtra("adult", adult)
//                        putExtra("birth", birth)
//                        putExtra("sex", sex)
//                        putExtra("dupinfo", dupinfo)
                        certType = data.getStringExtra("cert_type")
                        mbName = data.getStringExtra("mb_name")
                        phoneNo = data.getStringExtra("phone_no")
                        hashData = data.getStringExtra("hash_data")
                        adult = data.getStringExtra("adult")
                        birth = data.getStringExtra("birth")
                        sex = data.getStringExtra("sex")
                        dupInfo = data.getStringExtra("dupinfo")

                        handleAlReadyRegisterByCellPhoneUser(mbName, phoneNo)
                    } else {
                        // 앱 내부 간에 직접넘겨줘서 이럴일은 없겠으나.. 재시도를 할 수 있게 사용자에게 여지를 남겨둠
                    }
                } else {
                    // Failed
                }
            }
        }

        isRequestIdentification = false
        super.onActivityResult(requestCode, resultCode, data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        var API75 = NetworkHandler.getInstance(context!!)
                .getKey(NetworkApi.API75.toString(), "", "")

        when (data.arg1) {
            API75 -> handleAlReadyRegisterByCellPhoneUser(data)
        }
    }

    /**
     * 해당 휴대폰 번호 및 실명을 이용하여 가입된 사용자가 있는지 검사
     */
    private fun handleAlReadyRegisterByCellPhoneUser(mb_name: String, phone_no: String) {
        progressBar.visibility = View.VISIBLE

        var requestObj = JSONObject()
        requestObj.put("userName", mb_name)
        requestObj.put("userHp", phone_no)

        var body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString())
        val bus = NetworkBus(NetworkApi.API75.name, body)
        EventBus.getDefault().post(bus)
    }

    private fun handleAlReadyRegisterByCellPhoneUser(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            var response: API75 = Gson().fromJson(str, API75::class.java)

            progressBar.visibility = View.GONE
            if (response.status) {
                // 기존에 가입된 사용자가 있음을 알림
//                (activity as? SignUpActivity)?.replaceAlreadyRegisteredFragment(response.mb_id
//                        ?: "", response.mb_datetime ?: "")

            } else {
                // 기존에 사용자가 가입되어 있지 않음

//                (activity as? SignUpActivity)?.replaceInformationFragmentByCellPhone(
//                        certType, mbName, phoneNo, hashData, adult, birth, sex, dupInfo)
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("Validation ERR $errorResult")

            if (retryCellPhoneValidationCount < RETRY_CELLPHONE_VALIDATION_MAXIMUM_COUNT) {
                ++retryCellPhoneValidationCount
                handleAlReadyRegisterByCellPhoneUser(mbName, phoneNo)
            } else {
                AlertDialog.Builder(context!!)
                        .setMessage(R.string.error_app)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                            retryCellPhoneValidationCount = 0

                        }.show()
            }
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonCellPhoneAuth -> {
                if (!isRequestIdentification) {
                    startActivityForResult(Intent(it!!.context, PGAuthActivity::class.java).apply {
                        putExtra(PGAuthActivity.INTENT_EXTRA_KEY_CALL_MODE, PGAuthActivity.INTENT_EXTRA_VALUE_MODE_IDENTIFICATION)
                    }, REQUEST_CODE_CELLPHONE_IDENTIFICATION)

                    isRequestIdentification = true
                }
            }
        }
    }
}