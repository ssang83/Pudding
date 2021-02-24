package com.enliple.pudding.commons.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.enliple.pudding.commons.R;
import com.enliple.pudding.commons.app.Utils;

public class MaxHeightLinearLayout extends LinearLayout {

    private int maxHeightDp;
    private Context context;
    public MaxHeightLinearLayout(Context context) {
        super(context);
        this.context = context;
    }

    public MaxHeightLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MaxHeightLinearLayout, 0, 0);
        try {
            maxHeightDp = a.getInteger(R.styleable.MaxHeightLinearLayout_maxHeightDp, 0);
        } finally {
            a.recycle();
        }
    }

    public MaxHeightLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeightPx = Utils.ConvertDpToPx(context, maxHeightDp);
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxHeightPx, View.MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setMaxHeightDp(int maxHeightDp) {
        this.maxHeightDp = maxHeightDp;
        invalidate();
    }

}