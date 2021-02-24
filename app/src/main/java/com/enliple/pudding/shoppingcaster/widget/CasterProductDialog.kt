package com.enliple.pudding.shoppingcaster.widget

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.shoppingcaster.fragment.productdialog.CastCookieFragment
import com.enliple.pudding.shoppingcaster.fragment.productdialog.CastPurchaseFragment
import kotlinx.android.synthetic.main.dialog_caster_product.*

/**
 * 방송중 상품 현황 등을 표시하는 하단 팝업
 * (참고 : TAB 을 변경하는동안에도 실시간 반영이 되어야 하므로 Adapter 스타일로 구현하지 않음)
 * @author hkcha
 * @since 2018.09.04
 */
class CasterProductDialog : androidx.fragment.app.DialogFragment() {
    //private var followerFragment: CastFollowerFragment? = null
    private var purchaseFragment: CastPurchaseFragment? = null
    private var cookieFragment: CastCookieFragment? = null

    private var casterId = ""
    private var streamKey = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_caster_product, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_FRAME, R.style.PlayerProductListDialog)

        var bundle = arguments
        if (bundle != null) {
            casterId = bundle!!.getString("casterId")
            streamKey = bundle!!.getString("streamKey")
        }

        isCancelable = true
        //setCanceledOnTouchOutside(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        radioGroupType.setOnCheckedChangeListener(tabCheckedChangeListener)
        radioButtonPurchase.isChecked = true

        mainLayer.setOnClickListener { dismiss() }
    }

    /**
     * 판매현황 리스트 Fragment 를 표시
     */
    private fun replacePurchaseFragment() {
        var transaction = childFragmentManager.beginTransaction()

        purchaseFragment = childFragmentManager.findFragmentByTag(CastPurchaseFragment::class.java.name) as? CastPurchaseFragment
        if (purchaseFragment == null) {
            purchaseFragment = CastPurchaseFragment()
            purchaseFragment!!.setTotalQuantityListener(object : CastPurchaseFragment.OnTotalQuantityListener {
                override fun getTotalQuantity(quantity: String) {
                    textViewStatus.text = "방송중 총 판매 수량 : "
                    textViewCount.text = PriceFormatter.getInstance()!!.getFormattedValue(quantity)
                }
            })
        }

        purchaseFragment?.arguments = Bundle().apply {
            putString(CastPurchaseFragment.BUNDLE_EXTRA_KEY_CAST_ID, casterId)
            putString(CastPurchaseFragment.BUNDLE_EXTRA_KEY_STREAM_KEY, streamKey)
        }

        transaction.replace(R.id.layoutContainer, purchaseFragment!!, CastPurchaseFragment::class.java.name)
        transaction.commit()
    }

    /**
     * 푸딩현황 리스트 Fragment 를 표시
     */
    private fun replaceCookieFragment() {
        var transaction = childFragmentManager.beginTransaction()

        cookieFragment = childFragmentManager.findFragmentByTag(CastCookieFragment::class.java.name) as? CastCookieFragment
        if (cookieFragment == null) {
            cookieFragment = CastCookieFragment()
            cookieFragment!!.setTotalQuantityListener(object : CastCookieFragment.OnTotalQuantityListener {
                override fun getTotalQuantity(quantity: String) {
                    textViewStatus.text = "방송중 받은 총 젤리 수 : "
                    textViewCount.text = PriceFormatter.getInstance()!!.getFormattedValue(quantity)
                }
            })
        }

        cookieFragment?.arguments = Bundle().apply {
            putString(CastCookieFragment.BUNDLE_EXTRA_KEY_CAST_ID, casterId)
            putString(CastCookieFragment.BUNDLE_EXTRA_KEY_STREAM_KEY, streamKey)
        }

        transaction.replace(R.id.layoutContainer, cookieFragment!!, CastCookieFragment::class.java.name)
        transaction.commit()
    }

    private val tabCheckedChangeListener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        when (checkedId) {
//            R.id.radioButtonFollower    -> replaceFollowerFragment()
            R.id.radioButtonPurchase -> replacePurchaseFragment()
            R.id.radioButtonCookie -> replaceCookieFragment()
        }
    }
}