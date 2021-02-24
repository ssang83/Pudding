package com.enliple.pudding.fragment.main

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.activity.ShoppingPlayerActivity
import com.enliple.pudding.adapter.home.ShoppingListAdapter
import com.enliple.pudding.bus.SoftKeyboardBus
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.commons.widget.toast.AppToast
import com.enliple.pudding.model.PipInfo
import com.enliple.pudding.widget.main.CustomVerticalViewPager
import com.enliple.pudding.widget.main.VerticalViewPager
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_play_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

// Shopping 가운데 Fragment
class ShoppingMainFragment : androidx.fragment.app.Fragment() {

    private lateinit var playListAdapter: ShoppingListAdapter

    private var position = 0
    private var mContext: Context? = null
    private var myVODPosition: Int = -1
    private var data: VOD.DataBeanX? = null

    private var playerFlag = -1
    private var videoType = ""
    private var shareKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getArguments(arguments)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_play_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventBus.getDefault().register(this)

        mContext = view!!.context

        playListAdapter = ShoppingListAdapter(context!!, childFragmentManager, (activity as ShoppingPlayerActivity).mVideoData, myVODPosition, position,
                playerFlag, videoType, shareKey, object : ShoppingListAdapter.OnCallbackListener {
            override fun onCallback(data: VOD.DataBeanX) {
                (activity as ShoppingPlayerActivity).selectedData = data
            }
        })
        verticalViewpager.adapter = playListAdapter

        if (position > 0) {
            verticalViewpager.setCurrentItem(position, false)
        }

        Logger.e("position is $position, myVODPosition: $myVODPosition")
        verticalViewpager.overScrollMode = VerticalViewPager.OVER_SCROLL_NEVER
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            verticalViewpager.offscreenPageLimit = 1 // 2 이상이면 exoplayer 가 재생 안될 수 있다.
        }

        verticalViewpager.setOnPageChangeListener(listPageSelectedListener)
        FirebaseAnalytics.getInstance(activity!!).logEvent(playListAdapter.getItem(0).javaClass.simpleName, Bundle())
    }

    override fun onPause() {
        super.onPause()

        Logger.d("onPause()")
    }

    override fun onResume() {
        super.onResume()

        Logger.d("onResume()")
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
        //Logger.e("ShoppingMainFragment SoftKeyboardBus height: ${bus.height}")
        if (bus.height > 100) {
            verticalViewpager.setScrollDisabled(true)
        } else {
            verticalViewpager.setScrollDisabled(false)
        }
    }

    fun getCurrentPosition(): Int = verticalViewpager.currentItem

    fun getCurrentFragment(): ShoppingVideoFragment? {
        if (playListAdapter != null) {
            var f = playListAdapter.getFragment(getCurrentPosition())
            if (f is ShoppingVideoFragment) {
                return f
            }
        }
        return null
    }

    fun closeSubPop() {
        if (playListAdapter != null) {
            var f = playListAdapter.getFragment(getCurrentPosition())
            if (f is ShoppingVideoFragment) {
                f.closeSubPop()
            }
        }
    }

    fun isProductDialogShow() : Boolean {
        if (playListAdapter != null) {
            var f = playListAdapter.getFragment(getCurrentPosition())
            if (f is ShoppingVideoFragment) {
                Logger.e("video fragment")
                return f.isProductDialogShowing()
            } else {
                return false
            }
        }
        return false
    }

    fun getCurrentPipInfo(): PipInfo? {
        if (playListAdapter != null) {
            var f = playListAdapter.getFragment(getCurrentPosition())
            var pipInfo: PipInfo? = null
            if (f is ShoppingVideoFragment) {
                pipInfo = f.getPipInfo()
            } else if ( f is ShoppingLiveFragment ) {
                pipInfo = f.getPipInfo()
            }
            return pipInfo
        } else {
            return null
        }
    }

    /**
     * ShoppingVideoFragment 의 매개변수를 초기화
     */
    private fun getArguments(arguments: Bundle?) {
        if (arguments != null) {
            position = arguments.getInt(ShoppingPlayerActivity.INTENT_EXTRA_KEY_POSITION, 0)
            myVODPosition = arguments.getInt(ShoppingPlayerActivity.INTENT_EXTRA_KEY_MY_VOD_POSITION)
            playerFlag = arguments.getInt(ShoppingPlayerActivity.INTENT_EXTRA_KEY_PLAYER_FLAG)
            videoType = arguments.getString(ShoppingPlayerActivity.INTENT_EXTRA_KEY_VIDEO_TYPE) ?: ""
            shareKey = arguments.getString(ShoppingPlayerActivity.INTENT_EXTRA_KEY_SHARE_KEY) ?: ""

            Logger.d("getArguments position:$position  myVODPosition : $myVODPosition")
        }
    }

    private val listPageSelectedListener = object : CustomVerticalViewPager.OnPageChangeListener {
        val thresholdOffset = 0.5f // up /down 판단 지표

        var mStarted = false
        var mCheckDirection = false
        var mPrePlayPosition = -1
        var mIsPrePlayed = false
        var mScrollGap = 0 // scroll distance 를 계산하기 위해
        var mPosition = 0
        var mMutePosition = 0

        override fun onPageScrollStateChanged(state: Int) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                if (!mStarted && state == androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING) {
//                    mStarted = true
//                    mCheckDirection = true
//                } else {
//                    mStarted = false
//                }
//
//                // 스크롤이 멈추면 미리 play 했던 영상을 pause 시킨다.
//                if (state == androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE) {
//                    Logger.d("SCROLL_STATE_IDLE")
//
//                    // mute 시킨 video 다시 sound 켜야 한다.
//                    var f = playListAdapter.getFragment(mMutePosition)
//                    if (f is ShoppingVideoFragment) {
//                        //f.soundOn()
//                    }
//
//                    // 스크롤을 해서 다음 화면으로 가지 않은 경우는 pre play 한 비디오를 멈춰야 한다.
//                    if (mIsPrePlayed && mPosition != mPrePlayPosition) {
//                        var f = playListAdapter.getFragment(mPrePlayPosition)
//                        if (f is ShoppingVideoFragment) {
//                            f.pauseVideo()
//                            //f.soundOn()
//                        }
//                    }
//
//                    mIsPrePlayed = false
//                }
//            }
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
//            Logger.d("onPageScrolled $position  $positionOffset  $positionOffsetPixels")

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                if (mCheckDirection) {
//                    mCheckDirection = false
//
//                    mScrollGap = positionOffsetPixels
//
//                    // up / down scroll 에 맞춰 다음 영상 위치를 계산 한다.
//                    if (thresholdOffset > positionOffset) {
//                        Logger.i("going down: $position  $mScrollGap")
//                        mPrePlayPosition = position + 1
//                    } else {
//                        Logger.i("going up: $position")
//                        mPrePlayPosition = position
//                        if (mPrePlayPosition < 0) {
//                            mPrePlayPosition = 0
//                        }
//                    }
//                }
//
//                // 스크롤 하는 경우 현재 영상은 mute 하고 다음 VOD 영상을 재생 한다. ( 빠른 재생 속도를 위해 )
//                if (Math.abs(mScrollGap - positionOffsetPixels) > 200) {
//                    var f = playListAdapter.getFragment(mPrePlayPosition)
//                    if (f is ShoppingVideoFragment && !mIsPrePlayed) {
//                        mIsPrePlayed = true
//                        f.soundOff()
//                        f.playVideo()
//
//                        // 현재 영상  mute 시킨다.
//                        var current = playListAdapter.getFragment(mPrePlayPosition - 1)
//                        if (current is ShoppingVideoFragment) {
//                            mMutePosition = mPrePlayPosition - 1
//                            current.soundOff()
//                        }
//                    }
//                }
//            }
        }

        override fun onPageSelected(position: Int) {
            Logger.e("onPageSelected: $position")
            AppToast.cancelAllToast()
            // 200 ms 후에 영상을 처음부터 재생 시킨다.
//            Handler().post(Runnable {
//                var f = playListAdapter.getFragment(position)
//                if (f is ShoppingVideoFragment) {
//                    // 처음 부터 재생 시킨다.
//                    f.soundOn()
//                    f.startVideo()
//                }
//            })

            mPosition = position
            //mIsPrePlayed = false
            //mCheckDirection = true

            data = playListAdapter.getData(position)
            if (data != null) {
                (activity as ShoppingPlayerActivity).selectedData = data!!
            }

            FirebaseAnalytics.getInstance(activity!!).logEvent(playListAdapter.getItem(position).javaClass.simpleName, Bundle())
        }
    }
}