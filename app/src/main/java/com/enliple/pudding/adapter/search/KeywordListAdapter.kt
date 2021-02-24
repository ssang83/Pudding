package com.enliple.pudding.adapter.search

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API27
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import kotlinx.android.synthetic.main.adapter_keyword_list_item.view.*

/**
 * Created by Kim Joonsung on 2018-09-28.
 */
class KeywordListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<KeywordListAdapter.KeywordListHolder>() {

    private val items: MutableList<API27.KeywordVodItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mVodListener: KeywordVODListAdapter.Listener? = null
    private lateinit var mAdapter: KeywordVODListAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordListHolder {
        return KeywordListHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_keyword_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: KeywordListHolder, position: Int) {
        val item = getItem(position)

        if (item != null) {
            if ( position == items.size - 1 ) {
                holder.itemView.line.visibility = View.GONE
            } else {
                holder.itemView.line.visibility = View.VISIBLE
            }

            holder.itemView.textViewKeyword.text = "#${item.tag}"
            holder.itemView.textViewCount.text = item.vod_cnt.toString()

            holder.itemView.recyclerViewVod.setHasFixedSize(true)
            holder.itemView.recyclerViewVod.layoutManager = WrappedLinearLayoutManager(holder.itemView.context,
                    WrappedLinearLayoutManager.HORIZONTAL, false)
            mAdapter = KeywordVODListAdapter()
            mAdapter.setListener(mVodListener)
            mAdapter.setTagPosition(position)

            holder.itemView.recyclerViewVod.adapter = mAdapter
            mAdapter.setItem(item.vod)

            holder.itemView.layoutForDetail.setOnClickListener {
                mListener?.onDetailItemClicked(item)
            }

            holder.itemView.textViewKeyword.setOnClickListener {
                mListener?.onDetailItemClicked(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun setVodListener(listener: KeywordVODListAdapter.Listener) {
        mVodListener = listener
    }

    fun getItem(position: Int): API27.KeywordVodItem? = if (position in 0 until items.size) items[position] else null

    fun setItem(items: List<API27.KeywordVodItem>) {
        this.items.clear()
        if (items != null) {
            this.items.addAll(items)
        }

        notifyDataSetChanged()
    }

    fun getItems(position: Int) : MutableList<API27.KeywordVodItem.VodItem> = items[position].vod

    fun changeZzim(idx: String) {
        /**
        if ( items != null ) {
            for ( i in items.indices ) {
                var bItem = items[i]
                var relationPrd = bItem.re
                var relationPrdArr = relationPrd.data
                for ( j in relationPrdArr.indices ) {
                    var item = relationPrdArr[j]
                    if ( idx == item.idx ) {
                        Logger.e("mItems same idx is :: " + idx + " i is " + i + " , j is " + j)
                        var isWish = "N"
                        var wishCnt = 0
                        Logger.e("mItems before wish  :: " + item.is_wish)
                        Logger.e("mItems before wish_cnt  :: " + item.wish_cnt)
                        if ( item.is_wish == "N" ) {
                            isWish = "Y"
                            wishCnt = Integer.valueOf(item.wish_cnt) + 1
                        } else {
                            isWish = "N"
                            wishCnt = Integer.valueOf(item.wish_cnt) - 1
                        }
                        item.is_wish = isWish
                        item.wish_cnt = "$wishCnt"
                        Logger.e("mItems after wish  :: " + item.is_wish)
                        Logger.e("mItems after wish_cnt  :: " + item.wish_cnt)
                        relationPrdArr.set(j, item)
                    }
                }
                relationPrd.data = relationPrdArr
                bItem.relationPrd = relationPrd
                mItems.set(i, bItem)
            }
        }
        **/
    }


    class KeywordListHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onDetailItemClicked(data: API27.KeywordVodItem)
    }
}