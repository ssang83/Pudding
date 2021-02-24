package com.enliple.pudding.adapter.my

import android.os.Bundle
import com.enliple.pudding.commons.network.vo.API155
import com.enliple.pudding.enumeration.CalculateTab
import com.enliple.pudding.fragment.my.MainExchangeFragment

class CalculateTabPagerAdapter : androidx.fragment.app.FragmentStatePagerAdapter {
    private val fm: androidx.fragment.app.FragmentManager
    private val mViewPager: androidx.viewpager.widget.ViewPager
    private val fragments: MutableList<androidx.fragment.app.Fragment> = ArrayList(CalculateTab.values().size)
    private var userId = ""
    private var userCookie = "0"
    private var userPoint = "0"

    constructor(viewPager: androidx.viewpager.widget.ViewPager, fm: androidx.fragment.app.FragmentManager, userId: String, data: API155.ExchangeData) : super(fm) {
        this.mViewPager = viewPager
        this.fm = fm
        this.userId = userId
        this.userCookie = data.cookie
        this.userPoint = data.point

        var myTab: Array<CalculateTab> = CalculateTab.values()

        for (i in myTab) {
            when (i.ordinal) {
                CalculateTab.JELLY.ordinal -> {
                    fragments.add(MainExchangeFragment().apply {
                        arguments = Bundle().apply {
                            putString(MainExchangeFragment.BUNDLE_EXTRA_KEY_USER_ID, userId)
                            putString(MainExchangeFragment.BUNDLE_EXTRA_KEY_USER_COOKIE, userCookie)
                            putString(MainExchangeFragment.BUNDLE_EXTRA_KEY_USER_POINT, userPoint)
                            putBoolean(MainExchangeFragment.BUNDLE_EXTRA_KEY_ISJELLY, true)
                        }
                    })
                }

                CalculateTab.POINT.ordinal -> {
                    fragments.add(MainExchangeFragment().apply {
                        arguments = Bundle().apply {
                            putString(MainExchangeFragment.BUNDLE_EXTRA_KEY_USER_ID, userId)
                            putString(MainExchangeFragment.BUNDLE_EXTRA_KEY_USER_COOKIE, userCookie)
                            putString(MainExchangeFragment.BUNDLE_EXTRA_KEY_USER_POINT, userPoint)
                            putBoolean(MainExchangeFragment.BUNDLE_EXTRA_KEY_ISJELLY, false)
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