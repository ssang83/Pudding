package com.enliple.pudding.fragment.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.AbsBaseTabFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.search.SearchScheduleAdapter
import com.enliple.pudding.bus.SearchResultBus
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API124
import com.enliple.pudding.commons.network.vo.API98
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_search_schedule.*
import kotlinx.android.synthetic.main.fragment_search_schedule.topBtn
import kotlinx.android.synthetic.main.fragment_search_vod.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2019-03-07.
 */
class SearchScheduleFragment : AbsBaseTabFragment(), SearchScheduleAdapter.Listener {

    companion object {
        const val BUNDLE_EXTRA_KEY_KEYWORD = "keyword"

        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
        private const val PAGE_DATA_COUNT = 100
    }

    private lateinit var mAdapter:SearchScheduleAdapter
    private lateinit var mLayoutManager:WrappedLinearLayoutManager
    private var itemHolder:SearchScheduleAdapter.SearchScheduleItemHolder? = null

    private var keyword = ""
    private var dataKey = ""
    private var isReload = false
    private var isEndOfData = false
    private var pageCount = 1
    private var mIsVisibleToUser = false
    private var isAPICall = false
    private var alarmStatus = false
    private var streamKey = ""
    private var dbKey = ""
    private var alarmCount = 0
    private var alarmType = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        if(arguments != null) {
            keyword = arguments!!.getString(BUNDLE_EXTRA_KEY_KEYWORD)
        }

        Logger.d("schedule keyword : $keyword")

        topBtn.setOnClickListener { recyclerViewSchedule.scrollToPosition(0) }

        recyclerViewSchedule.setHasFixedSize(false)
        recyclerViewSchedule.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerViewSchedule.addOnScrollListener(scrollListener)

        mLayoutManager = WrappedLinearLayoutManager(context!!).apply {
            orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
        }

        recyclerViewSchedule.layoutManager = mLayoutManager
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser

        if(mIsVisibleToUser && !isAPICall) {
            NetworkBus(NetworkApi.API124.name, keyword, pageCount.toString()).let {
                EventBus.getDefault().post(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onTabChanged(tabIndex: Int) {}

    override fun onItemClick(item: API124.ScheduleItem) {
        streamKey = item.stream_key

        NetworkBus(NetworkApi.API98.name, item.stream_key, AppPreferences.getUserId(context!!)!!)
                .let { EventBus.getDefault().post(it) }
    }

    override fun onAlarmSet(item: API124.ScheduleItem, status:Boolean, holder:SearchScheduleAdapter.SearchScheduleItemHolder) {
        alarmStatus = status
        itemHolder = holder
        alarmCount = item.reservation_cnt.toInt()
        alarmType = item.my_alarm

        JSONObject().apply {
            put("streamKey", item!!.stream_key)
            put("status", if(status) "Y" else "N")
        }.let {
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
            EventBus.getDefault().post(NetworkBus(NetworkApi.API125.name, body))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:NetworkBusResponse) {
        val api125 = NetworkHandler.getInstance(context!!)
                .getKey(NetworkApi.API125.toString(), AppPreferences.getUserId(context!!)!!, "")
        val api98 = NetworkHandler.getInstance(context!!)
                .getKey(NetworkApi.API98.toString(), streamKey, "")
        if(data.arg1.startsWith(NetworkApi.API124.toString())) {
            handleNetworkAPI124(data)
        } else if(data.arg1 == api125) {
            if("ok" == data.arg2) {
                if(alarmStatus) {
                    LayoutInflater.from(context).inflate(R.layout.popup_schedule_alarm_on, null).let {
                        AppToast(context!!).showToastAtView(it, AppToast.DURATION_MILLISECONDS_FAST, AppToast.GRAVITY_MIDDLE)
                    }

                    if(alarmType == "Y") {
                        mAdapter.setAlarmCount(alarmCount.toString(), itemHolder!!)
                    } else {
                        ++alarmCount
                        mAdapter.setAlarmCount(alarmCount.toString(), itemHolder!!)
                    }
                } else {
                    LayoutInflater.from(context).inflate(R.layout.popup_schedule_alarm_off, null).let {
                        AppToast(context!!).showToastAtView(it, AppToast.DURATION_MILLISECONDS_FAST, AppToast.GRAVITY_MIDDLE)
                    }


                    if(alarmType == "Y") {
                        --alarmCount
                        if(alarmCount > 0) {
                            mAdapter.setAlarmCount(alarmCount.toString(), itemHolder!!)
                        } else {
                            mAdapter.setAlarmCount("0", itemHolder!!)
                        }
                    } else {
                        mAdapter.setAlarmCount(alarmCount.toString(), itemHolder!!)
                    }
                }
            }
        } else if(data.arg1.startsWith(api98)) {
            if("ok" == data.arg2) {
                dbKey = data.arg1
                val response: API98 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API98::class.java)
                if(response.nTotalCount > 0 ) {
                    Logger.e("URL : ${response.data[0].stream}")
                    startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.SCHEDULE_PLAYER)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, response.data[0].userId)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_SHARE_KEY, dbKey)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, response.data[0].videoType)

                        setData(Uri.parse("vcommerce://shopping?url=${response.data[0].stream}"))
                    })
                } else {
                    AppToast(context!!).showToastMessage("방송 정보가 존재하지 않습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            } else {
                Logger.e("error : ${data.arg3}, ${data.arg4}")
            }
        }
    }

    private fun handleNetworkAPI124(data: NetworkBusResponse) {
        if("ok" == data.arg2) {
            val response:API124 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API124::class.java)

            dataKey = data.arg1

            isEndOfData = response.nTotalCount < PAGE_DATA_COUNT
            pageCount = response.pageCount.toInt()

            if(!isReload) {
                mAdapter = SearchScheduleAdapter(context!!, response.nFormationTotalCount).apply {
                    setListener(this@SearchScheduleFragment)
                    setKeyword(keyword)
                }

                recyclerViewSchedule.adapter = mAdapter

                mAdapter.setItems(response.data)
            } else {
                mAdapter.addItems(response.data)
            }

            EventBus.getDefault().post(SearchResultBus(0, response.nVodTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(1, response.nProductTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(2, response.nFormationTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(3, response.nTagTotalCount.toInt()))
            EventBus.getDefault().post(SearchResultBus(4, response.nUserTotalCount.toInt()))

            isAPICall = true
        }
    }

    private val scrollListener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (mIsVisibleToUser) {
                var lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
                val visibleCount = mLayoutManager.findFirstVisibleItemPosition()
                var totalItemCount = mAdapter.itemCount - 1

                if ( topBtn.visibility == View.GONE ) {
                    if ( visibleCount > 8 )
                        topBtn.visibility = View.VISIBLE
                }

                if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
                    isReload = true
                    ++pageCount

                    NetworkBus(NetworkApi.API124.name, keyword, pageCount.toString()).let {
                        EventBus.getDefault().post(it)
                    }
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            var state = newState
            if ( state == RecyclerView.SCROLL_STATE_IDLE ) {
                Handler().postDelayed({
                    if ( state == RecyclerView.SCROLL_STATE_IDLE )
                        topBtn.visibility = View.GONE
                }, 1500)
            }
        }
    }
}