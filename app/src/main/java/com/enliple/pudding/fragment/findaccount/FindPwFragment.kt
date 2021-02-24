package com.enliple.pudding.fragment.findaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.enumeration.FindAuthType

/**
 * 비밀번호 찾기 Fragment
 */
class FindPwFragment : AbsBaseFragment() {

    private var memberInfoFragment: FindPwMemberInfoFragment? = null
    private var identificationFragment: FindPwIdentificationFragment? = null

    private var currentAuthType: FindAuthType = FindAuthType.MEMBER_INFO             // init Defaults

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_pw_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        replaceMemberInfoFragment()
    }

    /**
     * 회원정보 찾기 SubFragment 호출
     */
    private fun replaceMemberInfoFragment() {
        var transaction = childFragmentManager.beginTransaction()
        memberInfoFragment = childFragmentManager.findFragmentByTag(FindPwMemberInfoFragment::class.java.name) as? FindPwMemberInfoFragment
        if (memberInfoFragment == null) {
            memberInfoFragment = FindPwMemberInfoFragment()
        }
        transaction.replace(R.id.layoutSubContainer, memberInfoFragment!!, FindPwMemberInfoFragment::class.java.name)
        transaction.commit()

        currentAuthType = FindAuthType.MEMBER_INFO
    }

    /**
     * 본인명의 인증 SubFragment 호출
     */
    private fun replaceIdentificationFragment() {
        var transaction = childFragmentManager.beginTransaction()
        identificationFragment = childFragmentManager.findFragmentByTag(FindPwIdentificationFragment::class.java.name) as? FindPwIdentificationFragment
        if (identificationFragment == null) {
            identificationFragment = FindPwIdentificationFragment()
        }
        transaction.replace(R.id.layoutSubContainer, identificationFragment!!, FindPwIdentificationFragment::class.java.name)
        transaction.commit()

        currentAuthType = FindAuthType.IDENTIFICATION
    }
}