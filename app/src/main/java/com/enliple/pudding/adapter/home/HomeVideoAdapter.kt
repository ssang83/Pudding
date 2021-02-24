package com.enliple.pudding.adapter.home

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.widget.CategoryView
import kotlinx.android.synthetic.main.adapter_home_category_item.view.*
import kotlinx.android.synthetic.main.adapter_home_hot_item.view.*
import kotlinx.android.synthetic.main.common_landscape_item.view.*

/**
 * Created by Kim Joonsung on 2019-02-20.
 */
class HomeVideoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {
    companion object {
        private const val TYPE_HOT = 1
        private const val TYPE_CATEGORY = 2
        private const val TYPE_ITEM = 3
    }

    val items: MutableList<API114.VideoItem> = mutableListOf()
    val hotItems: MutableList<API114.HotDataItem> = mutableListOf()
    private val categoryItems: MutableList<API81.CategoryItem> = mutableListOf()
    private var mListener: Listener? = null
    private lateinit var mContext: Context
    private var totalCount = 0
    private var isCategoryClick = false

    private lateinit var mHotAdapter: HotShoppingAdapter
    private var mHotListener: HotShoppingAdapter.Listener? = null
    private var mCategoryListener: CategoryAdapter.Listener? = null

    private var categoryStr = ""
    private var orderStatus = "1"

    constructor(context: Context) : super() {
        if (context != null) {
            mContext = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_HOT -> PuddingHomeHotHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_hot_item, parent, false))
                TYPE_CATEGORY -> PuddingHomeCategoryHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_category_item, parent, false))
                else -> PuddingHomeItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_landscape_item, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Logger.d("onBindViewHolder:$ $position")

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

    fun setHotListener(listener: HotShoppingAdapter.Listener) {
        this.mHotListener = listener
    }

    fun setCategoryListener(listener: CategoryAdapter.Listener) {
        this.mCategoryListener = listener
    }

    fun setItems(list: List<API114.VideoItem>, isOrder: Boolean) {
        this.items.clear()
        this.items.addAll(list)

        if (!isOrder) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeChanged(2, items.size)
        }
    }

    fun getHotItemSize() : Int {
        return hotItems.size
    }

    fun getItemSize() : Int {
        return items.size
    }

    fun addItems(list: List<API114.VideoItem>) {
        if (list.isNotEmpty()) {
            var prevSize = items.size
            items.addAll(list)
            notifyItemInserted(prevSize + 2) //  HOT VIDEO, CATEGORY 2개를 더해야 한다.
        }
    }

    fun setHotItems(list: List<API114.HotDataItem>) {
        this.hotItems.clear()
        if (list.isNotEmpty()) {
            this.hotItems.addAll(list)
        }
    }

    fun setCategoryItems(list: List<API81.CategoryItem>, str: String) {
        this.categoryItems.clear()
        this.categoryItems.addAll(list)

        // 상세 카테고리가 있다면. 첫번째는 all 카테고리가 추가 되어야 한다.
        this.categoryItems.add(0, API81.CategoryItem().apply { categoryId = "all" })

        categoryStr = str
    }

    fun setTotalItemCount(count: Int) {
        this.totalCount = count
    }

    fun setCategoryClick(isClick: Boolean) {
        this.isCategoryClick = isClick
    }

    private fun onBindItem(holder: PuddingHomeItemHolder, position: Int) {
        Logger.e("onBindItem !!!")
        val realPosition = position - 2

        items[realPosition].let { item ->
            holder.itemView.fullLayer.setBackgroundColor(Color.parseColor("#ffffff"))

            ImageLoad.setImage(mContext, holder.itemView.thumbnail, item.largeThumbnailUrl, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

            when (item.videoType) {
                "LIVE" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_onair_label)
                "LASTLIVE" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_pass_live_label)
                else -> holder.itemView.label.setBackgroundResource(R.drawable.trip_video_label)
            }

/*            // 기능제외
            if ( "Y" == item.isFavorite ) {
                holder.itemView.iconLike.setBackgroundResource(R.drawable.ic_like_on)
            } else {
                holder.itemView.iconLike.setBackgroundResource(R.drawable.ic_like)
            }*/

            var price = PriceFormatter.getInstance()?.getFormattedValue(item.min_price)
            price = if (item?.relationPrd?.data?.size!! < 1) {
                "${price}원"
            } else {
                "${price}원~"
            }

            holder.itemView.price.text = price
            holder.itemView.title.text = item?.title
            holder.itemView.product.text = item.min_price_product
            holder.itemView.viewCount.text = StringUtils.getSnsStyleCountZeroBase(item.viewerCount.toInt())
            holder.itemView.likeCount.text = StringUtils.getSnsStyleCountZeroBase(item.favoriteCount.toInt())
            holder.itemView.hashTag.text = StringUtils.convertHashTagText(item?.strTag)

            ImageLoad.setImage(mContext, holder.itemView.profile, item?.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onItemClicked(item, realPosition)
                }
            })
        }
    }

    private fun onBindHotItem(holder: PuddingHomeHotHolder) {
        holder.itemView.recyclerViewHotVideo.setHasFixedSize(true)
        holder.itemView.recyclerViewHotVideo.layoutManager = WrappedLinearLayoutManager(mContext).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        mHotAdapter = HotShoppingAdapter(mContext, true).apply {
            setListener(mHotListener!!)
        }

        holder.itemView.recyclerViewHotVideo.adapter = mHotAdapter

        mHotAdapter.setItems(hotItems)
    }

    private fun onBindCategoryItem(holder: PuddingHomeCategoryHolder) {
        if (!isCategoryClick) {
            holder.itemView.categoryView.setListener(CategoryView.CategorySelectListener { categoryId ->
                Logger.e("selected categoryId :: " + categoryId)
                if (mListener != null) {
                    mListener?.onCategoryClick(categoryId)
                }
            })
            Logger.e("categoryStr bind :: " + categoryStr)
            holder.itemView.categoryView.setItems(categoryStr, null)
        }

        holder.itemView.textViewRecentOrder.setOnClickListener {
            orderStatus = ""
            holder.itemView.textViewRecentOrder.isSelected = true
            holder.itemView.textViewRecentOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_bold.otf")
            holder.itemView.textViewPopularityOrder.isSelected = false
            holder.itemView.textViewPopularityOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_regular.otf")

            mListener?.onRecentOrderClicked(orderStatus)
        }

        holder.itemView.textViewPopularityOrder.setOnClickListener {
            orderStatus = "1"
            holder.itemView.textViewRecentOrder.isSelected = false
            holder.itemView.textViewRecentOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_regular.otf")
            holder.itemView.textViewPopularityOrder.isSelected = true
            holder.itemView.textViewPopularityOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_bold.otf")

            mListener?.onPopularityOrderClicked(orderStatus)
        }

        holder.itemView.textViewTotalCount.text = String.format(mContext.getString(R.string.msg_search_result_count), PriceFormatter.getInstance()?.getFormattedValue(totalCount))

        setOrderStatus(holder)
    }

    private fun setOrderStatus(holder: PuddingHomeCategoryHolder) {
        if (orderStatus == "") {
            holder.itemView.textViewRecentOrder.isSelected = true
            holder.itemView.textViewRecentOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_bold.otf")
            holder.itemView.textViewPopularityOrder.isSelected = false
            holder.itemView.textViewPopularityOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_regular.otf")
        } else {
            holder.itemView.textViewRecentOrder.isSelected = false
            holder.itemView.textViewRecentOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_regular.otf")
            holder.itemView.textViewPopularityOrder.isSelected = true
            holder.itemView.textViewPopularityOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_bold.otf")
        }
    }

//    private fun setLikeStatus(isLike: String, holder: PuddingHomeItemHolder) {
//        if ("N" == isLike) {
//            holder.itemView.buttonLike.isSelected = false
//            holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#8192a5"))
//        } else {
//            holder.itemView.buttonLike.isSelected = true
//            holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#ff6c6c"))
//        }
//    }
//
//    private fun setCategoryLabel(categoryId: String, holder: PuddingHomeItemHolder) {
//        if (categoryItems?.isEmpty()) {
//            return
//        }
//
//        if (categoryId.isEmpty()) {
//            holder.itemView.textViewCategory.text = "전체"
//            holder.itemView.textViewCategory.setTextColor(Color.parseColor("#192028"))
//            holder.itemView.textViewCategory.setBackgroundResource(R.drawable.bg_category_all)
//            return
//        }
//
//        for (i in 0 until categoryItems.size) {
//            if (categoryItems[i].categoryId == categoryId) {
//                holder.itemView.textViewCategory.text = categoryItems[i].categoryName
//                holder.itemView.textViewCategory.setTextColor(Color.parseColor(categoryItems[i].categoryHex))
//
//                (holder.itemView.textViewCategory.background as GradientDrawable).apply {
//                    setStroke(Utils.ConvertDpToPx(mContext, 1), Color.parseColor(categoryItems[i].categoryHex))
//                }
//
//                return
//            } else {
//                holder.itemView.textViewCategory.text = "전체"
//                holder.itemView.textViewCategory.setTextColor(Color.parseColor("#192028"))
//                holder.itemView.textViewCategory.setBackgroundResource(R.drawable.bg_category_all)
//            }
//        }
//    }

    fun changeZzim(idx: String) {
        if ( items != null ) {
            for ( i in items.indices ) {
                var bItem = items[i]
                var relationPrd = bItem.relationPrd
                var relationPrdArr = relationPrd.data
                for ( j in relationPrdArr.indices ) {
                    var item = relationPrdArr[j]
                    if ( idx == item.idx ) {
                        Logger.e("items same idx is :: " + idx + " i is " + i + " , j is " + j)
                        var isWish = "N"
                        var wishCnt = 0
                        Logger.e("items before wish  :: " + item.is_wish)
                        Logger.e("items before wish_cnt  :: " + item.wish_cnt)
                        if ( item.is_wish == "N" ) {
                            isWish = "Y"
                            wishCnt = Integer.valueOf(item.wish_cnt) + 1
                        } else {
                            isWish = "N"
                            wishCnt = Integer.valueOf(item.wish_cnt) - 1
                        }
                        item.is_wish = isWish
                        item.wish_cnt = "$wishCnt"
                        Logger.e("items after wish  :: " + item.is_wish)
                        Logger.e("items after wish_cnt  :: " + item.wish_cnt)
                        relationPrdArr.set(j, item)
                    }
                }
                relationPrd.data = relationPrdArr
                bItem.relationPrd = relationPrd
                items.set(i, bItem)
            }
        }

        if ( hotItems != null ) {
            for ( i in hotItems.indices ) {
                var bItem = hotItems[i]
                var relationPrd = bItem.relationPrd
                var relationPrdArr = relationPrd.data
                for ( j in relationPrdArr.indices ) {
                    var item = relationPrdArr[j]
                    if ( idx == item.idx ) {
                        Logger.e("hotItems same idx is :: " + idx + " i is " + i + " , j is " + j)
                        var isWish = "N"
                        var wishCnt = 0
                        Logger.e("hotItems before wish  :: " + item.is_wish)
                        Logger.e("hotItems before wish_cnt  :: " + item.wish_cnt)
                        if ( item.is_wish == "N" ) {
                            isWish = "Y"
                            wishCnt = Integer.valueOf(item.wish_cnt) + 1
                        } else {
                            isWish = "N"
                            wishCnt = Integer.valueOf(item.wish_cnt) - 1
                        }
                        item.is_wish = isWish
                        item.wish_cnt = "$wishCnt"
                        Logger.e("hotItems after wish  :: " + item.is_wish)
                        Logger.e("hotItems after wish_cnt  :: " + item.wish_cnt)
                        relationPrdArr.set(j, item)
                    }
                }
                relationPrd.data = relationPrdArr
                bItem.relationPrd = relationPrd
                hotItems.set(i, bItem)
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
        fun onCategoryClick(categoryId: String)
    }
}