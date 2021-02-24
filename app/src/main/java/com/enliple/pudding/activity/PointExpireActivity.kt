package com.enliple.pudding.activity

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.PointExpireAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API103
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_point_expire.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2019-01-03.
 */
class PointExpireActivity : AbsBaseActivity() {

    private lateinit var mAdapter: PointExpireAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_expire)
        EventBus.getDefault().register(this)

        recyclerViewPointExpire.setItemViewCacheSize(20)

        mAdapter = PointExpireAdapter()
        recyclerViewPointExpire.adapter = mAdapter

        setEmptyText(getString(R.string.msg_my_shopping_point_history_empty),
                getString(R.string.msg_my_shopping_menu_point))

        val bus = NetworkBus(NetworkApi.API103.name)
        EventBus.getDefault().post(bus)

        buttonBack.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API103 = NetworkHandler.getInstance(this)
                .getKey(NetworkApi.API103.toString(), AppPreferences.getUserId(this)!!, "")

        when (data.arg1) {
            API103 -> handleNetworkAPI103(data)
        }
    }

    private fun handleNetworkAPI103(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val response: API103 = Gson().fromJson(DBManager.getInstance(this).get(data.arg1), API103::class.java)

            if (response.nTotalCount > 0) {
                recyclerViewPointExpire.visibility = View.VISIBLE
                textViewEmpty.visibility = View.GONE

                mAdapter.setItem(response.data)
            } else {
                recyclerViewPointExpire.visibility = View.GONE
                textViewEmpty.visibility = View.VISIBLE
            }
        } else {
            val error: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $error")
        }
    }

    /**
     * 포인트가 존재하지 않을 때 출력되는 문구를 설정
     * @param emptyText
     * @param colorSpannableText
     */
    private fun setEmptyText(emptyText: String, colorSpannableText: String) {
        val sp = SpannableStringBuilder(emptyText)
        sp.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.main_color)),
                0, colorSpannableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        sp.setSpan(StyleSpan(Typeface.BOLD), 0, colorSpannableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textViewEmpty.setText(sp)
    }
}