package com.enliple.pudding.commons.widget.recyclerview;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;

import com.enliple.pudding.commons.log.Logger;


/**
 * Created by hkcha on 2017-11-01.
 * <p>
 * SupportLibrary RecyclerView 에서 발생되는 내부 로직 Error에 대응 하는 GridLayoutManager
 * (https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in)
 */
public class WrappedGridLayoutManager extends GridLayoutManager {
    public WrappedGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public WrappedGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public WrappedGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Logger.p(e);
        }
    }
}
