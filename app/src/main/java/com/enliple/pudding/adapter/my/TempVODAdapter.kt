package com.enliple.pudding.adapter.my

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.CategoryModel
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.data.CategoryItem
import com.enliple.pudding.commons.data.TempVOD
import com.enliple.pudding.commons.db.VodDBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.shoppingcaster.data.BroadcastInfo
import com.enliple.pudding.widget.AppAlertDialog
import kotlinx.android.synthetic.main.adapter_temp_vod_item.view.*
import org.json.JSONObject

/**
 * Created by Kim Joonsung on 2018-11-05.
 */
class TempVODAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<TempVODAdapter.TempVODHodler> {

    private val items: MutableList<TempVOD> = ArrayList()
    private var mListener: Listener? = null
    private lateinit var mContext:Context
    private var broadCastInfo:BroadcastInfo? = null

    constructor(context:Context) : super() {
        if(context != null) {
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TempVODHodler {
        return TempVODHodler(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_temp_vod_item, parent, false))
    }

    override fun onBindViewHolder(holder: TempVODHodler, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.itemView.textViewDate.text = item.regDate
            holder.itemView.imageViewThumbnail.setImageBitmap(getThumbnailImage(item.thumbnail))

            val drawable = mContext.getDrawable(R.drawable.bg_rounding_dim_image) as GradientDrawable
            holder.itemView.imageViewThumbnail.background = drawable
            holder.itemView.imageViewThumbnail.clipToOutline = true

            broadCastInfo = getBroadCastInfo(item.broadCastInfo)

            holder.itemView.buttonDelete.setOnClickListener {
                val dialog = AppAlertDialog(holder.itemView.context)
                dialog.setTitle("임시보관함")
                dialog.setMessage("이 동영상을 삭제하시겠습니까?\n삭제 후에는 복구가 불가능합니다.")
                dialog.setLeftButton(holder.itemView.context.getString(R.string.msg_my_follow_cancel),
                        View.OnClickListener {
                            dialog.dismiss()
                        })

                dialog.setRightButton(holder.itemView.context.getString(R.string.msg_my_qna_del),
                        View.OnClickListener {
                            val document = VodDBManager.getInstance(holder.itemView.context).getDoc(item.id)
                            VodDBManager.getInstance(holder.itemView.context).delete(document)
                            items.removeAt(position)

                            if (items.size == 0) {
                                mListener?.onSetEmptyView()
                            }

                            notifyDataSetChanged()

                            dialog.dismiss()
                        })

                dialog.show()
            }

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(position)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list: List<TempVOD>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun getItem(position: Int): TempVOD? = if (position in 0 until items.size) items[position] else null

    private fun getThumbnailImage(images: ByteArray?): Bitmap {
        return BitmapFactory.decodeByteArray(images, 0, images!!.size)
    }

    private fun getBroadCastInfo(info:String):BroadcastInfo? {
        Logger.e("getBroadcastInfo :: $info")

        try {
            var infoModel = BroadcastInfo()
            var obj = JSONObject(info)
            infoModel.subject = obj.optString(BroadcastInfo.KEY_SUBJECT)
            infoModel.image = obj.optString(BroadcastInfo.KEY_IMAGE)

            var firstCategoryObj = obj.optJSONObject(BroadcastInfo.KEY_FIRST_CATEGORY)
            var firstCategoryItem: CategoryItem? = null
            if (firstCategoryObj != null) {
                var categoryId = firstCategoryObj.optString(BroadcastInfo.KEY_CATEGORY_ID)
                var categoryName = firstCategoryObj.optString(BroadcastInfo.KEY_CATEGORY_NAME)
                var categoryImage = firstCategoryObj.optString(BroadcastInfo.KEY_CATEGORY_IMAGE)
                firstCategoryItem = CategoryItem(0L, categoryId, categoryName, "", "", categoryImage)
            }

            infoModel.firstCategory = firstCategoryItem

            return infoModel
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    class TempVODHodler(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onSetEmptyView()
        fun onItemClicked(position: Int)
    }
}