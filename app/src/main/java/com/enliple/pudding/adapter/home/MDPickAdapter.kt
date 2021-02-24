package com.enliple.pudding.adapter.home

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.network.vo.API114
import kotlinx.android.synthetic.main.adapter_pudding_home_mdpick_item.view.*

/**
 * Created by Kim Joonsung on 2019-01-29.
 */
class MDPickAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<MDPickAdapter.MDPickHolder>() {
    private val VIEW_TOP = 10000
    private val VIEW_OTHER = 10001

    private val items: MutableList<API114.MdPickItem.EvPickBean> = mutableListOf()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MDPickHolder {
        if (viewType == VIEW_TOP) {
            return MDPickHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_pudding_home_mdpick_top_item, parent, false))
        }

        return MDPickHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_pudding_home_mdpick_item, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEW_TOP
        }

        return VIEW_OTHER
    }

    override fun onBindViewHolder(holder: MDPickHolder, position: Int) {
        if (position == 0) {
            // 0번은 무조건 투명 header View
            return
        }

        items[position].let { item ->
            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewThumbnail,
                    item.thumbnailUrl,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewProfile,
                    item.userImage,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            holder.itemView.textViewLikeCount.text = StringUtils.getSnsStyleCountZeroBase(item.favoriteCount.toInt())
            holder.itemView.textViewTitle.text = item.title
            holder.itemView.textViewNickName.text = item.userNick

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onMDPickClicked(item, position)
                }
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list: List<API114.MdPickItem.EvPickBean>) {
        this.items.clear()

        this.items.add(API114.MdPickItem.EvPickBean()) // 0번은 투명 아이템을 위해 넣어 준다
        this.items.addAll(list)

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    inner class MDPickHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onMDPickClicked(item: API114.MdPickItem.EvPickBean, position: Int)
    }
}