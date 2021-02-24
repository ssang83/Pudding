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
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API113
import com.enliple.pudding.commons.network.vo.API78
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.recyclerview.WrappedGridLayoutManager
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import kotlinx.android.synthetic.main.adapter_pudding_following_footer_item.view.*
import kotlinx.android.synthetic.main.adapter_pudding_following_header_item.view.*
import kotlinx.android.synthetic.main.common_landscape_item.view.*

/**
 * Created by Kim Joonsung on 2019-02-14.
 */
class FollowingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2
    }

    val items: MutableList<VOD.DataBeanX> = mutableListOf()
    private var mListener: Listener? = null
    private var mSubListener: FollowingUserAdapter.Listener? = null
    private var mFooterListener: RecommendUserAdapter.Listener? = null

    private lateinit var mUserAdapter: FollowingUserAdapter
    private var mFooterAdapter: RecommendUserAdapter? = null
    private val userItems: MutableList<API78.FollowItem> = mutableListOf()
    private val footerItems: MutableList<API113.UserItem> = mutableListOf()
    private var mContext: Context
    private var isFooter = false

    constructor(context: Context) : super() {
        mContext = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_HEADER -> FollowUserHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_pudding_following_header_item, parent, false))
                TYPE_FOOTER -> FollowFooterHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_pudding_following_footer_item, parent, false))
                else -> FollowHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_landscape_item, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_HEADER -> onBindHeader(holder as FollowUserHolder)
            TYPE_FOOTER -> onBindFooter(holder as FollowFooterHolder)
            else -> onBindItem(holder as FollowHolder, position)
        }
    }

    override fun getItemCount(): Int {
        if ( items.size > 0 ) {
            if ( isFooter ) {
                return items.size + 2
            } else {
                return items.size + 1
            }
        } else {
            return items.size + 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        if ( items.size > 0 ) {
            Logger.e("isFooter :: " + isFooter)
            if (position == 0) {
                return TYPE_HEADER
            } else if (position == items.size + 1 && isFooter) {
                return TYPE_FOOTER
            } else {
                return TYPE_ITEM
            }
        } else {
            if (position == 0) {
                return TYPE_HEADER
            } else if (position == items.size && isFooter) {
                return TYPE_FOOTER
            } else {
                return TYPE_ITEM
            }
        }

    }

    fun getItemSize(): Int {
        if (items != null)
            return items.size
        else
            return 0
    }

    fun setItems(data: List<VOD.DataBeanX>) {
        this.items.clear()
        this.items.addAll(data)

        notifyDataSetChanged()
    }

    fun addItems(list: List<VOD.DataBeanX>) {
        if (list.isNotEmpty()) {
            items.addAll(list)
            notifyDataSetChanged()
        }
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setFollowingUserItem(list: List<API78.FollowItem>) {
        this.userItems.clear()
        this.userItems.addAll(list)
    }

    fun setFooterItem(list: List<API113.UserItem>) {
        this.footerItems.clear()
        this.footerItems.addAll(list)
    }

    fun setSubListener(subListener: FollowingUserAdapter.Listener) {
        this.mSubListener = subListener
    }

    fun setFooterListener(footerListener: RecommendUserAdapter.Listener) {
        this.mFooterListener = footerListener
    }

    fun setIsFooter(isShow: Boolean) {
        this.isFooter = isShow
    }

    private fun onBindHeader(holder: FollowUserHolder) {
        holder.itemView.recyclerViewFollowingUser.setHasFixedSize(true)
        holder.itemView.recyclerViewFollowingUser.layoutManager = WrappedLinearLayoutManager(mContext).apply {
            orientation = RecyclerView.HORIZONTAL
        }

        mUserAdapter = FollowingUserAdapter(mContext).apply {
            setListener(mSubListener!!)
            setItems(userItems)
        }

        holder.itemView.recyclerViewFollowingUser.adapter = mUserAdapter
    }

    private fun onBindFooter(holder: FollowFooterHolder) {
        holder.itemView.recyclerViewRecommendUser?.setHasFixedSize(true)
        var manager = WrappedGridLayoutManager(mContext, 3).apply {
            orientation = RecyclerView.VERTICAL
            spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0) {
                        3
                    } else {
                        1
                    }
                }
            }
        }
        holder.itemView.recyclerViewRecommendUser?.layoutManager = manager

        mFooterAdapter = RecommendUserAdapter(mContext, false).apply {
            setListener(mFooterListener!!)
            setItems(footerItems)
        }

        holder.itemView.recyclerViewRecommendUser.adapter = mFooterAdapter
    }

    private fun onBindItem(holder: FollowHolder, position: Int) {
        val realPosition = position - 1
        items[realPosition].let { item ->
            ImageLoad.setImage(mContext, holder.itemView.thumbnail, item.largeThumbnailUrl, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

            when (item.videoType) {
                "LIVE" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_onair_label)
                "LASTLIVE" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_pass_live_label)
                else -> holder.itemView.label.setBackgroundResource(R.drawable.trip_video_label)
            }

            var price = PriceFormatter.getInstance()?.getFormattedValue(item?.min_price) ?: "0"
            price = if (item?.relationPrd?.data?.size!! < 1) {
                "${price}원"
            } else {
                "${price}원~"
            }

            holder.itemView.price.text = price
            holder.itemView.title.text = item?.title
            holder.itemView.product.text = item?.min_price_product
            holder.itemView.viewCount.text = StringUtils.getSnsStyleCountZeroBase(item.viewerCount.toInt())
            holder.itemView.likeCount.text = StringUtils.getSnsStyleCountZeroBase(item.favoriteCount.toInt())
            holder.itemView.hashTag.text = StringUtils.convertHashTagText(item?.strTag)

            ImageLoad.setImage(mContext, holder.itemView.profile, item?.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onItemClicked(item, realPosition)
                }
            })
        }
    }

    class FollowHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class FollowUserHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class FollowFooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: VOD.DataBeanX, position: Int)
    }
}