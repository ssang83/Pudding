package com.enliple.pudding.adapter.home

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.CategoryModel
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.network.vo.API113
import com.enliple.pudding.commons.network.vo.API81
import kotlinx.android.synthetic.main.adapter_follow_recommend_user_header.view.*
import kotlinx.android.synthetic.main.adapter_search_empty_item.view.*

/**
 * Created by Kim Joonsung on 2019-01-16.
 */
class RecommendUserAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<RecyclerView.ViewHolder> {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_USER = 1
    }

    private val items:MutableList<API113.UserItem> = mutableListOf()
    private var mListener:Listener? = null
    private lateinit var mCategoryItem: List<API81.CategoryItem>
    private var mContext:Context? = null
    private var mTopVisibility = true
    constructor(context: Context, topVisibility: Boolean) : super() {
        if(context != null) {
            mContext = context
            mTopVisibility = topVisibility
            mCategoryItem = CategoryModel.getCategoryList(context, "all")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when(viewType) {
                TYPE_HEADER -> EmptyHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_follow_recommend_user_header, parent, false))
                else -> RecommendUserHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_recommend_user_item, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {
            TYPE_HEADER -> {
                if ( mTopVisibility )
                    holder.itemView.imageLayer.visibility = View.VISIBLE
                else
                    holder.itemView.imageLayer.visibility = View.GONE
            }
            else -> onBindUser(holder as RecommendUserHolder, position)
        }
    }

    override fun getItemViewType(position: Int) =
            if(position == 0) {
                TYPE_HEADER
            } else {
                TYPE_USER
            }

    override fun getItemCount(): Int = items.size + 1

    fun setItems(list:List<API113.UserItem>) {
        this.items.clear()

        if(list != null) this.items.addAll(list) else {}

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    private fun onBindUser(holder: RecommendUserHolder, position:Int) {
        val realPos = position - 1
        items[realPos].let { item ->
            if(TextUtils.isEmpty(item.mb_user_img)) {
                holder.itemView.imageViewProfile.setBackgroundResource(R.drawable.profile_default_img)
            } else {
                ImageLoad.setImage(holder.itemView.context,
                        holder.itemView.imageViewProfile,
                        item.mb_user_img,
                        null,
                        ImageLoad.SCALE_CIRCLE_CROP,
                        DiskCacheStrategy.ALL)
            }

            setCategoryLabel(item.ca_id, holder)

            holder.itemView.textViewName.text = item.mb_nick
            holder.itemView.textViewFollowerCount.text = StringUtils.getSnsStyleCountZeroBase(item.follow_cnt.toInt())

            holder.itemView.setOnClickListener {
                mListener?.onUserClicked(item)
            }
        }
    }

    private fun setCategoryLabel(categoryId: String, holder: RecommendUserHolder) {
        if(mCategoryItem.isEmpty()) {
            return
        }

        if(categoryId.isEmpty()) {
            holder.itemView.textViewCategory.text = "전체"
            holder.itemView.textViewCategory.setTextColor(Color.parseColor("#192028"))
            holder.itemView.textViewCategory.setBackgroundResource(R.drawable.bg_category_all)

            return
        }

        for (i in 0 until mCategoryItem.size) {
            if (mCategoryItem.get(i).categoryId == categoryId) {
                holder.itemView.textViewCategory.text = mCategoryItem.get(i).categoryName
                holder.itemView.textViewCategory.setTextColor(Color.parseColor(mCategoryItem.get(i).categoryHex))

                (holder.itemView.textViewCategory.background as GradientDrawable).apply {
                    setStroke(Utils.ConvertDpToPx(mContext, 1), Color.parseColor(mCategoryItem[i].categoryHex))
                }

                return
            } else {
                holder.itemView.textViewCategory.text = "전체"
                holder.itemView.textViewCategory.setTextColor(Color.parseColor("#192028"))
                holder.itemView.textViewCategory.setBackgroundResource(R.drawable.bg_category_all)
            }
        }
    }

    inner class EmptyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class RecommendUserHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onUserClicked(item:API113.UserItem)
    }
}