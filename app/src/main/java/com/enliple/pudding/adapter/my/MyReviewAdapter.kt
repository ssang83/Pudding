package com.enliple.pudding.adapter.my

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.network.vo.API67
import kotlinx.android.synthetic.main.adapter_my_review_item.view.*

/**
 * Created by Kim Joonsung on 2018-12-14.
 */
class MyReviewAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<MyReviewAdapter.MyReviewHolder>() {

    private val items: MutableList<API67.ReviewItem> = ArrayList()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReviewHolder {
        return MyReviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_my_review_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyReviewHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.itemView.textViewTitle.text = item.it_name
            holder.itemView.textViewLikeCount.text = item.recommend
            holder.itemView.textViewUnLikeCount.text = item.not_recommend
            holder.itemView.textViewContent.text = item.is_content
            holder.itemView.ratingBarReview.numStars = item.is_score.toInt()

            ImageLoad.setImage(holder.itemView.context, holder.itemView.imageViewThumbnail, item.it_img1, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)
            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list: List<API67.ReviewItem>) {
        this.items.clear()
        if (list != null) this.items.addAll(list) else {
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    private fun getItem(position: Int): API67.ReviewItem? = if (position in 0 until items.size) items[position] else null

    class MyReviewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: API67.ReviewItem)
    }
}