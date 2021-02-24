package com.enliple.pudding.adapter.my

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.network.vo.API58
import kotlinx.android.synthetic.main.adapter_block_list_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-29.
 */
class BlockAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<BlockAdapter.BlockHolder> {

    private val items: MutableList<API58.BlockItem> = ArrayList()
    private var mListener: Listener? = null
    private var screenHeight: Int = 0
    private var statusbarHeight: Int = 0

    private var listener: OnPopupPositionListener? = null
    private var context: Context? = null

    interface OnPopupPositionListener {

    }

    fun setPopupPositionListener(listener: OnPopupPositionListener) {
        this.listener = listener
    }

    constructor(screenHeight: Int, statusbarHeight: Int) {
        this.screenHeight = screenHeight
        this.statusbarHeight = statusbarHeight
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockHolder {
        this.context = parent.context
        return BlockHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_block_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: BlockHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.itemView.textViewNickName.text = item.block_user

            if (item.bl_status_YN == "Y") {
                holder.itemView.textViewBolckType.text = "메시지 차단"
            }

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewThumbnail,
                    item!!.mb_user_img,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            holder.itemView.buttonMore.setOnClickListener {
                var viewHeight = holder.itemView.buttonMore.height // imgMore 이미지의 height
                val originalPos = IntArray(2)
                holder.itemView.buttonMore.getLocationInWindow(originalPos)
                var yPosition: Int = originalPos[1] // more 이미지의 y좌표 ( dialog의 시작점 기준 )
                var popupHeight = Utils.ConvertDpToPx(context, 146) // more click 시 나타나는 팝업의 높이
                var topMargin: Int
                // 팝업의 하단 y 좌표가 전체 화면의 높이값 보다 클 경우 팝업이 화면 아래로 가려지는 부분이 생기므로
                // 이때는 팝업을 more 버튼아래가 아니라 위에 그려줘야 함
                if ((yPosition + viewHeight + popupHeight) <= screenHeight) {
                    topMargin = yPosition + viewHeight - statusbarHeight
                } else {
                    topMargin = yPosition - popupHeight - statusbarHeight
                }
                if (mListener != null) {
                    mListener!!.getPopupStartY(topMargin, item)

                }
            }

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setItems(list: List<API58.BlockItem>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    private fun getItem(position: Int): API58.BlockItem? = if (position in 0..items.size) items[position] else null

    class BlockHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(data: API58.BlockItem)
        fun getPopupStartY(y: Int, item: API58.BlockItem)
    }
}