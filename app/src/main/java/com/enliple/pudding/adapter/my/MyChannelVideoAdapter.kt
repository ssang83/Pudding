package com.enliple.pudding.adapter.my

import android.graphics.Bitmap
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API0
import kotlinx.android.synthetic.main.adapter_my_channel_video_item.view.*


/**
 * My - My 영상 RecyclerView.Adapter
 * @author hkcha
 * @since 2018.08.07
 */
class MyChannelVideoAdapter(layoutWidth: Int) : androidx.recyclerview.widget.RecyclerView.Adapter<MyChannelVideoAdapter.VideoHolder>() {

    private var mListener: Listener? = null
    val items: MutableList<API0.DataBeanX> = mutableListOf()
    private var tempVODThumnailBitmap: Bitmap? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        return VideoHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_my_channel_video_item, parent, false))
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        var screenWidth = AppPreferences.Companion.getScreenWidth(holder.itemView.context)
        var rootWidth = (screenWidth - Utils.ConvertDpToPx(holder.itemView.context, 24)) / 2
        var rootHeight = (199 * rootWidth) / 162 // 디자인 시안은 162 : 144 인데 실제 적용시 거의 정사각형에 가깝도록 나와서 세로 길이를 좀 줄임 ( 다현 과장님과 협의 완료 )
        var pa = holder.itemView.main_root.layoutParams
        pa.width = rootWidth
        pa.height = rootHeight
        holder.itemView.main_root.layoutParams = pa

        pa = holder.itemView.contentBg.layoutParams as RelativeLayout.LayoutParams
        pa.width = rootWidth
        pa.height = (rootHeight / 2)

        holder.itemView.contentBg.layoutParams = pa

        val item = getItem(position)

        if (item != null) {
/*            holder.itemView.checkBoxLike.isChecked = item.isLike
            holder.itemView.checkBoxLike.setOnCheckedChangeListener { _, isChecked ->
                mListener?.onMyLikeStatusChanged(isChecked, position)
            }*/
            holder.itemView.imageViewLike.visibility = View.VISIBLE
            holder.itemView.textViewLikeCount.visibility = View.VISIBLE
            holder.itemView.textViewTitle.visibility = View.VISIBLE
            holder.itemView.layoutTemporarily.visibility = View.GONE
            holder.itemView.imageViewDim.visibility = View.GONE

            holder.itemView.textViewLikeCount.text = item.favoriteCount
            holder.itemView.textViewTitle.text = item.title

            setLikeStatus(item.isFavorite, holder)
            Logger.e("large test thumbnale change large")
            ImageLoad.setImage(holder.itemView.context, holder.itemView.imageViewThumbnail, item.largeThumbnailUrl, null, ImageLoad.SCALE_CENTER_CROP, DiskCacheStrategy.ALL)

            holder.itemView.imageViewThumbnail.setOnClickListener { mListener?.onItemClicked(item, position) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int): API0.DataBeanX? = if (position in 0 until items.size) items[position] else null

    fun setItems(items: List<API0.DataBeanX>) {
        this.items.clear()

        if (items != null) {
            this.items.addAll(items)
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener?) {
        mListener = listener
    }

    private fun setLikeStatus(isFavorite:String, holder: VideoHolder) {
        if(isFavorite == "N") {
            holder.itemView.imageViewLike.setBackgroundResource(R.drawable.my_search_like_ico)
            holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#ffffff"))
        } else {
            holder.itemView.imageViewLike.setBackgroundResource(R.drawable.like_on_ico)
            holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#ff6c6c"))
        }
    }

    class VideoHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onMyLikeStatusChanged(isMyLike: Boolean, itemPosition: Int)

        fun onItemClicked(item: API0.DataBeanX, position: Int)
    }
}