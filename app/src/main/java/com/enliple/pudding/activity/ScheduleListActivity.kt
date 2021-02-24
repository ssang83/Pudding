package com.enliple.pudding.activity

import android.graphics.Typeface
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.widget.AppCompatTextView
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.bus.ReservationBus
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.fragment.main.ScheduleListFragment
import com.enliple.pudding.fragment.main.SchedulePrevListFragment
import kotlinx.android.synthetic.main.activity_schedule_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 편성표 내역
 */
class ScheduleListActivity : AbsBaseActivity() {
    private lateinit var mAdapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_schedule_list)

        EventBus.getDefault().register(this)

        buttonBack.setOnClickListener { finish() }

        initTab()
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: ReservationBus) {
        Logger.e("onMessageEvent")

        val txt = when (data.type) {
            0 -> "예약내역"
            1 -> "이전내역"
            else -> ""
        }

        tabLayout.getTabAt(data.type)?.text = "$txt(${data.count})"
    }

    private fun initTab() {
        tabLayout.setupWithViewPager(viewPager)
        mAdapter = PagerAdapter(supportFragmentManager)
        viewPager.adapter = mAdapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                setupTabLayoutFonts()
                viewPager.currentItem = tab!!.position
            }
        })

        tabLayout.getTabAt(0)?.select()

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
                        tabViewChild.typeface = Typeface.createFromAsset(assets, "fonts/notosanskr_bold.otf")
                    } else {
                        tabViewChild.typeface = Typeface.createFromAsset(assets, "fonts/notosanskr_regular.otf")
                    }
                }
            }
        }
    }

    inner class PagerAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): androidx.fragment.app.Fragment =
                when(position) {
                    0 -> ScheduleListFragment()
                    else -> SchedulePrevListFragment()
                }

        override fun getCount(): Int = 2

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "예약내역"
                1 -> return "이전내역"
            }

            return ""
        }
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}
}