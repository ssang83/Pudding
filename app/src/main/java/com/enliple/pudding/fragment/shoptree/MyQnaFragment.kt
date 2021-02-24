package com.enliple.pudding.fragment.shoptree

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.adapter.shoptree.QnaAdapter
import com.enliple.pudding.bus.QnABus
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API91
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_my_qna.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-12-05.
 */
class MyQnaFragment : androidx.fragment.app.Fragment(), QnaAdapter.AdapterListener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var mAdapter: QnaAdapter

    var isLoadMore: Boolean = false
    var productId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_qna, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventBus.getDefault().register(this)

        if (arguments != null) {
            productId = arguments!!.getString("idx")
        }

        recyclerVeiwMyQnA.setHasFixedSize(false)
        recyclerVeiwMyQnA.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)

        mAdapter = QnaAdapter(activity)
        mAdapter.setAdapterListener(this@MyQnaFragment)
        recyclerVeiwMyQnA.adapter = mAdapter

        var bus = NetworkBus(NetworkApi.API91_1.name, productId)
        EventBus.getDefault().post(bus)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onLoadMoreClicked() {
        isLoadMore = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API91_1 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API91_1.toString(), productId, "")

        when (data.arg1) {
            API91_1 -> loadData(data)
        }
    }

    private fun loadData(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(activity).get(data.arg1)
            val response = Gson().fromJson(str, API91::class.java)

            if (response.nTotalCount > 0) {
                recyclerVeiwMyQnA.visibility = View.VISIBLE
                textViewEmpty.visibility = View.GONE

                mAdapter.setItems(response.data)
                mAdapter.setEndOfData(isLoadMore)
            } else {
                recyclerVeiwMyQnA.visibility = View.GONE
                textViewEmpty.visibility = View.VISIBLE
            }

            EventBus.getDefault().post(QnABus(1, response.nTotalCount))
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

//            AppToast(context!!).showToastMessage(errorResult.message,
//                    AppToast.DURATION_MILLISECONDS_DEFAULT,
//                    AppToast.GRAVITY_BOTTOM)
        }
    }

    fun refreshData() {
        isLoadMore = false
        var bus = NetworkBus(NetworkApi.API91_1.name, productId)
        EventBus.getDefault().post(bus)
    }
}