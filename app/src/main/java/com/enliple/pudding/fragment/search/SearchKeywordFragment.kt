package com.enliple.pudding.fragment.search

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.enliple.pudding.R
import com.enliple.pudding.activity.SearchActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_search_keyword.*
import kotlinx.android.synthetic.main.fragment_search_keyword.viewPagerContainer

/**
 * Created by Kim Joonsung on 2019-05-08.
 */
class SearchKeywordFragment : Fragment() {

    private lateinit var mPagerAdapter: PagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_search_keyword, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTab()
    }

    fun setKeyword(keyword:String) {
        (activity as SearchActivity).searchResult(keyword)
    }

    private fun initTab() {
        tabLayout.setupWithViewPager(viewPagerContainer)
        mPagerAdapter = PagerAdapter(viewPagerContainer, childFragmentManager)
        viewPagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        viewPagerContainer.offscreenPageLimit = 2
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                setupTabLayoutFonts()
                viewPagerContainer.currentItem = tab!!.position
            }
        })

        setupTabLayoutFonts()
    }

    private fun setupTabLayoutFonts() {
        val tabPosition = tabLayout.selectedTabPosition
        val vg = tabLayout.getChildAt(0) as ViewGroup
        val tabCnt = vg.childCount
        for (i in 0 until tabCnt) {
            val vgTab = vg.getChildAt(i) as ViewGroup
            val tabChildCnt = vgTab.childCount
            for (j in 0 until tabChildCnt) {
                val tabViewChild = vgTab.getChildAt(j)
                if (tabViewChild is AppCompatTextView) {
                    if (i == tabPosition) {
                        tabViewChild.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/notosanskr_bold.otf")
                    } else {
                        tabViewChild.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/notosanskr_regular.otf")
                    }
                }
            }
        }
    }

    inner class PagerAdapter:FragmentPagerAdapter {
        private val fm: androidx.fragment.app.FragmentManager
        private val mViewPager: androidx.viewpager.widget.ViewPager

        constructor(viewPager: androidx.viewpager.widget.ViewPager, fm: androidx.fragment.app.FragmentManager) : super(fm) {
            this.mViewPager = viewPager
            this.fm = fm

            this.mViewPager.adapter = this
        }

        override fun getItem(position: Int) =
                when(position) {
                    0 -> KeywordHotFragment()
                    else -> KeywordLatestFragment()
                }

        override fun getCount() = 2

        override fun getPageTitle(position: Int) : CharSequence? {
            when(position) {
                0 -> return "인기 검색어"
                1 -> return "최근 검색어"
            }

            return ""
        }

    }
}