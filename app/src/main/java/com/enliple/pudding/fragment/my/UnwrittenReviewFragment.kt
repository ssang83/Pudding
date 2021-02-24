package com.enliple.pudding.fragment.my

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.*
import com.enliple.pudding.adapter.my.UnwrittenReviewAdapter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API68
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.toast.AppToast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_unwritten_review.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-12-13.
 */
class UnwrittenReviewFragment : AbsBaseFragment(), UnwrittenReviewAdapter.Listener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
    }

    private lateinit var mAdapter: UnwrittenReviewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_unwritten_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        recyclerViewReview.setHasFixedSize(true)
        recyclerViewReview.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)

        mAdapter = UnwrittenReviewAdapter()
        mAdapter.setListener(this@UnwrittenReviewFragment)
        recyclerViewReview.adapter = mAdapter

        val bus = NetworkBus(NetworkApi.API68.name)
        EventBus.getDefault().post(bus)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onDeliverCheck(item: API68.DataBean.OrderItem.ItemsBean.ProductItem) {
        if(item.trackingInfo_url.isNotEmpty()) {
            startActivity(Intent(context!!, DeliveryCheckActivity::class.java).apply {
                putExtra(DeliveryCheckActivity.INTENT_EXTRA_KEY_URL, item.trackingInfo_url)
            })
        } else {
            AppToast(context!!).showToastMessage("배송지 정보가 없습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    override fun onDetailClicked(orderNo: String) {
        val intent = Intent(context!!, PurchaseHistoryDetailActivity::class.java).apply {
            putExtra(PurchaseHistoryDetailActivity.INTENT_EXTRA_KEY_ORDER_NUMBER, orderNo)
        }
        startActivity(intent)
    }

    override fun onReviewClicked(item: API68.DataBean.OrderItem.ItemsBean.ProductItem, orderNo: String, regDate: String, shopName: String, status: String) {
        if("배송 완료" == status) {
            val intent = Intent(context!!, WriteReviewActivity::class.java).apply {
                putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_OPTION, item.option)
                putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_PRICE, item.price)
                putExtra(WriteReviewActivity.INTENT_KEY_DELIVERY_COMPANY, item.trackingInfo_name)
                putExtra(WriteReviewActivity.INTENT_KEY_ITEM_KEY, item.itemKey)
                putExtra(WriteReviewActivity.INTENT_KEY_ORDER_NUMBER, orderNo)
                putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_IMG, item.img)
                putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_STATUS, item.status)
                putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_NAME, item.trackingInfo_name)
                putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_NAME, item.title)
                putExtra(WriteReviewActivity.INTENT_KEY_STORE_NAME, shopName)
                putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_CODE, item.trackingInfo_code)
                putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_URL, item.trackingInfo_url)
                putExtra(WriteReviewActivity.INTENT_KEY_CT_ID, item.ct_id)
                putExtra(WriteReviewActivity.INTENT_KEY_IT_ID, item.it_id)
                putExtra(WriteReviewActivity.INTENT_KEY_DATE, regDate)
            }

            startActivity(intent)
        } else {
            startActivity(Intent(context!!, WriteDetailReviewActivity::class.java).apply {
                putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_OPTION, item.option)
                putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_PRICE, item.price)
                putExtra(WriteReviewActivity.INTENT_KEY_DELIVERY_COMPANY, item.trackingInfo_name)
                putExtra(WriteReviewActivity.INTENT_KEY_ITEM_KEY, item.itemKey)
                putExtra(WriteReviewActivity.INTENT_KEY_ORDER_NUMBER, orderNo)
                putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_IMG, item.img)
                putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_STATUS, item.status)
                putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_NAME, item.trackingInfo_name)
                putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_NAME, item.title)
                putExtra(WriteReviewActivity.INTENT_KEY_STORE_NAME, shopName)
                putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_CODE, item.trackingInfo_code)
                putExtra(WriteReviewActivity.INTENT_KEY_TRACKING_URL, item.trackingInfo_url)
                putExtra(WriteReviewActivity.INTENT_KEY_CT_ID, item.ct_id)
                putExtra(WriteReviewActivity.INTENT_KEY_IT_ID, item.it_id)
                putExtra(WriteReviewActivity.INTENT_KEY_DATE, regDate)
            })
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API68 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API68.toString(), AppPreferences.getUserId(context!!)!!, "")

        when (data.arg1) {
            API68 -> getNotReviewList(data)
        }
    }

    private fun getNotReviewList(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            val response: API68 = Gson().fromJson(str, API68::class.java)

            if (response.nTotalCount > 0) {
                layoutList.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE

                mAdapter.setItem(response.data.orders)
            } else {
                layoutList.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            }

            (activity as MyReviewActivity).setUnReviewCount(response.nTotalCount)
        } else {
            val errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }
}