package com.enliple.pudding.adapter.search

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.*
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.network.vo.API124
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.widget.ScheduleProduct
import kotlinx.android.synthetic.main.adapter_search_schedule_header.view.*
import kotlinx.android.synthetic.main.adapter_search_schedule_item.view.*

/**
 * Created by Kim Joonsung on 2019-03-07.
 */
class SearchScheduleAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> {
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val items: MutableList<API124.ScheduleItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mContext: Context
    private var totalCount = -1
    private var mKeyword = ""

    constructor(context: Context, count: Int) : super() {
        mContext = context
        totalCount = count
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_HEADER -> SearchScheduleHeaderHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_search_schedule_header, parent, false))
                else -> SearchScheduleItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_search_schedule_item, parent, false))
            }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_HEADER -> onBindHeader(holder as SearchScheduleHeaderHolder)
            else -> onBindItem(holder as SearchScheduleItemHolder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (totalCount == 0) {
            when (position) {
                0 -> return TYPE_HEADER
                else -> return TYPE_ITEM
            }
        } else {
            return TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return items.size + (if (totalCount == 0) 1 else 0)
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setItems(list: List<API124.ScheduleItem>) {
        this.items.clear()
        this.items.addAll(list)

        notifyDataSetChanged()
    }

    fun addItems(list: List<API124.ScheduleItem>) {
        if (list.isNotEmpty()) {
            for (i in 0 until list.size) {
                this.items.add(list[i])
            }

            notifyItemInserted(items.size - list.size)
        }
    }

    fun setKeyword(keyword: String) {
        this.mKeyword = keyword
    }

    fun setAlarmCount(count: String, holder: SearchScheduleItemHolder) {
        holder.itemView.textViewAlarmCount.text = "${count}명"
    }

    private fun onBindHeader(holder: SearchScheduleHeaderHolder) {
        holder.itemView.textViewEmpty.text = "'${mKeyword}'"
    }

    private fun onBindItem(holder: SearchScheduleItemHolder, position: Int) {
        val realPosition = if (totalCount == 0) position - 1 else position

        items[realPosition].let { item ->
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

            holder.itemView.textViewRegDate.text = "${item.fo_year}.${item.fo_month}.${item.fo_day} ${item.fo_hour}:00"

            if(item.reservation_cnt.toInt() == 0) {
                holder.itemView.textViewAlarmCount.text = "${item.reservation_cnt}명"
            } else {
                holder.itemView.textViewAlarmCount.text = "${StringUtils.getSnsStyleCountZeroBase(item.reservation_cnt.toInt())}명"
            }

            holder.itemView.title.text = item.title

            if (!item.items.data.isNullOrEmpty()) {
                val price = item.items.data[0].nPrdSellPrice
                val product = item.items.data[0].strPrdName
                if (item.items.nTotalCount == 1) {
                    holder.itemView.price.text = "${PriceFormatter.getInstance()!!.getFormattedValue(price)}원"
                } else {
                    holder.itemView.price.text = "${PriceFormatter.getInstance()!!.getFormattedValue(price)}원~"
                }

                holder.itemView.product.text = product
            }

            if (item.is_mine == "Y") {
                holder.itemView.buttonAlarm.isEnabled = false
            } else {
                if (item.live_type == "empty") {
                    holder.itemView.buttonAlarm.isSelected = if (item.my_alarm == "Y") true else false
                } else {
                    holder.itemView.buttonAlarm.isEnabled = false
                }
            }

            if(item.live_type == "live") {
                holder.itemView.broadcastStatus.visibility = View.VISIBLE
                holder.itemView.label.visibility = View.VISIBLE
            } else {
                holder.itemView.broadcastStatus.visibility = View.GONE
                holder.itemView.label.visibility = View.GONE
            }

            setScheduleProduct(holder, item.items)

            holder.itemView.buttonAlarm.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    v!!.isSelected = !v.isSelected
                    mListener?.onAlarmSet(items[realPosition], v.isSelected, holder)
                }
            })

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onItemClick(items[realPosition])
                }
            })
        }
    }

    private fun setScheduleProduct(holder: SearchScheduleItemHolder, productItems:API124.ScheduleItem.Products) {
        holder.itemView.scheduleProduct.removeAllViews()

        for(i in 0 until productItems.data.size) {
            holder.itemView.scheduleProduct.addView(ScheduleProduct(holder.itemView.context!!, productItems.data[i].strPrdImg))
        }
    }


    class SearchScheduleHeaderHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    class SearchScheduleItemHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClick(item: API124.ScheduleItem)
        fun onAlarmSet(item: API124.ScheduleItem, status: Boolean, holder: SearchScheduleItemHolder)
    }
}