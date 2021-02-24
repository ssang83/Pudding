package com.enliple.pudding.widget;


import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.enliple.pudding.R;
import com.enliple.pudding.commons.app.ImageLoad;

public class LiveCookieSender extends FrameLayout implements View.OnClickListener {
    private boolean mIsVisible = false;

    public LiveCookieSender(Context context, String name, String photo) {
        super(context);

        init(context, name, photo);
    }

    private void init(Context context, String name, String photo) {
        View v = LayoutInflater.from(context).inflate(R.layout.view_cookie_sender, null, false);
        ((AppCompatTextView) v.findViewById(R.id.sender_name)).setText(name);
        v.findViewById(R.id.sender_image).setOnClickListener(this);
        if (!TextUtils.isEmpty(photo)) {
            ImageLoad.setImage(context, v.findViewById(R.id.sender_image), photo, null, ImageLoad.SCALE_CIRCLE_CROP, null);
            //ImageLoad.setImage(context, v.findViewById(R.id.sender_image), photo, photo);
        }

        addView(v);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sender_image) {
            if (!mIsVisible) {
                findViewById(R.id.sender_name).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.sender_name).setVisibility(View.GONE);
            }

            mIsVisible = !mIsVisible;
        }
    }
}