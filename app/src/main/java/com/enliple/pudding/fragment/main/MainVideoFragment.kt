package com.enliple.pudding.fragment.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.enliple.pudding.R
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.common.VideoManager
import com.enliple.pudding.commons.app.ImageLoad
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.shoppingplayer.StreamingType
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.video.VideoListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_main_video.*

/**
 * Main 화면 영상 재생 Fragment
 */
class MainVideoFragment : androidx.fragment.app.Fragment() {
    companion object {
//        fun newInstance(num: Int): MainVideoFragment {
//            val f = MainVideoFragment()
//            val args = Bundle()
//            args.putInt("num", num)
//            f.arguments = args
//            return f
//        }
    }

    private var player: SimpleExoPlayer? = null
    private var isPaused = false

    private var timePosition: Long = C.TIME_UNSET
    private var mVideoWidth: Int = 0
    private var mVideoHeight: Int = 0

    private var mInit: Boolean = false

    private var videoType: StreamingType = StreamingType.UNKNOWN
    private var mPosition: Int = 0
    private var mTab: Int = 0
    private var mIsVisibleToUser = false
    private lateinit var data: VOD.DataBeanX

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPosition = arguments?.getInt("position", -1)!!
        mTab = arguments?.getInt("tab", -1)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_main_video, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.i("onViewCreated: $mPosition  tab:$mTab")

        if (mPosition < 0) {
            return
        }

        var key = NetworkHandler.getInstance(context!!).getHomeKey(mTab, "", "", "")
        var json = DBManager.getInstance(context).get(key)
        if (!TextUtils.isEmpty(json)) {
            var result: VOD = Gson().fromJson(json, VOD::class.java)
            if (mPosition < result.data.size) {
                data = result.data[mPosition]
            } else {
                Logger.e("null db error:$key")
                return
            }
        } else {
            Logger.e("null db error:$key")
            return
        }

        Logger.d("url:" + data.thumbnailUrl)
        ImageLoad.setImage(context, imageViewThumbnail, data?.thumbnailUrl, null, ImageLoad.SCALE_NONE, DiskCacheStrategy.ALL)

//        if (data?.videoType == "LASTLIVE" || data?.videoType == "VOD") {
//            imageViewLiveLabel.setBackgroundResource(R.drawable.pass_live_label)
//        } else if (data?.videoType == "LIVE") {
//            imageViewLiveLabel.setBackgroundResource(R.drawable.live_label)
//        } else {
//            imageViewLiveLabel.setBackgroundResource(R.drawable.multi_live_label)
//        }
        when (data?.videoType) {
            "LIVE" -> imageViewLiveLabel.setBackgroundResource(R.drawable.live_label)
            "MULTILIVE" -> imageViewLiveLabel.setBackgroundResource(R.drawable.multi_live_label)
            else -> imageViewLiveLabel.setBackgroundResource(R.drawable.pass_live_label)
        }

        var hashtag = ""
        if (data.strTag != null && data.strTag.contains(",")) {
            var tag = data.strTag.split(",")
            for (i in 0 until tag.size) {
                hashtag += "#${tag.get(i)} "
            }
            textViewTag.text = hashtag
        } else {
            textViewTag.text = "#${data.strTag}"
        }

        textViewBroadcastTitle.text = data.title
        textViewTitle.text = data.relationPrd.data[0].strPrdName

        ImageLoad.setImage(context, imageViewProduct, data.relationPrd.data[0].strPrdImg, null, ImageLoad.SCALE_CIRCLE_CROP, DiskCacheStrategy.ALL)

        videoType = VideoManager.getVideoType(data.stream!!)
        if (videoType != StreamingType.UNKNOWN) {
            // 바로 하면 성능에 이슈가 생긴다.
            //initPlayer()
        }

        layoutVideoItem.setOnClickListener { videoClicked() }
        //buttonPlay.setOnClickListener { videoClicked() }
    }

    override fun onDetach() {
        super.onDetach()

        releaseVideo()
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

        if (isVisibleToUser) {
            // 재생
        } else {
            pauseVideo()
        }
    }

    fun playVideo() {
        Logger.e("playVideo: $mPosition")

        if (isPaused) {
            isPaused = false
        } else {
            if (!mInit) {
                initPlayer()
            }
        }

        player?.playWhenReady = true
        player?.playbackState
    }

    fun pauseVideo() {
        Logger.e("pauseVideo: $mPosition")

        isPaused = true
        player?.playWhenReady = false
        player?.playbackState
    }

    fun releaseVideo() {
        Logger.i("releaseVideo: $mPosition")

        player?.playWhenReady = false
        player?.stop()
        player?.release()
        player = null
    }

    private fun videoClicked() {
        Logger.e("videoClicked")
        timePosition = player?.currentPosition ?: C.TIME_UNSET

        var url = data?.stream ?: ""
        var userId = data?.userId

        Logger.d("URL : $url")
        if (!TextUtils.isEmpty(url)) {
            startActivity(Intent(view!!.context, ShoppingPlayerActivity::class.java).apply {
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CHAT_NICKNAME, AppPreferences.getUserId(view!!.context))
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_CHAT_ACCOUNT, userId)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_SCREEN_ORIENTATION_VERTICAL_ONLY, true)
                putExtra(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, mPosition)
                data = Uri.parse("vcommerce://shopping?url=$url")
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
        if (playerView != null) {
            if (videoType != StreamingType.UNKNOWN && videoType != StreamingType.RTMP && videoType != StreamingType.RTSP) {
                // 라이브 방송은  play 하지 않는다.
                player = VideoManager.getExoPlayer(activity!!, DefaultTrackSelector(AdaptiveTrackSelection.Factory()))
                player?.volume = 0f
                player?.addVideoListener(videoListener)

                var mediaSource = VideoManager.getMediaSource(activity!!, data.stream)
                player?.prepare(LoopingMediaSource(mediaSource)) // 반복 재생

                playerView.player = player
            }
        }

        mInit = true
    }

    /**
     * ExoPlayer VideoListener
     */
    private val videoListener: VideoListener = object : VideoListener {
        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            Logger.i("Video Size Changed : Width $mVideoWidth, Height : $mVideoHeight")
        }

        override fun onRenderedFirstFrame() {
            Logger.d("onRenderedFirstFrame $mPosition")

            imageViewThumbnail?.visibility = View.INVISIBLE
            buttonPlay?.visibility = View.GONE
        }
    }
}