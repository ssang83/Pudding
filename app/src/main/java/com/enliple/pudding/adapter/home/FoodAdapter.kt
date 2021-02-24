package com.enliple.pudding.adapter.home

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.adapter.TravelBannerAdapter
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.fragment.main.pudding.PuddingHomeTabFragment
import com.enliple.pudding.widget.CategoryView
import kotlinx.android.synthetic.main.category_item.view.*
import kotlinx.android.synthetic.main.common_landscape_item.view.*
import kotlinx.android.synthetic.main.sort_item.view.*
import kotlinx.android.synthetic.main.travel_banner_items.view.*
import java.util.*

/**
 * Created by Kim Joonsung on 2019-04-29.
 */
class FoodAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    companion object {
        private const val TYPE_BANNER = 0
        private const val TYPE_CATEGORY = 1
        private const val TYPE_SORT = 2
        private const val TYPE_ITEM = 3
    }

    val mItems: MutableList<API114.VideoItem> = mutableListOf()
    val mBannerItems: MutableList<API114.EventItem> = mutableListOf()
    var mListener: Listener? = null
    var mBannerAdapter: TravelBannerAdapter? = null

    var categoryStr = ""
    var f_category = ""
    var isCategoryClick = false
    var height = 0
    var pagerHeight = 0
    var imageWidth = 0;
    var imageHeight = 0;
    var bannerWidth = 0
    var isRecent = false
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var viewPager: ViewPager? = null
    private var currentPage: Int = 0
    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Logger.e("msg.what :: " + msg.what)
            if ( msg.what == 0 ) {
                if ( viewPager != null ) {
                    Logger.e("viewpager not null")
                    currentPage = currentPage + 1
                    Logger.e("currentPage :: " + currentPage)
                    viewPager!!.setCurrentItem(currentPage, true)
                } else {
                    Logger.e("viewpager null")
                }
            }
        }
    }
    constructor(context: Context) {
        bannerWidth = AppPreferences.getScreenWidth(context) - Utils.ConvertDpToPx(context, 30)
        pagerHeight = (bannerWidth * 195) / 330
        height = pagerHeight + Utils.ConvertDpToPx(context, 50)

        imageWidth = bannerWidth
        imageHeight = (imageWidth * 185) / 330

        runTimer()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_BANNER -> BannerHolder(LayoutInflater.from(parent.context).inflate(R.layout.travel_banner_items, parent, false))
                TYPE_CATEGORY -> CategoryHolder(LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false))
                TYPE_SORT -> SortHolder(LayoutInflater.from(parent.context).inflate(R.layout.sort_item, parent, false))
                else -> ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_landscape_item, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            when (holder.itemViewType) {
                TYPE_BANNER -> onBindBanner(holder as BannerHolder)
                TYPE_CATEGORY -> onBindCategory(holder as CategoryHolder)
                TYPE_SORT -> onBindSort(holder as SortHolder)
                else -> onBindItem(holder as ItemHolder, position)
            }

    override fun getItemCount(): Int {
        if (mItems.isNotEmpty()) {
            return mItems.size + 3
        } else {
            return 3
        }
    }

    fun setItem(items: List<API114.VideoItem>, eventItems: List<API114.EventItem>) {
        this.mItems.clear()
        this.mItems.addAll(items)

        this.mBannerItems.clear()
        this.mBannerItems.addAll(eventItems)

        notifyDataSetChanged()
    }

    fun setCategory(category: String) {
        Logger.e("category :: $category")
        this.categoryStr = category
    }

    fun addItems(items: List<API114.VideoItem>) {
        if (items.isNotEmpty()) {
            val prevSize = items.size
            mItems.addAll(items)
            notifyItemInserted(prevSize + 3) // Banner, Category 2개를 더해야 한다.
        }
    }

    fun getItems() : List<API114.VideoItem> {
        return mItems
    }

    fun setItem(position : Int , item : API114.VideoItem) {
        if ( mItems != null && mItems.size > 0 )
            mItems.set(position, item)
    }

    fun getItemSize() : Int {
        if ( mItems != null )
            return mItems.size
        else
            return 0
    }

    fun setPagerIndicatorState(holder: BannerHolder, position: Int) {
        try {
            if (holder != null) {
                for (i in mBannerItems.indices) {
                    if (i == position) {
                        holder.itemView.indicator.getChildAt(i).setSelected(true)
                    } else {
                        holder.itemView.indicator.getChildAt(i).setSelected(false)
                    }
                }
            }
        } catch (e: Exception ) {
            e.printStackTrace()
        }
    }

    private fun setPagerIndicator(holder: BannerHolder, pageCount: Int) {
        Logger.e("111111")
        if (holder.itemView.indicator.getChildCount() > 0) {
            holder.itemView.indicator.removeAllViews()
        }
        Logger.e("222222");
        for (i in 0 until pageCount) {
            Logger.e("333333");
            val dot = RadioButton(holder.itemView.context)
            dot.setButtonDrawable(R.drawable.dot_selector)
            dot.id = i
            holder.itemView.indicator.addView(dot)


            val param = dot.layoutParams as RadioGroup.LayoutParams
//                if (i != 0)
//                    param.leftMargin = Utils.ConvertDpToPx(holder.itemView.context, 2)
            param.width = Utils.ConvertDpToPx(holder.itemView.context, 12)
            param.height = Utils.ConvertDpToPx(holder.itemView.context, 12)
            dot.layoutParams = param
        }
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    override fun getItemViewType(position: Int) =
            when (position) {
                0 -> TYPE_BANNER
                1 -> TYPE_CATEGORY
                2 -> TYPE_SORT
                else -> TYPE_ITEM
            }


    private fun onBindBanner(holder: BannerHolder) {
//        var param = holder.itemView.root.layoutParams
//        param.width = AppPreferences.getScreenWidth(holder.itemView.context)
//        param.height = height
//        holder.itemView.root.layoutParams = param
        if(mBannerItems.size > 0) {
            var pr = holder.itemView.bannerPager.layoutParams
            pr.width = AppPreferences.getScreenWidth(holder.itemView.context)
            pr.height = pagerHeight
            holder.itemView.bannerPager.layoutParams = pr

            mBannerAdapter = TravelBannerAdapter(holder.itemView.context, object : TravelBannerAdapter.Listener {
                override fun onBannerClicked(banner: API114.EventItem?) {
                    mListener?.onBannerClick(banner!!)
                }
            })

            setPagerIndicator(holder, mBannerItems!!.size)

            holder.itemView.bannerPager.adapter = mBannerAdapter
            viewPager = holder.itemView.bannerPager
            holder.itemView.bannerPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    Logger.e("onPageScrolled")
//                mBannerAdapter!!.setPagerIndicatorState(position)
                    val realPos = position.rem(mBannerItems.size)
                    setPagerIndicatorState(holder, realPos)
                    currentPage = position
                }

                override fun onPageSelected(position: Int) {
//                mBannerAdapter!!.setPagerIndicatorState(position)
                    Logger.e("onPageSelected")
                    currentPage = position
                }

                override fun onPageScrollStateChanged(state: Int) {
                    Logger.e("onPageScrollStateChanged")
                }
            })
            mBannerAdapter!!.setItems(mBannerItems)
        }
    }

    private fun onBindCategory(holder: CategoryHolder) {
        if (!isCategoryClick) {
//            holder.itemView.categoryView.isLastLineVisible(false)
            Logger.e("f_category :: "  + f_category)
            holder.itemView.categoryView.setItems(categoryStr, f_category)
        }

        holder.itemView.categoryView.setListener(object : CategoryView.CategorySelectListener {
            override fun onCategorySelected(categoryId: String?) {
                mListener?.categoryClicked(categoryId!!)
            }
        })
    }

    private fun onBindSort(holder: SortHolder) {
        holder.itemView.sortTitle.text = "Hot Food"
        holder.itemView.sortFav.setOnClickListener {
            isRecent = false
            holder.itemView.sortFav.typeface = Typeface.createFromAsset(holder.itemView.context.assets, "fonts/notosanskr_bold.otf")
            holder.itemView.sortRecent.typeface = Typeface.createFromAsset(holder.itemView.context.assets, "fonts/notosanskr_medium.otf")
            holder.itemView.sortFav.setTextColor(Color.parseColor("#9f56f2"))
            holder.itemView.sortRecent.setTextColor(Color.parseColor("#bcc6d2"))
            mListener?.onSortClicked("1")
        }

        holder.itemView.sortRecent.setOnClickListener {
            isRecent = true
            holder.itemView.sortFav.typeface = Typeface.createFromAsset(holder.itemView.context.assets, "fonts/notosanskr_medium.otf")
            holder.itemView.sortRecent.typeface = Typeface.createFromAsset(holder.itemView.context.assets, "fonts/notosanskr_bold.otf")
            holder.itemView.sortFav.setTextColor(Color.parseColor("#bcc6d2"))
            holder.itemView.sortRecent.setTextColor(Color.parseColor("#9f56f2"))
            mListener?.onSortClicked("")
        }
    }

    private fun onBindItem(holder: ItemHolder, position: Int) {
        val realPos = position - 3

//        var param = holder.itemView.imageViewThumbnail.layoutParams
//        param.width = imageWidth
//        param.height = imageHeight
//        holder.itemView.imageViewThumbnail.layoutParams = param

        mItems[realPos].let { item ->
            holder.itemView.fullLayer.setBackgroundColor(Color.parseColor("#ffffff"))

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.thumbnail,
                    item.largeThumbnailUrl,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.profile,
                    item.userImage,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            when (item.videoType) {
                "LIVE" -> {
                    holder.itemView.label.setBackgroundResource(R.drawable.trip_onair_label)
                    holder.itemView.viewCount.text = "${StringUtils.getSnsStyleCountZeroBase(item.viewerCount.toInt())}"
                }

                "VOD" -> {
                    holder.itemView.label.setBackgroundResource(R.drawable.trip_video_label)
                    holder.itemView.viewCount.text = "${StringUtils.getSnsStyleCountZeroBase(item.viewerCount.toInt())}"
                }

                "LASTLIVE" -> {
                    holder.itemView.label.setBackgroundResource(R.drawable.trip_pass_live_label)
                    holder.itemView.viewCount.text = "${StringUtils.getSnsStyleCountZeroBase(item.viewerCount.toInt())}"
                }

                else -> holder.itemView.label.setBackgroundResource(R.drawable.trip_video_label)
            }

            /*// 기능제외
            if (item.isFavorite == "Y") {
                holder.itemView.iconLike.setBackgroundResource(R.drawable.ic_like_on)
            } else {
                holder.itemView.iconLike.setBackgroundResource(R.drawable.ic_like)
            }*/
            try {
                var price = item.min_price.toDouble()
                if(item.relationPrd.data.size < 1) {
                    holder.itemView.price.text = "${Utils.ToNumFormat(price)}원"
                } else {
                    holder.itemView.price.text = "${Utils.ToNumFormat(price)}원~"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            holder.itemView.product.text = item.min_price_product
            var tag = "#${item.strTag.replace(",", "  #")}"
            holder.itemView.hashTag.text = tag
            holder.itemView.title.text = item.title
            holder.itemView.likeCount.text = "${StringUtils.getSnsStyleCountZeroBase(item.favoriteCount.toInt())}"

            holder.itemView.setOnClickListener {
                mListener?.onItemClick(item, realPos)
            }
        }

//        if ( realPos == mItems.size - 1 )
//            holder.itemView.empty.visibility = View.VISIBLE
//        else
//            holder.itemView.empty.visibility = View.GONE
    }

    fun setFirstCategory(firstCategory : String) {
        this.f_category = firstCategory
    }

    fun changeZzim(idx: String) {
        if ( mItems != null ) {
            for ( i in mItems.indices ) {
                var bItem = mItems[i]
                var relationPrd = bItem.relationPrd
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
    }

    fun cancelTimer() {
        if ( timer != null) {
            Logger.e("cancelTimer")
            timer!!.cancel()
            timer = null
        }
    }

    fun runTimer() {
        if ( timer == null ) {
            Logger.e("runTimer")
            timerTask = object: TimerTask() {
                override fun run() {
                    Logger.e("timerTask run")
                    if ( mHandler != null )
                        mHandler!!.sendEmptyMessage(0)
                }
            }
            timer = Timer()
            timer!!.schedule(timerTask, PuddingHomeTabFragment.TIMER_DELAY , PuddingHomeTabFragment.TIMER_PERIOD)
        }
    }

    inner class SortHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class BannerHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClick(item: API114.VideoItem, position: Int)
        fun onBannerClick(item: API114.EventItem)
        fun categoryClicked(category: String)
        fun onSortClicked(order: String)
    }
}