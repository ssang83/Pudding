package com.enliple.pudding.adapter.search

import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.network.vo.API30
import kotlinx.android.synthetic.main.adapter_search_hastag_item.view.*

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
class SearchHashTagAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2
    }

    private val items: MutableList<API30.HashtagItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mTag = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when(viewType) {
                TYPE_FOOTER -> HashTagFooterHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_search_hastag_item_footer, parent, false))
                else -> HashTagHodler(LayoutInflater.from(parent.context).inflate(R.layout.adapter_search_hastag_item, parent, false))
            }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {
            TYPE_ITEM -> onBindItem(holder as HashTagHodler, position)
            else ->{}
        }
    }

    override fun getItemCount(): Int = items.size + 1

    override fun getItemViewType(position: Int) : Int {
        if ( isPositionFooter(position) ) {
            return TYPE_FOOTER
        }
        return TYPE_ITEM
    }

    private fun isPositionFooter(position: Int): Boolean {
        return position == items.size
    }

    private fun onBindItem(holder: HashTagHodler, position: Int) {
        val item = getItem(position)

        if (item != null) {
            var tagName = item.tag_name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val tag = "<font color='#9f56f2'>${mTag}</font>"
                tagName = tagName.replace(mTag, tag, true)

                holder.itemView.textViewHastag.text = Html.fromHtml(tagName, Html.FROM_HTML_MODE_LEGACY)
            } else {
                holder.itemView.textViewHastag.text = Html.fromHtml(tagName)
            }

            holder.itemView.textViewCount.text = StringUtils.getSnsStyleCountZeroBase(item.update_count.toInt())

            holder.itemView.setOnClickListener {
                mListener?.onHashtagClick(item)
            }
        }
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setItems(list: List<API30.HashtagItem>) {
        this.items.clear()
        if (list != null) this.items.addAll(list) else {
        }

        notifyDataSetChanged()
    }

    fun addItems(list:List<API30.HashtagItem>) {
        for(i in 0 until list.size) {
            this.items.add(list[i])
        }

        notifyItemInserted(items.size - list.size)
    }

    fun setKeyword(keyword: String) {
        this.mTag = keyword
    }

    fun getItem(position: Int): API30.HashtagItem? = if (position in 0..items.size) items[position] else null

    class HashTagHodler(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    class HashTagFooterHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onHashtagClick(item: API30.HashtagItem)
    }
}