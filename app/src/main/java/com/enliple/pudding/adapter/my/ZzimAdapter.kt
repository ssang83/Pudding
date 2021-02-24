package com.enliple.pudding.adapter.my

import android.content.Context
import android.graphics.Paint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.bus.ZzimStatusBus
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBus
import com.enliple.pudding.commons.network.vo.API127
import com.enliple.pudding.commons.network.vo.API145
import kotlinx.android.synthetic.main.adapter_zzim_item.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2019-03-13.
 */
class ZzimAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<ZzimAdapter.ZzimHolder>() {
    private val items:MutableList<API145.ProductItem> = mutableListOf()
    private var mListener:Listener? = null
    private var isEditMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZzimHolder {
        return ZzimHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_zzim_item, parent, false))
    }

    override fun onBindViewHolder(holder: ZzimHolder, position: Int) {

        items[position].let { item ->
            val zzimStatus = item.is_wish
            val shopName = item.sitename
            val price = item.price
            val originPrice = item.orgprice
            val productGubun = item.strType
            val shopFeeStatus = item.it_sc_type
            var percentage = ""
            var dPrice = 0.0
            var dOriginPrice = 0.0
            try {
                if (price == null || originPrice == null) {

                } else {
                    dPrice = java.lang.Double.valueOf(price)
                    dOriginPrice = java.lang.Double.valueOf(originPrice)

                    if (dPrice != dOriginPrice) {
                        Logger.e("dPrice :: $dPrice")
                        Logger.e("dOriginPrice :: $dOriginPrice")
                        val sRate = getRate(dOriginPrice, dPrice)
                        Logger.e("sRate :: $sRate")
                        if (!TextUtils.isEmpty(sRate))
                            percentage = getRate(dOriginPrice, dPrice) + "%"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            ImageLoad.setImage(holder.itemView.context, holder.itemView.imageViewThumbnail, item.image1, R.drawable.product_no_img, ImageLoad.SCALE_CENTER_CROP, DiskCacheStrategy.ALL)

            holder.itemView.textViewTitle.text = item.title
            holder.itemView.textViewPrice.text = "${String.format(holder.itemView.context.getString(R.string.msg_price_format),
                    PriceFormatter.getInstance()!!.getFormattedValue(item.price))}"
            if (TextUtils.isEmpty(originPrice) || price == originPrice) {
                holder.itemView.originPrice.setVisibility(View.INVISIBLE)
            } else {
                if ("0" == originPrice)
                    holder.itemView.originPrice.setVisibility(View.INVISIBLE)
                else {
                    holder.itemView.originPrice.setVisibility(View.VISIBLE)
                    holder.itemView.originPrice.setText(Utils.ToNumFormat(dOriginPrice) + "원")
                    holder.itemView.originPrice.setPaintFlags(holder.itemView.originPrice.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
                }
            }
            if(isEditMode) {
                holder.itemView.buttonCheck.visibility = View.VISIBLE
                holder.itemView.empty.visibility = View.GONE
            } else {
                holder.itemView.buttonCheck.visibility = View.GONE
                holder.itemView.empty.visibility = View.VISIBLE
            }
//
//            if(item.orgprice == item.price) {
//                holder.itemView.textViewDiscount.visibility = View.GONE
//            } else {
//                holder.itemView.textViewDiscount.visibility = View.VISIBLE
//
//                val orgPrice= item.orgprice.toDouble()
//                val price = item.price.toDouble()
//                try {
//                    if(orgPrice >= 0) {
//                        val div = orgPrice - price
//                        val rate = div / orgPrice
//                        val discount = (rate * 100).toInt()
//                        var percentage = ""
//                        if(discount >= 0) {
//                            percentage = "${discount}%"
//                        }
//
//                        holder.itemView.textViewDiscount.text = percentage
//                    }
//                } catch (e:Exception) {
//                    Logger.p(e)
//                }
//            }

            holder.itemView.buttonCheck.isSelected = item.isSelect
            holder.itemView.buttonCheck.setOnClickListener(object :OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    v!!.isSelected = !v.isSelected
                    if(v.isSelected) {
                        item.isSelect = true
                    } else {
                        item.isSelect = false
                    }

                    mListener?.updateSelectCount(getSelectedCount(), items.size)
                }
            })

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onItemClicked(item)
                }
            })

            if (item.it_stock_qty.toInt() > 0) {
                holder.itemView.soldOut.visibility = View.GONE
                holder.itemView.saleLayer.visibility = View.VISIBLE
            } else {
                Logger.e("else called")
                holder.itemView.soldOut.visibility = View.VISIBLE
                holder.itemView.saleLayer.visibility = View.GONE
            }

            if (TextUtils.isEmpty(percentage)) {
                holder.itemView.textViewDiscount.setVisibility(View.GONE)
            } else {
                holder.itemView.textViewDiscount.setVisibility(View.VISIBLE)
                holder.itemView.textViewDiscount.setText(percentage)
            }
            holder.itemView.shopName.setText(shopName)
            if ("Y" == zzimStatus) {
                holder.itemView.imageLike.setBackgroundResource(R.drawable.product_scrap_on_ico)
            } else {
                holder.itemView.imageLike.setBackgroundResource(R.drawable.product_scrap_off_ico)
            }

            if ("1" == shopFeeStatus) {
                holder.itemView.freeFeeBadge.setVisibility(View.VISIBLE)
            } else {
                holder.itemView.freeFeeBadge.setVisibility(View.GONE)
            }

            if (TextUtils.isEmpty(item.pointRate) || "0" == item.pointRate)
                holder.itemView.saveBadge.setVisibility(View.GONE)
            else {
                holder.itemView.saveBadge.setVisibility(View.VISIBLE)
                holder.itemView.saveBadge.setText(item.pointRate + "%적립")
            }

            if (TextUtils.isEmpty(productGubun)) {
                holder.itemView.productGubun.setBackgroundResource(R.drawable.item_shop_ic_1)
                holder.itemView.shopBg.setBackgroundResource(R.drawable.item_shop_background)
                holder.itemView.puddingPay.setVisibility(View.GONE)
            } else if ("1" == productGubun) {
                holder.itemView.productGubun.setBackgroundResource(R.drawable.item_shop_ic_1)
                holder.itemView.shopBg.setBackgroundResource(R.drawable.item_shop_background)
                holder.itemView.puddingPay.setVisibility(View.VISIBLE)
            } else if ("2" == productGubun) {
                holder.itemView.productGubun.setBackgroundResource(R.drawable.item_link_ic_1)
                holder.itemView.shopBg.setBackgroundResource(R.drawable.item_link_background)
                holder.itemView.puddingPay.setVisibility(View.GONE)
            } else if ("3" == productGubun) {
                holder.itemView.productGubun.setBackgroundResource(R.drawable.item_link_ic_1)
                holder.itemView.shopBg.setBackgroundResource(R.drawable.item_link_background)
                holder.itemView.puddingPay.setVisibility(View.GONE)
            } else {
                holder.itemView.productGubun.setBackgroundResource(R.drawable.item_shop_ic_1)
                holder.itemView.shopBg.setBackgroundResource(R.drawable.item_shop_background)
                holder.itemView.puddingPay.setVisibility(View.GONE)
            }

            holder.itemView.reviewPoint.setText(item.review_avg)
            holder.itemView.reviewCnt.setText(item.review_cnt)
            holder.itemView.zzimCnt.setText(item.wish_cnt)

            holder.itemView.btnLike.setOnClickListener(View.OnClickListener {
                var status = false
                if ( "Y" == item.is_wish )
                    status = false
                else
                    status = true
                processZzimStatus(holder.itemView.context, status, item.strType, item.idx)
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setEditMode(mode: Boolean) {
        this.isEditMode = mode

        notifyDataSetChanged()
    }

    fun getEditMode(): Boolean = this.isEditMode

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setItems(produts:List<API145.ProductItem>) {
        this.items.clear()
        this.items.addAll(produts)

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

    fun getSelectedId(): String {
        var key = ""

        for (i in 0 until items.size) {
            if (items[i].isSelect) {
                key += "${items[i].strType},${items[i].idx}^|^"
            }
        }

        return key
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

        mListener?.updateSelectCount(items.size, items.size)
        mListener?.updateTotalCount(items.size)

        notifyDataSetChanged()
    }

    fun selectItemDel() {
        val it = items.iterator()
        while(it.hasNext()) {
            val item = it.next()
            if(item.isSelect) {
                it.remove()
            }
        }

        mListener?.updateTotalCount(items.size)
        mListener?.updateSelectCount(0, items.size)

        notifyDataSetChanged()
    }

    class ZzimHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item:API145.ProductItem)
        fun updateSelectCount(selected:Int, total:Int)
        fun updateTotalCount(count: Int)
    }

    private fun getRate(originPrice: Double, price: Double): String {
        var result = ""
        if (originPrice == 0.0 || price == 0.0) {
            return ""
        }

        try {
            if (originPrice >= 0) {
                val rate = (originPrice - price) / originPrice
                Logger.d("rate :: $rate")
                val dPercentage = rate * 100
                val salePercentage = dPercentage.toInt()
                if (salePercentage > 0) {
                    result = "" + salePercentage
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

    private fun processZzimStatus(context: Context, status: Boolean, type: String, idx: String) {
        try {
            val obj = JSONObject()
            obj.put("user", AppPreferences.getUserId(context))
            obj.put("is_wish", if (status == true) "Y" else "N")
            obj.put("type", type)

            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString())
            val bus = NetworkBus(NetworkApi.API126.name, idx, body)
            EventBus.getDefault().post(bus)

            EventBus.getDefault().post(ZzimStatusBus(idx, if (status == true) "Y" else "N", ""))
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

    fun setLikeChaged(idx: String?, status: String) {
        if (idx == null || items == null)
            return
        Logger.e("mItems.size :: " + items.size)
        for (i in items.indices) {
            if (idx == items.get(i).idx) {

                val item = items.get(i)
                if (item.is_wish != status) {
//                    var iWishCnt = 0
//                    try {
//                        iWishCnt = Integer.valueOf(item.wish_cnt)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//
//                    Logger.e("item.wish_cnt = > $iWishCnt")
//                    if ("N" == status) {
//                        if (iWishCnt > 0) {
//                            iWishCnt--
//                        }
//                    } else {
//                        iWishCnt++
//                    }
//
//                    item.is_wish = status
//                    item.wish_cnt = iWishCnt.toString() + ""
//                    Logger.e("item wish :: " + item.is_wish)
//                    Logger.e("item.wish_cnt = > $iWishCnt")
//                    items.set(i, item)

                    items.removeAt(i)

                    notifyDataSetChanged()
                }
            }
        }
    }
}