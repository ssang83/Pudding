package com.enliple.pudding.commons.widget

import android.content.Context
import android.util.AttributeSet

import androidx.recyclerview.widget.RecyclerView

import com.enliple.pudding.commons.ui_compat.PixelUtil

class MaxHeightRecyclerView : RecyclerView {

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        heightMeasureSpec = PixelUtil.dpToPx(getContext(), 185)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
