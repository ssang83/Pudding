package com.enliple.pudding.adapter.promotion

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.network.vo.API11
import kotlinx.android.synthetic.main.adapter_promotion_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-22.
 */
class PromotionAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder>() {

    private val items: MutableList<API11.EventsLisItem> = mutableListOf()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionViewHolder {
        return PromotionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_promotion_item, parent, false))
    }

    override fun onBindViewHolder(holder: PromotionViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.itemView.imageViewPromotion.setRatio(item.main_img_width.toFloat() / item.main_img_height.toFloat())

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewPromotion,
                    item!!.main_img,
                    null,
                    ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setItem(list: List<API11.EventsLisItem>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    fun getItem(position: Int): API11.EventsLisItem? = if (position in 0 until items.size) items[position] else null

    class PromotionViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(data: API11.EventsLisItem)
    }
}