package com.enliple.pudding.adapter.home

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.network.vo.API78
import kotlinx.android.synthetic.main.adapter_following_header_item.view.*

/**
 * Created by Kim Joonsung on 2019-03-08.
 */
class FollowingUserAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<FollowingUserAdapter.FollowHeadHolder> {
    private val items: MutableList<API78.FollowItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mContext:Context
    private var mMargin15 = 0

    constructor(context:Context) : super() {
        mContext = context
        mMargin15 = Utils.ConvertDpToPx(context, 15)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            FollowHeadHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_following_header_item, parent, false))

    override fun onBindViewHolder(holder: FollowHeadHolder, position: Int) {
        val params = holder.itemView.root.layoutParams as RecyclerView.LayoutParams
        if(position == 0) {
            params.leftMargin = mMargin15
        } else if(position == items.size - 1) {
            params.rightMargin = mMargin15
        }

        holder.itemView.root.layoutParams = params

        items[position].let { item ->
            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewFollowingUser,
                    item.strThumbnail,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            holder.itemView.textViewName.text = item.strUserNickname

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onFollowingUserClick(item)
                }
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list: List<API78.FollowItem>) {
        this.items.clear()
        this.items.addAll(list)

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    class FollowHeadHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onFollowingUserClick(item: API78.FollowItem)
    }
}