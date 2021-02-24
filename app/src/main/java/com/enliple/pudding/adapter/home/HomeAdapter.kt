package com.enliple.pudding.adapter.home

import android.app.Activity
import android.content.Context
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.activity.MainActivity
import com.enliple.pudding.common.VideoManager
import com.enliple.pudding.commons.app.*
import com.enliple.pudding.commons.events.OnSingleClickListener
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.commons.network.vo.API81
import com.enliple.pudding.commons.widget.recyclerview.WrappedLinearLayoutManager
import com.enliple.pudding.fragment.main.pudding.PuddingHomeTabFragment
import com.enliple.pudding.fragment.main.pudding.PuddingHomeVideoFragment
import com.enliple.pudding.widget.main.DynamicHeightViewPager
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoListener
import kotlinx.android.synthetic.main.adapter_home_today_live_item.view.*
import kotlinx.android.synthetic.main.adapter_home_video_item.view.*
import kotlinx.android.synthetic.main.camera_bottombar.*
import kotlinx.android.synthetic.main.common_landscape_item.view.*
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by Kim Joonsung on 2019-01-24.
 */
class HomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {
    companion object {
        private const val TYPE_BEST = 1
        //private const val TYPE_BANNER = 1
        private const val TYPE_HOT_LIVE = 2
        //private const val TYPE_MDPICK = 3
        private const val TYPE_ITEM = 3
    }

    val bestItems: MutableList<API114.BestDataItem> = mutableListOf()
    val items: MutableList<API114.VideoItem> = mutableListOf()
    val hotItems: MutableList<API114.HotDataItem> = mutableListOf()
    val mdPickItems: MutableList<API114.MdPickItem.EvPickBean> = mutableListOf()
    var mBannerItem: MutableList<API114.BannerItem> = mutableListOf()
    private var activity: Activity? = null
    private var mListener: Listener? = null
    private var mHotListener: HotAdapter.Listener? = null
    private var mMdPickListener: MDPickAdapter.Listener? = null
    private var isVisible = false
    private var mdPickTitle = ""
    private var mdPickImg = ""
    private var mReFreshBest = true // best 갱신
    //private var mReFreshVideo = true // banner 갱신

    private lateinit var mCategoryItem: List<API81.CategoryItem>
    private lateinit var mContext: Context

    //private lateinit var mHotLiveAdapter: HotAdapter
    //private lateinit var mMDPickAdapter: MDPickAdapter
    private var mFragmentManager: FragmentManager

    private var mHomeVideoPagerAdapter: HomeVideoPagerAdapter? = null
    private var mParent: PuddingHomeTabFragment? = null
    private var mPrevVideoPosition = 0
    private var itemPlayPosition = -1
    private var bestHolder: PuddingHomeVideoHolder? = null
    private var hotHolder: PuddingHomeHotHolder? = null
    private var itemHolder: PuddingHomeItemHolder? = null
    private var mPlayer: SimpleExoPlayer? = null
    private var pager: DynamicHeightViewPager? = null
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var currentPage: Int = 0
    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Logger.e("msg.what :: " + msg.what)
            if ( msg.what == 0 ) {
                if ( pager != null ) {
                    Logger.e("viewpager not null")
                    currentPage = currentPage + 1
                    Logger.e("currentPage :: " + currentPage)
                    pager!!.setCurrentItem(currentPage, true)
                } else {
                    Logger.e("viewpager null")
                }
            }
        }
    }
    constructor(context: Context, fm: FragmentManager) : super() {
        if (context != null) {
            mCategoryItem = CategoryModel.getCategoryList(context, "all")
            mContext = context
        }

        mFragmentManager = fm
        runTimer()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_BEST) {
            bestHolder = PuddingHomeVideoHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_video_item, parent, false))
            return bestHolder!!
        } else if (viewType == TYPE_HOT_LIVE) {
            hotHolder = PuddingHomeHotHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_today_live_item, parent, false))
            return hotHolder!!
        } else {
            itemHolder = PuddingHomeItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_landscape_item, parent, false))
            return itemHolder!!
        }
    }

//            when (viewType) {
//                TYPE_BEST -> PuddingHomeVideoHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_video_item, parent, false))
//                //TYPE_BANNER -> PuddingHomeBannerHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_banner_item, parent, false))
//                TYPE_HOT_LIVE -> PuddingHomeHotHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_today_live_item, parent, false))
//                //TYPE_MDPICK -> PuddingHomeMdPickHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_mdpick_item, parent, false))
//                else -> PuddingHomeItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_landscape_item, parent, false))
//            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Logger.e("onBindViewHolder position:{$position} type:${holder.itemViewType}")
        if (holder is PuddingHomeVideoHolder) {
            onBindBestVideo(holder as PuddingHomeVideoHolder)
        } else if (holder is PuddingHomeHotHolder) {
            onBindHotItem(holder as PuddingHomeHotHolder)
        } else {
            onBindItem(holder as PuddingHomeItemHolder, position)
        }

//        when (holder.itemViewType) {
//            TYPE_BEST -> onBindBestVideo(holder as PuddingHomeVideoHolder)
//            //TYPE_BANNER -> onBindBanner(holder as PuddingHomeBannerHolder)
//            TYPE_HOT_LIVE -> onBindHotItem(holder as PuddingHomeHotHolder)
//            //TYPE_MDPICK -> onBindMdPickItem(holder as PuddingHomeMdPickHolder)
//            else -> onBindItem(holder as PuddingHomeItemHolder, position)
//        }
    }

    fun setActivity(activity: Activity) {
        this.activity = activity
    }

    override fun getItemViewType(position: Int) =
            when (position) {
                //0 -> TYPE_BANNER
                0 -> TYPE_BEST
                1 -> TYPE_HOT_LIVE
                //2 -> TYPE_MDPICK
                else -> TYPE_ITEM
            }

    override fun getItemCount(): Int = items.size + 2

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun setHotListener(listener: HotAdapter.Listener) {
        this.mHotListener = listener
    }

    fun setMdPickListener(listener: MDPickAdapter.Listener) {
        this.mMdPickListener = listener
    }

    fun setParentFragment(parent: PuddingHomeTabFragment) {
        mParent = parent
    }

    fun setItems(items: List<API114.VideoItem>) {
        this.items.clear()
        this.items.addAll(items)

        notifyDataSetChanged()
    }

    fun setViewCount(streamKey: String, count: Int) {
        for (i in 0 until items.size) {
            var vItem = items.get(i)
            var tItem = vItem
            var sKey = vItem.id
            if (streamKey == sKey) {
                tItem.viewerCount = count.toString()
                items.set(i, tItem)
                break
            }
        }

        for (i in 0 until bestItems.size) {
            var vItem = bestItems.get(i)
            var tItem = vItem
            var sKey = vItem.id
            if (streamKey == sKey) {
                tItem.viewerCount = count.toString()
                bestItems.set(i, tItem)
                break
            }
        }

        notifyDataSetChanged()
    }

    private fun isEqualCollection(a: List<API114.BestDataItem>, b: List<API114.BestDataItem>): Boolean {
        if (a.isEmpty() || b.isEmpty()) {
            return false
        }

        if (a.size != b.size) {
            return false
        }

        var sameCount = 0
        for (i in 0 until a.size) {
            if (a[i].id == b[i].id) {
                sameCount++
            }
        }
        return a.size == sameCount
    }

//    fun setBannerItem(list: List<API114.BannerItem>) {
//        mReFreshBest = true
//        if (isEqualCollection(list, mBannerItem)) {
//            Logger.d("setBannerItem need refresh")
//            // 바뀌부분이 있을때만 갱신한다. ( 배너 이미지 깜빡이는 문제 때문에 )
//            mReFreshBest = false
//            return
//        }
//
//        mBannerItem.clear()
//
//        if (!list.isEmpty()) {
//            this.mBannerItem.addAll(list)
//        }
//    }

    fun setBestItems(list: List<API114.BestDataItem>) {
        mReFreshBest = true
//        if (isEqualCollection(list, bestItems)) {
//            Logger.d("setBestItems need refresh")
//             바뀌부분이 있을때만 갱신한다.
//            mReFreshBest = false
//            return
//        }

        this.bestItems.clear()
        if (list.isNotEmpty()) {
            this.bestItems.addAll(list)
        }
    }

    fun setHotItems(list: List<API114.HotDataItem>) {
        this.hotItems.clear()
        if (list.isNotEmpty()) {
            this.hotItems.addAll(list)
        }
    }

    fun setMdPickItems(list: List<API114.MdPickItem.EvPickBean>) {
//        this.mdPickItems.clear()
//        if (list.isNotEmpty()) {
//            this.mdPickItems.addAll(list)
//        }
    }

    fun setMdPickInfo(isShow: Boolean, title: String, image: String) {
        this.isVisible = isShow
        this.mdPickTitle = title
        this.mdPickImg = image
    }

    private fun onBindItem(holder: PuddingHomeItemHolder, position: Int) {
        Logger.e("onBindItem")
        val realPosition = position - 2
        items[realPosition].let { item ->
            Logger.e("itemPlayPosition :: $itemPlayPosition and realPosition :: $realPosition  and position :: $position and item.videoType ${item.videoType}")
            ImageLoad.setImage(mContext, holder.itemView.thumbnail, item.largeThumbnailUrl, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)
            Logger.e("pip status :: " + mParent!!.getPipStatus())
            if (itemPlayPosition == position && !mParent!!.getPipStatus() ) {
                if ("LIVE" != item.videoType) {
                    Logger.e("item.stream :: ${item.stream}")
                    var videoUrl = item.stream
                    Logger.e("initPlayer url:$videoUrl")
//                    var url = videoUrl
//                    url = videoUrl.replace("http://cache.midibus.kinxcdn.com/hls/", "")
//                    Logger.e("url :: " + url)
//                    var arr = url.split("/")
//                    var valueKey = arr.get(1)
//                    url = "https://cache.midibus.kinxcdn.com/direct/${valueKey}"
//                    retriveVideoFrameFromVideo(url)
//                    Logger.e("url : " + url)

                    mPlayer = VideoManager.getExoPlayer(mContext!!, DefaultTrackSelector())
                    var mediaSource = VideoManager.getMediaSource(mContext!!, videoUrl!!)

//                    Main Auto Play 재생 시점
//                    server 에서 play time이 넘어오지 않을 경우 혹은 00:00:00으로 넘어올 경우 : 1 ~ 6초 구간 재생
//                    영상 재생시간이 12초이하일 경우 : 1 ~ 6초, 2 ~ 7초, 3 ~ 8초. 이 중에 랜덤으로 재생
//                    영상 재생시간이 12초 ~ 100초 일 경우 : 1 ~ 6초 , 영상의 25%시점부터 5초간, 영상의 50%시점부터 5초간. 이 중에 랜덤으로 재생
//                    영상 재생시간이 100초 이상일 경우 : 1 ~ 6초 , 영상의 25%시점부터 5초간, 영상의 50%시점부터 5초간, 영상의 75%시점부터 5초간. 이 중에 랜덤으로 재생
                    var duration = Utils.dateParseRegExp(item.duration)
                    duration /= 1000
                    Logger.e("duration :: $duration")
                    if ( duration <= 0L ) {
                        var url = videoUrl.replace("http://cache.midibus.kinxcdn.com/hls/", "")
                        Logger.e("url :: $url")
                        var arr = url.split("/")
                        var valueKey = arr.get(1)
                        url = "https://cache.midibus.kinxcdn.com/direct/${valueKey}"
                        duration = getDurationFromMetaData(url)
                        if ( duration > 0 )
                            duration /= 1000
                        Logger.e("duration from metadata :: $duration")
                    }
                    var start = 1
                    if ( duration <= 0L ) {
                    } else {
                        if ( duration <= 12L) {
                            var randomIndex = Random().nextInt(2)
                            if (randomIndex == 0) {
                                start = 1
                            } else if (randomIndex == 1) {
                                start = 2
                            } else {
                                start = 3
                            }
                        } else {
                            Logger.e("sstts  duration over 12")
                            try {
                                var halfValue = duration / 2
                                if ( halfValue >= 50 ) {
                                    var randomIndex = Random().nextInt(3)
                                    if (randomIndex == 0) {
                                        start = 1
                                    } else if (randomIndex == 1) {
                                        start = (halfValue / 4).toInt()
                                    } else if ( randomIndex == 2 ) {
                                        start = (halfValue / 2).toInt()
                                    } else {
                                        start = (halfValue * ( 3 / 4 )).toInt()
                                    }
                                } else {
                                    var randomIndex = Random().nextInt(2)
                                    if (randomIndex == 0) {
                                        start = 1
                                    } else if (randomIndex == 1) {
                                        start = (halfValue / 4).toInt()
                                    } else {
                                        start = (halfValue / 2).toInt()
                                    }
                                }
                            } catch(e:Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    if ( start <= 0 )
                        start = 1

                    var end = start + 5
//                    Toast.makeText(mContext, "시작시간 : $start , 종료시간 : $end", Toast.LENGTH_SHORT).show()
                    var startTime = start * 1000000L
                    var endTime = end * 1000000L

                    var clippingSource = ClippingMediaSource(mediaSource, startTime, endTime)
                    mPlayer?.prepare(clippingSource)
                    mPlayer?.volume = 0f // mute

                    mPlayer?.addListener(object : Player.EventListener {
                        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                            if (playbackState == Player.STATE_ENDED) {
                                holder.itemView.playerView.visibility = View.INVISIBLE
                                holder.itemView.thumbnail.visibility = View.VISIBLE
                            }
                        }
                    })
                    mPlayer?.addVideoListener(object : VideoListener {
                        override fun onRenderedFirstFrame() {
                            Logger.e("onRenderedFirstFragme")
                            holder.itemView.thumbnail.visibility = View.INVISIBLE
                            var animation = AlphaAnimation(1f, 0f)
                            animation.duration = 500
                            holder.itemView.playerView.visibility = View.VISIBLE
                            holder.itemView.thumbnail.animation = animation
                        }
                    })

                    if (holder.itemView.playerView != null) {
                        holder.itemView.playerView.player = mPlayer
                        holder.itemView.playerView.useController = false
                        holder.itemView.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        holder.itemView.playerView.animation = AnimationUtils.loadAnimation(mContext!!, R.anim.fade_in)

                        mPlayer?.playWhenReady = true
                        mPlayer?.playbackState
                    }
                } else {
                    holder.itemView.thumbnail.visibility = View.VISIBLE
                    holder.itemView.playerView.visibility = View.INVISIBLE
                }
            } else {
                holder.itemView.thumbnail.visibility = View.VISIBLE
                holder.itemView.playerView.visibility = View.INVISIBLE
            }


            when (item.videoType) {
                "LIVE" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_onair_label)
                "LASTLIVE" -> holder.itemView.label.setBackgroundResource(R.drawable.trip_pass_live_label)
                else -> holder.itemView.label.setBackgroundResource(R.drawable.trip_video_label)
            }

/*            // 기능제외
            if ("Y" == item.isFavorite) {
                holder.itemView.iconLike.setBackgroundResource(R.drawable.ic_like_on)
            } else {
                holder.itemView.iconLike.setBackgroundResource(R.drawable.ic_like)
            }*/

            var price = PriceFormatter.getInstance()?.getFormattedValue(item.min_price) ?: "0"
            price = if (item?.relationPrd?.data?.size!! < 1) {
                "${price}원"
            } else {
                "${price}원~"
            }

            holder.itemView.product.text = item.min_price_product
            holder.itemView.price.text = price
            holder.itemView.title.text = item?.title
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

    fun getDurationFromMetaData(videoPath: String): Long {
        var mediaMetadataRetriever: MediaMetadataRetriever? = null;
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, HashMap<String, String>())
            else
                mediaMetadataRetriever.setDataSource(videoPath)
            //   mediaMetadataRetriever.setDataSource(videoPath);
            Logger.e("value :: ${mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)}")
            var duration =  mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            var lDuration = duration.toLong()
            return lDuration

        } catch (e: Exception) {
            e.printStackTrace()

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release()
            }
        }
        return 0
    }

    fun retriveVideoFrameFromVideo(videoPath: String): Long {
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, HashMap<String, String>())
            else
                mediaMetadataRetriever.setDataSource(videoPath)
            //   mediaMetadataRetriever.setDataSource(videoPath);
            Logger.e("value :: ${mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)}")
            Logger.e("width :: ${mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)} , height :: ${mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)}" )
            var duration =  mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            var lDuration = duration.toLong()
            lDuration = lDuration * 1000
            return lDuration
            Logger.e("duration :: $duration")
        } catch (e: Exception) {
            e.printStackTrace()

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release()
            }
        }
        return 0
    }

    private fun onBindBestVideo(holder: PuddingHomeVideoHolder) {
        if (mHomeVideoPagerAdapter == null && hotItems.size > 0) {
            mHomeVideoPagerAdapter = HomeVideoPagerAdapter(mFragmentManager)
            pager = holder.itemView.bestViewPager
            pager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    Logger.i("onPageSelected: $position")
                    currentPage = position
                    val realPosition = position.rem(hotItems.size)
                    Logger.e("realPosition :: " + realPosition + " position :: " + position)
                    if (mPrevVideoPosition != realPosition) {
                        // 이전 video release
                        if (mHomeVideoPagerAdapter?.getFragment(mPrevVideoPosition) != null) {
                            (mHomeVideoPagerAdapter?.getFragment(mPrevVideoPosition) as PuddingHomeVideoFragment)?.releaseVideo()
                        }
                        mPrevVideoPosition = realPosition
                    }

                    Handler().postDelayed({
                        if (mHomeVideoPagerAdapter?.getFragment(realPosition) != null) {
                            (mHomeVideoPagerAdapter?.getFragment(realPosition) as PuddingHomeVideoFragment)?.forcePlayVideo()
                        }
                    }, 500)
                }

                // viewpager scroll 중에 refresh 안되도록..
                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        mParent?.enableSwipeRefresh(true)
                    } else {
                        mParent?.enableSwipeRefresh(false)
                    }
                }

                override fun onPageScrolled(position: Int, p1: Float, p2: Int) {
                    val realPosition = position.rem(hotItems.size)
                    Logger.e("realPosition :: " + realPosition + " position :: " + position)
                    currentPage = position
                    setPagerIndicatorState(holder, realPosition)
                }
            })

            holder.itemView.bestViewPager?.adapter = mHomeVideoPagerAdapter
        }

        if (mReFreshBest) {
            Logger.e("onBindBestVideo")

            setPagerIndicator(holder, hotItems?.size)

            mHomeVideoPagerAdapter?.setData(hotItems?.size)
            holder.itemView.bestViewPager?.currentItem = 0
        }
    }

    private fun onBindHotItem(holder: PuddingHomeHotHolder) {
        holder.itemView.recyclerViewHotLive.layoutManager = WrappedLinearLayoutManager(mContext).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        var adapter = HotAdapter(mContext, false).apply {
            setListener(mHotListener!!)
            setItems(bestItems)
        }

        holder.itemView.recyclerViewHotLive.adapter = adapter
    }

    private fun setPagerIndicator(holder: PuddingHomeVideoHolder, pageCount: Int) {
        if (holder.itemView.indicator.childCount > 0) {
            holder.itemView.indicator.removeAllViews()
        }

        for (i in 0 until pageCount) {
            val dot = RadioButton(mContext)
            dot.setButtonDrawable(R.drawable.home_dot_indicator)
            dot.id = i
            dot.isEnabled = false
            holder.itemView.indicator.addView(dot)

            (dot.layoutParams as RadioGroup.LayoutParams).apply {
                width = Utils.ConvertDpToPx(holder.itemView.context, 12)
                height = Utils.ConvertDpToPx(holder.itemView.context, 12)
            }
        }
    }

    private fun setPagerIndicatorState(holder: PuddingHomeVideoHolder, position: Int) {
        try {
            for (i in hotItems.indices) {
                if (i == position) {
                    holder.itemView.indicator.getChildAt(i).isSelected = true
                } else {
                    holder.itemView.indicator.getChildAt(i).isSelected = false
                }
            }
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

//    private fun onBindMdPickItem(holder: PuddingHomeMdPickHolder) {
//        if (isVisible) {
//            holder.itemView.layoutMDPick.visibility = View.VISIBLE
//            holder.itemView.textViewMDPickTitle.text = mdPickTitle
//
//            ImageLoad.setImage(
//                    mContext,
//                    holder.itemView.imageViewMDPickBG,
//                    mdPickImg,
//                    null,
//                    ImageLoad.SCALE_NONE,
//                    DiskCacheStrategy.ALL)
//
//            holder.itemView.recyclerViewMDPick.layoutManager = WrappedLinearLayoutManager(mContext).apply {
//                orientation = LinearLayoutManager.HORIZONTAL
//            }
//            mMDPickAdapter = MDPickAdapter().apply {
//                setListener(mMdPickListener!!)
//            }
//
//            holder.itemView.recyclerViewMDPick.adapter = mMDPickAdapter
//
//            mMDPickAdapter.setItems(mdPickItems)
//        } else {
//            holder.itemView.layoutMDPick.visibility = View.GONE
//        }
//    }

//    private fun setLikeStatus(isLike: String, holder: PuddingHomeItemHolder) {
//        if ("N" == isLike) {
//            holder.itemView.layoutLike.setBackgroundResource(R.drawable.like_circle_off)
//            holder.itemView.imageViewLike.setBackgroundResource(R.drawable.my_jjim_off_ico)
//            holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#bcc6d2"))
//        } else {
//            holder.itemView.layoutLike.setBackgroundResource(R.drawable.like_circle_on)
//            holder.itemView.imageViewLike.setBackgroundResource(R.drawable.my_jjim_on_ico)
//            holder.itemView.textViewLikeCount.setTextColor(Color.parseColor("#ff6c6c"))
//        }
//    }

    inner class HomeVideoPagerAdapter : FragmentStatePagerAdapter {
        private var mCount = Int.MAX_VALUE
        private var mNameMap = SparseArray<String>()

        constructor(fm: FragmentManager) : super(fm) {
            mFragmentManager = fm
        }

        fun setData(count: Int) {
            notifyDataSetChanged()

            Logger.e("setData: $count")
        }

        override fun getItemPosition(`object`: Any) = PagerAdapter.POSITION_NONE

        override fun getItem(position: Int): Fragment = PuddingHomeVideoFragment().apply {
            val realPosition = position.rem(hotItems.size)
            arguments = Bundle().apply {
                putInt("position", realPosition)
            }
        }

        override fun getCount(): Int = mCount

        override fun getPageTitle(position: Int): CharSequence? = ""

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            Logger.d("instantiateItem: $position")
            val realPosition = position.rem(hotItems.size)
            var fragment = getItem(realPosition)
            var name = makeFragmentName(container.id, realPosition)

            var transaction = mFragmentManager.beginTransaction()
            transaction.add(container.id, fragment, name)
            transaction.commitAllowingStateLoss()

            mNameMap.put(realPosition, name)
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            Logger.d("destroyItem: $position")
            var fragment = mFragmentManager.findFragmentByTag(makeFragmentName(container.id, position))
            if (fragment != null) {
                var transaction = mFragmentManager.beginTransaction()
                transaction.remove(fragment)
                transaction.commitAllowingStateLoss()
            }

            mNameMap.delete(position)
        }

        fun getFragment(position: Int): Fragment? {
            return mFragmentManager.findFragmentByTag(mNameMap[position])
        }

        private fun makeFragmentName(viewId: Int, id: Int): String {
            return "android:switcher:$viewId:$id"
        }
    }

    fun changeZzim(idx: String) {
        if (bestItems != null) {
            for (i in bestItems.indices) {
                var bItem = bestItems[i]
                var relationPrd = bItem.relationPrd
                var relationPrdArr = relationPrd.data
                for (j in relationPrdArr.indices) {
                    var item = relationPrdArr[j]
                    if (idx == item.idx) {
                        Logger.e("bestItems same idx is :: " + idx + " i is " + i + " , j is " + j)
                        var isWish = "N"
                        var wishCnt = 0
                        Logger.e("bestItems before wish  :: " + item.is_wish)
                        Logger.e("bestItems before wish_cnt  :: " + item.wish_cnt)
                        if (item.is_wish == "N") {
                            isWish = "Y"
                            wishCnt = Integer.valueOf(item.wish_cnt) + 1
                        } else {
                            isWish = "N"
                            wishCnt = Integer.valueOf(item.wish_cnt) - 1
                        }
                        item.is_wish = isWish
                        item.wish_cnt = "$wishCnt"
                        Logger.e("bestItems after wish  :: " + item.is_wish)
                        Logger.e("bestItems after wish_cnt  :: " + item.wish_cnt)
                        relationPrdArr.set(j, item)
                    }
                }
                relationPrd.data = relationPrdArr
                bItem.relationPrd = relationPrd
                bestItems.set(i, bItem)
            }
        }

        if (items != null) {
            for (i in items.indices) {
                var bItem = items[i]
                var relationPrd = bItem.relationPrd
                var relationPrdArr = relationPrd.data
                for (j in relationPrdArr.indices) {
                    var item = relationPrdArr[j]
                    if (idx == item.idx) {
                        Logger.e("items same idx is :: " + idx + " i is " + i + " , j is " + j)
                        var isWish = "N"
                        var wishCnt = 0
                        Logger.e("items before wish  :: " + item.is_wish)
                        Logger.e("items before wish_cnt  :: " + item.wish_cnt)
                        if (item.is_wish == "N") {
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

        if (hotItems != null) {
            for (i in hotItems.indices) {
                var bItem = hotItems[i]
                var relationPrd = bItem.relationPrd
                var relationPrdArr = relationPrd.data
                for (j in relationPrdArr.indices) {
                    var item = relationPrdArr[j]
                    if (idx == item.idx) {
                        Logger.e("hotItems same idx is :: " + idx + " i is " + i + " , j is " + j)
                        var isWish = "N"
                        var wishCnt = 0
                        Logger.e("hotItems before wish  :: " + item.is_wish)
                        Logger.e("hotItems before wish_cnt  :: " + item.wish_cnt)
                        if (item.is_wish == "N") {
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

    fun releaseVideo() {
        Logger.e("releaseVideo")
        mPlayer?.playWhenReady = false
        mPlayer?.stop()
        mPlayer?.release()
        mPlayer = null
    }

    fun playVideo(position: Int) {
        releaseVideo()
        itemPlayPosition = -1
        if (position < 0)
            return
        if (position == 0) {

        } else if (position == 1) {

        } else {
            playItem(position)
        }
    }

    private fun playItem(position: Int) {
        itemPlayPosition = position
        notifyDataSetChanged()
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
            timer!!.schedule(timerTask, PuddingHomeTabFragment.TIMER_HOME_DELAY , PuddingHomeTabFragment.TIMER_HOME_PERIOD)
        }
    }

    inner class PuddingHomeVideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    inner class PuddingHomeItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    inner class PuddingHomeHotHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
    //inner class PuddingHomeMdPickHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface Listener {
        fun onItemClicked(item: API114.VideoItem, position: Int)
    }
}