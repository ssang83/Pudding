package com.enliple.pudding.adapter.home

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.network.vo.API82
import kotlinx.android.synthetic.main.adapter_cookie_sender_item.view.*

/**
 * Created by Kim Joonsung on 2018-12-12.
 */
class CookieSenderAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<CookieSenderAdapter.CookieSenderHolder>() {

    private val items: MutableList<API82.CookieItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CookieSenderHolder {
        return CookieSenderHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_cookie_sender_item, parent, false))
    }

    override fun onBindViewHolder(holder: CookieSenderHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.itemView.textViewNickName.text = item.mb_nick

//            Glide.with(holder.itemView.context)
//                    .load(item.mb_user_img)
//                    .bitmapTransform(CropCircleTransformation(holder.itemView.context))
//                    .priority(Priority.HIGH)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(holder.itemView.imageViewThumbnail)
            ImageLoad.setImage(holder.itemView.context, holder.itemView.imageViewThumbnail, item.mb_user_img, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)
            holder.itemView.setOnClickListener {
                if (holder.itemView.textViewNickName.visibility == View.GONE) {
                    holder.itemView.textViewNickName.visibility = View.VISIBLE
                } else {
                    holder.itemView.textViewNickName.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list: List<API82.CookieItem>) {
        this.items.clear()
        if (list != null) this.items.addAll(list) else {
        }
    }

    private fun getItem(position: Int): API82.CookieItem? = if (position in 0 until items.size) items[position] else null

    class CookieSenderHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}