package com.enliple.pudding.adapter.search

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.network.vo.API27
import kotlinx.android.synthetic.main.adapter_keyword_sub_list_item.view.*


/**
 * Created by Kim Joonsung on 2018-09-28.
 */
class KeywordVODListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<KeywordVODListAdapter.KeywordVodHolder>() {
    private val items: MutableList<API27.KeywordVodItem.VodItem> = mutableListOf()
    private var mListener: Listener? = null
    private var tagPosition: Int = -1;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordVodHolder {
        return KeywordVodHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_keyword_sub_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: KeywordVodHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewThumbnail,
                    item!!.thumbnailUrl,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onVodItemClicked(item, position, tagPosition)
                }
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setListener(listener: Listener?) {
        mListener = listener
    }

    fun setTagPosition(position: Int) {
        tagPosition = position
    }

    fun getItem(position: Int): API27.KeywordVodItem.VodItem? = if (position in 0 until items.size) items[position] else null

    fun setItem(items: List<API27.KeywordVodItem.VodItem>) {
        this.items.clear()
        if (items != null) {
            this.items.addAll(items)
        }

        notifyDataSetChanged()
    }

    class KeywordVodHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onVodItemClicked(item: API27.KeywordVodItem.VodItem, position: Int, tagPosition: Int)
    }
}