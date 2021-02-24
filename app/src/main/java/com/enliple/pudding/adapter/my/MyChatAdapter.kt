package com.enliple.pudding.adapter.my

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API63
import kotlinx.android.synthetic.main.adapter_chat_received_item.view.*
import kotlinx.android.synthetic.main.adapter_chat_sender_item.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Kim Joonsung on 2018-10-23.
 */
class MyChatAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> {

    companion object {
        private const val VIEW_TYPE_SEND = 0
        private const val VIEW_TYPE_RECEIVED = 1
    }

    private val items: MutableList<API63.MyMessageItem> = ArrayList()
    private val mContext: Context

    constructor(context: Context) {
        this.mContext = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_SEND -> {
                return SentMessageHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_chat_sender_item, parent, false))
            }

            else -> {
                return ReceivedMessageHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_chat_received_item, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_SEND -> onSentMessageBind(holder as SentMessageHolder, position)
            VIEW_TYPE_RECEIVED -> onReceivedMessageBind(holder as ReceivedMessageHolder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        if (item!!.send_mb_id == AppPreferences.getUserId(mContext)) {
            return VIEW_TYPE_SEND
        } else {
            return VIEW_TYPE_RECEIVED
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<API63.MyMessageItem>) {
        this.items.clear()

        if (items != null) {
            this.items.addAll(items)
        }

        notifyDataSetChanged()
    }

    fun addItems(items: List<API63.MyMessageItem>, moreMessage: Boolean) {
        if (moreMessage) {
            // 신규 메시지를 추가한다.
            for (i in 0 until items.size) {
                this.items.add(items[i])
            }

            notifyItemInserted(this.items.size - items.size)
        } else {
            // 이전 메시지를 추가한다.
            this.items.addAll(0, items)
            notifyItemRangeInserted(0, items.size)
        }
    }

    fun getItem(position: Int): API63.MyMessageItem? = if (position in 0 until items.size) items[position] else null

    private fun onSentMessageBind(holder: SentMessageHolder, position: Int) {
        val item = getItem(position)

        var prevDate = ""
        if (position != 0) {
            val prevItem = getItem(position - 1)
            val prevDateArray = prevItem!!.datetime.split(" ")
            val prevDateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val prevSdf = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
            prevDate = prevSdf.format(prevDateSdf.parse(prevDateArray[0]))
        }

        if (item != null) {
            val dateArray = item.datetime.split(" ")  // 2018-12-18 17:28:36
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val sdf = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
            val currentDate = sdf.format(date.parse(dateArray[0]))

            if (prevDate == currentDate) {
                holder.itemView.textViewDate.visibility = View.GONE
            } else {
                holder.itemView.textViewDate.visibility = View.VISIBLE
                holder.itemView.textViewDate.text = currentDate
            }

            holder.itemView.textViewTime.text = dateArray[1].substring(0, dateArray[1].length - 3)
            holder.itemView.textViewContent.text = item.content

            if (!TextUtils.isEmpty(item.send_mb_img)) {
                ImageLoad.setImage(holder.itemView.context,
                        holder.itemView.imageViewThumbnail,
                        item.send_mb_img,
                        null,
                        ImageLoad.SCALE_CIRCLE_CROP,
                        DiskCacheStrategy.ALL)
            } else {
                holder.itemView.imageViewThumbnail.setBackgroundResource(R.drawable.profile_default_img)
            }
        }
    }

    private fun onReceivedMessageBind(holder: ReceivedMessageHolder, position: Int) {
        val item = getItem(position)

        var prevDate = ""
        if (position != 0) {
            val prevItem = getItem(position - 1)
            val prevDateArray = prevItem!!.datetime.split(" ")
            val prevDateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val prevSdf = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
            prevDate = prevSdf.format(prevDateSdf.parse(prevDateArray[0]))
        }

        if (item != null) {
            val dateArray = item.datetime.split(" ")  // 2018-12-18 17:28:36
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val sdf = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
            val currentDate = sdf.format(date.parse(dateArray[0]))

            if (prevDate == currentDate) {
                holder.itemView.textViewReceiveDate.visibility = View.GONE
            } else {
                holder.itemView.textViewReceiveDate.visibility = View.VISIBLE
                holder.itemView.textViewReceiveDate.text = currentDate
            }

            holder.itemView.textViewReceiveTime.text = dateArray[1].substring(0, dateArray[1].length - 3)
            holder.itemView.textViewReceiveContent.text = item.content
            Logger.e("message thumb :: " + item.send_mb_img);
            if (!TextUtils.isEmpty(item.send_mb_img)) {
                ImageLoad.setImage(holder.itemView.context,
                        holder.itemView.imageViewReceiveThumbnail,
                        item.send_mb_img,
                        null,
                        ImageLoad.SCALE_CIRCLE_CROP,
                        DiskCacheStrategy.ALL)
            } else {
                holder.itemView.imageViewReceiveThumbnail.setBackgroundResource(R.drawable.profile_default_img)
            }
        }
    }

    class SentMessageHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    class ReceivedMessageHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}