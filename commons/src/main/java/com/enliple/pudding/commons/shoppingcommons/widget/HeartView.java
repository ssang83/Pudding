package com.enliple.pudding.commons.shoppingcommons.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;

import com.enliple.pudding.commons.R;
import com.enliple.pudding.commons.log.Logger;

import java.util.Random;

public class HeartView extends View {
    class Data {
        public float x;
        public float y;
        public float startX;
        public float startY;
        public float endX;
        public float endY;
        public float control1X;
        public float control1Y;
        public float control2X;
        public float control2Y;
        public float amount = 0;
        public int type = 0;

        public void init(float width, float height) {
            this.x = width;
            this.y = height;

            this.startX = width / 2;
            this.startY = height - mBitmap.getHeight() + 15;
            this.endX = width / 2;
            this.endY = 0;

            this.control1X = (float) (Math.random() * width);
            this.control1Y = (float) (Math.random() * height);
            this.control2X = (float) (Math.random() * width);
            this.control2Y = (float) (Math.random() * height);
        }
    }

    private Paint mPaint;
    private Bitmap mBitmap;

    private Matrix mMatrix;

    private float mSpeed = 0;

    private Data mData;

    private int mParentHeight;

    FrameLayout mParent;
    View mView;

    /**
     * 좋아요 이미지들
     */
    private static final int[] HEART_RESOURCES = {
            R.drawable.candy_1, R.drawable.candy_2, R.drawable.candy_3,
            R.drawable.candy_4, R.drawable.grape, R.drawable.jelly_1,
            R.drawable.jelly_2, R.drawable.jelly_3, R.drawable.jelly_4,
            R.drawable.lemon, R.drawable.peach, R.drawable.strawberry
    };

    public HeartView(Context context, FrameLayout parent) {
        super(context);
        mParent = parent;
        mView = this;
        init(parent);
    }

//    public HeartViewExt(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public HeartViewExt(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }

    private void init(FrameLayout parent) {
        Random r = new Random();
        int resource = HEART_RESOURCES[r.nextInt(HEART_RESOURCES.length)];
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resource);
        mBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bitmap.recycle();

        mSpeed = (float) (Math.random() * 0.01);
        if (mSpeed > 0.009f) {
            mSpeed = 0.006f; // 너무 빠른 경우는 좀 느리게..
        } else if (mSpeed < 0.004f) {
            mSpeed = 0.006f; // 너무 느린 경우는 좀 빠르게..
        }

        mMatrix = new Matrix();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        mParentHeight = parent.getHeight();

        mData = new Data();
        mData.init(parent.getWidth(), mParentHeight);

        parent.addView(this);
        invalidate();
    }

    private void moveTo(Data data) {

        // 3차원 배열 곡선 함수
        float x = (float) (Math.pow((1 - data.amount), 3) * data.startX +
                3 * data.amount * Math.pow((1 - data.amount), 2) * data.control1X +
                3 * Math.pow(data.amount, 2) * (1 - data.amount) * data.control2X +
                Math.pow(data.amount, 3) * data.endX);

        float y = (float) (Math.pow((1 - data.amount), 3) * data.startY +
                3 * data.amount * Math.pow((1 - data.amount), 2) * data.control1Y +
                3 * Math.pow(data.amount, 2) * (1 - data.amount) * data.control2Y +
                Math.pow(data.amount, 3) * data.endY);

        if (y > mParentHeight - mBitmap.getHeight()) {
            //y = mParentHeight - mBitmap.getHeight();
        }

        data.x = x;
        data.y = y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap == null) {
            //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
            return;
        }

        mMatrix.setTranslate(0, 0);
        mMatrix.postTranslate(mData.x, mData.y);
        mPaint.setAlpha((int) (255 * Math.abs(1.0f - mData.amount)));
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);

        if (mData.amount < 1) {
            mData.amount = mData.amount + mSpeed;
            moveTo(mData);
            invalidate();
        } else {
            if (mBitmap != null) {
                mBitmap.eraseColor(Color.TRANSPARENT);
                canvas.drawBitmap(mBitmap, 0, 0, mPaint);
                mBitmap.recycle();
                mBitmap = null;
            }
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mParent.removeView(mView);
                }
            });
        }
    }
}