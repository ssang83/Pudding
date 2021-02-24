package com.enliple.pudding.shoppingcaster.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API72
import kotlinx.android.synthetic.main.adapter_caster_live_list_item.view.*

/**
 * 방송간 상품 리스트 출력 RecyclerView.Adapter
 * @author hkcha
 * @since 2018.08.29
 */
class CasterLiveProductAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    companion object {
        private const val LINK = "2"
        private const val PRODUCT = "1"
        private const val SAMPLE = "3"
    }

    private val items: MutableList<API72.RelationPrdBean.ProductItem> = ArrayList()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder =
            ProductHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_caster_live_list_item, parent, false))

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int): API72.RelationPrdBean.ProductItem? = if (position in 0 until items.size) items[position] else null

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setItems(items: List<API72.RelationPrdBean.ProductItem>) {
        this.items.clear()
        if (items != null) {
            this.items.addAll(items)
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        var sPercentage = "--%"
        var dSailedPrice: Double = 0.0
        if (item != null) {
            Logger.e("item.strType :: ${item.strType}")
            if (LINK.equals(item.strType)) {
                holder.itemView.productLayer.visibility = View.GONE
                holder.itemView.linkLayer.visibility = View.VISIBLE
//                Glide.with(holder.itemView.context)
//                        .load(item!!.strPrdImg)
//                        .asBitmap()
//                        .priority(Priority.HIGH)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .into(holder.itemView.link_imageViewThumbNail)
                ImageLoad.setImage(holder.itemView.context, holder.itemView.linkImageViewThumbNail, item!!.strPrdImg, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

                holder.itemView.link.text = item!!.strLinkUrl
                holder.itemView.linkTextViewSubject.text = item!!.strPrdName

                holder.itemView.setOnClickListener {
                    mListener?.onLinkClicked(position, item)
                }
            } else if (PRODUCT.equals(item.strType)) {
                holder.itemView.productLayer.visibility = View.VISIBLE
                holder.itemView.linkLayer.visibility = View.GONE

//                Glide.with(holder.itemView.context)
//                        .load(item!!.strPrdImg)
//                        .asBitmap()
//                        .priority(Priority.HIGH)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .into(holder.itemView.imageViewThumbNail)
                ImageLoad.setImage(holder.itemView.context, holder.itemView.imageViewThumbNail, item!!.strPrdImg, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)
                var d_origin_price = item.nPrdCustPrice.toDouble()
                var d_price = item.nPrdSellPrice.toDouble()
                Logger.e("d_origin_price :: " + d_origin_price)
                Logger.e("d_price :: " + d_price)
                try {
                    if (d_origin_price != d_price) {
                        holder.itemView.textViewSalePercent.visibility = View.VISIBLE
                        try {
                            if (d_origin_price <= 0) {
                            } else {
                                val div = d_origin_price - d_price
                                Logger.e("div :: $div")
                                val s_rt = div / d_origin_price
                                Logger.e("s_rt :: $s_rt")
                                val sale_percentage = (s_rt * 100).toInt()
                                Logger.e("sale_percentage :: $sale_percentage")
                                if (sale_percentage <= 0)
                                    sPercentage = ""
                                else
                                    sPercentage = "$sale_percentage%"
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        holder.itemView.textViewSalePercent.visibility = View.GONE
                    }
//                    var originPrice = item.nPrdCustPrice
//                    var sailedPrice = item.nPrdSellPrice
//                    var dOriginPrice = originPrice.toDouble()
//                    dSailedPrice = sailedPrice.toDouble()
//                    var percentage = ((dSailedPrice / dOriginPrice) * 100).toInt()
//                    sPercentage = "$percentage%"
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                holder.itemView.textViewSalePercent.text = sPercentage
                holder.itemView.textViewSubject.text = item.strPrdName
                holder.itemView.textViewPrice.text = "${String.format(holder.itemView.context.getString(R.string.msg_price_format),
                        PriceFormatter.getInstance()!!.getFormattedValue(d_price.toInt()))}"

                holder.itemView.setOnClickListener {
                    mListener?.onProductItemClicked(position, item)
                }
            } else {
                holder.itemView.productLayer.visibility = View.VISIBLE
                holder.itemView.linkLayer.visibility = View.GONE

//                Glide.with(holder.itemView.context)
//                        .load(item!!.strPrdImg)
//                        .asBitmap()
//                        .priority(Priority.HIGH)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .into(holder.itemView.imageViewThumbNail)
                ImageLoad.setImage(holder.itemView.context, holder.itemView.imageViewThumbNail, item!!.strPrdImg, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)
                try {
//                var originPrice = item.getnPrdCustPrice()
//                var sailedPrice = item.getnPrdSellPrice()
                    var originPrice = item.nPrdCustPrice
                    var sailedPrice = item.nPrdSellPrice
                    var dOriginPrice = originPrice.toDouble()
                    dSailedPrice = sailedPrice.toDouble()
                    var percentage = ((dSailedPrice / dOriginPrice) * 100).toInt()
                    sPercentage = "$percentage%"
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                holder.itemView.textViewSalePercent.text = sPercentage
                holder.itemView.textViewSubject.text = item.strPrdName
                holder.itemView.textViewPrice.text = "${String.format(holder.itemView.context.getString(R.string.msg_price_format),
                        PriceFormatter.getInstance()!!.getFormattedValue(dSailedPrice.toInt()))}"

                holder.itemView.setOnClickListener {
                    mListener?.onProductItemClicked(position, item)
                }
            }
        }
    }

    class ProductHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onProductItemClicked(position: Int, item: API72.RelationPrdBean.ProductItem)

        fun onProductCartClicked(position: Int, item: API72.RelationPrdBean.ProductItem)

        fun onLinkClicked(position: Int, item: API72.RelationPrdBean.ProductItem)
    }
}