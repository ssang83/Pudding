package com.enliple.pudding.adapter.ranking

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.commons.network.vo.API88
import kotlinx.android.synthetic.main.adapter_pudding_ranking_header_item.view.*
import kotlinx.android.synthetic.main.ranking_item.view.*

/**
 * Created by Kim Joonsung on 2018-12-26.
 */
class RankingAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> {
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val mDataList: MutableList<API88.DataBean> = ArrayList()
    private var mListener: AdapterListener? = null
    private var mCategoryItems: List<API81.CategoryItem>
    private var mSyncTime = ""

    constructor(categoryList: List<API81.CategoryItem>) : super() {
        mCategoryItems = categoryList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_HEADER -> RankingHeaderHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_pudding_ranking_header_item, parent, false))
                else -> RankingHolder(LayoutInflater.from(parent.context).inflate(R.layout.ranking_item, parent, false))
            }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        Logger.d("onBindViewHolder: $position")
        when (holder.itemViewType) {
            TYPE_HEADER -> onBindHeader(holder as RankingHeaderHolder)
            else -> onBindItem(holder as RankingHolder, position - 1)
        }
    }

    override fun getItemCount(): Int = mDataList.size + 1 // header 하나 포함

    fun setItems(items: List<API88.DataBean>) {
        Logger.e("setItems")

        mDataList.clear()

        if (items != null) {
            mDataList.addAll(items)
        }

        notifyDataSetChanged()
    }

    fun setServerSyncTime(time: String) {
        mSyncTime = time
    }

    fun setListener(listener: AdapterListener) {
        mListener = listener
    }

    fun getItem(position: Int): API88.DataBean? = if (position in 0 until mDataList.size) mDataList[position] else null

    private fun onBindHeader(holder: RankingHeaderHolder) {
        holder.itemView.title.text = "TODAY CREW RANKING"
        holder.itemView.date.text = mSyncTime
    }

    private fun onBindItem(holder: RankingHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            ImageLoad.setImage(holder.itemView.context,
                    holder.itemView.image,
                    item.userImage,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            holder.itemView.ranking.text = item.rank
            if (position < 3) {
                holder.itemView.ranking.setTextColor(Color.parseColor("#9f56f2"))
            } else {
                holder.itemView.ranking.setTextColor(Color.parseColor("#7e7e7e"))
            }

            holder.itemView.name.text = item.nickName
            holder.itemView.follower.text = StringUtils.getSnsStyleCountZeroBase(Integer.valueOf(item.followCnt.toInt()))
            holder.itemView.like.text = item.favorCnt.toString()

            if (item.ca_id.isEmpty()) {
                holder.itemView.textViewCategory.visibility = View.INVISIBLE
            } else {
                holder.itemView.textViewCategory.visibility = View.VISIBLE
                setCategoryLabel(item.ca_id, holder)
            }

            if (item.rankCal == "new") {
                holder.itemView.fixed.visibility = View.GONE
                holder.itemView.change_layer.visibility = View.VISIBLE
                holder.itemView.ranking_change.visibility = View.GONE
                holder.itemView.fixed.setBackgroundResource(R.drawable.rank_fixed)
                holder.itemView.ranking_change_str.setTextColor(Color.parseColor("#9f56f2"))
                holder.itemView.ranking_change_str.text = "NEW"
            } else {
                var rank = item?.rankCal?.toIntOrNull()
                if (rank != null) {
                    if (rank == 0) {
                        holder.itemView.fixed.visibility = View.VISIBLE
                        holder.itemView.change_layer.visibility = View.GONE
                        holder.itemView.ranking_change.visibility = View.GONE
                    } else if (rank > 0) {
                        holder.itemView.fixed.visibility = View.GONE
                        holder.itemView.change_layer.visibility = View.VISIBLE
                        holder.itemView.ranking_change.visibility = View.VISIBLE
                        holder.itemView.ranking_change.setBackgroundResource(R.drawable.rank_up_arrow)
                        holder.itemView.ranking_change_str.setTextColor(Color.parseColor("#8192a5"))
                        holder.itemView.ranking_change_str.text = item.rankCal
                    } else {
                        holder.itemView.fixed.visibility = View.GONE
                        holder.itemView.change_layer.visibility = View.VISIBLE
                        holder.itemView.ranking_change.visibility = View.VISIBLE
                        holder.itemView.ranking_change.setBackgroundResource(R.drawable.rank_down_arrow)
                        holder.itemView.ranking_change_str.setTextColor(Color.parseColor("#8192a5"))
                        if (item.rankCal.contains("-")) {
                            var editedStr = item.rankCal.replace("-".toRegex(), "")
                            holder.itemView.ranking_change_str.text = editedStr
                        } else {
                            holder.itemView.ranking_change_str.text = item.rankCal
                        }
                    }
                }
            }

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    private fun setCategoryLabel(categoryId: String, holder: RankingHolder) {
        if (mCategoryItems.isEmpty() || categoryId.isEmpty()) {
            return
        }

        for (i in 0 until mCategoryItems.size) {
            if (mCategoryItems[i].categoryId == categoryId) {
                holder.itemView.textViewCategory.text = mCategoryItems[i].categoryName
                holder.itemView.textViewCategory.setTextColor(Color.parseColor(mCategoryItems[i].categoryHex))

                (holder.itemView.textViewCategory.background as GradientDrawable).apply {
                    setStroke(Utils.ConvertDpToPx(holder.itemView.context, 1), Color.parseColor(mCategoryItems[i].categoryHex))
                }
            }
        }
    }

    class RankingHeaderHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    class RankingHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    interface AdapterListener {
        fun onItemClicked(item: API88.DataBean)
    }
}