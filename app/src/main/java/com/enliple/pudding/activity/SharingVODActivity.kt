package com.enliple.pudding.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.ShareContentAdapter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API105
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_sharing_vod.*
import kotlinx.android.synthetic.main.fragment_shared_vod.recyclerViewShare
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2019-05-14.
 */
class SharingVODActivity : AppCompatActivity(), ShareContentAdapter.Listener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var mAdapter:ShareContentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sharing_vod)
        EventBus.getDefault().register(this)

        buttonClose.setOnClickListener { finish() }

        recyclerViewShare.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerViewShare.setHasFixedSize(false)

        mAdapter = ShareContentAdapter()
        mAdapter.setListener(this)
        mAdapter.setListType(false)
        recyclerViewShare.adapter = mAdapter

        val bus = NetworkBus(NetworkApi.API106.name)
        EventBus.getDefault().post(bus)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onItemClicked(item: API105.SharedItem) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API106 = NetworkHandler.getInstance(this)
                .getKey(NetworkApi.API106.toString(), AppPreferences.getUserId(this)!!, "")

        when (data.arg1) {
            API106 -> loadData(data)
        }
    }

    private fun loadData(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API105 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API105::class.java)

            if (response.nTotalCount > 0) {
                recyclerViewShare.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE

                mAdapter.setItem(response.data)
            } else {
                recyclerViewShare.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            }
        } else {
            val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }
}