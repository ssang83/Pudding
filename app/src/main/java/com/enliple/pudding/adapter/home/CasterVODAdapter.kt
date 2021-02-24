package com.enliple.pudding.adapter.home

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.network.vo.API32
import kotlinx.android.synthetic.main.adapter_caster_profile_header.view.*
import kotlinx.android.synthetic.main.common_landscape_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-10.
 */
class CasterVODAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    val mItems: MutableList<API32.DataBeanX> = mutableListOf()
    private var mListener: Listener? = null
    private lateinit var mContext: Context
    private var totalCount = 0

    constructor(context: Context) : super() {
        if (context != null) {
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when(viewType) {
                TYPE_HEADER -> CasterProfileHeaderHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_caster_profile_header, parent, false))
                else -> CasterProfileHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_landscape_item, parent, false))
            }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {
            TYPE_HEADER -> onBindHeader(holder as CasterProfileHeaderHolder)
            else -> onBindItem(holder as CasterProfileHolder, position)
        }
    }

    override fun getItemCount(): Int = mItems.size + 1

    override fun getItemViewType(position: Int) =
            when(position) {
                0 -> TYPE_HEADER
                else -> TYPE_ITEM
            }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun getItem(position: Int): API32.DataBeanX? = if (position in 0 until mItems.size) mItems[position] else null

    fun setItem(items: List<API32.DataBeanX>, isOrderClick:Boolean) {
        this.mItems.clear()
        if (mItems != null) this.mItems.addAll(items)

        if(!isOrderClick) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeChanged(1, items.size)
        }
    }

    fun addItem(items: List<API32.DataBeanX>) {
        if(items.isNotEmpty()) {
            val prevSize = items.size
            mItems.addAll(items)
//            notifyItemInserted(prevSize) //  헤더 영역 추가
            notifyDataSetChanged()
        }
    }

    fun setTotalCount(count:Int) {
        this.totalCount = count
    }

    fun clearData() {
        this.mItems.clear()
        notifyDataSetChanged()
    }

    private fun onBindHeader(holder: CasterProfileHeaderHolder) {
        holder.itemView.textViewRecentlyOrder.isSelected = false
        holder.itemView.textViewRecentlyOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_medium.otf")
        holder.itemView.textViewPopularityOrder.isSelected = true
        holder.itemView.textViewPopularityOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_bold.otf")

        holder.itemView.textViewTotalCount.text = PriceFormatter.getInstance()?.getFormattedValue(totalCount)

        holder.itemView.textViewPopularityOrder.setOnClickListener {
            holder.itemView.textViewPopularityOrder.isSelected = true
            holder.itemView.textViewPopularityOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_bold.otf")
            holder.itemView.textViewRecentlyOrder.isSelected = false
            holder.itemView.textViewRecentlyOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_medium.otf")

            mListener?.onPopularityOrderClicked("1")
        }

        holder.itemView.textViewRecentlyOrder.setOnClickListener {
            holder.itemView.textViewPopularityOrder.isSelected = false
            holder.itemView.textViewPopularityOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_medium.otf")
            holder.itemView.textViewRecentlyOrder.isSelected = true
            holder.itemView.textViewRecentlyOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_bold.otf")

            mListener?.onRecentlyOrderClicked("")
        }
    }

    private fun onBindItem(holder: CasterProfileHolder, position: Int) {
        val realPosition = position - 1

        val item = getItem(realPosition)
        if (item != null) {
            holder.itemView.title.text = item.title
            holder.itemView.likeCount.text = StringUtils.getSnsStyleCountZeroBase(item.favoriteCount)
            holder.itemView.viewCount.text = StringUtils.getSnsStyleCountZeroBase(item.viewerCount)
            holder.itemView.hashTag.text = StringUtils.convertHashTagText(item.strTag)

            holder.itemView.product.text = item?.min_price_product
            var price = PriceFormatter.getInstance()?.getFormattedValue(item?.min_price)
            price = if (item?.relationPrd?.data!!.size < 1) {
                "${price}원"
            } else {
                "${price}원~"
            }
            holder.itemView.price.text = price

            when (item.videoType) {
                "VOD" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_video_label)
                "LASTLIVE" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_pass_live_label)
                else -> holder.itemView.label.setBackgroundResource(R.drawable.trip_onair_label)
            }

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.thumbnail,
                    item!!.largeThumbnailUrl,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            ImageLoad.setImage(
                    mContext,
                    holder.itemView.profile,
                    item?.userImage,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onItemClicked(item, realPosition)
                }
            })
        }
    }

    class CasterProfileHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    class CasterProfileHeaderHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: API32.DataBeanX, position: Int)
        fun onPopularityOrderClicked(order:String)
        fun onRecentlyOrderClicked(order:String)
    }
}