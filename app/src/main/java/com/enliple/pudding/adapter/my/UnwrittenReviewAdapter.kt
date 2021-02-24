package com.enliple.pudding.adapter.my

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.network.vo.API68
import kotlinx.android.synthetic.main.adapter_unwritten_review_item.view.*

/**
 * Created by Kim Joonsung on 2018-12-14.
 */
class UnwrittenReviewAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_FOOTER = 0
    private val TYPE_ITEM = 1

    private val items: MutableList<API68.DataBean.OrderItem> = ArrayList()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == TYPE_ITEM) {
            return UnwrittenReviewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_unwritten_review_item, parent, false))
        } else {
            return FooterHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_unwritten_review_footer, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == TYPE_ITEM) {
            onBindItem(holder as UnwrittenReviewHolder, position)
        }
    }

    private fun onBindItem(holder: UnwrittenReviewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            val array = item.regdate.split(" ")
            val time = array[0]
            val aTime = time.split("-")
            val fTime = "${aTime[0]}년${aTime[1]}월${aTime[2]}일"
            holder.itemView.textViewDate.text = fTime

            holder.itemView.layoutProductContainer.removeAllViews()
            if (item.items != null) {
                for (i in 0 until item.items.size) {
                    for (j in 0 until item.items.get(i).item.size) {
                        val items = item.items.get(i);
                        val productData = items.item.get(j)
                        val productHolder = ProductHolder(holder.itemView.context, holder.itemView.layoutProductContainer)

                        productHolder.textViewTitle.text = productData.title
                        productHolder.textViewOption.text = productData.option
                        productHolder.textViewShopName.text = items.store_name
                        productHolder.textViewStatus.text = productData.status

                        if("구매 확정" == productData.status
                                || "배송 완료" == productData.status) {
                            productHolder.buttonReivew.visibility = View.VISIBLE
                        } else {
                            productHolder.buttonReivew.visibility = View.GONE
                        }

                        ImageLoad.setImage(productHolder.rootView.context, productHolder.imageViewThumbnail, productData.img, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)
                        holder.itemView.layoutProductContainer.addView(productHolder.rootView)

                        productHolder.buttonDeliveryCheck.setOnClickListener(object:OnSingleClickListener() {
                            override fun onSingleClick(v: View?) {
                                mListener?.onDeliverCheck(productData)
                            }
                        })

                        productHolder.buttonReivew.setOnClickListener {
                            mListener?.onReviewClicked(productData, item.orderNumber, item.regdate, items.store_name, productData.status)
                        }

                        productHolder.rootView.setOnClickListener(object:OnSingleClickListener() {
                            override fun onSingleClick(v: View?) {
                                mListener?.onDetailClicked(item.orderNumber)
                            }
                        })
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (items == null) 0 else items.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size) {
            TYPE_FOOTER
        } else {
            TYPE_ITEM
        }
    }

    fun setItem(list: List<API68.DataBean.OrderItem>) {
        this.items.clear()
        if (list != null) this.items.addAll(list) else {
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    private fun getItem(position: Int): API68.DataBean.OrderItem? = if (position in 0 until items.size) items[position] else null

    class UnwrittenReviewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    class ProductHolder(context: Context, root: ViewGroup) {

        var rootView: LinearLayout
        var imageViewThumbnail: AppCompatImageView
        var textViewTitle: AppCompatTextView
        var textViewShopName: AppCompatTextView
        var textViewOption: AppCompatTextView
        var textViewStatus: AppCompatTextView
        var buttonReivew: AppCompatTextView
        var buttonDeliveryCheck:View

        init {
            rootView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_review_product, root, false) as LinearLayout

            imageViewThumbnail = rootView.findViewById(R.id.imageViewThumbnail)
            textViewTitle = rootView.findViewById(R.id.textViewTitle)
            textViewOption = rootView.findViewById(R.id.textViewOption)
            textViewShopName = rootView.findViewById(R.id.textViewShopName)
            buttonReivew = rootView.findViewById(R.id.buttonReview)
            buttonDeliveryCheck = rootView.findViewById(R.id.buttonDeliveryCheck)
            textViewStatus = rootView.findViewById(R.id.textViewStatus)
        }
    }

    internal class FooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onReviewClicked(item: API68.DataBean.OrderItem.ItemsBean.ProductItem, orderNo: String, regDate: String, shopName: String, status:String)
        fun onDetailClicked(orderNo: String)
        fun onDeliverCheck(item: API68.DataBean.OrderItem.ItemsBean.ProductItem)
    }
}