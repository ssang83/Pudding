package com.enliple.pudding.adapter.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API28
import kotlinx.android.synthetic.main.common_landscape_item.view.*

/**
 * Created by Kim Joonsung on 2018-09-28.
 */
class HashTagVodListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<HashTagVodListAdapter.HashTagVodHolder> {

    var items: MutableList<API28.VodItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mContext:Context? = null

    constructor(context: Context) : super() {
        if(context != null) {
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashTagVodHolder {
        return HashTagVodHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.common_landscape_item, parent, false))
    }

    override fun onBindViewHolder(holder: HashTagVodHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            when (item.videoType) {
                "LIVE" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_onair_label)
                "LASTLIVE" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_pass_live_label)
                else -> holder.itemView.label.setBackgroundResource(R.drawable.trip_video_label)
            }

            /*// 기능제외
            if ( "Y" == item.isFavorite ) {
                holder.itemView.iconLike.setBackgroundResource(R.drawable.ic_like_on)
            } else {
                holder.itemView.iconLike.setBackgroundResource(R.drawable.ic_like)
            }*/

            var price = PriceFormatter.getInstance()?.getFormattedValue(item.min_price)
            price = if (item?.relationPrd?.data!!.size < 1) {
                "${price}원"
            } else {
                "${price}원~"
            }

            holder.itemView.price.text = price
            holder.itemView.title.text = item?.title
            holder.itemView.product.text = item.min_price_product
            holder.itemView.viewCount.text = StringUtils.getSnsStyleCountZeroBase(item.viewerCount.toInt())
            holder.itemView.likeCount.text = StringUtils.getSnsStyleCountZeroBase(item.favoriteCount.toInt())
            holder.itemView.hashTag.text = StringUtils.convertHashTagText(item?.strTag)

            ImageLoad.setImage(mContext, holder.itemView.thumbnail, item.largeThumbnailUrl, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)
            ImageLoad.setImage(mContext, holder.itemView.profile, item?.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onVodItemClicked(item, position)
                }
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun getItem(position: Int): API28.VodItem? = if (position in 0 until items.size) items[position] else null

    fun removeItems() {
        Logger.e("removeItems")
        this.items.clear()
        this.items = ArrayList<API28.VodItem>()
        notifyDataSetChanged()
    }

    fun setItem(items: List<API28.VodItem>) {
        this.items.clear()
        if (items != null) this.items.addAll(items) else {
        }

        notifyDataSetChanged()
    }

    fun addItem(items: List<API28.VodItem>) {
        if ( items != null ) this.items.addAll(items)
        notifyDataSetChanged()
    }

    class HashTagVodHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onVodItemClicked(item: API28.VodItem, position: Int)
    }
}