package com.enliple.pudding.fragment.findaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.enumeration.FindMemberInfoSearchType
import kotlinx.android.synthetic.main.fragment_find_member.*

/**
 * 비밀번호 찾기 - 회원정보 찾기 방식 Main Fragment
 * @author hkcha
 * @since 2018.08.16
 */
class FindPwIdentificationFragment : AbsBaseFragment() {

    private var cellPhoneFragment: MemberCellPhoneFragment? = null
    private var emailFragment: MemberEmailFragment? = null

    private var findIdType = FindMemberInfoSearchType.CELLPHONE                               // init defaults


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_member, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        replaceFindEmailFragment()
    }


    /**
     * 휴대폰으로 찾기 화면으로 전환
     */
    private fun replaceFindCellPhoneFragment() {
        var transaction = childFragmentManager.beginTransaction()
        cellPhoneFragment = childFragmentManager.findFragmentByTag(MemberCellPhoneFragment::class.java.name) as? MemberCellPhoneFragment
        if (cellPhoneFragment == null) {
            cellPhoneFragment = MemberCellPhoneFragment().apply {
                arguments = Bundle().apply {
                    putInt(MemberCellPhoneFragment.BUNDLE_EXTRA_KEY_CALL_MODE, MemberCellPhoneFragment.BUNDLE_EXTRA_VALUE_FIND_PW)
                }
            }
        }
        transaction.replace(R.id.layoutFindContainer, cellPhoneFragment!!, MemberCellPhoneFragment::class.java.name)
        transaction.commit()

        findIdType = FindMemberInfoSearchType.CELLPHONE
    }

    /**
     * 이메일로 찾기 화면으로 전환
     */
    private fun replaceFindEmailFragment() {
        var transaction = childFragmentManager.beginTransaction()
        emailFragment = childFragmentManager.findFragmentByTag(MemberEmailFragment::class.java.name) as? MemberEmailFragment
        if (emailFragment == null) {
            emailFragment = MemberEmailFragment().apply {
                arguments = Bundle().apply {
                    putInt(MemberEmailFragment.BUNDLE_EXTRA_KEY_CALL_MODE, MemberEmailFragment.BUNDLE_EXTRA_VALUE_FIND_PW)
                }
            }
        }
        transaction.replace(R.id.layoutFindContainer, emailFragment!!, MemberEmailFragment::class.java.name)
        transaction.commit()

        findIdType = FindMemberInfoSearchType.EMAIL
    }
}