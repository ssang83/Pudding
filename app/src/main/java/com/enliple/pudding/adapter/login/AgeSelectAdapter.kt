package com.enliple.pudding.adapter.login

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.age_select_item.view.*

class AgeSelectAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<AgeSelectAdapter.ProductCheckHolder>() {

    companion object {
        private const val TAG = "AgeSelectAdapter"
    }

    private val items: MutableList<String> = ArrayList()
    private var mListener: Listener? = null

    // MultiSelect 관련
    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductCheckHolder {
        return ProductCheckHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.age_select_item, parent, false))
    }

    override fun onBindViewHolder(holder: ProductCheckHolder, position: Int) {
        Logger.e("onBindViewHolder")
        val item = getItem(position)

        if (item != null) {
//            holder.itemView.buttonRadio.isSelected = selectedPosition == position

            if ( position == selectedPosition ) {
                holder.itemView.buttonRadio.setBackgroundResource(R.drawable.radio_btn_on)
            } else {
                holder.itemView.buttonRadio.setBackgroundResource(R.drawable.radio_btn_off)
            }

            holder.itemView.age.text = "$item"
            holder.itemView.setOnClickListener {
                mListener?.onSelectProduct(position, false)
            }
//            holder.itemView.buttonRadio.setOnClickListener {
//                mListener?.onSelectProduct(position, false)
//            }
        } else {
            Logger.e("item null")
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list: MutableList<String>, sAge: String) {
        if ( items == null )
            return
        this.items.clear()
        if (list != null) {
            Logger.e("AgeSelectAdapter setItems list size :: " + list.size)
            this.items.addAll(list)
            Logger.e("AgeSelectAdapter setItems item size :: " + items.size)
        } else {
            Logger.e("AgeSelectAdapter setItems item null")
        }
        if ( !TextUtils.isEmpty(sAge) )
            mListener?.onSelectProduct(getSelectedPosition(sAge!!), true)
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

    private fun getItem(position: Int): String? = if (position in 0..items.size) items[position] else null

    class ProductCheckHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onSelectProduct(position: Int, isInit: Boolean)
    }

    fun getSelectedPosition(age: String): Int {
        var position = 0
        for ( i in 0.. items.size ) {
            if ( age == items.get(i) )
                return position
                position ++
        }
        return position
    }
}