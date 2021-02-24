package com.enliple.pudding.commons.widget.recyclerview;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;

import com.enliple.pudding.commons.log.Logger;

/**
 * Created by hkcha on 2017-11-01.
 * <p>
 * SupportLibrary RecyclerView 에서 발생되는 내부 로직 Error에 대응 하는 LinearLayoutManager
 * (https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in)
 */
public class WrappedLinearLayoutManager extends LinearLayoutManager {
    public WrappedLinearLayoutManager(Context context) {
        super(context);
    }

    public WrappedLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public WrappedLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
