package com.enliple.pudding.adapter.my

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.network.vo.API40
import kotlinx.android.synthetic.main.adapter_inquiry_footer_item.view.*
import kotlinx.android.synthetic.main.adapter_inquiry_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-30.
 */
class InquriyAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_FOOTER = 1
    }

    private val items: MutableList<API40.MyQAData> = ArrayList()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                return InquiryHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_inquiry_item, parent, false))
            }

            else -> {
                return InquiryFooterHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_inquiry_footer_item, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_ITEM -> onItemBind(holder as InquiryHolder, position)
            VIEW_TYPE_FOOTER -> onFooterBind(holder as InquiryFooterHolder)
        }
    }

    override fun getItemCount(): Int = items.size + 1

    override fun getItemViewType(position: Int): Int {
        if (position == items.size) {
            return VIEW_TYPE_FOOTER
        } else {
            return VIEW_TYPE_ITEM
        }
    }

    fun setItems(list: List<API40.MyQAData>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    private fun getItem(position: Int): API40.MyQAData? = if (position in 0..items.size) items[position] else null

    private fun onItemBind(holder: InquiryHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            if (item.status.equals("1")) {
                holder.itemView.imageViewInquiryStatus.setBackgroundResource(R.drawable.complet_label)
            } else {
                holder.itemView.imageViewInquiryStatus.setBackgroundResource(R.drawable.register_label)
            }

            holder.itemView.textViewDate.text = item.time
            holder.itemView.textViewInquiryType.text = item.category // 카테고리
            holder.itemView.textViewInquiry.text = item.con
            holder.itemView.textViewInquiryDetail.text = item.con
            holder.itemView.textViewReply.text = item.answerCon
            holder.itemView.textViewReplyDate.text = item.answerTime

            holder.itemView.layoutSpread.setOnClickListener {
                holder.itemView.buttonSpread.isSelected = !holder.itemView.buttonSpread.isSelected
                if (holder.itemView.buttonSpread.isSelected) {
                    if (item.status.equals("1")) {
                        holder.itemView.layoutReply.visibility = View.VISIBLE
                        holder.itemView.layoutInquiry.visibility = View.VISIBLE
                        holder.itemView.divider.visibility = View.GONE
                    } else {
                        holder.itemView.layoutReply.visibility = View.GONE
                        holder.itemView.layoutInquiry.visibility = View.VISIBLE
                        holder.itemView.divider.visibility = View.GONE
                    }
                } else {
                    holder.itemView.layoutReply.visibility = View.GONE
                    holder.itemView.layoutInquiry.visibility = View.GONE
                    holder.itemView.divider.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun onFooterBind(holder: InquiryFooterHolder) {
        holder.itemView.layoutMore.setOnClickListener {
            mListener?.onMoreClicked()
        }
    }

    class InquiryHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    class InquiryFooterHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onMoreClicked()
    }
}