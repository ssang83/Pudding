package com.enliple.pudding.fragment.main

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enliple.pudding.R
import com.enliple.pudding.VideoBaseFragment
import com.enliple.pudding.adapter.home.MainListAdapter
import com.enliple.pudding.commons.db.DBManager
import com.enliple.pudding.commons.log.Logger
import com.enliple.pudding.commons.network.NetworkApi
import com.enliple.pudding.commons.network.NetworkBusResponse
import com.enliple.pudding.commons.network.NetworkHandler
import com.enliple.pudding.commons.network.vo.VOD
import com.enliple.pudding.widget.flippablestackview.StackPageTransformer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_hot.*

/**
 * Main Live category
 */
class MainLiveFragment : VideoBaseFragment() {
    companion object {
        private const val WHAT_START = 1000
    }

    private lateinit var mAdapter: MainListAdapter

    private var mPrevPagerIndex = 0  // pager 위치를 저장

    var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            var position = msg.arg1

            if (msg.what == WHAT_START) {
                Logger.e("what_start : playVideo")
                getFragment(position)?.playVideo()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hot, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (outState != null) {
            Logger.d("onSaveInstanceState")

            outState.putInt("index", mPrevPagerIndex)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            Logger.e("onViewStateRestored index: ${savedInstanceState.getInt("index")}")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var savedIndex = 0
        if (savedInstanceState != null) {
            savedIndex = savedInstanceState.getInt("index", 0)
        }
        Logger.e("onActivityCreated prevIndex: $savedIndex")

        mAdapter = MainListAdapter(activity!!, childFragmentManager!!, 2)

        flippableViewPager?.initStack(2, StackPageTransformer.Orientation.VERTICAL, 0.95f, 0.8f, 1.0f, StackPageTransformer.Gravity.CENTER)
        flippableViewPager?.adapter = mAdapter
        flippableViewPager?.setOnPageChangeListener(listPageSelectedListener)
        //flippableViewPager?.height = 1556

        if (savedIndex > 0) {
            //TODO data 를 다시 얻어와서 갱신 한 후 위치로 이동 해야 한다.
            flippableViewPager.setCurrentItem(savedIndex, true) // 반드시 smoothScroll 을 사용해야 한다.
        }

        swipe_layout.setOnRefreshListener(this)

//        var bus = NetworkBus(NetworkApi.VOD0.name, "live", "1")
//        EventBus.getDefault().post(bus)
    }

    override fun handleNetworkResult(data: NetworkBusResponse) {
        var key = NetworkHandler.getInstance(context!!).getKey(NetworkApi.VOD0.toString(), "live", "1")
        if (data.arg1.startsWith(key)) {
            if ("ok" == data.arg2) {
                var result: VOD = Gson().fromJson(DBManager.getInstance(context).get(data.arg1), VOD::class.java)
                Logger.e("result totalCount:" + result.nTotalCount)

                mAdapter.count = result.nTotalCount
            } else {
                // error 처리
            }
        }

        swipe_layout.isRefreshing = false
    }

    private fun getFragment(position: Int): MainVideoFragment? {
        if (mAdapter != null && flippableViewPager != null) {
            Logger.e("getCurrentFragment:" + flippableViewPager.currentItem)
            return mAdapter.getFragment(position) as MainVideoFragment?
        }

        return null
    }

    override fun playVideo() {
        Logger.i("playVideo")

        if (flippableViewPager != null) {
            getFragment(flippableViewPager.currentItem)?.playVideo()
        }
    }

    override fun pauseVideo() {
        Logger.i("pauseVideo")

        if (flippableViewPager != null) {
            getFragment(flippableViewPager.currentItem)?.pauseVideo()
        }
    }

    override fun stopVideo() {
        Logger.i("stopVideo")

        if (flippableViewPager != null) {
            getFragment(flippableViewPager.currentItem)?.releaseVideo()
        }
    }

    private val listPageSelectedListener = object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            Logger.d("onPageSelected ==> $position")

            if (position != mPrevPagerIndex) {
                var f = mAdapter.getFragment(mPrevPagerIndex)
                if (f is MainVideoFragment) {
                    f.pauseVideo()

                    mHandler.removeMessages(WHAT_START)
                }

                mPrevPagerIndex = position
            }

            var msg = Message()
            msg.what = WHAT_START
            msg.arg1 = position

            var f = mAdapter.getFragment(position)
            if (f == null) {
                // fragment 가 만들어 지는 중..
                mHandler.sendMessageDelayed(msg, 800)
            } else if (f is MainVideoFragment) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mHandler.sendMessageDelayed(msg, 50)
                } else {
                    mHandler.sendMessageDelayed(msg, 300)
                }
            }

            swipe_layout.isEnabled = false
            if (mPrevPagerIndex == 0) {
                swipe_layout.isEnabled = true
            }
        }
    }
}