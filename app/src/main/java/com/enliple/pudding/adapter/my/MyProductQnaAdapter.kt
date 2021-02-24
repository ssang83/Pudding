package com.enliple.pudding.adapter.my

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.network.vo.API89
import kotlinx.android.synthetic.main.adapter_product_qna_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-25.
 */
class MyProductQnaAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<MyProductQnaAdapter.ProductQnaHolder>() {

    private val items: MutableList<API89.MyQnAItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductQnaHolder {
        return ProductQnaHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_product_qna_item, parent, false))
    }

    override fun onBindViewHolder(holder: ProductQnaHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.itemView.textViewTitle.text = item.title
            holder.itemView.textViewInquiry.text = "Q.${item.iq_subject}"
            holder.itemView.textViewInquiryDetail.text = item.iq_question

//            Glide.with(holder.itemView.context)
//                    .load(item.image1)
//                    .asBitmap()
//                    .priority(Priority.HIGH)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(holder.itemView.imageViewThumbnail)
            ImageLoad.setImage(holder.itemView.context, holder.itemView.imageViewThumbnail, item.image1, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

            if (item.answer.size > 0) {
                holder.itemView.layoutSeller.visibility = View.VISIBLE

                holder.itemView.textViewSellerDate.text = item.answer.get(0).iq_time
                holder.itemView.textViewSellerReply.text = item.answer.get(0).iq_answer
            } else {
                holder.itemView.layoutSeller.visibility = View.GONE
            }

            holder.itemView.layoutSpread.setOnClickListener {
                holder.itemView.buttonSpread.isSelected = !holder.itemView.buttonSpread.isSelected
                if (holder.itemView.buttonSpread.isSelected) {
                    holder.itemView.layoutReply.visibility = View.VISIBLE
                } else {
                    holder.itemView.layoutReply.visibility = View.GONE
                }
            }

            holder.itemView.textViewReplyView.setOnClickListener {
                holder.itemView.buttonSpread.isSelected = !holder.itemView.buttonSpread.isSelected
                if (holder.itemView.buttonSpread.isSelected) {
                    holder.itemView.layoutReply.visibility = View.VISIBLE
                } else {
                    holder.itemView.layoutReply.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItem(list: List<API89.MyQnAItem>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    private fun getItem(position: Int): API89.MyQnAItem? = if (position in 0..items.size) items[position] else null

    class ProductQnaHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}