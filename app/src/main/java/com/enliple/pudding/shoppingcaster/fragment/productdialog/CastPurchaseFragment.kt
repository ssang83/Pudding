package com.enliple.pudding.shoppingcaster.fragment.productdialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API83
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.shoppingcaster.adapter.productdialog.CastPurchaseAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_cast_purchase.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 방송중 상품 클릭시 표시되는 팝업의 방송중 상품 구매현황 리스트를 표현하기 위한 Fragment
 * @author hkcha
 * @since 2018.09.04
 */
class CastPurchaseFragment : androidx.fragment.app.Fragment() {
    companion object {
        private const val TAG = "CastPurchaseFragment"
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20

        const val BUNDLE_EXTRA_KEY_CAST_ID = "cast_id"
        const val BUNDLE_EXTRA_KEY_STREAM_KEY = "stream_key"
    }

    private var castId: String? = null
    private var adapter: CastPurchaseAdapter? = null
    private var listener: OnTotalQuantityListener? = null
    private var streamKey = ""

    fun setTotalQuantityListener(listener: OnTotalQuantityListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)

        castId = arguments?.getString(BUNDLE_EXTRA_KEY_CAST_ID)
        streamKey = arguments?.getString(BUNDLE_EXTRA_KEY_STREAM_KEY) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cast_purchase, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CastPurchaseAdapter(view!!.context)
        var layoutManager = WrappedLinearLayoutManager(view!!.context)
        layoutManager.orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
        list.setHasFixedSize(false)
        list.isNestedScrollingEnabled = false
        list.layoutManager = WrappedLinearLayoutManager(view!!.context)
        list.adapter = adapter

        val bus = NetworkBus(NetworkApi.API83.name, streamKey, castId)
        EventBus.getDefault().post(bus)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        var api83 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API83.toString(), streamKey, castId!!)
        when (data.arg1) {
            api83 -> handleNetworkResult(data)
        }
    }

    private fun handleNetworkResult(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            var str = DBManager.getInstance(context!!).get(data.arg1)
            var response: API83 = Gson().fromJson(str, API83::class.java)

            if (response.nTotalCount > 0) {
                list.visibility = View.VISIBLE
                emptyLayer.visibility = View.GONE

                adapter!!.setItems(response.data)
            } else {
                list.visibility = View.GONE
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