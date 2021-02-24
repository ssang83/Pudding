package com.enliple.pudding.commons.widget.coordinator;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.enliple.pudding.commons.log.Logger;

import java.lang.ref.WeakReference;

public final class FlingBehavior extends AppBarLayout.Behavior {

    private static final String TAG = "FlingBehavior";

    // The minimum I have seen for a dy, after the recycler view stopped.
//    private static final int MINIMUM_DELTA_Y = -4;
    private static final int MINIMUM_DELTA_Y = -10;

    boolean mEnabled;

    @Nullable
    RecyclerViewScrollListener mScrollListener;

    private boolean isPositive;

    public FlingBehavior() {
        mEnabled = true;
        setDragCallbacks();
    }

    public FlingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        mEnabled = true;
        setDragCallbacks();
    }

    public boolean callSuperOnNestedFling(
            CoordinatorLayout coordinatorLayout,
            AppBarLayout child,
            View target,
            float velocityX,
            float velocityY,
            boolean consumed) {
        Logger.d(TAG, "callSuperOnNestedFling : " + mEnabled);

        return mEnabled && super.onNestedFling(
                coordinatorLayout,
                child,
                target,
                velocityX,
                velocityY,
                consumed
        );
    }

    @Override
    public boolean onNestedFling(
            CoordinatorLayout coordinatorLayout,
            AppBarLayout child,
            View target,
            float velocityX,
            float velocityY,
            boolean consumed) {

        if (velocityY > 0 && !isPositive || velocityY < 0 && isPositive) {
            velocityY = velocityY * -1;
        }

        if (target instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) target;

            if (mScrollListener == null) {
                mScrollListener = new RecyclerViewScrollListener(
                        coordinatorLayout,
                        child,
                        this
                );
                recyclerView.addOnScrollListener(mScrollListener);
            }

            mScrollListener.setVelocity(velocityY);
        }

        Logger.d(TAG, "onNestedFling : " + mEnabled);

        return mEnabled && super.onNestedFling(
                coordinatorLayout,
                child,
                target,
                velocityX,
                velocityY,
                consumed
        );
    }

    @Override
    public void onNestedPreScroll(
            CoordinatorLayout coordinatorLayout,
            AppBarLayout child,
            View target,
            int dx,
            int dy,
            int[] consumed) {
        Logger.d(TAG, "onNestedPreScroll");

        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        isPositive = dy > 0;
    }

    public void setEnabled(boolean enabled) {
        Logger.d(TAG, "Fling Behavoir enabled : " + enabled);
        this.mEnabled = enabled;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes) {
        Logger.d(TAG, "onStartNestedScroll : " + mEnabled);
        return mEnabled && super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(parent, child, ev);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        try {
            return super.onTouchEvent(parent, child, ev);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public void setDragCallbacks() {
        setDragCallback(new DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return mEnabled;
            }
        });
    }

    private static class RecyclerViewScrollListener extends RecyclerView.OnScrollListener {
        @NonNull
        private final WeakReference<AppBarLayout> mAppBarLayoutWeakReference;

        @NonNull
        private final WeakReference<FlingBehavior> mBehaviorWeakReference;

        @NonNull
        private final WeakReference<CoordinatorLayout> mCoordinatorLayoutWeakReference;

        private int mDy;

        private float mVelocity;

        public RecyclerViewScrollListener(
                @NonNull CoordinatorLayout coordinatorLayout,
                @NonNull AppBarLayout child,
                @NonNull FlingBehavior barBehavior) {
            mCoordinatorLayoutWeakReference = new WeakReference<>(coordinatorLayout);
            mAppBarLayoutWeakReference = new WeakReference<>(child);
            mBehaviorWeakReference = new WeakReference<>(barBehavior);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (mDy < MINIMUM_DELTA_Y
                        && mAppBarLayoutWeakReference.get() != null
                        && mCoordinatorLayoutWeakReference.get() != null
                        && mBehaviorWeakReference.get() != null) {

                    // manually trigger the fling when it's scrolled at the top
                    mBehaviorWeakReference.get()
                            .callSuperOnNestedFling(
                                    mCoordinatorLayoutWeakReference.get(),
                                    mAppBarLayoutWeakReference.get(),
                                    recyclerView,
                                    0,
                                    mVelocity, //find a way to recalculate a correct velocity.
                                    false
                            );
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            mDy = dy;
        }

        public void setVelocity(float velocity) {
            mVelocity = velocity;
        }
    }
}