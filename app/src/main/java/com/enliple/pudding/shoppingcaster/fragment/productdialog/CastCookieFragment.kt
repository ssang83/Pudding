package com.enliple.pudding.shoppingcaster.fragment.productdialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.*
import com.enliple.pudding.commons.network.vo.API82
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.shoppingcaster.adapter.productdialog.CastCookieAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_cast_cookie.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * 방송중 상품 클릭시 표시되는 팝업의 방송중 유저들이 선물한 푸딩 리스트를 표현하기 위한 Fragment
 * @author hkcha
 * @since 2018.09.04
 */
class CastCookieFragment : androidx.fragment.app.Fragment() {
    companion object {
        const val BUNDLE_EXTRA_KEY_CAST_ID = "cast_id"
        const val BUNDLE_EXTRA_KEY_STREAM_KEY = "stream_key"

        private const val TAG = "CastCookieFragment"
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private var adapter: CastCookieAdapter? = null
    private var castId: String? = null
    private var streamKey = ""

    private var listener: OnTotalQuantityListener? = null

    fun setTotalQuantityListener(listener: OnTotalQuantityListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)

        castId = arguments?.getString(BUNDLE_EXTRA_KEY_CAST_ID)
        streamKey = arguments?.getString(BUNDLE_EXTRA_KEY_STREAM_KEY) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_cast_cookie, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = com.enliple.pudding.shoppingcaster.adapter.productdialog.CastCookieAdapter(view!!.context)
        var layoutManager = WrappedLinearLayoutManager(view!!.context)
        layoutManager.orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
        cookieList.setHasFixedSize(false)
        cookieList.isNestedScrollingEnabled = false
        cookieList.layoutManager = WrappedLinearLayoutManager(view!!.context)
        cookieList.adapter = adapter

        EventBus.getDefault().post(NetworkBus(NetworkApi.API82.name, streamKey, castId))
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusFastResponse) {
        val api82 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API82.toString(), streamKey, "")
        when (data.arg1) {
            api82 -> handleNetworkResult(data)
        }
    }

    private fun handleNetworkResult(data: NetworkBusFastResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            val response: API82 = Gson().fromJson(str, API82::class.java)
            if (response.nTotalCount > 0) {
                cookieList.visibility = View.VISIBLE
                emptyLayer.visibility = View.GONE

                adapter!!.setItems(response.data)
            } else {
                cookieList.visibility = View.GONE
                emptyLayer.visibility = View.VISIBLE
            }

            listener?.getTotalQuantity(response.nTotalCount.toString())
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }

    interface OnTotalQuantityListener {
        fun getTotalQuantity(quantity: String)
    }
}