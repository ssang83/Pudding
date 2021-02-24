package com.enliple.pudding.adapter.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API17
import kotlinx.android.synthetic.main.adapter_reply_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-05.
 */
class ReplyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: MutableList<API17.BroadcastReplyItem> = ArrayList()
    private var listener: OnPopupPositionListener? = null
    private var context: Context? = null

    interface OnPopupPositionListener {
        fun getPopupStartY(y: Int, item: API17.BroadcastReplyItem, position: Int)
        fun getItemCount(count: Int)
        fun onReplyDel(idx: String)
    }

    fun setPopupPositionListener(listener: OnPopupPositionListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        this.context = parent.context
        return ReplyItemHolder(LayoutInflater.from(context).inflate(R.layout.adapter_reply_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            Logger.e("onBindViewHolder Image:${item?.mb_user_img}")
            ImageLoad.setImage(holder.itemView.context, holder.itemView.imageViewThumbNail,
                    item!!.mb_user_img, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

            holder.itemView.textViewNickName.text = item.mb_nick
            holder.itemView.textViewContent.text = item.comment
            holder.itemView.textViewReplyDate.text = item.reg_date

            if ("Y" == item.is_mine) {
                holder.itemView.buttonMore.visibility = View.VISIBLE
            } else {
                holder.itemView.buttonMore.visibility = View.GONE
            }

            holder.itemView.buttonMore.setOnClickListener {
                var viewHeight = holder.itemView.buttonMore.height // imgMore 이미지의 height
                val originalPos = IntArray(2)
                holder.itemView.buttonMore.getLocationInWindow(originalPos)
                var yPosition: Int = originalPos[1] // more 이미지의 y좌표 ( dialog의 시작점 기준 )
                var topMargin = yPosition + (viewHeight / 2) - Utils.ConvertDpToPx(context, 49)
                if (listener != null) {
                    listener!!.getPopupStartY(topMargin, item, position)
                }
            }
        } else {
            Logger.e("ReplyAdapter onBindViewHolder item null")
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int): API17.BroadcastReplyItem? = if (position in 0 until items.size) items[position] else null

    fun setItem(items: List<API17.BroadcastReplyItem>) {
        Logger.e("ReplyAdapter:${items.size}")
        this.items.clear()

        if (items != null) {
            this.items.addAll(items)
        }

        notifyDataSetChanged()
    }

    fun delete(position: Int) {
        Logger.e("delete")
        listener?.onReplyDel(items[position]?.idx)
    }

    class ReplyItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}