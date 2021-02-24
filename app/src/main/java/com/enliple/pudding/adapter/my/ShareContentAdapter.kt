package com.enliple.pudding.adapter.my

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.network.vo.API105
import kotlinx.android.synthetic.main.adapter_share_list_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-25.
 */
class ShareContentAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_FOOTER = 0
    private val TYPE_ITEM = 1

    private val items: MutableList<API105.SharedItem> = mutableListOf()
    private var mListener: Listener? = null
    private var type: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == TYPE_ITEM) {
            return ShareContentHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_share_list_item, parent, false))
        } else {
            return ShareListFooterHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_share_list_footer, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == TYPE_ITEM) {
            onBindItem(holder as ShareContentHolder, position)
        }
    }

    override fun getItemCount(): Int {
        return if (items == null) 0 else items.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size) {
            TYPE_FOOTER
        } else {
            TYPE_ITEM
        }
    }

    private fun onBindItem(holder: ShareContentHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.itemView.textViewTitle.text = item.title

//            if (type) {
//                holder.itemView.layoutSharedUser.visibility = View.VISIBLE
//                holder.itemView.textViewUserCnt.text = " ${item.share_user_cnt}명"
//            } else {
//                holder.itemView.layoutSharedUser.visibility = View.GONE
//            }

            holder.itemView.layoutSharedUser.visibility = View.VISIBLE
            holder.itemView.textViewUserCnt.text = " ${item.share_user_cnt}명"

            holder.itemView.textViewAmountPrice.text =
                    " ${String.format(holder.itemView.context.getString(R.string.msg_price_format),
                            PriceFormatter.getInstance()!!.getFormattedValue(item.save_price))}"

            holder.itemView.textViewProductSalePrice.text =
                    " ${String.format(holder.itemView.context.getString(R.string.msg_price_format),
                            PriceFormatter.getInstance()!!.getFormattedValue(item.order_price))}"

            holder.itemView.textViewProductClick.text = " ${item.click}"

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewThumbnail,
                    item.thumb,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    fun getItem(position: Int): API105.SharedItem? = if (position in 0 until items.size) items[position] else null

    fun setItem(list: List<API105.SharedItem>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setListType(type: Boolean) {
        this.type = type
    }

    class ShareContentHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    internal class ShareListFooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: API105.SharedItem)
    }
}