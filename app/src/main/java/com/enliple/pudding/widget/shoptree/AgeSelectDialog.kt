package com.enliple.pudding.widget.shoptree


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import androidx.appcompat.app.AppCompatDialog
import android.view.View
import android.view.Window
import com.enliple.pudding.R
import com.enliple.pudding.adapter.login.AgeSelectAdapter
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.dialog_age_select.*

/**
 * Created by Kim Joonsung on 2018-11-02.
 */
class AgeSelectDialog : AppCompatDialog, AgeSelectAdapter.Listener {

    companion object {
        private const val TAG = "AgeSelectDialog"
    }

    private var selectProduct: String? = null
    private val items: MutableList<String> = ArrayList()

    private var canDismissOnBackPressed: Boolean = false
    private var mListener: Listener? = null
    private var selectedAge: String ? = ""
    private lateinit var mAdapter: AgeSelectAdapter

    constructor(context: Context, items: List<String>, sAge: String, listener: Listener) : super(context, true, null) {
        this.items.addAll(items)
        if (items != null) {
            Logger.e("items.size :: ${items.size}")

            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            setContentView(R.layout.dialog_age_select)

            setCancelable(true)
            setCanceledOnTouchOutside(false)
            if ( !TextUtils.isEmpty(sAge) )
                selectedAge = sAge
            mListener = listener

            buttonCancel.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    dismiss()
                }
            })

            buttonConfirm.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    if(selectProduct != null) {
                        mListener?.onProductItem(selectProduct!!)
                        dismiss()
                    }
                }
            })

            recyclerVeiwProduct.setHasFixedSize(true)

            mAdapter = AgeSelectAdapter()
            mAdapter.setListener(this@AgeSelectDialog)
            recyclerVeiwProduct.adapter = mAdapter

            loadData(selectedAge!!)
        } else {
            Logger.e("item is null")
        }
    }

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

    override fun onSelectProduct(position: Int, isInit: Boolean) {
        mAdapter?.setSelectedIndex(position)
        selectProduct = items[position]
        if(selectProduct == null) selectProduct = ""
        if ( isInit )
            recyclerVeiwProduct.scrollToPosition(position)
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    private fun loadData(selectedAge: String) {
        Logger.e("loadData items size :: ${items.size}")
        mAdapter.setItems(items, selectedAge)
    }

    interface Listener {
        fun onProductItem(item: String)
    }
}