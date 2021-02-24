package com.enliple.pudding.shoppingcaster.adapter

import android.database.Cursor
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.shoppingcaster.data.MediaInfo
import kotlinx.android.synthetic.main.layout_gallery_list_item.view.*

/**
 * Created by hkcha on 2018. 2. 22..
 * 버켓 내 등록된 미디어 리스트를 출력하기 위한 RecyclerView Adapter
 */
class PhotoSelectAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<PhotoSelectAdapter.MediaGridHolder>() {
    class MediaGridHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    companion object {
        private const val COLUMN_INDEX_KEY_ID = MediaStore.Files.FileColumns._ID
        private const val COLUMN_INDEX_KEY_DATA = MediaStore.Files.FileColumns.DATA
        private const val COLUMN_INDEX_KEY_MIME_TYPE = MediaStore.Files.FileColumns.MIME_TYPE
    }

    var listener: Listener? = null

    // 데이터 원본
    private var mMediaCursor: Cursor? = null

    // Column 호출을 빠르게 수행하기 위해 사전 등록되는 MediaStore Cursor Column index
    private var columnIndexId = 0
    private var columnIndexData = 0
    private var columnIndexMimeType = 0

    // MultiSelect 관련
    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaGridHolder {
        var holderView = LayoutInflater.from(parent?.context).inflate(R.layout.layout_gallery_list_item, null, false)!!
        return MediaGridHolder(holderView)
    }

    override fun getItemCount(): Int {
        return if (mMediaCursor == null || mMediaCursor?.isClosed != false || mMediaCursor?.count == 0) {
            0
        } else mMediaCursor?.count!!
    }

    override fun onBindViewHolder(holder: MediaGridHolder, position: Int) {
        holder.itemView.mCheckBoxSelect.isChecked = selectedPosition == position
        if ( holder.itemView.mCheckBoxSelect.isChecked ) {
            holder.itemView.angle.visibility = View.VISIBLE
        } else {
            holder.itemView.angle.visibility = View.GONE
        }
        var mediaInfo = getMediaInfo(position)
        if (mediaInfo != null) {
            val options = RequestOptions()
            options.dontAnimate()
            val drawableRequest = Glide.with(holder.itemView.context).setDefaultRequestOptions(options).load("file://${mediaInfo?.path}")
            drawableRequest.listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    Logger.e("Image Load failed [$position, ${mediaInfo?.path}] : $e")
                    if (holder.itemView.mImgThumbNail.visibility != View.INVISIBLE) {
                        holder.itemView.mImgThumbNail.visibility = View.INVISIBLE
                    }

                    if (holder.itemView.mBrokenImage.visibility != View.VISIBLE) {
                        holder.itemView.mBrokenImage.visibility = View.VISIBLE
                    }

                    holder.itemView.setOnClickListener(null)
                    holder.itemView.setOnLongClickListener(null)
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    if (holder.itemView.mImgThumbNail.visibility != View.VISIBLE) {
                        holder.itemView.mImgThumbNail.visibility = View.VISIBLE
                    }

                    if (holder.itemView.mBrokenImage.visibility != View.INVISIBLE) {
                        holder.itemView.mBrokenImage.visibility = View.INVISIBLE
                    }
                    holder.itemView.setOnClickListener {
                        if (listener != null) {
                            listener!!.onMediaItemClicked(position)
                        }
                    }
                    return false
                }
            })
            drawableRequest.into(holder.itemView.mImgThumbNail)
        } else {
            Logger.e("MediaInfo($position) is null")
            holder.itemView.mImgThumbNail.visibility = View.INVISIBLE
            holder.itemView.mBrokenImage.visibility = View.VISIBLE
        }
    }

    /**
     * Adapter 내 설정된 Cursor 를 Close
     */
    open fun closeCursor() {
        mMediaCursor?.close()
        notifyDataSetChanged()
    }

    /**
     * 현재 Data 원본을 Access 할 수 있는 상태 인지 확인
     */
    open fun isClosedCursor(): Boolean = (mMediaCursor == null || mMediaCursor?.isClosed!!)

    /**
     * RecyclerView 에 표시할 MediaStore 로 부터 Bucket 에 질의한 Data 원본을 설정
     */
    open fun setCursor(newCursor: Cursor?) {
        closeCursor()

        mMediaCursor = newCursor

        if (mMediaCursor != null && mMediaCursor?.isClosed == false) {
            columnIndexId = mMediaCursor?.getColumnIndex(COLUMN_INDEX_KEY_ID)!!
            columnIndexData = mMediaCursor?.getColumnIndex(COLUMN_INDEX_KEY_DATA)!!
            columnIndexMimeType = mMediaCursor?.getColumnIndex(COLUMN_INDEX_KEY_MIME_TYPE)!!
        }

        notifyDataSetChanged()
    }

    /**
     * Adapter 내 내용이 비어있는지 확인
     * @return
     */
    open fun isEmpty(): Boolean = (mMediaCursor == null || mMediaCursor?.count == 0)

    /**
     * 현재 설정된 Cursor 상의 Media 정보를 Media 객체로 변환하여 반환
     */
    open fun getMediaInfo(position: Int): MediaInfo? {
        return if (mMediaCursor != null && mMediaCursor?.isClosed == false && position < mMediaCursor?.count ?: 0 && position >= 0) {
            mMediaCursor?.moveToPosition(position)

            var id = mMediaCursor?.getLong(columnIndexId)
            var path = mMediaCursor?.getString(columnIndexData)
            var mimeType = mMediaCursor?.getString(columnIndexMimeType)

            MediaInfo(id!!, path, mimeType)
        } else {
            null
        }
    }

    /**
     * 해당 포지션의 선택상태를 설정
     * @param position              선택 상태가 변경되는 Position
     * @param selected              다중선택시 선택상태(단일 상태에서는 항상 true)
     */
    open fun setSelectedIndex(position: Int) {
        Logger.e("setSelectedIndex - position : $position")
        var releaseItemPosition: Int = selectedPosition

        selectedPosition = position
        if (releaseItemPosition != -1) {
            Logger.d("Item last selected status changed : previous $releaseItemPosition, present $selectedPosition")
            notifyItemChanged(releaseItemPosition)
        }

        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition)
        }
    }

    open interface Listener {
        /**
         * 미디어 아이템을 클릭하였음
         */
        fun onMediaItemClicked(position: Int)
    }
}