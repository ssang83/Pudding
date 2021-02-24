package com.enliple.pudding.activity

import android.os.Bundle
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.RestrictionAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API107
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_restriction.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-10-29.
 */
class RestrictionActivity : AbsBaseActivity() {

    companion object {
        private const val TAG = "RestrictionActivity"

        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var mAdapter: RestrictionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restriction)
        EventBus.getDefault().register(this)

        buttonClose.setOnClickListener(clickListener)

        recyclerVeiwRestriction.setHasFixedSize(false)
        recyclerVeiwRestriction.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)

        mAdapter = RestrictionAdapter()
        recyclerVeiwRestriction.adapter = mAdapter

        val bus = NetworkBus(NetworkApi.API107.name)
        EventBus.getDefault().post(bus)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API107 = NetworkHandler.getInstance(this)
                .getKey(NetworkApi.API107.toString(), AppPreferences.getUserId(this)!!, "")

        when (data.arg1) {
            API107 -> loadData(data)
        }
    }

    private fun loadData(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API107 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API107::class.java)

            if (response.nTotalCount > 0) {
                recyclerVeiwRestriction.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE

                mAdapter.setItems(response.data)
            } else {
                recyclerVeiwRestriction.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            }
        } else {
            val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()
        }
    }
}