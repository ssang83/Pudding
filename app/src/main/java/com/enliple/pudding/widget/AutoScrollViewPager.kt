package com.enliple.pudding.widget

import android.content.Context
import android.os.Handler
import android.os.Message
import androidx.core.view.MotionEventCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Interpolator
import com.enliple.pudding.commons.log.Logger
import java.lang.ref.WeakReference
import java.lang.reflect.Field

/**
 * Created by Kim Joonsung on 2018-10-22.
 */
class AutoScrollViewPager : androidx.viewpager.widget.ViewPager {
    companion object {
        private val DEFAULT_INTERVAL = 1500L

        private val LEFT = 0
        private val RIGHT = 1

        private val SLIDE_BORDER_MODE_NONE = 0
        private val SLIDE_BORDER_MODE_CYCLE = 1
        private val SLIDE_BORDER_MODE_TO_PARENT = 2

        private val SCROLL_WHAT = 0
    }

    private var interval: Long = DEFAULT_INTERVAL
    private var direction: Int = RIGHT
    private var isCycle: Boolean = true
    private var stopScrollWhenTouch: Boolean = true
    private var slideBorderMode: Int = SLIDE_BORDER_MODE_NONE
    private var isBorderAnimation: Boolean = true
    private var autoScrollFactor: Double = 1.0
    private var swipeScrollFactor: Double = 1.0

    private var handler: Handler? = null
    private var isAutoScroll: Boolean = false
    private var isStopByTouch: Boolean = false
    private var touchX: Float = 0f
    private var downX: Float = 0f
    private var scroller: CustomDurationScroller? = null


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun getHandler(): Handler {
        return super.getHandler()
    }

    init {
        handler = MyHandler(this)
        setViewPagerScroller()
    }

    fun startAutoScroll() {
        isAutoScroll = true
        sendScrollMessage((interval + scroller!!.duration / autoScrollFactor * swipeScrollFactor).toLong())

    }

    fun stopAutoScroll() {
        isAutoScroll = false
        handler?.removeMessages(SCROLL_WHAT)
    }

    fun setSwipeScrollDurationFactor(scrollFactor: Double) {
        swipeScrollFactor = scrollFactor
    }

    fun setAutoScrollDurationFactor(scrollFactor: Double) {
        autoScrollFactor = scrollFactor
    }

    private fun sendScrollMessage(delayTimeMills: Long) {
        /** remove messages before, keeps one message is running at most **/
        handler?.removeMessages(SCROLL_WHAT)
        handler?.sendEmptyMessageDelayed(SCROLL_WHAT, delayTimeMills)
        Logger.d("delayTimeMills : " + delayTimeMills);
    }

    private fun setViewPagerScroller() {
        var scrollFied: Field = androidx.viewpager.widget.ViewPager::class.java.getDeclaredField("mScroller")
        var interpolatorField: Field = androidx.viewpager.widget.ViewPager::class.java.getDeclaredField("sInterpolator")

        scrollFied.isAccessible = true
        interpolatorField.isAccessible = true

        scroller = CustomDurationScroller(context, interpolatorField.get(null) as Interpolator)
    }

    fun scrollOnce() {
        var adapter: androidx.viewpager.widget.PagerAdapter? = getAdapter()
        var currentItem: Int = getCurrentItem()
        var totalCount: Int = adapter!!.count
        if (totalCount <= 1) {
            return
        }

        val nextItem: Int = if (direction == LEFT) --currentItem else ++currentItem
        if (nextItem < 0) {
            if (isCycle) {
                setCurrentItem(totalCount - 1, isBorderAnimation)
            }
        } else if (nextItem == totalCount) {
            if (isCycle) {
                setCurrentItem(0, isBorderAnimation)
            }
        } else {
            setCurrentItem(nextItem, true)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val action: Int = MotionEventCompat.getActionMasked(ev)

        if (stopScrollWhenTouch) {
            if ((action == MotionEvent.ACTION_DOWN) && isAutoScroll) {
                isStopByTouch = true
                stopAutoScroll()
            } else if (ev!!.action == MotionEvent.ACTION_UP && isStopByTouch) {
                startAutoScroll()
            }
        }

        if (slideBorderMode == SLIDE_BORDER_MODE_TO_PARENT ||
                slideBorderMode == SLIDE_BORDER_MODE_CYCLE) {
            touchX = ev!!.x
            if (ev.action == MotionEvent.ACTION_DOWN) {
                downX = touchX
            }

            val currentItem: Int = getCurrentItem()
            val adapter: androidx.viewpager.widget.PagerAdapter? = adapter
            val pageCount: Int = adapter!!.count

            if ((currentItem == 0 && downX <= touchX) ||
                    (currentItem == pageCount - 1 && downX >= touchX)) {
                if (slideBorderMode == SLIDE_BORDER_MODE_TO_PARENT) {
                    parent.requestDisallowInterceptTouchEvent(false)
                } else {
                    if (pageCount > 1) {
                        setCurrentItem(pageCount - currentItem - 1, isBorderAnimation)
                    }
                    parent.requestDisallowInterceptTouchEvent(true)
                }

                return super.dispatchTouchEvent(ev)
            }
        }

        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }

    inner class MyHandler : Handler {
        val autoScrollVierPager: WeakReference<AutoScrollViewPager>

        constructor(autoScrollVierPager: AutoScrollViewPager) : super() {
            this.autoScrollVierPager = WeakReference<AutoScrollViewPager>(autoScrollVierPager)
        }

        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)

            Logger.d("message : " + msg!!.what)
            when (msg!!.what) {
                SCROLL_WHAT -> {
                    val pager: AutoScrollViewPager? = this.autoScrollVierPager.get()
                    pager!!.scroller!!.setScrollDurationFactor(pager.autoScrollFactor)
                    pager.scrollOnce()
                    pager.scroller!!.setScrollDurationFactor(pager.swipeScrollFactor)
                    pager.sendScrollMessage(pager.interval + pager.scroller!!.duration)
                }
            }
        }
    }

    fun getInterval(): Long {
        return interval
    }

    fun setInterval(interval: Long) {
        this.interval = interval
    }

    fun getDirection(): Int = if (direction == LEFT) LEFT else RIGHT

    fun setDDirection(direction: Int) {
        this.direction = direction
    }

    fun isCycle(): Boolean {
        return isCycle
    }

    fun setCycle(isCyle: Boolean) {
        this.isCycle = isCycle
    }

    fun isStopScrollWhenTouch(): Boolean {
        return stopScrollWhenTouch
    }

    fun setStopScrollWhenTouch(stopScrollWhenTouch: Boolean) {
        this.stopScrollWhenTouch = stopScrollWhenTouch
    }

    fun getSlideBorderMode(): Int {
        return slideBorderMode
    }

    fun setSlideBorderMode(slideBorderMode: Int) {
        this.slideBorderMode = slideBorderMode
    }

    fun isBorederAnimation(): Boolean {
        return isBorderAnimation
    }

    fun setBorderAnimation(isBorderAnimation: Boolean) {
        this.isBorderAnimation = isBorderAnimation
    }
}