package com.enliple.pudding.adapter.my

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API140
import kotlinx.android.synthetic.main.adapter_alarm_item.view.*

/**
 * Created by Kim Joonsung on 2019-03-11.
 */
class AlarmAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<AlarmAdapter.AlarmHolder>() {

    private val mItems:MutableList<API140.NoticeItem> = mutableListOf()
    private var mListener:Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmHolder {
        return AlarmHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_alarm_item, parent, false))
    }

    override fun onBindViewHolder(holder: AlarmHolder, position: Int) {
        mItems[position].let { item ->
            holder.itemView.textViewTitle.text = item.am_msg
            holder.itemView.textViewDate.text = item.am_reg_date

            holder.itemView.buttonDel.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onItemDelete(item, position)
                }
            })
        }
    }

    override fun getItemCount(): Int = mItems.size

    fun setItems(items:List<API140.NoticeItem>) {
        this.mItems.clear()
        this.mItems.addAll(items)

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun deleteItem(position: Int) {
        mItems.removeAt(position)
        notifyDataSetChanged()
    }

    class AlarmHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemDelete(item:API140.NoticeItem, position: Int)
    }
}