package com.enliple.pudding.fragment.shoptree

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.enliple.pudding.R
import com.enliple.pudding.bus.QnABus
import com.enliple.pudding.commons.app.SoftKeyboardUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API91
import com.enliple.pudding.commons.network.vo.API93
import com.enliple.pudding.commons.network.vo.BaseAPI
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.widget.NothingSelectedSpinnerAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_product_qna.*
import kotlinx.android.synthetic.main.product_qna_button.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2018-12-05.
 */
class ProductQnAFragment : androidx.fragment.app.Fragment() {

    companion object {
        private val TAB_ALL = 0
        private val TAB_MY = 1
    }
    interface Listener {
        fun scroll(scrollTo: Int?)
    }

    var listener: Listener? = null

    private var mSpinnerData: ArrayList<String> = ArrayList()
    private var qnaData: ArrayList<API93.QnaType> = ArrayList()
    private var productId = ""
    private var qnaType = ""

    private var allQnaFragment: QnAFragment? = null
    private var myQnaFragment: MyQnaFragment? = null
    private var isUpdated = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_product_qna, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)

        askingEditText.setOnEditorActionListener(searchActionListener)

        askingSpinner?.onItemSelectedListener = spinnerItemListener

        buttonQnA.setOnClickListener(clickListener)
        buttonAsking.setOnClickListener(clickListener)
        buttonCancel.setOnClickListener(clickListener)
        buttonSecretCheck.setOnClickListener(clickListener)
        layoutAllQna.setOnClickListener(clickListener)
        layoutMyQna.setOnClickListener(clickListener)

        if (arguments != null) {
            productId = arguments!!.getString("idx")
        }

        setQnaText(getString(R.string.msg_product_qna),
                getString(R.string.msg_product_qna_spanalbe))

        var bus = NetworkBus(NetworkApi.API93.name)
        EventBus.getDefault().post(bus)

        var bus1 = NetworkBus(NetworkApi.API91.name, productId)
        EventBus.getDefault().post(bus1)

        setTabUI(TAB_ALL)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: NetworkBusResponse) {
        val API93 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API93.toString(), "", "")
        val API92 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API92.toString(), productId, "")
        val API91 = NetworkHandler.getInstance(context!!).getKey(NetworkApi.API91.toString(), productId, "")

        when (data.arg1) {
            API93 -> setQnaType(data)
            API91 -> setQnaCount(data)
            API92 -> {
                if ("ok" == data.arg2) {
                    AppToast(context!!).showToastMessage("문의가 정상적으로 등록 되었습니다",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                    askingSpinner.setSelection(0)
                    textViewAskingType.visibility = View.VISIBLE
                    setSpinnerText(getString(R.string.msg_shop_tree_qna_type))

                    buttonSecretCheck.isSelected = false
                    layoutBeforeQnA.visibility = View.VISIBLE
                    layoutQnAInput.visibility = View.GONE
                    layoutButton.visibility = View.GONE

                    askingEditText.setText("")

                    isUpdated = true

                    var bus1 = NetworkBus(NetworkApi.API91_3.name, productId, allQnaFragment?.getSecretStatus())
                    EventBus.getDefault().post(bus1)

                } else {
                    var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
                    Logger.e("error : $errorResult")

                    AppToast(context!!).showToastMessage(errorResult.message,
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:QnABus) {
        when (data.type) {
            0 -> {
                if (data.count == 0) {
                    textViewAllCnt.setTextColor(Color.parseColor("#1b1b1b"))
                    textViewAllCntTitle.setTextColor(Color.parseColor("#1b1b1b"))
                } else {
                    textViewAllCnt.setTextColor(Color.parseColor("#9f56f2"))
                    textViewAllCntTitle.setTextColor(Color.parseColor("#9f56f2"))
                }

                textViewAllCnt.text = data.count.toString()
            }

            1 -> {
                if (data.count == 0) {
                    textViewMyCnt.setTextColor(Color.parseColor("#1b1b1b"))
                    textViewMyCntTitle.setTextColor(Color.parseColor("#1b1b1b"))
                } else {
                    textViewMyCnt.setTextColor(Color.parseColor("#9f56f2"))
                    textViewMyCntTitle.setTextColor(Color.parseColor("#9f56f2"))
                }

                textViewMyCnt.text = data.count.toString()
            }
        }
    }

    fun hideKeyboard() {
        SoftKeyboardUtils.hideKeyboard(askingEditText)
    }

    fun setEmptyHeight(height: Int) {
        var param = empty!!.layoutParams
        param.height = height
        empty!!.layoutParams = param
    }

    private fun initFragment() {
        allQnaFragment = QnAFragment()
        var bundle = Bundle()
        bundle.putString("idx", productId)
        allQnaFragment!!.arguments = bundle

        myQnaFragment = MyQnaFragment()
        var bundle1 = Bundle()
        bundle1.putString("idx", productId)
        myQnaFragment!!.arguments = bundle1

        var transaction = childFragmentManager.beginTransaction()

        transaction.add(layoutContainer.id, allQnaFragment!!, allQnaFragment!!::class.java.name)
        transaction.add(layoutContainer.id, myQnaFragment!!, myQnaFragment!!::class.java.name)

        transaction.hide(myQnaFragment!!)
        transaction.attach(allQnaFragment!!)
        transaction.show(allQnaFragment!!)

        transaction.commitAllowingStateLoss()
    }

    private fun showAllFragment() {
        if (allQnaFragment == null) {
            allQnaFragment = QnAFragment()
        }

        var transaction = childFragmentManager.beginTransaction()
        transaction.hide(myQnaFragment!!)
        transaction.attach(allQnaFragment!!)
        transaction.show(allQnaFragment!!)

        transaction.commitAllowingStateLoss()
    }

    private fun showMyFragment() {
        if (myQnaFragment == null) {
            myQnaFragment = MyQnaFragment()
        }

        var transaction = childFragmentManager.beginTransaction()
        transaction.hide(allQnaFragment!!)
        transaction.attach(myQnaFragment!!)
        transaction.show(myQnaFragment!!)

        transaction.commitAllowingStateLoss()
    }

    private fun initTab(allCount: Int, myCount: Int) {
        textViewAllCnt.text = allCount.toString()
        if (allCount == 0) {
            textViewAllCnt.setTextColor(Color.parseColor("#1b1b1b"))
            textViewAllCntTitle.setTextColor(Color.parseColor("#1b1b1b"))
        } else {
            textViewAllCnt.setTextColor(Color.parseColor("#9f56f2"))
            textViewAllCntTitle.setTextColor(Color.parseColor("#9f56f2"))
        }

        textViewMyCnt.text = myCount.toString()
        if (myCount == 0) {
            textViewMyCnt.setTextColor(Color.parseColor("#1b1b1b"))
            textViewMyCntTitle.setTextColor(Color.parseColor("#1b1b1b"))
        } else {
            textViewMyCnt.setTextColor(Color.parseColor("#9f56f2"))
            textViewMyCntTitle.setTextColor(Color.parseColor("#9f56f2"))
        }
    }

    private fun setQnaText(emptyText: String, colorSpannableText: String) {
        val sp = SpannableStringBuilder(emptyText)
        sp.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.main_color)),
                0, colorSpannableText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textViewQnA.text = sp
        textViewQnA1.text = sp
    }

    private fun setSpinnerText(emptyText: String) {
        val sp = SpannableStringBuilder(emptyText)
        sp.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.main_color)),
                14, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textViewAskingType.text = sp
    }

    private fun setQnaType(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            val str = DBManager.getInstance(context!!).get(data.arg1)
            val response: API93 = Gson().fromJson(str, API93::class.java)

            if (response.data.size > 0) {
                for (i in 0 until response.data.size) {
                    mSpinnerData.add(response.data.get(i).value)
                }

                qnaData.addAll(response.data)

                val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, mSpinnerData)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                askingSpinner.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_center_asking, context!!)

                setSpinnerText(getString(R.string.msg_shop_tree_qna_type))
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun sendQna() {
        val requestObj = JSONObject()
        requestObj.put("user", AppPreferences.getUserId(context!!))
        requestObj.put("content", askingEditText.text.toString())
        requestObj.put("type", qnaType)
        requestObj.put("secret", if (buttonSecretCheck.isSelected) "1" else "0")

        var body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString())

        var bus = NetworkBus(NetworkApi.API92.name, productId, body)
        EventBus.getDefault().post(bus)
    }

    private fun setQnaCount(data: NetworkBusResponse) {
        if ("ok" == data.arg2) {
            if ( isUpdated ) {

                val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm!!.hideSoftInputFromWindow(askingEditText.getWindowToken(), 0)

                if ( allQnaFragment != null ) {
                    allQnaFragment!!.refreshData()
                }

                if ( myQnaFragment != null ) {
                    myQnaFragment!!.refreshData()
                }
            } else {
                initFragment()
            }
        } else {
            var errorResult: BaseAPI = Gson().fromJson(data.arg4, BaseAPI::class.java)
            Logger.e("error : $errorResult")

            AppToast(context!!).showToastMessage(errorResult.message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
        }
    }

    private fun setTabUI(position: Int) {
        if (position == TAB_ALL) {
            viewAll.visibility = View.VISIBLE
            viewMy.visibility = View.GONE
        } else {
            viewAll.visibility = View.GONE
            viewMy.visibility = View.VISIBLE
        }
    }

    private val searchActionListener = TextView.OnEditorActionListener { v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            SoftKeyboardUtils.hideKeyboard(v)
            true
        }

        false
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.buttonQnA -> {
                layoutBeforeQnA.visibility = View.GONE
                layoutQnAInput.visibility = View.VISIBLE
                layoutButton.visibility = View.VISIBLE
            }

            R.id.buttonCancel -> {
                layoutBeforeQnA.visibility = View.VISIBLE
                layoutQnAInput.visibility = View.GONE
                layoutButton.visibility = View.GONE

                askingSpinner.setSelection(0)
                textViewAskingType.visibility = View.VISIBLE
                setSpinnerText(getString(R.string.msg_shop_tree_qna_type))

                buttonSecretCheck.isSelected = false

                askingEditText.setText("")

            }

            R.id.buttonAsking -> {
                if (TextUtils.isEmpty(qnaType)) {
                    AppToast(context!!).showToastMessage("문의 유형이 선택되지 않았습니다",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)
                } else {
                    sendQna()
                }
            }

            R.id.buttonCheck -> {
                if (!it.isSelected) it.isSelected = true else it.isSelected = false
            }

            R.id.layoutAllQna -> {
                setTabUI(TAB_ALL)
                showAllFragment()
            }

            R.id.layoutMyQna -> {
                setTabUI(TAB_MY)
                showMyFragment()
            }

            R.id.buttonSecretCheck -> {
                it.isSelected = !it.isSelected
            }
        }
    }

    private val spinnerItemListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            textViewAskingType.visibility = View.GONE
            if (position != 0) {
                qnaType = qnaData.get(position - 1).key.toString()
            }
        }
    }
}