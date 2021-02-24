package com.enliple.pudding.fragment.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseFragment
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
import kotlinx.android.synthetic.main.fragment_shared_vod.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-10-25.
 */
class SharedVODFragment : AbsBaseFragment(), ShareContentAdapter.Listener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var mAdapter: ShareContentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_shared_vod, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        recyclerViewShare.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerViewShare.setHasFixedSize(false)

        mAdapter = ShareContentAdapter()
        mAdapter.setListener(this@SharedVODFragment)
        mAdapter.setListType(true)
        recyclerViewShare.adapter = mAdapter

        val bus = NetworkBus(NetworkApi.API105.name)
        EventBus.getDefault().post(bus)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onItemClicked(item: API105.SharedItem) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API105 = NetworkHandler.getInstance(context!!)
                .getKey(NetworkApi.API105.toString(), AppPreferences.getUserId(context!!)!!, "")

        when (data.arg1) {
            API105 -> loadData(data)
        }
    }

    private fun loadData(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API105 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API105::class.java)

            if (response.nTotalCount > 0) {
                recyclerViewShare.visibility = View.VISIBLE
                textViewEmpty.visibility = View.GONE

                mAdapter.setItem(response.data)
            } else {
                recyclerViewShare.visibility = View.GONE
                textViewEmpty.visibility = View.VISIBLE
            }
        } else {
            val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }
}