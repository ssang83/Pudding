package com.enliple.pudding.adapter.my

import android.graphics.Color
import android.text.TextUtils
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.network.vo.API102
import kotlinx.android.synthetic.main.adapter_item_point_history_content.view.*

/**
 * Created by Kim Joonsung on 2019-01-03.
 */
class PointSaveAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<PointSaveAdapter.PointSaveHolder>() {

    private val items: MutableList<API102.SaveItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointSaveHolder {
        return PointSaveHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_item_point_history_content, parent, false))
    }

    override fun onBindViewHolder(holder: PointSaveHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.itemView.textViewStatus.text = item.status
            holder.itemView.textViewPointAmount.text =
                    String.format(holder.itemView.context.getString(R.string.msg_price_format),
                            PriceFormatter.getInstance()!!.getFormattedValue(item.point))
            holder.itemView.textViewPointAmount.setTextColor(Color.parseColor("#9f56f2"))
            holder.itemView.textViewDate.text = item.reg_date

            if(!TextUtils.isEmpty(item.od_id)) {
                holder.itemView.textViewAnnotation.text = "주문번호 ${item.od_id}"
            } else {
                holder.itemView.textViewAnnotation.text = item.reg_date
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItem(list: List<API102.SaveItem>) {
        this.items.clear()

        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    fun getItem(position: Int): API102.SaveItem? = if (position in 0 until items.size) items[position] else null

    class PointSaveHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}