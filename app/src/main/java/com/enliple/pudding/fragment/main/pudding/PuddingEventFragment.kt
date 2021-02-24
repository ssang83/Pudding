package com.enliple.pudding.fragment.main.pudding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.activity.EventDetailActivity
import com.enliple.pudding.activity.EventDetailTabActivity
import com.enliple.pudding.adapter.promotion.PromotionAdapter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API11
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_pudding_event.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2019-01-24.
 */
class PuddingEventFragment : androidx.fragment.app.Fragment(), PromotionAdapter.Listener {

    private lateinit var mAdapter: PromotionAdapter
    private var mIsVisibleToUser = false
    private var mIsFirst = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pudding_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.i("onViewCreated")

        EventBus.getDefault().register(this)

        mAdapter = PromotionAdapter().apply {
            setListener(this@PuddingEventFragment)
        }

        recyclerViewPromotions.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        Logger.e("PuddingEventFragment onResume")
        if (!mIsFirst) {
            refresh()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")

        mIsVisibleToUser = isVisibleToUser

        if (context != null && mIsVisibleToUser) {
            if (!mIsFirst) {
                refresh()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    override fun onItemClicked(data: API11.EventsLisItem) {
        if("Y" == data.is_tab) {
            startActivity(Intent(context!!, EventDetailTabActivity::class.java).apply {
                putExtra(EventDetailActivity.INTENT_KEY_EVENT_ID, data.ev_id)
                putExtra(EventDetailActivity.INTENT_KEY_EVENT_TYPE, data.ev_type)
            })
        } else {
            startActivity(Intent(context!!, EventDetailActivity::class.java).apply {
                putExtra(EventDetailActivity.INTENT_KEY_EVENT_ID, data.ev_id)
                putExtra(EventDetailActivity.INTENT_KEY_EVENT_TYPE, data.ev_type)
            })
        }
    }

    @Subscribe
    fun onMessageEvent(data: String) {
        if (data.startsWith("refresh")) {
            refresh()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API11.toString(), "", "")
        if (data.arg1 == key) {
            mIsFirst = true  // 최초 한번만 가져온다.
            handleNetworkResult(data)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            val response: API11 = Gson().fromJson(str, API11::class.java)
            if (response.events_cnt > 0) {
                setEmptyViewVisible(false)

                mAdapter.setItem(response.eventsList)
            } else {
                setEmptyViewVisible(true)
            }
        } else {
            val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }

    private fun setEmptyViewVisible(visible: Boolean) {
        if (visible) {
            layoutEmpty.visibility = View.VISIBLE
            recyclerViewPromotions.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            recyclerViewPromotions.visibility = View.VISIBLE
        }
    }

    private fun refresh() {
        if (mIsVisibleToUser && (parentFragment as PuddingHomeTabFragment)?.userVisibleHint) {
            NetworkBus(NetworkApi.API11.name).let {
                EventBus.getDefault().post(it)
            }
        }
    }
}