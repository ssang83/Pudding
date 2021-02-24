package com.enliple.pudding.adapter.search

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.R
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.network.vo.API144
import kotlinx.android.synthetic.main.adapter_keyword_hot_item.view.*

/**
 * Created by Kim Joonsung on 2019-05-08.
 */
class KeywordHotAdapter : RecyclerView.Adapter<KeywordHotAdapter.KeywordHolder>() {

    private val mItems:MutableList<API144.KeywordItem> = mutableListOf()
    private var mListener:Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            KeywordHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_keyword_hot_item, parent, false))

    override fun onBindViewHolder(holder: KeywordHolder, position: Int) {
        mItems[position].let { item ->
            if(position == 0) {
                holder.itemView.rank.setTextColor(Color.parseColor("#9f56f2"))
            } else if(position == 1) {
                holder.itemView.rank.setTextColor(Color.parseColor("#9f56f2"))
            } else if(position == 2) {
                holder.itemView.rank.setTextColor(Color.parseColor("#9f56f2"))
            } else {
                holder.itemView.rank.setTextColor(Color.parseColor("#192028"))
            }

            holder.itemView.rank.text = "${item.rank}."
            holder.itemView.keyword.text = item.keyword

            if(item.rankCal == "new") {
                holder.itemView.fixed.visibility = View.GONE
                holder.itemView.change_layer.visibility = View.VISIBLE
                holder.itemView.ranking_change.visibility = View.GONE
                holder.itemView.fixed.setBackgroundResource(R.drawable.rank_fixed)
                holder.itemView.ranking_change_str.setTextColor(Color.parseColor("#9f56f2"))
                holder.itemView.ranking_change_str.text = "NEW"
            } else {
                val rank = item.rankCal.toIntOrNull()
                if(rank != null) {
                    if(rank == 0) {
                        holder.itemView.fixed.visibility = View.VISIBLE
                        holder.itemView.change_layer.visibility = View.GONE
                        holder.itemView.ranking_change.visibility = View.GONE
                    } else if(rank > 0) {
                        holder.itemView.fixed.visibility = View.GONE
                        holder.itemView.change_layer.visibility = View.VISIBLE
                        holder.itemView.ranking_change.visibility = View.VISIBLE
                        holder.itemView.ranking_change.setBackgroundResource(R.drawable.rank_up_arrow_copy_3)
                        holder.itemView.ranking_change_str.setTextColor(Color.parseColor("#8192a5"))
                        holder.itemView.ranking_change_str.text = item.rankCal
                    } else {
                        holder.itemView.fixed.visibility = View.GONE
                        holder.itemView.change_layer.visibility = View.VISIBLE
                        holder.itemView.ranking_change.visibility = View.VISIBLE
                        holder.itemView.ranking_change.setBackgroundResource(R.drawable.rank_down_arrow_copy_2)
                        holder.itemView.ranking_change_str.setTextColor(Color.parseColor("#8192a5"))
                        if(item.rankCal.contains("-")) {
                            val str = item.rankCal.replace("-", "")
                            holder.itemView.ranking_change_str.text = str
                        } else {
                            holder.itemView.ranking_change_str.text = item.rankCal
                        }
                    }
                }
            }

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onKeywordClick(item)
                }
            })
        }
    }

    override fun getItemCount() = mItems.size

    fun setItems(items:List<API144.KeywordItem>) {
        this.mItems.clear()
        this.mItems.addAll(items)

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    class KeywordHolder(itemView:View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onKeywordClick(item:API144.KeywordItem)
    }
}