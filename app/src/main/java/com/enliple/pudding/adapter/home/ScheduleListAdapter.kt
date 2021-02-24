package com.enliple.pudding.adapter.home

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API130
import com.enliple.pudding.widget.ScheduleProduct
//import com.kakao.s2.StringSet.count
import kotlinx.android.synthetic.main.adapter_schedule_list_item.view.*

/**
 * Created by jhs
 */
class ScheduleListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> {
    private val mItems: MutableList<API130.ReservationItem> = mutableListOf()
    private var mListener: Listener? = null

    private lateinit var mContext: Context

    constructor(context: Context) : super() {
        if (context != null) {
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_schedule_list_item, parent, false))

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        onBindItem(holder as ItemHolder, position)
    }

    override fun getItemViewType(position: Int) = 0

    override fun getItemCount() = mItems.size

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setItems(list: List<API130.ReservationItem>) {
        this.mItems.clear()
        this.mItems.addAll(list)

        notifyDataSetChanged()
    }

    fun deleteReservation(position: Int) {
        mItems.removeAt(position)
        notifyDataSetChanged()
    }

    private fun onBindItem(holder: ItemHolder, position: Int) {
        Logger.d("onBindItem $position")

        var item = getItem(position)

        ImageLoad.setImage(
                holder.itemView.context,
                holder.itemView.thumbnail,
                item!!.live_img,
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

        val d = mContext.getDrawable(R.drawable.bg_rounding_image) as GradientDrawable
        holder.itemView.thumbnail.background = d
        holder.itemView.thumbnail.clipToOutline = true

        holder.itemView.title.text = item?.title
        holder.itemView.date.text = "${item?.fo_year}.${item?.fo_month}.${item?.fo_day} ${item?.fo_hour}:00"
        holder.itemView.alarmCount.text = "${item?.reservation_cnt}명"
        holder.itemView.casterName.text = item?.mb_nick


        if(item.items != null && !item.items.data.isNullOrEmpty()) {
            val price = PriceFormatter.getInstance()!!.getFormattedValue(item.items.data[0].nPrdSellPrice)
            val productTitle = item.items.data[0].strPrdName
            if(item.items.nTotalCount == 1) {
                holder.itemView.productPrice.text = "${price}원"
            } else {
                holder.itemView.productPrice.text = "${price}원~"
            }

            holder.itemView.product.text = productTitle
        }

        if (item?.live_possible == "Y") {
            holder.itemView.goBroadcast.visibility = View.VISIBLE
        } else {
            holder.itemView.goBroadcast.visibility = View.GONE
        }

        setReservationStatus(item?.fo_status, holder, item)
        setScheduleProduct(holder, item?.items)

        holder.itemView.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (item != null) {
                    mListener?.onItemClicked(item, position)
                }
            }
        })

        holder.itemView.textViewModify.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (item != null) {
                    mListener?.onReservationModify(item, position)
                }
            }
        })

        holder.itemView.textViewCancel.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (item != null) {
                    mListener?.onReservationCancel(item, position)
                }
            }
        })

        holder.itemView.goBroadcast.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (item != null) {
                    mListener?.onGoBroadCast(item)
                }
            }
        })
    }

    private fun getItem(position: Int): API130.ReservationItem? = if (position in 0 until mItems.size) mItems[position] else null

    private fun setReservationStatus(status: String, holder: ItemHolder, item:API130.ReservationItem) {
        if (status == "0") {
            holder.itemView.status.text = "승인대기중"
            holder.itemView.status.setTextColor(Color.parseColor("#9f56f2"))
            holder.itemView.alarm.visibility = View.GONE
        } else if (status == "1") {
            holder.itemView.status.text = "승인완료"
            holder.itemView.status.setTextColor(Color.parseColor("#ff6c6c"))
            holder.itemView.alarm.visibility = View.VISIBLE
        } else {
            holder.itemView.status.text = "승인거절 : ${item.cancel_reason}"
            holder.itemView.status.setTextColor(Color.parseColor("#192028"))
            holder.itemView.alarm.visibility = View.GONE
        }
    }


    private fun setScheduleProduct(holder: ItemHolder, productItems: API130.ReservationItem.Products) {
        holder.itemView.scheduleProduct.removeAllViews()

        for (i in 0 until productItems.data.size) {
            holder.itemView.scheduleProduct.addView(ScheduleProduct(mContext, productItems.data[i].strPrdImg))
        }
    }

    inner class ItemHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: API130.ReservationItem, position: Int)
        fun onReservationModify(item: API130.ReservationItem, position: Int)
        fun onReservationCancel(item: API130.ReservationItem, position: Int)
        fun onGoBroadCast(item: API130.ReservationItem)
    }
}