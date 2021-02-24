package com.enliple.pudding.adapter.home

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.network.vo.API129
import kotlinx.android.synthetic.main.adapter_pudding_beauty_event_item.view.*
import kotlinx.android.synthetic.main.view_schedule_day.view.*

/**
 * Created
 */
class ScheduleDaysAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<ScheduleDaysAdapter.ItemHolder>() {

    companion object {
        const val TODAY_POSITION = 2
    }

    private val mItemList: MutableList<API129.ShowDateItem> = ArrayList()
    private var mListener:Listener? = null
    private var selectedIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_schedule_day, parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = mItemList[position]
        if (item != null) {
            holder.itemView.dayDigit.setTextColor(Color.parseColor("#464646"))

            var day = item.date.split("-")[2]
            if(day.startsWith("0")) {
                day = day.replace("0", "")
            }

            val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            if(day.toInt() < 10) {
                param.leftMargin = Utils.ConvertDpToPx(holder.itemView.context, 4)
                holder.itemView.dayDigit.layoutParams = param
            } else {
                param.leftMargin = Utils.ConvertDpToPx(holder.itemView.context, 0)
                holder.itemView.dayDigit.layoutParams = param
            }

            holder.itemView.dayDigit.text = day
            holder.itemView.day.setTextColor(Color.parseColor("#464646"))
            holder.itemView.day.text = item.dayweek
            holder.itemView.dayLayout.setBackgroundResource(0)

            if("Y" == item.is_following_formation) {
                holder.itemView.myFollowing.visibility = View.VISIBLE

                if(selectedIndex == position) {
                    holder.itemView.myFollowing.setBackgroundResource(R.drawable.bg_schedule_whte)
                } else {
                    holder.itemView.myFollowing.setBackgroundResource(R.drawable.bg_schedule_day)
                }
            } else {
                holder.itemView.myFollowing.visibility = View.GONE
            }

            if (item.today == "Y") {
                holder.itemView.dayDigit.setTextColor(Color.parseColor("#ffffff"))

                holder.itemView.day.setTextColor(Color.parseColor("#ffffff"))
                holder.itemView.day.text = "오늘"
                holder.itemView.dayLayout.setBackgroundResource(R.drawable.bg_schedule_day)
            }

            holder.itemView.setOnClickListener {
                mListener?.onDateClicked(item, holder.itemView, position)

                selectedIndex = position
                notifyDataSetChanged()
            }
        }

        if(selectedIndex != -1) {
            if(selectedIndex == position) {
                holder.itemView.dayDigit.setTextColor(Color.parseColor("#ffffff"))
                holder.itemView.day.setTextColor(Color.parseColor("#ffffff"))
                holder.itemView.dayLayout.setBackgroundResource(R.drawable.bg_schedule_day)
            } else {
                holder.itemView.dayDigit.setTextColor(Color.parseColor("#464646"))
                holder.itemView.day.setTextColor(Color.parseColor("#464646"))
                holder.itemView.dayLayout.setBackgroundResource(0)
            }
        }
    }

    override fun getItemCount(): Int = mItemList.size

    fun setItems(list: List<API129.ShowDateItem>) {
        this.mItemList.clear()
        this.mItemList.addAll(list)

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setToday() {
        selectedIndex = TODAY_POSITION
        notifyDataSetChanged()
    }

    fun setDate(position: Int) {
        selectedIndex = position
        notifyDataSetChanged()
    }

    fun getData(position: Int) : API129.ShowDateItem? = if(position in 0 until mItemList.size) mItemList[position] else null

    inner class ItemHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onDateClicked(dateItem:API129.ShowDateItem, view: View, position: Int)
    }
}