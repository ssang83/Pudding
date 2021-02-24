package com.enliple.pudding.adapter.home

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.*
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkConst
import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.model.SecondCategoryItem
import com.enliple.pudding.model.ThirdCategoryItem
import com.enliple.pudding.model.ThreeCategoryItem
import com.enliple.pudding.widget.CategoryView
import kotlinx.android.synthetic.main.adapter_home_category_item.view.*
import kotlinx.android.synthetic.main.adapter_home_hot_item.view.*
import kotlinx.android.synthetic.main.adapter_pudding_home_item.view.*
import org.json.JSONObject
import java.util.ArrayList

/**
 * Created by Kim Joonsung on 2019-02-20.
 */
class HomeLiveAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> {
    companion object {
        private const val TYPE_HOT = 1
        private const val TYPE_CATEGORY = 2
        private const val TYPE_ITEM = 3
    }

    private val items: MutableList<API114.VideoItem> = mutableListOf()
    private val hotItems: MutableList<API114.HotDataItem> = mutableListOf()
    private val categoryItems: MutableList<API81.CategoryItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mItemWidth = 0
    private var mImageWidth = 0
    private var mImageHeight = 0
    private lateinit var mContext: Context
    private var totalCount = 0
    private var isEmptyVisible = false
    private var isCategoryClick = false

    private lateinit var mHotAdapter: HotAdapter
    private lateinit var mCategoryAdapter: CategoryAdapter
    private var mHotListener: HotAdapter.Listener? = null
    private var mCategoryListener: CategoryAdapter.Listener? = null
    private var categoryStr: String = ""
    constructor(context: Context) : super() {
        if (context != null) {
            var screenWidth = AppPreferences.getScreenWidth(context)
            mItemWidth = screenWidth / 2        // recycler view 좌우 padding 값제외한 순수 item 의 width
            mImageWidth = mItemWidth - Utils.ConvertDpToPx(context, 8)  // item의 마진 3과 라인을 위한 좌우 패딩 1을 계산한 이미지의 width
            mImageHeight = (199 * mItemWidth) / 162 // image width 의 비율에 맞춘 height 값

            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_HOT -> PuddingHomeHotHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_live_hot_item, parent, false))
                TYPE_CATEGORY -> PuddingHomeCategoryHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_category_item, parent, false))
                else -> PuddingHomeItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_pudding_home_item, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_HOT -> onBindHotItem(holder as PuddingHomeHotHolder)
            TYPE_CATEGORY -> onBindCategoryItem(holder as PuddingHomeCategoryHolder)
            else -> onBindItem(holder as PuddingHomeItemHolder, position)
        }
    }

    override fun getItemViewType(position: Int) =
            when (position) {
                0 -> TYPE_HOT
                1 -> TYPE_CATEGORY
                else -> TYPE_ITEM
            }

    override fun getItemCount(): Int = items.size + 2

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setHotListener(listener: HotAdapter.Listener) {
        this.mHotListener = listener
    }

    fun setCategoryListener(listener: CategoryAdapter.Listener) {
        this.mCategoryListener = listener
    }

    fun setItems(items: List<API114.VideoItem>, isOrder:Boolean) {
        this.items.clear()
        this.items.addAll(items)

        if(!isOrder) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeChanged(2, items.size)
        }
    }

    fun addItems(list:List<API114.VideoItem>) {
        if(list.isNotEmpty()) {
            for(i in 0 until list.size) {
                this.items.add(list[i])
            }

            notifyItemInserted(items.size - list.size)
        }
    }

    fun setHotItems(list: List<API114.HotDataItem>) {
        this.hotItems.clear()
        if (!list.isEmpty()) {
            this.hotItems.addAll(list)
        }
    }

    fun setCategoryItems(list: List<API81.CategoryItem>, str: String) {
        this.categoryItems.clear()
        this.categoryItems.addAll(list)
        categoryStr = str
        // 상세 카테고리가 있다면. 첫번째는 all 카테고리가 추가 되어야 한다.
        this.categoryItems.add(0, API81.CategoryItem().apply { categoryId = "all" })
    }

    fun setTotalItemCount(count: Int) {
        this.totalCount = count
    }

    fun setLiveEmptyVisible(isShow: Boolean) {
        this.isEmptyVisible = isShow
    }

    fun setCategoryClick(isClick: Boolean) {
        this.isCategoryClick = isClick
    }

    private fun onBindItem(holder: PuddingHomeItemHolder, position: Int) {
        val realPosition = position - 2

        var params = holder.itemView.root.layoutParams
        params.width = mItemWidth
        params.height = params.height
        holder.itemView.root.layoutParams = params
        params = holder.itemView.imageViewThumbnail.layoutParams
        params.width = mImageWidth
        params.height = mImageHeight
        holder.itemView.imageViewThumbnail.layoutParams = params

        items[realPosition].let { item ->
            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewThumbnail,
                    item.thumbnailUrl,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            when (item.videoType) {
                "LIVE" -> holder.itemView.imageViewLabel.setBackgroundResource(R.drawable.live_label)
                "VOD" -> holder.itemView.imageViewLabel.setBackgroundResource(R.drawable.video_label)
                "LASTLIVE" -> holder.itemView.imageViewLabel.setBackgroundResource(R.drawable.pass_live_label)
                "LIVE" -> holder.itemView.imageViewLabel.setBackgroundResource(R.drawable.live_label)
                else -> holder.itemView.imageViewLabel.setBackgroundResource(R.drawable.multi_live_label)
            }

            holder.itemView.textViewLikeCount.text = StringUtils.getSnsStyleCountZeroBase(item.favoriteCount.toInt())
            holder.itemView.textViewTitle.text = item.title

            setLikeStatus(item.isFavorite, holder)
            setCategoryLabel(item.categoryCode, holder)

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item, realPosition)
            }
        }
    }

    private fun onBindHotItem(holder: PuddingHomeHotHolder) {
        if (isEmptyVisible) {
            holder.itemView.layoutEmpty.visibility = View.VISIBLE
            holder.itemView.layoutHotItem.visibility = View.GONE
        } else {
            holder.itemView.layoutEmpty.visibility = View.GONE
            holder.itemView.layoutHotItem.visibility = View.VISIBLE

            holder.itemView.recyclerViewHotVideo.setHasFixedSize(true)
            holder.itemView.recyclerViewHotVideo.layoutManager = WrappedLinearLayoutManager(mContext).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }

            mHotAdapter = HotAdapter(mContext, true).apply {
                setListener(mHotListener!!)
            }

            holder.itemView.recyclerViewHotVideo.adapter = mHotAdapter

//            mHotAdapter.setItems(hotItems)
        }
    }

    private fun onBindCategoryItem(holder: PuddingHomeCategoryHolder) {
        holder.itemView.textViewTotalCount.text = String.format(mContext.getString(R.string.msg_search_result_count),
                PriceFormatter.getInstance()?.getFormattedValue(totalCount))
        holder.itemView.categoryView.setListener(CategoryView.CategorySelectListener { categoryId ->
            Logger.e("selected categoryId :: " + categoryId)
        })
        holder.itemView.categoryView.setItems(categoryStr, null)
//        holder.itemView.recyclerViewCategory.setHasFixedSize(true)
//        holder.itemView.recyclerViewCategory.layoutManager = WrappedLinearLayoutManager(mContext).apply {
//            orientation = LinearLayoutManager.VERTICAL
//        }
//
//        if (!isCategoryClick) {
//            mCategoryAdapter = CategoryAdapter(holder.itemView.context).apply {
//                setListener(mCategoryListener!!)
//            }
//
//            holder.itemView.recyclerViewCategory.adapter = mCategoryAdapter
//
//            try {
//                val obj = JSONObject(categoryStr)
//                val array = obj.optJSONArray("data")
//                val itemArray = ArrayList<ThreeCategoryItem>()
//
//                // 1차 카테고리 전체를 add
//                val firstAll = ThreeCategoryItem()
//                firstAll.categoryId = ""
//                firstAll.categoryName = "전체"
//                firstAll.isSelected = true
//                firstAll.categoryImage = NetworkConst.CATEGORY_ALL_IMAGE_API
//                firstAll.categoryImageOn = NetworkConst.CATEGORY_ALL_ON_IMAGE_API
//                firstAll.categoryImageOff = NetworkConst.CATEGORY_ALL_OFF_IMAGE_API
//                firstAll.secondCategory = null
//
//                itemArray.add(firstAll)
//
//                if (array != null && array!!.length() > 0) {
//                    for (i in 0 until array!!.length()) {
//                        val `object` = array!!.optJSONObject(i)
//                        val item = ThreeCategoryItem()
//
//                        val categoryId = `object`.optString("categoryId")
//                        val categoryName = `object`.optString("categoryName")
//                        val categoryImage = `object`.optString("categoryImage")
//                        val categoryImageOn = `object`.optString("categoryImageOn")
//                        val categoryImageOff = `object`.optString("categoryImageOff")
//                        val selected = false
//                        var secondCategoryArray: ArrayList<SecondCategoryItem>? = null
//
//                        val secondArray = `object`.optJSONArray("sub")
//                        if (secondArray != null && secondArray!!.length() > 0) {
//                            secondCategoryArray = ArrayList()
//                            val secondAllItem = SecondCategoryItem()
//                            secondAllItem.categoryId = categoryId
//                            secondAllItem.categoryName = "전체"
//                            secondAllItem.isSelected = true
//                            secondAllItem.thirdCategory = null
//
//                            secondCategoryArray.add(secondAllItem)
//
//                            for (j in 0 until secondArray!!.length()) {
//                                val secondObject = secondArray!!.optJSONObject(j)
//                                val secondItem = SecondCategoryItem()
//                                val s_categoryId = secondObject.optString("categoryId")
//                                val s_categoryName = secondObject.optString("categoryName")
//                                val s_selected = false
//
//                                var thirdCategoryArray: ArrayList<ThirdCategoryItem>? = null
//
//                                val thirdArray = secondObject.optJSONArray("sub")
//                                if (thirdArray != null && thirdArray!!.length() > 0) {
//                                    thirdCategoryArray = ArrayList()
//                                    val thirdAllItem = ThirdCategoryItem()
//                                    thirdAllItem.categoryId = s_categoryId
//                                    thirdAllItem.categoryName = "전체"
//                                    thirdAllItem.isSelected = true
//
//                                    thirdCategoryArray.add(thirdAllItem)
//
//                                    for (k in 0 until thirdArray!!.length()) {
//                                        val thirdObject = thirdArray!!.optJSONObject(k)
//                                        val thirdItem = ThirdCategoryItem()
//                                        val t_categoryId = thirdObject.optString("categoryId")
//                                        val t_categoryName = thirdObject.optString("categoryName")
//                                        val t_selected = false
//
//                                        thirdItem.categoryId = t_categoryId
//                                        thirdItem.categoryName = t_categoryName
//                                        thirdItem.isSelected = t_selected
//
//                                        thirdCategoryArray.add(thirdItem)
//                                    }
//                                }
//                                secondItem.categoryId = s_categoryId
//                                secondItem.categoryName = s_categoryName
//                                secondItem.isSelected = s_selected
//                                secondItem.thirdCategory = thirdCategoryArray
//                                secondCategoryArray.add(secondItem)
//                            }
//                        }
//                        item.categoryId = categoryId
//                        item.categoryName = categoryName
//                        item.isSelected = selected
//                        item.categoryImage = categoryImage
//                        item.categoryImageOn = categoryImageOn
//                        item.categoryImageOff = categoryImageOff
//                        item.secondCategory = secondCategoryArray
//                        itemArray.add(item)
//                    }
//                }
//                mCategoryAdapter.setCategoryItem(itemArray)
////                adapter.setCategoryItem(itemArray)
//
//                Logger.e("itemArray.size :: " + itemArray.size)
//
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
////            mCategoryAdapter.setItems(categoryItems)
//        }

        holder.itemView.textViewRecentOrder.setOnClickListener {
            holder.itemView.textViewRecentOrder.isSelected = true
            holder.itemView.textViewPopularityOrder.isSelected = false

            mListener?.onRecentOrderClicked("0")
        }

        holder.itemView.textViewPopularityOrder.setOnClickListener {
            holder.itemView.textViewRecentOrder.isSelected = false
            holder.itemView.textViewPopularityOrder.isSelected = true

            mListener?.onPopularityOrderClicked("1")
        }

        setOrderStatus(holder)
    }

    private fun setOrderStatus(holder: PuddingHomeCategoryHolder) {
        holder.itemView.textViewRecentOrder.isSelected = true
        holder.itemView.textViewPopularityOrder.isSelected = false
    }

    private fun setLikeStatus(isLike: String, holder: PuddingHomeItemHolder) {
        if ("N" == isLike) {
            holder.itemView.buttonLike.isSelected = false
            holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#8192a5"))
        } else {
            holder.itemView.buttonLike.isSelected = true
            holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#ff6c6c"))
        }
    }

    private fun setCategoryLabel(categoryId: String, holder: PuddingHomeItemHolder) {
        Logger.e("categoryId :: " + categoryId )
        if (categoryItems?.isEmpty()) {
            return
        }

        if (categoryId.isEmpty()) {
            holder.itemView.textViewCategory.text = "전체"
            holder.itemView.textViewCategory.setTextColor(Color.parseColor("#192028"))
            holder.itemView.textViewCategory.setBackgroundResource(R.drawable.bg_category_all)

            return
        }

        for (i in 0 until categoryItems.size) {
            if (categoryItems[i].categoryId == categoryId) {
                holder.itemView.textViewCategory.text = categoryItems[i].categoryName
                holder.itemView.textViewCategory.setTextColor(Color.parseColor(categoryItems[i].categoryHex))

                (holder.itemView.textViewCategory.background as GradientDrawable).apply {
                    setStroke(Utils.ConvertDpToPx(mContext, 1), Color.parseColor(categoryItems[i].categoryHex))
                }

                return
            } else {
                holder.itemView.textViewCategory.text = "전체"
                holder.itemView.textViewCategory.setTextColor(Color.parseColor("#192028"))
                holder.itemView.textViewCategory.setBackgroundResource(R.drawable.bg_category_all)
            }
        }
    }

    inner class PuddingHomeItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class PuddingHomeHotHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class PuddingHomeCategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: API114.VideoItem, position: Int)
        fun onRecentOrderClicked(order: String)
        fun onPopularityOrderClicked(order: String)
    }
}