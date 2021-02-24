package com.enliple.pudding.shoppingcaster.adapter

import android.graphics.drawable.Drawable
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.R
import com.enliple.pudding.shoppingcaster.data.BucketInfo
import kotlinx.android.synthetic.main.layout_bucket_list_item.view.*

/**
 * Created by hkcha on 2018. 2. 22..
 * Bucket List Popup Window RecyclerView Adapter
 */
class PhotoBucketAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<PhotoBucketAdapter.BucketViewHolder>() {

    // 버킷 정보
    private var mBucketList: MutableList<BucketInfo> = ArrayList()
    var clickListener: BucketClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BucketViewHolder {
        var itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_bucket_list_item, null, false)
        return BucketViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BucketViewHolder, position: Int) {
        var data: BucketInfo? = getItem(position)
        if (holder != null && data != null) {

            if (TextUtils.isEmpty(data.latestMediaPath)) {
                holder.itemView.mImgThumbNail.setImageResource(0)
            } else {
                val options = RequestOptions()
                options.dontAnimate()
                val drawableRequest = Glide.with(holder.itemView.context).setDefaultRequestOptions(options).load("file://" + data.latestMediaPath)
                drawableRequest.listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        Logger.e("onBindView onLoadFailed")
                        holder.itemView.mProgress.visibility = View.INVISIBLE
                        holder.itemView.mImgThumbNail.setImageResource(android.R.drawable.ic_dialog_alert)
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        Logger.e("onBindView onResourceReady")
                        holder.itemView.mProgress.visibility = View.INVISIBLE
                        return false
                    }
                })
                drawableRequest.into(holder.itemView.mImgThumbNail)

//                Glide.with(holder.itemView.context)
//                        .load("file://" + data.latestMediaPath)
//                        .listener(object:RequestListener<String, GlideDrawable> {
//                            override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?,
//                                                     isFirstResource: Boolean): Boolean {
//                                holder.itemView.mProgress.visibility = View.INVISIBLE
//                                holder.itemView.mImgThumbNail.setImageResource(android.R.drawable.ic_dialog_alert)
//
//                                return false
//                            }
//
//                            override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?,
//                                                         isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
//                                holder.itemView.mProgress.visibility = View.INVISIBLE
//
//                                return false
//                            }
//                        })
//                        .into(holder.itemView.mImgThumbNail)
            }

            Logger.e("data name: ${data.name}  ${data.path}")

            if (data.name.equals("0", true) || data.name.equals("/", true)) {
                holder.itemView.mBucketName.text = data.name
            } else {
                if ("전체보기".equals(data.name))
                    holder.itemView.mBucketName.text = data.name
                else {
                    var root = Environment.getExternalStorageDirectory().toString()
                    var sdCardRoot = ContextCompat.getExternalFilesDirs(holder.itemView.context, null)
                    var rest: String = ""
                    /**
                     * ContextCompat.getExternalFilesDirs(holder.itemView.context, null) 결과
                     * /storage/emulated/0/Android/data/com.enliple.pudding/files
                     * /storage/9C33-6BBD/Android/data/com.enliple.pudding/file
                     * /storage/emulated/0은 root 이고 /storage/9C33-6BBD는 SD CARD 경로
                     * 이 2가지를 제외한 나머지는 같으므로
                     * 1. 첫번째 경로 즉 ROOT가 포함된 경로에서 ROOT를 제외한 부분을 먼저 구함 (/Android/data/com.enliple.pudding/files)
                     * 2. 그 다음 경로에서 1번에서 구한 경로부분을 공백으로 치환하면 SD CARD 들의 경로가 나옴
                     * 3. data.path에 2번에서 구한 값들이 포함되면 이값은 sdcard sdcard1 등으로 치환 시킴
                     */
                    for (i in 0 until sdCardRoot.size) {
                        Logger.e("scCardRoot[${i}] :: ${sdCardRoot.get(i)}")
                        if (sdCardRoot.get(i).toString().contains(root.toString())) {
                            rest = sdCardRoot.get(i).toString().replace(root, "")
                            break
                        }
                    }
                    if (!rest!!.isEmpty()) {
                        var headerArray = ArrayList<String>()
                        for (i in 0 until sdCardRoot.size) {
                            if (!sdCardRoot.get(i).toString().contains(root.toString())) {
                                var header = sdCardRoot.get(i).toString().replace(rest, "")
                                headerArray.add(header)
                            }
                        }
                        var strPath = ""
                        if (data.path.contains(root)) {
                            strPath = data.path.replace(root, "내장 메모리")
                        } else {
                            for (i in 0 until headerArray.size) {
                                if (data.path.contains(headerArray.get(i))) {
                                    if (i == 0) {
                                        strPath = data.path.replace(headerArray.get(i), "SD 카드")
                                    } else {
                                        strPath = data.path.replace(headerArray.get(i), "SD 카드" + i)
                                    }
                                }
                            }
                        }
                        holder.itemView.mBucketName.text = strPath
                    }
                }
            }

            holder.itemView.mFileNumber.text = "${data.mediaCount}"
            holder.itemView.setOnClickListener { _ ->
                Logger.e("item clicked BuckedItemClicked called :: $position")
                clickListener?.onBucketItemClicked(position)
            }

//            holder.itemView.mItemLine.visibility = if (position == mBucketList.size - 1) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount(): Int = mBucketList.size

    private fun getItem(position: Int): BucketInfo? = mBucketList[position]

    fun setItems(list: MutableList<BucketInfo>) {
        mBucketList.clear()
        mBucketList.addAll(list)
        notifyDataSetChanged()
    }

    class BucketViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)


    open interface BucketClickListener {
        fun onBucketItemClicked(position: Int)
    }
}