package com.enliple.pudding.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.home.ScheduleAlarmListAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API131
import com.enliple.pudding.commons.network.vo.API98
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.widget.AppAlertDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_schedule_alarm_list.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 편성표 내역
 */
class ScheduleAlarmListActivity : AbsBaseActivity(), ScheduleAlarmListAdapter.Listener {

    private lateinit var mAdapter: ScheduleAlarmListAdapter
    private lateinit var dialog: AppAlertDialog
    private var alarmDelPos = -1
    private var streamKey = ""
    private var dbKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_schedule_alarm_list)

        EventBus.getDefault().register(this)

        buttonBack.setOnClickListener(View.OnClickListener { finish() })

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)

        mAdapter = ScheduleAlarmListAdapter(this)
        mAdapter.setListener(this@ScheduleAlarmListActivity)
        recyclerView.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        NetworkBus(NetworkApi.API131.name).let {
            EventBus.getDefault().post(it)
        }
        Logger.d("onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onItemClicked(item: API131.WatchItem, position: Int) {
        streamKey = item.stream_key
        NetworkBus(NetworkApi.API98.name, item.stream_key, AppPreferences.getUserId(this)!!)
                .let { EventBus.getDefault().post(it) }
    }

    override fun onAlarmDel(item: API131.WatchItem, position: Int) {
        alarmDelPos = position

        dialog = AppAlertDialog(this)
        dialog.setTitle("시청예약")
        dialog.setMessage("선택한 알람을 삭제 할까요?")
        dialog.setLeftButton("취소", View.OnClickListener {
            dialog.dismiss()
        })
        dialog.setRightButton("확인", View.OnClickListener {
            setScheduleReservation(item)
        })

        dialog.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val api131 = NetworkHandler.getInstance(this).getKey(NetworkApi.API131.toString(), AppPreferences.getUserId(this)!!, "")
        val api98 = NetworkHandler.getInstance(this).getKey(NetworkApi.API98.toString(), streamKey, "")

        if (data.arg1 == api131) {
            if ("ok" == data.arg2) {
                val response: API131 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API131::class.java)
                if (response.nTotalCount > 0) {
                    layoutEmpty?.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    mAdapter.setItems(response.data)
                } else {
                    layoutEmpty?.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }
        } else if (data.arg1.startsWith(api98)) {
            if ("ok" == data.arg2) {
                dbKey = data.arg1
                val response: API98 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API98::class.java)
                if (response.nTotalCount > 0) {
                    Logger.e("URL : ${response.data[0].stream}")
                    startActivity(Intent(this, ShoppingPlayerActivity::class.java).apply {
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG, AppConstants.SCHEDULE_PLAYER)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, response.data[0].userId)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, response.data[0].videoType)
                        putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_SHARE_KEY, dbKey)

                        setData(Uri.parse("vcommerce://shopping?url=${response.data[0].stream}"))
                    })
                } else {
                    AppToast(this).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            } else {
                Logger.e("error : ${data.arg3}, ${data.arg4}")
            }
        }
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    /**
     * 편성표 메인리스트의 시청예약API와 시청예약리스트의 알림해제API가 동일하게 때문에
     * network key가 동일하여 이벤트버스가 두 화면에서 동시에 처리되기 때문에 시청예약화면에서는 별도의 Retrofit API 만들어서 사용함.
     */
    private fun setScheduleReservation(item: API131.WatchItem) {
        JSONObject().apply {
            put("streamKey", item.stream_key)
            put("status", "N")
        }.let {
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), it.toString())
            val task = ShopTreeAsyncTask(this)
            task.postSchedule(body, { result, obj ->
                try {
                    mAdapter.deleteAlarm(alarmDelPos)
                    dialog.dismiss()

                    if (mAdapter.itemCount == 0) {
                        layoutEmpty?.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                } catch (e:Exception) {
                    Logger.p(e)
                }
            })
        }
    }
}