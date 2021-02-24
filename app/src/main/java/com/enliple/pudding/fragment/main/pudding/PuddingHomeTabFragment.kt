package com.enliple.pudding.fragment.main.pudding

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.enliple.pudding.R
import com.enliple.pudding.activity.CategoryActivity
import com.enliple.pudding.activity.MainActivity
import com.enliple.pudding.commons.log.Logger
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_pudding_home_tab.*
import org.greenrobot.eventbus.EventBus
import java.lang.ref.WeakReference

/**
 * Created by Kim Joonsung on 2019-01-25.
 */
class PuddingHomeTabFragment : androidx.fragment.app.Fragment(), SwipeRefreshLayout.OnRefreshListener {
    companion object {
        //private const val ACTIVITY_REQUEST_CODE_LOGIN_FOLLOWING = 0xAB04
        const val TIMER_HOME_DELAY = 10000L
        const val TIMER_HOME_PERIOD = 10000L
        const val TIMER_DELAY = 5000L
        const val TIMER_PERIOD = 5000L
        const val PAGE_HOME = 0
        const val PAGE_FASHION = 2
        const val PAGE_BEAUTY = 3
        const val PAGE_FOOD = 4
        const val PAGE_TRAVEL = 5

        private const val MESSAGE_ENABLE_SWIPE = 1000 // refresh on
        private const val MESSAGE_DISABLE_SWIPE = 1001 // refresh off

        //const val TAB_HOME = 0
        //const val TAB_FOLLOWING = 1
        //const val TAB_SHOPPING = 1
        //const val TAB_BEAUTY = 2
        //const val TAB_TRAVEL = 3
        //const val TAB_BEST = 5
        //const val TAB_RANKING = 4
        //const val TAB_EVENT = 5
    }

    private var mScrollX = -1
    private lateinit var mPagerAdapter: HomeTabPagerAdapter

    // Handler 메모리 릭 발생하니깐 항상 아래 처럼 만들자..
    private var mHandler: MyHandler? = null
    private var selectedPosition = -1
    private class MyHandler(fragment: PuddingHomeTabFragment) : Handler() {
        private val mReference: WeakReference<PuddingHomeTabFragment> = WeakReference(fragment)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_ENABLE_SWIPE -> mReference.get()?.enableSwipeRefresh(true)
                MESSAGE_DISABLE_SWIPE -> mReference.get()?.enableSwipeRefresh(false)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_home_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.i("onViewCreated")

        mHandler = MyHandler(this)

        tabLayout.setupWithViewPager(viewPagerContainer)

        mPagerAdapter = HomeTabPagerAdapter(childFragmentManager)

        FirebaseAnalytics.getInstance(activity!!).logEvent(mPagerAdapter.getItem(0).javaClass.simpleName, Bundle())

        viewPagerContainer.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                Logger.i("onPageSelected: $position")
                selectedPosition = position
                val intent = Intent("PAGE_CHANGED")
                intent.putExtra("page", position)
                intent.putExtra("gubun", "paging")
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)

                FirebaseAnalytics.getInstance(activity!!).logEvent(mPagerAdapter.getItem(position).javaClass.simpleName, Bundle())
                //(tabLayout.getChildAt(position) as RadioButton).isChecked = true
            }

            override fun onPageScrollStateChanged(state: Int) {
                Logger.d("onPageScrollStateChanged: $state")

                // scroll 중에 swiperefresh 기능을 off 해야 정상적으로 swipe 된다.
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    enableSwipeRefresh(true)
                } else {
                    enableSwipeRefresh(false)
                }
            }

            override fun onPageScrolled(position: Int, p1: Float, p2: Int) {
                Logger.d("onPageScrolled: $position")
            }
        })

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                setTabLayoutFont()

                viewPagerContainer.setCurrentItem(tab!!.position, false)
            }
        })

        // tabLayout scroll 중에 SwipeRefreshLayout disable 해야 scroll 에 문제가 없다.
        tabLayout.viewTreeObserver.addOnScrollChangedListener {
            var scrollX = tabLayout?.scrollX
            if (scrollX != null && scrollX != mScrollX) {
                mScrollX = scrollX

                mHandler?.sendEmptyMessage(MESSAGE_DISABLE_SWIPE)
                mHandler?.removeMessages(MESSAGE_ENABLE_SWIPE)
                mHandler?.sendEmptyMessageDelayed(MESSAGE_ENABLE_SWIPE, 300) // 0.3초 뒤에 refresh 되도록 한다.
            }
        }

        viewPagerContainer?.adapter = mPagerAdapter

        setTabLayoutFont()

        refreshLayout?.setOnRefreshListener(this)
        refreshLayout?.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        )

        fab?.setOnClickListener(clickListener)
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume")
        if ( context != null ) {
            val intent = Intent("PAGE_CHANGED")
            intent.putExtra("page", selectedPosition)
            intent.putExtra("gubun", "run")
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        Logger.d("onPause")
        if ( context != null ) {
            val intent = Intent("PAGE_CHANGED")
            intent.putExtra("page", selectedPosition)
            intent.putExtra("gubun", "cancel")
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")
        if ( context != null ) {
            if ( isVisibleToUser ) {
                val intent = Intent("PAGE_CHANGED")
                intent.putExtra("page", selectedPosition)
                intent.putExtra("gubun", "run")
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            } else {
                val intent = Intent("PAGE_CHANGED")
                intent.putExtra("page", selectedPosition)
                intent.putExtra("gubun", "cancel")
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            }
        }
    }

    override fun onRefresh() {
        Logger.e("onRefresh")

        EventBus.getDefault().post("refresh")

        refreshLayout.isRefreshing = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == ACTIVITY_REQUEST_CODE_LOGIN_FOLLOWING) {
//            if (resultCode == Activity.RESULT_OK) {
//                viewPagerContainer.setCurrentItem(TAB_FOLLOWING, false)
//            } else {
//                viewPagerContainer.setCurrentItem(TAB_HOME, false)
//            }
//        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun enableSwipeRefresh(enable: Boolean) {
        refreshLayout?.isEnabled = enable
    }

    private fun setTabLayoutFont() {
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
                        tabViewChild.typeface = Typeface.createFromAsset(context!!.assets, "fonts/notosanskr_bold.otf")
                    } else {
                        tabViewChild.typeface = Typeface.createFromAsset(context!!.assets, "fonts/notosanskr_regular.otf")
                    }
                }
            }
        }
    }

//    private fun handleFollowingClicked() {
//        var isLogin = AppPreferences.getLoginStatus(context!!)
//        if (isLogin) {
//            viewPagerContainer.setCurrentItem(TAB_FOLLOWING, false)
//        } else {
//            startActivityForResult(Intent(context!!, LoginActivity::class.java), ACTIVITY_REQUEST_CODE_LOGIN_FOLLOWING)
//        }
//    }

    inner class HomeTabPagerAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentStatePagerAdapter(fm) {
        private val mFragmentList: MutableList<androidx.fragment.app.Fragment> = mutableListOf()

        init {
            mFragmentList.add(PuddingHomeFragment())
            //mFragmentList.add(PuddingFollowingFragment())
            mFragmentList.add(PuddingHomeShoppingFragment())
            mFragmentList.add(PuddingFashionFragment())
            mFragmentList.add(PuddingBeautyFragment())
            mFragmentList.add(PuddingFoodFragment())
            mFragmentList.add(PuddingTravelFragment())
            mFragmentList.add(RankingFragment())
            mFragmentList.add(PuddingEventFragment())
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//            super.destroyItem(container, position, `object`)
        }

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int = mFragmentList.size

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "홈"
                1 -> return "쇼핑"
                2 -> return "패션"
                3 -> return "뷰티"
                4 -> return "푸드"
                5 -> return "여행"
                6 -> return "크루랭킹"
                7 -> return "이벤트"
            }

            return ""
        }
    }

    private val clickListener = View.OnClickListener {
        if (it != null) {
            when (it.id) {
                R.id.fab -> startActivity(Intent(it.context, CategoryActivity::class.java))
            }
        }
    }

    fun getPipStatus(): Boolean {
        return (activity as MainActivity)!!.getPipStatus()
    }
}