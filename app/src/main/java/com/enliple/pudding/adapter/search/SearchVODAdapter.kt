package com.enliple.pudding.adapter.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.bus.ViewCountBus
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API29
import kotlinx.android.synthetic.main.adapter_search_header_item.view.*
import kotlinx.android.synthetic.main.common_landscape_item.view.*

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
class SearchVODAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    val items: MutableList<API29.VodItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mContext:Context? = null
    private var mTotalCount = -1
    private var mKeyword = ""

    constructor(context: Context, totalCount:Int) : super() {
        if(context != null) {
            mContext = context
            mTotalCount = totalCount
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when(viewType) {
                TYPE_HEADER -> SearchVODHeader(LayoutInflater.from(parent.context).inflate(R.layout.adapter_search_header_item, parent, false))
                else -> SearchVODHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_landscape_item, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            when(holder.itemViewType) {
                TYPE_HEADER -> onBindHeader(holder as SearchVODHeader)
                else -> onBindItem(holder as SearchVODHolder, position)
            }

    override fun getItemCount(): Int {
        if(mTotalCount == 0) {
            return items.size + 1
        } else {
            return items.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(mTotalCount == 0) {
            if(position == 0) {
                return TYPE_HEADER
            } else {
                return TYPE_ITEM
            }
        } else {
            return TYPE_ITEM
        }
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun getItem(position: Int): API29.VodItem? = if (position in 0 until items.size) items[position] else null

    fun setItem(list: List<API29.VodItem>) {
        this.items.clear()
        if (list.isNotEmpty()) this.items.addAll(list) else {}

        notifyDataSetChanged()
    }

    fun setKeyword(keyword:String) {
        this.mKeyword = keyword
    }

    fun addItem(list:List<API29.VodItem>) {
        if (list.isNotEmpty()) {
            var prevSize = items.size
            items.addAll(list)
            notifyItemInserted(prevSize)
        }
    }

    fun setViewCount(streamKey:String, count:Int) {
        for ( i in 0 until items.size ) {
            var vItem = items.get(i)
            var tItem = vItem
            var sKey = vItem.id
            if ( streamKey == sKey ) {
                tItem.viewerCount = count.toString()
                items.set(i, tItem)
                break
            }
        }

        notifyDataSetChanged()
    }

    private fun onBindHeader(holder:SearchVODHeader) {
        holder.itemView.textViewKeyword.text = "'${mKeyword}'"
        holder.itemView.textViewEmptyComment.text = "에 대한 영상이 없습니다."
        holder.itemView.textViewEmpty.text = "추천 영상"
    }

    private fun onBindItem(holder:SearchVODHolder, position: Int) {
        val realPos = if(mTotalCount == 0) position - 1 else position

        val item = getItem(realPos)
        if (item != null) {
            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.thumbnail,
                    item!!.largeThumbnailUrl,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.profile,
                    item!!.userImage,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            holder.itemView.product.text = item?.min_price_product
            var price = PriceFormatter.getInstance()?.getFormattedValue(item?.min_price)
            price = if (item?.relationPrd?.data!!.size < 1) {
                "${price}원"
            } else {
                "${price}원~"
            }

            holder.itemView.price.text = price
            holder.itemView.title.text = item?.title
            holder.itemView.viewCount.text = StringUtils.getSnsStyleCountZeroBase(item.viewerCount.toInt())
            holder.itemView.likeCount.text = StringUtils.getSnsStyleCountZeroBase(item.favoriteCount.toInt())
            holder.itemView.hashTag.text = StringUtils.convertHashTagText(item?.strTag)

            when (item.videoType) {
                "LIVE" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_onair_label)
                "LASTLIVE" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_pass_live_label)
                else -> holder.itemView.label.setBackgroundResource(R.drawable.trip_video_label)
            }

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onVodItemClicked(item, realPos)
                }
            })
        }
    }

    fun changeZzim(idx: String) {
        if ( items != null ) {
            for ( i in items.indices ) {
                var bItem = items[i]
                var relationPrd = bItem.relationPrd
                var relationPrdArr = relationPrd.data
                for ( j in relationPrdArr.indices ) {
                    var item = relationPrdArr[j]
                    if ( idx == item.idx ) {
                        Logger.e("mItems same idx is :: " + idx + " i is " + i + " , j is " + j)
                        var isWish = "N"
                        var wishCnt = 0
                        Logger.e("mItems before wish  :: " + item.is_wish)
                        Logger.e("mItems before wish_cnt  :: " + item.wish_cnt)
                        if ( item.is_wish == "N" ) {
                            isWish = "Y"
                            wishCnt = Integer.valueOf(item.wish_cnt) + 1
                        } else {
                            isWish = "N"
                            wishCnt = Integer.valueOf(item.wish_cnt) - 1
                        }
                        item.is_wish = isWish
                        item.wish_cnt = "$wishCnt"
                        Logger.e("mItems after wish  :: " + item.is_wish)
                        Logger.e("mItems after wish_cnt  :: " + item.wish_cnt)
                        relationPrdArr.set(j, item)
                    }
                }
                relationPrd.data = relationPrdArr
                bItem.relationPrd = relationPrd
                items.set(i, bItem)
            }
        }
    }


    inner class SearchVODHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class SearchVODHeader(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onVodItemClicked(item: API29.VodItem, position: Int)
    }
}