package com.enliple.pudding.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.enliple.pudding.AbsBaseActivity
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.NetworkStatusUtils
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.vo.API35
import com.enliple.pudding.commons.network.vo.API36
import com.enliple.pudding.commons.network.vo.API39
import com.enliple.pudding.widget.NothingSelectedSpinnerAdapter
import com.enliple.pudding.widget.ProductCheckDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_center_asking.*
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 고객센터 1:1 문의하기
 */
class CenterAskingActivity : AbsBaseActivity(), ProductCheckDialog.Listener {

    companion object {
        private const val TAG = "CenterAskingActivity"

        private const val REQUEST_GO_CART = 50222
    }

    var categoryList: List<API36.CategoryData> = ArrayList<API36.CategoryData>()
    var orderList: List<API39.MyOrderData> = ArrayList<API39.MyOrderData>()
    var mSpinnerData: ArrayList<String> = ArrayList<String>()
    var mSpinnerIndex: Int = -1
    var mSpinnerDetailIndex: Int = -1
    var strFirstCategory = ""
    var strSubCategory = ""
    var isReceived = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center_asking)

        askingEditText.setOnEditorActionListener(searchActionListener)
        askingEditText.addTextChangedListener(textChangeListener)
//        askingEditText.setOnFocusChangeListener { v, hasFocus ->
//            Logger.e("hasFocus :: " + hasFocus)
//            if ( !hasFocus ) {
//                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.hideSoftInputFromWindow(askingEditText.getWindowToken(), 0)
//            }
//        }

        buttonBack.setOnClickListener { onBackPressed() }
        buttonMessage.setOnClickListener { startActivity(Intent(applicationContext, MessageActivity::class.java)) }
        buttonCart.setOnClickListener {
            val intent = Intent(this, ProductCartActivity::class.java)
            startActivityForResult(intent, REQUEST_GO_CART)
        }

        alertRadioGroup.setOnCheckedChangeListener(findTypeChangeListener)

        rb_receive.post {
            rb_receive.isChecked = true
        }

        buttonCancel.setOnClickListener { onBackPressed() }
        buttonOk.setOnClickListener { requestNow() }
        buttonProductCheck.setOnClickListener { showProductCheckDialog() }

        askingSpinner?.onItemSelectedListener = spinnerItemListener
        askingDetailSpinner?.onItemSelectedListener = spinnerDetailItemListener

        EventBus.getDefault().register(this)

        var bus = NetworkBus(NetworkApi.API36.name)
        EventBus.getDefault().post(bus)

        bus = NetworkBus(NetworkApi.API39.name, AppPreferences.getUserId(this@CenterAskingActivity))
        EventBus.getDefault().post(bus)

        var messageCnt = intent.getIntExtra("MESSAGE_CNT", 0)
        var cartCnt = intent.getIntExtra("CART_CNT", 0)

        setCartBadgeCount(cartCnt)
        setSmsBadgeCount(messageCnt)
    }

    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun requestNow() {
        Logger.e("spinner index: $mSpinnerIndex")
        Logger.e("spinnerDetail index: $mSpinnerDetailIndex")
        Logger.e("requestData: ${askingEditText.editableText}")

        var content = askingEditText.text.toString()
        Logger.e("firstVal 1 :: $strFirstCategory , secondVal 1 :: $strSubCategory")
        var orderNo = ""
        if (TextUtils.isEmpty(strFirstCategory) || TextUtils.isEmpty(strSubCategory)) {
            Toast.makeText(this@CenterAskingActivity, "문의유형 / 문의 상세유형을 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this@CenterAskingActivity, "문의내용을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        if (layoutOrderNumber.visibility == View.VISIBLE) {
            orderNo = textViewOrderNumber.text.toString()
        }

//        val index = alertRadioGroup.indexOfChild(alertRadioGroup.findViewById(alertRadioGroup.checkedRadioButtonId))
//        var notification = "1"
//        if (index == 1)
//            notification = "0"
        var cartId = ""
        var notification = "1"
        if ( isReceived )
            notification = "1"
         else
            notification = "0"

        var map = HashMap<String, String>()
        map.put("user", AppPreferences.getUserId(this@CenterAskingActivity)!!)
        map.put("category", strFirstCategory)
        map.put("category2", strSubCategory)
        map.put("content", content)
        map.put("notification", notification)
        if (!TextUtils.isEmpty(orderNo))
            map.put("orderId", orderNo)
        if (!TextUtils.isEmpty(cartId))
            map.put("cartId", cartId)
        var body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (JSONObject(map)).toString())
        val bus = NetworkBus(NetworkApi.API35.name, body)
        EventBus.getDefault().post(bus)

//        Logger.e("selected index: $index")
    }

    private fun showProductCheckDialog() {
        if (orderList != null && orderList.isNotEmpty()) {
            val dialog = ProductCheckDialog(this@CenterAskingActivity, orderList).apply {
                setListener(this@CenterAskingActivity)
            }
            dialog.show()
        } else {
            Toast.makeText(this@CenterAskingActivity, "내역이 존재하지 않습니다", Toast.LENGTH_SHORT).show()
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

    override fun onProductItem(item: API39.MyOrderData) {
        if ( item != null ) {
            Logger.e("Product Select Item : ${item.item_name}")
            textViewOrderNumber.setTextColor(Color.parseColor("#192028"))
            textViewOrderNumber.text = item.order_id
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    override fun onNetworkStatusChanged(status: NetworkStatusUtils.NetworkStatus) {}

    override fun onDozeModeStateChanged(dozeEnable: Boolean) {}

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        Logger.e("onMessageEvent one by one : ${data.arg1} and name :: ${NetworkApi.API36.name}")
        var myOrderKey = "GET/support/myorder?user=${AppPreferences.getUserId(this@CenterAskingActivity)}"
        var categoryKey = "GET/support/category"
        var sendQAKey = "PUT/support/myqa"

        if (data.arg1 == categoryKey) {
            val str = DBManager.getInstance(this).get(categoryKey)
            val response = Gson().fromJson(str, API36::class.java)
            categoryList = response.data
            if (categoryList != null && categoryList.size > 0) {
                for (i in 0 until categoryList.size) {
                    mSpinnerData.add(categoryList.get(i).FirstCategory)
                }
                // Create an ArrayAdapter using a simple spinner layout and languages array
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mSpinnerData)
                // Set layout to use when the list of choices appear
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Set Adapter to Spinner
                askingSpinner.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_center_asking, this@CenterAskingActivity)
            }
        } else if (data.arg1 == sendQAKey) {
            val str = DBManager.getInstance(this).get(sendQAKey)
            val response = Gson().fromJson(str, API35::class.java)
            if (response != null) {
                var result = response.result
                if (result == "success") {
                    Toast.makeText(this@CenterAskingActivity, "문의내용을 전송하였습니다", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    var message = response.message
                    if ( !message.isEmpty() ) {
                        Toast.makeText(this@CenterAskingActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (data.arg1 == myOrderKey) {
            val str = DBManager.getInstance(this).get(myOrderKey)
            Logger.e("str to string 39 :: " + str)
            val response = Gson().fromJson(str, API39::class.java)
            orderList = response.data
        }
    }

    private val textChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            askingEditTextResult.text = "0/500"

            if (s?.toString()?.length ?: 0 > 0) {
                askingEditTextResult.text = "${s?.length}/500"
            }
        }
    }

    private val searchActionListener = TextView.OnEditorActionListener { v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            SoftKeyboardUtils.hideKeyboard(v)
            true
        }

        false
    }

    private val spinnerDetailItemListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            mSpinnerDetailIndex = position
            askingDetailType.visibility = View.GONE

            strSubCategory = parent?.getItemAtPosition(position).toString()
            when (strSubCategory) {
                "주문내역/확인", "구매결정/상품평" -> {
                    layoutOrderNumber.visibility = View.VISIBLE
                }
                else -> {
                    layoutOrderNumber.visibility = View.GONE
                }
            }
        }
    }

    private val spinnerItemListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            Logger.e("onNothingSelected")
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            Logger.e("onItemSelected")
            mSpinnerIndex = position
            askingType.visibility = View.GONE

            strFirstCategory = parent?.getItemAtPosition(position).toString()
            var subCategory = getSubCategory(strFirstCategory)
            if (subCategory != null) {
                val deailAdapter = ArrayAdapter(parent!!.context, android.R.layout.simple_spinner_item, subCategory)
                deailAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                askingDetailSpinner.adapter = NothingSelectedSpinnerAdapter(deailAdapter, R.layout.spinner_center_asking_detail, this@CenterAskingActivity)
            }
        }
    }

    private val findTypeChangeListener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        when (checkedId) {
            R.id.rb_receive -> {
                hideKeyboard()
                isReceived = true
            }
            R.id.rb_not_receive -> {
                hideKeyboard()
                isReceived = false
            }
        }
    }

    private fun getSubCategory(firstCategory: String): List<String>? {
        if (categoryList != null) {
            for (i in 0 until categoryList.size) {
                var f_category = categoryList.get(i).FirstCategory
                if (f_category == firstCategory) {
                    return categoryList.get(i).SubCategory
                }
            }
        }
        return null
    }

    fun hideKeyboard(v: View) {
        Logger.e("hideKeyboard")
        hideKeyboard()
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(askingEditText.getWindowToken(), 0)
    }
}