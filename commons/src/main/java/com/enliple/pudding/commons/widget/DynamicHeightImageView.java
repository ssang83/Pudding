package com.enliple.pudding.commons.widget;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.enliple.pudding.commons.R;

public class DynamicHeightImageView extends AppCompatImageView {

    private float whRatio = 1.0f;

    public DynamicHeightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setRatio(context.obtainStyledAttributes(attrs, R.styleable.DynamicHeightImageView)
                .getFloat(R.styleable.DynamicHeightImageView_heightRatio, whRatio));
    }

    public DynamicHeightImageView(Context context) {
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