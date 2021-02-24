package com.enliple.pudding.activity

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.enumeration.SignUpStep
import com.enliple.pudding.fragment.signup.*
import com.enliple.pudding.widget.DoubleButtonDialog
import kotlinx.android.synthetic.main.activity_signup.*

/**
 * 회원가입 Activity
 * @author hkcha
 * @since 2018.08.10
 */
class SignUpActivity : AbsBaseActivity(), DoubleButtonDialog.Listener {

    companion object {
        const val INTENT_EXTRA_KEY_FROM_LOGIN = "from_login"
    }


    private var infoFragment: SignUpInfoFragment? = null
//    private var identificationFragment: SignUpIdentificationFragment? = null
//    private var informationFragment: SignUpInformationFragment? = null
    private var completeFragment: SignUpCompleteFragment? = null
//    private var alreadyRegisteredFragment: AlreadyRegisteredFragment? = null

    private var currentStep: SignUpStep = SignUpStep.INFORMATION              // Init Defaults
    private var fromLogin = false // 이미 회원가입 되어 있을 경우 AlreadyRegisteredFragment에서 사용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fromLogin = intent.getBooleanExtra(INTENT_EXTRA_KEY_FROM_LOGIN, false)
        setContentView(R.layout.activity_signup)

        buttonClose.setOnClickListener(clickListener)

        infoFragment = supportFragmentManager.findFragmentByTag(SignUpInfoFragment::class.java.name) as? SignUpInfoFragment
        if (infoFragment == null) {
            infoFragment = SignUpInfoFragment()
        }

        var transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.layoutContainer, infoFragment!!, SignUpInfoFragment::class.java.name)
        transaction.commit()

        currentStep = SignUpStep.INFORMATION

//        replaceIdentificationFragment()
    }

    override fun onBackPressed() {
        when (currentStep) {
//            SignUpStep.IDENTIFICATION -> {
//                setResult(Activity.RESULT_CANCELED)
//                finish()
//            }

            SignUpStep.INFORMATION -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
//                replaceIdentificationFragment()
            }

            SignUpStep.COMPLETE -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onConfirm() {
        onBackPressed()
    }

    /**
     * 회원가입 1단계 : 본인인증 Fragment 를 표시
     */
//    fun replaceIdentificationFragment() {
//        identificationFragment = supportFragmentManager.findFragmentByTag(SignUpIdentificationFragment::class.java.name) as? SignUpIdentificationFragment
//        if (identificationFragment == null) {
//            identificationFragment = SignUpIdentificationFragment()
//        }
//
//        var transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.layoutContainer, identificationFragment!!, SignUpIdentificationFragment::class.java.name)
//        transaction.commit()
//
//        currentStep = SignUpStep.IDENTIFICATION
//    }

    /**
     * 이미 가입된 이력이 있음을 알리는 Fragment 를 표시
     * @param registeredId
     * @param registeredDate
     */
//    fun replaceAlreadyRegisteredFragment(registeredId: String,
//                                         registeredDate: String) {
//        alreadyRegisteredFragment = supportFragmentManager
//                .findFragmentByTag(AlreadyRegisteredFragment::class.java.name) as? AlreadyRegisteredFragment
//        if (alreadyRegisteredFragment == null) {
//            alreadyRegisteredFragment = AlreadyRegisteredFragment()
//        }
//
//        alreadyRegisteredFragment?.arguments = Bundle().apply {
//            putString(AlreadyRegisteredFragment.BUNDLE_EXTRA_KEY_DATE, registeredDate)
//            putString(AlreadyRegisteredFragment.BUNDLE_EXTRA_KEY_ID, registeredId)
//        }
//
//        var transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.layoutContainer, alreadyRegisteredFragment!!, AlreadyRegisteredFragment::class.java.name)
//        transaction.commit()
//
//        currentStep = SignUpStep.IDENTIFICATION
//    }

    /**
     * 회원가입 2단계 : 휴대폰 인증 이후 회원정보 Fragment 를 표시
     */
//    fun replaceInformationFragmentByCellPhone(cert_type: String?,
//                                              mb_name: String?,
//                                              phone_no: String?,
//                                              hash_data: String?,
//                                              adult: String?,
//                                              birth: String?,
//                                              sex: String?,
//                                              dupinfo: String?) {
//        informationFragment = supportFragmentManager.findFragmentByTag(SignUpInformationFragment::class.java.name) as? SignUpInformationFragment
//        if (informationFragment == null) {
//            informationFragment = SignUpInformationFragment()
//        }
//
//        informationFragment?.arguments = Bundle().apply {
//            putInt(SignUpInformationFragment.BUNDLE_EXTRA_KEY_CERTIFICATION_TYPE, SignUpInformationFragment.BUNDLE_EXTRA_VALUE_CERTIFICATION_TYPE_CELLPHONE)
//            putString(SignUpInformationFragment.BUNDLE_EXTRA_KEY_CERTIFICATION_CELLPHONE, phone_no)
//            putString(SignUpInformationFragment.BUNDLE_EXTRA_KEY_CERTIFICATION_NAME, mb_name)
//            putString(SignUpInformationFragment.BUNDLE_EXTRA_KEY_CERTIFICATION_HASH_DATA, hash_data)
//            putString(SignUpInformationFragment.BUNDLE_EXTRA_KEY_CERTIFICATION_ADULT, adult)
//            putString(SignUpInformationFragment.BUNDLE_EXTRA_KEY_CERTIFICATION_BIRTH, birth)
//            putString(SignUpInformationFragment.BUNDLE_EXTRA_KEY_CERTIFICATION_SEX, sex)
//            putString(SignUpInformationFragment.BUNDLE_EXTRA_KEY_CERTIFICATION_DUP_INFO, dupinfo)
//        }
//
//        var transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.layoutContainer, informationFragment!!, SignUpInformationFragment::class.java.name)
//        transaction.commit()
//
//        currentStep = SignUpStep.INFORMATION
//    }

    /**
     * 회원가입 완료 Fragment 를 표시
     */
    fun replaceCompleteFragment() {
        completeFragment = supportFragmentManager.findFragmentByTag(SignUpCompleteFragment::class.java.name) as? SignUpCompleteFragment
        if (completeFragment == null) {
            completeFragment = SignUpCompleteFragment()
        }

        var transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.layoutContainer, completeFragment!!, SignUpCompleteFragment::class.java.name)
        transaction.commit()

        currentStep = SignUpStep.COMPLETE
    }

    /**
     * 회원 가입이 로그인 화면에서 호출되었는지 확인
     */
    fun isFromLoginActrivity(): Boolean = fromLogin

    private val clickListener = object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            when (v?.id) {
                R.id.buttonClose -> {
                    DoubleButtonDialog(
                            this@SignUpActivity,
                            "회원가입을 취소하고 이전 화면으로 돌아갑니다.",
                            "회원가입").apply {
                        setListener(this@SignUpActivity)
                        show()
                    }
                }
            }
        }
    }
}