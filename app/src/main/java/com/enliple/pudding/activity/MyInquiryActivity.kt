package com.enliple.pudding.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.InquriyAdapter
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API40
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_my_qna.*

/**
 * Created by Kim Joonsung on 2018-10-30.
 */
class MyInquiryActivity : AbsBaseActivity(), InquriyAdapter.Listener {

    companion object {
        private const val TAG = "MyInquiryActivity"
        private const val RECYCLER_VIEW_ITEM_CACHE_SIZE = 20
        private const val REQUEST_GO_CART = 50222
    }

    private lateinit var mAdater: InquriyAdapter
    private var data: API40? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_qna)

        buttonBack.setOnClickListener(clickListener)
        buttonMessage.setOnClickListener(clickListener)
        buttonCart.setOnClickListener(clickListener)

        recyclerVeiwInquiry.setItemViewCacheSize(RECYCLER_VIEW_ITEM_CACHE_SIZE)
        recyclerVeiwInquiry.setHasFixedSize(false)

        mAdater = InquriyAdapter()
        mAdater.setListener(this@MyInquiryActivity)
        recyclerVeiwInquiry.adapter = mAdater

        var key = intent.getStringExtra("KEY")
        var messageCnt = intent.getIntExtra("MESSAGE_CNT", 0)
        var cartCnt = intent.getIntExtra("CART_CNT", 0)
        if (TextUtils.isEmpty(key)) {
            finish()
        } else {
            val str = DBManager.getInstance(this).get(key)
            Logger.e("str ::~!~~~ "  + str)
            data = Gson().fromJson(str, API40::class.java)
            loadData(data!!)
        }

        setCartBadgeCount(cartCnt)
        setSmsBadgeCount(messageCnt)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}
    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onMoreClicked() {
        startActivity(Intent(this@MyInquiryActivity, MyProductQnaActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GO_CART) {
                val cartCnt = data!!.getIntExtra("CART_CNT", 0)
                setCartBadgeCount(cartCnt)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun loadData(data: API40) {
        if(data.data.isNotEmpty()) {
            layoutEmpty.visibility = View.GONE
            recyclerVeiwInquiry.visibility = View.VISIBLE

            mAdater.setItems(data.data)
        } else {
            layoutEmpty.visibility = View.VISIBLE
            recyclerVeiwInquiry.visibility = View.GONE
        }
    }

    private fun setSmsBadgeCount(count: Int) {
        textViewMessageBadge.text = "" + count
        if (count > 0) {
//            textViewMessageBadge.visibility = View.VISIBLE
        } else {
            textViewMessageBadge.visibility = View.GONE
        }
    }

    /**
     * 장바구니 Badge 에 설정할 Count 를 지정 (0 개인 경우 숨김처리)
     * @param count
     */
    private fun setCartBadgeCount(count: Int) {
        textViewCartBadge.text = "" + count
        if (count > 0) {
//            textViewCartBadge.visibility = View.VISIBLE
        } else {
            textViewCartBadge.visibility = View.GONE
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonBack -> onBackPressed()

            R.id.buttonMessage -> startActivity(Intent(this@MyInquiryActivity, MessageActivity::class.java))

            R.id.buttonCart -> {
                val intent = Intent(this, ProductCartActivity::class.java)
                startActivityForResult(intent, REQUEST_GO_CART)
            }
        }
    }
}