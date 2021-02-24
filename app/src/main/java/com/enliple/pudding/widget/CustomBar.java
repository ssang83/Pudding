package com.enliple.pudding.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.Nullable;

import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.enliple.pudding.commons.app.Utils;

public class CustomBar extends View {
    private int percentage;
    private int defaultColor;
    private int fillColor;
    private Context context;

    public CustomBar(Context context) {
        super(context);
        this.context = context;
    }

    public CustomBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CustomBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public CustomBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getSuggestedMinimumHeight();
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(height, heightMeasureSpec));


    }

    @Override
    protected void onDraw(Canvas canvas) {
        float bottom = getHeight();
        float hcenter = getWidth() * 0.5f;
        Log.e("TAG", "width :: " + getWidth());
        Log.e("TAG", "height :: " + getHeight());
        int radius = Utils.ConvertDpToPx(context, 2);
        Paint paint = new Paint();
        paint.setColor(0xFFD9E1EB);
        RectF r = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(r, radius, radius, paint);

        int barWidth = ((getWidth() * percentage) / 100);

        paint.setColor(0xFF5774f4);
        r = new RectF(0, 0, barWidth, getHeight());
        canvas.drawRoundRect(r, radius, radius, paint);
    }

    public void setPercentage(int defaultColor, int fillColor, int percentage) {
        this.percentage = percentage;
        this.defaultColor = defaultColor;
        this.fillColor = fillColor;
        invalidate();
    }
}