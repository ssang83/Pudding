package com.enliple.pudding.fragment.findaccount

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.LoginActivity
import kotlinx.android.synthetic.main.fragment_pw_change.*

/**
 * Created by Kim Joonsung on 2019-04-26.
 */
class PasswdChangeFragment : Fragment() {

    private var mUserId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mUserId = arguments?.getString("userId") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_pw_change, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonLogin.setOnClickListener{
            startActivity(Intent(context!!, LoginActivity::class.java))
            if ( activity != null )
                activity!!.finish()
        }

        val sb = SpannableStringBuilder(mUserId).apply {
            setSpan(ForegroundColorSpan(Color.parseColor("#9f56f2")), 0, mUserId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), 0, mUserId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            append("님의 비밀번호가 성공적으로 변경되었습니다.")
        }

        textViewAccount.append(sb)
    }
}