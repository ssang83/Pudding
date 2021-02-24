package com.enliple.pudding.fragment.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.R
import com.enliple.pudding.activity.BroadcastSettingActivity
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.home.ScheduleAdapter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API129
import com.enliple.pudding.commons.network.vo.API98
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.fragment.main.pudding.PuddingScheduleFragment
import com.enliple.pudding.shoppingcaster.activity.ShoppingCastActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_schedule.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Kim Joonsung on 2019-05-15.
 */
class ScheduleFragment : Fragment(), ScheduleAdapter.Listener {

    companion object {
        private const val PAGE_DATA_COUNT = 100
    }

    private lateinit var mScheduleAdapter: ScheduleAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var itemHolder: ScheduleAdapter.ScheduleHolder? = null

    private var mIsVisibleToUser = false
    private var isReload = false
    private var pageCount = 1
    private var isEndOfData = false
    private var alarmStatus = false
    private var streamKey = ""
    private var dbKey = ""
    private var alarmCount = 0
    private var alarmType = ""
    private var mDay = ""
    private var mToday = ""
    private var mDate = ""
    private var mMonth = ""
    private var currentDate = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewMain.addOnScrollListener(scrollListener)
        mLayoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
        recyclerViewMain.layoutManager = mLayoutManager

        mScheduleAdapter = ScheduleAdapter(context!!).apply {
            setListener(this@ScheduleFragment)
        }

        recyclerViewMain.adapter = mScheduleAdapter
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.d("setUserVisibleHint isVisibleToUser : $isVisibleToUser")
        mIsVisibleToUser = isVisibleToUser

        if(mIsVisibleToUser) {
            mDate = arguments?.getString("date")!!
            mMonth = mDate.split("-")[1]

            if (!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().register(this)

            refresh()
        }
    }

    override fun onPlay(item: API129.Schedule.ScheduleItem) {
        streamKey = item.stream_key
        NetworkBus(NetworkApi.API98.name, item.stream_key, AppPreferences.getUserId(context!!)!!)
                .let { EventBus.getDefault().post(it) }
    }

    override fun onAlarmClicked(isEnable: Boolean, item: API129.Schedule.ScheduleItem, holder: ScheduleAdapter.ScheduleHolder) {
        Logger.e("isEnable :: " + isEnable)
        alarmStatus = isEnable
        itemHolder = holder
        alarmCount = item.reservation_cnt.toInt()
        alarmType = item.my_alarm

        JSONObject().apply {
            put("streamKey", item.stream_key)
            put("status", if (isEnable) "Y" else "N")
        }.let {
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
            EventBus.getDefault().post(NetworkBus(NetworkApi.API125.name, body))
        }
    }

    override fun onBroadcasting(item: API129.Schedule.ScheduleItem, position: Int) {
        startActivity(Intent(context!!, ShoppingCastActivity::class.java).apply {
            putExtra(BroadcastSettingActivity.INFO_GUBUN, 2)
            putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(context!!)!!)
            putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(context!!)!!)
            putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "")
            putExtra("streamKey", item.stream_key)
        })
    }

    private fun refresh() {
        Logger.e("refresh")
        currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date(System.currentTimeMillis()))
        mToday = currentDate.split("-")[2]
        mDay = mDate.split("-")[2]
        if (mDay.startsWith("0")) {
            mDay = mDay.replace("0", "")
        }

        NetworkBus(NetworkApi.API129.name, mDate).let {
            EventBus.getDefault().post(it)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val api129 = "${NetworkApi.API129}?date=$mDate"
        val api125 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API125.toString(), AppPreferences.getUserId(context!!)!!, "")

        if((parentFragment as PuddingScheduleFragment).userVisibleHint) {
            if(data.arg1 == api129) {
                handleNetworkAPI129(data)
            } else if (data.arg1 == api125) {
                if ("ok" == data.arg2) {
                    if ( itemHolder != null ) {
                        if (alarmStatus) {
                            Logger.e("alarmStatus true")
                            LayoutInflater.from(context).inflate(R.layout.popup_schedule_alarm_on, null).let {
                                AppToast(context!!).showToastAtView(it, AppToast.DURATION_MILLISECONDS_FAST, AppToast.GRAVITY_MIDDLE)
                            }

                            if (alarmType == "Y") {
                                mScheduleAdapter.setAlarmCount(alarmCount.toString(), itemHolder)
                            } else {
                                ++alarmCount
                                mScheduleAdapter.setAlarmCount(alarmCount.toString(), itemHolder)
                            }
                        } else {
                            Logger.e("alarmStatus false")
                            LayoutInflater.from(context).inflate(R.layout.popup_schedule_alarm_off, null).let {
                                AppToast(context!!).showToastAtView(it, AppToast.DURATION_MILLISECONDS_FAST, AppToast.GRAVITY_MIDDLE)
                            }

                            if (alarmType == "Y") {
                                --alarmCount
                                if (alarmCount > 0) {
                                    mScheduleAdapter.setAlarmCount(alarmCount.toString(), itemHolder)
                                } else {
                                    mScheduleAdapter.setAlarmCount("0", itemHolder)
                                }
                            } else {
                                mScheduleAdapter.setAlarmCount(alarmCount.toString(), itemHolder)
                            }
                        }
                    }
                } else {
                    Logger.e("error : ${data.arg3}, ${data.arg4}")
                }
            } else if (data.arg1 == "GET/mui/live/${streamKey}?user=${AppPreferences.getUserId(context!!)}") {
                if ("ok" == data.arg2) {
                    dbKey = data.arg1
                    val response: API98 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API98::class.java)
                    if (response.nTotalCount > 0) {
                        Logger.e("URL : ${response.data[0].stream}")
                        startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                            putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.SCHEDULE_PLAYER)
                            putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_SHARE_KEY, dbKey)
                            putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, response.data[0].userId)
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
    }

    private fun handleNetworkAPI129(data:NetworkBusResponse) {
        if("ok" == data.arg2) {
            val response: API129 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API129::class.java)

            isEndOfData = response.data.size < PAGE_DATA_COUNT
            if (response.nTotalCount > 0) {
                recyclerViewMain.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE
                if (!isReload) {
                    mScheduleAdapter.setScheduleDay(mToday, mDay, mMonth)
                    mScheduleAdapter.setItems(response.data)
                } else {
                    mScheduleAdapter.addItems(response.data)
                }
            } else {
                recyclerViewMain.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
                textViewEmpty.text = "${mDay}일 편성표 내역이 없습니다"
            }
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            var lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition()
            var totalItemCount = mScheduleAdapter.itemCount - 1
            if ((lastVisibleItemPosition == totalItemCount) && !isEndOfData) {
                isReload = true
            }
        }
    }
}