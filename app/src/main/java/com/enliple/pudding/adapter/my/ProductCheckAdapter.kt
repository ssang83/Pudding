package com.enliple.pudding.adapter.my

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API39
import kotlinx.android.synthetic.main.adapter_product_check_item.view.*

/**
 * Created by Kim Joonsung on 2018-11-02.
 */
class ProductCheckAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<ProductCheckAdapter.ProductCheckHolder>() {

    companion object {
        private const val TAG = "ProductCheckAdapter"
    }

    private val items: MutableList<API39.MyOrderData> = ArrayList()
    private var mListener: Listener? = null

    // MultiSelect 관련
    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductCheckHolder {
        return ProductCheckHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_product_check_item, parent, false))
    }

    override fun onBindViewHolder(holder: ProductCheckHolder, position: Int) {
        Logger.e("onBindViewHolder")
        val item = getItem(position)

        if (item != null) {
            holder.itemView.buttonRadio.isSelected = selectedPosition == position
            var time = ""
            if ( item.time != null ) {
                var time_arr = item.time.split(" ")
                if ( time_arr != null ) {
                    Logger.e("time_arr size :: ${time_arr.size}")
                    if ( time_arr.size > 0 )
                        time = time_arr[0]
                }
            }

            holder.itemView.textViewOrderNumber.text = "$time - 주문번호 ${item.order_id}"
            holder.itemView.textViewTitle.text = item.item_name
            Logger.e("order no str :: ${holder.itemView.textViewOrderNumber.text}")
            Logger.e("title str :: ${holder.itemView.textViewTitle.text}")
            holder.itemView.setOnClickListener {
                mListener?.onSelectProduct(position)
            }
        } else {
            Logger.e("item null")
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list: MutableList<API39.MyOrderData>) {
        this.items.clear()
        if (list != null) {
            Logger.e("ProductCheckAdapter setItems list size :: " + list.size)
            this.items.addAll(list)
            Logger.e("ProductCheckAdapter setItems item size :: " + items.size)
        } else {
            Logger.e("ProductCheckAdapter setItems item null")
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    /**
     * 해당 포지션의 선택상태를 설정
     * @param position              선택 상태가 변경되는 Position
     * @param selected              다중선택시 선택상태(단일 상태에서는 항상 true)
     */
    open fun setSelectedIndex(position: Int) {
        Logger.e(TAG, "setSelectedIndex - position : $position")
        var releaseItemPosition: Int = selectedPosition

        selectedPosition = position
        if (releaseItemPosition != -1) {
            Logger.d(TAG, "Item last selected status changed : previous $releaseItemPosition, present $selectedPosition")
            notifyItemChanged(releaseItemPosition)
        }

        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition)
        }
    }

    private fun getItem(position: Int): API39.MyOrderData? = if (position in 0..items.size) items[position] else null

    class ProductCheckHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onSelectProduct(position: Int)
    }
}