package com.enliple.pudding.adapter.my

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import kotlinx.android.synthetic.main.adapter_event_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-22.
 */
class EventAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<EventAdapter.EventListHolder>() {

    private val items: MutableList<Any> = ArrayList()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventListHolder {
        return EventListHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_event_item, parent, false))
    }

    override fun onBindViewHolder(holder: EventListHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewThumbnail,
                    "",
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun getItem(position: Int): Any? = if (position in 0 until items.size) items[position] else null

    fun setItem(list: List<Any>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    class EventListHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(data: Any)
    }
}