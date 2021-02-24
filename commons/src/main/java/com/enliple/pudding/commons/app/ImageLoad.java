package com.enliple.pudding.commons.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

public class ImageLoad {
    public static final int SCALE_NONE = -1;
    public static final int SCALE_FIT_CENTER = 0;
    public static final int SCALE_CENTER_CROP = 1;
    public static final int SCALE_CIRCLE_CROP = 2;
    public static final int SCALE_CENTER_INSIDE = 3;

    public static void setImage(Context context, View view, Object path, Object placeHolder, int scaleType, DiskCacheStrategy strategy) {
        if (view == null || path == null || context == null) {
            return;
        }

        ImageView iv = (ImageView) view;

        RequestOptions options = new RequestOptions();
        if (scaleType == SCALE_FIT_CENTER)
            options.fitCenter();
        else if (scaleType == SCALE_CENTER_CROP)
            options.centerCrop();
        else if (scaleType == SCALE_CENTER_INSIDE)
            options.centerInside();
        else if (scaleType == SCALE_CIRCLE_CROP)
            options.circleCrop();

        if (placeHolder instanceof Integer) {
            int iPlaceHolder = (int) placeHolder;
            options.placeholder(iPlaceHolder);
        } else if (placeHolder instanceof Drawable) {
            Drawable dr = (Drawable) placeHolder;
            options.placeholder(dr);
        }

        if (strategy != null) {
            options.diskCacheStrategy(strategy);
            if ( strategy == DiskCacheStrategy.NONE ) {
                options.skipMemoryCache(true);
            }

        }
        options.format(DecodeFormat.PREFER_ARGB_8888);
        options.priority(Priority.HIGH);
//        options.override(Target.SIZE_ORIGINAL);

        Glide.with(context).setDefaultRequestOptions(options).load(path).thumbnail(0.1f).into(iv);
    }

    public static void setImage1(Context context, View view, Object path, Object placeHolder, int scaleType, DiskCacheStrategy strategy) {
        if (view == null || path == null || context == null) {
            return;
        }

        ImageView iv = (ImageView) view;

        RequestOptions options = new RequestOptions();
        if (scaleType == SCALE_FIT_CENTER)
            options.fitCenter();
        else if (scaleType == SCALE_CENTER_CROP)
            options.centerCrop();
        else if (scaleType == SCALE_CENTER_INSIDE)
            options.centerInside();
        else if (scaleType == SCALE_CIRCLE_CROP)
            options.circleCrop();

        if (placeHolder instanceof Integer) {
            int iPlaceHolder = (int) placeHolder;
            options.placeholder(iPlaceHolder);
        } else if (placeHolder instanceof Drawable) {
            Drawable dr = (Drawable) placeHolder;
            options.placeholder(dr);
        }

        Glide.with(context).setDefaultRequestOptions(options).load(path).into(iv);
    }

    public static void setImage(Context context, View view, String path, String signature) {
        if (view == null || path == null || context == null) {
            return;
        }

        ImageView iv = (ImageView) view;
        ObjectKey key = new ObjectKey(signature);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        requestOptions.signature(key);

        Glide.with(context).setDefaultRequestOptions(requestOptions).load(path).into(iv);
    }
}
