package com.enliple.pudding.adapter.home

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.activity.BroadcastReportActivity
import kotlinx.android.synthetic.main.adapter_image_file_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-08.
 */
class ImageFileAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val items: ArrayList<BroadcastReportActivity.FileItem> = ArrayList()
    private var mListener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return ImageFileItemHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_image_file_item, parent, false))
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.itemView.textViewFileName.text = item.imageName

            holder.itemView.buttonDel.setOnClickListener({
                items.removeAt(position)
                notifyDataSetChanged()

                mListener?.onFileDelete(items.size)
            })
        }
    }

    override fun getItemCount(): Int = items.size

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun getItem() = items

    fun getItem(position: Int): BroadcastReportActivity.FileItem? = if (position in 0 until items.size) items[position] else null

    fun addItem(file: BroadcastReportActivity.FileItem) {
        if (file != null) {
            this.items.add(file)
        }

        notifyDataSetChanged()
    }

    fun clearItem() {
        items.clear()
        notifyDataSetChanged()
    }

    class ImageFileItemHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onFileDelete(count: Int)
    }
}