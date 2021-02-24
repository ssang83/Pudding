package com.enliple.pudding.fragment.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.BroadcastSettingActivity
import com.enliple.pudding.adapter.home.ScheduleListAdapter
import com.enliple.pudding.bus.ReservationBus
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API130
import com.enliple.pudding.shoppingcaster.activity.ShoppingCastActivity
import com.enliple.pudding.widget.AppAlertDialog
import com.enliple.pudding.widget.ScheduleCancelDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_schedule_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-12-13.
 */
class ScheduleListFragment : AbsBaseFragment(), ScheduleListAdapter.Listener {
    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private var reservationPos = -1
    private var streamKey = ""
    private lateinit var mAdapter: ScheduleListAdapter
    private lateinit var dialog:ScheduleCancelDialog
    private var mTotalCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        Logger.d("onViewCreated")

        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context!!, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)

        mAdapter = ScheduleListAdapter(context!!)
        mAdapter.setListener(this@ScheduleListFragment)
        recyclerView.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        NetworkBus(NetworkApi.API130.name).let { EventBus.getDefault().post(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:NetworkBusResponse) {
        val api130 = NetworkHandler.getInstance(context!!)
                .getKey(NetworkApi.API130.toString(), AppPreferences.getUserId(context!!)!!, "")
        val api133 = "${NetworkHandler.getInstance(context!!)
                .getKey(NetworkApi.API133.toString(), AppPreferences.getUserId(context!!)!!, "")}?streamKey=${streamKey}"

        when(data.arg1) {
            api130 -> {
                if("ok" == data.arg2) {
                    val response:API130 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API130::class.java)

                    mTotalCount = response.nTotalCount
                    if(response.nTotalCount > 0) {
                        recyclerView.visibility = View.VISIBLE
                        layoutEmpty.visibility = View.GONE

                        mAdapter.setItems(response.data)
                    } else {
                        recyclerView.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
                    }

                    EventBus.getDefault().post(ReservationBus(0, response.nTotalCount))
                }
            }

            api133 -> {
                if("ok" == data.arg2) {
                    mAdapter.deleteReservation(reservationPos)
                    dialog.dismiss()

                    EventBus.getDefault().post(ReservationBus(0, mTotalCount.dec()))
                }
            }
        }
    }

    override fun onItemClicked(item: API130.ReservationItem, position: Int) {

    }

    override fun onGoBroadCast(item: API130.ReservationItem) {
        streamKey = item!!.stream_key

        startActivity(Intent(context!!, ShoppingCastActivity::class.java).apply {
            putExtra(BroadcastSettingActivity.INFO_GUBUN, 2)
            putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, AppPreferences.getUserId(context!!)!!)
            putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(context!!)!!)
            putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_CHAT_ROOM_ID, "")
            putExtra("streamKey", streamKey)
        })
    }

    override fun onReservationCancel(item: API130.ReservationItem, position: Int) {
        reservationPos = position
        streamKey = item!!.stream_key

        dialog = ScheduleCancelDialog(context!!, object : ScheduleCancelDialog.Listener {
            override fun onConfirm(content: String) {
                Logger.d("############## content : $content")
                NetworkBus(NetworkApi.API133.name, streamKey).let {EventBus.getDefault().post(it) }
            }
        })

        dialog.show()
    }

    override fun onReservationModify(item: API130.ReservationItem, position: Int) {
        streamKey = item!!.stream_key

        startActivity(Intent(context, BroadcastSettingActivity::class.java).apply {
            putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_TAB, BroadcastSettingActivity.SCHEDULE_LIST)
            putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_SCHEDULE_POSITION, position)
            putExtra(BroadcastSettingActivity.INTENT_EXTRA_KEY_SCHEDULE_STREAM_KEY, streamKey)
        })
    }
}