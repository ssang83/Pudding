package com.enliple.pudding.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.MyProductQnaAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API21
import com.enliple.pudding.commons.network.vo.API89
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_product_qna.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2018-10-25.
 */
class MyProductQnaActivity : AbsBaseActivity() {

    companion object {
        private const val TAG = "MyProductQnaActivity"
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20

        private const val REQUEST_GO_CART = 50222
    }

    private lateinit var mAdapterMy: MyProductQnaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_qna)

        EventBus.getDefault().register(this)

        recyclerVeiwQnA.setHasFixedSize(false)
        recyclerVeiwQnA.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)

        buttonClose.setOnClickListener(clickListener)
//        buttonCart.setOnClickListener(clickListener)
//        buttonMessage.setOnClickListener(clickListener)

        mAdapterMy = MyProductQnaAdapter()
        recyclerVeiwQnA.adapter = mAdapterMy

        val bus = NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(this))
        EventBus.getDefault().post(bus)

        val bus1 = NetworkBus(NetworkApi.API89.name)
        EventBus.getDefault().post(bus1)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GO_CART) {
                val cartCnt = data!!.getIntExtra("CART_CNT", 0)
//                setCartBadgeCount(cartCnt)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API21 = NetworkHandler.getInstance(this)
                .getKey(NetworkApi.API21.toString(), AppPreferences.getUserId(this)!!, "")

        val API89 = NetworkHandler.getInstance(this)
                .getKey(NetworkApi.API89.toString(), AppPreferences.getUserId(this)!!, "")

        when (data.arg1) {
            API21 -> initData(data)
            API89 -> loadData(data)
        }
    }

    private fun initData(data: NetworkBusResponse) {
        val str = DBManager.getInstance(this).get(data.arg1)
        val response = Gson().fromJson(str, API21::class.java)
        var smsCount = 0
        var cartCount = 0
        try {
            smsCount = Integer.valueOf(response.newMessage)
            cartCount = Integer.valueOf(response.cartCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        setSmsBadgeCount(smsCount)
//        setCartBadgeCount(cartCount)
    }

/*    private fun setSmsBadgeCount(count: Int) {
        textViewMessageBadge.text = "" + count
        if (count > 0) {
//            textViewMessageBadge.visibility = View.VISIBLE
        } else {
            textViewMessageBadge.visibility = View.GONE
        }
    }*/

    /**
     * 장바구니 Badge 에 설정할 Count 를 지정 (0 개인 경우 숨김처리)
     */
/*    private fun setCartBadgeCount(count: Int) {
        textViewCartBadge.text = "" + count
        if (count > 0) {
//            textViewCartBadge.visibility = View.VISIBLE
        } else {
            textViewCartBadge.visibility = View.GONE
        }
    }*/

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonClose -> onBackPressed()
            R.id.buttonCart -> {
                val intent = Intent(this, ProductCartActivity::class.java)
                startActivityForResult(intent, REQUEST_GO_CART)
            }
            R.id.buttonMessage -> startActivity(Intent(this, MessageActivity::class.java))
        }
    }

    private fun loadData(data: NetworkBusResponse) {
        val str = DBManager.getInstance(this).get(data.arg1)
        val response = Gson().fromJson(str, API89::class.java)

        if (response.nTotalCount > 0) {
            recyclerVeiwQnA.visibility = View.VISIBLE
            textViewEmpty.visibility = View.GONE

            mAdapterMy.setItem(response.data)
        } else {
            recyclerVeiwQnA.visibility = View.GONE
            textViewEmpty.visibility = View.VISIBLE
        }
    }
}