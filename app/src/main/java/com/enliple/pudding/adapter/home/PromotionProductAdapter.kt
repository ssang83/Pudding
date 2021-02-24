package com.enliple.pudding.adapter.home

import android.content.Context
import android.content.Intent
import android.graphics.Paint
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
import com.enliple.pudding.commons.network.vo.API117
import kotlinx.android.synthetic.main.adapter_caster_product_item.view.*

/**
 * Created by Kim Joonsung on 2019-01-10.
 */
class PromotionProductAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<PromotionProductAdapter.PromotionProductHolder> {
    private val items:MutableList<API117.DataItem.EvPickItem.ThemeDataItem> = mutableListOf()
    private var mListener:Listener? = null
    private lateinit var mContext: Context

    constructor(context: Context) : super() {
        if(context != null) {
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionProductHolder {
        return PromotionProductHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_caster_product_item, parent, false))
    }

    override fun onBindViewHolder(holder: PromotionProductHolder, position: Int) {
        items[position].let { item ->
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

            if("Y" == item.is_wish) {
                holder.itemView.imageLike.setBackgroundResource(R.drawable.product_scrap_on_ico)
            } else {
                holder.itemView.imageLike.setBackgroundResource(R.drawable.product_scrap_off_ico)
            }

            if("0" == item.pointRate) {
                holder.itemView.saveBadge.visibility = View.GONE
            } else {
                holder.itemView.saveBadge.visibility = View.VISIBLE
                holder.itemView.saveBadge.text = "${item.pointRate}%적립"
            }

            val shopFeeStatus = item.it_sc_type
            if ("1" == shopFeeStatus) {
                holder.itemView.freeFeeBadge.visibility = View.VISIBLE
            } else {
                holder.itemView.freeFeeBadge.visibility = View.GONE
            }

            if ("1" == item.strType) {
                holder.itemView.shopTree.visibility = View.VISIBLE
                holder.itemView.link.visibility = View.GONE
                holder.itemView.shopName.text = item.sitename
                holder.itemView.puddingPay.visibility = View.VISIBLE
            } else {
                holder.itemView.shopTree.visibility = View.GONE
                holder.itemView.link.visibility = View.VISIBLE
                holder.itemView.linkName.text = item.sitename
                holder.itemView.puddingPay.visibility = View.GONE
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
            holder.itemView.shopName.text = item.sitename
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

            holder.itemView.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onProductClicked(item)
                }
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list:List<API117.DataItem.EvPickItem.ThemeDataItem>) {
        this.items.clear()
        this.items.addAll(list)

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
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

    class PromotionProductHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onProductClicked(item:API117.DataItem.EvPickItem.ThemeDataItem)
        fun onLikeClicked(item: API117.DataItem.EvPickItem.ThemeDataItem, status:Boolean)
    }
}