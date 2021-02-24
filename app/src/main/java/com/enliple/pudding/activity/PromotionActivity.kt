package com.enliple.pudding.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.promotion.PromotionAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API11
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_promotion.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Created by Kim Joonsung on 2018-10-22.
 */
class PromotionActivity : AbsBaseActivity(), PromotionAdapter.Listener {

    companion object {
        private const val TAG = "PromotionActivity"
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var mAdapter: PromotionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promotion)
        EventBus.getDefault().register(this)

        buttonClose.setOnClickListener(clickListener)
        imageViewPick.setOnClickListener(clickListener)

        recyclerViewPromotions.setHasFixedSize(false)
        recyclerViewPromotions.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerViewPromotions.isNestedScrollingEnabled = false

        mAdapter = PromotionAdapter()
        mAdapter.setListener(this@PromotionActivity)

        recyclerViewPromotions.adapter = mAdapter

        var bus = NetworkBus(NetworkApi.API11.name)
        EventBus.getDefault().post(bus)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onItemClicked(data: API11.EventsLisItem) {
        startActivity(Intent(this@PromotionActivity, EventDetailActivity::class.java).apply {
            putExtra(EventDetailActivity.INTENT_KEY_EVENT_ID, data.ev_id)
            putExtra(EventDetailActivity.INTENT_KEY_EVENT_TYPE, data.ev_type)
        })
    }

    @Subscribe()
    fun onMessageEvent(data: NetworkBusResponse) {
        var key = NetworkHandler.getInstance(this@PromotionActivity)
                .getKey(NetworkApi.API11.toString(), "", "")

        if (data.arg1.startsWith(key)) {
            handleNetworkResult(data)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(this@PromotionActivity).get(data.arg1)
            var response: API11 = Gson().fromJson(str, API11::class.java)

            if (response.events_cnt > 0) {
                setEmptyViewVisible(false)

                mAdapter.setItem(response.eventsList)
            } else {
                setEmptyViewVisible(true)
            }
        } else {
            // error 처리
        }
    }

    private fun setEmptyViewVisible(visible: Boolean) {
        if (visible) {
            textViewEmpty.visibility = View.VISIBLE
            recyclerViewPromotions.visibility = View.GONE
        } else {
            textViewEmpty.visibility = View.GONE
            recyclerViewPromotions.visibility = View.VISIBLE
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()

            R.id.imageViewPick -> {
                val intent = Intent(this@PromotionActivity, com.enliple.pudding.shoppingcaster.activity.LiveProductActivity::class.java)
                intent.putExtra("from_promotion", true)

                startActivity(intent)
            }
        }
    }
}