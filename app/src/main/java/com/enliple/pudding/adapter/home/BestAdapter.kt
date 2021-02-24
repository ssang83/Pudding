package com.enliple.pudding.adapter.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.Utils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API114
import com.google.gson.Gson
import kotlinx.android.synthetic.main.adapter_pudding_best_header_item.view.*
import kotlinx.android.synthetic.main.adapter_pudding_best_item.view.*


/**
 * Created by Kim Joonsung on 2019-04-05.
 */
class BestAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    val mItems: MutableList<API114.VideoItem> = mutableListOf()
    private var mListener: Listener? = null
    private var mPagerAdapter: BestPagerAdapter? = null
    private lateinit var mContext: Context

    private var mMargin15 = 0
    private var mMargin3 = 0
    private var mMargin6 = 0
    private var mImageHeight = 0

    constructor(context: Context) : super() {
        if (context != null) {
            var itemWidth = (AppPreferences.getScreenWidth(context) - Utils.ConvertDpToPx(context, 18)) / 2 // recycler view 좌우 padding 값제외한 순수 item 의 width
            mImageHeight = (itemWidth * 1.23F).toInt() // image width 의 비율에 맞춘 height 값
            Logger.e("mImageHeight:$mImageHeight")

            mMargin15 = Utils.ConvertDpToPx(context, 15)
            mMargin3 = Utils.ConvertDpToPx(context, 3)
            mMargin6 = Utils.ConvertDpToPx(context, 6)

            mContext = context
        }
    }

    override fun getItemViewType(position: Int) = if (position == 0) TYPE_HEADER else TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_ITEM -> BestIemHolder(LayoutInflater.from(parent.context).inflate(com.enliple.pudding.R.layout.adapter_pudding_best_item, parent, false))
                else -> BestHeaderHolder(LayoutInflater.from(parent.context).inflate(com.enliple.pudding.R.layout.adapter_pudding_best_header_item, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_HEADER -> onBindHeader(holder as BestHeaderHolder)
            else -> onBindItem(holder as BestIemHolder, position)
        }
    }

    override fun getItemCount(): Int = mItems.size + 1

    fun setItems(items: List<API114.VideoItem>) {
        this.mItems.clear()
        this.mItems.addAll(items)

        notifyDataSetChanged()
    }

    fun addItems(items: List<API114.VideoItem>) {
        if(items.isNotEmpty()) {
            val prevSize = items.size
            mItems.addAll(items)
            notifyItemInserted(prevSize + 1) // ViewPager 영역 추가해야함.
        }
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun getPagerItems() : MutableList<API114.VideoItem> {
        val items:MutableList<API114.VideoItem> = mutableListOf()
        for(i in 0..2) {
            items.add(mItems[i])
        }

        return items
    }

    private fun onBindHeader(holder: BestHeaderHolder) {
        mPagerAdapter = BestPagerAdapter(mContext, mListener!!)
        holder.itemView.viewPager?.offscreenPageLimit = 3
        holder.itemView.viewPager?.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                Logger.d("onPageSelected : $position")
            }
        })

        holder.itemView.viewPager?.adapter = mPagerAdapter
        holder.itemView.indicator.setViewPager(holder.itemView.viewPager)
    }

    private fun onBindItem(holder: BestIemHolder, position: Int) {
        val realPosition = position + 2

        if (realPosition >= mItems?.size) {
            return
        }

        var marginParams = holder.itemView.root.layoutParams as ViewGroup.MarginLayoutParams
        if (realPosition % 2 == 0) { //2개중 오른쪽 비디오
            marginParams.setMargins(mMargin3, 0, mMargin15, mMargin6)
            holder.itemView.root.layoutParams = marginParams
            holder.itemView.root.requestLayout()
        } else { // 2개중 왼쪽 비디오
            marginParams.setMargins(mMargin15, 0, mMargin3, mMargin6)
            holder.itemView.root.layoutParams = marginParams
            holder.itemView.root.requestLayout()
        }

        var params = holder.itemView.imageViewThumbnail.layoutParams
        params.height = mImageHeight
        holder.itemView.imageViewThumbnail.layoutParams = params

        mItems[realPosition].let { item ->
            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewThumbnail,
                    item.thumbnailUrl,
                    null,
                    ImageLoad.SCALE_NONE,
                    DiskCacheStrategy.ALL)

            ImageLoad.setImage(
                    holder.itemView.context,
                    holder.itemView.imageViewProfile,
                    item.userImage,
                    null,
                    ImageLoad.SCALE_CIRCLE_CROP,
                    DiskCacheStrategy.ALL)

            holder.itemView.textViewNickName.text = item.userNick
            holder.itemView.textViewTitle.text = item.title
            holder.itemView.textViewViewerCnt.text = item.viewerCount.toString()
            holder.itemView.textViewLikeCount.text = item.favoriteCount.toString()
            holder.itemView.textViewRank.text = "${realPosition + 1}"

//            if(item.isFavorite == "N") {
//                holder.itemView.imageViewLike.setBackgroundResource(R.drawable.search_like_ico)
//                holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#ffffff"))
//            } else {
//                holder.itemView.imageViewLike.setBackgroundResource(R.drawable.follow_like_off_ico)
//                holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#ff6c6c"))
//            }

            holder.itemView.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    mListener?.onItemClick(item, realPosition)
                }
            })
        }
    }

    inner class BestIemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class BestHeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class BestPagerAdapter(context: Context, mListener: Listener) : PagerAdapter() {
        private val inflater: LayoutInflater
        private var mContext: Context
        private var mData: API114.VideoItem? = null
        private var dbKey = ""

        init {
            inflater = LayoutInflater.from(context)
            mContext = context

            dbKey = "GET/mui/main/best?page=1&category=&age=all&sex=all&user=${AppPreferences.getUserId(context!!)}&order="
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            Logger.d("position : $position")

            val layout = inflater.inflate(R.layout.fragment_best_video, container, false) as ConstraintLayout

            val thumbnail = layout.findViewById<AppCompatImageView>(R.id.imageViewThumbnail)
            val userImg = layout.findViewById<AppCompatImageView>(R.id.imageViewProfile)
            val title = layout.findViewById<AppCompatTextView>(R.id.textViewTitle)
            val viewCount = layout.findViewById<AppCompatTextView>(R.id.textViewViewerCnt)
            val likeCount = layout.findViewById<AppCompatTextView>(R.id.textViewLikeCount)
            val tagList = layout.findViewById<AppCompatTextView>(R.id.textViewTag)
            val nickName = layout.findViewById<AppCompatTextView>(R.id.textViewNickName)
            val rank = layout.findViewById<AppCompatTextView>(R.id.textViewRank)

            val json = DBManager.getInstance(mContext).get(dbKey)
            if (json.isNotEmpty()) {
                val response: API114 = Gson().fromJson(json, API114::class.java)
                if (position < response.data.size) {
                    mData = response.data[position]
                } else {
                    Logger.d("null item error:$dbKey")
                }
            } else {
                Logger.d("null db error:$dbKey")
            }

            ImageLoad.setImage(mContext, thumbnail, mData?.largeThumbnailUrl, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)
            ImageLoad.setImage(mContext, userImg, mData?.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

            title.text = mData?.title
            viewCount.text = mData?.viewerCount
            likeCount.text = mData?.favoriteCount
            nickName.text = mData?.userNick
            rank.text = "${position + 1}"

            val tag = mData?.strTag
            if (!tag.isNullOrEmpty()) {
                var hashTag = ""

                var split = tag?.split(",")
                if (split!!.isNotEmpty()) {
                    for (result in split!!) {
                        hashTag += "#$result "
                    }
                }
                tagList.text = hashTag
            }

            layout.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View?) {
                    if (mListener != null) {
                        mListener!!.onPagerClick(mData!!, position)
                    }
                }
            })

            container.addView(layout)
            return layout
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            (container as ViewPager).removeView(`object` as View)
        }

        override fun getCount() = 3

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }
    }

    interface Listener {
        fun onItemClick(item: API114.VideoItem, position: Int)
        fun onPagerClick(item: API114.VideoItem, position: Int)
    }
}