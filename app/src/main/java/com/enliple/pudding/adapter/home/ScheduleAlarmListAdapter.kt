package com.enliple.pudding.adapter.home

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.CategoryModel
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API131
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.widget.ScheduleProduct
//import com.kakao.s2.StringSet.count
import kotlinx.android.synthetic.main.adapter_schedule_alarm_item.view.*


/**
 * Created by jhs
 */
class ScheduleAlarmListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> {
    private val mItems: MutableList<API131.WatchItem> = mutableListOf()
    private var mCategoryItem: MutableList<API81.CategoryItem> = mutableListOf()
    private var mListener: Listener? = null

    private lateinit var mContext: Context

    constructor(context: Context) : super() {
        if (context != null) {
            mCategoryItem = CategoryModel.getCategoryList(context, "all")
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_schedule_alarm_item, parent, false))

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        onBindItem(holder as ItemHolder, position)
    }

    override fun getItemViewType(position: Int) = 0

    override fun getItemCount(): Int = mItems.size

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setItems(list: List<API131.WatchItem>) {
        this.mItems.clear()
        this.mItems.addAll(list)

        notifyDataSetChanged()
    }

    fun deleteAlarm(position: Int) {
        try {
            mItems.removeAt(position)

            notifyDataSetChanged()
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

    private fun onBindItem(holder: ItemHolder, position: Int) {
        Logger.d("onBindItem $position")

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
                    item.mb_user_img,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            val d = mContext.getDrawable(R.drawable.bg_rounding_image) as GradientDrawable
            holder.itemView.thumbnail.background = d
            holder.itemView.thumbnail.clipToOutline = true

            holder.itemView.title.text = item.title
            holder.itemView.date.text = "${item.fo_year}.${item.fo_month}.${item.fo_day} ${item.fo_hour}:00"
            holder.itemView.alarmCount.text = "${item.reservation_cnt}명"
            holder.itemView.casterName.text = item.mb_nick

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

            if(item.my_alarm == "Y") {
                holder.itemView.alarmSwitch.isSelected = true
            } else {
                holder.itemView.alarmSwitch.isSelected = false
            }

            when (item.live_type) {
                "lastlive" -> {
                    holder.itemView.broadcastStatus.visibility = View.GONE
                    holder.itemView.label.visibility = View.GONE
                }

                "live" -> {
                    holder.itemView.broadcastStatus.visibility = View.VISIBLE
                    holder.itemView.label.visibility = View.VISIBLE
                }

                else -> {
                    holder.itemView.broadcastStatus.visibility = View.GONE
                    holder.itemView.label.visibility = View.GONE
                }
            }

            setScheduleProduct(holder, item.items)

            holder.itemView.alarmSwitch.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onAlarmDel(item, position)
                }
            })

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onItemClicked(item, position)
                }
            })
        }
    }

    private fun setScheduleProduct(holder: ItemHolder, productItems: API131.WatchItem.Products) {
        holder.itemView.scheduleProduct.removeAllViews()

        for (i in 0 until productItems.data.size) {
            holder.itemView.scheduleProduct.addView(ScheduleProduct(mContext, productItems.data[i].strPrdImg))
        }
    }

    inner class ItemHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onAlarmDel(item: API131.WatchItem, position: Int)
        fun onItemClicked(item: API131.WatchItem, position: Int)
    }
}