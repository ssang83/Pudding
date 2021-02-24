package com.enliple.pudding.adapter.home

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.activity.LoginActivity
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API33
import kotlinx.android.synthetic.main.adapter_caster_product_item.view.*
import kotlinx.android.synthetic.main.adapter_caster_profile_product_header.view.*

/**
 * Created by Kim Joonsung on 2018-11-26.
 */
class CasterProductAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val items: MutableList<API33.ProductItem> = ArrayList()
    private var mListener: Listener? = null
    private var totalCount = 0
    private lateinit var mContext:Context

    constructor(context: Context) : super() {
        if(context != null) {
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when(viewType) {
                TYPE_HEADER -> CasterLiveHeaderHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_caster_profile_product_header, parent, false))
                else -> CasterLiveHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_caster_product_item, parent, false))
            }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {
            TYPE_HEADER -> onBindHeader(holder as CasterLiveHeaderHolder)
            else -> onBindItem(holder as CasterLiveHolder, position)
        }
    }

    override fun getItemCount(): Int = items.size + 1

    override fun getItemViewType(position: Int) =
            when(position) {
                0 -> TYPE_HEADER
                else -> TYPE_ITEM
            }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setTotalCount(count:Int) {
        this.totalCount = count
    }

    fun getItem(position: Int): API33.ProductItem? = if (position in 0 until items.size) items[position] else null

    fun setItem(items: List<API33.ProductItem>, isOrderClick:Boolean) {
        this.items.clear()
        if (items != null) this.items.addAll(items)

        if(!isOrderClick) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeChanged(1, items.size)
        }
    }

    fun addItem(items: List<API33.ProductItem> ) {
        if ( items.isNotEmpty() ) {
            val prefSize = items.size
            this.items.addAll(items)
            notifyDataSetChanged()
        }
    }

    fun likeSuccess(idx:String) {
        if (items != null) {
            for (i in items.indices) {
                val itemIdx = items[i].idx
                if (itemIdx == idx) {
                    val item = items[i]
                    var wish = item.is_wish
                    val wish_cnt = item.wish_cnt
                    var iWishCnt = 0
                    try {
                        iWishCnt = wish_cnt
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    Logger.e("item.wish_cnt = > $iWishCnt")
                    if ("Y" == wish) {
                        wish = "N"
                        if (iWishCnt > 0)
                            iWishCnt--
                    } else {
                        wish = "Y"
                        iWishCnt++
                    }
                    Logger.e("item.is_siwh = > " + item.is_wish)
                    Logger.e("item.wish_cnt = > $iWishCnt")
                    item.is_wish = wish
                    item.wish_cnt = iWishCnt
                    Logger.e("item.is_siwh = > " + item.is_wish)
                    items.set(i, item)
                }
            }

            notifyDataSetChanged()
        }
    }

    private fun onBindHeader(holder: CasterLiveHeaderHolder) {
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

            mListener?.onPopularityOrderClicked(holder, "1")
        }

        holder.itemView.textViewRecentlyOrder.setOnClickListener {
            holder.itemView.textViewPopularityOrder.isSelected = false
            holder.itemView.textViewPopularityOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_medium.otf")
            holder.itemView.textViewRecentlyOrder.isSelected = true
            holder.itemView.textViewRecentlyOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_bold.otf")

            mListener?.onRecentlyOrderClicked(holder, "")
        }
    }

    private fun onBindItem(holder:CasterLiveHolder, position: Int) {
        val realPosition = position - 1
        val item = getItem(realPosition)
        if (item != null) {
            ImageLoad.setImage(
                    mContext,
                    holder.itemView.image,
                    item.image1,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            val d = mContext!!.getDrawable(R.drawable.bg_round_product_image) as GradientDrawable
            holder.itemView.image.background = d
            holder.itemView.image.clipToOutline = true

            if(item.strType == "1") {
                holder.itemView.shopTree.visibility = View.VISIBLE
                holder.itemView.freeFeeBadge.visibility = View.VISIBLE
                holder.itemView.link.visibility = View.GONE
                holder.itemView.shopName.text = item.sitename
                holder.itemView.puddingPay.visibility = View.VISIBLE
            } else {
                holder.itemView.link.visibility = View.VISIBLE
                holder.itemView.shopTree.visibility = View.GONE
                holder.itemView.freeFeeBadge.visibility = View.GONE
                holder.itemView.linkName.text = item.sitename
                holder.itemView.puddingPay.visibility = View.GONE
            }

            if("Y" == item.is_wish) {
                holder.itemView.imageLike.setBackgroundResource(R.drawable.product_scrap_on_ico)
            } else {
                holder.itemView.imageLike.setBackgroundResource(R.drawable.product_scrap_off_ico)
            }

//            if("1" == item.it_sc_type) {
//                holder.itemView.freeFeeBadge.visibility = View.GONE
//            } else {
//                holder.itemView.freeFeeBadge.visibility = View.VISIBLE
//            }

            if(item.pointRate.isEmpty() || "0" == item.pointRate) {
                holder.itemView.saveBadge.visibility = View.GONE
            } else {
                holder.itemView.saveBadge.visibility = View.VISIBLE
                holder.itemView.saveBadge.text = "${item.pointRate}%적립"
            }

            if(item.discount.isNotEmpty()) {
                holder.itemView.originPrice.visibility = View.VISIBLE
                holder.itemView.layoutSale.visibility = View.VISIBLE

                holder.itemView.originPrice.text = "${PriceFormatter.getInstance()!!.getFormattedValue(item.orgprice)}원"
                holder.itemView.originPrice.paintFlags = holder.itemView.originPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.itemView.sale.text = item.discount
            } else {
                holder.itemView.originPrice.visibility = View.GONE
                holder.itemView.layoutSale.visibility = View.GONE
            }

            holder.itemView.price.text = PriceFormatter.getInstance()!!.getFormattedValue(item.price)
            holder.itemView.productName.text = item.title
            holder.itemView.reviewPoint.text = item.review_avg
            holder.itemView.reviewCnt.text = item.review_cnt.toString()
            holder.itemView.zzimCnt.text = item.wish_cnt.toString()

            holder.itemView.btnLike.setOnClickListener(object:OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    if(!AppPreferences.getLoginStatus(mContext)) {
                        mContext.startActivity(Intent(mContext, LoginActivity::class.java))
                    } else {
                        val status = if("Y" == item.is_wish) {
                            false
                        } else {
                            true
                        }

                        mListener?.onLikeClicked(item, status)
                    }
                }
            })

            holder.itemView.setOnClickListener(object:OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onItemClicked(item)
                }
            })
        }
    }

    class CasterLiveHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    class CasterLiveHeaderHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: API33.ProductItem)
        fun onPopularityOrderClicked(holder: CasterLiveHeaderHolder, order:String)
        fun onRecentlyOrderClicked(holder: CasterLiveHeaderHolder, order:String)
        fun onLikeClicked(item: API33.ProductItem, status:Boolean)
    }
}