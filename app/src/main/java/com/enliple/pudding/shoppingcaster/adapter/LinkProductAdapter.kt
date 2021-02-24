package com.enliple.pudding.shoppingcaster.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.network.vo.API137
import kotlinx.android.synthetic.main.adapter_link_product_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-01.
 */
class LinkProductAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<LinkProductAdapter.LinkProductHolder>() {

    private val items: MutableList<API137.MobionItem> = mutableListOf()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkProductHolder {
        return LinkProductHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_link_product_item, parent, false))
    }

    override fun onBindViewHolder(holder: LinkProductHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.itemView.textViewShopName.text = item.ad_title
            holder.itemView.textViewAdvertisingPayment.text = "${String.format(holder.itemView.context.getString(R.string.msg_price_format),
                    PriceFormatter.getInstance()!!.getFormattedValue(item.ad_usemoney.toInt()))}"

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int): API137.MobionItem? = if (position in 0 until items.size) items[position] else null

    fun setItem(items: List<API137.MobionItem>) {
        this.items.clear()
        if (items != null) {
            this.items.addAll(items)
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    class LinkProductHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: API137.MobionItem)
    }
}