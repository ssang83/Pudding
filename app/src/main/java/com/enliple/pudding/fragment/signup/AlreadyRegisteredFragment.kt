package com.enliple.pudding.fragment.signup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.FindAccountActivity
import com.enliple.pudding.activity.LoginActivity
import com.enliple.pudding.activity.SignUpActivity
import kotlinx.android.synthetic.main.fragment_signup_already_registered.*
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * SignUpActivity 에서 이미 가입된 회원 정보가 있는 경우 사용자에게 표시되는 Fragment
 * @author hkcha
 * @since 2018.08.24
 */
class AlreadyRegisteredFragment : AbsBaseFragment() {
    companion object {
        const val BUNDLE_EXTRA_KEY_ID = "id"
        const val BUNDLE_EXTRA_KEY_DATE = "date"

        private val SERVER_DATE_FORMAT: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        private val SHOW_DATE_FORMAT: DateFormat = SimpleDateFormat("yyyy.MM.dd")
    }

    private var id: String? = null
    private var date: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        id = arguments?.getString(BUNDLE_EXTRA_KEY_ID)
        date = arguments?.getString(BUNDLE_EXTRA_KEY_DATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup_already_registered, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (!TextUtils.isEmpty(id)) {
            textViewAccount.text = id
        }

        if (!TextUtils.isEmpty(date)) {
            textViewDate.text = SHOW_DATE_FORMAT.format(SERVER_DATE_FORMAT.parse(date))
        }

        buttonLogin.setOnClickListener {
            if ((activity as? SignUpActivity)?.isFromLoginActrivity() == true) {
                activity?.setResult(Activity.RESULT_CANCELED)
                activity?.finish()
            } else {
                startActivity(Intent(it.context, LoginActivity::class.java))
            }
        }

        buttonFindPassword.setOnClickListener {
            startActivity(Intent(it.context, FindAccountActivity::class.java).apply {
                putExtra(FindAccountActivity.INTENT_EXTRA_KEY_CALL_MODE, FindAccountActivity.CALL_MODE_PW)
            })
        }
    }
}