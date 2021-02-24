package com.enliple.pudding.commons.widget

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import com.enliple.pudding.commons.R

class DynamicHeightConstraintLayout : ConstraintLayout {

    private var whRatio = 1.0f

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setRatio(context.obtainStyledAttributes(attrs, R.styleable.DynamicHeightConstraintLayout)
                .getFloat(R.styleable.DynamicHeightConstraintLayout_constraintHeightRatio, whRatio))
    }

    fun setRatio(ratio: Float) {
        whRatio = ratio.toFloat()
        requestLayout()
    }

    fun getRatio(): Float = whRatio

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val measuredWidth = measuredWidth
        setMeasuredDimension(measuredWidth, (measuredWidth / whRatio).toInt())

    }
}