package com.enliple.pudding.adapter.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enliple.pudding.R
import com.enliple.pudding.commons.data.KeywordItem
import com.enliple.pudding.commons.db.KeywordDBManager
import com.enliple.pudding.commons.events.OnSingleClickListener
import kotlinx.android.synthetic.main.adapter_keyword_latest_item.view.*

/**
 * Created by Kim Joonsung on 2019-05-08.
 */
class KeywordLatestAdapter : RecyclerView.Adapter<KeywordLatestAdapter.KeywordHolder>() {

    private val mItems:MutableList<KeywordItem> = mutableListOf()
    private var mListener:Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            KeywordHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_keyword_latest_item, parent, false))

    override fun onBindViewHolder(holder: KeywordHolder, position: Int) {
        mItems[position].let { item ->
            holder.itemView.keyword.text = item.keyword
            holder.itemView.date.text = item.regDate

            holder.itemView.delete.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    val document = KeywordDBManager.getInstance(holder.itemView.context).getDoc(item.id)
                    KeywordDBManager.getInstance(holder.itemView.context).delete(document)
                    mItems.removeAt(position)

                    if(mItems.size == 0) {
                        mListener?.onSetEmptyView()
                    }

                    notifyDataSetChanged()
                }
            })

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onKeywordClicked(item.keyword)
                }
            })
        }
    }

    override fun getItemCount() = mItems.size

    fun setItems(items:List<KeywordItem>) {
        this.mItems.clear()
        this.mItems.addAll(items)

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    class KeywordHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onKeywordClicked(keyword:String)
        fun onSetEmptyView()
    }
}