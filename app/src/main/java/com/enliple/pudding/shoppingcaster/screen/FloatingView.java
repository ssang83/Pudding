package com.enliple.pudding.shoppingcaster.screen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * 비디오 Floating View
 */

public class FloatingView extends LinearLayout {
    private float mTouchStartX;
    private float mTouchStartY;
    private float mLastX;
    private float mLastY;
    private float mMaxX = 0;
    private float mMaxY = 0;

    private WindowManager mWindowManager;

    private WindowManager.LayoutParams mLayoutParams;

    public FloatingView(Context context) {
        super(context);
        init();
    }

    public FloatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mWindowManager = (WindowManager) getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public void setParams(WindowManager.LayoutParams params) {
        mLayoutParams = params;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mLastX = event.getRawX();
        mLastY = event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                updateViewPosition();
                break;

            case MotionEvent.ACTION_UP:
                mTouchStartX = 0;
                mTouchStartY = 0;
                break;
        }
        return true;
    }

    private void updateViewPosition() {
        if (mLayoutParams != null) {
            mMaxX = mWindowManager.getDefaultDisplay().getWidth() - this.getWidth();
            mMaxY = mWindowManager.getDefaultDisplay().getHeight() - this.getHeight();

            float newX = (mLastX - mTouchStartX);
            float newY = (mLastY - mTouchStartY);
            mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;

            if (newX < 0) {
                newX = 0;
            }

            if (newY < 0) {
                newY = 0;
            }

            if (newX > mMaxX) {
                newX = mMaxX;
            }

            if (newY > mMaxY) {
                newY = mMaxY;
            }
            mLayoutParams.x = (int) newX;
            mLayoutParams.y = (int) newY;
            mWindowManager.updateViewLayout(this, mLayoutParams);
        }
    }
}