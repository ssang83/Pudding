package com.enliple.pudding.adapter.home

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.CategoryModel
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.data.CategoryItem
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API116
import com.enliple.pudding.commons.network.vo.API81
import kotlinx.android.synthetic.main.adapter_trip_item.view.*

/**
 * Created by Kim Joonsung on 2019-01-09.
 */
class PromotionVODAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<PromotionVODAdapter.PromotionDetailHolder> {

    val items:MutableList<API116.DataBeanX.EvPickItem.TagDataItem> = mutableListOf()
    private var mListener:Listener? = null
//    private var mItemWidth = 0
    private var mImageWidth = 0
    private var mImageHeight = 0
    private lateinit var mCategoryItem: List<API81.CategoryItem>
    private var mContext:Context? = null

    constructor(context: Context) : super() {
        if(context != null) {
            mImageWidth  = AppPreferences.getScreenWidth(context) - Utils.ConvertDpToPx(context, 30)
            mImageHeight = (mImageWidth * 185) / 330
//            var screenWidth = AppPreferences.getScreenWidth(context)
//            mItemWidth = (screenWidth - Utils.ConvertDpToPx(context, 24)) / 2 // recycler view 좌우 padding 값제외한 순수 item 의 width
//            mImageWidth = mItemWidth - Utils.ConvertDpToPx(context,8)  // item의 마진 3과 라인을 위한 좌우 패딩 1을 계산한 이미지의 width
//            mImageHeight = (199 * mItemWidth) / 162 // image width의 비율에 맞춘 height 값

            mCategoryItem = CategoryModel.getCategoryList(context, "all")
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionDetailHolder {
        return PromotionDetailHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_trip_item, parent, false))
    }

    override fun onBindViewHolder(holder: PromotionDetailHolder, position: Int) {
        items[position].let { item ->

//            var params = holder.itemView.main_root.layoutParams
//            params.width = mItemWidth
//            params.height = params.height
//            holder.itemView.main_root.layoutParams = params
//            params = holder.itemView.imageViewThumbnail.layoutParams
//            params.width = mImageWidth
//            params.height = mImageHeight
//            holder.itemView.imageViewThumbnail.layoutParams = params

            var param = holder.itemView.imageViewThumbnail.layoutParams
            param.width = mImageWidth
            param.height = mImageHeight
            Logger.e("mImageWidth :: " + mImageWidth)
            Logger.e("mImageHeight :: " + mImageHeight)
            holder.itemView.imageViewThumbnail.layoutParams = param
            Logger.e("item.large image :: " + item.largeThumbnailUrl)
            Logger.e("item.normal image :: " + item.thumbnailUrl)
            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewThumbnail,
                    item.largeThumbnailUrl,
                    null,
                    ImageLoad.SCALE_CENTER_CROP,
                    DiskCacheStrategy.ALL)

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewProfile,
                    item.userImage,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            if(item.videoType == "VOD") {
                holder.itemView.imageViewLabel.setBackgroundResource(R.drawable.trip_video_label)
                holder.itemView.textViewViewCount.text = "${StringUtils.getSnsStyleCountZeroBase(item.viewerCount.toInt())}"
            } else if(item.videoType == "LIVE") {
                holder.itemView.imageViewLabel.setBackgroundResource(R.drawable.trip_onair_label)
                holder.itemView.textViewViewCount.text = "${StringUtils.getSnsStyleCountZeroBase(item.viewerCount.toInt())}"
            } else if(item.videoType == "LASTLIVE") {
                holder.itemView.imageViewLabel.setBackgroundResource(R.drawable.trip_pass_live_label)
                holder.itemView.textViewViewCount.text = "${StringUtils.getSnsStyleCountZeroBase(item.viewerCount.toInt())}"
            } else {
                holder.itemView.imageViewLabel.setBackgroundResource(R.drawable.trip_video_label)
            }
            if ( item.favoriteCount != null )
                holder.itemView.textViewLikeCount.text = StringUtils.getSnsStyleCountZeroBase(item.favoriteCount.toInt())
            holder.itemView.textViewTitle.text = item.title

            try {
                var price = item.min_price.toDouble()
                if(item.relationPrd.data.size < 1) {
                    holder.itemView.price.text = "${Utils.ToNumFormat(price)}원"
                } else {
                    holder.itemView.price.text = "${Utils.ToNumFormat(price)}원~"
                }
            } catch (e: Exception ) {
                e.printStackTrace()
            }

            holder.itemView.productName.text = item.min_price_product
            var tag = "#${item.strTag.replace(",", "  #")}"
            holder.itemView.textViewTag.text = tag

//            setCategoryLabel(item.categoryCode, holder)
            setLikeStatus(item.isFavorite, holder)

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onVodClicked(item, position)
                }
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list:List<API116.DataBeanX.EvPickItem.TagDataItem>) {
        this.items.clear()
        this.items.addAll(list)

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    private fun setLikeStatus(isLike: String, holder: PromotionDetailHolder) {
        /*// 기능제외
        if ( "N" == isLike ) {
            holder.itemView.imageViewLike.setBackgroundResource(R.drawable.ic_like)
//            holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#8192a5"))
        } else {
            holder.itemView.imageViewLike.setBackgroundResource(R.drawable.ic_like_on)
//            holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#ff6c6c"))
        }*/
    }

//    private fun setCategoryLabel(categoryId: String, holder: PromotionDetailHolder) {
//        if(mCategoryItem.isEmpty()) {
//            return
//        }
//
//        if(categoryId.isEmpty()) {
//            holder.itemView.textViewCategory.text = "전체"
//            holder.itemView.textViewCategory.setTextColor(Color.parseColor("#192028"))
//            holder.itemView.textViewCategory.setBackgroundResource(R.drawable.bg_category_all)
//
//            return
//        }
//
//        for (i in 0 until mCategoryItem.size) {
//            if (mCategoryItem.get(i).categoryId == categoryId) {
//                holder.itemView.textViewCategory.text = mCategoryItem.get(i).categoryName
//                holder.itemView.textViewCategory.setTextColor(Color.parseColor(mCategoryItem.get(i).categoryHex))
//
//                (holder.itemView.textViewCategory.background as GradientDrawable).apply {
//                    setStroke(Utils.ConvertDpToPx(mContext, 1), Color.parseColor(mCategoryItem[i].categoryHex))
//                }
//
//                return
//            } else {
//                holder.itemView.textViewCategory.text = "전체"
//                holder.itemView.textViewCategory.setTextColor(Color.parseColor("#192028"))
//                holder.itemView.textViewCategory.setBackgroundResource(R.drawable.bg_category_all)
//            }
//        }
//    }

    class PromotionDetailHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

    interface Listener {
        fun onVodClicked(item:API116.DataBeanX.EvPickItem.TagDataItem, position: Int)
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
                        Logger.e("bestItems same idx is :: " + idx + " i is " + i + " , j is " + j)
                        var isWish = "N"
                        var wishCnt = 0
                        Logger.e("bestItems before wish  :: " + item.is_wish)
                        Logger.e("bestItems before wish_cnt  :: " + item.wish_cnt)
                        if ( item.is_wish == "N" ) {
                            isWish = "Y"
                            wishCnt = Integer.valueOf(item.wish_cnt) + 1
                        } else {
                            isWish = "N"
                            wishCnt = Integer.valueOf(item.wish_cnt) - 1
                        }
                        item.is_wish = isWish
                        item.wish_cnt = "$wishCnt"
                        Logger.e("bestItems after wish  :: " + item.is_wish)
                        Logger.e("bestItems after wish_cnt  :: " + item.wish_cnt)
                        relationPrdArr.set(j, item)
                    }
                }
                relationPrd.data = relationPrdArr
                bItem.relationPrd = relationPrd
                items.set(i, bItem)
            }
        }
    }
}