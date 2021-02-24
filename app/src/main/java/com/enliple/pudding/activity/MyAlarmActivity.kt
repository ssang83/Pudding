package com.enliple.pudding.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.AlarmAdapter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API140
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_my_alarm.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2019-03-11.
 */
class MyAlarmActivity : AppCompatActivity(), AlarmAdapter.Listener {

    private lateinit var mAdapter:AlarmAdapter
    private var itemPos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_alarm)
        EventBus.getDefault().register(this)

        buttonBack.setOnClickListener{finish()}

        recyclerViewAlarm.setHasFixedSize(true)

        mAdapter = AlarmAdapter().apply {
            setListener(this@MyAlarmActivity)
        }

        recyclerViewAlarm.adapter = mAdapter

        NetworkBus(NetworkApi.API140.name).let { EventBus.getDefault().post(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onItemDelete(item: API140.NoticeItem, position:Int) {
        itemPos = position
        NetworkBus(NetworkApi.API141.name, item.am_idx).let { EventBus.getDefault().post(it) }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:NetworkBusResponse) {
        val api140 = NetworkHandler.getInstance(this)
                .getKey(NetworkApi.API140.toString(), AppPreferences.getUserId(this)!!, "")

        val api141 = NetworkHandler.getInstance(this)
                .getKey(NetworkApi.API140.toString(), AppPreferences.getUserId(this)!!, "")

        if(data.arg1 == api140) {
            handleNetworkAPI140(data)
        } else if(data.arg1.startsWith(api141)) {
            if("ok" == data.arg2) {
                mAdapter.deleteItem(itemPos)
            } else {
                Logger.e("error : ${data.arg3}, ${data.arg4}")
            }
        }
    }

    private fun handleNetworkAPI140(data: NetworkBusResponse) {
        if("ok" == data.arg2) {
            val response: API140 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API140::class.java)

            mAdapter.setItems(response.data)
        } else {
            Logger.e("error : ${data.arg3}, ${data.arg4}")
        }
    }
}