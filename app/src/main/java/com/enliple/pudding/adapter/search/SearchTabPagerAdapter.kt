package com.enliple.pudding.adapter.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.enliple.pudding.AbsBaseTabFragment
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.enumeration.SearchResultTab
import com.enliple.pudding.fragment.search.*

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
class SearchTabPagerAdapter : androidx.fragment.app.FragmentStatePagerAdapter {
    private val fm: androidx.fragment.app.FragmentManager
    private val mViewPager: androidx.viewpager.widget.ViewPager
    private val fragments: MutableList<AbsBaseTabFragment> = ArrayList(SearchResultTab.values().size)
    private var mKeyword = ""

    constructor(viewPager: androidx.viewpager.widget.ViewPager, fm: androidx.fragment.app.FragmentManager, keyword: String) : super(fm) {
        this.mViewPager = viewPager
        this.fm = fm
        this.mKeyword = keyword

        var tab: Array<SearchResultTab> = SearchResultTab.values()

        Logger.e("mKeyword : $mKeyword")
        for (i in tab) {
            when (i.ordinal) {
                SearchResultTab.VOD.ordinal -> {
                    fragments.add(SearchVODFragment().apply {
                        arguments = Bundle().apply { putString(SearchVODFragment.BUNDLE_EXTRA_KEY_KEYWORD, mKeyword) }
                    })
                }

                SearchResultTab.HASHTAG.ordinal -> {
                    fragments.add(SearchHashtagFragment().apply {
                        arguments = Bundle().apply { putString(SearchHashtagFragment.BUNDLE_EXTRA_KEY_KEYWORD, mKeyword) }
                    })
                }

                SearchResultTab.USER.ordinal -> {
                    fragments.add(SearchUserFragment().apply {
                        arguments = Bundle().apply { putString(SearchUserFragment.BUNDLE_EXTRA_KEY_KEYWORD, mKeyword) }
                    })
                }

                SearchResultTab.PRODUCT.ordinal -> {
                    fragments.add(SearchProductFragment().apply {
                        arguments = Bundle().apply { putString(SearchProductFragment.BUNDLE_EXTRA_KEY_KEYWORD, mKeyword) }
                    })
                }

                SearchResultTab.SCHEDULE.ordinal -> {
                    fragments.add(SearchScheduleFragment().apply {
                        arguments = Bundle().apply { putString(SearchScheduleFragment.BUNDLE_EXTRA_KEY_KEYWORD, mKeyword) }
                    })
                }
            }
        }

        this.mViewPager.adapter = this
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return "영상"
            1 -> return "상품"
            2 -> return "편성표"
            3 -> return "태그"
            4 -> return "유저"
        }

        return ""
    }
}