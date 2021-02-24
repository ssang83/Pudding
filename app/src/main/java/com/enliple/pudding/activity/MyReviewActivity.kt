package com.enliple.pudding.activity

import android.graphics.Typeface
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.appcompat.widget.AppCompatTextView
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.fragment.my.MyReviewFragment
import com.enliple.pudding.fragment.my.UnwrittenReviewFragment
import kotlinx.android.synthetic.main.activity_my_review.*

/**
 * Created by Kim Joonsung on 2018-12-13.
 */
class MyReviewActivity : AbsBaseActivity() {

    private lateinit var mAdapter: PagerAdapter

    enum class Tab {
        UNWRITTEN_REVIEW,
        MY_REVIEW
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_review)
        buttonBack.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                finish()
            }
        })
        initTab()
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    fun setMyReviewCount(count: Int) {
        tabLayout.getTabAt(1)!!.text = "내가 쓴 리뷰(${count})"
        setupTabLayoutFonts()
    }

    fun setUnReviewCount(count: Int) {
        tabLayout.getTabAt(0)!!.text = "미작성 리뷰(${count})"
        setupTabLayoutFonts()
    }

    private fun initTab() {
        mAdapter = PagerAdapter(supportFragmentManager)
        viewPager.adapter = mAdapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addTab(tabLayout.newTab().setText("미작성 리뷰"))
        tabLayout.addTab(tabLayout.newTab().setText("내가 쓴 리뷰"))

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

    class PagerAdapter : androidx.fragment.app.FragmentPagerAdapter {
        private val fm: androidx.fragment.app.FragmentManager
        private var fragments: MutableList<AbsBaseFragment> = ArrayList(Tab.values().size)

        constructor(fm: androidx.fragment.app.FragmentManager) : super(fm) {
            this.fm = fm

            var tab: Array<Tab> = Tab.values()
            for (i in tab) {
                when (i.ordinal) {
                    Tab.UNWRITTEN_REVIEW.ordinal -> fragments.add(UnwrittenReviewFragment())
                    Tab.MY_REVIEW.ordinal -> fragments.add(MyReviewFragment())
                }
            }
        }

        override fun getItem(position: Int): androidx.fragment.app.Fragment = fragments[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = fragments.size
    }
}