package com.enliple.pudding.activity

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.fragment.findaccount.FindIDFragment
import com.enliple.pudding.fragment.findaccount.FindPwFragment
import com.enliple.pudding.fragment.findaccount.PasswdChangeFragment
import com.enliple.pudding.fragment.findaccount.PasswdSettingFragment
import kotlinx.android.synthetic.main.activity_find_account.*
import kotlinx.android.synthetic.main.layout_find_account_id_result.*

/**
 * 아이디 / 패스워드 찾기 Activity
 * @author hkcha
 * @since 2018.08.07
 */
class FindAccountActivity : AbsBaseActivity() {

    companion object {
        private const val TAG = "FindAccountActivity"

        const val INTENT_EXTRA_KEY_CALL_MODE = "callmode"
        const val CALL_MODE_ID = 1
        const val CALL_MODE_PW = 2
    }

    private var callMode = CALL_MODE_ID                 // init Defaults

    private var idFragment: FindIDFragment? = null
    private var pwFragment: FindPwFragment? = null
    private var pwSettingFragment : PasswdSettingFragment? = null
    private var pwChangeFragment : PasswdChangeFragment? = null
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callMode = intent?.getIntExtra(INTENT_EXTRA_KEY_CALL_MODE, CALL_MODE_ID) ?: -1
        setContentView(R.layout.activity_find_account)

        when (callMode) {
            CALL_MODE_ID -> {
                replaceIdFragment()
                textViewTitle.setText(R.string.msg_find_account_id_title)
            }

            CALL_MODE_PW -> {
                replacePwFragment()
                textViewTitle.setText(R.string.msg_find_account_pw_title)
            }
            else -> finish()
        }

        buttonClose.setOnClickListener(clickListener)
        buttonLogin.setOnClickListener(clickListener)
        buttonFindPassword.setOnClickListener(clickListener)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    /**
     * 아이디 찾기 Fragment 를 표시
     */
    private fun replaceIdFragment() {
        var transaction = supportFragmentManager.beginTransaction()

        idFragment = supportFragmentManager.findFragmentByTag(FindIDFragment::class.java.name) as? FindIDFragment
        if (idFragment == null) {
            idFragment = FindIDFragment()
        }

        transaction.replace(R.id.layoutContainer, idFragment!!, FindIDFragment::class.java.name)
        transaction.commit()
    }

    /**
     * 비밀번호 찾기 Fragment 를 표시
     */
    private fun replacePwFragment() {
        var transaction = supportFragmentManager.beginTransaction()

        pwFragment = supportFragmentManager.findFragmentByTag(FindPwFragment::class.java.name) as? FindPwFragment
        if (pwFragment == null) {
            pwFragment = FindPwFragment()
        }

        transaction.replace(R.id.layoutContainer, pwFragment!!, FindPwFragment::class.java.name)
        transaction.commit()
    }

    /**
     * 휴대전화 인증 확인을 요청
     * @param confirmation
     */
    fun requestCellPhoneAuthConfirm(confirmation: String) {
//        layoutProgress.visibility = View.VISIBLE
//        Handler().postDelayed({ layoutProgress.visibility = View.INVISIBLE }, 2000L)
    }

    /**
     * 이메일을 통한 인증번호 확인을 요청
     * @param confirmation
     */
    fun requestEmailAuthConfirm(confirmation: String) {
//        layoutProgress.visibility = View.VISIBLE
//        Handler().postDelayed({ layoutProgress.visibility = View.INVISIBLE }, 2000L)
    }

    /**
     * 아이디 찾기 결과를 화면상에 표시
     * @param userId
     */
    fun showFindIdResult(userId: String) {
        layoutFindIdResult.visibility = View.VISIBLE

        if(userId.isNotEmpty()) {
            layoutSuccess.visibility = View.VISIBLE
            layoutFail.visibility = View.GONE

            val sb = StringBuilder(userId).apply {
                setCharAt(userId.length.minus(1), '*')
                setCharAt(userId.length.minus(2), '*')
                setCharAt(userId.length.minus(3), '*')
            }

            val resultMessage = String.format(getString(R.string.msg_find_account_id_result_format), sb.toString())
            val spanStr = SpannableString(resultMessage)
            val startSpanIndex = resultMessage.indexOf(sb.toString())

            if (startSpanIndex != -1) {
                spanStr.setSpan(ForegroundColorSpan(0xFF9f56f2.toInt()),
                        startSpanIndex, (startSpanIndex + userId.length), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            textViewFindIdResult.text = spanStr
        } else {
            layoutSuccess.visibility = View.GONE
            layoutFail.visibility = View.VISIBLE
            val resultMessage = String.format(getString(R.string.msg_find_account_id_result_fail_format), "정보를 다시 확인")
            val spanStr = SpannableString(resultMessage)
            val startSpanIndex = resultMessage.indexOf("정보를 다시 확인")

            if (startSpanIndex != -1) {
                spanStr.setSpan(ForegroundColorSpan(0xFF9f56f2.toInt()),
                        startSpanIndex, (startSpanIndex + 9), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spanStr.setSpan(StyleSpan(Typeface.BOLD), startSpanIndex, (startSpanIndex + 9), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            textViewFail.text = spanStr
        }
    }

    /**
     * 비밀번호 재 설정 Fragment 를 표시
     */
    fun showPasswordChange(id : String, certNumber:String) {
        userId = id

        var transaction = supportFragmentManager.beginTransaction()

        pwSettingFragment = supportFragmentManager.findFragmentByTag(PasswdSettingFragment::class.java.name) as? PasswdSettingFragment
        if (pwSettingFragment == null) {
            pwSettingFragment = PasswdSettingFragment().apply {
                arguments = Bundle().apply {
                    putString("certNum", certNumber)
                    putString("userId", id)
                }
            }
        }

        transaction.replace(R.id.layoutContainer, pwSettingFragment!!, PasswdSettingFragment::class.java.name)
        transaction.commit()
    }

    /**
     * 비밀번호 변경 완료 Fragment 를 표시
     */
    fun showPasswordChangeComplete() {
        var transaction = supportFragmentManager.beginTransaction()

        pwChangeFragment = supportFragmentManager.findFragmentByTag(PasswdChangeFragment::class.java.name) as? PasswdChangeFragment
        if (pwChangeFragment == null) {
            pwChangeFragment = PasswdChangeFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
        }

        transaction.replace(R.id.layoutContainer, pwChangeFragment!!, PasswdChangeFragment::class.java.name)
        transaction.commit()
    }

    /**
     * 현재 ID 검색 결과가 표시되고 있는 상태 인지 확인
     * @return
     */
    fun isIdResultShown(): Boolean = (layoutFindIdResult?.visibility
            ?: View.INVISIBLE) == View.VISIBLE


    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> {
                setResult(RESULT_CANCELED)
                finish()
            }

            R.id.buttonLogin -> {
                setResult(RESULT_OK)
                finish()
            }

            R.id.buttonFindPassword -> {
                // 현재의 Activity 를 종료하고 새롭게 비밀번호 찾기 모드로 호출
                startActivity(Intent(this, FindAccountActivity::class.java).apply {
                    putExtra(FindAccountActivity.INTENT_EXTRA_KEY_CALL_MODE, FindAccountActivity.CALL_MODE_PW)
                })
                finish()
            }
        }
    }
}