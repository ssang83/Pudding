package com.enliple.pudding.adapter.my

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API54
import kotlinx.android.synthetic.main.adapter_message_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-23.
 */
class MessageAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private val items: MutableList<API54.MessageItem> = ArrayList()
    private var mListener: Listener? = null
    private var titleHeight: Int = 0
    private var screenHeight: Int = 0
    private var context: Context? = null

    constructor(titleHeight: Int, screenHeight: Int) {
        this.titleHeight = titleHeight
        this.screenHeight = screenHeight
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        this.context = parent.context
        return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_message_item, parent, false))
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            if(item.is_send_mb_leave == "Y") {
                holder.itemView.textViewNickName.setTextColor(Color.parseColor("#bcc6d2"))
                holder.itemView.textViewMessage.setTextColor(Color.parseColor("#bcc6d2"))
            } else {
                holder.itemView.textViewNickName.setTextColor(Color.parseColor("#192028"))
                holder.itemView.textViewMessage.setTextColor(Color.parseColor("#546170"))
            }

            holder.itemView.textViewNickName.text = item.send_mb_nick
            holder.itemView.textViewMessage.text = item.content
            holder.itemView.textViewDate.text = item.datetime

            if (TextUtils.isEmpty(item.send_mb_img)) {
                holder.itemView.imageViewThumbnail.setBackgroundResource(R.drawable.profile_default_img)
            } else {
                ImageLoad.setImage(
                        holder.itemView.context,
                        holder.itemView.imageViewThumbnail,
                        item.send_mb_img,
                        null,
                        ImageLoad.SCALE_CIRCLE_CROP,
                        DiskCacheStrategy.ALL)
            }

            holder.itemView.layoutContent.setOnClickListener {
                mListener?.onItemClicked(item)
            }

            holder.itemView.buttonMore.setOnClickListener {
                var viewHeight = holder.itemView.imgMore.height // imgMore 이미지의 height
                val originalPos = IntArray(2)
                holder.itemView.imgMore.getLocationInWindow(originalPos)

                var yPosition: Int = originalPos[1] // more 버튼의 y좌표 , 이 값은 상태바의 height 까지 포함한 높이
                var y = yPosition - Utils.GetStatusBarHeight(holder.itemView.context) + Utils.ConvertDpToPx(holder.itemView.context, 10) // 실제 y 좌표는 yPosition이 상태바 포함 높이 이므로 상태바 높이 값을 빼주고 more image와 popup 사이의 margin 값 10dp를 더해준 값이 실제 팝업의 y 좌표가 된다.

                if ((y + convertDpToPx(context, 150)) >= screenHeight) {
                    y = (yPosition - Utils.GetStatusBarHeight(holder.itemView.context)) - convertDpToPx(context, 150) - viewHeight - Utils.ConvertDpToPx(holder.itemView.context, 20)
                }
                mListener?.onMoreClicked(position, item, y)
            }

            // new badge
            holder.itemView.newBadge.visibility = View.GONE
            if (item.isNew > 0) {
                holder.itemView.newBadge.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setItem(items: List<API54.MessageItem>) {
        this.items.clear()
        if (items != null) {
            this.items.addAll(items)
        }

        notifyDataSetChanged()
    }

    fun deleteMessage(position: Int) {
        items.removeAt(position)

        notifyDataSetChanged()
    }

    private fun getItem(position: Int): API54.MessageItem? = if (position in 0 until items.size) items[position] else null

    class MessageViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(data: API54.MessageItem)
        fun onMoreClicked(position: Int, data: API54.MessageItem, y: Int)
    }

    private fun convertDpToPx(context: Context?, dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context!!.getResources().getDisplayMetrics()).toInt()
    }
}