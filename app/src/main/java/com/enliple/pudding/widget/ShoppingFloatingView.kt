package com.enliple.pudding.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import com.enliple.pudding.bus.VideoPipBus
import com.enliple.pudding.common.VideoManager
import com.enliple.pudding.commons.internal.AppPreferences
import com.enliple.pudding.commons.log.Logger
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.android.synthetic.main.shopping_pip.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import com.enliple.pudding.commons.ui_compat.PixelUtil.pxToDp



/**
 * 비디오 Floating View
 */

class ShoppingFloatingView : FrameLayout {
    companion object {
        const val MAX_CLICK_DURATION = 150
        const val MAX_CLICK_DISTANCE = 70
    }
    private var mTouchStartX: Float = 0.toFloat()
    private var mTouchStartY: Float = 0.toFloat()
    private var mLastX: Float = 0.toFloat()
    private var mLastY: Float = 0.toFloat()
    private var mScreenWidth = 0
    private var mScreenHeight = 0
    private lateinit var mUrl: String
    private var mPosition: Long = 0
    private var mStreamKey: String = ""
    private var mStatusBarHeight = 0

    private var mWindowManager: WindowManager? = null

    private var mLayoutParams: WindowManager.LayoutParams? = null

    private var player: SimpleExoPlayer? = null

    private var isPaused = false
    private var mIsInit: Boolean = false
    private lateinit var mTrackSelector: DefaultTrackSelector
    private var pipBus: VideoPipBus? = null
    private var pressStartTime: Long? = 0L
    private var pressedX: Float? = 0f
    private var pressedY: Float? = 0f
    var listener: CListener? = null
    private var isFirst = true;
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }
    interface CListener {
        fun clicked()
        fun ended()
    }

    fun setCListener(listener: CListener) {
        this.listener = listener
    }
    private fun init() {
        mWindowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Logger.e("onDetachedFromWindow")

        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: String) {
        when (data) {
            "resume_floating_video" -> playVideo()
            "pause_floating_video" -> pauseVideo()
        }
    }


    fun setData(url: String, position: Long, streamKey: String) {
        Logger.e("setData:$url")

        mUrl = url
        mPosition = position
        mStreamKey = streamKey
        initVideo()
    }

    fun setPlayerInfo(bus: VideoPipBus) {
        pipBus = bus
    }

    fun setMute(isMute:Boolean, volume:Float) {
        Logger.e("isMute:$isMute")
        if(isMute) {
            player?.volume = 0f
        } else {
            if(AppPreferences.getVideoVolumn(context!!) == 0f) {
                player?.volume = volume
            } else {
                player?.volume = AppPreferences.getVideoVolumn(context!!)
            }
        }
    }

    /**
     * 요청된 DataSource 에 근거하여 StreamingPlayer 를 초기화
     */
    private fun initVideo() {
        if (playerView != null) {
            Logger.e("initVideo")
            mTrackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory())
            player = VideoManager.getExoPlayer(context!!, mTrackSelector)

            playerView.player = player

            var mediaSource = VideoManager.getMediaSource(context!!, mUrl)
            player?.prepare(LoopingMediaSource(mediaSource, 1)) // 반복 재생 X

            player?.addListener(playerEventListener)
            //player?.addVideoListener(videoListener)

            playerView.useController = false
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            mIsInit = true

            player?.seekTo(mPosition)
            playVideo()
        }
    }

    /**
     * 비디오 재생
     */
    fun playVideo() {
        Logger.c("playVideo: $mPosition")

        if (!mIsInit) {
            initVideo()
        }

        if (isPaused) {
            isPaused = false
        }

        player?.playWhenReady = true
        player?.playbackState
    }

    /**
     * 비디오 일시정지
     */
    fun pauseVideo() {
        Logger.c("pauseVideo: $mPosition")

        if (player?.playbackState ?: Player.STATE_IDLE != Player.STATE_IDLE) {
            player?.playWhenReady = false
            isPaused = true
        }
    }

    /**
     * 비디오 정지
     */
    fun releaseVideo() {
        Logger.c("releaseVideo: $mPosition")

        player?.playWhenReady = false
        player?.release()
        player = null
    }

    fun getCurrentPosition() : Long? {
        return player?.currentPosition
    }
    fun getStreamKey() : String? {
        return mStreamKey
    }
    private val playerEventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            // 영상 재생 종료 시 controller 가 자동으로 보이는 것을 막는다.!!
            Logger.d("onPlayerStateChanged $playWhenReady  state: $playbackState")
            if (playbackState == Player.STATE_ENDED) {
                playerView.hideController()
                if ( listener != null )
                    listener!!.ended()
            }
        }
    }

    fun setParams(params: WindowManager.LayoutParams, width: Int, height: Int, statusBarHeight: Int) {
        Logger.d("setParam width:$width  height:$height  statusHeight$statusBarHeight")

        mLayoutParams = params
        mScreenWidth = width
        mScreenHeight = height
        mStatusBarHeight = statusBarHeight
        if ( isFirst ) {
            isFirst = false
        } else {
            mLayoutParams?.x = 0
            mWindowManager?.updateViewLayout(this, mLayoutParams)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //Logger.d("onTouchEvent ${event.action}")

        mLastX = event.rawX
        mLastY = event.rawY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressStartTime = System.currentTimeMillis()
                mTouchStartX = event.x
                mTouchStartY = event.y
                Logger.e("down event.y :: " + event.y)
                return false
            }

            MotionEvent.ACTION_UP -> {
                Logger.e("up event.y :: " + event.y)
                var presssDuration = System.currentTimeMillis() - pressStartTime!!
                Logger.e("pressDuration :: " + presssDuration)
                if ( (presssDuration < MAX_CLICK_DURATION) && (distance(mTouchStartX!!, mTouchStartY!!, event.x, event.y) < MAX_CLICK_DISTANCE) ) {
                    Logger.e("clicked called")
                    if ( listener != null )
                        listener!!.clicked()
                } else {
                    Logger.e("clicked not called")
                }
                mTouchStartX = 0f
                mTouchStartY = 0f
                return false
            }

            MotionEvent.ACTION_MOVE -> updateViewPosition()
        }
        return true
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        Logger.e("x1 : $x1 and y1: $y1 and x2 : $x2 and y2 : $y2")
        val dx = x1 - x2
        val dy = y1 - y2
        Logger.e("dx :: $dx and dy :: $dy")
        val distanceInPx = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        Logger.e("distanceInPx :: " + distanceInPx)
        return pxToDp(distanceInPx).toFloat()
    }

    private fun pxToDp(px: Float): Float {
        return px / resources.displayMetrics.density
    }

    private fun updateViewPosition() {
        if (mLayoutParams != null) {
            mLayoutParams?.gravity = Gravity.LEFT or Gravity.TOP

            var newX = mLastX - mTouchStartX
            var newY = mLastY - mTouchStartY
            if (newX < 0) {
                newX = 0f
            }
            if (newY < 0) {
                newY = 0f
            }

            var maxX = mScreenWidth - width
            var maxY = mScreenHeight - height - mStatusBarHeight

            if (newX > maxX) {
                newX = maxX.toFloat()
            }
            if (newY > maxY) {
                newY = maxY.toFloat()
            }
            Logger.e("newX.toInt() ${newX.toInt()}")
            mLayoutParams?.x = newX.toInt()
            mLayoutParams?.y = newY.toInt()
            mWindowManager?.updateViewLayout(this, mLayoutParams)
        }
    }
}