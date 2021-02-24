package com.enliple.pudding.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.data.DialogModel
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import kotlinx.android.synthetic.main.adapter_dialog_product_list_item.view.*

/**
 * 방송간 상품 리스트 출력 RecyclerView.Adapter
 * @author hkcha
 * @since 2018.08.29
 */
class DialogProductListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val LINK = "2"
        private const val PRODUCT = "1"
        private const val SAMPLE = "3"
    }

    private val items: MutableList<DialogModel> = ArrayList()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ProductHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_dialog_product_list_item, parent, false))

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int): DialogModel? = if (position in 0 until items.size) items[position] else null

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setItems(items: List<DialogModel>) {
        this.items.clear()
        if (items != null) {
            this.items.addAll(items)
        }
        notifyDataSetChanged()
    }

    fun updateZzim(idx:String) {
        try {
            for(i in 0 until items.size) {
                val data = items[i]
                if(items[i].idx == idx) {
                    var count = data.wish_cnt.toInt()
                    if("Y" == data.is_wish) {
                        if(count > 0) {
                            --count
                        }
                        data.is_wish = "N"
                        data.wish_cnt = count.toString()
                    } else {
                        ++count
                        data.is_wish = "Y"
                        data.wish_cnt = count.toString()
                    }

                    items.removeAt(i)
                    items.add(i, data)
                }
            }

            notifyDataSetChanged()
        } catch (e : Exception) {
            Logger.p(e)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            if (item.type == "2" || item.type == "3") {
                holder.itemView.link.visibility = View.VISIBLE
                holder.itemView.shopTree.visibility = View.GONE
                holder.itemView.btnCart.visibility = View.GONE
                holder.itemView.linkName.text = item.storeName
            } else {
                holder.itemView.link.visibility = View.GONE
                holder.itemView.shopTree.visibility = View.VISIBLE
                holder.itemView.btnCart.visibility = View.VISIBLE
                holder.itemView.shopName.text = item.storeName
            }

            ImageLoad.setImage(holder.itemView.context, holder.itemView.thumbnail, item.thumbNail, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

            holder.itemView.textViewSubject.text = item.name
            holder.itemView.numLike.text = item.wish_cnt
            Logger.e("item.wish_cnt :: " + item.wish_cnt)
            if("Y" == item.is_wish) {
                setLikeStatus(holder, true)
            } else {
                setLikeStatus(holder, false)
            }

            holder.itemView.setOnClickListener {
                mListener?.onProductItemClicked(position, item)
            }

            holder.itemView.btnLike.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onProductZzimClicked(position, item)
                }
            })

            holder.itemView.btnCart.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onProductCartClicked(position, item)
                }
            })

            try {
                var price = item.custPrice?.toDouble()
                var sellPrice = item.sellPrice?.toDouble()
                if (sellPrice != null) {
                    holder.itemView.textViewPrice.text = Utils.ToNumFormat(sellPrice!!) + "원"

                    if (price != null && price != sellPrice) {
                        holder.itemView.originalPrice.text = Utils.ToNumFormat(price!!) + "원"
                        holder.itemView.originalPrice.paintFlags = holder.itemView.originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }

//                if (price != sellPrice) {
//                    sPercentage = getRate(price, sellPrice)
                    //holder.itemView.textViewSalePercent.visibility = View.GONE
//                if (!sPercentage.isNullOrEmpty()) {
//                    holder.itemView.textViewSalePercent.visibility = View.VISIBLE
//                    holder.itemView.textViewSalePercent.text = sPercentage
//                }

//                    holder.itemView.textViewPrice.text = "${String.format(holder.itemView.context.getString(R.string.msg_price_format),
//                            PriceFormatter.getInstance()!!.getFormattedValue(sellPrice.toInt()))}"
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    private fun setLikeStatus(holder: RecyclerView.ViewHolder, status:Boolean) {
        if(status) {
            holder.itemView.btnLike.setBackgroundResource(R.drawable.like_circle_on)
            holder.itemView.likeImage.setBackgroundResource(R.drawable.my_jjim_on_ico)
            holder.itemView.numLike.setTextColor(Color.parseColor("#ff6c6c"))
        } else {
            holder.itemView.btnLike.setBackgroundResource(R.drawable.like_circle_off)
            holder.itemView.likeImage.setBackgroundResource(R.drawable.my_jjim_off_ico)
            holder.itemView.numLike.setTextColor(Color.parseColor("#bcc6d2"))
        }
    }

    private fun getRate(price: Double?, sellPrice: Double?): String {
        var result = ""
        if (price == null || sellPrice == null) {
            return ""
        }

        try {
            if (price >= 0) {
                val rate = (price - sellPrice) / price
                Logger.d("rate:$rate")
                val salePercentage = (rate * 100).toInt()
                if (salePercentage > 0) {
                    result = "$salePercentage%"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

    class ProductHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onProductItemClicked(position: Int, item: DialogModel)
        fun onProductCartClicked(position: Int, item: DialogModel)
        fun onProductZzimClicked(position: Int, item: DialogModel)
    }
}