package com.enliple.pudding.adapter.search

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
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.network.vo.API31
import com.enliple.pudding.commons.network.vo.API81
import kotlinx.android.synthetic.main.adapter_search_empty_item.view.*
import kotlinx.android.synthetic.main.adapter_search_header_item.view.*

/**
 * Created by Kim Joonsung on 2019-01-14.
 */
class SearchUserEmptyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val items: MutableList<API31.UserItem> = mutableListOf()
    private var mListener: Listener? = null
    private lateinit var mCategoryItem: List<API81.CategoryItem>
    private var mItemWidth = 0
    private var mImageWidth = 0
    private var mContext:Context? = null
    private var mKeyword = ""

    constructor(context: Context) : super() {
        if(context != null) {
            var screenWidth = AppPreferences.getScreenWidth(context)
            mItemWidth = (screenWidth - Utils.ConvertDpToPx(context, 36)) / 3 // recycler view 좌우 padding 과 내부 padding 6 값 제외한 item의 width
            mImageWidth = mItemWidth - Utils.ConvertDpToPx(context,40)  // item의 패딩 20을 제외하고 계산한 값

            mCategoryItem = CategoryModel.getCategoryList(context, "all")
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when(viewType) {
                TYPE_HEADER -> SearchUserHeader(LayoutInflater.from(parent.context).inflate(R.layout.adapter_search_header_item, parent, false))
                else -> SearchUserEmptyHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_search_empty_item, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            when(holder.itemViewType) {
                TYPE_HEADER -> onBindHeader(holder as SearchUserHeader)
                else -> onBindItem(holder as SearchUserEmptyHolder, position)
            }

    override fun getItemViewType(position: Int) : Int {
        if(position == 0) {
            return TYPE_HEADER
        } else {
            return TYPE_ITEM
        }
    }

    override fun getItemCount(): Int = items.size + 1

    fun setItems(list: List<API31.UserItem>) {
        this.items.clear()

        if (list != null) this.items.addAll(list) else {
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setKeyword(keyword:String) {
        this.mKeyword = keyword
    }

    private fun onBindHeader(holder:SearchUserHeader) {
        holder.itemView.textViewKeyword.text = "'${mKeyword}'"
        holder.itemView.textViewEmptyComment.text = "에 대한 유저가 없습니다."
        holder.itemView.textViewEmpty.text = "내가 좋아할 만한 사람"
    }

    private fun onBindItem(holder:SearchUserEmptyHolder, position:Int) {
        val realPos = position - 1
        items[realPos].let { item ->
            var params = holder.itemView.main_root.layoutParams
            params.width = mItemWidth
            params.height = params.height
            holder.itemView.main_root.layoutParams = params

            params = holder.itemView.imageViewProfile.layoutParams
            params.width = mItemWidth
            params.height = mImageWidth
            holder.itemView.imageViewProfile.layoutParams = params

            if (TextUtils.isEmpty(item.mb_user_img)) {
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

    private fun setCategoryLabel(categoryId: String, holder: SearchUserEmptyHolder) {
        if(mCategoryItem.isEmpty()) return

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

    class SearchUserEmptyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class SearchUserHeader(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onUserClicked(item: API31.UserItem)
    }
}