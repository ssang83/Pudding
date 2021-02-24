package com.enliple.pudding.commons.widget.recyclerview;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import com.enliple.pudding.commons.log.Logger;

/**
 * Created by hkcha on 2017-11-01.
 * <p>
 * SupportLibrary RecyclerView 에서 발생되는 내부 로직 Error에 대응 하는 GridLayoutManager
 * (https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in)
 */
public class WrappedStaggeredGridLayoutManager extends StaggeredGridLayoutManager {

    public WrappedStaggeredGridLayoutManager(int spanCount, int orientation) {
        super(spanCount, orientation);
    }

    public WrappedStaggeredGridLayoutManager(Context context, AttributeSet attrs, int defstyleAttr, int defStyleRes) {
        super(context, attrs, defstyleAttr, defStyleRes);
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
