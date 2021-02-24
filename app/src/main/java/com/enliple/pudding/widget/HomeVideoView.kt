package com.enliple.pudding.widget

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.enliple.pudding.common.VideoManager
import com.enliple.pudding.commons.log.Logger
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.android.synthetic.main.shopping_pip.view.*

/**
 * 비디오 Floating View
 */

class HomeVideoView : RelativeLayout {
    private var mScreenWidth = 0
    private var mScreenHeight = 0
    private var mStatusBarHeight = 0

    private var mWindowManager: WindowManager? = null

    private var mLayoutParams: WindowManager.LayoutParams? = null

    private var player: SimpleExoPlayer? = null

    private var isPaused = false
    private var mIsInit: Boolean = false
    private lateinit var mTrackSelector: DefaultTrackSelector

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mWindowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun setData(url: String, position: Long) {
        Logger.e("setData:$url")
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Logger.e("onTouchEvent ${event.action}")
        return false
    }
}