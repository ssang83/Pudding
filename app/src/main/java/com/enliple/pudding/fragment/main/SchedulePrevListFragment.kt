package com.enliple.pudding.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.enliple.pudding.R
import com.enliple.pudding.adapter.home.SchedulePrevListAdapter
import com.enliple.pudding.bus.ReservationBus
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API130
import com.enliple.pudding.widget.AppAlertDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_schedule_list.*
import kotlinx.android.synthetic.main.fragment_schedule_prev_list.*
import kotlinx.android.synthetic.main.fragment_schedule_prev_list.layoutEmpty
import kotlinx.android.synthetic.main.fragment_schedule_prev_list.recyclerView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2019-05-09.
 */
class SchedulePrevListFragment : Fragment(), SchedulePrevListAdapter.Listener {
    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var mAdapter:SchedulePrevListAdapter
    private lateinit var dialog: AppAlertDialog
    private var streamKey = ""
    private var mPosition = -1
    private var mTotalCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_schedule_prev_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context!!, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)

        mAdapter = SchedulePrevListAdapter().apply {
            setListener(this@SchedulePrevListFragment)
        }

        recyclerView.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        refresh("GET/formation/${AppPreferences.getUserId(context!!)!!}")
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onDelete(item: API130.PreviousItem, position: Int) {
        mPosition = position
        streamKey = item.stream_key

        dialog = AppAlertDialog(context!!)
        dialog.setTitle("방송 예약 삭제")
        dialog.setMessage("선택한 방송 이전 예약을 삭제 할까요?")
        dialog.setLeftButton("취소", View.OnClickListener {
            dialog.dismiss()
        })
        dialog.setRightButton("확인", View.OnClickListener {
            NetworkBus(NetworkApi.API133.name, streamKey).let {
                EventBus.getDefault().post(it)
            }
        })

        dialog.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val api130 = NetworkHandler.getInstance(context!!)
                .getKey(NetworkApi.API130.toString(), AppPreferences.getUserId(context!!)!!, "")
        val api133 = "${NetworkHandler.getInstance(context!!)
                .getKey(NetworkApi.API133.toString(), AppPreferences.getUserId(context!!)!!, "")}?streamKey=${streamKey}"

        when(data.arg1) {
            api130 -> {
                if("ok" == data.arg2) {
                    val response: API130 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API130::class.java)

                    mTotalCount = response.nTotalLastCount
                    if(response.nTotalLastCount > 0) {
                        recyclerView.visibility = View.VISIBLE
                        layoutEmpty.visibility = View.GONE

                        mAdapter.setItems(response.lastData)
                    } else {
                        recyclerView.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
                    }

                    EventBus.getDefault().post(ReservationBus(1, response.nTotalLastCount))
                }
            }

            api133 -> {
                if("ok" == data.arg2) {
                    mAdapter.deleteReservation(mPosition)
                    dialog.dismiss()

                    EventBus.getDefault().post(ReservationBus(1, mTotalCount.dec()))
                }
            }
        }
    }

    private fun refresh(dbKey:String) {
        val json = DBManager.getInstance(context!!).get(dbKey)
        if(json.isNotEmpty()) {
            val response: API130 = Gson().fromJson(json, API130::class.java)
            mTotalCount = response.nTotalLastCount
            if(response.nTotalLastCount > 0) {
                recyclerView.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE

                mAdapter.setItems(response.lastData)
            } else {
                recyclerView.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            }

            EventBus.getDefault().post(ReservationBus(1, response.nTotalLastCount))
        } else {
            NetworkBus(NetworkApi.API130.name).let { EventBus.getDefault().post(it) }
        }
    }
}