package com.enliple.pudding.adapter.home

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.fragment.main.pudding.PuddingHomeTabFragment
import com.enliple.pudding.widget.CategoryView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.adapter_pudding_beauty_category_item.view.*
import kotlinx.android.synthetic.main.adapter_pudding_beauty_event_item.view.*
import kotlinx.android.synthetic.main.common_landscape_item.view.*
import java.util.*
import kotlin.coroutines.coroutineContext

/**
 * Created by Kim Joonsung on 2019-04-18.
 */
class FashionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {
    companion object {
        private const val TYPE_EVENT = 1
        private const val TYPE_CATEGORY = 2
        private const val TYPE_ITEM = 3
    }

    val mItems: MutableList<API114.VideoItem> = mutableListOf()
    private val mEventItems: MutableList<API114.EventItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mEventPagerAdapter: EventPagerAdapter? = null
    private lateinit var mContext: Context

    private var mMargin25 = 0
    private var mViewPagerHeight = 0
    private var isCategoryClick = false
    private var orderStatus = "1"
    private var firstCategory = ""
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

    constructor(context: Context) : super() {
        if (context != null) {
            mMargin25 = Utils.ConvertDpToPx(context, 25)
            mViewPagerHeight = (AppPreferences.getScreenWidth(context) * 1.116).toInt()
            this.mContext = context

            runTimer()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_EVENT -> BeautyEventHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_pudding_beauty_event_item, parent, false))
                TYPE_CATEGORY -> BeautyCategoryHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_pudding_beauty_category_item, parent, false))
                else -> BeautyItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_landscape_item, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_EVENT -> onBindEvent(holder as BeautyEventHolder)
            TYPE_CATEGORY -> onBindCategory(holder as BeautyCategoryHolder)
            else -> onBindItem(holder as BeautyItemHolder, position)
        }
    }

    override fun getItemViewType(position: Int) =
            when (position) {
                0 -> TYPE_EVENT
                1 -> TYPE_CATEGORY
                else -> TYPE_ITEM
            }

    override fun getItemCount() =
            if (mItems.size == 0) {
                2
            } else {
                mItems.size + 2
            }

    fun getItemSize(): Int {
        if (mItems != null)
            return mItems.size
        else
            return 0
    }

    fun setFirstCategory(firstCategory : String ) {
        this.firstCategory = firstCategory
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setEventItems(items: List<API114.EventItem>) {
        Logger.e("setEventItems")
        this.mEventItems.clear()
        this.mEventItems.addAll(items)

    }

    fun setItems(items: List<API114.VideoItem>, isOrder: Boolean) {
        this.mItems.clear()
        this.mItems.addAll(items)

        if (!isOrder) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeChanged(2, items.size)
        }
    }

    fun addItems(items: List<API114.VideoItem>) {
        if (items.isNotEmpty()) {
            var prevSize = items.size
            mItems.addAll(items)
            notifyItemInserted(prevSize + 2) //  EVENT, CATEGORY 2개를 더해야 한다.
        }
    }

    fun setCategoryClick(isClick: Boolean) {
        this.isCategoryClick = isClick
    }

    private fun onBindEvent(holder: BeautyEventHolder) {
        if(mEventItems.size > 0) {
            mEventPagerAdapter = EventPagerAdapter(mContext)
            viewPager = holder.itemView.viewPager
            viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    currentPage = position
                }

                override fun onPageSelected(position: Int) {
                    Logger.d("onPageSelected : $position")
                    currentPage = position
                }
            })

            holder.itemView.viewPager.adapter = mEventPagerAdapter
            holder.itemView.viewPager.clipToPadding = false
            holder.itemView.viewPager.setPadding(mMargin25, 0, mMargin25, 0)
            holder.itemView.viewPager.pageMargin = mMargin25 / 2
            holder.itemView.viewPager.currentItem = mEventItems.size.times(1000)

            var param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            param.height = mViewPagerHeight
            holder.itemView.viewPager.layoutParams = param
        }
    }

    private fun onBindCategory(holder: BeautyCategoryHolder) {
        holder.itemView.hotTitle.text = "Hot Fashion"

        if (!isCategoryClick) {
            val json = DBManager.getInstance(mContext).get(NetworkHandler.getInstance(mContext).getKey(NetworkApi.API81.toString(), "select_union", "", "fashion"))
            holder.itemView.categoryView.setItems(json, firstCategory)
        }

        holder.itemView.categoryView.setListener(object : CategoryView.CategorySelectListener {
            override fun onCategorySelected(categoryId: String?) {
                mListener?.onCategoryClick(categoryId!!)
            }
        })

        holder.itemView.textViewRecentOrder.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                orderStatus = ""
                holder.itemView.textViewRecentOrder.isSelected = true
                holder.itemView.textViewRecentOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_bold.otf")
                holder.itemView.textViewPopularityOrder.isSelected = false
                holder.itemView.textViewPopularityOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_regular.otf")

                mListener?.onRecentOrderClicked(orderStatus)
            }
        })

        holder.itemView.textViewPopularityOrder.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                orderStatus = "1"
                holder.itemView.textViewRecentOrder.isSelected = false
                holder.itemView.textViewRecentOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_regular.otf")
                holder.itemView.textViewPopularityOrder.isSelected = true
                holder.itemView.textViewPopularityOrder.typeface = Typeface.createFromAsset(mContext.assets, "fonts/notosanskr_bold.otf")

                mListener?.onRecentOrderClicked(orderStatus)
            }
        })

        setOrderStatus(holder)
    }

    private fun onBindItem(holder: BeautyItemHolder, position: Int) {
        val realPosition = position - 2

        mItems[realPosition].let { item ->
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

            var price = PriceFormatter.getInstance()?.getFormattedValue(item?.min_price) ?: "0"
            price = if (item?.relationPrd?.data?.size!! < 1) {
                "${price}원"
            } else {
                "${price}원~"
            }
            holder.itemView.price.text = price
            holder.itemView.title.text = item?.title
            holder.itemView.product.text = item?.min_price_product
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

    private fun setOrderStatus(holder: BeautyCategoryHolder) {
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

    inner class BeautyItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class BeautyEventHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class BeautyCategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class EventPagerAdapter(context: Context) : PagerAdapter() {
        private val inflater: LayoutInflater
        private var mContext: Context
        private var mData: API114.EventItem? = null

        init {
            inflater = LayoutInflater.from(context)
            mContext = context
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            Logger.d("position : $position")
            val realPos = position.rem(mEventItems.size)

            val layout = inflater.inflate(R.layout.fragment_beauty_event_image, container, false) as LinearLayout

            val eventImg = layout.findViewById<AppCompatImageView>(R.id.imageViewBeautyEvent)
            val evenContent = layout.findViewById<AppCompatTextView>(R.id.textViewEventTitle)
            val eventSubContent = layout.findViewById<AppCompatTextView>(R.id.textViewSubTitle)
            val eventSubject = layout.findViewById<AppCompatTextView>(R.id.textViewEventSubject)

            mData = mEventItems[realPos]

            ImageLoad.setImage(mContext, eventImg, mData?.main_img, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

            evenContent.text = mData?.ev_title
            eventSubContent.text = mData?.ev_content2
            eventSubject.text = mData?.ev_content1

            layout.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    if (mListener != null) {
                        mListener!!.onPagerClicked(mEventItems[realPos])
                    }
                }
            })

            container.addView(layout)
            return layout
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            (container as ViewPager).removeView(`object` as View)
        }

        override fun getCount() = Int.MAX_VALUE

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }
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

    interface Listener {
        fun onItemClicked(item: API114.VideoItem, position: Int)
        fun onCategoryClick(categoryId: String)
        fun onRecentOrderClicked(order: String)
        fun onPopularityOrderClicked(order: String)
        fun onPagerClicked(item: API114.EventItem)
    }
}