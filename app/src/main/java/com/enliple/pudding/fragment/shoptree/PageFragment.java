package com.enliple.pudding.fragment.shoptree;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.enliple.pudding.R;
import com.enliple.pudding.commons.log.Logger;

public class PageFragment extends Fragment {

    private String path;
    private ScrollView scroll;
    AppCompatImageView longimage;
    RelativeLayout root, longLayer;
    private int rootWidth, rootHeight, shortRootHeight;
    boolean isOpen = false;

    public static PageFragment create(String path, int rootWidth, int rootHeight, int shortRootHeight, boolean open) {
        PageFragment fragment = new PageFragment();
        Bundle args = new Bundle();
        args.putString("path", path);
        args.putInt("width", rootWidth);
        args.putInt("height", rootHeight);
        args.putInt("s_height", shortRootHeight);
        args.putBoolean("isOpen", open);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        path = getArguments().getString("path");
        rootWidth = getArguments().getInt("width");
        rootHeight = getArguments().getInt("height");
        shortRootHeight = getArguments().getInt("s_height");
        isOpen = getArguments().getBoolean("isOpen");
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_page, container, false);

        longimage = rootView.findViewById(R.id.longimage);
        root = rootView.findViewById(R.id.root);
        scroll = rootView.findViewById(R.id.scroll);
        longLayer = rootView.findViewById(R.id.longLayer);
        return rootView;
    }

    private void registerReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(openReceiver, new IntentFilter("is_exp_opened"));
    }

    private void unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(openReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstancestate) {
        super.onViewCreated(view, savedInstancestate);
        Logger.e("onCreateView path :: " + path);
//        Glide.with(getActivity())
//                .load(path)
//                .asBitmap()
//                .priority(Priority.HIGH)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                        float ratio = (float) rootWidth / (float) resource.getWidth();
//                        int imageHeight = (int) (resource.getHeight() * ratio);
//                        Logger.e("ratio :: " + ratio + " imageHeight :: " + imageHeight + " isOpen :: " + isOpen);
//                    }
//
//                    @Override
//                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                        super.onLoadFailed(e, errorDrawable);
//                    }
//                });

    }

    private BroadcastReceiver openReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("is_exp_opened")) {
                boolean isOpen = intent.getBooleanExtra("isOpened", false);
                Logger.e("openReceiver received :: " + isOpen);
                Glide.with(getActivity())
                        .asBitmap()
                        .load(path)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                float ratio = (float) rootWidth / (float) resource.getWidth();
                                int imageHeight = (int) (resource.getHeight() * ratio);
                                if (imageHeight <= rootHeight) {
                                    ViewGroup.LayoutParams para = longLayer.getLayoutParams();
                                    para.width = rootWidth;
                                    para.height = rootHeight;
                                    longLayer.setLayoutParams(para);
                                }

                                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(rootWidth, imageHeight);
                                param.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                                longimage.setLayoutParams(param);
                                longimage.setImageBitmap(resource);
                                longimage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            }

//                            @Override
//                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                                float ratio = (float) rootWidth / (float) resource.getWidth();
//                                int imageHeight = (int) (resource.getHeight() * ratio);
//                                if (imageHeight <= rootHeight) {
//                                    ViewGroup.LayoutParams para = longLayer.getLayoutParams();
//                                    para.width = rootWidth;
//                                    para.height = rootHeight;
//                                    longLayer.setParams(para);
//                                }
//
//                                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(rootWidth, imageHeight);
//                                param.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//                                longimage.setParams(param);
//                                longimage.setImageBitmap(resource);
//                                longimage.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                            }
//
//                            @Override
//                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
//                                super.onLoadFailed(e, errorDrawable);
//                                Logger.d("image loadFail :: ");
//                            }
                        });
//

            }
        }
    };
}