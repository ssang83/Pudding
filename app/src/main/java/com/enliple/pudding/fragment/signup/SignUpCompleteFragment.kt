package com.enliple.pudding.fragment.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseFragment
import kotlinx.android.synthetic.main.fragment_signup_success.*
import kotlinx.android.synthetic.main.layout_signup_level_status.*
import com.enliple.pudding.R
import com.igaworks.v2.abxExtensionApi.AbxCommon
import com.igaworks.v2.core.AdBrixRm
import java.lang.Exception

/**
 * 회원가입 완료 안내 Fragment
 * @author hkcha
 * @since 2018.08.14
 */
class SignUpCompleteFragment : AbsBaseFragment() {

    companion object {
        private const val TAG = "SignUpCompleteFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            AbxCommon.signUp(AdBrixRm.CommonSignUpChannel.Google, AdBrixRm.CommonProperties.SignUp())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        imageViewStatus3.isSelected = true
        buttonConfirm.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}