package com.enliple.pudding.adapter.home

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.network.vo.API130
import com.enliple.pudding.widget.ScheduleProduct
import kotlinx.android.synthetic.main.adapter_schedule_prev_list_item.view.*

/**
 * Created by Kim Joonsung on 2019-05-09.
 */
class SchedulePrevListAdapter : RecyclerView.Adapter<SchedulePrevListAdapter.SchedulePrevHolder>() {

    private val mItems:MutableList<API130.PreviousItem> = mutableListOf()
    private var mListener:Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            SchedulePrevHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_schedule_prev_list_item, parent, false))

    override fun onBindViewHolder(holder: SchedulePrevHolder, position: Int) {
        mItems[position].let { item ->
            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.thumbnail,
                    item.live_img,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.profile,
                    item!!.mb_user_img,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            val context = holder.itemView.context
            val d = context.getDrawable(R.drawable.bg_rounding_image) as GradientDrawable
            holder.itemView.thumbnail.background = d
            holder.itemView.thumbnail.clipToOutline = true

            holder.itemView.title.text = item.title
            holder.itemView.date.text = "${item.fo_year}.${item.fo_month}.${item.fo_day} ${item.fo_hour}:00"
            holder.itemView.alarmCount.text = "${item.reservation_cnt}명"
            holder.itemView.casterName.text = item?.mb_nick

            if(!item.items.data.isNullOrEmpty()) {
                val price = PriceFormatter.getInstance()!!.getFormattedValue(item.items.data[0].nPrdSellPrice)
                val productTitle = item.items.data[0].strPrdName
                if(item.items.nTotalCount == 1) {
                    holder.itemView.productPrice.text = "${price}원"
                } else {
                    holder.itemView.productPrice.text = "${price}원~"
                }

                holder.itemView.product.text = productTitle
            }

            if(item.fo_status == "2") {
                holder.itemView.broadcastStatus.visibility = View.VISIBLE
                holder.itemView.status.text = "승인거절 : ${item.cancel_reason}"
            } else {
                holder.itemView.broadcastStatus.visibility = View.GONE
            }

            setScheduleProduct(holder, item.items)

            holder.itemView.delete.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onDelete(item, position)
                }
            })
        }
    }

    override fun getItemCount() = mItems.size

    fun setItems(items:List<API130.PreviousItem>) {
        this.mItems.clear()
        this.mItems.addAll(items)

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun deleteReservation(position: Int) {
        mItems.removeAt(position)
        notifyDataSetChanged()
    }

    private fun setScheduleProduct(holder: SchedulePrevHolder, productItems: API130.PreviousItem.Products) {
        holder.itemView.scheduleProduct.removeAllViews()

        for (i in 0 until productItems.data.size) {
            holder.itemView.scheduleProduct.addView(ScheduleProduct(holder.itemView.context, productItems.data[i].strPrdImg))
        }
    }

    class SchedulePrevHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onDelete(item:API130.PreviousItem, position:Int)
    }
}