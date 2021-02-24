package com.enliple.pudding.shoppingcaster.adapter

import android.graphics.Bitmap
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.shoppingcaster.data.VODInfo
import kotlinx.android.synthetic.main.layout_gallery_list_item.view.*
import java.io.ByteArrayOutputStream

/**
 */
class VODSelectAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<VODSelectAdapter.VODGridHolder>() {
    companion object {
        private const val TAG = "VODSelectAdapter"
    }

    private val items: MutableList<VODInfo> = ArrayList()
    private var mListener: Listener? = null

    // MultiSelect 관련
    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VODGridHolder {
        return VODGridHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_gallery_list_item, null, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VODGridHolder, position: Int) {
        // 현재 편집 상태 모드
        holder.itemView.mCheckBoxSelect.isChecked = selectedPosition == position
        if (holder.itemView.mCheckBoxSelect.isChecked) {
            holder.itemView.angle.visibility = View.VISIBLE
        } else {
            holder.itemView.angle.visibility = View.GONE
        }

        val item = getItem(position)
        if (item != null) {
            var thumbnails = getVideoThumbnail(holder, item.id)
            if (thumbnails != null) {
                val stream = ByteArrayOutputStream()
                thumbnails.compress(Bitmap.CompressFormat.PNG, 100, stream)

                val drawableRequest = Glide.with(holder.itemView.context).asBitmap().load(stream.toByteArray())
                drawableRequest.listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        holder.itemView.mImgThumbNail.visibility = View.INVISIBLE
                        holder.itemView.mBrokenImage.visibility = View.VISIBLE
                        holder.itemView.videoPlayImage.visibility = View.GONE

                        holder.itemView.setOnClickListener(null)
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        holder.itemView.mImgThumbNail.visibility = View.VISIBLE
                        holder.itemView.mBrokenImage.visibility = View.INVISIBLE
                        holder.itemView.videoPlayImage.visibility = View.VISIBLE

                        holder.itemView.setOnClickListener { mListener?.onVODItemClicked(position) }
                        return false
                    }
                })
                drawableRequest.into(holder.itemView.mImgThumbNail)
            } else {
                Logger.e("thumbnail null !!")
            }
        } else {
            Logger.d("VODInfo($position) is null")
            holder.itemView.mImgThumbNail.visibility = View.INVISIBLE
            holder.itemView.mBrokenImage.visibility = View.VISIBLE
        }
    }

    fun setItems(data: List<VODInfo>) {
        this.items.clear()
        if (data != null) {
            this.items.addAll(data)
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun getItem(position: Int): VODInfo? = if (position in 0..items.size) items[position] else null

    /**
     * 해당 포지션의 선택상태를 설정
     * @param position              선택 상태가 변경되는 Position
     * @param selected              다중선택시 선택상태(단일 상태에서는 항상 true)
     */
    fun setSelectIndex(position: Int) {
        Logger.e("setSelectIndex - position : $position")
        var releaseItemPosition: Int = selectedPosition

        selectedPosition = position
        if (releaseItemPosition != -1) {
            Logger.e("Item last selected status changed : previous $releaseItemPosition, present $selectedPosition")
            notifyItemChanged(releaseItemPosition)
        }

        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition)
        }
    }

    private fun getVideoThumbnail(holder: VODGridHolder, id: Long): Bitmap? {
        Logger.d("getVideoThumbnail id: $id")

        var bitmap: Bitmap? = null
        try {
            if (holder != null) {
                bitmap = MediaStore.Video.Thumbnails.getThumbnail(holder.itemView.context.contentResolver,
                        id, MediaStore.Video.Thumbnails.MINI_KIND, null) //MICRO_KIND :작은이미지(정사각형) MINI_KIND (중간이미지)
            }
        } catch (e: Exception) {
            Logger.p(e)
            bitmap = null
        }

        return bitmap
    }

    class VODGridHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onVODItemClicked(position: Int)
    }
}