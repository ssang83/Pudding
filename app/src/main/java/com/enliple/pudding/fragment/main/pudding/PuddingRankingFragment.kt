package com.enliple.pudding.fragment.main.pudding

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.enliple.pudding.R
import com.enliple.pudding.adapter.ranking.RankingTabPagerAdapter
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.fragment_pudding_ranking.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by Kim Joonsung on 2019-01-24.
 */
class PuddingRankingFragment : androidx.fragment.app.Fragment() {

    private var mIsVisibleToUser = false
    private var pagerAdapter: RankingTabPagerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.i("onViewCreated")

        //EventBus.getDefault().post("ranking_refresh_day")
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")

        mIsVisibleToUser = isVisibleToUser

        if (mIsVisibleToUser) {
            if (pagerAdapter == null) {
                init()  // 속도 문제로 크루랭킹 탭을 눌렀을 경우에 그리자..
            }

            // refresh 가 필요하다.
            Logger.e("currentItem: ${viewPagerContainer.currentItem}")
            when (viewPagerContainer.currentItem) {
                0 -> EventBus.getDefault().post("ranking_refresh_day")
                1 -> EventBus.getDefault().post("ranking_refresh_week")
                2 -> EventBus.getDefault().post("ranking_refresh_month")
            }
        }
    }

    private fun init() {
        Logger.i("init")

        pagerAdapter = RankingTabPagerAdapter(childFragmentManager)
        viewPagerContainer.offscreenPageLimit = 3
        viewPagerContainer.adapter = pagerAdapter

        viewPagerContainer.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                Logger.i("onPageSelected: $position")

                (tabLayout.getChildAt(position) as RadioButton).isChecked = true
            }

            override fun onPageScrollStateChanged(state: Int) {
                // scroll 중에 swiperefresh 기능을 off 해야 정상적으로 swipe 된다.
                if (state == androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE) {
                    (parentFragment as PuddingHomeTabFragment).enableSwipeRefresh(true)
                } else {
                    (parentFragment as PuddingHomeTabFragment).enableSwipeRefresh(false)
                }
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }
        })

        tabLayout.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                when (checkedId) {
                    R.id.btn_1 -> {
                        EventBus.getDefault().post("ranking_refresh_day")
                        viewPagerContainer.setCurrentItem(0, false)

                        btn_1.setBackgroundResource(R.drawable.btn_tab_bg_box)
                        btn_2.setBackgroundResource(R.drawable.btn_tab_bg_top_bottom)
                        btn_3.setBackgroundResource(R.drawable.btn_tab_bg_box)
                    }

                    R.id.btn_2 -> {
                        EventBus.getDefault().post("ranking_refresh_week")
                        viewPagerContainer.setCurrentItem(1, false)

                        btn_1.setBackgroundResource(R.drawable.btn_tab_bg_top_left_bottom)
                        btn_2.setBackgroundResource(R.drawable.btn_tab_bg_box)
                        btn_3.setBackgroundResource(R.drawable.btn_tab_bg_top_right_bottom)
                    }

                    R.id.btn_3 -> {
                        EventBus.getDefault().post("ranking_refresh_month")
                        viewPagerContainer.setCurrentItem(2, false)

                        btn_1.setBackgroundResource(R.drawable.btn_tab_bg_box)
                        btn_2.setBackgroundResource(R.drawable.btn_tab_bg_top_bottom)
                        btn_3.setBackgroundResource(R.drawable.btn_tab_bg_box)
                    }
                }
            }
        })
    }
}