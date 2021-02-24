package com.enliple.pudding.adapter.my

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.network.vo.API107
import kotlinx.android.synthetic.main.adapter_restriction_item.view.*

/**
 * Created by Kim Joonsung on 2018-10-29.
 */
class RestrictionAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<RestrictionAdapter.RestrictionHolder>() {

    private val items: MutableList<API107.SancItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestrictionHolder {
        return RestrictionHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_restriction_item, parent, false))
    }

    override fun onBindViewHolder(holder: RestrictionHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            holder.itemView.textViewSubject.text = item.sa_content
            holder.itemView.textViewDate.text = item.sa_reg_date
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(list: List<API107.SancItem>) {
        this.items.clear()
        if (list != null) {
            this.items.addAll(list)
        }

        notifyDataSetChanged()
    }

    private fun getItem(position: Int): API107.SancItem? = if (position in 0..items.size) items[position] else null

    class RestrictionHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}