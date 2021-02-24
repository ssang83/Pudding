package com.enliple.pudding.fragment.main.pudding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.common.VideoManager
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppConstants
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.API114
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.shoppingplayer.StreamingType
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_best_video.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Kim Joonsung on 2019-04-17.
 */
class PuddingBestVideoFragment : Fragment() {

    private var isPaused = false
    private var mIsInit: Boolean = false

    private var videoType: StreamingType = StreamingType.UNKNOWN
    private var mPosition: Int = 0
    private var mTab: Int = 0
    private var mIsVisibleToUser = false
    private var dbKey = ""

    private var mData: API114.VideoItem? = null
    private var mPlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPosition = arguments?.getInt("position", -1)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_best_video, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.e("onViewCreated: pos:$mPosition")

        EventBus.getDefault().register(this)

        if(mPosition < 0) {
            return
        }

        dbKey = "GET/mui/main/best?page=1&category=&age=all&sex=all&user=${AppPreferences.getUserId(context!!)}&order="
        val json = DBManager.getInstance(context!!).get(dbKey)
        if(json.isNotEmpty()) {
            val response : API114 = Gson().fromJson(json, API114::class.java)
            if(mPosition < response.data.size) {
                mData = response.data[mPosition]
            } else {
                Logger.d("null item error:$dbKey")
                return
            }
        } else {
            Logger.d("null db error:$dbKey")
            return
        }

        Logger.d("url:" + mData?.largeThumbnailUrl)

//        loadingView?.visibility = View.GONE

        ImageLoad.setImage(context, imageViewThumbnail, mData?.thumbnailUrl, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

        val tag = mData?.strTag
        if (!tag.isNullOrEmpty()) {
            var hashTag = ""

            var split = tag?.split(",")
            if (split!!.isNotEmpty()) {
                for (result in split!!) {
                    hashTag += "#$result "
                }
            }
            textViewTag.text = hashTag
        }

        textViewTitle.text = mData?.title
        textViewViewerCnt.text = mData?.viewerCount
        textViewLikeCount.text = mData?.favoriteCount
        textViewNickName.text = mData?.userNick
        textViewRank.text = "${mPosition + 1}"

        ImageLoad.setImage(context, imageViewProfile, mData?.userImage, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

        videoType = VideoManager.getVideoType(mData?.stream!!)
        if (videoType != StreamingType.UNKNOWN) {
            // 바로 하면 성능에 이슈가 생긴다.
        }

        layoutVideoItem.setOnClickListener { videoClicked() }
    }

    override fun onDetach() {
        super.onDetach()

//        releaseVideo()
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

//        if (isVisibleToUser) {
//            // 재생
//            Handler().post { playVideo() }
//        } else {
//            imageViewThumbnail?.visibility = View.VISIBLE
//
//            releaseVideo()
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data:String) {
        if(data == "home_video_play") {
            if(mIsVisibleToUser) {
//                initPlayer()
            }
        }
    }

    fun playVideo() {
        var ready = mPlayer?.playWhenReady
        if (ready != null && ready) {
            Logger.e("playVideo playing now!! $mPosition")
            return
        }

        Logger.c("playVideo: $mPosition")

        isPaused = false

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

    private fun releaseVideo() {
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

        Logger.d("URL : $url")
        if (!TextUtils.isEmpty(url)) {
//            imageViewThumbnail?.visibility = View.VISIBLE
//            releaseVideo()

            startActivity(Intent(context!!, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, mPosition)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CASTER_ID, mData?.userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE, mData?.videoType)

                data = Uri.parse("vcommerce://shopping?url=${mData?.stream}")
            })
        } else {
            AppToast(view!!.context).showToastMessage("방송 정보 메타 데이터가 존재하지 않습니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM)
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

//        if (playerView != null) {
//            playerView?.player = mPlayer
//            playerView?.useController = false
//            playerView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//
//            if (mIsVisibleToUser) {
//                playVideo()
//            }
//        }
    }

    /**
     * ExoPlayer VideoListener
     */
    private val videoListener: VideoListener = object : VideoListener {
        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            //Logger.i("Video Size Changed : Width $mVideoWidth, Height : $mVideoHeight")
        }

        override fun onRenderedFirstFrame() {
            Logger.d("onRenderedFirstFrame $mPosition")

            imageViewThumbnail?.visibility = View.GONE

            //loadingView?.visibility = View.GONE
            // 첫번째 frame bitmap 으로 가져올 수 있다.
            //imageViewThumbnail?.setImageBitmap((playerView?.videoSurfaceView as TextureView)?.bitmap)
            //Handler().postDelayed({imageViewThumbnail?.visibility = View.INVISIBLE}, 1000)
        }
    }

    private val playerEventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Logger.d("onPlayerStateChanged $playWhenReady  state: $playbackState")

            if (playbackState == Player.STATE_ENDED) {
                imageViewThumbnail?.visibility = View.VISIBLE
            }
        }
    }
}