package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import android.view.View
import android.view.Window
import com.enliple.pudding.R
import com.enliple.pudding.adapter.my.ProductCheckAdapter
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API39
import kotlinx.android.synthetic.main.dialog_product_check.*

/**
 * Created by Kim Joonsung on 2018-11-02.
 */
class ProductCheckDialog : AppCompatDialog, ProductCheckAdapter.Listener {

    companion object {
        private const val TAG = "ProductCheckDialog"
    }

    private var selectProduct: API39.MyOrderData? = null
    private val items: MutableList<API39.MyOrderData> = ArrayList()

    private var canDismissOnBackPressed: Boolean = false
    private var mListener: Listener? = null

    private lateinit var mAdapter: ProductCheckAdapter

    constructor(context: Context, items: List<API39.MyOrderData>) : super(context, true, null) {
        this.items.addAll(items)
        if (items != null) {
            Logger.e("items.size :: ${items.size}")

            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            setContentView(R.layout.dialog_product_check)

            setCancelable(true)
            setCanceledOnTouchOutside(false)

            buttonCancel.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    dismiss()
                }
            })

            buttonConfirm.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onProductItem(selectProduct!!)
                    dismiss()
                }
            })

            recyclerVeiwProduct.setHasFixedSize(true)

            mAdapter = ProductCheckAdapter()
            mAdapter.setListener(this@ProductCheckDialog)
            recyclerVeiwProduct.adapter = mAdapter

            loadData()
        } else {
            Logger.e("item is null")
        }
    }

//    init {
//        Logger.e("init")
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        setContentView(R.layout.dialog_product_check)
//
//        setCancelable(true)
//        setCanceledOnTouchOutside(false)
//
//        buttonCancel.setOnClickListener(object : OnSingleClickListener() {
//            override fun onSingleClick(v: View?) {
//                dismiss()
//            }
//        })
//
//        buttonConfirm.setOnClickListener(object : OnSingleClickListener() {
//            override fun onSingleClick(v: View?) {
//                mListener?.onProductItem(selectProduct!!)
//                dismiss()
//            }
//        })
//
//        recyclerVeiwProduct.setHasFixedSize(true)
//
//        mAdapter = ProductCheckAdapter()
//        mAdapter.setListener(this@ProductCheckDialog)
//        recyclerVeiwProduct.adapter = mAdapter
//
//        loadData()
//    }

    override fun onBackPressed() {
        if (canDismissOnBackPressed) {
            dismiss()
        }
    }

    override fun dismiss() {
        if (this.isShowing) {
            super.dismiss()
        }
    }

    override fun onSelectProduct(position: Int) {
        mAdapter?.setSelectedIndex(position)
        selectProduct = items[position]
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    private fun loadData() {
        Logger.e("loadData items size :: ${items.size}")
        mAdapter.setItems(items)
    }

    interface Listener {
        fun onProductItem(item: API39.MyOrderData)
    }
}