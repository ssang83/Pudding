package com.enliple.pudding.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.ProgressBar

/**
 * 화면단위 클릭을 방지하는 투명 Progress 팝업
 * @author hkcha
 * @since 2018.08.17
 */
class ProgressLoading : LinearLayout {
    companion object {
        private const val TAG = "ProgressLoading"
    }

    private var progress: ProgressBar

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleRes: Int) : super(context, attrs, defStyleRes)

    init {
        gravity = Gravity.CENTER

        progress = ProgressBar(context)
        progress.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        progress.isIndeterminate = true

        addView(progress)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }
}