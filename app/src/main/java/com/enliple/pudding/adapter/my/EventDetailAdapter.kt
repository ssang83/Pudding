package com.enliple.pudding.adapter.my

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R

/**
 * Created by Kim Joonsung on 2018-10-22.
 */
class EventDetailAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<EventDetailAdapter.EventDetailListHolder>() {

    private val items: MutableList<Any> = ArrayList()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventDetailListHolder {
        return EventDetailListHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_event_detail_item, parent, false))
    }

    override fun onBindViewHolder(holder: EventDetailListHolder, position: Int) {
        val item = getItem(position)

/*        if(item != null) {
            holder.itemView.textViewTitle.text = item.title
            holder.itemView.textViewDiscountRate.text = "${item.dcRate}%"
            holder.itemView.textViewPrice.text = "${String.format(holder.itemView.context.getString(R.string.msg_price_format),
                    PriceFormatter.getInstance()!!.getFormattedValue(item.price))}"

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewEvent,
                    item.image,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }*/
    }

    override fun getItemCount(): Int = items.size

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setItem(list: List<Any>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    fun getItem(position: Int): Any? = if (position in 0 until items.size) items[position] else null

    class EventDetailListHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(data: Any)
    }
}