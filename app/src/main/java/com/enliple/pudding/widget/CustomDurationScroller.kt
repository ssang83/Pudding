package com.enliple.pudding.widget

import android.content.Context
import android.view.animation.Interpolator
import android.widget.Scroller

/**
 * Created by Kim Joonsung on 2018-10-22.
 */
class CustomDurationScroller : Scroller {

    private var scrollFactor: Double = 1.0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, interpolator: Interpolator?) : super(context, interpolator)

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, (duration * scrollFactor) as Int)
    }

    fun setScrollDurationFactor(scrollFactor: Double) {
        this.scrollFactor = scrollFactor
    }
}