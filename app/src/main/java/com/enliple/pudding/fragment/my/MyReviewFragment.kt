package com.enliple.pudding.fragment.my

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.MyReviewActivity
import com.enliple.pudding.activity.WriteDetailReviewActivity
import com.enliple.pudding.activity.WriteReviewActivity
import com.enliple.pudding.adapter.my.MyReviewAdapter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API67
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_my_review.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

/**
 * Created by Kim Joonsung on 2018-12-13.
 */
class MyReviewFragment : AbsBaseFragment(), MyReviewAdapter.Listener {

    companion object {
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20

        private const val REQUEST_REVIEW_DETAIL = 0
    }

    private lateinit var mAdapter: MyReviewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        recyclerViewReview.setHasFixedSize(true)
        recyclerViewReview.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)

        mAdapter = MyReviewAdapter()
        mAdapter.setListener(this@MyReviewFragment)
        recyclerViewReview.adapter = mAdapter

        val bus = NetworkBus(NetworkApi.API67.name)
        EventBus.getDefault().post(bus)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK &&
                requestCode == REQUEST_REVIEW_DETAIL) {
            val bus = NetworkBus(NetworkApi.API67.name)
            EventBus.getDefault().post(bus)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onItemClicked(item: API67.ReviewItem) {
        startActivityForResult(Intent(context!!, WriteDetailReviewActivity::class.java).apply {
            putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_NAME, item.it_name)
            putExtra(WriteReviewActivity.INTENT_KEY_STORE_NAME, item.store_name)
            putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_OPTION, item.ct_option)
            putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_PRICE, item.ct_price.toDouble())
            putExtra(WriteReviewActivity.INTENT_KEY_DELIVERY_COMPANY, item.trackingInfo_name)
            putExtra(WriteReviewActivity.INTENT_KEY_ITEM_KEY, "")
            putExtra(WriteReviewActivity.INTENT_KEY_ST_ORDER_NUMBER, "")
            putExtra(WriteReviewActivity.INTENT_KEY_ORDER_NUMBER, item.od_id)
            putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_IMG, item.it_img1)
            putExtra(WriteReviewActivity.INTENT_KEY_PRODUCT_STATUS, item.ct_status)
            putExtra(WriteReviewActivity.INTENT_KEY_CT_ID, item.ct_id)
            putExtra(WriteReviewActivity.INTENT_KEY_IT_ID, item.it_id)
            putExtra(WriteDetailReviewActivity.INTENT_KEY_IS_ID, item.is_id)
            putExtra(WriteDetailReviewActivity.WRITE_DETAIL_REVIEW_SCORE, item.is_score)
            putExtra(WriteDetailReviewActivity.WRITE_DETAIL_REVIEW_CONTENT, item.is_content)
            putExtra(WriteReviewActivity.INTENT_KEY_DATE, item.od_time)

            if (item.is_photo.size > 0) {
                val photo = JSONArray()
                for (i in 0 until item.is_photo.size) {
                    photo.put(item.is_photo.get(i))
                }

                putExtra(WriteDetailReviewActivity.WRITE_DETAIL_REVIEW_PHOTO, photo.toString())
            }
        }, REQUEST_REVIEW_DETAIL)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API67 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API67.toString(), AppPreferences.getUserId(context!!)!!, "")

        when (data.arg1) {
            API67 -> getMyReviewList(data)
        }
    }

    fun getMyReviewList(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            val response: API67 = Gson().fromJson(str, API67::class.java)

            if (response.nTotalCount > 0) {
                recyclerViewReview.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE

                mAdapter.setItems(response.data)
            } else {
                recyclerViewReview.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            }

            (activity as MyReviewActivity).setMyReviewCount(response.nTotalCount)
        } else {
            val errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")
        }
    }
}