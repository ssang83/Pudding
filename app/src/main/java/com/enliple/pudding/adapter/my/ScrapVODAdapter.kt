package com.enliple.pudding.adapter.my

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
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API23
import com.enliple.pudding.commons.widget.toast.AppToast
import kotlinx.android.synthetic.main.adapter_scrap_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-25.
 */
class ScrapVODAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<ScrapVODAdapter.ScrapContentHolder> {

    val items: MutableList<API23.MyScrapVODItem> = mutableListOf()
    private var mListener: Listener? = null
    private var isEditMode: Boolean = false
    private var mContext:Context? = null
    private var i_width = 0
    private var i_height = 0
    private var extendedImageWidth = 0
    private var extendedImageHeight = 0
    private var imageWidth = 0
    private var imageHeight = 0

    constructor(context: Context, extendedWidth : Int, extendedHeight : Int, width : Int, height : Int) : super() {
        if (context != null) {
            mContext = context
            extendedImageWidth = extendedWidth
            extendedImageHeight = extendedHeight
            imageWidth = width
            imageHeight = height
            i_width = extendedImageWidth
            i_height = extendedImageHeight
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScrapContentHolder {
        return ScrapContentHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_scrap_item, parent, false))
    }

    override fun onBindViewHolder(holder: ScrapContentHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            if (isEditMode) {
                holder.itemView.buttonCheck.visibility = View.VISIBLE
                var param = holder.itemView.root.layoutParams as RecyclerView.LayoutParams
                param.setMargins(0, 0, -Utils.ConvertDpToPx(holder.itemView.context, 51), 0)
                holder.itemView.root.layoutParams = param
            } else {
                holder.itemView.buttonCheck.visibility = View.GONE
                var param = holder.itemView.root.layoutParams as RecyclerView.LayoutParams
                param.setMargins(0, 0, 0, 0)
                holder.itemView.root.layoutParams = param
            }

            try {
                var param = holder.itemView.contentLayer.layoutParams
                param.width = i_width
                holder.itemView.contentLayer.layoutParams = param

                var tt_param = holder.itemView.thumbnail.layoutParams
                tt_param.width = i_width
                tt_param.height = i_height
                holder.itemView.thumbnail.layoutParams = tt_param

                var t_param = holder.itemView.layoutWarning.layoutParams
                t_param.width = i_width
                t_param.height = i_height
                holder.itemView.layoutWarning.layoutParams = t_param

                var l_param = holder.itemView.userNotShowing.layoutParams
                l_param.width = i_width
                l_param.height = i_height
                holder.itemView.userNotShowing.layoutParams = l_param
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            if(item.delete_YN == "Y") {
                holder.itemView.layoutWarning.visibility = View.VISIBLE
                holder.itemView.imageViewWarning.visibility = View.GONE
                holder.itemView.contentLayer.visibility = View.GONE
                holder.itemView.textViewWarning.text = "${item.userNick}이 삭제한 동영상입니다."
            } else {
                if (item.show_YN == "N") {
                    holder.itemView.layoutWarning.visibility = View.VISIBLE
                    holder.itemView.imageViewWarning.visibility = View.VISIBLE
                    holder.itemView.contentLayer.visibility = View.GONE
                    holder.itemView.textViewWarning.text = holder.itemView.context.getString(R.string.msg_my_content_warning1)
                } else {
                    holder.itemView.contentLayer.visibility = View.VISIBLE
                    holder.itemView.layoutWarning.visibility = View.GONE

                    if (item.user_show_YN == "N") {
                        holder.itemView.userNotShowing.visibility = View.VISIBLE
                    } else {
                        holder.itemView.userNotShowing.visibility = View.GONE
                    }

                    holder.itemView.title.text = item.title
                    holder.itemView.likeCount.text = StringUtils.getSnsStyleCountZeroBase(item.favoriteCount.toInt())
                    holder.itemView.viewCount.text = StringUtils.getSnsStyleCountZeroBase(item.viewerCount.toInt())
                    holder.itemView.hashTag.text = StringUtils.convertHashTagText(item.strTag)

                    if (item.videoType == "LIVE") {
                        holder.itemView.label.setBackgroundResource(R.drawable.trip_onair_label)
                    } else if (item.videoType == "LASTLIVE") {
                        holder.itemView.label.setBackgroundResource(R.drawable.trip_pass_live_label)
                    } else {
                        holder.itemView.label.setBackgroundResource(R.drawable.trip_video_label)
                    }

                    holder.itemView.product.text = item?.min_price_product
                    var price = PriceFormatter.getInstance()?.getFormattedValue(item?.min_price)
                    price = if (item?.relationPrd?.data!!.size < 1) {
                        "${price}원"
                    } else {
                        "${price}원~"
                    }
                    holder.itemView.price.text = price

/*                    // 기능제외
                    if("Y" == item.isFavorite) {
                        holder.itemView.likeIcon.setBackgroundResource(R.drawable.ic_like_on)
                    } else {
                        holder.itemView.likeIcon.setBackgroundResource(R.drawable.ic_like)
                    }*/

                    ImageLoad.setImage(
                            holder.itemView.context,
                            holder.itemView.thumbnail,
                            item!!.largeThumbnailUrl,
                            null,
                            ImageLoad.SCALE_CENTER_CROP,
                            DiskCacheStrategy.ALL)

                    ImageLoad.setImage(
                            holder.itemView.context,
                            holder.itemView.profile,
                            item!!.userImage,
                            null,
                            ImageLoad.SCALE_CIRCLE_CROP,
                            DiskCacheStrategy.ALL)
                }
            }

            holder.itemView.buttonCheck.isSelected = item.isSelect
            holder.itemView.buttonCheck.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    v!!.isSelected = !v.isSelected
                    if (v.isSelected) {
                        item.isSelect = true
                        mListener?.updateSelectCount(getSelectedCount(), items.size, position)
                    } else {
                        item.isSelect = false
                        mListener?.updateSelectCount(getSelectedCount(), items.size, position)
                    }
                }
            })

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    if (isEditMode) {
                        holder.itemView.buttonCheck.isSelected = !holder.itemView.buttonCheck.isSelected
                        if (holder.itemView.buttonCheck.isSelected) {
                            item.isSelect = true
                            mListener?.updateSelectCount(getSelectedCount(), items.size, position)
                        } else {
                            item.isSelect = false
                            mListener?.updateSelectCount(getSelectedCount(), items.size, position)
                        }
                    } else {
                        if (item.user_show_YN == "N")
                            AppToast(mContext!!).showToastMessage("비공개 처리된 영상입니다.",
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_BOTTOM)
                        else
                            mListener?.onItemClicked(item, position)
                    }
                }
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItem(list: List<API23.MyScrapVODItem>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    fun addItems(list:List<API23.MyScrapVODItem>, isSelected: Boolean) {
        if(list.isNotEmpty()) {
            val prevSize = list.size
            items.addAll(list)
            if ( isSelected ) {
                for (i in 0 until items.size) {
                    items.get(i).isSelect = true
                }
                if ( mListener != null )
                    mListener!!.setTotalCount(items.size)
            }
            notifyItemChanged(prevSize)
        }
    }

    fun setListener(listener: Listener) {
        mListener = listener
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

    fun setEditMode(mode: Boolean) {
        this.isEditMode = mode

        notifyDataSetChanged()
    }

    fun getEditMode(): Boolean = this.isEditMode

    fun getSelectedCount(): Int {
        var count = 0
        for (i in 0 until items.size) {
            if (items.get(i).isSelect) {
                count++
            }
        }

        return count
    }

    fun getSelectItemKey(): String {
        var streamKey = ""
        for (i in 0 until items.size) {
            if (items[i].isSelect) {
                streamKey += "${items[i].id},"
            }
        }

        if (streamKey.isNotEmpty()) {
            streamKey = streamKey.substring(0, streamKey.length - 1) // 맨 뒤에 "," 제거
        }
        Logger.e("getSelectItemKey:$streamKey")
        return streamKey
    }

    private fun getItem(position: Int): API23.MyScrapVODItem? = if (position in 0 until items.size) items[position] else null

    class ScrapContentHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun updateSelectCount(selected: Int, total: Int, position: Int)
        fun setEmptyView(visible: Boolean)
        fun updateTotalCount(count: Int)
        fun onItemClicked(item: API23.MyScrapVODItem, position: Int)
        fun setTotalCount(itemCount: Int)
    }
}