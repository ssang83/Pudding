package com.enliple.pudding.adapter.my

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.network.vo.API0
import kotlinx.android.synthetic.main.adapter_live_channel_item.view.*

/**
 * Created by Kim Joonsung on 2019-01-24.
 */
class ChannelAdapter : RecyclerView.Adapter<ChannelAdapter.ChannelHolder> {
    val items: MutableList<API0.DataBeanX> = mutableListOf()
    private var mListener: Listener? = null
    private var mContext: Context? = null

    constructor(context: Context, isHorizontalScroll: Boolean) : super() {
        if (context != null) {
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelHolder {
        return ChannelHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_live_channel_item, parent, false))
    }

    override fun onBindViewHolder(holder: ChannelHolder, position: Int) {
        items[position].let { item ->
            var i_width = AppPreferences.getScreenWidth(holder.itemView.context) - Utils.ConvertDpToPx(holder.itemView.context, 66)
            var i_height = (i_width * 167)/295

            var param = holder.itemView.thumbnail.layoutParams
            param.width = i_width
            param.height = i_height
            holder.itemView.thumbnail.layoutParams = param

            holder.itemView.gradient.alpha = 0.6f

            ImageLoad.setImage(mContext, holder.itemView.thumbnail, item.largeThumbnailUrl, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)
            ImageLoad.setImage(mContext, holder.itemView.profile, item.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

            holder.itemView.title.text = item?.title

            val array = item.reg_date.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val time = array[0]
            val aTime = time.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val fTime = "${aTime[0]}년 ${aTime[1]}월 ${aTime[2]}일"
            holder.itemView.textViewRegDate.text = fTime

            var iShareCount = 0
            if ( item?.shareCount.isNotEmpty() )
                iShareCount = item?.shareCount.toInt()
            var sShareCount = StringUtils.getSnsStyleCount(iShareCount)
            if ( TextUtils.isEmpty(sShareCount) )
                sShareCount = "0"

            var iViewCount = 0
            if ( item?.viewerCount.isNotEmpty() )
                iViewCount = item?.viewerCount.toInt()
            var sViewCount = StringUtils.getSnsStyleCount(iViewCount)
            if ( TextUtils.isEmpty(sViewCount) )
                sViewCount = "0"

            var iFavoriteCount = 0
            if ( item?.favoriteCount.isNotEmpty() )
                iFavoriteCount = item?.favoriteCount.toInt()
            var sFavoriteCount = StringUtils.getSnsStyleCount(iFavoriteCount)
            if ( TextUtils.isEmpty(sFavoriteCount) )
                sFavoriteCount = "0"

            holder.itemView.viewCount.text = sViewCount
            holder.itemView.likeCount.text = sFavoriteCount
            holder.itemView.shareCount.text = sShareCount
            holder.itemView.scrapCount.text = StringUtils.getSnsStyleCountZeroBase(item.scrapCount.toInt())

            var iOrderNo = 0
            var dOrderPrice = 0.0
            if ( item?.shop_total_cnt.isNotEmpty() ) {
                iOrderNo = item?.shop_total_cnt.toInt()
            }
            if ( item?.shop_total_price.isNotEmpty() ) {
                dOrderPrice = item?.shop_total_price.toDouble()
            }
            var sOrderNo = Utils.ToNumFormat(iOrderNo)
            var sOrderPrice = "${Utils.ToNumFormat(dOrderPrice)}원"
            holder.itemView.orderCount.text = "판매수 : $sOrderNo"
            holder.itemView.orderPrice.text = "판매금액 : $sOrderPrice"
            holder.itemView.commission.text = "수수료 : ${item.shop_max_video_ratio}%"
            holder.itemView.salesRevenue.text = "판매수익 : ${Utils.ToNumFormat(item.shop_total_fee.toInt())}원"
            holder.itemView.clickCnt.text = "클릭수 : ${Utils.ToNumFormat(item.link_total_click.toInt())}"
            holder.itemView.clickRevenue.text = "클릭수익 : ${Utils.ToNumFormat(item.link_total_fee.toInt())}원"

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onItemClicked(item, position)
                }
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<API0.DataBeanX>) {
        this.items.clear()
        if (items.isNotEmpty()) {
            this.items.addAll(items)
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    inner class ChannelHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: API0.DataBeanX, position: Int)
    }
}