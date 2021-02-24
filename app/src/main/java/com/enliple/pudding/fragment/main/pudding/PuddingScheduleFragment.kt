package com.enliple.pudding.fragment.main.pudding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.enliple.pudding.R
import com.enliple.pudding.adapter.home.ScheduleDaysAdapter
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API129
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.fragment.main.ScheduleFragment
import com.enliple.pudding.widget.ScheduleMenuDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_pudding_schedule.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by jhs on 2019-03-07.
 * 라이브 방송 예약 Main 화면
 */
class PuddingScheduleFragment : Fragment(), ScheduleDaysAdapter.Listener, SwipeRefreshLayout.OnRefreshListener {

    private lateinit var mDayAdapter: ScheduleDaysAdapter
    private lateinit var mPagerAdapter: SchedulePagerAdapter
    private lateinit var mDayLayoutManager: LinearLayoutManager

    private var mIsVisibleToUser = false
    private var date = ""
    private var currentDate = ""
    private var day = ""
    private var today = ""
    private var dbKey = ""
    private var mIsFirst = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        mDayLayoutManager = LinearLayoutManager(context!!, RecyclerView.HORIZONTAL, false)
        daysRecyclerView.layoutManager = mDayLayoutManager
        mDayAdapter = ScheduleDaysAdapter().apply {
            setListener(this@PuddingScheduleFragment)
        }
        daysRecyclerView.adapter = mDayAdapter

        mPagerAdapter = SchedulePagerAdapter(childFragmentManager)
        viewPagerContainer.addOnPageChangeListener(object:ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            // viewpager scroll 중에 refresh 안되도록...
            override fun onPageScrollStateChanged(state: Int) {
                if(state == ViewPager.SCROLL_STATE_IDLE) {
                    enableSwipeRefresh(true)
                } else {
                    enableSwipeRefresh(false)
                }
            }

            override fun onPageSelected(position: Int) {
                Logger.d("position $position")
                date = mDayAdapter.getData(position)!!.date
                mDayAdapter.setDate(position)

                scrollToCenter(position)
                viewPagerContainer.setCurrentItem(position, false)
            }
        })

        viewPagerContainer.adapter = mPagerAdapter

        fab.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                ScheduleMenuDialog().show(fragmentManager!!, "dialog")

//                if ("Y" == isReservation) {
//                    ScheduleMenuDialog().show(fragmentManager, "dialog")
//                } else {
//                    startActivity(Intent(context, BroadcastSettingActivity::class.java).apply {
//                        putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.TAB_RESERVE_BROADCAST)
//                    })
//                }
            }
        })

        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        )
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume")

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        if (mIsVisibleToUser && context != null) {
            refresh()
        }
    }

    override fun onStop() {
        super.onStop()
        // 시청예약 화면에서도 알람삭제 API 를 사용하기 때문에 event가 겹치는 관계로 등록해제 후 이동한다.
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    override fun onDateClicked(dateItem: API129.ShowDateItem, view: View, position: Int) {
        date = dateItem.date
        scrollToCenter(view)
        viewPagerContainer.setCurrentItem(position, false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")

        mIsVisibleToUser = isVisibleToUser

        if (context != null && mIsVisibleToUser) {
            refresh()
        }
    }

    override fun onRefresh() {
        Logger.e("onRefresh")

        NetworkBus(NetworkApi.API129.name, date).let { EventBus.getDefault().post(it) }

        refreshLayout.isRefreshing = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if (data.arg1 == "${NetworkApi.API129}?date=$date") {
            handleNetworkAPI129(data)
        }
    }

    @Subscribe
    fun onMessageEvent(data: String) {
        if (data.startsWith("refresh")) {
            refresh()
        }
    }

    private fun enableSwipeRefresh(enable:Boolean) {
        refreshLayout.isEnabled = enable
    }

    // api 갱신은 여기서 하자
    private fun refresh() {
        if (mIsVisibleToUser) {
            Logger.e("refresh")

            if (date.isEmpty()) {
                currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis()))
                date = currentDate
                day = currentDate.split("-")[2]
                today = day
                if (day.startsWith("0")) {
                    day = day.replace("0", "")
                }
            }

            NetworkBus(NetworkApi.API129.name, date).let {
                EventBus.getDefault().post(it)
            }
        }
    }

    private fun handleNetworkAPI129(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            dbKey = data.arg1
            val response: API129 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API129::class.java)

            if (!mIsFirst) {
                mIsFirst = true
                mDayAdapter.setItems(response.show_date)
                mDayAdapter.setToday()
                mDayLayoutManager.scrollToPosition(0)

                mPagerAdapter.setData(response.show_date)
                viewPagerContainer.setCurrentItem(ScheduleDaysAdapter.TODAY_POSITION, false)
                viewPagerContainer.visibility = View.VISIBLE    // speed issue
            } else {
                mDayAdapter.setItems(response.show_date)
            }
        } else {
            val error = JSONObject(data.arg4)
            AppToast(context!!).showToastMessage(error["message"].toString(),
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun scrollToCenter(view: View) {
        val itemToScroll = daysRecyclerView.getChildAdapterPosition(view)
        val centerOfScreen = daysRecyclerView.width / 2 - view.width / 2
        mDayLayoutManager.scrollToPositionWithOffset(itemToScroll, centerOfScreen)
    }

    private fun scrollToCenter(position: Int) {
        val centerOfScreen = daysRecyclerView.width / 2 - (Utils.ConvertDpToPx(context!!, 52) / 2)
        mDayLayoutManager.scrollToPositionWithOffset(position, centerOfScreen)
    }

    private inner class SchedulePagerAdapter : FragmentStatePagerAdapter {
        private var mCount = 0
        private var mFragmentManager:FragmentManager
        private var mItems:List<API129.ShowDateItem> = mutableListOf()

        constructor(fm:FragmentManager) : super(fm) {
            mFragmentManager = fm
        }

        fun setData(items:List<API129.ShowDateItem>) {
            this.mCount = items.size
            this.mItems = items
            notifyDataSetChanged()
        }

        override fun getItem(position: Int):Fragment = ScheduleFragment().apply {
            arguments = Bundle().apply {
                putString("date", mItems[position].date)
            }
        }

        override fun getCount() = mCount

        override fun getPageTitle(position: Int): CharSequence? = ""

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            Logger.d("position : $position")
            var fragment = getItem(position)
            var name = makeFragmentName(container.id, position)

            var transaction = mFragmentManager.beginTransaction()
            transaction.add(container.id, fragment, name)
            transaction.commitAllowingStateLoss()

            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            var fragment = mFragmentManager.findFragmentByTag(makeFragmentName(container.id, position))
            if (fragment != null) {
                var transaction = mFragmentManager.beginTransaction()
                transaction.remove(fragment)
                transaction.commitAllowingStateLoss()
            }
        }

        private fun makeFragmentName(viewId: Int, id: Int): String {
            return "android:switcher:$viewId:$id"
        }
    }
}