package com.enliple.pudding.fragment.findaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.enumeration.FindAuthType

/**
 * 아이디 찾기 Fragment
 * @author hkcha
 * @since 2018.08.16
 */
class FindIDFragment : AbsBaseFragment() {

    private var memberInfoFragment: FindIDMemberInfoFragment? = null
    private var identificationFragment: FindIDIdentificationFragment? = null

    private var currentAuthType: FindAuthType = FindAuthType.MEMBER_INFO             // init defaults


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_id_main, container, false)
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
        memberInfoFragment = childFragmentManager.findFragmentByTag(FindIDMemberInfoFragment::class.java.name) as? FindIDMemberInfoFragment
        if (memberInfoFragment == null) {
            memberInfoFragment = FindIDMemberInfoFragment()
        }
        transaction.replace(R.id.layoutSubContainer, memberInfoFragment!!, FindIDMemberInfoFragment::class.java.name)
        transaction.commit()

        currentAuthType = FindAuthType.MEMBER_INFO
    }

    /**
     * 본인명의 인증 SubFragment 호출
     */
    private fun replaceIdentificationFragment() {
        var transaction = childFragmentManager.beginTransaction()
        identificationFragment = childFragmentManager.findFragmentByTag(FindIDIdentificationFragment::class.java.name) as? FindIDIdentificationFragment
        if (identificationFragment == null) {
            identificationFragment = FindIDIdentificationFragment()
        }
        transaction.replace(R.id.layoutSubContainer, identificationFragment!!, FindIDIdentificationFragment::class.java.name)
        transaction.commit()

        currentAuthType = FindAuthType.IDENTIFICATION
    }
}