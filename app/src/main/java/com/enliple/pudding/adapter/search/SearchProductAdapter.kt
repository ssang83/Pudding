package com.enliple.pudding.adapter.search

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.activity.LoginActivity
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API90
import kotlinx.android.synthetic.main.adapter_search_header_item.view.*
import kotlinx.android.synthetic.main.adapter_search_product_item.view.*

/**
 * Created by Kim Joonsung on 2018-12-06.
 */
class SearchProductAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val items: MutableList<API90.ProductItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mTotalCount = 0
    private var mContext:Context? = null
    private var mKeyword = ""

    constructor(context: Context, totalCount:Int) : super() {
        if(context != null) {
            mContext = context
            mTotalCount = totalCount
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when(viewType) {
                TYPE_HEADER -> SearchProductHeader(LayoutInflater.from(parent.context).inflate(R.layout.adapter_search_header_item, parent, false))
                else -> SearchProductHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_search_product_item, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            when(holder.itemViewType) {
                TYPE_HEADER -> onBindHeader(holder as SearchProductHeader)
                else -> onBindItem(holder as SearchProductHolder, position)
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

    override fun getItemCount(): Int {
        if(mTotalCount == 0) {
            return items.size + 1
        } else {
            return items.size
        }
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setItems(list: List<API90.ProductItem>) {
        this.items.clear()
        if (list != null) this.items.addAll(list) else {
        }

        notifyDataSetChanged()
    }

    fun addItems(list:List<API90.ProductItem>) {
        if (list.isNotEmpty()) {
            items.addAll(list)
            notifyDataSetChanged()
        }
    }

    fun setKeyword(keyword:String) {
        this.mKeyword = keyword
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

    private fun onBindHeader(holder:SearchProductHeader) {
        holder.itemView.textViewKeyword.text = "'${mKeyword}'"
        holder.itemView.textViewEmptyComment.text = "에 대한 상품이 없습니다."
        holder.itemView.textViewEmpty.text = "내가 관심 있을 만한 상품"
    }

    private fun onBindItem(holder:SearchProductHolder, position: Int) {
        val realPos = if(mTotalCount == 0) position - 1 else position

        val item = getItem(realPos)
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

            var it_stock_qty = item.it_stock_qty
            if ( it_stock_qty <= 0 ) {
                holder.itemView.soldOut.visibility = View.VISIBLE
                holder.itemView.priceLayer.visibility = View.GONE
            } else {
                holder.itemView.soldOut.visibility = View.GONE
                holder.itemView.priceLayer.visibility = View.VISIBLE
            }

            if(item.strType == 1) {
                holder.itemView.shopTree.visibility = View.VISIBLE
                holder.itemView.link.visibility = View.GONE
                if(1 == item.it_sc_type) {
                    holder.itemView.freeFeeBadge.visibility = View.VISIBLE
                } else {
                    holder.itemView.freeFeeBadge.visibility = View.GONE
                }
                holder.itemView.shopName.text = item.sitename
                holder.itemView.puddingPay.setVisibility(View.VISIBLE)
            } else {
                holder.itemView.shopTree.visibility = View.GONE
                holder.itemView.link.visibility = View.VISIBLE
                holder.itemView.freeFeeBadge.visibility = View.GONE
                holder.itemView.linkName.text = item.sitename
                holder.itemView.puddingPay.setVisibility(View.GONE)
            }

            if("Y" == item.is_wish) {
                holder.itemView.imageLike.setBackgroundResource(R.drawable.product_scrap_on_ico)
            } else {
                holder.itemView.imageLike.setBackgroundResource(R.drawable.product_scrap_off_ico)
            }



            if(0 == item.pointRate) {
                holder.itemView.saveBadge.visibility = View.GONE
            } else {
                holder.itemView.saveBadge.visibility = View.VISIBLE
                holder.itemView.saveBadge.text = "${item.pointRate}%적립"
            }

            if(item.discount.isNotEmpty()) {
                holder.itemView.originPrice.visibility = View.VISIBLE
                holder.itemView.layoutSale.visibility = View.VISIBLE

                holder.itemView.originPrice.text = "${PriceFormatter.getInstance()!!.getFormattedValue(item.orgprice)}원"
                holder.itemView.originPrice.setPaintFlags(holder.itemView.originPrice.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
                holder.itemView.sale.text = item.discount
            } else {
                holder.itemView.originPrice.visibility = View.GONE
                holder.itemView.layoutSale.visibility = View.GONE
            }

            holder.itemView.price.text = PriceFormatter.getInstance()!!.getFormattedValue(item.price)
            holder.itemView.productName.text = item.title
            holder.itemView.reviewPoint.text = item.review_avg.toString()
            holder.itemView.reviewCnt.text = item.review_cnt.toString()
            holder.itemView.zzimCnt.text = item.wish_cnt.toString()

            holder.itemView.btnLike.setOnClickListener(object:OnSingleClickListener(){
                override fun onSingleClick(v: View?) {
                    if(!AppPreferences.getLoginStatus(mContext!!)) {
                        mContext!!.startActivity(Intent(mContext!!, LoginActivity::class.java))
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

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onItemClicked(item)
                }
            })
        }
    }

    private fun getItem(position: Int): API90.ProductItem? = if (position in 0 until items.size) items[position] else null

    class SearchProductHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class SearchProductHeader(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: API90.ProductItem)
        fun onLikeClicked(item:API90.ProductItem, status:Boolean)
    }
}