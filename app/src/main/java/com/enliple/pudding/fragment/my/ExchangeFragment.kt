package com.enliple.pudding.fragment.my

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import kotlinx.android.synthetic.main.fragment_exchange.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

class ExchangeFragment : androidx.fragment.app.Fragment() {
    companion object {
        const val BUNDLE_EXTRA_KEY_USER_ID = "ExchangeFragment_userId"
        const val BUNDLE_EXTRA_KEY_IS_JELLY = "ExchangeFragment_isJelly"
        const val BUNDLE_EXTRA_KEY_USER_COOKIE = "ExchangeFragment_userCookie"
        const val BUNDLE_EXTRA_KEY_USER_POINT = "ExchangeFragment_point"
        const val JELLY_COMMISSION = 40
        const val POINT_COMMISSION = 5
        const val JELLY_PRICE = 100 // 젤리 1개당 100원
    }

    private var userId = ""
    private var isJelly = false
    private var userPoint = "0"
    private var userCookie = "0"
    private var editedResult: Double = 0.0
    private var alertDialog: Dialog? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.e("ExchangeFragment onCreateView")
        return inflater.inflate(R.layout.fragment_exchange, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.e("ExchangeFragment onCreate")
        if (arguments != null) {
            userId = arguments!!.getString(BUNDLE_EXTRA_KEY_USER_ID)
            isJelly = arguments!!.getBoolean(BUNDLE_EXTRA_KEY_IS_JELLY)
            userCookie = arguments!!.getString(BUNDLE_EXTRA_KEY_USER_COOKIE)
            userPoint = arguments!!.getString(BUNDLE_EXTRA_KEY_USER_POINT)
            Logger.e("ExchangeFragment userId :: $userId")
            Logger.e("ExchangeFragment isJelly :: $isJelly")
            Logger.e("ExchangeFragment userCookie :: $userCookie")
            Logger.e("ExchangeFragment userPoint :: $userPoint")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.e("ExchangeFragment onViewCreated")
//        EventBus.getDefault().register(this)
        if (isJelly) {
            jelly_title.text = "젤리 갯수"
            cnt.text = "개"
            remainTitle.text = "환전 후 남은 젤리"
            comment1.text = getString(R.string.jelly_exchange_1)
            comment2.text = getString(R.string.jelly_exchange_2)
            comment3.text = getString(R.string.jelly_exchange_3)
            comment4.text = getString(R.string.jelly_exchange_4)
            remainTitle.text = "환전 후 남은 젤리 :  "
            editJellyCnt.hint = getString(R.string.hint_jelly_count)
        } else {
            jelly_title.text = "포인트"
            cnt.text = "원"
            remainTitle.text = "환전 후 남은 포인트"
            comment1.text = getString(R.string.jelly_exchange_1)
            comment2.text = getString(R.string.jelly_exchange_2)
            comment3.text = getString(R.string.jelly_exchange_3)
            comment4.text = getString(R.string.jelly_exchange_4)
            remainTitle.text = "환전 후 남은 포인트 :  "
            editJellyCnt.hint = getString(R.string.hint_point_count)
        }

//        NetworkBus(NetworkApi.API21.name, AppPreferences.getUserId(context!!)).let {
//            EventBus.getDefault().post(it)
//        }

        editJellyCnt.addTextChangedListener(watcher)
        editTextPhone.addTextChangedListener(etcWatcher)
        editTextName.addTextChangedListener(etcWatcher)
        editBankName.addTextChangedListener(etcWatcher)
        editAccount.addTextChangedListener(etcWatcher)

        buttonExchange.setOnClickListener(clickListener)
        buttonCancel.setOnClickListener(clickListener)
    }

    private var clickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if ( v!!.getId() == R.id.buttonExchange ) {
                if ( setRequestEnable() )
                    requestExchange()
            } else if ( v!!.getId() == R.id.buttonCancel ) {
                activity!!.finish()
            }
        }

    }

    private var etcWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            buttonExchange.isEnabled = setRequestEnable()
        }
    }

    private var watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            var s_edited = s.toString().replace(",".toRegex(), "")
            Logger.e("s_edited :: " + s_edited + " , length :: " + s_edited.length + " , s :: " + s.toString())

            try {
                var d_edited = s_edited.toDouble()
                var d_uValue = 0.0
                if ( isJelly ) {
                    d_uValue = userCookie.toDouble()
                } else {
                    d_uValue = userPoint.toDouble()
                }
                if ( d_edited > d_uValue ) {
                    s_edited = "$d_uValue"
                    d_edited = d_uValue
                }
                var sFinal = Utils.ToNumFormat(d_edited)
                Logger.e("sFinal :: " + sFinal)
                if ( s.toString() !=  sFinal ) {
                    editJellyCnt.setText(sFinal)
                    editJellyCnt.setSelection(sFinal.length)
                }
                if ( isJelly )
                    setJellyPrice(s_edited)
                else
                    setPointPrice(s_edited)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            buttonExchange.isEnabled = setRequestEnable()
        }
    }

    private fun setJellyPrice(cnt: String) {
        Logger.e("setJellyPrice cnt :: " + cnt)
        var price = cnt.toDouble()
        Logger.e("setJellyPrice price :: " + price)
        var recalcPrice = price * JELLY_PRICE
        Logger.e("setJellyPrice recalcPrice :: " + recalcPrice)
        if ( recalcPrice > 0 ) {
            expectLayer.visibility = View.VISIBLE
        } else {
            expectLayer.visibility = View.GONE
        }

        var commissionRate = JELLY_COMMISSION
        var commission = recalcPrice * commissionRate / 100
        Logger.e("commission :: " + commission)
        var calcResult = recalcPrice - commission
        var sRest = Utils.ToNumFormat(userCookie.toDouble())
        if ( calcResult > 0 ) {
            var duCookie = userCookie.toDouble()
            var dRest = duCookie - cnt.toDouble()
            sRest = Utils.ToNumFormat(dRest)
        }
        var sFinal = Utils.ToNumFormat(calcResult)

        expectedPrice.text = "${sFinal}원"
        remainPrice.text = "${sRest}개"
    }

    private fun setPointPrice(uPoint: String) {
        var price = uPoint.toDouble()
        Logger.e("price :: " + price)
        if ( price > 0 ) {
            expectLayer.visibility = View.VISIBLE
        } else {
            expectLayer.visibility = View.GONE
        }
        var commissionRate = POINT_COMMISSION
        var commission = price * commissionRate / 100
        Logger.e("commission :: " + commission)
        var calcResult = price - commission
        var sRest = Utils.ToNumFormat(userPoint.toDouble())
        if ( calcResult > 0 ) {
            var duPoint = userPoint.toDouble()
            var dRest = duPoint - price
            sRest = Utils.ToNumFormat(dRest)
        }
        var sFinal = Utils.ToNumFormat(calcResult)
        expectedPrice.text = "${sFinal}원"
        remainPrice.text = "${sRest}원"


    }

    private fun requestExchange() {
        try {
            var check = check()
            if ( check ) {
                var type = "point"
                if ( isJelly ) {
                    type = "cookie"
                }
                var price = editJellyCnt.getText()!!.toString().replace(",".toRegex(), "")
                val requestObj = JSONObject()
                requestObj.put("type", type)
                requestObj.put("strName", editTextName.getText()!!.toString())
                requestObj.put("strNum", editTextPhone.getText()!!.toString())
                requestObj.put("strBankName", editBankName.getText()!!.toString())
                requestObj.put("strBankAccount", editAccount.getText()!!.toString())
                requestObj.put("point", price)
                val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestObj.toString())

                var task = ShopTreeAsyncTask(context!!)
                task.postExchange(AppPreferences.getUserId(context!!), body, object: ShopTreeAsyncTask.OnDefaultObjectCallbackListener{
                    override fun onResponse(result: Boolean, obj: Any?) {
                        if ( result ) {
                            var jsonObject = obj as JSONObject
                            if ( jsonObject != null ) {
                                var result = jsonObject.optString("result")
                                if ( result != null )
                                    result = result.toLowerCase()
                                if ( "success" == result ) {
                                    AppToast(context!!).showToastMessage("요청이 완료되었습니다.",
                                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                                            AppToast.GRAVITY_BOTTOM)
                                    activity!!.finish()
                                } else {
                                    var status = jsonObject.optString("status")
                                    var message = jsonObject.optString("message")
                                    if (status != null ) {
                                        status = status.toLowerCase()
                                        if ( status == "false") {
                                            if ( !TextUtils.isEmpty(message) ) {
                                                showDialog(message)
//                                                AppToast(context!!).showToastMessage(message,
//                                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
//                                                        AppToast.GRAVITY_BOTTOM)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                })

            } else {
                AppToast(context!!).showToastMessage("입력하신 정보를 확인해주세요.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun check() : Boolean {
        var price = editJellyCnt.getText()!!.toString().replace(",".toRegex(), "")
        var name = editTextName.getText()!!.toString().replace(" ", "")
        var phone = editTextPhone.getText()!!.toString().replace(" ", "").replace("-", "")
        var bank = editBankName.getText()!!.toString().replace(" ", "")
        var account = editAccount.getText()!!.toString().replace(" ", "")
        var usablePrice = false
        var usableName = false
        var usablePhone = false
        var usableBank = false
        var usableAccount = false

        var message = ""

        if ( name != null && name.isNotEmpty()) {
            usableName = true
        } else {
            showDialog("이름을 입력해주세요")
            usableName = false
            return false
        }

        if ( phone != null && phone.isNotEmpty()) {
            usablePhone = true
        } else {
            showDialog("휴대폰 번호를 확인해주세요")
            usablePhone = false
            return false
        }

        if ( bank != null && bank.isNotEmpty()) {
            usableBank = true
        } else {
            showDialog("은행명을 입력해주세요")
            usableBank = false
            return false
        }

        if ( account != null && account.isNotEmpty()) {
            usableAccount = true
        } else {
            showDialog("계좌번호를 입력해주세요")
            usableAccount = false
            return false
        }

        if ( price != null && price.isNotEmpty()) {
            if ( isJelly ) {
                try {
                    var dPrice =price.toDouble()
                    // 최소 3만원이 되어야 요청 가능. 젤리는 개당 100원
                    if ( (dPrice * JELLY_PRICE) < 30000 ) {
                        message = "젤리 환전 요청은 최소 300개부터 가능합니다."
                        showDialog(message)
                        usablePrice = false
                        return false
                    } else {
                        usablePrice = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                }
            } else {
                try {
                    var dPrice =price.toDouble()
                    // 최소 3만원이 되어야 요청 가능
                    if ( dPrice < 30000 ) {
                        message = "포인트 환전 요청은 최소 30,000원부터 가능합니다."
                        showDialog(message)
                        usablePrice = false
                        return false
                    } else {
                        usablePrice = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                }
            }
        } else {
            var message = ""
            if ( isJelly ) {
                message = "젤리 갯수를 입력해주세요."
            } else {
                message = "포인트를 입력해주세요."
            }
            showDialog(message)
            usablePrice = false
            return false
        }


        return usableName && usablePhone && usableBank && usableAccount && usablePrice;
    }


    private fun showDialog(message: String) {
        if (alertDialog != null)
            alertDialog!!.dismiss()
        alertDialog = null
        alertDialog = AlertDialog.Builder(context!!)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }.create()
        alertDialog!!.show()
    }

    private fun isUsableName(): Boolean {
        var name = editTextName.getText()!!.toString().replace(" ", "")
        Logger.e("usableName :: ${name != null && name.isNotEmpty()}")
        return name != null && name.isNotEmpty()
    }

    private fun isUsablePhone(): Boolean {
        var phone = editTextPhone.getText()!!.toString().replace(" ", "").replace("-", "")
        Logger.e("usablePhone :: ${phone != null && phone.isNotEmpty()}")
        return phone != null && phone.isNotEmpty()
    }

    private fun isUsableBank(): Boolean {
        var bank = editBankName.getText()!!.toString().replace(" ", "")
        Logger.e("usable bank :: ${bank != null && bank.isNotEmpty()}")
        return bank != null && bank.isNotEmpty()
    }

    private fun isUsableAccount(): Boolean {
        var account = editAccount.getText()!!.toString().replace(" ", "")
        Logger.e("usableAccount :: ${account != null && account.isNotEmpty()}")
        return account != null && account.isNotEmpty()
    }

    private fun isUsablePrice(): Boolean {
        var price = editJellyCnt.getText()!!.toString().replace(",".toRegex(), "")
        if ( price != null && price.isNotEmpty()) {
            if ( isJelly ) {
                try {
                    var dPrice =price.toDouble()
                    // 최소 3만원이 되어야 요청 가능. 젤리는 개당 100원
                    Logger.e("price err5")
                    return (dPrice * JELLY_PRICE) >= 30000
                } catch (e: Exception) {
                    e.printStackTrace()
                    Logger.e("price err4")
                    return false
                }
            } else {
                try {
                    var dPrice =price.toDouble()
                    // 최소 3만원이 되어야 요청 가능
                    Logger.e("price err3")
                    return dPrice >= 30000
                } catch (e: Exception) {
                    e.printStackTrace()
                    Logger.e("price err2")
                    return false
                }
            }
        } else {
            Logger.e("price err1")
            return false
        }
    }

    private fun setRequestEnable(): Boolean {
        var result = isUsablePrice() && isUsableAccount() && isUsableBank() && isUsableAccount() && isUsableName() && isUsablePhone()
                if (result) {
                    buttonExchange.setBackgroundColor(Color.parseColor("#9f56f2"))
                } else {
                    buttonExchange.setBackgroundColor(Color.parseColor("#d9e1eb"))
                }
        return result
    }
}