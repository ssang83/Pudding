package com.enliple.pudding.activity

import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.fragment.my.SharedVODFragment
import com.enliple.pudding.fragment.my.SharingVODFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_share_content.*

/**
 * Created by Kim Joonsung on 2018-11-13.
 */
class ShareContentActivity : AbsBaseActivity() {

    private lateinit var mAdapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_share_content)

        mAdapter = PagerAdapter(supportFragmentManager)
        viewPager.adapter = mAdapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        buttonClose.setOnClickListener { finish() }
        initTab()
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    private fun initTab() {
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.msg_my_shared_vod)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.msg_my_share_vod)))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                setupTabLayoutFonts()
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
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
                        tabViewChild.typeface = Typeface.createFromAsset(assets, "fonts/notosanskr_bold.otf")
                    } else {
                        tabViewChild.typeface = Typeface.createFromAsset(assets, "fonts/notosanskr_regular.otf")
                    }
                }
            }
        }
    }

    class PagerAdapter : androidx.fragment.app.FragmentPagerAdapter {
        private val fm: androidx.fragment.app.FragmentManager
        private var fragments: MutableList<AbsBaseFragment> = ArrayList(Tab.values().size)

        constructor(fm: androidx.fragment.app.FragmentManager) : super(fm) {
            this.fm = fm

            var tab: Array<Tab> = Tab.values()
            for (i in tab) {
                when (i.ordinal) {
                    Tab.SHARED_VOD.ordinal -> fragments.add(SharedVODFragment())
                    Tab.SHARING_VOD.ordinal -> fragments.add(SharingVODFragment())
                }
            }
        }

        override fun getItem(position: Int): androidx.fragment.app.Fragment = fragments[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = fragments.size
    }

    enum class Tab {
        SHARED_VOD,
        SHARING_VOD
    }
}