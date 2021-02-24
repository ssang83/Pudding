package com.enliple.pudding.fragment.my

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.CalculateSubTabAdapter
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.ui_compat.PixelUtil
import com.enliple.pudding.commons.widget.tab_layout.WrappedTabLayoutStripUtil
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_calc_main.*

class MainExchangeFragment  : androidx.fragment.app.Fragment() {

    companion object {
        const val BUNDLE_EXTRA_KEY_USER_ID = "MainExchangeFragment_userId"
        const val BUNDLE_EXTRA_KEY_ISJELLY = "MainExchangeFragment_isJelly"
        const val BUNDLE_EXTRA_KEY_USER_COOKIE = "MainExchangeFragment_userCookie"
        const val BUNDLE_EXTRA_KEY_USER_POINT = "MainExchangeFragment_point"
    }

    private var userId = ""
    private var userCookie = "0"
    private var userPoint = "0"
    private var isJelly = false
    private var pagerAdapter: CalculateSubTabAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calc_main, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            userId = arguments!!.getString(BUNDLE_EXTRA_KEY_USER_ID)
            isJelly = arguments!!.getBoolean(BUNDLE_EXTRA_KEY_ISJELLY)
            userCookie =arguments!!.getString(BUNDLE_EXTRA_KEY_USER_COOKIE)
            userPoint =arguments!!.getString(BUNDLE_EXTRA_KEY_USER_POINT)
            Logger.e("CasterVODFragment userId :: $userId")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if ( isJelly ) {
            title.text = "현재 보유중인 젤리"
            try {
                var dJellyCnt = userCookie.toDouble()
                point.text = Utils.ToNumFormat(dJellyCnt) + "개"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            title.text = "현재 보유중인 포인트"
            try {
                var dUserPoint = userPoint.toDouble()
                point.text = Utils.ToNumFormat(dUserPoint) + "원"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        initTab(userId)
    }

    private fun initTab(userId: String) {
        if (pagerAdapter == null) {
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.require_exchange)))
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.exchange_list)))
        }

        pagerAdapter = CalculateSubTabAdapter(container, childFragmentManager, userId, userCookie, userPoint, isJelly)
        tabLayout.addOnTabSelectedListener(selectedListener)

        // TabStrip Size 축소
        WrappedTabLayoutStripUtil.wrapTabIndicatorToTitle(tabLayout,
                PixelUtil.dpToPx(context!!, 60),
                PixelUtil.dpToPx(context!!, 60))

        tabLayout.getTabAt(0)!!.select()

        setupTabLayoutFonts()
    }

    private fun setupTabLayoutFonts() {
        val tabPosition = tabLayout.getSelectedTabPosition()
        val vg = tabLayout.getChildAt(0) as ViewGroup
        val tabCnt = vg.childCount
        for (i in 0 until tabCnt) {
            val vgTab = vg.getChildAt(i) as ViewGroup
            val tabChildCnt = vgTab.childCount
            for (j in 0 until tabChildCnt) {
                val tabViewChild = vgTab.getChildAt(j)
                if (tabViewChild is AppCompatTextView) {
                    if (i == tabPosition) {
                        tabViewChild.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/notosanskr_medium.otf")
                        tabViewChild.textSize = 14f
                    } else {
                        tabViewChild.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/notosanskr_regular.otf")
                        tabViewChild.textSize = 14f
                    }
                }
            }
        }
    }

    private val selectedListener = object : TabLayout.OnTabSelectedListener {

        override fun onTabSelected(tab: TabLayout.Tab) {
            setupTabLayoutFonts()
            if (tab != null) {
                Logger.e("tab.getPosition :: " + tab.position)
                container.currentItem = tab.position
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {

        }

        override fun onTabReselected(tab: TabLayout.Tab?) {

        }
    }
}