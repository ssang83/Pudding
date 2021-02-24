package com.enliple.pudding.fragment.search

import android.graphics.Typeface
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.adapter.search.SearchTabPagerAdapter
import com.enliple.pudding.bus.SearchResultBus
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.fragment_search_result.*
import kotlinx.android.synthetic.main.fragment_search_result.viewPagerContainer
import kotlinx.android.synthetic.main.fragment_video_caster_profile.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
class SearchResultFragment : AbsBaseFragment() {
    private lateinit var pagerAdapter: SearchTabPagerAdapter
    private var keyword = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_result, container, false)
    }

    fun setKeyword(keyword: String) {
        this.keyword = keyword

        initTab(keyword)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: SearchResultBus) {
        Logger.e("onMessageEvent")

        var txt = when (data.type) {
            0 -> "영상"
            1 -> "상품"
            2 -> "편성표"
            3 -> "태그"
            4 -> "유저"
            else -> "영상"
        }

        if(data.count > 999) {
            tabLayout.getTabAt(data.type)?.text = "$txt\n(999+)"
        } else {
            tabLayout.getTabAt(data.type)?.text = "$txt\n(${data.count})"
        }

        setupTabLayoutFonts()
    }

    /**
     * Tab Content 초기화
     */
    private fun initTab(keyword: String) {
        tabLayout.setupWithViewPager(viewPagerContainer)
        pagerAdapter = SearchTabPagerAdapter(viewPagerContainer, childFragmentManager, keyword)
        viewPagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        viewPagerContainer.offscreenPageLimit = 5
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                setupTabLayoutFonts()
                viewPagerContainer.currentItem = tab!!.position
            }
        })
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
}