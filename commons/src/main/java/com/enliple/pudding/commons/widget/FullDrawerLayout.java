package com.enliple.pudding.commons.widget;

import android.content.Context;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.customview.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.enliple.pudding.commons.log.Logger;

import java.lang.reflect.Field;

public class FullDrawerLayout extends DrawerLayout {

    private static final int MIN_DRAWER_MARGIN = 0; // dp

    public FullDrawerLayout(Context context) {
        this(context, null);
    }

    public FullDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FullDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        try {
            Field f = DrawerLayout.class.getDeclaredField("mLeftDragger");
            f.setAccessible(true);
            ViewDragHelper vdh = (ViewDragHelper) f.get(this);
//
//            Field edgeSizeField = ViewDragHelper.class.getDeclaredField("mEdgeSize");
//            edgeSizeField.setAccessible(true);
//            int newEdgeSize = (int) edgeSizeField.get(vdh) * 5;
//            edgeSizeField.setInt(vdh, newEdgeSize);

            Field slop = ViewDragHelper.class.getDeclaredField("mTouchSlop");
            slop.setAccessible(true);
            int newSlopValue = (int) slop.get(vdh) * 5; // 왼쪽 scroll 민감도 조절 값
            slop.setInt(vdh, newSlopValue);

            Field rf = DrawerLayout.class.getDeclaredField("mRightDragger");
            rf.setAccessible(true);
            ViewDragHelper rvdh = (ViewDragHelper) rf.get(this);

//            Field rEdgeSizeField = ViewDragHelper.class.getDeclaredField("mEdgeSize");
//            rEdgeSizeField.setAccessible(true);
//            int newSize = (int) rEdgeSizeField.get(rvdh) * 5; // edge 범위 조절
//            rEdgeSizeField.setInt(rvdh, newSize);

            Field touchSlop = ViewDragHelper.class.getDeclaredField("mTouchSlop");
            touchSlop.setAccessible(true);
            int newSlop = (int) touchSlop.get(rvdh) * 20; // 오른쪽 scroll 민감도 조절 값
            touchSlop.setInt(rvdh, newSlop);
        } catch (Exception e) {
            Logger.p(e);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalArgumentException(
                    "DrawerLayout must be measured with MeasureSpec.EXACTLY.");
        }

        setMeasuredDimension(widthSize, heightSize);

        // Gravity value for each drawer we've seen. Only one of each permitted.
        int foundDrawers = 0;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (isContentView(child)) {
                // Content views get measured at exactly the layout's size.
                final int w = MeasureSpec.makeMeasureSpec(widthSize - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY);
                final int h = MeasureSpec.makeMeasureSpec(heightSize - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY);
                child.measure(w, h);
            } else if (isDrawerView(child)) {
                final int childGravity = getDrawerViewGravity(child) & Gravity.HORIZONTAL_GRAVITY_MASK;
                if ((foundDrawers & childGravity) != 0) {
                    throw new IllegalStateException("Child drawer has absolute gravity " +
                            gravityToString(childGravity) + " but this already has a " +
                            "drawer view along that edge");
                }

                final int width = getChildMeasureSpec(widthMeasureSpec, MIN_DRAWER_MARGIN + lp.leftMargin + lp.rightMargin, lp.width);
                final int height = getChildMeasureSpec(heightMeasureSpec, lp.topMargin + lp.bottomMargin, lp.height);
                child.measure(width, height);
            } else {
                throw new IllegalStateException("Child " + child + " at index " + i +
                        " does not have a valid layout_gravity - must be Gravity.LEFT, " +
                        "Gravity.RIGHT or Gravity.NO_GRAVITY");
            }
        }
    }

    boolean isContentView(View child) {
        return ((LayoutParams) child.getLayoutParams()).gravity == Gravity.NO_GRAVITY;
    }

    boolean isDrawerView(View child) {
        final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
        final int absGravity = Gravity.getAbsoluteGravity(gravity, child.getLayoutDirection());
        return (absGravity & (Gravity.LEFT | Gravity.RIGHT)) != 0;
    }

    int getDrawerViewGravity(View drawerView) {
        final int gravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
        return Gravity.getAbsoluteGravity(gravity, drawerView.getLayoutDirection());
    }

    static String gravityToString(int gravity) {
        if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            return "LEFT";
        }
        if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
            return "RIGHT";
        }
        return Integer.toHexString(gravity);
    }
}