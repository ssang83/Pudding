package com.enliple.pudding.adapter.home

import android.os.Bundle
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.fragment.main.CasterProductFragment
import com.enliple.pudding.fragment.main.CasterVODFragment
import com.enliple.pudding.shoppingplayer.CasterTab

/**
 * Created by Kim Joonsung on 2018-10-10.
 */
class CasterTabPagerAdapter : androidx.fragment.app.FragmentStatePagerAdapter {

    private val fm: androidx.fragment.app.FragmentManager
    private val mViewPager: androidx.viewpager.widget.ViewPager
    private val fragments: MutableList<androidx.fragment.app.Fragment> = ArrayList(CasterTab.values().size)
    private var userId = ""

    constructor(viewPager: androidx.viewpager.widget.ViewPager, fm: androidx.fragment.app.FragmentManager, userId: String) : super(fm) {
        Logger.d("init:$userId")

        this.mViewPager = viewPager
        this.fm = fm
        this.userId = userId

        var myTab: Array<CasterTab> = CasterTab.values()

        for (i in myTab) {
            when (i.ordinal) {
                CasterTab.VIDEO.ordinal -> {
                    fragments.add(CasterVODFragment().apply {
                        arguments = Bundle().apply { putString(CasterVODFragment.BUNDLE_EXTRA_KEY_USER_ID, userId) }
                    })
                }

                CasterTab.PRODUCT.ordinal -> {
                    fragments.add(CasterProductFragment().apply {
                        arguments = Bundle().apply { putString(CasterProductFragment.BUNDLE_EXTRA_KEY_USER_ID, userId) }
                    })
                }
            }
        }

        this.mViewPager.adapter = this
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence? = ""
}