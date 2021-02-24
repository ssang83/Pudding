package com.enliple.pudding.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.notice.NoticeListAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API110
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_notice_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 공지사항 리스트 Activity
 * @author hkcha
 * @since 2018.08.30
 */
class NoticeListActivity : AbsBaseActivity(), NoticeListAdapter.Listener {

    companion object {
        private const val TAG = "NoticeListActivity"
        private const val RECYCLER_VIEW_ITEM_CACHE_COUNT = 20
    }

    private lateinit var mAdapter: NoticeListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_list)
        EventBus.getDefault().register(this)

        recyclerViewNoticeList.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_COUNT)
        recyclerViewNoticeList.setHasFixedSize(true)
        mAdapter = NoticeListAdapter().apply {
            setListener(this@NoticeListActivity)
        }
        recyclerViewNoticeList.adapter = mAdapter

        buttonBack.setOnClickListener { onBackPressed() }

        val bus = NetworkBus(NetworkApi.API110.name)
        EventBus.getDefault().post(bus)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onNoticeListItemClicked(data: API110.NoticeItem) {
        var nextIntent = Intent(this, NoticeDetailActivity::class.java).apply {
            // TODO : API 가 형성되면 수정
            putExtra(NoticeDetailActivity.INTENT_EXTRA_KEY_CONTENT_URL, data.content)
            putExtra(NoticeDetailActivity.INTENT_EXTRA_KEY_SUBJECT, data.subject)
            putExtra(NoticeDetailActivity.INTENT_EXTRA_KEY_NOTICE_ID, data.id)
            putExtra(NoticeDetailActivity.INTENT_EXTRA_KEY_REG_DATE, data.reg_date)
        }

        startActivity(nextIntent)
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        if (data.arg1 == NetworkApi.API110.toString()) {
            handleNetworkAPI110(data)
        }
    }

    private fun handleNetworkAPI110(data: NetworkBusResponse) {
        progressBar.visibility = View.GONE
        if ("ok" == data.arg2) {
            val response: API110 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API110::class.java)

            mAdapter.setItem(response.data)
        } else {
            val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }
}