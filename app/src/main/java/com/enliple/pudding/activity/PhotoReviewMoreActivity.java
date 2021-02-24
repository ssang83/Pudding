package com.enliple.pudding.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

import com.enliple.pudding.AbsBaseActivity;
import com.enliple.pudding.R;
import com.enliple.pudding.adapter.shoptree.PhotoReviewMoreAdapter;
import com.enliple.pudding.commons.app.NetworkStatusUtils;
import com.enliple.pudding.commons.app.Utils;
import com.enliple.pudding.commons.db.DBManager;
import com.enliple.pudding.commons.log.Logger;
import com.enliple.pudding.commons.network.NetworkApi;
import com.enliple.pudding.commons.network.NetworkBus;
import com.enliple.pudding.commons.network.NetworkBusResponse;
import com.enliple.pudding.commons.network.vo.API46;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhotoReviewMoreActivity extends AbsBaseActivity {
    private RecyclerView recyclerView;
    private RelativeLayout buttonClose;
    private PhotoReviewMoreAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private String photoReviewKey;
    private String it_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_review_more);

        recyclerView = findViewById(R.id.recyclerView);
        buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int imageWidth = (displayMetrics.widthPixels - Utils.ConvertDpToPx(PhotoReviewMoreActivity.this, 12)) / 3;
        adapter = new PhotoReviewMoreAdapter(PhotoReviewMoreActivity.this, imageWidth);

        gridLayoutManager = new GridLayoutManager(PhotoReviewMoreActivity.this, 3);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
//        ArrayList<String> dataArray = new ArrayList<String>();
//        for ( int i = 0 ; i < 33 ; i ++ ) {
//            dataArray.add("https://images.shop-tree.com/images/stores/7kQRzGJ8go/products/1612927034721825.jpg");
//        }
//        adapter.setItems(dataArray);

        Intent intent = getIntent();
        it_id = intent.getStringExtra("it_id");
        if (!TextUtils.isEmpty(it_id)) {
            EventBus.getDefault().register(this);
            getPhotoReview();
        } else {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NetworkBusResponse data) {
        Logger.e("photoReviewKey :: " + photoReviewKey);
        Logger.e("data.arg1 :: " + data.arg1);
        if (data.arg1.equals(photoReviewKey)) {
            String str = DBManager.getInstance(PhotoReviewMoreActivity.this).get(data.arg1);
            API46 response = new Gson().fromJson(str, API46.class);

            List<API46.ReviewItem> dataArray = response.data;
            adapter.setItems(dataArray);
        }
    }

    private void getPhotoReview() {
        photoReviewKey = NetworkApi.API46.toString() + "?it_id=" + it_id;
        NetworkBus bus = new NetworkBus(NetworkApi.API46.name(), it_id);
        EventBus.getDefault().post(bus);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNetworkStatusChanged(@NotNull NetworkStatusUtils.NetworkStatus status) {

    }

    @Override
    public void onDozeModeStateChanged(boolean dozeEnable) {

    }
}
