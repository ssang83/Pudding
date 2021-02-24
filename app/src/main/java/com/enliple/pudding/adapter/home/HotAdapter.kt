package com.enliple.pudding.adapter.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.network.vo.API114
import kotlinx.android.synthetic.main.adapter_pudding_home_live_item.view.*

/**
 * Created by Kim Joonsung on 2019-01-24.
 */
class HotAdapter : RecyclerView.Adapter<HotAdapter.HotLiveHolder> {
    private val items: MutableList<API114.BestDataItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mContext: Context? = null

    constructor(context: Context, isHorizontalScroll: Boolean) : super() {
        if (context != null) {
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotLiveHolder {
        return HotLiveHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_pudding_home_live_item, parent, false))
    }

    override fun onBindViewHolder(holder: HotLiveHolder, position: Int) {
        items[position].let { item ->
            ImageLoad.setImage(mContext, holder.itemView.thumbnail, item.largeThumbnailUrl, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

/*            // 기능제외
            if ( "Y" == item?.isFavorite )
                holder.itemView.likeIcon.setBackgroundResource(R.drawable.ic_like_on)
            else
                holder.itemView.likeIcon.setBackgroundResource(R.drawable.ic_like)*/


            holder.itemView.product.text = item?.min_price_product
            var price = item?.min_price
            price = PriceFormatter.getInstance()?.getFormattedValue(price) ?: "0"
            price = if (item?.relationPrd?.data?.size!! < 2) {
                "${price}원"
            } else {
                "${price}원~"
            }
            holder.itemView.price.text = price
            holder.itemView.title.text = item?.title

            holder.itemView.viewCount.text = StringUtils.getSnsStyleCountZeroBase(item?.viewerCount.toInt())
            holder.itemView.likeCount.text = StringUtils.getSnsStyleCountZeroBase(item?.favoriteCount.toInt())
            holder.itemView.hashTag.text = StringUtils.convertHashTagText(item?.strTag)

            ImageLoad.setImage(mContext, holder.itemView.profile, item?.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onHotItemClicked(item, position)
                }
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<API114.BestDataItem>) {
        this.items.clear()
        if (items.isNotEmpty()) {
            this.items.addAll(items)
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    inner class HotLiveHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onHotItemClicked(item: API114.BestDataItem, position: Int)
    }
}