package com.enliple.pudding.fragment.my

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.AbsBaseTabFragment
import com.enliple.pudding.R
import com.enliple.pudding.activity.*
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API55
import com.enliple.pudding.commons.network.vo.API56
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_my_delivery_header.*
import kotlinx.android.synthetic.main.layout_my_delivery_menu.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class MyShoppingFragment : AbsBaseTabFragment() {
    private var mIsVisibleToUser = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_shopping, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d("onViewCreated")

        EventBus.getDefault().register(this)

        buttonPurchaseHistory.setOnClickListener {
            startActivity(Intent(view.context, PurchaseHistoryActivity::class.java))
        }

        btnPayCompleted.setOnClickListener {
            var intent = Intent(view.context, DeliveryStatusActivity::class.java).apply {
                putExtra("STATUS", DeliveryStatusActivity.PAYMENT_COMPLETED)
            }
            startActivity(intent)
        }

        btnDeliveryPending.setOnClickListener {
            var intent = Intent(view.context, DeliveryStatusActivity::class.java).apply {
                putExtra("STATUS", DeliveryStatusActivity.PRODUCT_READY)
            }
            startActivity(intent)
        }

        btnDelivering.setOnClickListener {
            var intent = Intent(view.context, DeliveryStatusActivity::class.java).apply {
                putExtra("STATUS", DeliveryStatusActivity.PRODUCT_SENDING)
            }
            startActivity(intent)
        }

        btnDeliveryComplete.setOnClickListener {
            var intent = Intent(view.context, DeliveryStatusActivity::class.java).apply {
                putExtra("STATUS", DeliveryStatusActivity.DELIVERY_FINISHED)
            }
            startActivity(intent)
        }

        buttonCRE.setOnClickListener {
            startActivity(Intent(view.context, CREActivity::class.java))
        }

        buttonProductViewHistory.setOnClickListener {
            startActivity(Intent(view.context, RecentProductsActivity::class.java))
        }

        layoutCompany.setOnClickListener {
            if (!buttonDromDown.isSelected) {
                buttonDromDown.isSelected = true
                layoutCompanyInfo.visibility = View.VISIBLE

                setCompanyInfo()
            } else {
                buttonDromDown.isSelected = false
                layoutCompanyInfo.visibility = View.GONE
            }
        }

        buttonCustomerCenter.setOnClickListener {
            startActivity(Intent(view.context, CustomerCenterMainActivity::class.java))
        }

        buttonNotice.setOnClickListener {
            startActivity(Intent(view.context, NoticeListActivity::class.java))
        }

        buttonTerms.setOnClickListener {
            startActivity(Intent(view.context, AgreementActivity::class.java).apply {
                putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE, AgreementActivity.INTENT_EXTRA_VALUE_TERM)
            })
        }

        buttonPrivacy.setOnClickListener {
            startActivity(Intent(view.context, AgreementActivity::class.java).apply {
                putExtra(AgreementActivity.INTENT_EXTRA_KEY_MODE, AgreementActivity.INTENT_EXTRA_VALUE_PRIVACY_USE)
            })
        }

        buttonQnA.setOnClickListener {
            startActivity(Intent(view.context, MyProductQnaActivity::class.java))
        }

        layoutScrap.setOnClickListener {
            startActivity(Intent(view.context, ScrapVODActivity::class.java))
        }

        layoutVodHistory.setOnClickListener {
            var intent = Intent(view.context, LatestViewVODActivity::class.java)
            intent.putExtra("cnt", textViewRecentCount.text.toString())
            startActivity(intent)
        }

        buttonMyReview.setOnClickListener {
            startActivity(Intent(view.context, MyReviewActivity::class.java))
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint: $isVisibleToUser")

        mIsVisibleToUser = isVisibleToUser

        if (isVisibleToUser) {
            refresh()
        }
    }

    override fun onResume() {
        super.onResume()

        refresh()
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    private fun refresh() {
        if (!mIsVisibleToUser) return

        Logger.d("refresh")

        EventBus.getDefault().post(NetworkBus(NetworkApi.API55.name))

        if (textViewPayCompleteCount == null) return

        var task = ShopTreeAsyncTask(activity)
        task.getProductCount { result, obj ->
            val data = obj as JSONObject
            if (data != null) {
                var subObject = data.optJSONObject("deliveryType")
                var payCompleteCount = subObject.optString("count_pay")
                var pendingProductCount = subObject.optString("ready_pay")
                var deliveringCount = subObject.optString("delivery_pay")
                var completeCount = subObject.optString("complete_pay")
                var wish_cnt = subObject.optString("wish_cnt")
                var scrap_cnt = subObject.optString("scrap_cnt")
                var product_cnt = subObject.optString("product_cnt")
                var vod_cnt = subObject.optString("vod_cnt")
                Logger.e("payCompleteCount $payCompleteCount")
                Logger.e("pendingProductCount $pendingProductCount")
                Logger.e("deliveringCount $deliveringCount")
                Logger.e("completeCount $completeCount")
                Logger.e("wish_cnt $wish_cnt")
                Logger.e("scrap_cnt $scrap_cnt")
                Logger.e("product_cnt $product_cnt")
                Logger.e("vod_cnt $vod_cnt")
                textViewPayCompleteCount.text = if (payCompleteCount != "null") "$payCompleteCount" else "0"
                textViewDeliveryPendingCount.text = if (pendingProductCount != "null") "$pendingProductCount" else "0"
                textViewDeliveringCount.text = if (deliveringCount != "null") "$deliveringCount" else "0"
                textViewDeliveryCompleteCount.text = if (completeCount != "null") "$completeCount" else "0"

                if ("0" == textViewPayCompleteCount.text) {
                    textViewPayCompleteCount.setTextColor(Color.parseColor("#bcc6d2"))
                } else {
                    textViewPayCompleteCount.setTextColor(Color.parseColor("#ff6c6c"))
                }

                if ("0" == textViewDeliveryPendingCount.text) {
                    textViewDeliveryPendingCount.setTextColor(Color.parseColor("#bcc6d2"))
                } else {
                    textViewDeliveryPendingCount.setTextColor(Color.parseColor("#ff6c6c"))
                }

                if ("0" == textViewDeliveringCount.text) {
                    textViewDeliveringCount.setTextColor(Color.parseColor("#bcc6d2"))
                } else {
                    textViewDeliveringCount.setTextColor(Color.parseColor("#ff6c6c"))
                }

                if ("0" == textViewDeliveryCompleteCount.text) {
                    textViewDeliveryCompleteCount.setTextColor(Color.parseColor("#bcc6d2"))
                } else {
                    textViewDeliveryCompleteCount.setTextColor(Color.parseColor("#ff6c6c"))
                }

                textViewProductHistoryCount.text = if (product_cnt != "null") "$product_cnt" else "0"
                textViewScrapCount.text = if (scrap_cnt != "null") "$scrap_cnt" else "0"
                textViewRecentCount.text = if (vod_cnt != "null") "$vod_cnt" else "0"
            }
        }
    }

    override fun onTabChanged(tabIndex: Int) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val api55 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API55.toString(), AppPreferences.getUserId(context!!)!!, "")
        if (data.arg1 == api55) {
            if (data.arg2 == "ok") {
                val response: API55 = Gson().fromJson(DBManager.getInstance(context!!).get(data.arg1), API55::class.java)
                if (response.result == "success") {
                    if (response.nTotalCount > 0) {
                        textViewProductHistoryCount.visibility = View.VISIBLE
                        textViewProductHistoryCount.text = response.nTotalCount.toString()
                    } else {
                        textViewProductHistoryCount.visibility = View.GONE
                    }
                }
            } else {
                val error = Gson().fromJson(data.arg4, BaseAPI::class.java)
                Logger.e("errpr : $error")
            }
        }
    }

    private fun setCompanyInfo() {
        try {
            var str = DBManager.getInstance(context!!).get(NetworkApi.API56.toString())
            if ( !TextUtils.isEmpty(str) ) {
                val response: API56 = Gson().fromJson(str, API56::class.java)

                textViewCompanyName.text = response.data.name
                textViewCompanyOwner.text = response.data.owner
                textViewCompanyNumber.text = response.data.company_no
                textViewCommunicationNumber.text = response.data.communication_no
                textViewCustomerCenter.text = response.data.tel
                textViewCompanyMail.text = response.data.email
                textViewCompanyAddr.text = response.data.addr
                textViewCompanyHosting.text = response.data.hosting
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
}