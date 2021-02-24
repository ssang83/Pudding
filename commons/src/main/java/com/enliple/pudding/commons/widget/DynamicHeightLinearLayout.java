package com.enliple.pudding.commons.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.enliple.pudding.commons.R;

public class DynamicHeightLinearLayout extends LinearLayout {

    private float whRatio = 1.0f;

    public DynamicHeightLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setRatio(context.obtainStyledAttributes(attrs, R.styleable.DynamicHeightLinearLayout)
                .getFloat(R.styleable.DynamicHeightLinearLayout_linearHeightRatio, whRatio));
    }

    public DynamicHeightLinearLayout(Context context) {
        super(context);
    }

    public void setRatio(float ratio) {
        whRatio = ratio;
        requestLayout();
    }

    public double getRatio() {
        return whRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int) (measuredWidth / whRatio));
    }
}