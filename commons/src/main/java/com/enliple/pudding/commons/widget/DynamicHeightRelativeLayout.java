package com.enliple.pudding.commons.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.enliple.pudding.commons.R;

public class DynamicHeightRelativeLayout extends RelativeLayout {

    private float whRatio = 1.0f;

    public DynamicHeightRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setRatio(context.obtainStyledAttributes(attrs, R.styleable.DynamicHeightRelativeLayout)
                .getFloat(R.styleable.DynamicHeightRelativeLayout_relativeHeightRatio, whRatio));
    }

    public DynamicHeightRelativeLayout(Context context) {
        super(context);
    }

    public DynamicHeightRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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