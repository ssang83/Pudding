package com.enliple.pudding.shoppingcaster.tools

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

/**
 * Created by Kim Joonsung on 2018-11-07.
 */
class FullScreenVideoView : VideoView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }
}