package com.enliple.pudding.adapter.my

import android.os.Bundle
import com.enliple.pudding.enumeration.CalculateSubTab
import com.enliple.pudding.fragment.my.ExchangeFragment
import com.enliple.pudding.fragment.my.ExchangeListFragment

class CalculateSubTabAdapter : androidx.fragment.app.FragmentStatePagerAdapter {
    private val fm: androidx.fragment.app.FragmentManager
    private val mViewPager: androidx.viewpager.widget.ViewPager
    private val fragments: MutableList<androidx.fragment.app.Fragment> = ArrayList(CalculateSubTab.values().size)
    private var userId = ""
    private var userCookie = "0"
    private var userPoint = "0"
    private var isJelly = false
    constructor(viewPager: androidx.viewpager.widget.ViewPager, fm: androidx.fragment.app.FragmentManager, userId: String, userCookie: String, userPoint: String, isJelly: Boolean) : super(fm) {
        this.mViewPager = viewPager
        this.fm = fm
        this.userId = userId
        this.isJelly = isJelly
        this.userCookie = userCookie
        this.userPoint = userPoint

        var myTab: Array<CalculateSubTab> = CalculateSubTab.values()

        for (i in myTab) {
            when (i.ordinal) {
                CalculateSubTab.REQUEST_EXCHANGE.ordinal -> {
                    fragments.add(ExchangeFragment().apply {
                        arguments = Bundle().apply {
                            putString(ExchangeFragment.BUNDLE_EXTRA_KEY_USER_ID, userId)
                            putString(ExchangeFragment.BUNDLE_EXTRA_KEY_USER_COOKIE, userCookie)
                            putString(ExchangeFragment.BUNDLE_EXTRA_KEY_USER_POINT, userPoint)
                            putBoolean(ExchangeFragment.BUNDLE_EXTRA_KEY_IS_JELLY, isJelly)
                        }
                    })
                }

                CalculateSubTab.EXCHANGE_LIST.ordinal -> {
                    fragments.add(ExchangeListFragment().apply {
                        arguments = Bundle().apply {
                            putString(ExchangeListFragment.BUNDLE_EXTRA_KEY_USER_ID, userId)
                            putString(ExchangeListFragment.BUNDLE_EXTRA_KEY_USER_COOKIE, userCookie)
                            putString(ExchangeListFragment.BUNDLE_EXTRA_KEY_USER_POINT, userPoint)
                            putBoolean(ExchangeListFragment.BUNDLE_EXTRA_KEY_IS_JELLY, isJelly)
                        }
                    })
                }
            }
        }

        this.mViewPager.adapter = this
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment = fragments[position]

    override fun getCount(): Int = fragments.size
}