package com.enliple.pudding.adapter.ranking

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.enliple.pudding.fragment.main.pudding.RankingFragment

/**
 */
class RankingTabPagerAdapter : androidx.fragment.app.FragmentStatePagerAdapter {
    private var mFragmentManager: androidx.fragment.app.FragmentManager

    constructor(fm: androidx.fragment.app.FragmentManager) : super(fm) {
        mFragmentManager = fm
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment =
            RankingFragment().apply {
                arguments = Bundle().apply {
                    putInt("position", position)
                }
            }

    override fun getCount(): Int = 3

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return "일간"
            1 -> return "주간"
            2 -> return "월간"
        }

        return ""
    }
}