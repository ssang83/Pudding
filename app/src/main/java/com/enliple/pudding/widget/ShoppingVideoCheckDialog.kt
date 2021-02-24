package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.enliple.pudding.R
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.adapter_shopping_video_check_item.view.*
import kotlinx.android.synthetic.main.dialog_product_check.*
import java.util.*

/**
 */
class ShoppingVideoCheckDialog : AppCompatDialog {
    companion object {
        const val MODE_RESOLUTION = 1000
        const val MODE_RATIO = 1001

//        const val RESOLUTION_1 = "저화질(240P)"
//        const val RESOLUTION_2 = "일반화질(480P)"
//        const val RESOLUTION_3 = "고화질(720P)"

//        const val RATIO_1 = "원본비율"
//        const val RATIO_2 = "꽉찬화면"
//        const val RATIO_3 = "비율유지"
    }

    private var mListener: ClickListener? = null
    private var mMode = MODE_RATIO

    private lateinit var mAdapter: ProductCheckAdapter

    interface ClickListener {
        fun onSelected(mode: Int, position: Int, data: String)
    }

    constructor(context: Context, mode: Int) : super(context, true, null) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setContentView(R.layout.dialog_product_check)

        mMode = mode
        if (mode == MODE_RESOLUTION) {
            textViewTitle.text = "화질 선택"
        } else {
            textViewTitle.text = "화면비율 선택"
        }

        setCancelable(true)
        setCanceledOnTouchOutside(false)

        buttonCancel.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                dismiss()
            }
        })

        buttonConfirm.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                mListener?.onSelected(mMode, mAdapter.getSelectedPosition(), mAdapter.getSelectedItem())
                dismiss()
            }
        })

        recyclerVeiwProduct.setHasFixedSize(true)

        mAdapter = ProductCheckAdapter()
        recyclerVeiwProduct.adapter = mAdapter

//        val items: MutableList<String> = ArrayList()
//        if (mode == MODE_RATIO) {
//            items.add(RATIO_1)
//            items.add(RATIO_2)
//            items.add(RATIO_3)
//        }
//
//        mAdapter.setItems(items)
//        mAdapter.setSelectedIndex(0)
    }

    fun setData(items: MutableList<String>, defaultIndex: Int) {
        mAdapter.setItems(items)
        mAdapter.setSelectedIndex(defaultIndex)
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun dismiss() {
        if (this.isShowing) {
            super.dismiss()
        }
    }

    fun setListener(listener: ClickListener) {
        mListener = listener
    }

    class ProductCheckHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    private class ProductCheckAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<ProductCheckHolder>() {
        private val mItems: MutableList<String> = ArrayList()

        private var mSelectedPosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductCheckHolder {
            return ProductCheckHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_shopping_video_check_item, parent, false))
        }

        override fun onBindViewHolder(holder: ProductCheckHolder, position: Int) {
            val item = mItems[position]
            if (item != null) {
                holder.itemView.radioBtn.isSelected = mSelectedPosition == position
                holder.itemView.title.text = item

                holder.itemView.setOnClickListener {
                    setSelectedIndex(position)
                }
            } else {
                Logger.e("item null")
            }
        }

        fun setItems(items: MutableList<String>) {
            mItems.clear()
            if (items != null) {
                mItems.addAll(items)
            }

            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = mItems.size

        open fun getSelectedPosition() = mSelectedPosition

        open fun getSelectedItem() = mItems[mSelectedPosition]

        open fun setSelectedIndex(position: Int) {
            var prevItemPosition = mSelectedPosition

            mSelectedPosition = position
            if (prevItemPosition != -1) {
                notifyItemChanged(prevItemPosition)
            }

            if (mSelectedPosition != -1) {
                notifyItemChanged(mSelectedPosition)
            }
        }
    }
}