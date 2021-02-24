package com.enliple.pudding.adapter.my

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.PuddingApplication
import com.enliple.pudding.R
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.commons.app.CategoryModel
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.data.CategoryItem
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API0
import com.enliple.pudding.commons.network.vo.API21
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.shoppingcaster.data.BroadcastInfo
import kotlinx.android.synthetic.main.adapter_live_channel_item.view.*
import kotlinx.android.synthetic.main.adapter_my_vod_header_item.view.*
import kotlinx.android.synthetic.main.adapter_my_vod_new_item.view.*
import org.json.JSONObject
import java.lang.NullPointerException

/**
 * Created by Kim Joonsung on 2018-10-26.
 */
class MyVODAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<RecyclerView.ViewHolder> {
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    val items: MutableList<API0.DataBeanX> = mutableListOf()
    private var mSummaryItem:API0.VODSummary? = null
    private var mListener: Listener? = null
    private var tempVODThumnailBitmap: Bitmap? = null
    private var isTempVOD: Boolean = false
    private var broadCastInfo = ""
    private lateinit var mCategoryItems: List<API81.CategoryItem>
    private var mContext:Context? = null
    private var i_width = 0
    private var i_height = 0
    private var root_height = 0
    private var checkboxWidth = 0
    private var extendedImageWidth = 0
    private var extendedImageHeight = 0
    private var imageWidth = 0
    private var imageHeight = 0
    private var isEditMode = false
    private var mTempVodDate = ""

    constructor(context: Context, extendedWidth : Int, extendedHeight : Int, width : Int, height : Int) : super() {
        if (context != null) {
            mContext = context
            mCategoryItems = CategoryModel.getCategoryList(context, "all")
            extendedImageWidth = extendedWidth
            extendedImageHeight = extendedHeight
            imageWidth = width
            imageHeight = height
            i_width = extendedImageWidth
            i_height = extendedImageHeight

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when(viewType) {
                TYPE_ITEM -> MyContentHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_my_vod_new_item, parent, false))
                else -> MyContentHeaderHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_my_vod_header_item, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {
            TYPE_ITEM -> onBindItem(holder as MyContentHolder, position)
            else -> onBindHeader(holder as MyContentHeaderHolder)
        }
    }

    override fun getItemViewType(position: Int) =
            if(position == 0) {
                TYPE_HEADER
            } else {
                TYPE_ITEM
            }

    override fun getItemCount(): Int {
        if (isTempVOD) {
            return items.size + 2
        } else {
            return items.size + 1
        }
    }

    fun setEditMode(isEditMode : Boolean) {
        this.isEditMode = isEditMode
        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setItem(list: List<API0.DataBeanX>, summary:API0.VODSummary) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        this.mSummaryItem = summary

        notifyDataSetChanged()
    }

    fun addItem(list: List<API0.DataBeanX>, isSelect:Boolean) {
        if(list.isNotEmpty()) {
            val prevSize = list.size
            items.addAll(list)

            if(isSelect) {
                for(i in 0 until items.size) {
                    items[i].isSelect = true
                }

                mListener?.setTotalCount(items.size)
            }

            notifyItemInserted(prevSize)
        }
    }

    fun itemSelectAll() {
        for (i in 0 until items.size) {
            items.get(i).isSelect = true
        }

        notifyDataSetChanged()
    }

    fun itemUnSelectAll() {
        for (i in 0 until items.size) {
            items.get(i).isSelect = false
        }

        notifyDataSetChanged()
    }

    fun itemDeleteAll() {
        items.clear()

        mListener?.updateSelectCount(items.size, items.size, -1)
        mListener?.updateTotalCount(items.size)

        notifyDataSetChanged()
    }

    fun selectItemDel() {
        val it = items.iterator()
        while (it.hasNext()) {
            val item = it.next()
            if (item.isSelect) {
                it.remove()
            }
        }

        mListener?.updateTotalCount(items.size)
        mListener?.updateSelectCount(0, items.size, -1)

        notifyDataSetChanged()
    }

    fun getSelectedCount(): Int {
        var count = 0
        for (i in 0 until items.size) {
            if (items.get(i).isSelect) {
                count++
            }
        }

        return count
    }

    fun getSelectedKey(): String {
        var key = ""

        for (i in 0 until items.size) {
            if (items[i].isSelect) {
                key += "${items[i].id}, "
            }
        }

        return key
    }

    fun setTempVODVisible(visible: Boolean) {
        this.isTempVOD = visible
    }

    fun setTempVODThumnailImages(images: ByteArray) {
        tempVODThumnailBitmap = getThumbnailImage(images)
    }

    fun setBroadCastInfo(info: String) {
        this.broadCastInfo = info
    }

    fun setTempVodDate(date:String) {
        this.mTempVodDate = date
    }

    private fun getItem(position: Int): API0.DataBeanX? = if (position in 0..items.size) items[position] else null

    private fun getThumbnailImage(images: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(images, 0, images.size)
    }

    private fun getBroadCastInfo(info: String): BroadcastInfo? {
        Logger.e("getBroadcastInfo :: $info")

        try {
            var infoModel = BroadcastInfo()
            var obj = JSONObject(info)
            infoModel.subject = obj.optString(BroadcastInfo.KEY_SUBJECT)

            var firstCategoryObj = obj.optJSONObject(BroadcastInfo.KEY_FIRST_CATEGORY)
            var firstCategoryItem: CategoryItem? = null
            if (firstCategoryObj != null) {
                var categoryId = firstCategoryObj.optString(BroadcastInfo.KEY_CATEGORY_ID)
                var categoryName = firstCategoryObj.optString(BroadcastInfo.KEY_CATEGORY_NAME)
                var categoryImage = firstCategoryObj.optString(BroadcastInfo.KEY_CATEGORY_IMAGE)
                firstCategoryItem = CategoryItem(0L, categoryId, categoryName, "", "", categoryImage)
            }

            infoModel.firstCategory = firstCategoryItem

            return infoModel
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun onBindHeader(holder: MyContentHeaderHolder) {
        if(mSummaryItem != null) {
            holder.itemView.textViewTotalRevenue.text = "${Utils.ToNumFormat(mSummaryItem?.total_order!!.toInt())}원"
            holder.itemView.textViewTotalViewCnt.text = "${Utils.ToNumFormat(mSummaryItem?.total_hit!!.toInt())}"
            holder.itemView.textViewTotalShareCnt.text = "${Utils.ToNumFormat(mSummaryItem?.total_share!!.toInt())}"
            holder.itemView.textViewTotallikeCnt.text = "${Utils.ToNumFormat(mSummaryItem?.total_favor!!.toInt())}"
            holder.itemView.textViewTotalScrapCnt.text = "${Utils.ToNumFormat(mSummaryItem?.total_scrap!!.toInt())}"
        }
    }

    private fun onBindItem(holder: MyContentHolder, position: Int) {
        var pos = 0
        if (isTempVOD) pos = position - 2 else pos = position - 1

        if ( isEditMode ) {
            holder.itemView.buttonSelect.visibility = View.VISIBLE
            Logger.e("checkboxWidth :: $checkboxWidth")
            var param = holder.itemView.root.layoutParams as RecyclerView.LayoutParams
            param.setMargins(0, 0, -Utils.ConvertDpToPx(holder.itemView.context, 51), 0)
            holder.itemView.root.layoutParams = param
        } else {
            holder.itemView.buttonSelect.visibility = View.GONE
            var param = holder.itemView.root.layoutParams as RecyclerView.LayoutParams
            param.setMargins(0, 0, 0, 0)
            holder.itemView.root.layoutParams = param
        }

        if (isTempVOD && position == 1) {
            if ( holder.itemView.buttonSelect.visibility == View.VISIBLE )
                holder.itemView.buttonSelect.visibility = View.INVISIBLE
            try {
                var param = holder.itemView.my_vod_item.layoutParams
                param.width = i_width
                holder.itemView.my_vod_item.layoutParams = param

                var tt_param = holder.itemView.thumbnail.layoutParams
                tt_param.width = i_width
                tt_param.height = i_height
                holder.itemView.thumbnail.layoutParams = tt_param

                var t_param = holder.itemView.tempLayer.layoutParams
                t_param.width = i_width
                t_param.height = i_height
                holder.itemView.tempLayer.layoutParams = t_param

                var l_param = holder.itemView.lockLayer.layoutParams
                l_param.width = i_width
                l_param.height = i_height
                holder.itemView.lockLayer.layoutParams = l_param
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            holder.itemView.tempLayer.visibility = View.VISIBLE
            holder.itemView.item.visibility = View.VISIBLE
            holder.itemView.warning.visibility = View.GONE
            holder.itemView.gradient.visibility = View.GONE
            holder.itemView.lockLayer.visibility = View.GONE

            holder.itemView.viewCount.text = "0"
            holder.itemView.likeCount.text = "0"
            holder.itemView.shareCount.text = "0"
            holder.itemView.scrapCount.text = "0"
            holder.itemView.orderCount.text = "판매수 : 0"
            holder.itemView.orderPrice.text = "판매금액 : 0원"
            holder.itemView.commission.text = "수수료 : 0%"
            holder.itemView.salesRevenue.text = "판매수익 : 0원"
            holder.itemView.clickCnt.text = "클릭수 : 0"
            holder.itemView.clickRevenue.text = "클릭수익 : 0원"

            holder.itemView.thumbnail.setImageBitmap(tempVODThumnailBitmap)
            getBroadCastInfo(broadCastInfo).let {
                holder.itemView.title.text = it!!.subject

                val image = PuddingApplication.mLoginUserData?.userIMG
                ImageLoad.setImage(
                        holder.itemView.context,
                        holder.itemView.profile,
                        image,
                        null,
                        ImageLoad.SCALE_CIRCLE_CROP,
                        DiskCacheStrategy.ALL)

                val array = mTempVodDate.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val time = array[0]
                val aTime = time.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val fTime = "${aTime[0]}년 ${aTime[1]}월 ${aTime[2]}일"
                holder.itemView.textViewRegDate.text = fTime
            }

            holder.itemView.setOnClickListener {
                mListener?.onTemVODClicked()
            }
        } else {
            val item = getItem(pos)

            // 헤더에 있는 shadow 영역때문에 2번포지션 부터 따로 동적으로 마진값 설정
            if(position >= 2) {
                var param = holder.itemView.root.layoutParams as RecyclerView.LayoutParams
                param.setMargins(0, Utils.ConvertDpToPx(holder.itemView.context, 20), 0, 0)
                holder.itemView.root.layoutParams = param
            }

            if (item != null) {
                if ( isEditMode )
                    holder.itemView.buttonSelect.visibility = View.VISIBLE
                else
                    holder.itemView.buttonSelect.visibility = View.GONE
                try {
                    var param = holder.itemView.my_vod_item.layoutParams
                    param.width = i_width
                    holder.itemView.my_vod_item.layoutParams = param

                    var tt_param = holder.itemView.thumbnail.layoutParams
                    tt_param.width = i_width
                    tt_param.height = i_height
                    holder.itemView.thumbnail.layoutParams = tt_param

                    var t_param = holder.itemView.tempLayer.layoutParams
                    t_param.width = i_width
                    t_param.height = i_height
                    holder.itemView.tempLayer.layoutParams = t_param

                    var l_param = holder.itemView.lockLayer.layoutParams
                    l_param.width = i_width
                    l_param.height = i_height
                    holder.itemView.lockLayer.layoutParams = l_param
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }

                if (item.show_YN == "N") {
                    try {
                        var param = holder.itemView.my_vod_item.layoutParams
                        param.width = i_width
                        param.height = i_height
                        holder.itemView.my_vod_item.layoutParams = param
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                    holder.itemView.tempLayer.visibility = View.GONE
                    holder.itemView.warning.visibility = View.VISIBLE
                    holder.itemView.item.visibility = View.GONE

                    val array = item.reg_date.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val time = array[0]
                    val aTime = time.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val fTime = "${aTime[0]}년 ${aTime[1]}월 ${aTime[2]}일"
                    holder.itemView.textViewWarningDate.text = fTime
                } else {
                    if (item.user_show_YN == "N") {
                        holder.itemView.lockLayer.visibility = View.VISIBLE
                        holder.itemView.gradient.visibility = View.GONE
                    } else {
                        holder.itemView.lockLayer.visibility = View.GONE
                        holder.itemView.gradient.visibility = View.VISIBLE
                        holder.itemView.gradient.alpha = 0.6f
                    }

                    holder.itemView.tempLayer.visibility = View.GONE
                    holder.itemView.warning.visibility = View.GONE
                    holder.itemView.item.visibility = View.VISIBLE
                    holder.itemView.profileLayer.visibility = View.VISIBLE

                    holder.itemView.title.text = item.title
                    holder.itemView.likeCount.text = StringUtils.getSnsStyleCountZeroBase(item.favoriteCount.toInt())

                    val array = item.reg_date.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val time = array[0]
                    val aTime = time.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val fTime = "${aTime[0]}년 ${aTime[1]}월 ${aTime[2]}일"
                    holder.itemView.textViewRegDate.text = fTime

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
                }


                var iShareCount = 0
                if ( item?.shareCount.isNotEmpty() )
                    iShareCount = item?.shareCount.toInt()
                var sShareCount = StringUtils.getSnsStyleCountZeroBase(iShareCount)
                if ( TextUtils.isEmpty(sShareCount) )
                    sShareCount = "0"

                var iViewCount = 0
                if ( item?.viewerCount.isNotEmpty() )
                    iViewCount = item?.viewerCount.toInt()
                var sViewCount = StringUtils.getSnsStyleCountZeroBase(iViewCount)
                if ( TextUtils.isEmpty(sViewCount) )
                    sViewCount = "0"

                var iFavoriteCount = 0
                if ( item?.favoriteCount.isNotEmpty() )
                    iFavoriteCount = item?.favoriteCount.toInt()
                var sFavoriteCount = StringUtils.getSnsStyleCountZeroBase(iFavoriteCount)
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
                var sOrderPrice = Utils.ToNumFormat(dOrderPrice)
                holder.itemView.orderCount.text = "판매수 : $sOrderNo"
                holder.itemView.orderPrice.text = "판매금액 : ${sOrderPrice}원"
                holder.itemView.commission.text = "수수료 : ${item.shop_max_video_ratio}%"
                holder.itemView.salesRevenue.text = "판매수익 : ${Utils.ToNumFormat(item.shop_total_fee.toInt())}원"
                holder.itemView.clickCnt.text = "클릭수 : ${Utils.ToNumFormat(item.link_total_click.toInt())}"
                holder.itemView.clickRevenue.text = "클릭수익 : ${Utils.ToNumFormat(item.link_total_fee.toInt())}원"

                holder.itemView.imageViewSelect.isSelected = item.isSelect

                if ( item.isSelect )
                    holder.itemView.imageViewSelect.setBackgroundResource(R.drawable.check_circle_on)
                else
                    holder.itemView.imageViewSelect.setBackgroundResource(R.drawable.check_off)

                holder.itemView.buttonSelect.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View?) {
                        holder.itemView.imageViewSelect!!.isSelected = !holder.itemView.imageViewSelect.isSelected
                        if (holder.itemView.imageViewSelect.isSelected) {
                            item.isSelect = true
                            mListener?.updateSelectCount(getSelectedCount(), items.size, position)
                            holder.itemView.imageViewSelect.setBackgroundResource(R.drawable.check_circle_on)
                        } else {
                            item.isSelect = false
                            mListener?.updateSelectCount(getSelectedCount(), items.size, position)
                            holder.itemView.imageViewSelect.setBackgroundResource(R.drawable.check_off)
                        }
                    }
                })

                holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View?) {
                        mListener?.onItemClicked(item, pos)
                    }
                })

//                holder.itemView.layoutLike.setOnClickListener {
//                    val status = if(holder.itemView.buttonLike.isSelected) "N" else "Y"
//                    mListener?.onLikeClicked(item, holder, status)
//                }
            }
        }

        if ( position == itemCount - 1 )
            holder.itemView.botEmpty.visibility = View.VISIBLE
        else
            holder.itemView.botEmpty.visibility = View.GONE
    }

    class MyContentHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    class MyContentHeaderHolder(itemView:View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun updateSelectCount(selected: Int, total: Int, position: Int)
        fun setEmptyView(visible: Boolean)
        fun updateTotalCount(count: Int)
        fun onItemClicked(item: API0.DataBeanX, position: Int)
        fun onTemVODClicked()
        fun setTotalCount(itemCount: Int)
    }
}