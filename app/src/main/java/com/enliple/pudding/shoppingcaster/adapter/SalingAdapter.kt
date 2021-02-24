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
import kotlinx.android.synthetic.main.adapter_saling_item.view.*

class SalingAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<SalingAdapter.SelectedHolder> {
    companion object {
        private const val MAX_ITEM_SIZE = 10
    }

    private val items: MutableList<com.enliple.pudding.shoppingcaster.data.SalingItem> = ArrayList()
    private var mListener: SalingAdapter.Listener? = null
    private var mFullListener: SalingAdapter.FullListener? = null
    private var gubun = ""

    constructor(gubun: String) {
        this.gubun = gubun
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedHolder {
        Logger.e("adapter onCreateViewHolder")
        return SelectedHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_saling_item, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SelectedHolder, position: Int) {
        val item = getItem(position)
        //val imagePath: String? = item!!.image

        if (item != null) {
            var quantityStr = item?.quantity
            var rateStr = item?.rate
            Logger.e("quantity:$quantityStr rate:$rateStr")

            if (quantityStr.isNullOrEmpty() && rateStr.isNullOrEmpty()) {
                holder.itemView.reservationLayer.visibility = View.GONE
            } else {
                holder.itemView.reservationLayer.visibility = View.VISIBLE

                if (item.isLink) {
                    var ad = item?.adUsemoney
                    if (!ad.isNullOrEmpty()) {
                        holder.itemView.quantity.text = "1일예산:${item?.adUsemoney}"
                    }
                } else {
                    if (!quantityStr.isNullOrEmpty()) {
                        holder.itemView.quantity.text = "재고:$quantityStr"
                    }
                    holder.itemView.rate.text = "수익수수료:$rateStr%"
                }
            }

            var name: String = item.name
            var storeName: String = item.storeName
            var image: String = item.image
            var price: String = item.price
            var isLink: Boolean = item.isLink
            var type = item.productType
            Logger.e("adapter name :: $name")
            Logger.e("adapter storeName :: $storeName")
            Logger.e("adapter image :: $image")
            Logger.e("adapter price :: $price")
            Logger.e("adapter isLink :: $isLink")

            if (isLink) {
                holder.itemView.product_layer.visibility = View.GONE
                holder.itemView.link_layer.visibility = View.VISIBLE

                holder.itemView.name.text = item.name
                holder.itemView.linkName.text = item.storeName
            } else {
                if ( type == 3 ) {
                    holder.itemView.product_layer.visibility = View.GONE
                    holder.itemView.link_layer.visibility = View.VISIBLE

                    holder.itemView.name.text = item.name
                    holder.itemView.linkName.text = item.storeName
                } else if ( type == 1 ) {
                    holder.itemView.product_layer.visibility = View.VISIBLE
                    holder.itemView.link_layer.visibility = View.GONE

                    holder.itemView.name.text = item.name
                    holder.itemView.storeName.text = item.storeName
                }
            }

            holder.itemView.price.text = "${String.format(holder.itemView.context.getString(R.string.msg_price_format),
                    PriceFormatter.getInstance()!!.getFormattedValue(item.price))}"

//            Glide.with(holder.itemView.context)
//                    .load(item!!.image)
//                    .asBitmap()
//                    .priority(Priority.HIGH)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(holder.itemView.image)

            ImageLoad.setImage(holder.itemView.context, holder.itemView.image, item?.image, R.drawable.product_no_img, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

            holder.itemView.del.setOnClickListener {
                if (isLink) {
                    mListener?.onDelClicked(item)
                } else {
                    this.items.remove(item)
                    if (this.items.size < MAX_ITEM_SIZE) {
                        mFullListener!!.onItemFull(true)
                    }
                    notifyDataSetChanged()
                }
            }
        } else {
            Logger.e("adapter item null")
        }
    }

    fun setListener(listener: Listener?) {
        mListener = listener
    }

    fun setFullListener(listener: FullListener?) {
        mFullListener = listener
    }

    fun setItems(items: List<com.enliple.pudding.shoppingcaster.data.SalingItem>) {
        this.items.clear()
        if (items != null) {
            this.items.addAll(items)
        }
        notifyDataSetChanged()
    }

    fun addItem(item: com.enliple.pudding.shoppingcaster.data.SalingItem) {

        if (this.items != null) {
            if (this.items.size >= MAX_ITEM_SIZE) {
                mFullListener!!.onItemFull(false)
            } else {
                this.items.add(item)
                Logger.e("adapter addItem name :: ${item.name}")
            }
        } else {
            Logger.e("Adapter addItem null")
        }
        notifyDataSetChanged();
    }

    fun clearItems() {
        this.items.clear()
        notifyDataSetChanged()
    }

    fun delItem(position: Int) {
        if (items != null) {
            this.items.removeAt(position)
        }
        notifyDataSetChanged()
    }

    fun deleteLink(item: com.enliple.pudding.shoppingcaster.data.SalingItem) {
        this.items.remove(item)
        if (this.items.size < MAX_ITEM_SIZE) {
            mFullListener!!.onItemFull(true)
        }
        notifyDataSetChanged()
    }

    fun getItem(position: Int): com.enliple.pudding.shoppingcaster.data.SalingItem? = if (position in 0 until items.size) items[position] else null

    fun hasSameItem(type: Int, idx: String, title: String): Boolean {
        if (type == 1) { // 상품
            for (i in 0 until this.items.size) {
                if (items.get(i).productIndex == idx) {
                    return true
                }
            }
            return false
        } else if (type == 2) { // 링크
            for (i in 0 until this.items.size) {
                if (items.get(i).name == title)
                    return true
            }
            return false
        } else if ( type == 3 ) {
            for (i in 0 until this.items.size) {
                if (items.get(i).productIndex == idx) {
                    return true
                }
            }
            return false
        } else {
            return false
        }
    }

    class SelectedHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onDelClicked(item: com.enliple.pudding.shoppingcaster.data.SalingItem)
    }

    interface FullListener {
        fun onItemFull(isFull: Boolean)
    }
}