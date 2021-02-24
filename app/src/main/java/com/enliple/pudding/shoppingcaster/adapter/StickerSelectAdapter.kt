package com.enliple.pudding.shoppingcaster.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import kotlinx.android.synthetic.main.adapter_sticker_select_item.view.*

/**
 * 스티커 리스트 아답터
 * @author hkcha
 * @since 2018.08.29
 */
class StickerSelectAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<StickerSelectAdapter.StickerHolder>() {

    private var mListener: Listener? = null

    private val items: MutableList<Int> = ArrayList<Int>().apply {
        add(R.drawable.sticker_1)
        add(R.drawable.sticker_2)
        add(R.drawable.sticker_3)
        add(R.drawable.sticker_4)
        add(R.drawable.sticker_5)
        add(R.drawable.sticker_6)
        add(R.drawable.sticker_7)
        add(R.drawable.sticker_8)
        add(R.drawable.sticker_9)
        add(R.drawable.sticker_10)
        add(R.drawable.sticker_11)
        add(R.drawable.sticker_12)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerHolder {
        return StickerHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_sticker_select_item, parent, false))
    }

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int): Int = if (position in 0 until items.size) items[position] else -1

    override fun onBindViewHolder(holder: StickerHolder, position: Int) {
        val item = getItem(position)
        holder.itemView?.imageViewSticker?.setImageResource(0)

        if (item != -1) {
            holder.itemView?.imageViewSticker?.setImageResource(item)
            holder.itemView?.setOnClickListener { mListener?.onStickerItemClicked(item) }
        }
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    class StickerHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onStickerItemClicked(stickerResourceId: Int)
    }
}