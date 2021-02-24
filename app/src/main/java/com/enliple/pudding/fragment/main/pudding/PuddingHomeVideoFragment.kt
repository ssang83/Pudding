package com.enliple.pudding.fragment.main.pudding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.couchbase.lite.MutableDocument
import com.enliple.pudding.R
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.bus.ViewCountBus
import com.enliple.pudding.common.VideoDataContainer
import com.enliple.pudding.common.VideoManager
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.app.PriceFormatter
import com.enliple.pudding.commons.app.StringUtils
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.commons.network.vo.API98
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.shoptree.network.ShopTreeAsyncTask
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.shoppingplayer.StreamingType
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_home_video_item.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Main 홈 화면 영상 재생 Fragment
 */
class PuddingHomeVideoFragment : Fragment() {
    private var isPaused = false

    private var mIsInit: Boolean = false

    private var videoType: StreamingType = StreamingType.UNKNOWN
    private var mPosition: Int = 0
    private var mIsVisibleToUser = false
    private var dbKey = ""

    private var mData: API114.HotDataItem? = null
    private var mPlayer: SimpleExoPlayer? = null
    private var mHomeHotData: MutableList<API114.HotDataItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPosition = arguments?.getInt("position", -1)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_home_video_item, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.e("onViewCreated: pos:$mPosition")

        EventBus.getDefault().register(this)
        if (mPosition < 0) {
            return
        }

        //var key = NetworkHandler.getInstance(context!!).getHomeKey(0, "1", "", "")
/*        dbKey = "GET/mui/main/top?page=1&category=&age=all&sex=all&user=${AppPreferences.getUserId(context!!)}&order="
        val json = DBManager.getInstance(context).get(dbKey)
        if (!TextUtils.isEmpty(json)) {
            mHomeList = Gson().fromJson(json, API114::class.java)
/**
            if (mPosition < mHomeList!!.data.size) {
                mData = mHomeList!!.hotData[mPosition]
            } else {
                Logger.d("null item error:$dbKey")
                return
            }
            **/

            if ( mHomeList != null && mHomeList!!.hotData != null ) {
                if (mPosition < mHomeList!!.hotData.size) {
                    mData = mHomeList!!.hotData[mPosition]
                } else {
                    Logger.d("null item error:$dbKey")
                    return
                }
            } else {
                return
            }
        } else {
            Logger.d("null db error:$dbKey")
            return
        }*/

        mHomeHotData = VideoDataContainer.getInstance().mHomeHotData
        mData = mHomeHotData[mPosition]

        Logger.d("url:" + mData?.largeThumbnailUrl)

        ImageLoad.setImage(context, thumbnail, mData?.largeThumbnailUrl, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

        product.text = mData?.min_price_product

        var priceValue = mData?.min_price ?: ""
        priceValue = PriceFormatter.getInstance()?.getFormattedValue(priceValue) ?: "0"
        priceValue = if (mData?.relationPrd?.data?.size!! < 1) {
            "${priceValue}원"
        } else {
            "${priceValue}원~"
        }
        price.text = priceValue
        title.text = mData?.title

        viewCount.text = StringUtils.getSnsStyleCountZeroBase(mData?.viewerCount!!.toInt())
        likeCount.text = StringUtils.getSnsStyleCountZeroBase(mData?.favoriteCount!!.toInt())
        hashTag.text = StringUtils.convertHashTagText(mData?.strTag!!)

        if ("Y" == mData?.isFavorite)
            likeIcon.setBackgroundResource(R.drawable.jjim_on_ico)
        else
            likeIcon.setBackgroundResource(R.drawable.ic_like)

        when (mData?.videoType) {
            "LIVE" -> videoLabel.setBackgroundResource(R.drawable.trip_onair_label)
            "LASTLIVE" -> videoLabel.setBackgroundResource(R.drawable.trip_pass_live_label)
            else -> videoLabel.setBackgroundResource(R.drawable.trip_video_label)
        }

        ImageLoad.setImage(context, profile, mData?.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

        videoType = VideoManager.getVideoType(mData?.stream!!)
        if (videoType != StreamingType.UNKNOWN) {
            // 바로 하면 성능에 이슈가 생긴다.
        }

        fullLayer.setOnClickListener { videoClicked() }
        //buttonPlay.setOnClickListener { videoClicked() }
    }

    override fun onDetach() {
        super.onDetach()

        releaseVideo()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Logger.e("onDestroyView: $mPosition")
    }

    // 현재 보여지지 않는 화면일 경우 pause 시킨다.
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Logger.i("setUserVisibleHint:$isVisibleToUser  pos:$mPosition")

        mIsVisibleToUser = isVisibleToUser

        itemLayout?.visibility = View.VISIBLE

        if (isVisibleToUser) {
            // 재생
            Handler().postDelayed({ playVideo() }, 1000)
        } else {
            releaseVideo()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:ViewCountBus) {
        if(mData!!.id == data.streamKey) {
//            mHomeList!!.hotData[data.position].viewerCount = data.count.toString()
//
//            var temp = Gson().toJson(mHomeList)
//            val document = MutableDocument(dbKey)
//            document.setString(dbKey, temp)
//            DBManager.getInstance(context).put(document)

            VideoDataContainer.getInstance().mHomeHotData[data.position].viewerCount = data.count.toString()
            viewCount.text = StringUtils.getSnsStyleCountZeroBase(data.count)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: String) {
        if (data == "home_video_play") {
            if (mIsVisibleToUser) {
                initPlayer()
            }
        }
    }

    fun forcePlayVideo() {
        Logger.c("forcePlayVideo: $mPosition")
        if("LIVE" == mData?.videoType) {
            return
        }

        mIsVisibleToUser = true
        isPaused = false

        if (!mIsInit) {
            initPlayer()
        } else {
            mPlayer?.playWhenReady = true
            mPlayer?.playbackState
        }
    }

    private fun playVideo() {
        var ready = mPlayer?.playWhenReady
        if (ready != null && ready) {
            Logger.e("playVideo already playing:$mPosition")
            return
        }

        if (!mIsVisibleToUser) {
            Logger.e("playVideo not isVisibleToUser:$mPosition")
            return
        }

        Logger.c("playVideo: $mPosition")

        isPaused = false

        if("LIVE" == mData?.videoType) {
            return
        }

        if (!mIsInit) {
            initPlayer()
        } else {
            mPlayer?.playWhenReady = true
            mPlayer?.playbackState
        }
    }

    private fun pauseVideo() {
        Logger.e("pauseVideo: $mPosition")

        isPaused = true
        mPlayer?.playWhenReady = false
        mPlayer?.playbackState
    }

    fun releaseVideo() {
        Logger.i("releaseVideo: $mPosition")

        mPlayer?.playWhenReady = false
        mPlayer?.stop()
        mPlayer?.release()
        mPlayer = null
        mIsInit = false
    }

    private fun videoClicked() {
        Logger.e("videoClicked")

        var url = mData?.stream ?: ""

        val json = Gson().toJson(mHomeHotData)
        val videoItems: MutableList<VOD.DataBeanX> = Gson().fromJson(json, object : TypeToken<MutableList<VOD.DataBeanX>>() {}.type)
        VideoDataContainer.getInstance().mVideoData = videoItems

        Logger.d("URL : $url, mPosition : $mPosition")
        if ("LIVE" == mData?.videoType) {
            val task = ShopTreeAsyncTask(context!!)
            task.getLiveInfo(mData?.id, { result, obj ->
                try {
                    val response = Gson().fromJson(obj.toString(), API98::class.java)
                    if (response.data.size > 0) {
                        itemLayout?.visibility = View.VISIBLE
                        releaseVideo()

                        if ("Y" == response.data[0].isOnAir) {
                            startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, mPosition)
                                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, response.data[0].userId)

                                data = Uri.parse("vcommerce://shopping?url=${response.data[0].stream}")
                            })
                        } else {
                            AppToast(context!!).showToastMessage("종료된 라이브 방송입니다.",
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_BOTTOM)
                        }
                    } else {
                        AppToast(context!!).showToastMessage("종료된 라이브 방송입니다.",
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM)
                    }
                } catch (e: Exception) {
                    Logger.p(e)
                }
            })
        } else {
            if (!TextUtils.isEmpty(url)) {
                itemLayout?.visibility = View.VISIBLE
                releaseVideo()

                startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                    putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, mPosition)
                    putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, mData?.userId)

                    data = Uri.parse("vcommerce://shopping?url=${mData?.stream}")
                })
            } else {
                AppToast(view!!.context).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM)
            }
        }
    }

    /**
     * 요청된 DataSource 에 근거하여 StreamingPlayer 를 초기화
     */
    private fun initPlayer() {
        if (context == null || mData == null) {
            return
        }

        mIsInit = true

        var videoUrl = mData?.stream
        Logger.e("initPlayer pos:$mPosition url:$videoUrl")

        mPlayer = VideoManager.getExoPlayer(context!!, DefaultTrackSelector())
        var mediaSource = VideoManager.getMediaSource(context!!, videoUrl!!)

        // Clip to start at 1 seconds and end at 6 seconds.
        var clippingSource = ClippingMediaSource(mediaSource, 1_000_000, 6_000_000)
        mPlayer?.prepare(clippingSource)
        mPlayer?.volume = 0f // mute

        mPlayer?.addListener(playerEventListener)
        mPlayer?.addVideoListener(videoListener)

        if (playerView != null) {
            playerView?.player = mPlayer
            playerView?.useController = false
            playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//            playerView?.animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)

            if (mIsVisibleToUser) {
                playVideo()
            }
        }
    }

    /**
     * ExoPlayer VideoListener
     */
    private val videoListener: VideoListener = object : VideoListener {
        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
        }

        override fun onRenderedFirstFrame() {
            Logger.d("onRenderedFirstFrame $mPosition")

            itemLayout?.visibility = View.INVISIBLE

            var animation = AlphaAnimation(1f, 0f)
            animation.duration = 500
            itemLayout?.animation = animation

            // 첫번째 frame bitmap 으로 가져올 수 있다.
            //imageViewThumbnail?.setImageBitmap((playerView?.videoSurfaceView as TextureView)?.bitmap)
            //Handler().postDelayed({imageViewThumbnail?.visibility = View.INVISIBLE}, 1000)
        }
    }

    private val playerEventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Logger.d("onPlayerStateChanged $playWhenReady  state: $playbackState")

            if (playbackState == Player.STATE_ENDED) {
                itemLayout?.visibility = View.VISIBLE
            }
        }
    }
}