package com.enliple.pudding.commons.shoppingcommons.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.widget.FrameLayout;

import com.enliple.pudding.commons.R;
import com.enliple.pudding.commons.log.Logger;

import java.util.Random;

import androidx.appcompat.widget.AppCompatImageView;

public class HeartViewExt extends AppCompatImageView {
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
            this.startY = height - mBitmap.getHeight() + 5;
            this.endX = width;
            this.endY = 0;

            Logger.e("startX:" + startX + " startY:" + startY + " endX:" + endX);

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

    private int mParentWidth;
    private int mParentHeight;

    /**
     * 좋아요 이미지들
     */
    private static final int[] HEART_RESOURCES = {
            R.drawable.candy_1, R.drawable.candy_2, R.drawable.candy_3,
            R.drawable.candy_4, R.drawable.grape, R.drawable.jelly_1,
            R.drawable.jelly_2, R.drawable.jelly_3, R.drawable.jelly_4,
            R.drawable.lemon, R.drawable.peach, R.drawable.strawberry
    };

    public HeartViewExt(Context context, FrameLayout parent) {
        super(context);

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
            mSpeed = 0.007f; // 너무 빠른 경우는 좀 느리게..
        } else if (mSpeed < 0.006f) {
            mSpeed = 0.006f; // 너무 느린 경우는 좀 빠르게..
        }
        Logger.e("mSpeed:" + mSpeed);

        //mMatrix = new Matrix();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        mParentWidth = parent.getWidth();
        mParentHeight = parent.getHeight();
        Logger.d("mParentHeight:" + mParentHeight);

        mMatrix = new Matrix();

        mData = new Data();
        mData.init(mParentWidth, mParentHeight);

        mBmOffsetX = mBitmap.getWidth() / 2;
        mBmOffsetY = mBitmap.getHeight() / 2;

        path = new Path();
        path.moveTo(mData.startX + 100, mData.startY + 70);
        //path.cubicTo(0, mData.startY - 250, mData.endX + 300, mData.startY - 500, mData.startX, mData.startY - 800);

        path.cubicTo(50, mData.startY - 50, mData.endX + 200, mData.startY - 150, mData.startX, mData.startY - 250);
        path.cubicTo(50, mData.startY - 350, mData.endX + 300, mData.startY - 550, mData.startX, mData.startY - 800);

        //path.cubicTo(0, mData.startY - 150, mData.endX + 300, mData.startY - 300, mData.startX, mData.startY - 500);

        mPathMeasure = new PathMeasure(path, false);
        mPathLength = mPathMeasure.getLength();

        this.setScaleType(ScaleType.MATRIX);

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
            y = mParentHeight - mBitmap.getHeight() + 5;
        }
        data.y = y;
        data.x = x;

        invalidate();
    }

    private float mScale = 1.0f;

    private Path path;
    private PathMeasure mPathMeasure;
    private float mPathLength;
    private float mStep = 7.0f;   //mDistance each mStep
    private float mDistance = 0;  //mDistance moved

    private float[] mPos = new float[2];
    private float[] mTan = new float[2];
    private int mBmOffsetX, mBmOffsetY;

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap == null) {
            return;
        }

        mPathMeasure.getPosTan(mDistance, mPos, mTan);

        mMatrix.reset();
        mMatrix.postTranslate(mPos[0] - mBmOffsetX, mPos[1] - mBmOffsetY);

        mScale = 1.0f - (mData.amount / 2);
        if (mScale < 0.3f) {
            mScale = 0.3f;
        }
        mMatrix.postScale(mScale, mScale, mPos[0] - mBmOffsetX, mPos[1] - mBmOffsetY);

        int alpha = 0;
        if (mDistance < 250) {
            // 처음에는 0에서 점점 밝아지는 효과.
            alpha = (int) (mDistance / 3);
        } else if (mDistance > 250 && mDistance < 500) {
            alpha = 255;
        } else {
            // 일정 위치 부터는 점점 사라지는 효과
            //alpha = (int) (255 * Math.abs(1.0f - mData.amount));
            alpha = 255 - (int) (mDistance / 8);
            if (alpha < 20) {
                alpha = 20;
            }
        }
        mPaint.setAlpha(alpha);
        Logger.e("alpha:" + alpha);

        canvas.drawBitmap(mBitmap, mMatrix, mPaint);

        mDistance += mStep;

        mData.amount += mSpeed;
        Logger.d("amount:" + mData.amount + " mDistance:" + mDistance);

//        mPaint = new Paint();
//        mPaint.setColor(Color.WHITE);
//        mPaint.setStrokeWidth(1);
//        mPaint.setStyle(Paint.Style.STROKE);
//        canvas.drawPath(path, mPaint);

        if (mDistance < mPathLength) {
            invalidate();
        } else {
            if (mBitmap != null) {
                mBitmap.eraseColor(Color.TRANSPARENT);
                canvas.drawBitmap(mBitmap, 0, 0, mPaint);
                mBitmap.recycle();
                mBitmap = null;
                Logger.d("recycle");
            }
        }
    }
}
