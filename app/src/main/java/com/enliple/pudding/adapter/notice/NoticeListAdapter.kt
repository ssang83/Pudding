package com.enliple.pudding.adapter.notice

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.network.vo.API110
import kotlinx.android.synthetic.main.adapter_notice_list_item.view.*

/**
 * 공지사항 리스트 어댑터
 * @author hkcha
 * @since 2018.08.30
 */
class NoticeListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<NoticeListAdapter.NoticeListHolder>() {

    private val items: MutableList<API110.NoticeItem> = ArrayList()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeListHolder {
        return NoticeListHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_notice_list_item, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: NoticeListHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.itemView.textViewSubject.text = item.subject
            holder.itemView.textViewDate.text = item.reg_date

            // TODO : ITEM 속성에 체크된 경우에만 VISIBLE 처리
            if (item.notice.equals("Y")) {
                holder.itemView.imageViewImportant.visibility = View.VISIBLE
            } else {
                holder.itemView.imageViewImportant.visibility = View.INVISIBLE
            }

            holder.itemView.setOnClickListener {
                mListener?.onNoticeListItemClicked(item)
            }
        }
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setItem(list: List<API110.NoticeItem>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    fun getItem(position: Int): API110.NoticeItem? = if (position in 0 until items.size) items[position] else null

    class NoticeListHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onNoticeListItemClicked(data: API110.NoticeItem)
    }
}