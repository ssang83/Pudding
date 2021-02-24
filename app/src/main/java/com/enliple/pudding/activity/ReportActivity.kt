package com.enliple.pudding.activity

import android.os.Bundle
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.ReportAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API6
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_report.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Created by Kim Joonsung on 2018-10-29.
 */
class ReportActivity : AbsBaseActivity() {

    companion object {
        private const val TAG = "ReportActivity"

        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var mAdapter: ReportAdapter

    private var listPage = 1
    private var totalPageCount = 1

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        buttonClose.setOnClickListener(clickListener)

        recyclerVeiwReport.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerVeiwReport.setHasFixedSize(false)

        mAdapter = ReportAdapter()
        recyclerVeiwReport.adapter = mAdapter

        var bus = NetworkBus(NetworkApi.API6.name, AppPreferences.getUserId(this@ReportActivity))
        EventBus.getDefault().post(bus)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onBackPressed() {
        finish()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe()
    fun onMessageEvent(data: NetworkBusResponse) {
        var key = NetworkHandler.getInstance(this@ReportActivity)
                .getKey(NetworkApi.API6.toString(), AppPreferences.getUserId(this@ReportActivity)!!, "")

        if (data.arg1.startsWith(key)) {
            handleNetworkResult(data)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(this@ReportActivity).get(data.arg1)
            var respone: API6 = Gson().fromJson(str, API6::class.java)

            totalPageCount = respone.nTotalCount
            listPage = respone.pageCount

            if (totalPageCount > 0) {
                recyclerVeiwReport.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE

                mAdapter.setItems(respone.data)
            } else {
                recyclerVeiwReport.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            }
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()
        }
    }
}