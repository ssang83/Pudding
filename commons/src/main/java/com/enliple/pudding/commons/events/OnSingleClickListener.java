package com.enliple.pudding.commons.events;

import android.os.SystemClock;
import android.view.View;

/**
 * Created by hkcha on 2017-12-04.
 * 중복 연타 클릭 방지용 OnClickListener
 */
public abstract class OnSingleClickListener implements View.OnClickListener {

    private static final long MIN_INTERVAL = 600;
    private long mLastClickTime = 0;

    public abstract void onSingleClick(View v);

    @Override
    public void onClick(View view) {
        long currentClickTime = SystemClock.uptimeMillis();
        long elapsedTime = currentClickTime - mLastClickTime;
        mLastClickTime = currentClickTime;

        if (elapsedTime <= MIN_INTERVAL) {
            return;
        }

        onSingleClick(view);
    }
}
