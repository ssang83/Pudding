package com.enliple.pudding.adapter.my

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.model.CoverMultiSelectModel
import kotlinx.android.synthetic.main.layout_gallery_multi_list_item.view.*
import java.io.File

/**
 * Created by hkcha on 2018. 2. 22..
 * 버켓 내 등록된 미디어 리스트를 출력하기 위한 RecyclerView Adapter
 */
class PhotoMultiSelectAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<PhotoMultiSelectAdapter.MediaGridHolder> {
    class MediaGridHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    companion object {
        private const val TAG = "PhotoMultiSelectAdapter"
        public const val COLUMN_INDEX_KEY_ID = MediaStore.Files.FileColumns._ID
        public const val COLUMN_INDEX_KEY_DATA = MediaStore.Files.FileColumns.DATA
        public const val COLUMN_INDEX_KEY_MIME_TYPE = MediaStore.Files.FileColumns.MIME_TYPE
    }

    private var items: MutableList<CoverMultiSelectModel> = ArrayList()
    var listener: Listener? = null
    var selectedImageCount = 0
//    var selectedPosition = -1

    constructor(selectedImageCount: Int) {
        this.selectedImageCount = selectedImageCount
    }

    fun setItems(datum: List<CoverMultiSelectModel>) {
        if (items != null) {
            items.clear()
            items = ArrayList()
        } else {
            items = ArrayList()
        }
        items.addAll(datum)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaGridHolder {
        var holderView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.layout_gallery_multi_list_item, null, false)!!
        return MediaGridHolder(holderView)
    }

    override fun getItemCount(): Int {
        if (items != null && items.size > 0)
            return items.size
        else
            return 0
    }

    override fun getItemId(position: Int): Long {
        return if (hasStableIds()) {
            position.toLong()
        } else {
            0
        }
    }

    override fun onBindViewHolder(holder: MediaGridHolder, position: Int) {
        object : AsyncTask<Void, Void, Void>() {
            var selectModel: CoverMultiSelectModel? = null

            override fun onPreExecute() {
                // 현재 편집 상태 모드
                if (items.get(position).isSelected)
                    holder.itemView.mCheckBoxSelect.setBackgroundResource(R.drawable.photo_check_on)
                else
                    holder.itemView.mCheckBoxSelect.setBackgroundResource(R.drawable.photo_check_off)
            }

            override fun doInBackground(vararg params: Void?): Void? {
                selectModel = items.get(position)
                return null
            }

            override fun onPostExecute(result: Void?) {
                if (selectModel != null && selectModel!!.getInfo() != null) {
                    val options = RequestOptions()
                    options.dontAnimate()
                    val drawableRequest = Glide.with(holder.itemView.context).setDefaultRequestOptions(options).load("file://${selectModel!!.getInfo()?.path}")
                    drawableRequest.listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                            Logger.e(TAG, "Image Load failed [$position, ${selectModel!!.getInfo()?.path}] : $e")

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

                            holder.itemView.setOnClickListener { _ ->
                                //                                        listener?.onMediaItemClicked(position)
                                var selected = selectModel!!.isSelected
                                var info = selectModel!!.getInfo()
                                selected = !selected
                                var selectedCount = 0
                                for (i in 0 until items.size) {
                                    var selected = items.get(i).isSelected
                                    if (selected)
                                        selectedCount++
                                }
                                Logger.e("before selectedCount :: $selectedCount")
                                if (selected) {
                                    selectedCount++
                                } else {
                                    selectedCount--
                                }
                                Logger.e("selectedCount :: $selectedCount and selectedImageCount :: $selectedImageCount")
                                // 현재 등록되어 있는 이미지 갯수와 등록하려고 선택한 이미지의 갯수의 합이 6개가 넘어갈 경우 추가 선택을 할 수 없도록한다.
                                if (selectedCount + selectedImageCount <= 6) {
                                    var model = CoverMultiSelectModel()
                                    model.setSelected(selected)
                                    model.setInfo(info)

                                    items.removeAt(position)
                                    items.add(position, model)

                                    notifyDataSetChanged()
                                } else {
                                    Toast.makeText(holder.itemView.context, "이미지는 최대 6개까지 등록하실 수 있습니다", Toast.LENGTH_SHORT).show()
                                }
                            }
                            return false
                        }
                    })
                    drawableRequest.into(holder.itemView.mImgThumbNail)

//                    Glide.with(holder.itemView.context)
//                            .load("file://${selectModel!!.getInfo()?.path}")
//                            .dontAnimate()
//                            .priority(Priority.HIGH)
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .listener(object:RequestListener<String, GlideDrawable> {
//                                override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?,
//                                                         isFirstResource: Boolean): Boolean {
//                                    Logger.e(TAG, "Image Load failed [$position, ${selectModel!!.getInfo()?.path}] : $e")
//
//                                    if (holder.itemView.mImgThumbNail.visibility != View.INVISIBLE) {
//                                        holder.itemView.mImgThumbNail.visibility = View.INVISIBLE
//                                    }
//
//                                    if (holder.itemView.mBrokenImage.visibility != View.VISIBLE) {
//                                        holder.itemView.mBrokenImage.visibility = View.VISIBLE
//                                    }
//
//                                    holder.itemView.setOnClickListener(null)
//                                    holder.itemView.setOnLongClickListener(null)
//                                    return false
//                                }
//
//                                override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?,
//                                                             isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
//                                    if (holder.itemView.mImgThumbNail.visibility != View.VISIBLE) {
//                                        holder.itemView.mImgThumbNail.visibility = View.VISIBLE
//                                    }
//
//                                    if (holder.itemView.mBrokenImage.visibility != View.INVISIBLE) {
//                                        holder.itemView.mBrokenImage.visibility = View.INVISIBLE
//                                    }
//
//                                    holder.itemView.setOnClickListener { _ ->
////                                        listener?.onMediaItemClicked(position)
//                                        var selected = selectModel!!.isSelected
//                                        var info = selectModel!!.getInfo()
//                                        selected = !selected
//                                        var selectedCount = 0
//                                        for ( i in 0 until items.size ) {
//                                            var selected  = items.get(i).isSelected
//                                            if ( selected )
//                                                selectedCount ++
//                                        }
//                                        Logger.e("before selectedCount :: $selectedCount")
//                                        if ( selected ) {
//                                            selectedCount++
//                                        } else {
//                                            selectedCount--
//                                        }
//                                        Logger.e("selectedCount :: $selectedCount and selectedImageCount :: $selectedImageCount")
//                                        // 현재 등록되어 있는 이미지 갯수와 등록하려고 선택한 이미지의 갯수의 합이 6개가 넘어갈 경우 추가 선택을 할 수 없도록한다.
//                                        if ( selectedCount + selectedImageCount <= 6) {
//                                            var model = CoverMultiSelectModel()
//                                            model.setSelected(selected)
//                                            model.setInfo(info)
//
//                                            items.removeAt(position)
//                                            items.add(position, model)
//
//                                            notifyDataSetChanged()
//                                        } else {
//                                            Toast.makeText(holder.itemView.context, "이미지는 최대 6개까지 등록하실 수 있습니다", Toast.LENGTH_SHORT).show()
//                                        }
//                                    }
//
//                                    return false
//                                }
//                            })
//                            .into(holder.itemView.mImgThumbNail)
                } else {
                    Logger.e(TAG, "MediaInfo($position) is null")
                    holder.itemView.mImgThumbNail.visibility = View.INVISIBLE
                    holder.itemView.mBrokenImage.visibility = View.VISIBLE
                }
            }
        }.execute()
    }

    /**
     * 해당 포지션의 선택상태를 설정
     * @param position              선택 상태가 변경되는 Position
     * @param selected              다중선택시 선택상태(단일 상태에서는 항상 true)
     */
//    open fun setSelectedIndex(position:Int) {
//        Logger.e(TAG, "setSelectedIndex - position : $position")
//        var releaseItemPosition:Int = selectedPosition
//
//        selectedPosition = position
//        if (releaseItemPosition != -1) {
//            Logger.d(TAG, "Item last selected status changed : previous $releaseItemPosition, present $selectedPosition")
//            notifyItemChanged(releaseItemPosition)
//        }
//
//        if (selectedPosition != -1) {
//            notifyItemChanged(selectedPosition)
//        }
//    }

    fun getSelectedItem(): ArrayList<Uri>? {
        if (items != null && items.size > 0) {
            var array = ArrayList<Uri>()
            for (i in 0 until items.size) {
                if (items.get(i).isSelected) {
                    var path = Uri.fromFile(File(items.get(i).getInfo()?.path))
                    array.add(path)
                }
            }
            return array
        } else
            return null
    }

    open interface Listener {
        /**
         * 미디어 아이템을 클릭하였음
         */
        fun onMediaItemClicked(position: Int)
    }
}